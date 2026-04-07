package sudark2.Sudark.rentLandPro.File;

import org.bukkit.Material;

import java.util.concurrent.ConcurrentHashMap;

public class LandInfoManager {

    public static ConcurrentHashMap<Long, LandInfo> landInfoMap = new ConcurrentHashMap<>();

    public static LandInfo findByChunk(Long chunkKey) {
        for (LandInfo info : landInfoMap.values()) {
            for (Long ck : info.getLandPile()) {
                if (ck.equals(chunkKey)) return info;
            }
        }
        return null;
    }

    public static class LandInfo {

        private final Long landId;
        private String landName;
        private String landOwnerQQ;
        // 租期(小时)：后端按小时倒计时，前端展示按天 (hours/24)
        private int landDuration;
        private Material landSignature;
        private Long[] landPile;
        private int[] teleportPoint;

        public LandInfo(Long landId, String landName, String landOwnerQQ, int landDuration, Material landSignature, Long[] landPile, int[] teleportPoint) {
            this.landId = landId;
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

        /** 用于前端显示的"X.XX 天" */
        public String getDurationDaysDisplay() {
            return String.format("%.2f", landDuration / 24.0);
        }

        public Long[] getLandPile() {
            return landPile;
        }

        public void setLandPile(Long[] landPile) {
            this.landPile = landPile;
        }

        public void addChunkToPile(Long chunkKey) {
            for (Long existing : landPile) {
                if (existing.equals(chunkKey)) return;
            }
            Long[] newPile = new Long[landPile.length + 1];
            System.arraycopy(landPile, 0, newPile, 0, landPile.length);
            newPile[landPile.length] = chunkKey;
            this.landPile = newPile;
            BinaryEditor.writeLandInfo();
        }

        public boolean removeChunkFromPile(Long chunkKey) {
            int index = -1;
            for (int i = 0; i < landPile.length; i++) {
                if (landPile[i].equals(chunkKey)) {
                    index = i;
                    break;
                }
            }
            if (index == -1) return false;
            if (landPile.length == 1) return false;

            Long[] newPile = new Long[landPile.length - 1];
            System.arraycopy(landPile, 0, newPile, 0, index);
            System.arraycopy(landPile, index + 1, newPile, index, landPile.length - index - 1);
            this.landPile = newPile;
            BinaryEditor.writeLandInfo();
            return true;
        }

        public int[] getTeleportPoint() {
            return teleportPoint;
        }

        public void setTeleportPoint(int[] teleportPoint) {
            this.teleportPoint = teleportPoint;
            BinaryEditor.writeLandInfo();
        }

        public Long getLandId() {
            return landId;
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
