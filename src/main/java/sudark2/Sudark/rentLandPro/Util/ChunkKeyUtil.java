package sudark2.Sudark.rentLandPro.Util;

import org.bukkit.Chunk;

public class ChunkKeyUtil {

    public static Long genKey(Chunk chunk) {
        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();

        return ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
    }

}
