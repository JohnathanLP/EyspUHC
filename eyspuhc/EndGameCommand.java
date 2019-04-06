package eyspuhc;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class EndGameCommand implements CommandExecutor
{
	@Override
	public boolean onCommand(CommandSender sender, Command command, String arg2, String[] arg3)
	{
		GameState.end();

		return false;
	}

}
