package sudark2.Sudark.rentLandPro.InventoryMenu;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import sudark2.Sudark.rentLandPro.File.LandFunctionsManager;

import java.util.concurrent.ConcurrentHashMap;

import static sudark2.Sudark.rentLandPro.File.LandFunctionsManager.*;
import static sudark2.Sudark.rentLandPro.Util.ItemUtil.createOption;

public class LandFunctionsMenu {

    public static final String landFunctionsMenuTitle = "领地功能设置 §7| §lFunctions";

    public static final ConcurrentHashMap<String, Long> editingLandId = new ConcurrentHashMap<>();

    public static final int FUNC_COUNT = 5;

    private static final int[] FUNCTION_HEADER_SLOTS = {2, 3, 4, 5, 6};
    private static final int[] FUNCTION_ON_SLOTS = {11, 12, 13, 14, 15};
    private static final int[] FUNCTION_OFF_SLOTS = {20, 21, 22, 23, 24};

    private static final String[] FUNCTION_NAMES = {"方块破坏", "方块放置", "玩家交互", "爆炸破坏", "流体流动"};
    private static final int[] FLAGS = {FLAG_BREAK, FLAG_PLACE, FLAG_INTERACT, FLAG_EXPLODE, FLAG_FLUID};

    public static void openLandFunctionsMenu(Player pl, Long landId) {
        Inventory inv = Bukkit.createInventory(null, 27, landFunctionsMenuTitle);
        editingLandId.put(pl.getName(), landId);

        int flags = landFunctionFlags.getOrDefault(landId, 0);

        for (int i = 0; i < FUNC_COUNT; i++) {
            boolean isEnabled = (flags & FLAGS[i]) != 0;

            inv.setItem(FUNCTION_HEADER_SLOTS[i], createOption(Material.MAP, "§f" + FUNCTION_NAMES[i],
                    isEnabled ? "§b当前: §f允许" : "§e当前: §7禁止"));

            inv.setItem(FUNCTION_ON_SLOTS[i], createOption(Material.LIME_STAINED_GLASS_PANE, "§b允许", "§7拖动雪球至此"));

            inv.setItem(FUNCTION_OFF_SLOTS[i], createOption(Material.RED_STAINED_GLASS_PANE, "§e禁止", "§7拖动雪球至此"));

            int snowballSlot = isEnabled ? FUNCTION_ON_SLOTS[i] : FUNCTION_OFF_SLOTS[i];
            inv.setItem(snowballSlot, createOption(Material.SNOWBALL, "§l" + FUNCTION_NAMES[i],
                    "§7拖动此雪球切换开关"));
        }

        inv.setItem(0, createOption(Material.MAP, "§f功能列表", "§7参见上方"));
        inv.setItem(9, createOption(Material.LIME_STAINED_GLASS_PANE, "§b允许区域", "§7雪球在此行=允许"));
        inv.setItem(18, createOption(Material.RED_STAINED_GLASS_PANE, "§e禁止区域", "§7雪球在此行=禁止"));

        int[] fillerCols = {1, 7, 8};
        for (int row = 0; row < 3; row++) {
            for (int col : fillerCols) {
                int s = row * 9 + col;
                if (inv.getItem(s) == null) {
                    inv.setItem(s, createOption(Material.WHITE_STAINED_GLASS_PANE, " ", ""));
                }
            }
        }

        pl.openInventory(inv);
    }

    public static int getColumnBySlot(int slot) {
        return slot % 9;
    }

    public static int getRowBySlot(int slot) {
        return slot / 9;
    }

    public static int getFunctionIndexByColumn(int col) {
        if (col >= 2 && col <= 6) return col - 2;
        return -1;
    }

    public static void applyFunctionState(Long landId, int funcIndex, boolean enabled) {
        switch (funcIndex) {
            case 0 -> LandFunctionsManager.setFlagBreak(landId, enabled);
            case 1 -> LandFunctionsManager.setFlagPlace(landId, enabled);
            case 2 -> LandFunctionsManager.setFlagInteract(landId, enabled);
            case 3 -> LandFunctionsManager.setFlagExplode(landId, enabled);
            case 4 -> LandFunctionsManager.setFlagFluid(landId, enabled);
        }
    }
}
