package sudark2.Sudark.rentLandPro.Listener;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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
import sudark2.Sudark.rentLandPro.File.LandFunctionsManager;
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

    public static final ConcurrentHashMap<String, DenyChunkSets> playerDenyChunks = new ConcurrentHashMap<>();
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

    public static void rebuildPlayerDenyChunks(String playerName) {
        String playerQQ = IdentityUtil.getUserQQ(playerName);

        Set<Long> denyBreak = new HashSet<>();
        Set<Long> denyPlace = new HashSet<>();
        Set<Long> denyInteract = new HashSet<>();
        Set<Long> denyExplode = new HashSet<>();

        for (LandInfoManager.LandInfo info : landInfoMap.values()) {
            Long landId = info.getLandId();

            boolean isMember = false;
            LandMembersManager.LandMembership membership = landMembers.get(landId);
            if (membership != null) {
                isMember = membership.operators().contains(playerQQ) || membership.members().contains(playerQQ);
            }

            if (isMember) continue;

            int flags = landFunctionFlags.getOrDefault(landId, 0);

            for (Long chunkKey : info.getLandPile()) {
                if ((flags & FLAG_BREAK) == 0) denyBreak.add(chunkKey);
                if ((flags & FLAG_PLACE) == 0) denyPlace.add(chunkKey);
                if ((flags & FLAG_INTERACT) == 0) denyInteract.add(chunkKey);
                if ((flags & FLAG_EXPLODE) == 0) denyExplode.add(chunkKey);
            }
        }

        playerDenyChunks.put(playerName, new DenyChunkSets(denyBreak, denyPlace, denyInteract, denyExplode));
    }

    public static void rebuildAllPlayersDenyChunks() {
        for (String playerName : playerDenyChunks.keySet()) {
            rebuildPlayerDenyChunks(playerName);
        }
    }

    public static void updateDenyChunksForLand(Long landId, Long[] chunkKeys) {
        LandInfoManager.LandInfo info = landInfoMap.get(landId);
        if (info == null) {
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

        int flags = landFunctionFlags.getOrDefault(landId, 0);
        LandMembersManager.LandMembership membership = landMembers.get(landId);

        for (var entry : playerDenyChunks.entrySet()) {
            String playerName = entry.getKey();
            String playerQQ = IdentityUtil.getUserQQ(playerName);
            DenyChunkSets sets = entry.getValue();

            boolean isMember = membership != null &&
                    (membership.operators().contains(playerQQ) || membership.members().contains(playerQQ));

            for (Long chunkKey : chunkKeys) {
                if (isMember) {
                    sets.denyBreak.remove(chunkKey);
                    sets.denyPlace.remove(chunkKey);
                    sets.denyInteract.remove(chunkKey);
                    sets.denyExplode.remove(chunkKey);
                } else {
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

        LandMembersManager.LandMembership membership = landMembers.get(landInfo.getLandId());
        if (membership != null) {
            if (membership.operators().contains(playerQQ) || membership.members().contains(playerQQ)) {
                return;
            }
        }

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

        playerDenyChunks.remove(name);

        if (InventoryTempStorage.containsKey(name)) {
            ItemStack[] stored = InventoryTempStorage.remove(name);
            if (stored != null) {
                for (int i = 0; i < stored.length; i++) {
                    player.getInventory().setItem(9 + i, stored[i]);
                }
            }
        }
        LandMembersMenu.editingLandId.remove(name);
        LandMembersMenu.currentPage.remove(name);
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
        event.blockList().removeIf(block -> {
            Long chunkKey = ChunkKeyUtil.genKey(block.getChunk());
            return LandFunctionsManager.denyExplodeChunks.contains(chunkKey);
        });
    }

    @EventHandler
    public void onEntityExplode(org.bukkit.event.entity.EntityExplodeEvent event) {
        event.blockList().removeIf(block -> {
            Long chunkKey = ChunkKeyUtil.genKey(block.getChunk());
            return LandFunctionsManager.denyExplodeChunks.contains(chunkKey);
        });
    }

    @EventHandler
    public void onFluidFlow(org.bukkit.event.block.BlockFromToEvent event) {
        Long chunkKey = ChunkKeyUtil.genKey(event.getToBlock().getChunk());
        if (LandFunctionsManager.denyFluidChunks.contains(chunkKey)) {
            event.setCancelled(true);
        }
    }
}
