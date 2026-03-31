package sudark2.Sudark.rentLandPro.Util;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import sudark2.Sudark.rentLandPro.Listener.LandCreationListener;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

public class LandMapRenderer extends MapRenderer {

    private static final Color YELLOW = new Color(255, 255, 0);

    private final String playerName;
    private final Set<Integer> lastDrawnPixels = new HashSet<>();

    public LandMapRenderer(String playerName) {
        super(true);
        this.playerName = playerName;
    }

    @Override
    public void render(MapView map, MapCanvas canvas, Player player) {
        Set<Long> pending = LandCreationListener.pendingChunks.get(playerName);

        int centerX = map.getCenterX();
        int centerZ = map.getCenterZ();
        int scale = map.getScale().getValue();
        int bpp = 1 << scale;
        int mapMinX = centerX - 64 * bpp;
        int mapMinZ = centerZ - 64 * bpp;

        Set<Integer> currentPixels = new HashSet<>();
        if (pending != null && !pending.isEmpty()) {
            for (Long chunkKey : pending) {
                int chunkX = (int) (chunkKey >> 32);
                int chunkZ = (int) (chunkKey & 0xFFFFFFFFL);

                int blockMinX = chunkX * 16;
                int blockMinZ = chunkZ * 16;

                int pxStart = Math.max(0, (blockMinX - mapMinX) / bpp);
                int pxEnd = Math.min(127, (blockMinX + 15 - mapMinX) / bpp);
                int pzStart = Math.max(0, (blockMinZ - mapMinZ) / bpp);
                int pzEnd = Math.min(127, (blockMinZ + 15 - mapMinZ) / bpp);

                for (int px = pxStart; px <= pxEnd; px++) {
                    for (int pz = pzStart; pz <= pzEnd; pz++) {
                        currentPixels.add(px << 8 | pz);
                    }
                }
            }
        }

        for (int key : lastDrawnPixels) {
            if (!currentPixels.contains(key)) {
                canvas.setPixelColor(key >> 8, key & 0xFF, null);
            }
        }

        for (int key : currentPixels) {
            canvas.setPixelColor(key >> 8, key & 0xFF, YELLOW);
        }

        lastDrawnPixels.clear();
        lastDrawnPixels.addAll(currentPixels);
    }
}
