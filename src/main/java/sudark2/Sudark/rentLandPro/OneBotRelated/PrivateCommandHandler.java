package sudark2.Sudark.rentLandPro.OneBotRelated;

import sudark2.Sudark.rentLandPro.File.ConfigManager;
import sudark2.Sudark.rentLandPro.LandLogic.Clock;

import static sudark2.Sudark.rentLandPro.File.LandInfoManager.landInfoMap;

public class PrivateCommandHandler {

    public static void handle(String userQQ, String msg) {
        msg = msg.trim();

        if (msg.equals("我的领地")) {
            GroupCommandHandler.handle(userQQ, msg);
            return;
        }

        if (!ConfigManager.OpQQ.contains(userQQ)) return;

        if (msg.startsWith("续费 ")) {
            handleRenew(userQQ, msg.substring(3).trim());
        } else if (msg.startsWith("删除领地 ")) {
            handleRemoveLand(msg.substring(5).trim());
        } else if (msg.equals("领地总览")) {
            handleOverview(userQQ);
        }
    }

    private static void handleRenew(String userQQ, String args) {
        String[] parts = args.split(" ", 2);
        if (parts.length < 2) {
            OneBotApi.sendP(userQQ, "格式: 续费 <领地名> <天数>");
            return;
        }

        String landName = parts[0].trim();
        int days;
        try {
            days = Integer.parseInt(parts[1].trim());
        } catch (NumberFormatException e) {
            OneBotApi.sendP(userQQ, "天数必须为数字");
            return;
        }

        for (var info : landInfoMap.values()) {
            if (info.getLandName().equals(landName)) {
                info.setLandDuration(info.getlandDuration() + days);
                OneBotApi.sendP(userQQ, "[续费成功] " + landName + " 新租期: " + info.getlandDuration() + " 天");
                return;
            }
        }
        OneBotApi.sendP(userQQ, "未找到名为 " + landName + " 的领地");
    }

    private static void handleRemoveLand(String landName) {
        landName = landName.trim();
        for (var entry : landInfoMap.entrySet()) {
            if (entry.getValue().getLandName().equals(landName)) {
                Clock.removeLand(entry.getKey());
                OneBotApi.sendG("[管理操作] 领地 " + landName + " 已被管理员删除");
                return;
            }
        }
    }

    private static void handleOverview(String userQQ) {
        int totalLands = landInfoMap.size();
        int totalChunks = 0;
        for (var info : landInfoMap.values()) {
            totalChunks += info.getLandPile().length;
        }
        String msg = "[领地总览]\n" +
                "领地数: " + totalLands + "\n" +
                "总区块: " + totalChunks;
        OneBotApi.sendP(userQQ, msg);
    }
}
