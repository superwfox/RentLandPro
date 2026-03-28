package sudark2.Sudark.rentLandPro.File;

import org.bukkit.Material;

import java.util.concurrent.ConcurrentHashMap;

public class LandInfoManager {

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
        private int[] teleportPoint;

        public LandInfo(String landName, String landOwnerQQ, int landDuration, Material landSignature, Long[] landPile, int[] teleportPoint) {
            this.landName = landName;
            this.landOwnerQQ = landOwnerQQ;
            this.landDuration = landDuration;
            this.landSignature = landSignature;
            this.landPile = landPile;
            this.teleportPoint = teleportPoint;
        }

        public String getLandName() {
            return landName;
        }

        public void setLandName(String landName) {
            this.landName = landName;
            BinaryEditor.writeLandInfo();
        }

        public String getLandOwnerQQ() {
            return landOwnerQQ;
        }

        public int getlandDuration() {
            return landDuration;
        }

        public void setLandDuration(int landDuration) {
            this.landDuration = landDuration;
            BinaryEditor.writeLandInfo();
        }

        public Long[] getLandPile() {
            return landPile;
        }

        public void addChunkToPile(Long chunkKey) {
            Long[] newPile = new Long[landPile.length + 1];
            System.arraycopy(landPile, 0, newPile, 0, landPile.length);
            newPile[landPile.length] = chunkKey;
            this.landPile = newPile;
            BinaryEditor.writeLandInfo();
        }

        public int[] getTeleportPoint() {
            return teleportPoint;
        }

        public void setTeleportPoint(int[] teleportPoint) {
            this.teleportPoint = teleportPoint;
            BinaryEditor.writeLandInfo();
        }

        public Long getLandId() {
            return landPile[0];
        }

        public Material getLandSignature() {
            return landSignature;
        }

        public void setLandSignature(Material landSignature) {
            this.landSignature = landSignature;
            BinaryEditor.writeLandInfo();
        }
    }
}
