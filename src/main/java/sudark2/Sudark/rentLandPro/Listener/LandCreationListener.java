package sudark2.Sudark.rentLandPro.Listener;

import org.bukkit.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import sudark2.Sudark.rentLandPro.File.BinaryEditor;
import sudark2.Sudark.rentLandPro.File.ConfigManager;
import sudark2.Sudark.rentLandPro.File.LandFunctionsManager;
import sudark2.Sudark.rentLandPro.File.LandInfoManager;
import sudark2.Sudark.rentLandPro.File.LandMembersManager;
import sudark2.Sudark.rentLandPro.OneBotRelated.OneBotApi;
import sudark2.Sudark.rentLandPro.Util.ChunkKeyUtil;
import sudark2.Sudark.rentLandPro.Util.GlassUtil;
import sudark2.Sudark.rentLandPro.Util.IdentityUtil;
import sudark2.Sudark.rentLandPro.Util.ItemUtil;
import sudark2.Sudark.rentLandPro.Util.LocationUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static sudark2.Sudark.rentLandPro.File.LandInfoManager.landInfoMap;
import static sudark2.Sudark.rentLandPro.RentLandPro.get;

public class LandCreationListener implements Listener {

    public static final ConcurrentHashMap<String, Long> editingLand = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, LinkedHashSet<Long>> pendingChunks = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Long, BukkitRunnable> particleTasks = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, World> pendingWorlds = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, Set<Long>> originalChunks = new ConcurrentHashMap<>();

    // 框选法相关数据结构
    private static final ConcurrentHashMap<String, int[]> frameSelectionCorner1 = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Item> frameSelectionMarker1 = new ConcurrentHashMap<>();

    // 超时任务存储
    private static final ConcurrentHashMap<String, BukkitRunnable> creationTimeoutTasks = new ConcurrentHashMap<>();
    private static final long CREATION_TIMEOUT_TICKS = 20L * 60 * 5; // 5分钟 = 6000 ticks

    private static final Particle.DustTransition DUST_TRANSITION = new Particle.DustTransition(
            Color.ORANGE, Color.YELLOW, 5.0f);

    private static final int[] DX = {-1, 0, 1, -1, 1, -1, 0, 1};
    private static final int[] DZ = {-1, -1, -1, 0, 0, 1, 1, 1};

    @EventHandler
    public void onPlayerUseMap(PlayerInteractEvent event) {
        if (event.getItem() == null) return;
        if (event.getItem().getType() != Material.MAP && event.getItem().getType() != Material.FILLED_MAP) return;

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_BLOCK && action != Action.LEFT_CLICK_BLOCK) return;

        event.setCancelled(true);

        Player pl = event.getPlayer();
        String playerName = pl.getName();
        Chunk chunk = pl.getLocation().getChunk();
        Long chunkKey = ChunkKeyUtil.genKey(chunk);

        boolean isRightClick = (action == Action.RIGHT_CLICK_BLOCK);

