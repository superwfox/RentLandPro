package sudark2.Sudark.rentLandPro.InventoryMenu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import sudark2.Sudark.rentLandPro.File.landInfoManager;
import sudark2.Sudark.rentLandPro.Listener.LandHomeMenuListener;
import sudark2.Sudark.rentLandPro.Util.IdentityUtil;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static sudark2.Sudark.rentLandPro.File.landInfoManager.landInfoSet;
import static sudark2.Sudark.rentLandPro.RentLandPro.get;
import static sudark2.Sudark.rentLandPro.Util.ItemUtil.createOption;

public class LandHomeMenu {

    public static ConcurrentHashMap<String, List<landInfoManager.LandInfo>> landInfoTempMap = new ConcurrentHashMap<>();

    public static final String landHomeMenuTitle = "领地菜单首页 §7| §lLands";
    public static void openLandHomeMenu(Player pl) {
        Inventory inv = Bukkit.createInventory(null, 9,landHomeMenuTitle );

        short slot = 0;
        String name = pl.getName();
        String qq = IdentityUtil.getUserQQ(name);

        List<landInfoManager.LandInfo> landInfos = landInfoSet.stream()
                .filter(
                        landInfo -> landInfo.getLandOwnerQQ().equals(qq)
                ).toList();

        landInfoTempMap.put(name, landInfos);
        for (landInfoManager.LandInfo landInfo : landInfos) {
            inv.setItem(slot, createOption(landInfo.getLandSignature(), landInfo.getLandName(), "§e左键§7进行领地设置 §b右键§7管理成员"));
            slot++;
        }

        pl.openInventory(inv);
        Bukkit.getPluginManager().registerEvents(new LandHomeMenuListener(), get());
    }


}
