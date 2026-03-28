package sudark2.Sudark.rentLandPro.Listener;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import sudark2.Sudark.rentLandPro.File.LandInfoManager;
import sudark2.Sudark.rentLandPro.File.LandMembersManager;
import sudark2.Sudark.rentLandPro.OneBotRelated.OneBotApi;
import sudark2.Sudark.rentLandPro.Util.ChunkKeyUtil;
import sudark2.Sudark.rentLandPro.Util.IdentityUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static sudark2.Sudark.rentLandPro.File.LandInfoManager.landInfoMap;
import static sudark2.Sudark.rentLandPro.RentLandPro.get;

public class LandCreationListener implements Listener {

    // 玩家正在编辑的领地ID（书与笔模式）
    public static final ConcurrentHashMap<String, Long> editingLand = new ConcurrentHashMap<>();

    // 粒子效果任务
    private static final ConcurrentHashMap<Long, BukkitRunnable> particleTasks = new ConcurrentHashMap<>();

    // 橙黄渐变粒子
    private static final Particle.DustTransition DUST_TRANSITION = new Particle.DustTransition(
            Color.ORANGE, Color.YELLOW, 1.0f);

    @EventHandler
    public void onPlayerUseBookOnGround(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getItem() == null) return;
        if (event.getItem().getType() != Material.WRITABLE_BOOK) return;
        if (event.getClickedBlock() == null) return;

        event.setCancelled(true);

        Player pl = event.getPlayer();
        String playerName = pl.getName();
        String qq = IdentityUtil.getUserQQ(playerName);
        Chunk chunk = event.getClickedBlock().getChunk();
        Long chunkKey = ChunkKeyUtil.genKey(chunk);

        // 检查该区块是否已被其他领地占用
        for (LandInfoManager.LandInfo info : landInfoMap.values()) {
            for (Long existingChunk : info.getLandPile()) {
                if (existingChunk.equals(chunkKey)) {
                    pl.sendMessage("§e该区块已被领地 §b" + info.getLandName() + " §e占用");
                    return;
                }
            }
        }

        Long editingLandId = editingLand.get(playerName);

        if (editingLandId == null) {
            // 创建新领地
            createNewLand(pl, qq, chunkKey, chunk);
        } else {
            // 扩展现有领地
            expandLand(pl, editingLandId, chunkKey, chunk);
        }
    }

    private void createNewLand(Player pl, String qq, Long chunkKey, Chunk chunk) {
        String landName = "新领地-" + System.currentTimeMillis() % 10000;
        int defaultDuration = 30;
        Material signature = Material.GRASS_BLOCK;
        int[] teleportPoint = {chunk.getX() * 16 + 8, 64, chunk.getZ() * 16 + 8};

        LandInfoManager.LandInfo newLand = new LandInfoManager.LandInfo(
                landName, qq, defaultDuration, signature, new Long[]{chunkKey}, teleportPoint);

        landInfoMap.put(chunkKey, newLand);

        // 创建成员关系，地主自动成为 operator
        Set<String> ops = new HashSet<>();
        ops.add(qq);
        LandMembersManager.landMembers.put(chunkKey, new LandMembersManager.LandMembership(ops, new HashSet<>()));

        // 保存
        sudark2.Sudark.rentLandPro.File.BinaryEditor.saveAll();

        // 更新所有在线玩家的拒绝区块集合
        GeneralListener.rebuildAllPlayersDenyChunks();

        // 进入编辑模式
        editingLand.put(pl.getName(), chunkKey);

        // 启动粒子效果
        startParticleEffect(chunkKey, chunk.getWorld());

        pl.sendMessage("§b领地创建成功！§f再次点击其他区块可扩展领地");
        pl.sendMessage("§7输入 §e/land confirm §7完成创建");

        // 发送群通知
        OneBotApi.sendLandCreatedNotice(pl.getName(), qq, 1, defaultDuration);
    }

    private void expandLand(Player pl, Long landId, Long chunkKey, Chunk chunk) {
        LandInfoManager.LandInfo info = landInfoMap.get(landId);
        if (info == null) {
            editingLand.remove(pl.getName());
            pl.sendMessage("§e领地不存在，请重新创建");
            return;
        }

        // 检查重复
        for (Long existingChunk : info.getLandPile()) {
            if (existingChunk.equals(chunkKey)) {
                pl.sendMessage("§e该区块已在领地范围内");
                return;
            }
        }

        // 扩展领地
        info.addChunkToPile(chunkKey);

        // 更新所有在线玩家的拒绝区块集合
        GeneralListener.updateDenyChunksForLand(landId, new Long[]{chunkKey});

        // 启动该区块的粒子效果
        startParticleEffect(chunkKey, chunk.getWorld());

        pl.sendMessage("§b区块已添加到领地！§f当前领地面积: §e" + info.getLandPile().length + " §7区块");
    }

    public static void confirmLandCreation(Player pl) {
        String playerName = pl.getName();
        Long landId = editingLand.remove(playerName);

        if (landId == null) {
            pl.sendMessage("§e您当前没有正在编辑的领地");
            return;
        }

        // 停止所有该领地的粒子效果
        LandInfoManager.LandInfo info = landInfoMap.get(landId);
        if (info != null) {
            for (Long chunkKey : info.getLandPile()) {
                stopParticleEffect(chunkKey);
            }
            pl.sendMessage("§b领地 §e" + info.getLandName() + " §b创建完成！");
            pl.sendMessage("§7面积: §e" + info.getLandPile().length + " §7区块");
        }
    }

    public static void cancelLandCreation(Player pl) {
        String playerName = pl.getName();
        Long landId = editingLand.remove(playerName);

        if (landId != null) {
            LandInfoManager.LandInfo info = landInfoMap.get(landId);
            if (info != null) {
                for (Long chunkKey : info.getLandPile()) {
                    stopParticleEffect(chunkKey);
                }
            }
            pl.sendMessage("§e已退出编辑模式");
        }
    }

    private void startParticleEffect(Long chunkKey, World world) {
        int chunkX = (int) (chunkKey >> 32);
        int chunkZ = (int) (chunkKey & 0xFFFFFFFFL);

        double centerX = chunkX * 16 + 8;
        double centerZ = chunkZ * 16 + 8;

        BukkitRunnable task = new BukkitRunnable() {
            double y = 60;
            boolean goingUp = true;

            @Override
            public void run() {
                Location loc = new Location(world, centerX, y, centerZ);
                world.spawnParticle(Particle.DUST_COLOR_TRANSITION, loc, 5, 0.5, 0.5, 0.5, DUST_TRANSITION);

                if (goingUp) {
                    y += 0.5;
                    if (y >= 80) goingUp = false;
                } else {
                    y -= 0.5;
                    if (y <= 60) goingUp = true;
                }
            }
        };

        task.runTaskTimer(get(), 0L, 5L);
        particleTasks.put(chunkKey, task);
    }

    private static void stopParticleEffect(Long chunkKey) {
        BukkitRunnable task = particleTasks.remove(chunkKey);
        if (task != null) {
            task.cancel();
        }
    }
}
