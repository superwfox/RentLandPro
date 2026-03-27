package sudark2.Sudark.rentLandPro.File;

import org.bukkit.Material;

import java.util.concurrent.ConcurrentHashMap;

public class LandInfo {

    private String landName;
    private String landOwnerQQ;
    private int landPrice;
    private Material landSignature;
    private Long[] landPile;

    public LandInfo(String landName, String landOwnerQQ, int landPrice, Material landSignature, Long[] landPile) {
        this.landName = landName;
        this.landOwnerQQ = landOwnerQQ;
        this.landPrice = landPrice;
        this.landSignature = landSignature;
        this.landPile = landPile;
    }

    public String getLandName() {
        return landName;
    }

    public String getLandOwnerQQ() {
        return landOwnerQQ;
    }

    public int getLandPrice() {
        return landPrice;
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
