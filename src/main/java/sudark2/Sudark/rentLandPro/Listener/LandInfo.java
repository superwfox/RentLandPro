package sudark2.Sudark.rentLandPro.Listener;

public class LandInfo {

    private String landName;
    private String landOwner;
    private String landPrice;
    private Long[] landPile;

    public LandInfo(String landName, String landOwner, String landPrice, Long[] landPile) {
        this.landName = landName;
        this.landOwner = landOwner;
        this.landPrice = landPrice;
        this.landPile = landPile;
    }

    public String getLandName() {
        return landName;
    }

    public String getLandOwner() {
        return landOwner;
    }

    public String getLandPrice() {
        return landPrice;
    }

    public Long[] getLandPile() {
        return landPile;
    }
}
