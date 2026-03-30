package sudark2.Sudark.rentLandPro.InventoryMenu;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import sudark2.Sudark.rentLandPro.File.LandMembersManager;
import sudark2.Sudark.rentLandPro.Util.IdentityUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static sudark2.Sudark.rentLandPro.File.CourierCSVLoader.QQ2ID;
import static sudark2.Sudark.rentLandPro.Util.ItemUtil.createHead;
import static sudark2.Sudark.rentLandPro.Util.ItemUtil.createOption;

public class LandMembersMenu {

    public static final String LanMembersMenuTitle = "管理领地成员 | §lMembers";

    public static final ConcurrentHashMap<String, ItemStack[]> InventoryTempStorage = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, Long> editingLandId = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, Integer> currentPage = new ConcurrentHashMap<>();
    public static final java.util.Set<String> skipNextClose = ConcurrentHashMap.newKeySet();

    private static final int ITEMS_PER_PAGE = 45;

    public static void openLandMembersMenu(Player pl, Long landId) {
        openLandMembersMenu(pl, landId, 0);
    }

    public static void openLandMembersMenu(Player pl, Long landId, int page) {
        LandMembersManager.LandMembership membership = LandMembersManager.getLandMembers(landId);
        if (membership == null) {
            pl.sendMessage("§7领地成员数据不存在");
            return;
        }

        editingLandId.put(pl.getName(), landId);
        currentPage.put(pl.getName(), page);

        ItemStack[] backup = new ItemStack[27];
        for (int i = 9; i <= 35; i++) {
            backup[i - 9] = pl.getInventory().getItem(i);
            pl.getInventory().setItem(i, null);
        }
        InventoryTempStorage.put(pl.getName(), backup);
       // System.out.println(Arrays.toString(InventoryTempStorage.get(pl.getName())));

        Set<String> allMemberQQs = new java.util.HashSet<>();
        allMemberQQs.addAll(membership.operators());
        allMemberQQs.addAll(membership.members());

        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        int slotIdx = 0;
        for (Player online : onlinePlayers) {
            if (slotIdx >= 27) break;
            String qq = IdentityUtil.getUserQQ(online.getName());
            if (qq != null && allMemberQQs.contains(qq)) continue;
            pl.getInventory().setItem(9 + slotIdx, createHead(online.getName(), "§7左键添加为成员", "", false));
            slotIdx++;
        }

        Inventory inv = Bukkit.createInventory(null, 54, LanMembersMenuTitle);

        List<String> allMembers = new ArrayList<>();
        for (String op : membership.operators()) {
            allMembers.add("OP:" + op);
        }
        for (String member : membership.members()) {
            allMembers.add("M:" + member);
        }

        int totalPages = Math.max(1, (allMembers.size() - 1) / ITEMS_PER_PAGE + 1);
        int startIdx = page * ITEMS_PER_PAGE;
        int endIdx = Math.min(startIdx + ITEMS_PER_PAGE, allMembers.size());

        int slot = 0;
        for (int i = startIdx; i < endIdx; i++) {
            String entry = allMembers.get(i);
            boolean isOp = entry.startsWith("OP:");
            String qq = entry.substring(isOp ? 3 : 2);
            String name = QQ2ID.get(qq);
            if (name == null) name = "Unknown";

            String loreAction = isOp ? "§e领地管理 §7| §7左键移除" : "§f普通成员 §7| §7左键移除 §7| §7右键升级管理";
            inv.setItem(slot++, createHead(name, loreAction, qq, isOp));
        }

        if (page > 0) {
            inv.setItem(45, createOption(Material.ARROW, "§b上一页", "§7第 " + page + "/" + totalPages + " 页"));
        }
        if (page < totalPages - 1) {
            inv.setItem(53, createOption(Material.ARROW, "§b下一页", "§7第 " + (page + 2) + "/" + totalPages + " 页"));
        }

        inv.setItem(49, createOption(Material.BARRIER, "§e关闭并恢复背包", "§7点击关闭菜单"));

        pl.openInventory(inv);
    }

}
