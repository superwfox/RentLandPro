package sudark2.Sudark.rentLandPro.LandLogic;

import org.bukkit.scheduler.BukkitRunnable;
import sudark2.Sudark.rentLandPro.OneBotRelated.OneBotApi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static sudark2.Sudark.rentLandPro.RentLandPro.get;

public class LandRequestManager {

    public static final long REQUEST_TIMEOUT_TICKS = 2 * 60 * 20L;
    public static final long COOLDOWN_MS = 2 * 60 * 1000L;

    public static final class PendingRequest {
        public final String requesterQQ;
        public final String requesterName;
        public final Long landId;
        public final String landName;
        public final String ownerQQ;
        public BukkitRunnable timeoutTask;

        public PendingRequest(String requesterQQ, String requesterName, Long landId, String landName, String ownerQQ) {
            this.requesterQQ = requesterQQ;
            this.requesterName = requesterName;
            this.landId = landId;
            this.landName = landName;
            this.ownerQQ = ownerQQ;
        }
    }

    private static final ConcurrentHashMap<String, List<PendingRequest>> requestsByOwner = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Long> requesterCooldown = new ConcurrentHashMap<>();

    public static boolean isOnCooldown(String requesterQQ) {
        Long last = requesterCooldown.get(requesterQQ);
        if (last == null) return false;
        if (System.currentTimeMillis() - last >= COOLDOWN_MS) {
            requesterCooldown.remove(requesterQQ);
            return false;
        }
        return true;
    }

    public static long cooldownRemainingSec(String requesterQQ) {
        Long last = requesterCooldown.get(requesterQQ);
        if (last == null) return 0;
        long left = COOLDOWN_MS - (System.currentTimeMillis() - last);
        return Math.max(0, left / 1000);
    }

    public static void markUsed(String requesterQQ) {
        requesterCooldown.put(requesterQQ, System.currentTimeMillis());
    }

    public static synchronized void addRequest(PendingRequest req) {
        List<PendingRequest> list = requestsByOwner.computeIfAbsent(req.ownerQQ, k -> new ArrayList<>());
        list.add(req);

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                PendingRequest removed = removeRequest(req.ownerQQ, req);
                if (removed != null) {
                    OneBotApi.sendP(removed.requesterQQ, "[领地] 系统已经拒绝你的领地申请");
                }
            }
        };
        task.runTaskLater(get(), REQUEST_TIMEOUT_TICKS);
        req.timeoutTask = task;
    }

    public static synchronized List<PendingRequest> getRequests(String ownerQQ) {
        List<PendingRequest> list = requestsByOwner.get(ownerQQ);
        if (list == null) return List.of();
        return new ArrayList<>(list);
    }

    public static synchronized PendingRequest takeByIndex(String ownerQQ, int index1Based) {
        List<PendingRequest> list = requestsByOwner.get(ownerQQ);
        if (list == null || index1Based < 1 || index1Based > list.size()) return null;
        PendingRequest req = list.remove(index1Based - 1);
        if (list.isEmpty()) requestsByOwner.remove(ownerQQ);
        if (req.timeoutTask != null) {
            try { req.timeoutTask.cancel(); } catch (Exception ignored) {}
        }
        return req;
    }

    private static synchronized PendingRequest removeRequest(String ownerQQ, PendingRequest target) {
        List<PendingRequest> list = requestsByOwner.get(ownerQQ);
        if (list == null) return null;
        if (list.remove(target)) {
            if (list.isEmpty()) requestsByOwner.remove(ownerQQ);
            return target;
        }
        return null;
    }
}
