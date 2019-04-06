package eyspuhc;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;
import java.util.Random;

import org.bukkit.block.Block;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;

public enum GameState
{    
	//TODO use this
	INACTIVE, SETUP, PHASE1, PHASE2, COMPLETE;

	//private static List<Player> playerList;
	private static List<UUID> playerIDList = new ArrayList<UUID>();
	private static Location spawn;
	private static String currPhase = "inactive";
	private static int x = 0;
	private static int y = 200;
	private static int z = 0;

    private static int wbsize_initial = 1000;            // length of a side (initially)
    private static int wbsize_final = 50;               // length of a side (finally)
    private static int wbcollapse_delay = 300;          // time until sides collapse (in seconds)
    private static int wbcollapse_duration = 1200;        // time that sides take to collapse (in seconds)

	/*
	 * PHASE INITIALIZATION
	 */
	public static boolean setup(CommandSender sender)
	{
	    if(currPhase.equals("inactive"))
		{
		    // set phase
			currPhase = "setup";

			// create objects
			Player admin = (Player) sender;
            World overworld = admin.getWorld();
			List<World> worlds = Bukkit.getWorlds();

            // set spawn Location
            spawn = new Location(overworld, x+0.5, y, z+0.5);

			// announce setup
			Bukkit.broadcastMessage("UHC Setup in Progress");

            // set day
            overworld.setTime(1000);

            // TODO clear weather

            // set world borders
            overworld.getWorldBorder().setCenter(spawn);
            overworld.getWorldBorder().setSize(wbsize_initial);

            // loop through all dimensions to set game rules
            for (World world: worlds)
            {
                // disable daylight cycle
                world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
                // disable natural regeneration
                world.setGameRule(GameRule.NATURAL_REGENERATION, false);
                // disable weather
                world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
                // reduced debug screen
                world.setGameRule(GameRule.REDUCED_DEBUG_INFO, true);
                // disable achievement announceAchievements
                world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
                // set difficulty to peaceful
                world.setDifficulty(Difficulty.PEACEFUL);
            }

            // build platform - TODO Call twice to allow carpet and glass panes to form correctly
            buildPlatform(overworld, false);
            buildPlatform(overworld, false);

            // TODO remove item drops (specifically for broken entities from the platform gen)
	        Bukkit.dispatchCommand(sender, "kill @e[type=item]");
      
            // create teams
            Scoreboard scoreboard = Bukkit.getServer().getScoreboardManager().getMainScoreboard();
            Set<Team> existingTeams = scoreboard.getTeams();
            // delete existing teams
            if (!existingTeams.isEmpty())
			{
			    for(Team team:existingTeams)
				{
				    scoreboard.getTeam(team.getName()).unregister();
				}
			}
            Team liveTeam = scoreboard.registerNewTeam("live");
            Team deadTeam = scoreboard.registerNewTeam("dead");

            // configure teams
            liveTeam.setOption(Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OWN_TEAM);
            liveTeam.setColor(ChatColor.GREEN);
	        deadTeam.setOption(Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OTHER_TEAMS);
            deadTeam.setColor(ChatColor.RED);

            // clear objectives
            if (!scoreboard.getObjectives().isEmpty())
            {
                for (Objective objective: scoreboard.getObjectives())
                {
                    objective.unregister();
                }
            }

            // add health to tab menu
            scoreboard.registerNewObjective("health", "health", "Health");
            scoreboard.getObjective("health").setDisplaySlot(DisplaySlot.PLAYER_LIST);
            scoreboard.getObjective("health").setRenderType(RenderType.HEARTS);

            // loop through all players currently connected, setup each
            for (Player player: Bukkit.getOnlinePlayers())
            {
                //Bukkit.broadcastMessage("Detected player " + player.getDisplayName() + " with UUID " + player.getUniqueId());
                playerSetup(player.getUniqueId());
            }

            return true;
        }

        // handle other phases
        else if (currPhase.equals("setup"))
		{
			Bukkit.broadcastMessage("Setup has already begun!");
		}
		else if (currPhase.equals("phase1") || currPhase.equals("phase2"))
		{
			Bukkit.broadcastMessage("Game is already in progress");
		}
		else if (currPhase.equals("complete"))
		{
			Bukkit.broadcastMessage("Game has already ended!");
		}
		return false;
	}

