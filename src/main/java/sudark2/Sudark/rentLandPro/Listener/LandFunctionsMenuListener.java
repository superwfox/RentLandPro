package sudark2.Sudark.rentLandPro.Listener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import sudark2.Sudark.rentLandPro.File.BinaryEditor;
import sudark2.Sudark.rentLandPro.File.LandFunctionsManager;
import sudark2.Sudark.rentLandPro.File.LandInfoManager;

import static sudark2.Sudark.rentLandPro.InventoryMenu.LandFunctionsMenu.*;

public class LandFunctionsMenuListener implements Listener {

    @EventHandler
    public void onFunctionsMenuDrag(InventoryDragEvent event) {
        if (!event.getView().getTitle().equals(landFunctionsMenuTitle)) return;

        Player pl = (Player) event.getWhoClicked();
        Long landId = editingLandId.get(pl.getName());
        if (landId == null) return;

        for (int slot : event.getNewItems().keySet()) {
            if (slot >= 27) continue;

            int col = getColumnBySlot(slot);
            int row = getRowBySlot(slot);
            int funcIndex = getFunctionIndexByColumn(col);

            if (funcIndex < 0) continue;

            ItemStack item = event.getNewItems().get(slot);
            if (item != null && item.getType() == Material.SNOWBALL) {
                if (row == 1) {
                    applyFunctionState(landId, funcIndex, true);
                } else if (row == 2) {
                    applyFunctionState(landId, funcIndex, false);
                }
            }
        }
    }

    @EventHandler
    public void onFunctionsMenuClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(landFunctionsMenuTitle)) return;

        if (event.getClickedInventory() != event.getView().getTopInventory()) {
            event.setCancelled(true);
            return;
        }

        int slot = event.getSlot();
        if (slot < 0 || slot >= 27) return;

        int col = getColumnBySlot(slot);
        int funcIndex = getFunctionIndexByColumn(col);

        if (funcIndex < 0) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFunctionsMenuClose(InventoryCloseEvent event) {
        if (!event.getView().getTitle().equals(landFunctionsMenuTitle)) return;

        Player pl = (Player) event.getPlayer();
        Long landId = editingLandId.remove(pl.getName());
        if (landId == null) return;

        Inventory inv = event.getInventory();

        int[] funcCols = {2, 3, 4, 5, 6};
        for (int i = 0; i < FUNC_COUNT; i++) {
            int col = funcCols[i];
            boolean found = false;

            int onSlot = 9 + col;
            ItemStack onItem = inv.getItem(onSlot);
            if (onItem != null && onItem.getType() == Material.SNOWBALL) {
                applyFunctionState(landId, i, true);
                found = true;
            }

            int offSlot = 18 + col;
            ItemStack offItem = inv.getItem(offSlot);
            if (offItem != null && offItem.getType() == Material.SNOWBALL) {
                applyFunctionState(landId, i, false);
                found = true;
            }

            if (!found) {
                applyFunctionState(landId, i, false);
            }
        }

        BinaryEditor.writeLandFunctions();
        LandFunctionsManager.rebuildWorldSets();

        LandInfoManager.LandInfo info = LandInfoManager.landInfoMap.get(landId);
        if (info != null) {
            GeneralListener.updateDenyChunksForLand(landId, info.getLandPile());
        }

        pl.sendMessage("§b领地功能设置已保存");
    }
}
