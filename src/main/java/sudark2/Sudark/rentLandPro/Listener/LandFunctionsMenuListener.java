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
import sudark2.Sudark.rentLandPro.File.LandInfoManager;

import static sudark2.Sudark.rentLandPro.InventoryMenu.LandFunctionsMenu.*;

public class LandFunctionsMenuListener implements Listener {

    @EventHandler
    public void onFunctionsMenuDrag(InventoryDragEvent event) {
        if (!event.getView().getTitle().equals(landFunctionsMenuTitle)) return;

        Player pl = (Player) event.getWhoClicked();
        Long landId = editingLandId.get(pl.getName());
        if (landId == null) return;

        Inventory inv = event.getInventory();

        // 拖拽结束后检查雪球位置
        for (int slot : event.getNewItems().keySet()) {
            if (slot >= 27) continue;

            int col = getColumnBySlot(slot);
            int row = getRowBySlot(slot);
            int funcIndex = getFunctionIndexByColumn(col);

            if (funcIndex < 0) continue;

            ItemStack item = event.getNewItems().get(slot);
            if (item != null && item.getType() == Material.SNOWBALL) {
                // row 1 = 开启, row 2 = 关闭
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

        // 允许在功能区域移动雪球，但不允许拿出菜单
        if (event.getClickedInventory() != event.getView().getTopInventory()) {
            event.setCancelled(true);
            return;
        }

        int slot = event.getSlot();
        if (slot < 0 || slot >= 27) return;

        int col = getColumnBySlot(slot);
        int funcIndex = getFunctionIndexByColumn(col);

        // 只允许在功能列(2-5)操作雪球
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

        // 关闭时根据雪球最终位置确定功能状态
        int[] funcCols = {2, 3, 4, 5};
        for (int i = 0; i < 4; i++) {
            int col = funcCols[i];
            boolean found = false;

            // 检查第2行(开启位置)
            int onSlot = 9 + col;
            ItemStack onItem = inv.getItem(onSlot);
            if (onItem != null && onItem.getType() == Material.SNOWBALL) {
                applyFunctionState(landId, i, true);
                found = true;
            }

            // 检查第3行(关闭位置)
            int offSlot = 18 + col;
            ItemStack offItem = inv.getItem(offSlot);
            if (offItem != null && offItem.getType() == Material.SNOWBALL) {
                applyFunctionState(landId, i, false);
                found = true;
            }

            // 找不到雪球则默认禁止
            if (!found) {
                applyFunctionState(landId, i, false);
            }
        }

        BinaryEditor.writeLandFunctions();

        // 更新所有在线玩家的拒绝区块集合
        LandInfoManager.LandInfo info = LandInfoManager.landInfoMap.get(landId);
        if (info != null) {
            GeneralListener.updateDenyChunksForLand(landId, info.getLandPile());
        }

        pl.sendMessage("§b领地功能设置已保存");
    }
}
