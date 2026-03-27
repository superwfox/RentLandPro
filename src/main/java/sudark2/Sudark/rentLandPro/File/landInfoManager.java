package sudark2.Sudark.rentLandPro.File;

import org.bukkit.Material;

import java.util.HashSet;
import java.util.Set;

public class landInfoManager {

    public static Set<LandInfo> landInfoSet = new HashSet<>();

    public static void createLandInfo(String landName, String landOwner, int landPrice, Material landSignature, Long[] landPile) {
        landInfoSet.add(new LandInfo(landName, landOwner, landPrice, landSignature, landPile));

    }

    public static void removeLandInfo(String landName) {
        landInfoSet.removeIf(landInfo -> landInfo.getLandName().equals(landName));

    }
}
