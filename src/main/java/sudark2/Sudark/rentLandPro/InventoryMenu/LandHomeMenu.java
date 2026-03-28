package sudark2.Sudark.rentLandPro.InventoryMenu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import sudark2.Sudark.rentLandPro.File.LandInfoManager;
import sudark2.Sudark.rentLandPro.File.LandMembersManager;
import sudark2.Sudark.rentLandPro.Util.IdentityUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static sudark2.Sudark.rentLandPro.File.LandInfoManager.landInfoMap;
import static sudark2.Sudark.rentLandPro.File.LandMembersManager.landMembers;
import static sudark2.Sudark.rentLandPro.Util.ItemUtil.createOption;

public class LandHomeMenu {

    public record PlayerLandCache(List<LandInfoManager.LandInfo> operators, List<LandInfoManager.LandInfo> members) {
    }

    public static final ConcurrentHashMap<String, PlayerLandCache> landInfoTempMap = new ConcurrentHashMap<>();

    public static final String landHomeMenuTitle = "领地菜单首页 §7| §lLands";

    public static void openLandHomeMenu(Player pl) {
        String qq = IdentityUtil.getUserQQ(pl.getName());

        List<LandInfoManager.LandInfo> operatorLands = new ArrayList<>();  // 有管理权限
        List<LandInfoManager.LandInfo> memberLands = new ArrayList<>();    // 仅普通成员

        for (var entry : landMembers.entrySet()) {
            Long landId = entry.getKey();
            LandMembersManager.LandMembership membership = entry.getValue();

            LandInfoManager.LandInfo landInfo = landInfoMap.get(landId);
            if (landInfo == null) continue;

            if (membership.operators().contains(qq)) {
                operatorLands.add(landInfo);
            } else if (membership.members().contains(qq)) {
                memberLands.add(landInfo);
            }
        }

        landInfoTempMap.put(qq, new PlayerLandCache(operatorLands, memberLands));
        int size = (operatorLands.size() + memberLands.size() - 1) / 9 * 9 + 9;

        Inventory inv = Bukkit.createInventory(null, Math.min(size, 54), landHomeMenuTitle);
        int slot = 0;

        for (LandInfoManager.LandInfo land : operatorLands) {
            if (slot >= 54) break;
            inv.setItem(slot++, createOption(land.getLandSignature(), land.getLandName(),
                    "§e左键§7传送至领地 §b右键§7领地设置"));
        }

        for (LandInfoManager.LandInfo land : memberLands) {
            if (slot >= 54) break;
            inv.setItem(slot++, createOption(land.getLandSignature(), land.getLandName(),
                    "§e左键§7传送至领地"));
        }

        pl.openInventory(inv);
    }
}
