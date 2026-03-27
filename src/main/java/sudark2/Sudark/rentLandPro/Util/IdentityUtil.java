package sudark2.Sudark.rentLandPro.Util;

import sudark2.Sudark.rentLandPro.File.CourierCSVLoader;

import static sudark2.Sudark.rentLandPro.File.CourierCSVLoader.ID2QQ;
import static sudark2.Sudark.rentLandPro.File.CourierCSVLoader.QQ2ID;

public class IdentityUtil {

    public static String getUserQQ(String name){
        CourierCSVLoader.refresh();
        return ID2QQ.get(name);
    }

    public static String getUserID(String QQ){
        CourierCSVLoader.refresh();
        return QQ2ID.get(QQ);
    }

}
