package sudark2.Sudark.rentLandPro.File;

import it.unimi.dsi.fastutil.Pair;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class LandFunctionsManager {

    public static final int FLAG_BREAK = 1;          // bit 0: 方块破坏
    public static final int FLAG_PLACE = 1 << 1;     // bit 1: 方块放置
    public static final int FLAG_INTERACT = 1 << 2;  // bit 2: 玩家交互
    public static final int FLAG_EXPLODE = 1 << 3;   // bit 3: 爆炸破坏

    public static final ConcurrentHashMap<Long, Integer> landFunctionFlags = new ConcurrentHashMap<>();

    public static void setLandFunctions(long landId, List<Pair<Integer, Boolean>> functions) {
        for (Pair<Integer, Boolean> function : functions) {
            switch (function.left()) {
                case FLAG_BREAK:
                    setFlagBreak(landId, function.right());
                    break;
                case FLAG_PLACE:
                    setFlagPlace(landId, function.right());
                    break;
                case FLAG_INTERACT:
                    setFlagInteract(landId, function.right());
                    break;
                case FLAG_EXPLODE:
                    setFlagExplode(landId, function.right());
                    break;
            }
        }
        BinaryEditor.writeLandFunctions();
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

    public static boolean isFlagBreak(long landId) {
        return (landFunctionFlags.getOrDefault(landId, 0) & FLAG_BREAK) != 0;
    }

    public static boolean isFlagPlace(long landId) {
        return (landFunctionFlags.getOrDefault(landId, 0) & FLAG_PLACE) != 0;
    }

    public static boolean isFlagInteract(long landId) {
        return (landFunctionFlags.getOrDefault(landId, 0) & FLAG_INTERACT) != 0;
    }

    public static boolean isFlagExplode(long landId) {
        return (landFunctionFlags.getOrDefault(landId, 0) & FLAG_EXPLODE) != 0;
    }
}
