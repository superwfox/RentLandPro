package sudark2.Sudark.rentLandPro.InventoryMenu;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import sudark2.Sudark.rentLandPro.File.LandMembersManager;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static sudark2.Sudark.rentLandPro.Util.ItemUtil.createOption;

public class LandMembersMenu {

    public static final String LanMembersMenuTitle = "管理领地成员 §7| §lMembers";

    public static final ConcurrentHashMap<String, ItemStack[]> InventoryTempStorage = new ConcurrentHashMap<>();

    public static void openLandMembersMenu(Player pl, Long landId) {
        Inventory inv = Bukkit.createInventory(null, 54, LanMembersMenuTitle);

        LandMembersManager.LandMembership landMembership = LandMembersManager.getLandMembers(landId);
        Set<String> ops = landMembership.operators();
        for (String op : ops) {
            inv.addItem(createOption(Material.PLAYER_HEAD, op, "§b普通成员"));
        }
    }
}
