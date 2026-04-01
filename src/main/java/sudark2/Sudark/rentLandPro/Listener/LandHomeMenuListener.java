package sudark2.Sudark.rentLandPro.Listener;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import sudark2.Sudark.rentLandPro.File.LandInfoManager;
import sudark2.Sudark.rentLandPro.InventoryMenu.LandDetailsMenu;
import sudark2.Sudark.rentLandPro.InventoryMenu.LandHomeMenu;
import sudark2.Sudark.rentLandPro.Util.IdentityUtil;
import sudark2.Sudark.rentLandPro.Util.LocationUtil;

import static sudark2.Sudark.rentLandPro.InventoryMenu.LandHomeMenu.landHomeMenuTitle;
import static sudark2.Sudark.rentLandPro.InventoryMenu.LandHomeMenu.landInfoTempMap;
import static sudark2.Sudark.rentLandPro.Util.LevelNameUtil.MainWorld;

public class LandHomeMenuListener implements Listener {

    @EventHandler
    public void onLandHomeMenuClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(landHomeMenuTitle)) return;

        event.setCancelled(true);

        int slot = event.getSlot();
        if (slot < 0) return;

        Player pl = (Player) event.getView().getPlayer();
        String qq = IdentityUtil.getUserQQ(pl.getName());

        LandHomeMenu.PlayerLandCache cache = landInfoTempMap.get(qq);
        if (cache == null) return;

        LandInfoManager.LandInfo landInfo = getLandInfoBySlot(cache, slot);
        if (landInfo == null) return;

        boolean isOperator = cache.operators().contains(landInfo);
        ClickType clickType = event.getClick();

        if (clickType == ClickType.LEFT) {
            int[] tp = landInfo.getTeleportPoint();
            if (tp != null && tp.length == 2) {
                int y = LocationUtil.getHighestSolidY(MainWorld, tp[0], tp[1]) + 1;
                pl.teleport(new Location(MainWorld, tp[0] + 0.5, y, tp[1] + 0.5));
                pl.sendMessage("已传送至领地: §e" + landInfo.getLandName());
            } else {
                pl.sendMessage("§e该领地传送点未设置");
            }
        } else if (clickType == ClickType.RIGHT && isOperator) {
            LandDetailsMenu.openLandDetailsMenu(pl, landInfo);
        }
    }

    private LandInfoManager.LandInfo getLandInfoBySlot(LandHomeMenu.PlayerLandCache cache, int slot) {
        int operatorsSize = cache.operators().size();

        if (slot < operatorsSize) {
            return cache.operators().get(slot);
        } else if (slot < operatorsSize + cache.members().size()) {
            return cache.members().get(slot - operatorsSize);
        }
        return null;
    }

    @EventHandler
    public void onLandHomeMenuClose(InventoryCloseEvent event) {
        if (!event.getView().getTitle().equals(landHomeMenuTitle)) return;

        Player pl = (Player) event.getPlayer();
        String qq = IdentityUtil.getUserQQ(pl.getName());
        landInfoTempMap.remove(qq);
    }
}
