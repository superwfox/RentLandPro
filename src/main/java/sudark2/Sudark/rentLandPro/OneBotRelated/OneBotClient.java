package sudark2.Sudark.rentLandPro.OneBotRelated;


import org.bukkit.Bukkit;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

import static sudark2.Sudark.rentLandPro.RentLandPro.get;


public class OneBotClient extends WebSocketClient {

    public OneBotClient(URI serverUri) {
        super(serverUri);
    }

    public void onOpen(ServerHandshake serverHandshake) {
        get().getLogger().info("§f OneBotWebsocket 连接成功");
    }

    public void onMessage(String s) {
        OneBotHandler.MsgDivider(s);
    }

    public void onClose(int i, String s, boolean b) {
        retry();
    }

    public void onError(Exception e) {
    }

    private void retry() {
        if (!this.isOpen()) {
            new Thread(() -> {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    //WARN 没有必要
                }
                this.connect();
            }).start();
        }
    }
}