	public static boolean begin(CommandSender sender)
	{
	    if(currPhase.equals("setup"))
		{
		    //set phase
			currPhase = "phase1";

            // create objects
            Player admin = (Player) sender;
            World overworld = admin.getWorld();
            List<World> worlds = Bukkit.getWorlds();

			//announce setup
			Bukkit.broadcastMessage("Start!");

			//set day
			overworld.setTime(1000);

            // TODO clear weather

            // loop through all dimensions to set game rules
            for (World world: worlds)
            {
                // enable daylight cycle
                world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
                // enable weather
                world.setGameRule(GameRule.DO_WEATHER_CYCLE, true);
                // set difficulty to hard
                world.setDifficulty(Difficulty.HARD);
            } 

			// remove platform
			removePlatform(overworld);

			//make needed changes for each player
			for(UUID t : playerIDList)
			{
				playerBegin(t);
			}

			//TODO fix this
			// scatter players
			Bukkit.dispatchCommand(sender, "spreadplayers 0 0 100 " + wbsize_initial/2 +" false @a");
			//spreadplayers <x> <z> <spreadDistance> <maxRange> <respectTeams> <player …>

			//TODO fix this
			//give all players all recipes
			Bukkit.dispatchCommand(sender, "recipe give @a *");

			// set worldborders to shrink
			// TODO update phase to phase2
			Bukkit.getScheduler().scheduleSyncDelayedTask(Bukkit.getPluginManager().getPlugin("EyspUHC"), new Runnable(){
			                    public void run(){Bukkit.broadcastMessage("Borders are now shrinking!");    // announce borders shrinking
                                overworld.getWorldBorder().setSize(wbsize_final, wbcollapse_duration);}     // set borders to shrink
                    			}, wbcollapse_delay*20);                                                    //after delay
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

    public static boolean end()
	{
        playerIDList = new ArrayList<UUID>();
        return true;
    }

	/*
	 * END PHASE INITIALIZATION
	 */

	/*
	 * HELPER FUNCTIONS
	 */

    // Platform builder method - currently takes a boolean value for testing to enable/disable
    // helpful debugging messages. The intent is to eventually add a platform testing command
    // for easy testing of new platform.txt files - with additional information printed out.
    // Currently it is only called with false
	public static boolean buildPlatform(World world, boolean testing)
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
			int platY = y+sizeY-2;
			int platZ = z-Math.round(sizeZ/2);
			//offsets player from center (positive in the x direction)
			int spawnOffset = platformIn.nextInt();
			spawn.add(spawnOffset, 0, 0);
			// get material count, initialize arrays for symbols and materials
			int materialCount = platformIn.nextInt();
			List<Character> symbols = new ArrayList<Character>();
			List<Material> materials = new ArrayList<Material>();
            
            // for testing only - to disable pass false to this method
            /*
            if(testing)
            {
                Bukkit.broadcastMessage("Materials Found: " +  Material.values().length);
                for(Material testing: Material.values())
                {
                    System.out.print(testing.toString());
                }
                Bukkit.broadcastMessage("Platform dimensions: " + sizeX + ", " + sizeZ + ", " + sizeY);
                Bukkit.broadcastMessage("Material Count: " + materialCount);
            }
            */

            // read symbols from file, get materials from strings
			for(int i=0; i<materialCount; i++)
			{
				symbols.add(platformIn.next().charAt(0));
                String nameIn = platformIn.next();
                Material temp = Material.getMaterial(nameIn);
                materials.add(temp);
			}

            // for testing only - to disable pass false to this method
            if(testing)
            {
                for (Material material: materials)
                {
                    if(material != null)
                        Bukkit.broadcastMessage(symbols.get(materials.indexOf(material)) + " : "+ material.toString());
                    else
                        Bukkit.broadcastMessage(symbols.get(materials.indexOf(material)) + " : Error");
                }
            }
            
            // set blocks
			for(int i=0; i<sizeY; i++)
			{
				for(int j=0; j<sizeZ; j++)
				{
					for(int k=0; k<sizeX; k++)
					{
						char symbolIn = platformIn.next().charAt(0);
						new Location(world, platX+k, platY-i, platZ+j).getBlock().setType(materials.get(symbols.indexOf(symbolIn)), true);
					}
				}
			}

			platformIn.close();
		}
        // handle file open error
		catch (FileNotFoundException e)
		{
			Bukkit.broadcastMessage("Problem constructing platform: File not found!");
			e.printStackTrace();
		}
		return true;
	}

