package sudark2.Sudark.rentLandPro.OneBotRelated;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import sudark2.Sudark.rentLandPro.File.ConfigManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static sudark2.Sudark.rentLandPro.RentLandPro.client;

public class OneBotApi {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy/M/d-HH:mm");

    public static void sendG(String message) {
        if (client == null || !client.isOpen()) return;

        JsonObject params = new JsonObject();
        params.addProperty("group_id", ConfigManager.GroupId);
        params.addProperty("message", stripColor(message));
        params.addProperty("auto_escape", false);

        JsonObject request = new JsonObject();
        request.addProperty("action", "send_group_msg");
        request.add("params", params);

        try {
            client.send(request.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendP(String userQQ, String message) {
        if (client == null || !client.isOpen()) return;

        JsonObject params = new JsonObject();
        params.addProperty("user_id", userQQ);
        params.addProperty("message", stripColor(message));
        params.addProperty("auto_escape", false);

        JsonObject request = new JsonObject();
        request.addProperty("action", "send_private_msg");
        request.add("params", params);

        try {
            client.send(request.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendGroupAt(String userQQ) {
        if (client == null || !client.isOpen()) return;

        JsonObject atData = new JsonObject();
        atData.addProperty("qq", userQQ);

        JsonObject atSegment = new JsonObject();
        atSegment.addProperty("type", "at");
        atSegment.add("data", atData);

        JsonArray msgArray = new JsonArray();
        msgArray.add(atSegment);

        JsonObject params = new JsonObject();
        params.addProperty("group_id", ConfigManager.GroupId);
        params.add("message", msgArray);

        JsonObject request = new JsonObject();
        request.addProperty("action", "send_group_msg");
        request.add("params", params);

        try {
            client.send(request.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendLandCreatedNotice(String playerName, String ownerQQ, int chunkCount, int duration) {
        String timeStr = LocalDateTime.now().format(TIME_FMT);
        String msg = "[RENTLAND]\n" +
                "==============\n" +
                "地主：" + playerName + "\n" +
                "面积：" + chunkCount + " 区块\n" +
                "租期：" + duration + " 天\n" +
                "==============\n" +
                "[ " + timeStr + " ]";

        sendG(msg);
        sendGroupAt(ownerQQ);
    }

    public static void sendVisitorAlert(String ownerQQ, String visitorName, String landName) {
        String msg = "[领地提醒] 玩家 " + visitorName + " 进入了您的领地 " + landName;
        sendP(ownerQQ, msg);
    }

    private static String stripColor(String msg) {
        if (msg == null) return "";
        return msg.replaceAll("§[0-9a-fk-or]", "");
    }
}
