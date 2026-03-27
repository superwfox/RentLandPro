package sudark2.Sudark.rentLandPro.InventoryMenu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import sudark2.Sudark.rentLandPro.File.LandInfo;

public class LandDetailsMenu {

    public static final String LandDetailsMenuTitle = "当前领地详情 §7| §lDetails";

    public static void openLandDetailsMenu(Player pl, LandInfo landInfo) {

        Inventory inv = Bukkit.createInventory(null, 9, LandDetailsMenuTitle);

    }

}
