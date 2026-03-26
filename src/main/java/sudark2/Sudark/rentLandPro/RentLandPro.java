package sudark2.Sudark.rentLandPro;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import sudark2.Sudark.rentLandPro.OneBotRelated.OneBotClient;

import java.net.URI;
import java.net.URISyntaxException;

public final class RentLandPro extends JavaPlugin {

    public static OneBotClient client;

    @Override
    public void onEnable() {
        try {
            client = new OneBotClient(new URI("ws://127.0.0.1:3001"));
            client.connect();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

    }

    public static Plugin get(){
        return Bukkit.getPluginManager().getPlugin("RentLandPro");
    }
}
