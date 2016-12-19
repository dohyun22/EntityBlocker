package dohyun22.EntityBlocker;

import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

@Mod(modid="EntityBlocker", name="EntityBlocker", version="1.1.0MC1.7.10", dependencies="required-after:FML", acceptableRemoteVersions="*")
public class EntityBlocker
{
    public static Property bannedEntities;
    public static List<String> entities = new ArrayList();
    public static Configuration cfg;
    public static final String cfg_comment = "add the name of the entity that you want to ban here. Case sensitive. Example: PrimedTnt;IC2.Itnt;IC2.Nuke";

	@Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        cfg = new Configuration(event.getSuggestedConfigurationFile());
        try {
            cfg.load();

            bannedEntities = cfg.get("Settings", "BannedEntities", "");
            bannedEntities.comment = cfg_comment;
            
        } catch (Exception e) {
        	System.err.println("Error loading config.");
            throw new RuntimeException(e);
        } finally {
            cfg.save();
        }
    }
	
	@Mod.EventHandler
	public void load(FMLInitializationEvent event) {
		entities = parseString(bannedEntities.getString());
		MinecraftForge.EVENT_BUS.register(new ModEvHandler());
	}
	
	@Mod.EventHandler
	public void serverLoad(FMLServerStartingEvent event) {
		event.registerServerCommand(new ModCommands());
	}
	
	public static List<String> parseString(String str) {
		List<String> list = new ArrayList();
		try {
			String[] s = str.split(";");
			for (String s2 : s) {
				if (s2 != null && !(s2.replace(" ", "").equals("")))
					list.add(s2);
			}
		} catch (Exception e) {
			System.err.println("Error parsing config.");
		}
		return list;
	}
}
