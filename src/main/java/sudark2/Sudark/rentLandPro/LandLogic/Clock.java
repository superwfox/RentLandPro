package sudark2.Sudark.rentLandPro.LandLogic;

import org.bukkit.scheduler.BukkitRunnable;
import sudark2.Sudark.rentLandPro.File.BinaryEditor;
import sudark2.Sudark.rentLandPro.File.LandInfoManager;
import sudark2.Sudark.rentLandPro.File.LandMembersManager;
import sudark2.Sudark.rentLandPro.Listener.GeneralListener;
import sudark2.Sudark.rentLandPro.OneBotRelated.OneBotApi;

import java.util.ArrayList;
import java.util.List;

import static sudark2.Sudark.rentLandPro.File.LandInfoManager.landInfoMap;
import static sudark2.Sudark.rentLandPro.RentLandPro.get;

public class Clock {

    public static void startDailyTask() {
        // 每小时 tick 一次，存储单位为小时
        new BukkitRunnable() {
            @Override
            public void run() {
                updateAllLandDurations();
            }
        }.runTaskTimer(get(), 20L * 60, 20L * 60 * 60);
    }

    public static void updateAllLandDurations() {
        List<Long> expiredLands = new ArrayList<>();

        for (var entry : landInfoMap.entrySet()) {
            Long landId = entry.getKey();
            LandInfoManager.LandInfo info = entry.getValue();
            int currentDuration = info.getlandDuration();

            if (currentDuration <= 0) {
                expiredLands.add(landId);
                continue;
            }

            int newDuration = currentDuration - 1;
            info.setLandDuration(newDuration);

            String ownerQQ = info.getLandOwnerQQ();
            if (newDuration == 120) {
                OneBotApi.sendP(ownerQQ, "[领地提醒] 您的领地 " + info.getLandName() + " 剩余 5天 租期");
            } else if (newDuration == 48) {
                OneBotApi.sendP(ownerQQ, "[领地提醒] 您的领地 " + info.getLandName() + " 剩余 2天 租期，请尽快续费");
            } else if (newDuration == 0) {
                OneBotApi.sendP(ownerQQ, "[领地警告] 您的领地 " + info.getLandName() + " 已停止保护！");
                expiredLands.add(landId);
            }
        }

        for (Long landId : expiredLands) {
            removeLand(landId);
        }

        BinaryEditor.writeLandInfo();
    }

    public static void removeLand(Long landId) {
        LandInfoManager.LandInfo info = landInfoMap.get(landId);
        Long[] chunkKeys = null;

        if (info != null) {
            chunkKeys = info.getLandPile();
            String ownerQQ = info.getLandOwnerQQ();
            OneBotApi.sendP(ownerQQ, "§e[领地通知] §f您的领地 §b" + info.getLandName() + " §f因租期到期已被删除");
        }

        landInfoMap.remove(landId);
        LandMembersManager.landMembers.remove(landId);
        sudark2.Sudark.rentLandPro.File.LandFunctionsManager.landFunctionFlags.remove(landId);

        if (chunkKeys != null) {
            GeneralListener.updateDenyChunksForLand(landId, chunkKeys);
        }

        BinaryEditor.saveAll();
        sudark2.Sudark.rentLandPro.File.LandFunctionsManager.rebuildWorldSets();
    }
}
