package sudark2.Sudark.rentLandPro.Listener;

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

import static sudark2.Sudark.rentLandPro.InventoryMenu.LandHomeMenu.landHomeMenuTitle;
import static sudark2.Sudark.rentLandPro.InventoryMenu.LandHomeMenu.landInfoTempMap;

public class LandHomeMenuListener implements Listener {

    @EventHandler
    public void onLandHomeMenuClick(InventoryClickEvent event) {
        // 只处理领地菜单
        if (!event.getView().getTitle().equals(landHomeMenuTitle)) return;

        event.setCancelled(true); // 防止拿走物品

        int slot = event.getSlot();
        if (slot < 0) return; // 点击了窗口外

        Player pl = (Player) event.getView().getPlayer();
        String qq = IdentityUtil.getUserQQ(pl.getName());

        LandHomeMenu.PlayerLandCache cache = landInfoTempMap.get(qq);
        if (cache == null) return;

        // 根据 slot 判断点击的是哪个领地
        // 排列顺序：先 owning，再 operating
        LandInfoManager.LandInfo landInfo = getLandInfoBySlot(cache, slot);
        if (landInfo == null) return;

        boolean isOperator = cache.operators().contains(landInfo);
        ClickType clickType = event.getClick();

        if (clickType == ClickType.LEFT) {
            // TODO: 传送到领地
            pl.sendMessage("§a传送至领地: " + landInfo.getLandName());
        } else if (clickType == ClickType.RIGHT && isOperator) {
            // 只有 operator 才能右键打开设置
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

        // 玩家关闭菜单时清理缓存
        Player pl = (Player) event.getPlayer();
        String qq = IdentityUtil.getUserQQ(pl.getName());
        landInfoTempMap.remove(qq);
    }
}
