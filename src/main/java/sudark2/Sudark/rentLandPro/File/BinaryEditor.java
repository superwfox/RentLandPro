package sudark2.Sudark.rentLandPro.File;

import org.bukkit.Material;

import java.io.*;
import java.util.*;

import static sudark2.Sudark.rentLandPro.File.LandFunctionsManager.landFunctionFlags;
import static sudark2.Sudark.rentLandPro.File.LandMembersManager.landMembers;
import static sudark2.Sudark.rentLandPro.RentLandPro.get;

public class BinaryEditor {

    // =====================================================================
    //  landInfo.dat —— 领地基本信息
    // =====================================================================
    //  文件布局：
    //  ┌────────────────────────────────────────────┐
    //  │ [int]  recordCount        领地总数          │
    //  ├────────────────────────────────────────────┤
    //  │ 重复 recordCount 次：                       │
    //  │   [long] landId           领地ID            │
    //  │   [UTF]  landName         领地名称          │
    //  │   [UTF]  ownerQQ          领主QQ           │
    //  │   [int]  duration         租期(小时)        │
    //  │   [UTF]  materialName     标志方块名称       │
    //  │   [int]  chunkCount       区块数量          │
    //  │   [long × chunkCount]     区块 key 数组     │
    //  └────────────────────────────────────────────┘

