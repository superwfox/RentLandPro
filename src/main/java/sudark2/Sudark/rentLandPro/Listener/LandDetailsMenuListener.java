package sudark2.Sudark.rentLandPro.Listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import sudark2.Sudark.rentLandPro.File.ConfigManager;
import sudark2.Sudark.rentLandPro.File.LandInfoManager;
import sudark2.Sudark.rentLandPro.InventoryMenu.LandFunctionsMenu;
import sudark2.Sudark.rentLandPro.InventoryMenu.LandMembersMenu;
import sudark2.Sudark.rentLandPro.LandLogic.Clock;
import sudark2.Sudark.rentLandPro.Util.ChunkKeyUtil;
import sudark2.Sudark.rentLandPro.Util.GlassUtil;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static sudark2.Sudark.rentLandPro.InventoryMenu.LandDetailsMenu.LandDetailsMenuTitle;
import static sudark2.Sudark.rentLandPro.Util.ItemUtil.createMap;

public class LandDetailsMenuListener implements Listener {

    public static final ConcurrentHashMap<String, LandInfoManager.LandInfo> editingLandInfo = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, ChatInputMode> anvilInputMode = new ConcurrentHashMap<>();

    public enum ChatInputMode {
        RENAME,
        DURATION
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
                anvilInputMode.put(pl.getName(), ChatInputMode.RENAME);
                pl.closeInventory();
                pl.sendMessage("§7请在聊天栏输入新的领地名称：");
            }
            case 2 -> {
                anvilInputMode.put(pl.getName(), ChatInputMode.DURATION);
                pl.closeInventory();
                pl.sendMessage("§7请在聊天栏输入新的租期天数：");
                pl.sendMessage("§7输入 §e0 §7将删除领地");
            }
            case 3 -> {
                ItemStack hand = pl.getInventory().getItemInMainHand();
                if (hand.getType() == Material.AIR) {
                    pl.sendMessage("§7请手持物品后再点击");
                    return;
                }
                landInfo.setLandSignature(hand.getType());
                pl.sendMessage("§7领地标志已设置为: §e" + hand.getType().name());
                pl.closeInventory();
            }
            case 4 -> {
                pl.closeInventory();
                LandMembersMenu.openLandMembersMenu(pl, landInfo.getLandId());
            }
            case 5 -> {
                pl.closeInventory();
                LandFunctionsMenu.openLandFunctionsMenu(pl, landInfo.getLandId());
            }
            case 6 -> {
                Location loc = pl.getLocation();
                Long currentChunk = ChunkKeyUtil.genKey(loc.getChunk());
                Long landIdChunk = landInfo.getLandId();

                int landChunkX = (int) (landIdChunk >> 32);
                int landChunkZ = (int) (landIdChunk & 0xFFFFFFFFL);
                int currentChunkX = (int) (currentChunk >> 32);
                int currentChunkZ = (int) (currentChunk & 0xFFFFFFFFL);

                int distance = Math.abs(landChunkX - currentChunkX) + Math.abs(landChunkZ - currentChunkZ);

                if (distance > 6) {
                    pl.sendMessage("§e传送点距离领地中心超过6区块，设置失败");
                    return;
                }

                int[] point = {loc.getBlockX(), loc.getBlockZ()};
                landInfo.setTeleportPoint(point);
                pl.sendMessage("§7传送点已设置为当前位置");
                pl.closeInventory();
            }
            case 7 -> {
                LandInfoManager.LandInfo info = landInfo;
                Long landId = info.getLandId();
                World world = pl.getWorld();

                LinkedHashSet<Long> pending = new LinkedHashSet<>();
                Set<Long> original = new HashSet<>();
                for (Long ck : info.getLandPile()) {
                    pending.add(ck);
                    original.add(ck);
                    LandCreationListener.startParticleEffectStatic(ck, world);
                    GlassUtil.placeGlass(ck, world);
                }

                LandCreationListener.editingLand.put(pl.getName(), landId);
                LandCreationListener.pendingChunks.put(pl.getName(), pending);
                LandCreationListener.originalChunks.put(pl.getName(), original);

                // 启动超时任务
                LandCreationListener.resetCreationTimeout(pl.getName());
                if (pl.getItemInHand().isEmpty())
                    pl.setItemInHand(createMap(pl.getLocation()));
                else
                    pl.getInventory().addItem(createMap(pl.getLocation()));

                pl.closeInventory();
                pl.sendMessage("§7已进入领地范围编辑模式");
                pl.sendMessage("§7右键 §f添加区块 §7| §7左键 §f取消区块");
                pl.sendMessage("§7输入 §e/land confirm §7完成 | §e/land cancel §7取消");
                pl.sendMessage("[ 5分钟内未完成将自动取消 ]");
            }
        }
    }

    public static void handleChatInput(Player pl, String message) {
        ChatInputMode mode = anvilInputMode.remove(pl.getName());
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
                        pl.sendMessage("§7领地将被删除...");
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
                        if (pl.getLevel() < cost) {
                            pl.sendMessage("§e经验不足！需要 §b" + cost + " §e级经验");
                            return;
                        }
                        pl.setLevel(pl.getLevel() - cost);
                        pl.sendMessage("§7已扣除 §e" + cost + " §7级经验");
                    } else if (diff < 0) {
                        pl.setLevel(pl.getLevel() + cost);
                        pl.sendMessage("§7已退还 §e" + cost + " §7级经验");
                    }

                    landInfo.setLandDuration(newDuration);
                    pl.sendMessage("§7租期已修改为: §e" + newDuration + " §7天");

                } catch (NumberFormatException e) {
                    pl.sendMessage("§e请输入有效的数字");
                }
            }
        }
    }
}
