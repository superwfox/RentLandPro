package sudark2.Sudark.rentLandPro;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import sudark2.Sudark.rentLandPro.Command.CommandExecutor;
import sudark2.Sudark.rentLandPro.Command.CommandTabCompleter;
import sudark2.Sudark.rentLandPro.File.FileManager;
import sudark2.Sudark.rentLandPro.LandLogic.Clock;
import sudark2.Sudark.rentLandPro.Listener.*;
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

        FileManager.init();
        sudark2.Sudark.rentLandPro.File.LandFunctionsManager.rebuildWorldSets();

        Bukkit.getPluginManager().registerEvents(new GeneralListener(), this);
        Bukkit.getPluginManager().registerEvents(new LandHomeMenuListener(), this);
        Bukkit.getPluginManager().registerEvents(new LandDetailsMenuListener(), this);
        Bukkit.getPluginManager().registerEvents(new LandFunctionsMenuListener(), this);
        Bukkit.getPluginManager().registerEvents(new LandCreationListener(), this);
        Bukkit.getPluginManager().registerEvents(new LandMembersMenuListener(), this);

        getCommand("land").setExecutor(new CommandExecutor());
        getCommand("land").setTabCompleter(new CommandTabCompleter());

        Clock.startDailyTask();

        getLogger().info("§bRentLandPro §f已加载");
    }

    @Override
    public void onDisable() {
        LandCreationListener.cleanupAllPending();
        sudark2.Sudark.rentLandPro.File.BinaryEditor.saveAll();
        getLogger().info("§bRentLandPro §f已保存数据并卸载");
    }

    public static Plugin get() {
        return Bukkit.getPluginManager().getPlugin("RentLandPro");
    }
}
