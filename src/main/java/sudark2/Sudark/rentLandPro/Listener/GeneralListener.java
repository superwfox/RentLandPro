package sudark2.Sudark.rentLandPro.Listener;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import sudark2.Sudark.rentLandPro.File.LandInfoManager;
import sudark2.Sudark.rentLandPro.File.LandMembersManager;
import sudark2.Sudark.rentLandPro.InventoryMenu.LandMembersMenu;
import sudark2.Sudark.rentLandPro.OneBotRelated.OneBotApi;
import sudark2.Sudark.rentLandPro.Util.ChunkKeyUtil;
import sudark2.Sudark.rentLandPro.Util.IdentityUtil;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static sudark2.Sudark.rentLandPro.File.LandFunctionsManager.*;
import static sudark2.Sudark.rentLandPro.File.LandInfoManager.landInfoMap;
import static sudark2.Sudark.rentLandPro.File.LandMembersManager.landMembers;
import static sudark2.Sudark.rentLandPro.InventoryMenu.LandMembersMenu.InventoryTempStorage;

public class GeneralListener implements Listener {

    // 玩家的拒绝区块集合：玩家名 -> DenyChunkSets
    public static final ConcurrentHashMap<String, DenyChunkSets> playerDenyChunks = new ConcurrentHashMap<>();

    // 游客进入领地的冷却时间（毫秒）
    private static final long VISITOR_ALERT_COOLDOWN = 2 * 60 * 1000;
    private static final ConcurrentHashMap<String, Long> visitorAlertCooldown = new ConcurrentHashMap<>();

    public record DenyChunkSets(
            Set<Long> denyBreak,
            Set<Long> denyPlace,
            Set<Long> denyInteract,
            Set<Long> denyExplode
    ) {
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player pl = event.getPlayer();
        rebuildPlayerDenyChunks(pl.getName());
    }

    /**
     * 为指定玩家重建拒绝区块集合
     */
    public static void rebuildPlayerDenyChunks(String playerName) {
        String playerQQ = IdentityUtil.getUserQQ(playerName);

        Set<Long> denyBreak = new HashSet<>();
        Set<Long> denyPlace = new HashSet<>();
        Set<Long> denyInteract = new HashSet<>();
        Set<Long> denyExplode = new HashSet<>();

        // 遍历所有领地
        for (LandInfoManager.LandInfo info : landInfoMap.values()) {
            Long landId = info.getLandId();

            // 检查玩家是否是该领地成员
            boolean isMember = false;
            LandMembersManager.LandMembership membership = landMembers.get(landId);
            if (membership != null) {
                isMember = membership.operators().contains(playerQQ) || membership.members().contains(playerQQ);
            }

            // 如果是成员，不需要加入拒绝集合
            if (isMember) continue;

            // 获取该领地的功能开关
            int flags = landFunctionFlags.getOrDefault(landId, 0);

            // 遍历该领地所有区块
            for (Long chunkKey : info.getLandPile()) {
                // 如果功能关闭（外人不可执行），加入拒绝集合
                if ((flags & FLAG_BREAK) == 0) denyBreak.add(chunkKey);
                if ((flags & FLAG_PLACE) == 0) denyPlace.add(chunkKey);
                if ((flags & FLAG_INTERACT) == 0) denyInteract.add(chunkKey);
                if ((flags & FLAG_EXPLODE) == 0) denyExplode.add(chunkKey);
            }
        }

        playerDenyChunks.put(playerName, new DenyChunkSets(denyBreak, denyPlace, denyInteract, denyExplode));
    }

    /**
     * 更新所有在线玩家的拒绝区块集合（领地创建/删除/功能修改时调用）
     */
    public static void rebuildAllPlayersDenyChunks() {
        for (String playerName : playerDenyChunks.keySet()) {
            rebuildPlayerDenyChunks(playerName);
        }
    }

