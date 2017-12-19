package eyspuhc;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DepthDispCommand implements CommandExecutor 
{
	@Override
	public boolean onCommand(CommandSender sender, Command command, String arg2, String[] arg3) 
	{
		Player player = (Player) sender;
		player.sendMessage("Your Y coordinate is: " + player.getLocation().getY());
		
		return false;
	}

}
