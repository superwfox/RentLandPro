package sudark2.Sudark.rentLandPro.Command;

import org.bukkit.entity.Player;
import sudark2.Sudark.rentLandPro.InventoryMenu.LandHomeMenu;
import sudark2.Sudark.rentLandPro.Listener.LandCreationListener;

public class CommandExecutor implements org.bukkit.command.CommandExecutor {
    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (!(sender instanceof Player pl)) return false;

        if (args.length == 0) {
            LandHomeMenu.openLandHomeMenu(pl);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "confirm" -> {
                int days = 1;
                if (args.length >= 2) {
                    try {
                        days = Integer.parseInt(args[1]);
                        if (days < 1) {
                            pl.sendMessage("§e租期至少为 §b1 §e天");
                            return true;
                        }
                    } catch (NumberFormatException e) {
                        pl.sendMessage("§e请输入有效的天数");
                        return true;
                    }
                }
                LandCreationListener.confirmLandCreation(pl, days);
            }
            case "cancel" -> LandCreationListener.cancelLandCreation(pl);
            default -> pl.sendMessage("§7未知子命令: " + args[0]);
        }
        return true;
    }
}
