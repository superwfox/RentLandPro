package sudark2.Sudark.rentLandPro.OneBotRelated;

import net.sf.json.JSONObject;

import static sudark2.Sudark.rentLandPro.File.ConfigManager.GroupId;

public class OneBotHandler {

    public static void MsgDivider(String rawMsg) {
        JSONObject jsonObject = JSONObject.fromObject(rawMsg);

        if (jsonObject.containsKey("post_type")) {

            String parsedMsg = parseMsg(jsonObject.getJSONArray("message").getJSONObject(0).getJSONObject("type"));

            switch (jsonObject.getString("message_type")) {
                case "private" -> PrivateMsgHandler(jsonObject.getJSONObject("sender"), parsedMsg);
                case "group" -> GroupMsgHandler(jsonObject.getJSONObject("sender"), parsedMsg);
            }

        }
    }

    public static void PrivateMsgHandler(JSONObject msgSender, String msg) {

    }

    public static void GroupMsgHandler(JSONObject msgSender, String msg) {
        String groupId = msgSender.getString("group_id");
        if(!groupId.equals(GroupId)) return;

        String UserQQ = msgSender.getString("user_id");

    }

    private static String parseMsg(JSONObject obj) {
        StringBuilder mb = new StringBuilder();
        String type = obj.optString("type");
        switch (type) {
            case "text" -> mb.append(obj.getJSONObject("data").getString("text"));
            case "face" -> mb.append("[表情]");
            case "image" -> mb.append("[图片]");
            case "at" -> {
                String nickname = obj.getJSONObject("data").getString("name");
                mb.append("[@").append(nickname).append("]");
            }
            case "reply" -> mb.append("[回复]");
            case "video" -> mb.append("[视频]");
            case "record" -> mb.append("[语音]");
            default -> mb.append("[未知消息]");
        }
        return mb.toString();
    }
}
