package sudark2.Sudark.rentLandPro.Listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.plugin.Plugin;
import sudark2.Sudark.rentLandPro.File.LandInfo;
import sudark2.Sudark.rentLandPro.InventoryMenu.LandDetailsMenu;

import static sudark2.Sudark.rentLandPro.InventoryMenu.LandHomeMenu.landHomeMenuTitle;
import static sudark2.Sudark.rentLandPro.InventoryMenu.LandHomeMenu.landInfoTempMap;

public class LandHomeMenuListener implements Listener {

    @EventHandler
    public void onLandHomeMenuClick(InventoryClickEvent event) {
        int slot = event.getSlot();
        Player pl = (Player) event.getView().getPlayer();
        String name = pl.getName();
        if(!landInfoTempMap.containsKey(name))return;
        LandInfo landInfo = landInfoTempMap.get(name).get(slot);

        LandDetailsMenu.openLandDetailsMenu(pl, landInfo);
    }

    @EventHandler
    public void onLandHomeMenuClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals(landHomeMenuTitle) ) HandlerList.unregisterAll(this);
    }
}
