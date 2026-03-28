package sudark2.Sudark.rentLandPro.InventoryMenu;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import sudark2.Sudark.rentLandPro.File.landInfoManager;

import static sudark2.Sudark.rentLandPro.Util.ItemUtil.createOption;

public class LandDetailsMenu {

    public static final String LandDetailsMenuTitle = "当前领地详情 §7| §lDetails";

    public static void openLandDetailsMenu(Player pl, landInfoManager.LandInfo landInfo) {

        Inventory inv = Bukkit.createInventory(null, 9, LandDetailsMenuTitle);

        int slot = 2;

        inv.setItem(slot++,createOption(Material.WRITABLE_BOOK, "修改领地名称", "§7§l" + landInfo.getLandName()));
        inv.setItem(slot++,createOption(Material.CLOCK, "修改领地租期", "§7§l" + landInfo.getlandDuration()));
        inv.setItem(slot++,createOption(Material.BEDROCK, "修改领地标志", "§7§l" + landInfo.getLandSignature()));
        inv.setItem(slot++,createOption(Material.PIGLIN_HEAD, "管理领地成员", "为§e在线玩家§f增删权限"));
        inv.setItem(slot,createOption(Material.CRAFTING_TABLE, "设置领地功能", "管理其他玩家在领地中能做的行为"));

        pl.openInventory(inv);
    }

}