	public static boolean removePlatform(World world)
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
			int platY = y+sizeY-2;
			int platZ = z-Math.round(sizeZ/2);

			for(int i=0; i<sizeY; i++)
			{
				for(int j=0; j<sizeZ; j++)
				{
					for(int k=0; k<sizeX; k++)
					{
						new Location(world, platX+k, platY-i, platZ+j).getBlock().setType(Material.AIR);
					}
				}
			}

			platformIn.close();
		}
		catch (FileNotFoundException e)
		{
			Bukkit.broadcastMessage("Problem removing platform: File not found!");
			e.printStackTrace();
		}
		return true;
	}

    public static void playerSetup(UUID playerIDIn)
    {
        // test if player has already connected
        if (!playerIDList.contains(playerIDIn))
        {
            // get player, add to player list
            Player player = Bukkit.getPlayer(playerIDIn);
            playerIDList.add(playerIDIn);

            Bukkit.broadcastMessage("Setting up player " + player.getDisplayName());

            // move player to spawn, set spawn
            player.teleport(spawn);
            player.setBedSpawnLocation(spawn, true);
            // damage player to enable tab menu health display
            player.setHealth(10);
            // clear inventory
            player.getInventory().clear();
            // set gamemode to adventure, make players unkillable and unable to do damage, clear inventory
            player.setGameMode(GameMode.ADVENTURE);
            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 720000, 255, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 720000, 255, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 720000, 255, false, false));
            // add to live team
            Bukkit.getScoreboardManager().getMainScoreboard().getTeam("live").addEntry(player.getName());
        }
        else
        {
            Bukkit.broadcastMessage("Existing Player");
        }
    }

	public static boolean playerBegin(UUID playerIDIn)
	{
		//get player
		Player playerIn = Bukkit.getPlayer(playerIDIn);
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
    public static string getRemainingMessage(int remaining)
    {
        Random rand = new Random();

        int sel = rand.nextInt(0);
        switch(sel)
        {
            case 0: return "There are only " + remaining + " players remaining! Fight on!";
        }
    }
    */

	/*
	 * END HELPER FUNCTIONS
	 */

    /*
	 * BEGIN EVENT FUNCTIONS
	 */

	public static void playerJoin(Player playerIn)
	{
		// handle player as needed
		if(currPhase.equals("setup"))
		{
            // game has not begun, setup player
			playerSetup(playerIn.getUniqueId());
		}
		else if(currPhase.equals("inactive"))
		{
			// game is not active, do nothing
		}
		else
		{
			// game is in progress, "kill" player
			playerIDList.add(playerIn.getUniqueId());
			playerDeath(playerIn);
		}
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
            currPhase = "inactive";
            end();
		}
		else if(remaining > 1)
		{
			Bukkit.broadcastMessage("There are " + remaining + " players remaining. Fight on!");
		}
        else if(remaining == 0)
        {
            Bukkit.broadcastMessage("There are no players remaining! What a tragedy!");
            currPhase = "inactive";
            end();
        }
        else if(remaining < 0)
        {
            Bukkit.broadcastMessage("Um, how did that happen");
            currPhase = "inactive";
            end();
        }
		return true;
	}

	/*
	 * END EVENT FUNCTIONS
	 */
}
