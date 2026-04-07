package sudark2.Sudark.rentLandPro.OneBotRelated;

import sudark2.Sudark.rentLandPro.File.ConfigManager;
import sudark2.Sudark.rentLandPro.File.LandMembersManager;
import sudark2.Sudark.rentLandPro.Util.IdentityUtil;

import java.util.Set;

import static sudark2.Sudark.rentLandPro.File.LandInfoManager.landInfoMap;
import static sudark2.Sudark.rentLandPro.File.LandMembersManager.landMembers;

public class PrivateCommandHandler {

    public static void handle(String userQQ, String msg) {
        msg = msg.trim();

        if (msg.equals("我的领地")) {
            GroupCommandHandler.handle(userQQ, msg);
            return;
        }

        if (!ConfigManager.OpQQ.contains(userQQ)) return;

        if (msg.equals("领地总览")) {
            handleOverview(userQQ);
        }
    }

    private static void handleOverview(String userQQ) {
        int totalLands = landInfoMap.size();
        int totalChunks = 0;
        Set<String> opSet = new java.util.LinkedHashSet<>();
        Set<String> memberSet = new java.util.LinkedHashSet<>();

        for (var info : landInfoMap.values()) {
            totalChunks += info.getLandPile().length;
        }
        for (LandMembersManager.LandMembership m : landMembers.values()) {
            opSet.addAll(m.operators());
            memberSet.addAll(m.members());
        }

        StringBuilder sb = new StringBuilder("[领地总览]\n");
        sb.append("领地数: ").append(totalLands).append("\n");
        sb.append("总区块: ").append(totalChunks).append("\n");
        sb.append("管理员(").append(opSet.size()).append("):\n");
        for (String qq : opSet) {
            String name = IdentityUtil.getUserID(qq);
            sb.append(name != null ? name : "?").append("(").append(qq).append(")\n");
        }
        sb.append("普通成员(").append(memberSet.size()).append("):\n");
        for (String qq : memberSet) {
            String name = IdentityUtil.getUserID(qq);
            sb.append(name != null ? name : "?").append("(").append(qq).append(")\n");
        }

        OneBotApi.sendP(userQQ, sb.toString());
    }
}
