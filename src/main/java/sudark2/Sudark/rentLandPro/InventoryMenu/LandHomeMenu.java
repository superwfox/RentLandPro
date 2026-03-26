package sudark2.Sudark.rentLandPro.InventoryMenu;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import static sudark2.Sudark.rentLandPro.Util.ItemUtil.createOption;

public class LandHomeMenu {

    public static Inventory inv;

    public static void openLandHomeMenu(Player pl) {
        inv = Bukkit.createInventory(null, 9, "Lands | §l领地菜单首页");
    }
}
