package sudark2.Sudark.rentLandPro.File;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LandFunctionsManager {

    public static final int FLAG_BREAK = 1;
    public static final int FLAG_PLACE = 1 << 1;
    public static final int FLAG_INTERACT = 1 << 2;
    public static final int FLAG_EXPLODE = 1 << 3;
    public static final int FLAG_FLUID = 1 << 4;

    public static final ConcurrentHashMap<Long, Integer> landFunctionFlags = new ConcurrentHashMap<>();

    public static volatile Set<Long> denyExplodeChunks = new HashSet<>();
    public static volatile Set<Long> denyFluidChunks = new HashSet<>();

    public static void rebuildWorldSets() {
        Set<Long> explode = new HashSet<>();
        Set<Long> fluid = new HashSet<>();

        for (LandInfoManager.LandInfo info : LandInfoManager.landInfoMap.values()) {
            int flags = landFunctionFlags.getOrDefault(info.getLandId(), 0);
            for (Long ck : info.getLandPile()) {
                if ((flags & FLAG_EXPLODE) == 0) explode.add(ck);
                if ((flags & FLAG_FLUID) == 0) fluid.add(ck);
            }
        }

        denyExplodeChunks = explode;
        denyFluidChunks = fluid;
    }

    public static void setFlagBreak(long landId, boolean flag) {
        int raw = landFunctionFlags.getOrDefault(landId, 0);
        raw = flag ? (raw | FLAG_BREAK) : (raw & ~FLAG_BREAK);
        landFunctionFlags.put(landId, raw);
    }

    public static void setFlagPlace(long landId, boolean flag) {
        int raw = landFunctionFlags.getOrDefault(landId, 0);
        raw = flag ? (raw | FLAG_PLACE) : (raw & ~FLAG_PLACE);
        landFunctionFlags.put(landId, raw);
    }

    public static void setFlagInteract(long landId, boolean flag) {
        int raw = landFunctionFlags.getOrDefault(landId, 0);
        raw = flag ? (raw | FLAG_INTERACT) : (raw & ~FLAG_INTERACT);
        landFunctionFlags.put(landId, raw);
    }

    public static void setFlagExplode(long landId, boolean flag) {
        int raw = landFunctionFlags.getOrDefault(landId, 0);
        raw = flag ? (raw | FLAG_EXPLODE) : (raw & ~FLAG_EXPLODE);
        landFunctionFlags.put(landId, raw);
    }

    public static void setFlagFluid(long landId, boolean flag) {
        int raw = landFunctionFlags.getOrDefault(landId, 0);
        raw = flag ? (raw | FLAG_FLUID) : (raw & ~FLAG_FLUID);
        landFunctionFlags.put(landId, raw);
    }
}
