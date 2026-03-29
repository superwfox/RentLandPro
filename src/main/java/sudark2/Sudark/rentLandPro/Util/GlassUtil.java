package sudark2.Sudark.rentLandPro.Util;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Set;

public class GlassUtil {

    private static final Material GLASS = Material.YELLOW_STAINED_GLASS;

    public static void placeGlass(Long chunkKey, World world) {
        int chunkX = (int) (chunkKey >> 32);
        int chunkZ = (int) (chunkKey & 0xFFFFFFFFL);
        int baseX = chunkX * 16;
        int baseZ = chunkZ * 16;
        int y = world.getMaxHeight() - 1;

        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                Block block = world.getBlockAt(baseX + dx, y, baseZ + dz);
                if (block.getType() == Material.AIR) {
                    block.setType(GLASS, false);
                }
            }
        }
    }

    public static void removeGlass(Long chunkKey, World world) {
        int chunkX = (int) (chunkKey >> 32);
        int chunkZ = (int) (chunkKey & 0xFFFFFFFFL);
        int baseX = chunkX * 16;
        int baseZ = chunkZ * 16;
        int y = world.getMaxHeight() - 1;

        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                Block block = world.getBlockAt(baseX + dx, y, baseZ + dz);
                if (block.getType() == GLASS) {
                    block.setType(Material.AIR, false);
                }
            }
        }
    }

    public static void cleanupAllGlass(Set<Long> allPendingChunks, World world) {
        for (Long chunkKey : allPendingChunks) {
            removeGlass(chunkKey, world);
        }
    }
}
