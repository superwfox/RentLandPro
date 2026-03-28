package sudark2.Sudark.rentLandPro.File;

import org.bukkit.Material;

import java.util.concurrent.ConcurrentHashMap;

public class landInfoManager {

    public static ConcurrentHashMap<
            Long,// chunkKey
            LandInfo
            > landInfoMap = new ConcurrentHashMap<>();

    public static void createLandInfo(Long landId, String landName, String landOwner, int landPrice, Material landSignature, Long[] landPile) {
        landInfoMap.put(landId, new LandInfo(landName, landOwner, landPrice, landSignature, landPile));
        BinaryEditor.writeLandInfo();
    }

    public static void removeLandInfo(Long landId) {
        landInfoMap.remove(landId);
        BinaryEditor.writeLandInfo();
    }

    public static LandInfo getLandInfo(Long landId) {
        return landInfoMap.get(landId);
    }

    public static class LandInfo {

        private String landName;
        private String landOwnerQQ;
        private int landDuration;
        private Material landSignature;
        private Long[] landPile;

        public LandInfo(String landName, String landOwnerQQ, int landDuration, Material landSignature, Long[] landPile) {
            this.landName = landName;
            this.landOwnerQQ = landOwnerQQ;
            this.landDuration = landDuration;
            this.landSignature = landSignature;
            this.landPile = landPile;
        }

        public String getLandName() {
            return landName;
        }

        public String getLandOwnerQQ() {
            return landOwnerQQ;
        }

        public int getlandDuration() {
            return landDuration;
        }

        public Long[] getLandPile() {
            return landPile;
        }

        public Long getLandId(LandInfo landInfo) {
            return landPile[0];
        }

        public Material getLandSignature() {
            return landSignature;
        }
    }
}
