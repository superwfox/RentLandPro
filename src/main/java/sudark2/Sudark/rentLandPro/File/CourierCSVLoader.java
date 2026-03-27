package sudark2.Sudark.rentLandPro.File;

import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.ConcurrentHashMap;

public class CourierCSVLoader {

    public static ConcurrentHashMap<String,String> QQ2ID = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String,String> ID2QQ = new ConcurrentHashMap<>();

    public static void refresh() {
        File csvFile = new File(Bukkit.getServer().getPluginManager().getPlugin("Courier").getDataFolder(), "courier.csv");

        if(!csvFile.exists())return;

        try(var reader = Files.newBufferedReader(csvFile.toPath())){
            String line;
            while((line = reader.readLine()) != null){
                String[] splitLine = line.split(",");
                if(splitLine.length < 3) continue;

                String qq = splitLine[2];
                String id = splitLine[1];

                QQ2ID.put(qq,id);
                ID2QQ.put(id,qq);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
