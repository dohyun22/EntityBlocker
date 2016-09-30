package dohyun22.EntityBlocker;

import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.common.FMLCommonHandler;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.CommandBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.config.Property;

public class modCommands extends CommandBase{
	private List aliases;
	private final String moreInfo = "Type 」e/entityblocker help」f or 」e/ebl help」f for more info.";
	
	public modCommands () {
		this.aliases = new ArrayList();
		this.aliases.add("ebl");
		this.aliases.add("entityblocker");
	}
	
	@Override
	public int compareTo(Object arg0) {
		return 0;
	}

	@Override
	public String getCommandName() {
		return "entityblocker";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/entityblocker -" + moreInfo;
	}

	@Override
	public List getCommandAliases() {
		return this.aliases;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] strings) {
		if (strings.length > 0) {
			String arg0 = strings[0].toLowerCase();
			if (arg0.equals("help")) {
				sendMsg(sender, new String [] {
						"」b:: EntityBlocker Commands ::",
						"」e/ebl list 」f-Shows the list of banned entities.",
						"」e/ebl list add <Entity Name> 」f-Adds the entity to the blacklist. (Case-sensitive)",
						"」e/ebl list del <Entity Name> 」f-Removes the entity from the blacklist. (Case-sensitive)",
						"」e/ebl remove 」f-Removes banned entities from the loaded chunks",
						"」e/ebl reload 」f-Reloads the config"
				});
				
			} else if (arg0.equals("list")) {
				if (strings.length == 1) {
					String str = "";
					
					for (int i = 0; i < EntityBlocker.entities.size(); i++) {
						str += (EntityBlocker.entities.get(i) + (i < EntityBlocker.entities.size()-1 ? ", " : ""));
					}
					
					sendMsg(sender, new String [] {
							"」b:: List of banned entities ::",
							str
					});
				} else if (strings.length > 2) {
					if (strings[1].equals("add") || strings[1].equals("del")) {
						boolean remove = strings[1].equals("del");
						boolean b = modifyList(strings[2], remove);
						String msg = "";
						
						if (remove) {
							msg = b ? "」aSuccessfully removed " + strings[2] + " from the blacklist" 
									: "」cCan't remove " + strings[2] + " from the blacklist!";
						} else {
							msg = b ? "」aSuccessfully added " + strings[2] + " to the blacklist" 
									: "」cCan't add " + strings[2] + " to the blacklist!";
						}
						
						sendMsg(sender, msg);
					} else {
						sendMsg(sender, "」cInvalied arguments! 」f" + moreInfo);
					}
				} else {
					sendMsg(sender, "」cNot enough arguments! 」f" + moreInfo);
				}
			} else if (arg0.equals("remove")) {
				int count = 0;
				WorldServer[] ws = DimensionManager.getWorlds();
				
				for (WorldServer ws2 : ws) {
					for (Object e : ws2.loadedEntityList) {
						if (e instanceof Entity) {
							if (EntityBlocker.entities.contains(EntityList.getEntityString((Entity) e))) {
								((Entity) e).isDead = true;
								count++;
							}
						}
					}
				}
				
				sendMsg(sender, "」eRemoved " + count + " banned entities!");
			} else if (arg0.equals("reload")) {
				reloadConfig();
				sendMsg(sender, "」aSuccessfully Reloaded the config");
			} else {
				sendMsg(sender, "」cInvalied arguments! 」f" + moreInfo);
			}
		} else {
			sendMsg(sender, "」cNot enough arguments! 」f" + moreInfo);
		}
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] str) {
		
		List ret = null;
		if (str.length == 1) {
			ret = getListOfStringsMatchingLastWord(str, new String[] {"help", "list", "remove", "reload"});
		} else if (str.length == 2) {
			if ("list".equals(str[0])) {
				ret = getListOfStringsMatchingLastWord(str, new String[] {"add", "del"});
			}
		} else if (str.length == 3) {
			if ("list".equals(str[0]) && "del".equals(str[1])) {
				String[] s = new String[EntityBlocker.entities.size()];
				for (int i = 0; i < s.length; i++)
					s[i] = EntityBlocker.entities.get(i);
				ret = getListOfStringsMatchingLastWord(str, s);
			}
		}
		
		return ret;
	}

	@Override
	public boolean isUsernameIndex(String[] sender, int num) {
		return false;
	}
	
	public void sendMsg(ICommandSender sender, String msg) {
		sender.addChatMessage(new ChatComponentText(msg));
	}
	
	public void sendMsg(ICommandSender sender, String[] msgs) {
		for(String str : msgs) {
			sender.addChatMessage(new ChatComponentText(str));
		}
	}
	
	public boolean modifyList(String str, boolean remove) {
		if (str == null || str.contains(";") || str.contains(" "))
			return false;
		
		if (remove) {
			if (!EntityBlocker.entities.remove(str))
				return false;
		} else {
			if (EntityBlocker.entities.contains(str))
				return false;
			EntityBlocker.entities.add(str);
		}
		
		String str2 = "";
		for (int i = 0; i < EntityBlocker.entities.size(); i++) {
			str2 += (EntityBlocker.entities.get(i) + (i < EntityBlocker.entities.size()-1 ? ";" : ""));
		}
		
		EntityBlocker.cfg.load();
		Property prop = EntityBlocker.cfg.get("Settings", "BannedEntities", "");
		prop.comment = EntityBlocker.cfg_comment;
		prop.set(str2);
		EntityBlocker.cfg.save();
		return true;
	}
	
	public void reloadConfig() {
		EntityBlocker.cfg.load();
		Property prop = EntityBlocker.cfg.get("Settings", "BannedEntities", "");
		EntityBlocker.entities = EntityBlocker.parseString(prop.getString());
	}
}
