package eyspuhc;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public enum GameState 
{
	INACTIVE, SETUP, PHASE1, PHASE2, COMPLETE;
	
	private static List<Player> playerList;
	private static Location spawn;
	private static String currPhase = "inactive";
	private static int x = 0;
	private static int y = 150;
	private static int z = 0;
	
	/*
	 * PHASE INITIALIZATION
	 */
	public static boolean setup(CommandSender sender)
	{
		if(currPhase.equals("inactive"))
		{
			//set phase
			currPhase = "setup";
			
			//create objects
			Player player = (Player) sender;
			World world = player.getWorld();
			
			//announce setup
			Bukkit.broadcastMessage("UHC Setup in Progress");
			
			//set day, disable night cycle
			world.setTime(1000);
			world.setGameRuleValue("doDaylightCycle", "false");
			
			//TODO clear weather
			
			// set difficulty to peaceful
			world.setDifficulty(Difficulty.PEACEFUL);
			
			// set gamerule to no natural regeneration
			world.setGameRuleValue("naturalRegeneration", "false");
			
			//TODO set location to a specific height above terrain
			spawn = new Location(world, x+0.5, y, z+0.5);
			
			//TODO create platform
			buildPlatform(world);
			
			//scan for all present players - add them to gamestate list
			playerList = player.getWorld().getPlayers();
			
			//make needed changes for each player
			for(Player t : playerList)
			{
				playerSetup(t);
			}
			return true;
		}
		else if (currPhase.equals("setup"))
		{
			Bukkit.broadcastMessage("Setup has already begun!");
		}
		else if (currPhase.equals("phase1") || currPhase.equals("phase2"))
		{
			Bukkit.broadcastMessage("Game is in progress");
		}
		else if (currPhase.equals("complete"))
		{
			Bukkit.broadcastMessage("Game has ended!");
		}
		return false;
	}
	/*
	 * END PHASE INITIALIZATION
	 */
	
	/*
	 * HELPER FUNCTIONS
	 */
	public static boolean buildPlatform(World world)
	{
		//TODO program platform array input
		Location target = new Location(world,x,y,z);
		target.add(-5,-2,-5);
		for(int i=0; i<10; i++)
		{
			for(int j=0; j<10; j++)
			{
				target.getBlock().setType(Material.GLASS);
				target.add(1,0,0);
			}
			target.add(-10,0,1);
		}
		return true;
	}
	
	public static boolean playerSetup(Player playerIn)
	{
		//move player to platform, set spawn
		playerIn.setBedSpawnLocation(spawn, true);
		playerIn.teleport(spawn);
		//set gamemode, add resistance, saturation and regeneration
		playerIn.setGameMode(GameMode.ADVENTURE);
		playerIn.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 720000, 255, false, false));
		playerIn.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 720000, 255, false, false));
		playerIn.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 720000, 255, false, false));
		//clear inventory
		playerIn.getInventory().clear();
		return true;
	}
	/*
	 * END HELPER FUNCTIONS
	 */
}
