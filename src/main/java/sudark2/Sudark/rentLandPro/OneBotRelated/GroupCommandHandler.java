package sudark2.Sudark.rentLandPro.OneBotRelated;

import sudark2.Sudark.rentLandPro.File.LandInfoManager;
import sudark2.Sudark.rentLandPro.File.LandMembersManager;
import sudark2.Sudark.rentLandPro.Util.IdentityUtil;

import static sudark2.Sudark.rentLandPro.File.LandInfoManager.landInfoMap;
import static sudark2.Sudark.rentLandPro.File.LandMembersManager.landMembers;

public class GroupCommandHandler {

    public static void handle(String userQQ, String msg) {
        msg = msg.trim();

        if (msg.equals("我的领地")) {
            handleMyLands(userQQ);
        } else if (msg.equals("领地列表")) {
            handleLandList();
        } else if (msg.startsWith("查询领地 ")) {
            handleQueryLand(msg.substring(5));
        }
    }

    private static void handleMyLands(String userQQ) {
        String playerName = IdentityUtil.getUserID(userQQ);
        if (playerName == null) {
            OneBotApi.sendG("未找到绑定的游戏账号");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("[我的领地]\n");
        int count = 0;

        for (var entry : landMembers.entrySet()) {
            Long landId = entry.getKey();
            LandMembersManager.LandMembership m = entry.getValue();

            if (!m.operators().contains(userQQ) && !m.members().contains(userQQ)) continue;

            LandInfoManager.LandInfo info = landInfoMap.get(landId);
            if (info == null) continue;

            String role = m.operators().contains(userQQ) ? "地主" : "成员";
            sb.append(info.getLandName())
              .append(" | ").append(info.getLandPile().length).append("区块")
              .append(" | 剩余").append(info.getlandDuration()).append("天")
              .append(" (").append(role).append(")\n");
            count++;
        }

        if (count == 0) sb.append("暂无领地");
        OneBotApi.sendG(sb.toString());
    }

    private static void handleLandList() {
        if (landInfoMap.isEmpty()) {
            OneBotApi.sendG("[领地列表]\n暂无领地");
            return;
        }

        StringBuilder sb = new StringBuilder("[领地列表]\n");
        int i = 0;
        for (var info : landInfoMap.values()) {
            if (i >= 20) {
                sb.append("... 共 ").append(landInfoMap.size()).append(" 个领地");
                break;
            }
            String ownerName = IdentityUtil.getUserID(info.getLandOwnerQQ());
            sb.append(info.getLandName())
              .append(" | ").append(ownerName != null ? ownerName : "?")
              .append(" | ").append(info.getLandPile().length).append("区块")
              .append(" | 剩余").append(info.getlandDuration()).append("天\n");
            i++;
        }
        OneBotApi.sendG(sb.toString());
    }

    private static void handleQueryLand(String landName) {
        landName = landName.trim();
        for (var info : landInfoMap.values()) {
            if (info.getLandName().equals(landName)) {
                String ownerName = IdentityUtil.getUserID(info.getLandOwnerQQ());
                String msg = "[领地信息]\n" +
                        "名称: " + info.getLandName() + "\n" +
                        "地主: " + (ownerName != null ? ownerName : "?") + "\n" +
                        "面积: " + info.getLandPile().length + " 区块\n" +
                        "租期: 剩余 " + info.getlandDuration() + " 天";
                OneBotApi.sendG(msg);
                return;
            }
        }
        OneBotApi.sendG("未找到名为 " + landName + " 的领地");
    }
}
