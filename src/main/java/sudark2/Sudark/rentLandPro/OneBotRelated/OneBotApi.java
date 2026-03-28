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
        JsonObject params = new JsonObject();
        params.addProperty("group_id", ConfigManager.GroupId);
        params.addProperty("message", message);
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
        JsonObject params = new JsonObject();
        params.addProperty("user_id", userQQ);
        params.addProperty("message", message);
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
        String msg = "§b[RENTLAND]\n" +
                "==============\n" +
                "§f地主：§e" + playerName + "\n" +
                "§f面积：§b" + chunkCount + " §7区块\n" +
                "§f租期：§e" + duration + " §7天\n" +
                "==============\n" +
                "§7[ " + timeStr + " ]";

        sendG(msg);
        sendGroupAt(ownerQQ);
    }

    public static void sendVisitorAlert(String ownerQQ, String visitorName, String landName) {
        String msg = "§e[领地提醒] §f玩家 §b" + visitorName + " §f进入了您的领地 §e" + landName;
        sendP(ownerQQ, msg);
    }
}
