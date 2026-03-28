package sudark2.Sudark.rentLandPro.LandLogic;

import sudark2.Sudark.rentLandPro.File.BinaryEditor;

import static sudark2.Sudark.rentLandPro.File.LandFunctionsManager.landFunctionFlags;
import static sudark2.Sudark.rentLandPro.File.LandMembersManager.landMembers;

public class LandManager {

    public static void removeLandInfo(Long landId) {
        landInfoMap.remove(landId);
        landMembers.remove(landId);
        landFunctionFlags.remove(landId);
        BinaryEditor.saveAll();
    }

}
