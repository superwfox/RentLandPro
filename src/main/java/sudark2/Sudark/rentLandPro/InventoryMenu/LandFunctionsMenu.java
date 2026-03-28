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

    // 27格布局：3行9列
    // 第1列(0,9,18): 状态指示 - 空/绿玻璃/红玻璃
    // 第2-5列: 四个功能列，每列顶部空地图指示，玩家用雪球拖动选择
    // 雪球在第2行=开启，第3行=关闭

    private static final int[] INDICATOR_SLOTS = {0, 9, 18};
    private static final int[] FUNCTION_HEADER_SLOTS = {2, 3, 4, 5};
    private static final int[] FUNCTION_ON_SLOTS = {11, 12, 13, 14};
    private static final int[] FUNCTION_OFF_SLOTS = {20, 21, 22, 23};

    private static final String[] FUNCTION_NAMES = {"方块破坏", "方块放置", "玩家交互", "爆炸破坏"};
    private static final int[] FLAGS = {FLAG_BREAK, FLAG_PLACE, FLAG_INTERACT, FLAG_EXPLODE};

    public static void openLandFunctionsMenu(Player pl, Long landId) {
        Inventory inv = Bukkit.createInventory(null, 27, landFunctionsMenuTitle);
        editingLandId.put(pl.getName(), landId);

        int flags = landFunctionFlags.getOrDefault(landId, 0);

        // 第1列：状态指示（空）
        for (int slot : INDICATOR_SLOTS) {
            inv.setItem(slot, null);
        }

        // 功能列
        for (int i = 0; i < 4; i++) {
            boolean isEnabled = (flags & FLAGS[i]) != 0;

            // 顶部：空地图指示
            inv.setItem(FUNCTION_HEADER_SLOTS[i], createOption(Material.MAP, "§f" + FUNCTION_NAMES[i],
                    isEnabled ? "§b当前: §f允许" : "§e当前: §7禁止"));

            // 第2行：开启位置（绿色玻璃板背景）
            inv.setItem(FUNCTION_ON_SLOTS[i], createOption(Material.LIME_STAINED_GLASS_PANE, "§b允许", "§7拖动雪球至此"));

            // 第3行：关闭位置（红色玻璃板背景）
            inv.setItem(FUNCTION_OFF_SLOTS[i], createOption(Material.RED_STAINED_GLASS_PANE, "§e禁止", "§7拖动雪球至此"));

            // 雪球放置在当前状态位置
            int snowballSlot = isEnabled ? FUNCTION_ON_SLOTS[i] : FUNCTION_OFF_SLOTS[i];
            inv.setItem(snowballSlot, createOption(Material.SNOWBALL, "§l" + FUNCTION_NAMES[i],
                    "§7拖动此雪球切换开关"));
        }

        // 左侧状态指示列
        inv.setItem(9, createOption(Material.LIME_STAINED_GLASS_PANE, "§b允许区域", "§7雪球在此行=允许"));
        inv.setItem(18, createOption(Material.RED_STAINED_GLASS_PANE, "§e禁止区域", "§7雪球在此行=禁止"));

        pl.openInventory(inv);
    }

    public static int getColumnBySlot(int slot) {
        return slot % 9;
    }

    public static int getRowBySlot(int slot) {
        return slot / 9;
    }

    public static int getFunctionIndexByColumn(int col) {
        if (col >= 2 && col <= 5) return col - 2;
        return -1;
    }

    public static void applyFunctionState(Long landId, int funcIndex, boolean enabled) {
        switch (funcIndex) {
            case 0 -> LandFunctionsManager.setFlagBreak(landId, enabled);
            case 1 -> LandFunctionsManager.setFlagPlace(landId, enabled);
            case 2 -> LandFunctionsManager.setFlagInteract(landId, enabled);
            case 3 -> LandFunctionsManager.setFlagExplode(landId, enabled);
        }
    }
}
