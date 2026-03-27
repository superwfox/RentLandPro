package sudark2.Sudark.rentLandPro.File;

import java.io.File;

import static sudark2.Sudark.rentLandPro.RentLandPro.get;

public class FileManager {

    public static void init() {
        checkFile("landInfo.dat");
        checkFile("permission.dat");
        checkFile("functions.dat");
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
