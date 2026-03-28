package sudark2.Sudark.rentLandPro.File;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LandMembersManager {

    public record LandMembership(Set<String> operators, Set<String> members) {
    }

    public static ConcurrentHashMap<Long, LandMembership> landMembers = new ConcurrentHashMap<>();

    public static String removeLandMember(Long landId, String memberQQ) {
        if (landMembers.containsKey(landId)) {
            if (landMembers.get(landId).operators.contains(memberQQ)) {
                landMembers.get(landId).operators.remove(memberQQ);
            } else if (landMembers.get(landId).members.contains(memberQQ)) {
                landMembers.get(landId).members.remove(memberQQ);
            }
            BinaryEditor.writeLandMembers();
            return "删除成功";
        } else return "该领地不存在";

    }

    public static String addLandMember(Long landId, String memberQQ, boolean isOperator) {
        if (landMembers.containsKey(landId)) {
            if (isOperator) {
                landMembers.get(landId).operators.add(memberQQ);
            } else
                landMembers.get(landId).members.add(memberQQ);
            BinaryEditor.writeLandMembers();
            return "添加成功";
        } else return "该领地不存在";
    }

    public static LandMembership getLandMembers(Long landId) {
        return landMembers.getOrDefault(landId, null);
    }

}
