package sudark2.Sudark.rentLandPro.Command;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import sudark2.Sudark.rentLandPro.File.LandInfoManager;
import sudark2.Sudark.rentLandPro.InventoryMenu.LandHomeMenu;
import sudark2.Sudark.rentLandPro.LandLogic.Clock;
import sudark2.Sudark.rentLandPro.Listener.LandCreationListener;
import sudark2.Sudark.rentLandPro.OneBotRelated.OneBotApi;
import sudark2.Sudark.rentLandPro.Util.ChunkKeyUtil;

import static sudark2.Sudark.rentLandPro.File.LandInfoManager.landInfoMap;

public class CommandExecutor implements org.bukkit.command.CommandExecutor {
    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (!(sender instanceof Player pl)) return false;

        if (args.length == 0) {
            LandHomeMenu.openLandHomeMenu(pl);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "confirm" -> {
                int days = 1;
                if (args.length >= 2) {
                    try {
                        days = Integer.parseInt(args[1]);
                        if (days < 1) {
                            pl.sendMessage("§e租期至少为 §b1 §e天");
                            return true;
                        }
                    } catch (NumberFormatException e) {
                        pl.sendMessage("§e请输入有效的天数");
                        return true;
                    }
                }
                LandCreationListener.confirmLandCreation(pl, days);
            }
            case "cancel" -> LandCreationListener.cancelLandCreation(pl);
            case "return" -> handleLandReturn(pl);
            default -> pl.sendMessage("§7未知子命令: " + args[0]);
        }
        return true;
    }

    private void handleLandReturn(Player pl) {
        if (!pl.isOp()) {
            pl.sendMessage("§c该命令仅限管理员使用");
            return;
        }

        Chunk chunk = pl.getLocation().getChunk();
        Long chunkKey = ChunkKeyUtil.genKey(chunk);

        // 查找当前区块属于哪个领地
        LandInfoManager.LandInfo targetLand = null;
        for (LandInfoManager.LandInfo info : landInfoMap.values()) {
            for (Long landChunk : info.getLandPile()) {
                if (landChunk.equals(chunkKey)) {
                    targetLand = info;
                    break;
                }
            }
            if (targetLand != null) break;
        }

        if (targetLand == null) {
            pl.sendMessage("§7当前区块不属于任何领地");
            return;
        }

        String landName = targetLand.getLandName();
        String ownerQQ = targetLand.getLandOwnerQQ();
        Long landId = targetLand.getLandId();

        // 删除领地
        Clock.removeLand(landId);

        // 发送群通知@地主
        OneBotApi.sendLandReturnNotice(ownerQQ, landName);

        pl.sendMessage("§b已收回领地「§e" + landName + "§b」并通知地主");
    }
}
