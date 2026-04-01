package sudark2.Sudark.rentLandPro.Util;

import org.bukkit.World;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class LevelNameUtil {

    public static World MainWorld;

    public static String getLevelName() {
        Properties properties = new Properties();
        File propertiesFile = new File("server.properties");

        try (FileReader reader = new FileReader(propertiesFile)) {
            properties.load(reader);
            return properties.getProperty("level-name");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
