package sudark2.Sudark.rentLandPro.InventoryMenu;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import sudark2.Sudark.rentLandPro.File.LandMembersManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static sudark2.Sudark.rentLandPro.File.CourierCSVLoader.QQ2ID;
import static sudark2.Sudark.rentLandPro.Util.ItemUtil.createHead;
import static sudark2.Sudark.rentLandPro.Util.ItemUtil.createOption;

public class LandMembersMenu {

    public static final String LanMembersMenuTitle = "管理领地成员 §7| §lMembers";

    public static final ConcurrentHashMap<String, ItemStack[]> InventoryTempStorage = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, Long> editingLandId = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, Integer> currentPage = new ConcurrentHashMap<>();

    private static final int ITEMS_PER_PAGE = 45;

    public static void openLandMembersMenu(Player pl, Long landId) {
        openLandMembersMenu(pl, landId, 0);
    }

    public static void openLandMembersMenu(Player pl, Long landId, int page) {
        editingLandId.put(pl.getName(), landId);
        currentPage.put(pl.getName(), page);

        // 保存玩家背包
        InventoryTempStorage.put(pl.getName(), pl.getInventory().getContents().clone());
        pl.getInventory().clear();

        // 获取在线玩家头颅放入玩家背包
        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        int start = 0;
        int end = Math.min(36, onlinePlayers.size());

        for (int i = start; i < end; i++) {
            Player online = onlinePlayers.get(i);
            pl.getInventory().addItem(createHead(online.getName(), "§7点击添加为成员", "", false));
        }

        // 创建菜单显示现有成员
        Inventory inv = Bukkit.createInventory(null, 54, LanMembersMenuTitle);

        LandMembersManager.LandMembership membership = LandMembersManager.getLandMembers(landId);
        if (membership == null) {
            pl.sendMessage("§e领地成员数据不存在");
            return;
        }

        List<String> allMembers = new ArrayList<>();
        for (String op : membership.operators()) {
            allMembers.add("OP:" + op);
        }
        for (String member : membership.members()) {
            allMembers.add("M:" + member);
        }

        int totalPages = (allMembers.size() - 1) / ITEMS_PER_PAGE + 1;
        int startIdx = page * ITEMS_PER_PAGE;
        int endIdx = Math.min(startIdx + ITEMS_PER_PAGE, allMembers.size());

        int slot = 0;
        for (int i = startIdx; i < endIdx; i++) {
            String entry = allMembers.get(i);
            boolean isOp = entry.startsWith("OP:");
            String qq = entry.substring(isOp ? 3 : 2);
            String name = QQ2ID.get(qq);
            if (name == null) name = "Unknown";

            inv.setItem(slot++, createHead(name, isOp ? "§e领地管理" : "§f普通成员", qq, isOp));
        }

        // 导航按钮
        if (page > 0) {
            inv.setItem(45, createOption(Material.ARROW, "§b上一页", "§7第 " + page + "/" + totalPages + " 页"));
        }
        if (page < totalPages - 1) {
            inv.setItem(53, createOption(Material.ARROW, "§b下一页", "§7第 " + (page + 2) + "/" + totalPages + " 页"));
        }

        // 返回按钮
        inv.setItem(49, createOption(Material.BARRIER, "§e关闭并恢复背包", "§7点击关闭菜单"));

        pl.openInventory(inv);
    }

    public static void restoreInventory(Player pl) {
        ItemStack[] stored = InventoryTempStorage.remove(pl.getName());
        if (stored != null) {
            pl.getInventory().setContents(stored);
        }
        editingLandId.remove(pl.getName());
        currentPage.remove(pl.getName());
    }
}
