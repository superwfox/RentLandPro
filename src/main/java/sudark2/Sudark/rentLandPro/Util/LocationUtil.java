package sudark2.Sudark.rentLandPro.Util;

import org.bukkit.Chunk;
import org.bukkit.World;

public class LocationUtil {

    public static int getHighestSolidY(World w, int worldX, int worldZ) {
        int referY = w.getHighestBlockYAt(worldX, worldZ);

        if (referY < -60) return referY;

        while (!w.getBlockAt(worldX, referY, worldZ).getType().isOccluding())
            referY--;

        return referY;
    }
}