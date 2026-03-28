package sudark2.Sudark.rentLandPro.File;

import java.io.File;

import static sudark2.Sudark.rentLandPro.RentLandPro.get;

public class FileManager {

    public static final String LandInfoFileName = "landInfo.dat";
    public static final String PermissionFileName = "permission.dat";
    public static final String FunctionsFileName = "functions.dat";

    public static void init() {
        checkFile(LandInfoFileName);
        checkFile(PermissionFileName);
        checkFile(FunctionsFileName);

        ConfigManager.loadConfig();
        CourierCSVLoader.refresh();
        BinaryEditor.loadAll();
    }

    private static void checkFile(String fileName) {
        File file = new File(get().getDataFolder(),fileName);
        if (!file.exists()) {
            try{
            file.createNewFile();
        }catch (Exception e) {
            }
        }
    }
}
