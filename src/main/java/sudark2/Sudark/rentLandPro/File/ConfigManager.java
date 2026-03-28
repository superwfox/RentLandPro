package sudark2.Sudark.rentLandPro.File;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import static sudark2.Sudark.rentLandPro.RentLandPro.get;

public class ConfigManager {

    public static String GroupId = "101";

    public static void loadConfig() {
        get().saveDefaultConfig();
        FileConfiguration config = get().getConfig();

        GroupId = config.getString("QQGroupId");
    }
}
