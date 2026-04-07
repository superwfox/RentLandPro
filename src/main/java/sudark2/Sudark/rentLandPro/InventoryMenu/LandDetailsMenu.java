package sudark2.Sudark.rentLandPro.InventoryMenu;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import sudark2.Sudark.rentLandPro.File.LandInfoManager;
import sudark2.Sudark.rentLandPro.Listener.LandDetailsMenuListener;

import static sudark2.Sudark.rentLandPro.Util.ItemUtil.createOption;

public class LandDetailsMenu {

    public static final String LandDetailsMenuTitle = "当前领地详情 §7| §lDetails";

    public static void openLandDetailsMenu(Player pl, LandInfoManager.LandInfo landInfo) {
        LandDetailsMenuListener.editingLandInfo.put(pl.getName(), landInfo);

        Inventory inv = Bukkit.createInventory(null, 9, LandDetailsMenuTitle);

        int[] tp = landInfo.getTeleportPoint();
        String tpStr = tp != null && tp.length == 2 ? tp[0] + ", " + tp[1] : "未设置";

        inv.setItem(1, createOption(Material.WRITABLE_BOOK, "§f修改领地名称", "§7当前: §l" + landInfo.getLandName()));
        inv.setItem(2, createOption(Material.CLOCK, "§f修改领地租期", "§7当前: §e" + landInfo.getDurationDaysDisplay() + " §7天"));
        inv.setItem(3, createOption(Material.BEDROCK, "§f修改领地标志", "§7当前: §b" + landInfo.getLandSignature().name()));
        inv.setItem(4, createOption(Material.PIGLIN_HEAD, "§f管理领地成员", "§7为§e在线玩家§7增删权限"));
        inv.setItem(5, createOption(Material.CRAFTING_TABLE, "§f设置领地功能", "§7管理游客在领地中的行为"));
        inv.setItem(6, createOption(Material.BEACON, "§f设置传送目的地", "§7当前: §b" + tpStr));
        inv.setItem(7, createOption(Material.FILLED_MAP, "§f重塑领地范围", "§7当前: §e" + landInfo.getLandPile().length + " §7区块"));

        pl.openInventory(inv);
    }
}