        if (isRightClick) {
            handleAddChunk(pl, playerName, chunkKey, chunk);
        } else {
            handleRemoveChunk(pl, playerName, chunkKey);
        }
    }

    @EventHandler
    public void onPlayerDropMap(PlayerDropItemEvent event) {
        ItemStack droppedItem = event.getItemDrop().getItemStack();
        if (droppedItem.getType() != Material.MAP && droppedItem.getType() != Material.FILLED_MAP) return;

        Player pl = event.getPlayer();
        String playerName = pl.getName();

        // 检查玩家是否正在编辑领地
        Long editingLandId = editingLand.get(playerName);
        if (editingLandId == null) return;

        event.setCancelled(true);

        // 在玩家direction前0.5格位置生成标记实体
        Location eyeLoc = pl.getEyeLocation();
        Vector direction = eyeLoc.getDirection().normalize().multiply(0.5);
        Location markerLoc = eyeLoc.add(direction);

        // 记录当前区块坐标作为对角
        Chunk chunk = pl.getLocation().getChunk();
        int[] currentCorner = {chunk.getX(), chunk.getZ()};

        // 检查是否已有第一个对角
        int[] corner1 = frameSelectionCorner1.get(playerName);
        if (corner1 == null) {
            // 记录第一个对角
            Item markerItem = pl.getWorld().dropItem(markerLoc, new ItemStack(Material.FILLED_MAP));
            markerItem.setPickupDelay(Integer.MAX_VALUE);
            markerItem.setVelocity(new Vector(0, 0, 0));
            markerItem.setGlowing(true);

            frameSelectionCorner1.put(playerName, currentCorner);
            frameSelectionMarker1.put(playerName, markerItem);

            // 5秒后自动移除标记
            new BukkitRunnable() {
                @Override
                public void run() {
                    Item marker = frameSelectionMarker1.remove(playerName);
                    if (marker != null && !marker.isDead()) {
                        marker.remove();
                    }
                    frameSelectionCorner1.remove(playerName);
                }
            }.runTaskLater(get(), 240 * 20L);

            pl.sendMessage("§b已记录第一个对角 §e(" + currentCorner[0] + ", " + currentCorner[1] + ")");
            pl.sendMessage("[ 请在4分钟内丢出地图选择第二个对角 ]");
        } else {
            // 记录第二个对角并执行框选
            Item marker1 = frameSelectionMarker1.remove(playerName);
            if (marker1 != null && !marker1.isDead()) {
                marker1.remove();
            }
            frameSelectionCorner1.remove(playerName);

            // 生成第二个标记（也会在5秒后消失）
            Item markerItem2 = pl.getWorld().dropItem(markerLoc, new ItemStack(Material.FILLED_MAP));
            markerItem2.setPickupDelay(Integer.MAX_VALUE);
            markerItem2.setVelocity(new Vector(0, 0, 0));
            markerItem2.setGravity(false);
            markerItem2.setGlowing(true);

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!markerItem2.isDead()) {
                        markerItem2.remove();
                    }
                }
            }.runTaskLater(get(), 100L);

            pl.sendMessage("§b已记录第二个对角 §7(" + currentCorner[0] + ", " + currentCorner[1] + ")");

            // 框选两对角间所有的区块
            int minX = Math.min(corner1[0], currentCorner[0]);
            int maxX = Math.max(corner1[0], currentCorner[0]);
            int minZ = Math.min(corner1[1], currentCorner[1]);
            int maxZ = Math.max(corner1[1], currentCorner[1]);

            int addedCount = 0;
            int skippedCount = 0;
            World world = pl.getWorld();

            for (int cx = minX; cx <= maxX; cx++) {
                for (int cz = minZ; cz <= maxZ; cz++) {
                    long chunkKey = ((long) cx << 32) | (cz & 0xFFFFFFFFL);

                    // 检查是否已被其他领地占用
                    boolean occupied = false;
                    for (LandInfoManager.LandInfo info : landInfoMap.values()) {
                        if (!info.getLandId().equals(editingLandId)) {
                            for (Long existingChunk : info.getLandPile()) {
                                if (existingChunk.equals(chunkKey)) {
                                    occupied = true;
                                    break;
                                }
                            }
                        }
                        if (occupied) break;
                    }

                    if (occupied) {
                        skippedCount++;
                        continue;
                    }

                    LinkedHashSet<Long> pending = pendingChunks.get(playerName);
                    if (pending == null) {
                        pending = new LinkedHashSet<>();
                        pendingChunks.put(playerName, pending);
                    }

                    if (!pending.contains(chunkKey)) {
                        pending.add(chunkKey);
                        startParticleEffect(chunkKey, world);
                        GlassUtil.placeGlass(chunkKey, world);
                        addedCount++;
                    }
                }
            }

            pl.sendMessage("§b框选完成！§7添加了 §e" + addedCount + " §7个区块" +
                    (skippedCount > 0 ? "，§c跳过 " + skippedCount + " 个已占用区块" : ""));
            pl.sendMessage("§7输入 §e/land confirm §7完成 | §e/land cancel §7取消");
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        String name = event.getPlayer().getName();
        
        // 取消超时任务
        cancelCreationTimeout(name);
        
        World world = pendingWorlds.get(name);
        Set<Long> pending = pendingChunks.get(name);
        if (pending != null && world != null) {
            for (Long ck : pending) {
                GlassUtil.removeGlass(ck, world);
                stopParticleEffect(ck);
            }
        }
        pendingWorlds.remove(name);

        // 清理框选法相关数据
        Item marker = frameSelectionMarker1.remove(name);
        if (marker != null && !marker.isDead()) {
            marker.remove();
        }
        frameSelectionCorner1.remove(name);
    }

    private void handleAddChunk(Player pl, String playerName, Long chunkKey, Chunk chunk) {
        for (LandInfoManager.LandInfo info : landInfoMap.values()) {
            for (Long existingChunk : info.getLandPile()) {
                if (existingChunk.equals(chunkKey)) {
                    pl.sendMessage("§7该区块已被领地 §e" + info.getLandName() + " §7占用");
                    return;
                }
            }
        }

        Long editingLandId = editingLand.get(playerName);

        if (editingLandId == null) {
            String qq = IdentityUtil.getUserQQ(playerName);
            if (qq == null) {
                pl.sendMessage("§e您尚未绑定QQ，无法创建领地");
                return;
            }
            createNewLand(pl, qq, chunkKey, chunk);
            pl.setItemInHand(null);
            pl.getInventory().addItem(ItemUtil.createMap(pl.getLocation()));
        } else {
            expandLand(pl, editingLandId, chunkKey, chunk);
        }
    }

    private void handleRemoveChunk(Player pl, String playerName, Long chunkKey) {
        Long editingLandId = editingLand.get(playerName);
        if (editingLandId == null) {
            pl.sendMessage("§7您当前没有正在编辑的领地");
            return;
        }

        Set<Long> pending = pendingChunks.get(playerName);
        if (pending == null || !pending.contains(chunkKey)) {
            pl.sendMessage("§7该区块不在待添加列表中");
            return;
        }

        if (chunkKey.equals(editingLandId)) {
            pl.sendMessage("§e无法移除领地的起始区块");
            return;
        }

        pending.remove(chunkKey);
        stopParticleEffect(chunkKey);
        GlassUtil.removeGlass(chunkKey, pl.getWorld());
        pl.sendMessage("§7区块已移除 §7| §7剩余: §e" + pending.size() + " §7区块");
    }

    private boolean isAdjacentToExisting(Long chunkKey, Set<Long> existingChunks) {
        if (existingChunks.isEmpty()) return true;

        int chunkX = (int) (chunkKey >> 32);
        int chunkZ = (int) (chunkKey & 0xFFFFFFFFL);

        for (int i = 0; i < 8; i++) {
            int adjX = chunkX + DX[i];
            int adjZ = chunkZ + DZ[i];
            long adjKey = ((long) adjX << 32) | (adjZ & 0xFFFFFFFFL);

            if (existingChunks.contains(adjKey)) {
                return true;
            }
        }
        return false;
    }

    private void createNewLand(Player pl, String qq, Long chunkKey, Chunk chunk) {
        String landName = "新领地-" + System.currentTimeMillis() % 10000;
        Material signature = Material.GRASS_BLOCK;
        int[] teleportPoint = {chunk.getX() * 16 + 8, chunk.getZ() * 16 + 8};

        LandInfoManager.LandInfo newLand = new LandInfoManager.LandInfo(
                chunkKey, landName, qq, 1, signature, new Long[]{chunkKey}, teleportPoint);

        landInfoMap.put(chunkKey, newLand);

        Set<String> ops = new HashSet<>();
        ops.add(qq);
        LandMembersManager.landMembers.put(chunkKey, new LandMembersManager.LandMembership(ops, new HashSet<>()));

        LinkedHashSet<Long> pending = new LinkedHashSet<>();
        pending.add(chunkKey);
        pendingChunks.put(pl.getName(), pending);
        editingLand.put(pl.getName(), chunkKey);
        pendingWorlds.put(pl.getName(), chunk.getWorld());

        startParticleEffect(chunkKey, chunk.getWorld());
        GlassUtil.placeGlass(chunkKey, chunk.getWorld());

        pl.sendMessage("§7右键 §f添加区块 §7| §7左键 §f取消区块");
        pl.sendMessage("§7输入 §e/land confirm [天数:可选] §7完成创建 | §e/land cancel§7 取消");
        pl.sendMessage("[ 5分钟内未完成将自动取消 ]");

        // 启动超时任务
        startCreationTimeout(pl.getName());
    }

    private void expandLand(Player pl, Long landId, Long chunkKey, Chunk chunk) {
        LandInfoManager.LandInfo info = landInfoMap.get(landId);
        if (info == null) {
            editingLand.remove(pl.getName());
            pendingChunks.remove(pl.getName());
            pendingWorlds.remove(pl.getName());
            pl.sendMessage("§e领地不存在，请重新创建");
            return;
        }

        LinkedHashSet<Long> pending =  pendingChunks.get(pl.getName());
        if (pending == null) {
            pending = new LinkedHashSet<>();
            for (Long existingChunk : info.getLandPile()) {
                pending.add(existingChunk);
            }
            pendingChunks.put(pl.getName(), pending);
        }

        if (pending.contains(chunkKey)) {
            pl.sendMessage("§7该区块已在待添加列表中");
            return;
        }

        if (!isAdjacentToExisting(chunkKey, pending)) {
            pl.sendMessage("§e新区块必须与现有区块相邻");
            return;
        }

        pending.add(chunkKey);
        startParticleEffect(chunkKey, chunk.getWorld());
        GlassUtil.placeGlass(chunkKey, chunk.getWorld());

        pl.sendMessage("§7区块已添加 §7| §7当前: §e" + pending.size() + " §7区块");
    }

    public static void confirmLandCreation(Player pl, int days) {
        String playerName = pl.getName();
        cancelCreationTimeout(playerName);
        Long landId = editingLand.remove(playerName);
        LinkedHashSet<Long> pending = pendingChunks.remove(playerName);
        World world = pendingWorlds.remove(playerName);
        Set<Long> original = originalChunks.remove(playerName);

        if (landId == null) {
            pl.sendMessage("§7您当前没有正在编辑的领地");
            return;
        }

        LandInfoManager.LandInfo info = landInfoMap.get(landId);
        if (info == null || pending == null) return;

        boolean isReshape = (original != null);
        int duration;
        int billableChunks;

        if (isReshape) {
            duration = info.getlandDuration();
            billableChunks = 0;
            for (Long ck : pending) {
                if (!original.contains(ck)) billableChunks++;
            }
        } else {
            duration = days;
            billableChunks = pending.size();
            info.setLandDuration(days);
        }

        int cost = ConfigManager.calculateRentCost(billableChunks, duration);
        if (cost > 0 && pl.getLevel() < cost) {
            pl.sendMessage("§e经验不足！需要 §b" + cost + " §e级经验");
            editingLand.put(playerName, landId);
            pendingChunks.put(playerName, pending);
            if (world != null) pendingWorlds.put(playerName, world);
            if (original != null) originalChunks.put(playerName, original);
            return;
        }
        if (cost > 0) pl.setLevel(pl.getLevel() - cost);

        Long[] newPile = pending.toArray(new Long[0]);
        info.setLandPile(newPile);
        LandFunctionsManager.landFunctionFlags.putIfAbsent(landId, 0);

        for (Long chunkKey : pending) {
            stopParticleEffect(chunkKey);
            if (world != null) GlassUtil.removeGlass(chunkKey, world);
        }

        if (!isReshape && pl.getInventory().getItemInMainHand().getType() == Material.FILLED_MAP) {
            pl.getInventory().setItemInMainHand(null);
        }

        BinaryEditor.saveAll();
        GeneralListener.rebuildAllPlayersDenyChunks();
        LandFunctionsManager.rebuildWorldSets();

        if (isReshape) {
            pl.sendMessage("§b领地 §e" + info.getLandName() + " §b范围已更新！");
            pl.sendMessage("§7面积: §e" + pending.size() + " §7区块 §7| §7新增: §e" + billableChunks + " §7| §7花费: §e" + cost + " §7级经验");
        } else {
            String ownerQQ = info.getLandOwnerQQ();
            OneBotApi.sendLandCreatedNotice(playerName, ownerQQ, pending.size(), duration);
            pl.sendMessage("§b领地 §e" + info.getLandName() + " §b创建完成！");
            pl.sendMessage("§7面积: §e" + pending.size() + " §7区块 §7| §7租期: §e" + duration + " §7天 §7| §7花费: §e" + cost + " §7级经验");
        }
    }

    public static void cancelLandCreation(Player pl) {
        String playerName = pl.getName();
        cancelCreationTimeout(playerName);
        Long landId = editingLand.remove(playerName);
        Set<Long> pending = pendingChunks.remove(playerName);
        World world = pendingWorlds.remove(playerName);
        Set<Long> original = originalChunks.remove(playerName);

        if (landId == null) {
            pl.sendMessage("§7您当前没有正在编辑的领地");
            return;
        }

        if (pending != null) {
            for (Long chunkKey : pending) {
                stopParticleEffect(chunkKey);
                if (world != null) GlassUtil.removeGlass(chunkKey, world);
            }
        }

        if (original != null) {
            LandInfoManager.LandInfo info = landInfoMap.get(landId);
            if (info != null) {
                info.setLandPile(original.toArray(new Long[0]));
            }
            pl.sendMessage("§7已取消领地范围修改");
        } else {
            landInfoMap.remove(landId);
            LandMembersManager.landMembers.remove(landId);
            pl.sendMessage("§7已取消领地创建");
        }
    }

    public static void cleanupAllPending() {
        for (var entry : pendingChunks.entrySet()) {
            World world = pendingWorlds.get(entry.getKey());
            if (world == null) continue;
            for (Long ck : entry.getValue()) {
                GlassUtil.removeGlass(ck, world);
                stopParticleEffect(ck);
            }
        }
        // 取消所有超时任务
        for (BukkitRunnable task : creationTimeoutTasks.values()) {
            task.cancel();
        }
        creationTimeoutTasks.clear();
        pendingChunks.clear();
        editingLand.clear();
        pendingWorlds.clear();
        originalChunks.clear();
    }

    public static void startParticleEffectStatic(Long chunkKey, World world) {
        new LandCreationListener().startParticleEffect(chunkKey, world);
    }

    private void startParticleEffect(Long chunkKey, World world) {
        int chunkX = (int) (chunkKey >> 32);
        int chunkZ = (int) (chunkKey & 0xFFFFFFFFL);
        int centerX = chunkX * 16 + 8;
        int centerZ = chunkZ * 16 + 8;

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                int baseY = LocationUtil.getHighestSolidY(world, centerX, centerZ) + 1;
                for (int y = 0; y <= 20; y++) {
                    Location loc = new Location(world, centerX + 0.5, baseY + y, centerZ + 0.5);
                    world.spawnParticle(Particle.DUST_COLOR_TRANSITION, loc, 3,
                            0, 0, 0, DUST_TRANSITION);
                }
            }
        };

        task.runTaskTimer(get(), 0L, 15L);
        particleTasks.put(chunkKey, task);
    }

    private static void stopParticleEffect(Long chunkKey) {
        BukkitRunnable task = particleTasks.remove(chunkKey);
        if (task != null) {
            task.cancel();
        }
    }

    private static void startCreationTimeout(String playerName) {
        cancelCreationTimeout(playerName);

        BukkitRunnable timeoutTask = new BukkitRunnable() {
            @Override
            public void run() {
                creationTimeoutTasks.remove(playerName);

                Long landId = editingLand.remove(playerName);
                Set<Long> pending = pendingChunks.remove(playerName);
                World world = pendingWorlds.remove(playerName);
                Set<Long> original = originalChunks.remove(playerName);

                if (landId == null) return;

                // 清理玻璃和粒子效果
                if (pending != null && world != null) {
                    for (Long chunkKey : pending) {
                        stopParticleEffect(chunkKey);
                        GlassUtil.removeGlass(chunkKey, world);
                    }
                }

                // 如果是新建领地则删除，如果是修改则恢复原状
                if (original != null) {
                    LandInfoManager.LandInfo info = landInfoMap.get(landId);
                    if (info != null) {
                        info.setLandPile(original.toArray(new Long[0]));
                    }
                } else {
                    landInfoMap.remove(landId);
                    LandMembersManager.landMembers.remove(landId);
                }

                // 通知玩家
                Player pl = Bukkit.getPlayer(playerName);
                if (pl != null && pl.isOnline()) {
                    pl.sendMessage("§c领地创建超时，已自动取消");
                }
            }
        };

        timeoutTask.runTaskLater(get(), CREATION_TIMEOUT_TICKS);
        creationTimeoutTasks.put(playerName, timeoutTask);
    }

    private static void cancelCreationTimeout(String playerName) {
        BukkitRunnable task = creationTimeoutTasks.remove(playerName);
        if (task != null) {
            task.cancel();
        }
    }

    public static void resetCreationTimeout(String playerName) {
        if (editingLand.containsKey(playerName)) {
            startCreationTimeout(playerName);
        }
    }
}
