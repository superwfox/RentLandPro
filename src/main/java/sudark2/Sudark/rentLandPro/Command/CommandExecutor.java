package sudark2.Sudark.rentLandPro.Command;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import sudark2.Sudark.rentLandPro.File.BinaryEditor;
import sudark2.Sudark.rentLandPro.File.ConfigManager;
import sudark2.Sudark.rentLandPro.File.LandInfoManager;
import sudark2.Sudark.rentLandPro.File.LandMembersManager;
import sudark2.Sudark.rentLandPro.InventoryMenu.LandHomeMenu;
import sudark2.Sudark.rentLandPro.LandLogic.Clock;
import sudark2.Sudark.rentLandPro.LandLogic.LandRequestManager;
import sudark2.Sudark.rentLandPro.Listener.GeneralListener;
import sudark2.Sudark.rentLandPro.Listener.LandCreationListener;
import sudark2.Sudark.rentLandPro.OneBotRelated.OneBotApi;
import sudark2.Sudark.rentLandPro.Util.ChunkKeyUtil;
import sudark2.Sudark.rentLandPro.Util.IdentityUtil;

import java.util.HashSet;
import java.util.List;

import static sudark2.Sudark.rentLandPro.File.LandInfoManager.landInfoMap;
import static sudark2.Sudark.rentLandPro.File.LandMembersManager.landMembers;

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
            case "req" -> handleRequest(pl);
            case "yes" -> handleResponse(pl, args, true);
            case "no" -> handleResponse(pl, args, false);
            default -> pl.sendMessage("§7未知子命令: " + args[0]);
        }
        return true;
    }

    private void handleRequest(Player pl) {
        Chunk chunk = pl.getLocation().getChunk();
        Long chunkKey = ChunkKeyUtil.genKey(chunk);
        LandInfoManager.LandInfo landInfo = landInfoMap.get(chunkKey);
        if (landInfo == null) {
            for (LandInfoManager.LandInfo info : landInfoMap.values()) {
                for (Long ck : info.getLandPile()) {
                    if (ck.equals(chunkKey)) { landInfo = info; break; }
                }
                if (landInfo != null) break;
            }
        }
        if (landInfo == null) {
            pl.sendMessage("§7当前区块不属于任何领地");
            return;
        }

        String requesterQQ = IdentityUtil.getUserQQ(pl.getName());
        if (requesterQQ == null) {
            pl.sendMessage("§c未找到你的QQ绑定，无法申请");
            return;
        }

        String ownerQQ = landInfo.getLandOwnerQQ();
        if (requesterQQ.equals(ownerQQ)) {
            pl.sendMessage("§7你是该领地的地主");
            return;
        }

        LandMembersManager.LandMembership membership = landMembers.get(landInfo.getLandId());
        if (membership != null
                && (membership.operators().contains(requesterQQ) || membership.members().contains(requesterQQ))) {
            pl.sendMessage("§7你已经是该领地的成员");
            return;
        }

        if (LandRequestManager.isOnCooldown(requesterQQ)) {
            pl.sendMessage("§e申请冷却中，剩余 §b" + LandRequestManager.cooldownRemainingSec(requesterQQ) + " §e秒");
            return;
        }

        LandRequestManager.PendingRequest req = new LandRequestManager.PendingRequest(
                requesterQQ, pl.getName(), landInfo.getLandId(), landInfo.getLandName(), ownerQQ);
        LandRequestManager.addRequest(req);
        LandRequestManager.markUsed(requesterQQ);

        String ownerName = IdentityUtil.getUserID(ownerQQ);
        Player ownerPlayer = ownerName != null ? Bukkit.getPlayerExact(ownerName) : null;

        int index = LandRequestManager.getRequests(ownerQQ).size();

        if (ownerPlayer != null && ownerPlayer.isOnline()) {
            ownerPlayer.sendMessage("[§e领地§f] §b" + pl.getName() + " §f请求成为§e" + landInfo.getLandName() + "§f的成员");
            ownerPlayer.sendMessage(" §7使用 /land yes/no " + index + " 同意或拒绝申请");
        } else {
            String msg = "[领地消息] " + pl.getName() + "(" + requesterQQ + ") 申请 成为 " + landInfo.getLandName() + "的成员\n"
                    + " 发送 \"同意 " + index + "\" 同意\n"
                    + " 拒绝请无视该消息";
            OneBotApi.sendP(ownerQQ, msg);
        }

        pl.sendMessage("§b申请已发送，等待地主回应（2分钟内有效）");
    }

    private void handleResponse(Player pl, String[] args, boolean approve) {
        if (args.length < 2) {
            pl.sendMessage("§7用法: /land " + (approve ? "yes" : "no") + " <index>");
            return;
        }
        int index;
        try {
            index = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            pl.sendMessage("§7请输入有效的 index");
            return;
        }

        String ownerQQ = IdentityUtil.getUserQQ(pl.getName());
        if (ownerQQ == null) {
            pl.sendMessage("§c未找到你的QQ绑定");
            return;
        }

        LandRequestManager.PendingRequest req = LandRequestManager.takeByIndex(ownerQQ, index);
        if (req == null) {
            pl.sendMessage("§7该申请不存在或已过期");
            return;
        }

        if (approve) {
            approveRequest(req);
            pl.sendMessage("§b已同意 §e" + req.requesterName + " §b加入领地 §e" + req.landName);
        } else {
            OneBotApi.sendP(req.requesterQQ, "[领地] 系统已经拒绝你的领地申请");
            pl.sendMessage("§7已拒绝 §e" + req.requesterName + " §7的申请");
        }
    }

    public static void approveRequest(LandRequestManager.PendingRequest req) {
        LandMembersManager.LandMembership membership = landMembers.get(req.landId);
        if (membership == null) {
            membership = new LandMembersManager.LandMembership(new HashSet<>(), new HashSet<>());
            landMembers.put(req.landId, membership);
        }
        membership.members().add(req.requesterQQ);

        BinaryEditor.writeLandMembers();

        LandInfoManager.LandInfo info = landInfoMap.get(req.landId);
        if (info != null) {
            GeneralListener.updateDenyChunksForLand(req.landId, info.getLandPile());
        }

        Player requester = Bukkit.getPlayerExact(req.requesterName);
        if (requester != null && requester.isOnline()) {
            requester.sendMessage("§b你的领地申请已被同意：§e" + req.landName);
        } else {
            OneBotApi.sendP(req.requesterQQ, "[领地] 你的领地申请已被同意：" + req.landName);
        }
    }

    private void handleLandReturn(Player pl) {
        if (!pl.isOp()) {
            pl.sendMessage("§c该命令仅限管理员使用");
            return;
        }

        Chunk chunk = pl.getLocation().getChunk();
        Long chunkKey = ChunkKeyUtil.genKey(chunk);

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
        int dayPrice = ConfigManager.calculateLandPrice(targetLand.getLandPile().length);

        String valueLine = "领地价值 ： " + dayPrice;
        pl.sendMessage("§7" + valueLine);
        OneBotApi.sendG("[" + landName + "] " + valueLine);

        Clock.removeLand(landId);

        OneBotApi.sendLandReturnNotice(ownerQQ, landName);

        pl.sendMessage("§b已收回领地「§e" + landName + "§b」并通知地主");
    }
}
