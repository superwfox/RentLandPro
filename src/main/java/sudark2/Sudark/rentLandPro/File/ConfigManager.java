package sudark2.Sudark.rentLandPro.File;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static sudark2.Sudark.rentLandPro.RentLandPro.get;

public class ConfigManager {

    public static String GroupId = "101";
    public static int ChunkPricePerDay = 5;
    public static Set<String> OpQQ = new HashSet<>();

    public static void loadConfig() {
        get().saveDefaultConfig();
        FileConfiguration config = get().getConfig();

        GroupId = config.getString("QQGroupId", "101");
        ChunkPricePerDay = config.getInt("ChunkPricePerDay", 5);

        List<String> opList = config.getStringList("OpQQ");
        OpQQ = new HashSet<>(opList);
    }

    public static int calculateRentCost(int chunkCount, int days) {
        return chunkCount * ChunkPricePerDay * days;
    }

    public static int calculateLandPrice(int area) {
        return (int) Math.pow(area, 0.8891);
    }
}
