package eyspuhc;

import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;

public enum GameState 
{
	//TODO use this
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
			
			// set reducedDebugInfo to true
			//TODO fix this
			//world.setGameRuleValue("reducedDebugInfo", "true");
			Bukkit.dispatchCommand(sender, "gamerule reducedDebugInfo true");
			
			// hide achievements
			//TODO fix this
			Bukkit.dispatchCommand(sender, "gamerule announceAchievements false");
			Bukkit.dispatchCommand(sender, "gamerule announceAdvancements false");
				
			//set location to a specific height above terrain
			spawn = new Location(world, x+0.5, y, z+0.5);
			
			//set worldborders
			WorldBorder border = world.getWorldBorder();
			border.setCenter(new Location(world, x,y,z));
			border.setSize(1000);
			
			//create platform
			buildPlatform(world);
			
			//create teams for live and dead players
			Scoreboard scoreboard = Bukkit.getServer().getScoreboardManager().getMainScoreboard();
			Set<Team> existingTeams = scoreboard.getTeams();
			if (!existingTeams.isEmpty())
			{
				for(Team temp:existingTeams)
				{
					scoreboard.getTeam(temp.getName()).unregister();
				}
			}			
			Team liveTeam = scoreboard.registerNewTeam("live");
			liveTeam.setOption(Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OTHER_TEAMS);
			//TODO fix this
			//liveTeam.setColor(ChatColor.GREEN);
			Bukkit.dispatchCommand(sender, "scoreboard teams option live color green");
			Team deadTeam = scoreboard.registerNewTeam("dead");
			deadTeam.setOption(Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OWN_TEAM);
			//TODO fix this
			//deadTeam.setColor(ChatColor.RED);
			Bukkit.dispatchCommand(sender, "scoreboard teams option dead color red");
			
			// add health to tab
			//TODO fix this
			Bukkit.dispatchCommand(sender, "scoreboard objectives add health health");
			Bukkit.dispatchCommand(sender, "scoreboard objectives setdisplay list health");
			
			// scan for all present players - add them to gamestate list
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
	
	public static boolean begin(CommandSender sender)
	{
		if(currPhase.equals("setup"))
		{
			//set phase
			currPhase = "phase1";
			
			//create objects
			Player player = (Player) sender;
			World world = player.getWorld();
			
			//announce setup
			Bukkit.broadcastMessage("Start!");
			
			//set day, enable night cycle
			world.setTime(1000);
			world.setGameRuleValue("doDaylightCycle", "true");
			
			// set difficulty to peaceful
			world.setDifficulty(Difficulty.HARD);
			
			//make needed changes for each player
			for(Player t : playerList)
			{
				playerBegin(t);
			}
			
			//TODO fix this
			// scatter players
			Bukkit.dispatchCommand(sender, "spreadplayers 0 0 200 500 false @a");
			//spreadplayers <x> <z> <spreadDistance> <maxRange> <respectTeams> <player …>
			
			// set worldborders to shrink
			// TODO balance these values
			Bukkit.getScheduler().scheduleSyncDelayedTask(Bukkit.getPluginManager().getPlugin("EyspUHC"), new Runnable(){
				public void run(){Bukkit.broadcastMessage("Borders are now shrinking!");
								  world.getWorldBorder().setSize(100, 100);}
			}, 60L);
			return true;
		}
		else if (currPhase.equals("inactive"))
		{
			Bukkit.broadcastMessage("You have to set up first!");
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
		int platX = x-5;
		int platY = y-2;
		int platZ = z-5;
		target.add(-5,-2,-5);
		for(int i=0; i<5; i++)
		{
			for(int j=0; j<10; j++)
			{
				for(int k=0; k<10; k++)
				{
					if(i == 0 || j == 0 || k == 0 || j == 9 || k == 9)
					{
						new Location(world,platX+j,platY+i,platZ+k).getBlock().setType(Material.BARRIER);
					}
					//target.add(1,0,0);
				}
				//target.add(-10,0,1);
			}
			//target.add(-10,1,-10);
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
		//add to live team
		Bukkit.getScoreboardManager().getMainScoreboard().getTeam("live").addEntry(playerIn.getName());
		//clear inventory
		playerIn.getInventory().clear();
		return true;
	}
	
	public static boolean playerBegin(Player playerIn)
	{
		//set gamemode, remove resistance, saturation and regeneration
		playerIn.setGameMode(GameMode.SURVIVAL);
		playerIn.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
		playerIn.removePotionEffect(PotionEffectType.SATURATION);
		playerIn.removePotionEffect(PotionEffectType.REGENERATION);
		//clear inventory
		playerIn.getInventory().clear();
		return true;
	}
	/* 
	 * END HELPER FUNCTIONS
	 */
	
	/*
	 * BEGIN EVENT FUNCTIONS
	 */
		
	public static boolean playerJoin(Player playerIn)
	{
		// return true if player already exists in playerList
		for(Player t : playerList)
		{
			if(t.equals(playerIn))
			{
				return true;
			}
		}
		
		// else, handle player as needed
		if(currPhase.equals("setup"))
		{
			playerList.add(playerIn);
			playerSetup(playerIn);
		}
		else if(currPhase.equals("inactive"))
		{
			// game is not active, do nothing
			return true;
		}
		else
		{
			// game is in progress, "kill" player
			playerList.add(playerIn);
			playerDeath(playerIn);
		}
		return true;
	}
	
	public static boolean playerDeath(Player playerIn)
	{
		//set player to spectator, move to dead team
		Bukkit.getScoreboardManager().getMainScoreboard().getTeam("dead").addEntry(playerIn.getName());
		playerIn.setGameMode(GameMode.SPECTATOR);
		return true;
	}
	
	/*
	 * END EVENT FUNCTIONS
	 */
}
