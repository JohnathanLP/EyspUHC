package eyspuhc;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;
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
			spawn = new Location(world, x+3.5, y, z+0.5);
			
			//set worldborders
			WorldBorder border = world.getWorldBorder();
			border.setCenter(new Location(world, x,y,z));
			border.setSize(1000);
			
			//create platform
			buildPlatform(world);
			//BuildTemplate.build(new Location(world, x,y,z), new File("UHCConfig/platform.txt"));
			
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
			liveTeam.setOption(Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OWN_TEAM);
			//TODO fix this
			//liveTeam.setColor(ChatColor.GREEN);
			Bukkit.dispatchCommand(sender, "scoreboard teams option live color green");
			Team deadTeam = scoreboard.registerNewTeam("dead");
			deadTeam.setOption(Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OTHER_TEAMS);
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
			
			//TODO fix this
			//give all players all recipes
			Bukkit.dispatchCommand(sender, "recipe give @a *");
			
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
		Scanner platformIn;
		try 
		{
			File file = new File("UHCConfig/platform.txt");
			platformIn = new Scanner(file);
			//size of platform (from file)
			int sizeX = Integer.parseInt(platformIn.next());
			int sizeZ = Integer.parseInt(platformIn.next());
			int sizeY = Integer.parseInt(platformIn.next());
			//indicates the top corner of the platform
			int platX = x-Math.round(sizeX/2);
			int platY = y+2;
			int platZ = z-Math.round(sizeZ/2);
			
			String row = "";
			for(int i=0; i<sizeY; i++)
			{
				for(int j=0; j<sizeZ; j++)
				{
					row = platformIn.next();
					for(int k=0; k<sizeX; k++)
					{
						switch(row.charAt(k))
						{
						case '.':
							//place air
							new Location(world,platX+k, platY-i, platZ+j).getBlock().setType(Material.AIR);
							break;
						case 'B':
							//place barrier
							new Location(world,platX+k, platY-i, platZ+j).getBlock().setType(Material.BARRIER);
							break;
						case 'G':
							//place glass
							new Location(world,platX+k, platY-i, platZ+j).getBlock().setType(Material.GLASS);
							break;
						case 'g':
							//place glass pane
							new Location(world,platX+k, platY-i, platZ+j).getBlock().setType(Material.THIN_GLASS);
							break;
						case 'O':
							//place obsidian
							new Location(world,platX+k, platY-i, platZ+j).getBlock().setType(Material.OBSIDIAN);
							break;
						case 'i':
							//place ice
							new Location(world,platX+k, platY-i, platZ+j).getBlock().setType(Material.ICE);
							break;
						case 'I':
							//place packed ice
							new Location(world,platX+k, platY-i, platZ+j).getBlock().setType(Material.PACKED_ICE);
							break;
						case 'W':
							//place water
							new Location(world,platX+k, platY-i, platZ+j).getBlock().setType(Material.WATER);
							break;
						case 'S':
							//place sea lantern
							new Location(world,platX+k, platY-i, platZ+j).getBlock().setType(Material.SEA_LANTERN);
							break;
						case 'f':
							//place fire
							new Location(world,platX+k, platY-i, platZ+j).getBlock().setType(Material.FIRE);
							break;
						case 'Q':
							//place quartz block
							new Location(world,platX+k, platY-i, platZ+j).getBlock().setType(Material.QUARTZ_BLOCK);
							break;
						case 'N':
							//place netherrack
							new Location(world,platX+k, platY-i, platZ+j).getBlock().setType(Material.NETHERRACK);
							break;
						case 'L':
							//place lava
							new Location(world,platX+k, platY-i, platZ+j).getBlock().setType(Material.LAVA);
							break;
						case 'b':
							//place iron bars
							new Location(world,platX+k, platY-i, platZ+j).getBlock().setType(Material.IRON_FENCE);
							break;
						case 's':
							//place stone brick
							new Location(world,platX+k, platY-i, platZ+j).getBlock().setType(Material.SMOOTH_BRICK);
							break;
						}
					}
				}
			}
			
			Bukkit.broadcastMessage("Platform dimensions: " + sizeX + ", " + sizeZ + ", " + sizeY);
			
			platformIn.close();
		} 
		catch (FileNotFoundException e) 
		{
			Bukkit.broadcastMessage("Problem constructing platform: File not found!");
			e.printStackTrace();
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
			if(t.getName().equals(playerIn.getName()))
			{
				Bukkit.broadcastMessage("Existing Player");
				return true;
			}
		}
		
		Bukkit.broadcastMessage("New Player");
		
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
		//test if only one player is still alive
		int remaining = Bukkit.getScoreboardManager().getMainScoreboard().getTeam("live").getEntries().size();
		if(remaining == 1)
		{
			String winnerName = Bukkit.getScoreboardManager().getMainScoreboard().getTeam("live").getName();
			Bukkit.broadcastMessage("Game Over! " + winnerName + "is the winner!");
		}
		else
		{
			Bukkit.broadcastMessage("There are " + remaining + " players remaining. Fight on!");
		}
		return true;
	}
	
	/*
	 * END EVENT FUNCTIONS
	 */
}
