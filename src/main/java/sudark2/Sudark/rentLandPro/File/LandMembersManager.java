package sudark2.Sudark.rentLandPro.File;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LandMembersManager {

    public record LandMembership(Set<String> operators, Set<String> members) {
    }

    public static ConcurrentHashMap<Long, LandMembership> landMembers = new ConcurrentHashMap<>();

    public static LandMembership getLandMembers(Long landId) {
        return landMembers.getOrDefault(landId, null);
    }

}
