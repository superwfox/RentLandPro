package sudark2.Sudark.rentLandPro.Listener;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import sudark2.Sudark.rentLandPro.File.BinaryEditor;
import sudark2.Sudark.rentLandPro.File.LandInfoManager;
import sudark2.Sudark.rentLandPro.File.LandMembersManager;
import sudark2.Sudark.rentLandPro.InventoryMenu.LandMembersMenu;
import sudark2.Sudark.rentLandPro.Util.IdentityUtil;

import static sudark2.Sudark.rentLandPro.File.LandMembersManager.landMembers;
import static sudark2.Sudark.rentLandPro.InventoryMenu.LandMembersMenu.*;
import static sudark2.Sudark.rentLandPro.Util.ItemUtil.extractQQFromItem;
import static sudark2.Sudark.rentLandPro.Util.ItemUtil.qqPrefix;

public class LandMembersMenuListener implements Listener {

    @EventHandler
    public void onMembersMenuClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(LanMembersMenuTitle)) return;

        Player pl = (Player) event.getWhoClicked();
        Long landId = editingLandId.get(pl.getName());
        if (landId == null) return;

        Inventory topInv = event.getView().getTopInventory();
        Inventory clickedInv = event.getClickedInventory();
        int slot = event.getSlot();
        ClickType click = event.getClick();

        // 阻止所有可能将物品移出菜单的操作（shift-click, number key, double click等）
        if (click == ClickType.SHIFT_LEFT || click == ClickType.SHIFT_RIGHT ||
            click == ClickType.NUMBER_KEY || click == ClickType.DOUBLE_CLICK ||
            click == ClickType.SWAP_OFFHAND || click == ClickType.CONTROL_DROP ||
            click == ClickType.CREATIVE || click == ClickType.MIDDLE) {
            event.setCancelled(true);
            return;
        }

        if (clickedInv == topInv) {
            event.setCancelled(true);

            if (slot == 45) {
                Integer page = currentPage.get(pl.getName());
                if (page != null && page > 0) {
                    skipNextClose.add(pl.getName());
                    pl.closeInventory();
                    LandMembersMenu.openLandMembersMenu(pl, landId, page - 1);
                }
                return;
            }

            if (slot == 53) {
                Integer page = currentPage.get(pl.getName());
                if (page != null) {
                    skipNextClose.add(pl.getName());
                    pl.closeInventory();
                    LandMembersMenu.openLandMembersMenu(pl, landId, page + 1);
                }
                return;
            }

            if (slot == 49) {
                pl.closeInventory();
                return;
            }

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() != Material.PLAYER_HEAD) return;
            String memberQQ = extractQQFromItem(clickedItem);
            if (memberQQ == null) return;

            if (click == ClickType.LEFT) {
                removeMember(pl, landId, memberQQ);
                skipNextClose.add(pl.getName());
                pl.closeInventory();
                LandMembersMenu.openLandMembersMenu(pl, landId, currentPage.getOrDefault(pl.getName(), 0));
            } else if (click == ClickType.RIGHT) {
                promoteMember(pl, landId, memberQQ);
                skipNextClose.add(pl.getName());
                pl.closeInventory();
                LandMembersMenu.openLandMembersMenu(pl, landId, currentPage.getOrDefault(pl.getName(), 0));
            }

        } else if (clickedInv == pl.getInventory()) {
            // 玩家背包中的槽位9-35是临时存放头颅的区域，阻止所有操作
            if (slot >= 9 && slot <= 35) {
                event.setCancelled(true);
                ItemStack clickedItem = event.getCurrentItem();
                if (clickedItem != null && clickedItem.getType() == Material.PLAYER_HEAD) {
                    if (click == ClickType.LEFT) {
                        String playerName = getPlayerNameFromHead(clickedItem);
                        if (playerName != null) {
                            String playerQQ = IdentityUtil.getUserQQ(playerName);
                            addMember(pl, landId, playerQQ);
                            skipNextClose.add(pl.getName());
                            pl.closeInventory();
                            LandMembersMenu.openLandMembersMenu(pl, landId, currentPage.getOrDefault(pl.getName(), 0));
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onMembersMenuDrag(InventoryDragEvent event) {
        if (!event.getView().getTitle().equals(LanMembersMenuTitle)) return;

        for (int s : event.getRawSlots()) {
            if (s < 54) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onMembersMenuClose(InventoryCloseEvent event) {
        if (!event.getView().getTitle().equals(LanMembersMenuTitle)) return;

        Player pl = (Player) event.getPlayer();
        if (skipNextClose.remove(pl.getName())) return;
        
        // 延迟1tick恢复物品栏，确保关闭事件完全处理完毕
        Bukkit.getScheduler().runTaskLater(
            Bukkit.getPluginManager().getPlugin("RentLandPro"),
            () -> LandMembersMenu.restoreInventory(pl),
            1L
        );
    }

    private String getPlayerNameFromHead(ItemStack item) {
        if (item == null || item.getType() != Material.PLAYER_HEAD) return null;
        if (!(item.getItemMeta() instanceof SkullMeta skullMeta)) return null;

        if (skullMeta.getPlayerProfile() != null) {
            return skullMeta.getPlayerProfile().getName();
        }
        return skullMeta.getDisplayName();
    }

    private void addMember(Player pl, Long landId, String memberQQ) {
        LandMembersManager.LandMembership membership = landMembers.get(landId);
        if (membership == null) {
            pl.sendMessage("§7领地成员数据不存在");
            return;
        }

        if (membership.operators().contains(memberQQ) || membership.members().contains(memberQQ)) {
            pl.sendMessage("§7该玩家已是领地成员");
            return;
        }

        membership.members().add(memberQQ);
        BinaryEditor.writeLandMembers();
        GeneralListener.rebuildAllPlayersDenyChunks();
        pl.sendMessage("§b成功添加成员");
    }

    private void removeMember(Player pl, Long landId, String memberQQ) {
        LandMembersManager.LandMembership membership = landMembers.get(landId);
        if (membership == null) {
            pl.sendMessage("§7领地成员数据不存在");
            return;
        }

        LandInfoManager.LandInfo info = LandInfoManager.landInfoMap.get(landId);
        String ownerQQ = info != null ? info.getLandOwnerQQ() : null;

        if (memberQQ.equals(ownerQQ)) {
            pl.sendMessage("§e无法移除领地主人");
            return;
        }

        if (membership.operators().contains(memberQQ)) {
            String plQQ = IdentityUtil.getUserQQ(pl.getName());
            if (!memberQQ.equals(ownerQQ) && (plQQ == null || !plQQ.equals(ownerQQ))) {
                pl.sendMessage("§e只有领主可以移除领地管理");
                return;
            }
        }

        boolean removed = membership.operators().remove(memberQQ) || membership.members().remove(memberQQ);
        if (removed) {
            BinaryEditor.writeLandMembers();
            GeneralListener.rebuildAllPlayersDenyChunks();
            pl.sendMessage("§b成功移除成员");
        } else {
            pl.sendMessage("§7该玩家不是领地成员");
        }
    }

    private void promoteMember(Player pl, Long landId, String memberQQ) {
        LandMembersManager.LandMembership membership = landMembers.get(landId);
        if (membership == null) return;

        if (membership.operators().contains(memberQQ)) {
            pl.sendMessage("§7该玩家已是领地管理");
            return;
        }

        if (!membership.members().remove(memberQQ)) {
            pl.sendMessage("§7该玩家不是普通成员，无法升级");
            return;
        }

        membership.operators().add(memberQQ);
        BinaryEditor.writeLandMembers();
        pl.sendMessage("§b已将该成员升级为领地管理");
    }
}
