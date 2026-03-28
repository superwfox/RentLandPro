package sudark2.Sudark.rentLandPro.File;

import org.bukkit.configuration.file.FileConfiguration;

import static sudark2.Sudark.rentLandPro.RentLandPro.get;

public class ConfigManager {

    public static String GroupId = "101";
    public static int ChunkPricePerDay = 5;

    public static void loadConfig() {
        get().saveDefaultConfig();
        FileConfiguration config = get().getConfig();

        GroupId = config.getString("QQGroupId", "101");
        ChunkPricePerDay = config.getInt("ChunkPricePerDay", 5);
    }

    public static int calculateRentCost(int chunkCount, int days) {
        return chunkCount * ChunkPricePerDay * days;
    }
}
