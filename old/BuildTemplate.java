package eyspuhc;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class BuildTemplate 
{
	@SuppressWarnings("deprecation")
	public static void build(Location origin, File file)
	{
		Scanner scanner;
		try 
		{
			scanner = new Scanner(file);
			World world = origin.getWorld();
			int sizeX = Integer.parseInt(scanner.next());
			int sizeZ = Integer.parseInt(scanner.next());
			int sizeY = Integer.parseInt(scanner.next());
			ArrayList<String> symbols = new ArrayList<String>();
			ArrayList<Integer> ids    = new ArrayList<Integer>();
			long originX = Math.round(origin.getX());
			long originY = Math.round(origin.getY());
			long originZ = Math.round(origin.getZ());
			int idCounts = Integer.parseInt(scanner.next());
			for(int i=0; i<idCounts; i++)
			{
				symbols.add(scanner.next());
				ids.add(Integer.parseInt(scanner.next()));
				Bukkit.broadcastMessage(String.format("added symbol: %s,%i", symbols.get(i), ids.get(i)));
			}
			
			String row = "";
			for(int i=0; i<sizeY; i++)
			{
				for(int j=0; j<sizeZ; j++)
				{
					row = scanner.next();
					for(int k=0; k<sizeX; k++)
					{
						String symbol = Character.toString(row.charAt(k));
						for(int t=0; t<symbols.size(); t++)
						{
							if(symbol.equals(symbols.get(t)))
							{
								new Location(world, originX, originY, originZ).getBlock().setTypeId(ids.get(t));
							}
						}
					}
				}
			}
			
			origin.getBlock().setTypeId(20);
			scanner.close();
		} 
		catch (FileNotFoundException e) 
		{
			// TODO Auto-generated catch block
			Bukkit.broadcastMessage("Failed to open file");
			e.printStackTrace();
		}
		
	}
}