    /**
     * 针对特定领地更新所有玩家的拒绝区块（优化版，只更新受影响的区块）
     */
    public static void updateDenyChunksForLand(Long landId, Long[] chunkKeys) {
        LandInfoManager.LandInfo info = landInfoMap.get(landId);
        if (info == null) {
            // 领地被删除，从所有玩家的拒绝集合中移除这些区块
            for (DenyChunkSets sets : playerDenyChunks.values()) {
                for (Long chunkKey : chunkKeys) {
                    sets.denyBreak.remove(chunkKey);
                    sets.denyPlace.remove(chunkKey);
                    sets.denyInteract.remove(chunkKey);
                    sets.denyExplode.remove(chunkKey);
                }
            }
            return;
        }

        // 领地存在，重新计算
        int flags = landFunctionFlags.getOrDefault(landId, 0);
        LandMembersManager.LandMembership membership = landMembers.get(landId);

        for (var entry : playerDenyChunks.entrySet()) {
            String playerName = entry.getKey();
            String playerQQ = IdentityUtil.getUserQQ(playerName);
            DenyChunkSets sets = entry.getValue();

            // 检查是否是成员
            boolean isMember = membership != null &&
                    (membership.operators().contains(playerQQ) || membership.members().contains(playerQQ));

            for (Long chunkKey : chunkKeys) {
                if (isMember) {
                    // 成员不受限制，移除拒绝
                    sets.denyBreak.remove(chunkKey);
                    sets.denyPlace.remove(chunkKey);
                    sets.denyInteract.remove(chunkKey);
                    sets.denyExplode.remove(chunkKey);
                } else {
                    // 非成员，根据功能开关决定
                    if ((flags & FLAG_BREAK) == 0) sets.denyBreak.add(chunkKey);
                    else sets.denyBreak.remove(chunkKey);

                    if ((flags & FLAG_PLACE) == 0) sets.denyPlace.add(chunkKey);
                    else sets.denyPlace.remove(chunkKey);

                    if ((flags & FLAG_INTERACT) == 0) sets.denyInteract.add(chunkKey);
                    else sets.denyInteract.remove(chunkKey);

                    if ((flags & FLAG_EXPLODE) == 0) sets.denyExplode.add(chunkKey);
                    else sets.denyExplode.remove(chunkKey);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getChunk().equals(event.getTo().getChunk())) return;

        Player pl = event.getPlayer();
        Chunk toChunk = event.getTo().getChunk();
        Long chunkKey = ChunkKeyUtil.genKey(toChunk);

        LandInfoManager.LandInfo landInfo = landInfoMap.get(chunkKey);
        if (landInfo == null) return;

        String playerQQ = IdentityUtil.getUserQQ(pl.getName());
        String ownerQQ = landInfo.getLandOwnerQQ();

        // 检查是否是领地成员
        LandMembersManager.LandMembership membership = landMembers.get(landInfo.getLandId());
        if (membership != null) {
            if (membership.operators().contains(playerQQ) || membership.members().contains(playerQQ)) {
                return;
            }
        }

        // 是游客，检查冷却时间
        String cooldownKey = playerQQ + ":" + landInfo.getLandId();
        Long lastAlert = visitorAlertCooldown.get(cooldownKey);
        long now = System.currentTimeMillis();

        if (lastAlert == null || now - lastAlert > VISITOR_ALERT_COOLDOWN) {
            visitorAlertCooldown.put(cooldownKey, now);
            OneBotApi.sendVisitorAlert(ownerQQ, pl.getName(), landInfo.getLandName());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String name = player.getName();

        // 清理玩家数据
        playerDenyChunks.remove(name);

        if (InventoryTempStorage.containsKey(name)) {
            player.getInventory().setContents(InventoryTempStorage.get(name));
            InventoryTempStorage.remove(name);
        }
        LandMembersMenu.editingLandId.remove(name);
        LandMembersMenu.currentPage.remove(name);
        LandCreationListener.editingLand.remove(name);
        LandDetailsMenuListener.editingLandInfo.remove(name);
        LandDetailsMenuListener.anvilInputMode.remove(name);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player pl = event.getPlayer();
        if (LandDetailsMenuListener.anvilInputMode.containsKey(pl.getName())) {
            event.setCancelled(true);
            LandDetailsMenuListener.handleChatInput(pl, event.getMessage());
        }
    }

    @EventHandler
    public void onPlayerBreak(BlockBreakEvent event) {
        Player pl = event.getPlayer();
        Long chunkKey = ChunkKeyUtil.genKey(event.getBlock().getChunk());

        DenyChunkSets sets = playerDenyChunks.get(pl.getName());
        if (sets != null && sets.denyBreak.contains(chunkKey)) {
            event.setCancelled(true);
            pl.sendMessage("§e你没有在此领地破坏方块的权限");
        }
    }

    @EventHandler
    public void onPlayerPlace(BlockPlaceEvent event) {
        Player pl = event.getPlayer();
        Long chunkKey = ChunkKeyUtil.genKey(event.getBlock().getChunk());

        DenyChunkSets sets = playerDenyChunks.get(pl.getName());
        if (sets != null && sets.denyPlace.contains(chunkKey)) {
            event.setCancelled(true);
            pl.sendMessage("§e你没有在此领地放置方块的权限");
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;

        Player pl = event.getPlayer();
        Long chunkKey = ChunkKeyUtil.genKey(event.getClickedBlock().getChunk());

        DenyChunkSets sets = playerDenyChunks.get(pl.getName());
        if (sets != null && sets.denyInteract.contains(chunkKey)) {
            event.setCancelled(true);
            pl.sendMessage("§e你没有在此领地交互的权限");
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        // 移除所有在受保护领地内的受影响方块
        event.blockList().removeIf(block -> {
            Long chunkKey = ChunkKeyUtil.genKey(block.getChunk());
            LandInfoManager.LandInfo landInfo = landInfoMap.get(chunkKey);
            if (landInfo == null) return false;

            // 检查该领地是否禁止爆炸
            int flags = landFunctionFlags.getOrDefault(landInfo.getLandId(), 0);
            return (flags & FLAG_EXPLODE) == 0;
        });
    }
}
