package sudark2.Sudark.rentLandPro.Command;

import org.bukkit.entity.Player;
import sudark2.Sudark.rentLandPro.InventoryMenu.LandHomeMenu;

public class CommandExecutor implements org.bukkit.command.CommandExecutor {
    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {

        if(sender instanceof Player pl) {
            LandHomeMenu.openLandHomeMenu(pl);
            return true;

        }
        return false;
    }
}
