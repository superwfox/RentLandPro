package sudark2.Sudark.rentLandPro.Command;

import org.bukkit.entity.Player;

public class CommandExecutor implements org.bukkit.command.CommandExecutor {
    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {

        if(sender instanceof Player pl)
        return false;
    }
}
