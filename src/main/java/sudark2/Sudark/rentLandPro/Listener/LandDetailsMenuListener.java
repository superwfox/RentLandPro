package sudark2.Sudark.rentLandPro.Listener;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import sudark2.Sudark.rentLandPro.File.ConfigManager;
import sudark2.Sudark.rentLandPro.File.LandInfoManager;
import sudark2.Sudark.rentLandPro.InventoryMenu.LandFunctionsMenu;
import sudark2.Sudark.rentLandPro.InventoryMenu.LandMembersMenu;
import sudark2.Sudark.rentLandPro.LandLogic.Clock;
import sudark2.Sudark.rentLandPro.Util.ChunkKeyUtil;

import java.util.concurrent.ConcurrentHashMap;

import static sudark2.Sudark.rentLandPro.InventoryMenu.LandDetailsMenu.LandDetailsMenuTitle;

public class LandDetailsMenuListener implements Listener {

    // 当前正在编辑的领地
    public static final ConcurrentHashMap<String, LandInfoManager.LandInfo> editingLandInfo = new ConcurrentHashMap<>();

    // 铁砧输入模式
    public static final ConcurrentHashMap<String, AnvilInputMode> anvilInputMode = new ConcurrentHashMap<>();

    public enum AnvilInputMode {
        RENAME,      // 修改领地名称
        DURATION     // 修改租期
    }

    @EventHandler
    public void onLandDetailsMenuClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(LandDetailsMenuTitle)) return;

        event.setCancelled(true);

        Player pl = (Player) event.getWhoClicked();
        LandInfoManager.LandInfo landInfo = editingLandInfo.get(pl.getName());
        if (landInfo == null) return;

        int slot = event.getSlot();

        switch (slot) {
            case 1 -> {
                // 修改领地名称 - 打开铁砧输入
                anvilInputMode.put(pl.getName(), AnvilInputMode.RENAME);
                pl.closeInventory();
                pl.sendMessage("§b请在聊天栏输入新的领地名称");
            }
            case 2 -> {
                // 修改领地租期 - 打开铁砧输入
                anvilInputMode.put(pl.getName(), AnvilInputMode.DURATION);
                pl.closeInventory();
                pl.sendMessage("§b请在聊天栏输入新的租期天数 §7(当前: " + landInfo.getlandDuration() + "天)");
                pl.sendMessage("§7租期减少会退还经验，增加会扣除经验");
                pl.sendMessage("§7设置为 §e0 §7将删除领地");
            }
            case 3 -> {
                // 修改领地标志 - 使用手持物品
                ItemStack hand = pl.getInventory().getItemInMainHand();
                if (hand.getType() == Material.AIR) {
                    pl.sendMessage("§e请手持物品后再点击");
                    return;
                }
                landInfo.setLandSignature(hand.getType());
                pl.sendMessage("§b领地标志已设置为: §f" + hand.getType().name());
                pl.closeInventory();
            }
            case 4 -> {
                // 管理领地成员
                pl.closeInventory();
                LandMembersMenu.openLandMembersMenu(pl, landInfo.getLandId());
            }
            case 5 -> {
                // 设置领地功能
                pl.closeInventory();
                LandFunctionsMenu.openLandFunctionsMenu(pl, landInfo.getLandId());
            }
            case 6 -> {
                // 设置传送目的地
                Location loc = pl.getLocation();
                Long currentChunk = ChunkKeyUtil.genKey(loc.getChunk());
                Long landIdChunk = landInfo.getLandId();

                // 计算曼哈顿距离
                int landChunkX = (int) (landIdChunk >> 32);
                int landChunkZ = (int) (landIdChunk & 0xFFFFFFFFL);
                int currentChunkX = (int) (currentChunk >> 32);
                int currentChunkZ = (int) (currentChunk & 0xFFFFFFFFL);

                int distance = Math.abs(landChunkX - currentChunkX) + Math.abs(landChunkZ - currentChunkZ);

                if (distance > 6) {
                    pl.sendMessage("§e传送点距离领地中心超过6区块，设置失败");
                    return;
                }

                int[] point = {loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()};
                landInfo.setTeleportPoint(point);
                pl.sendMessage("§b传送点已设置为当前位置");
                pl.closeInventory();
            }
            case 7 -> {
                // 扩展领地范围 - 进入书与笔模式
                LandCreationListener.editingLand.put(pl.getName(), landInfo.getLandId());
                pl.closeInventory();
                pl.sendMessage("§b已进入扩展模式，使用 §e书与笔 §b点击地面添加区块");
                pl.sendMessage("§7输入 §e/land confirm §7完成扩展");
            }
        }
    }

    public static void handleChatInput(Player pl, String message) {
        AnvilInputMode mode = anvilInputMode.remove(pl.getName());
        if (mode == null) return;

        LandInfoManager.LandInfo landInfo = editingLandInfo.get(pl.getName());
        if (landInfo == null) return;

        switch (mode) {
            case RENAME -> {
                landInfo.setLandName(message);
                pl.sendMessage("§b领地名称已修改为: §f" + message);
            }
            case DURATION -> {
                try {
                    int newDuration = Integer.parseInt(message);

                    if (newDuration == 0) {
                        // 删除领地
                        pl.sendMessage("§e领地将被删除...");
                        Clock.removeLand(landInfo.getLandId());
                        editingLandInfo.remove(pl.getName());
                        pl.sendMessage("§b领地已删除");
                        return;
                    }

                    int oldDuration = landInfo.getlandDuration();
                    int chunkCount = landInfo.getLandPile().length;
                    int diff = newDuration - oldDuration;
                    int cost = ConfigManager.calculateRentCost(chunkCount, Math.abs(diff));

                    if (diff > 0) {
                        // 增加租期，扣除经验
                        if (pl.getLevel() < cost) {
                            pl.sendMessage("§e经验不足！需要 §b" + cost + " §e级经验");
                            return;
                        }
                        pl.setLevel(pl.getLevel() - cost);
                        pl.sendMessage("§b已扣除 §e" + cost + " §b级经验");
                    } else if (diff < 0) {
                        // 减少租期，退还经验
                        pl.setLevel(pl.getLevel() + cost);
                        pl.sendMessage("§b已退还 §e" + cost + " §b级经验");
                    }

                    landInfo.setLandDuration(newDuration);
                    pl.sendMessage("§b租期已修改为: §e" + newDuration + " §b天");

                } catch (NumberFormatException e) {
                    pl.sendMessage("§e请输入有效的数字");
                }
            }
        }
    }
}
