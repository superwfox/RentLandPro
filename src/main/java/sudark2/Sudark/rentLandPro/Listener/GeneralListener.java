package sudark2.Sudark.rentLandPro.Listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import static sudark2.Sudark.rentLandPro.InventoryMenu.LandMembersMenu.InventoryTempStorage;

public class GeneralListener implements Listener {

    @EventHandler
    public void onPlayerJoin() {

    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (InventoryTempStorage.containsKey(player.getName())) {
            player.getInventory().setContents(InventoryTempStorage.get(player.getName()));
        }

    }

    @EventHandler
    public void onPlayerDig(PlayerInteractEvent event) {

    }

    @EventHandler
    public void onPlayerBreak(BlockBreakEvent event) {

    }

    @EventHandler
    public void onPlayerPlace(BlockPlaceEvent event) {

    }

    @EventHandler
    public void onPlayerInteract(BlockExplodeEvent event) {

    }

}