    /**
     * 将内存中的领地信息集合写入 landInfo.dat
     */
    public static void writeLandInfo() {
        File file = new File(get().getDataFolder(), FileManager.LandInfoFileName);

        // try-with-resources: 括号里创建的流会在块结束时自动 close()，无需手动关闭
        try (DataOutputStream dos = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(file)))) {

            // 第 1 步：写入记录总数，读取时靠这个数字决定循环次数
            dos.writeInt(LandInfoManager.landInfoMap.size());

            // 第 2 步：逐条写入每个 LandInfo 的字段
            for (Map.Entry<Long, LandInfoManager.LandInfo> mapEntry : LandInfoManager.landInfoMap.entrySet()) {
                dos.writeLong(mapEntry.getKey());               // 领地ID（Map 的 key）

                LandInfoManager.LandInfo info = mapEntry.getValue();
                dos.writeUTF(info.getLandName());               // 字符串 → writeUTF
                dos.writeUTF(info.getLandOwnerQQ());
                dos.writeInt(info.getlandDuration());           // 整数   → writeInt
                dos.writeUTF(info.getLandSignature().name());   // Material 转 name() 存储

                // 变长数组：先写长度，再逐个写元素
                Long[] pile = info.getLandPile();
                dos.writeInt(pile.length);
                for (Long chunkKey : pile) {
                    dos.writeLong(chunkKey);                    // 长整数 → writeLong
                }

                int[] teleportPoint = info.getTeleportPoint();
                dos.writeInt(teleportPoint.length);
                for (int point : teleportPoint) {
                    dos.writeInt(point);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从 landInfo.dat 读取领地信息，填充到 landInfoManager.landInfoSet
     */
    public static void readLandInfo() {
        File file = new File(get().getDataFolder(), FileManager.LandInfoFileName);

        // 空文件直接跳过，避免 EOFException
        if (!file.exists() || file.length() == 0) return;

        try (DataInputStream dis = new DataInputStream(
                new BufferedInputStream(new FileInputStream(file)))) {

            // 读取顺序与写入完全镜像
            int recordCount = dis.readInt();

            for (int i = 0; i < recordCount; i++) {
                // 读取顺序必须和写入完全一致：先 landId，再各字段
                long landId = dis.readLong();
                String landName = dis.readUTF();
                String ownerQQ = dis.readUTF();
                int duration = dis.readInt();
                Material signature = Material.valueOf(dis.readUTF()); // name() → valueOf() 还原枚举

                int chunkCount = dis.readInt();
                Long[] pile = new Long[chunkCount];
                for (int j = 0; j < chunkCount; j++) {
                    pile[j] = dis.readLong();
                }

                int teleportPointCount = dis.readInt();
                int[] teleportPoint = new int[teleportPointCount];
                for (int j = 0; j < teleportPointCount; j++) {
                    teleportPoint[j] = dis.readInt();
                }

                LandInfoManager.landInfoMap.put(
                        landId, new LandInfoManager.LandInfo(landId, landName, ownerQQ, duration, signature, pile, teleportPoint));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // =====================================================================
    //  permission.dat —— 领地成员关系 (OP / 普通成员)
    // =====================================================================
    //  文件布局：
    //  ┌────────────────────────────────────────────┐
    //  │ [int]  landCount          领地总数          │
    //  ├────────────────────────────────────────────┤
    //  │ 重复 landCount 次：                         │
    //  │   [long] landId           领地ID            │
    //  │   [int]  opCount          OP数量            │
    //  │   [UTF × opCount]         OP 的 QQ 列表     │
    //  │   [int]  memberCount      普通成员数量       │
    //  │   [UTF × memberCount]     普通成员 QQ 列表   │
    //  └────────────────────────────────────────────┘

    /**
     * 将内存中的领地成员关系写入 permission.dat
     */
    public static void writeLandMembers() {
        File file = new File(get().getDataFolder(), FileManager.PermissionFileName);

        try (DataOutputStream dos = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(file)))) {

            dos.writeInt(landMembers.size());

            for (Map.Entry<Long, LandMembersManager.LandMembership> entry
                    : landMembers.entrySet()) {

                dos.writeLong(entry.getKey());                  // 领地ID

                Set<String> ops = entry.getValue().operators();
                dos.writeInt(ops.size());
                for (String op : ops) {
                    dos.writeUTF(op);
                }

                Set<String> members = entry.getValue().members();
                dos.writeInt(members.size());
                for (String member : members) {
                    dos.writeUTF(member);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从 permission.dat 读取领地成员关系，填充到 LandMembersManager.landMembers
     */
    public static void readLandMembers() {
        File file = new File(get().getDataFolder(), FileManager.PermissionFileName);
        if (!file.exists() || file.length() == 0) return;

        try (DataInputStream dis = new DataInputStream(
                new BufferedInputStream(new FileInputStream(file)))) {

            int landCount = dis.readInt();

            for (int i = 0; i < landCount; i++) {
                long landId = dis.readLong();

                int opCount = dis.readInt();
                Set<String> ops = new HashSet<>(opCount);
                for (int j = 0; j < opCount; j++) {
                    ops.add(dis.readUTF());
                }

                int memberCount = dis.readInt();
                Set<String> members = new HashSet<>(memberCount);
                for (int j = 0; j < memberCount; j++) {
                    members.add(dis.readUTF());
                }

                landMembers.put(landId, new LandMembersManager.LandMembership(ops, members));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // =====================================================================
    //  functions.dat —— 领地功能开关（管理外人在领地里能做什么）
    // =====================================================================
    //  文件布局：
    //  ┌────────────────────────────────────────────┐
    //  │ [int]  landCount          领地总数          │
    //  ├────────────────────────────────────────────┤
    //  │ 重复 landCount 次：                         │
    //  │   [long] landId           领地ID            │
    //  │   [int]  flags            功能位掩码         │
    //  └────────────────────────────────────────────┘
    //
    //  === 位掩码 (bitfield) 原理 ===
    //  一个 int 有 32 个 bit，每个 bit 代表一个功能的开/关：
    //    bit 0 (值 0x1) → 方块破坏   对应 GeneralListener.onPlayerBreak
    //    bit 1 (值 0x2) → 方块放置   对应 GeneralListener.onPlayerPlace
    //    bit 2 (值 0x4) → 玩家交互   对应 GeneralListener.onPlayerDig
    //    bit 3 (值 0x8) → 爆炸破坏   对应 GeneralListener.onPlayerInteract(BlockExplode)
    //
    //  1 = 允许外人执行, 0 = 禁止外人执行 (默认全禁止 = 0)
    //
    //  判断某功能是否开启：  (flags & FLAG_XXX) != 0
    //  开启某功能：          flags |= FLAG_XXX
    //  关闭某功能：          flags &= ~FLAG_XXX
    //  切换某功能：          flags ^= FLAG_XXX

    /**
     * 将内存中的领地功能设置写入 functions.dat
     */
    public static void writeLandFunctions() {
        File file = new File(get().getDataFolder(), FileManager.FunctionsFileName);

        try (DataOutputStream dos = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(file)))) {

            dos.writeInt(landFunctionFlags.size());

            for (Map.Entry<Long, Integer> entry : landFunctionFlags.entrySet()) {
                dos.writeLong(entry.getKey());     // 领地ID: 8 字节
                dos.writeInt(entry.getValue());    // flags:  4 字节，一个 int 装 32 个开关
            }
            // 每条记录只有 12 字节，极其紧凑

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从 functions.dat 读取领地功能设置，填充到 landFunctionFlags
     */
    public static void readLandFunctions() {
        File file = new File(get().getDataFolder(), FileManager.FunctionsFileName);
        if (!file.exists() || file.length() == 0) return;

        try (DataInputStream dis = new DataInputStream(
                new BufferedInputStream(new FileInputStream(file)))) {

            int landCount = dis.readInt();

            for (int i = 0; i < landCount; i++) {
                long landId = dis.readLong();
                int flags = dis.readInt();
                landFunctionFlags.put(landId, flags);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // =====================================================================
    //  便捷方法：一键全部读取 / 全部保存
    // =====================================================================

    /**
     * 服务器启动时调用，从三个 .dat 文件加载所有数据到内存
     */
    public static void loadAll() {
        readLandInfo();
        readLandMembers();
        readLandFunctions();
    }

    /**
     * 数据变更后调用，将内存数据持久化到三个 .dat 文件
     */
    public static void saveAll() {
        writeLandInfo();
        writeLandMembers();
        writeLandFunctions();
    }
}
