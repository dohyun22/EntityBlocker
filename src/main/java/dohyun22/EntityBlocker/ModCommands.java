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
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.config.Property;

public class ModCommands extends CommandBase{
	private List aliases;
	private final String moreInfo = "Type " + EnumChatFormatting.YELLOW + "/entityblocker help" + EnumChatFormatting.WHITE + " or " + EnumChatFormatting.YELLOW + "/ebl help" + EnumChatFormatting.WHITE + " for more info.";
	
	public ModCommands () {
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
						EnumChatFormatting.AQUA + ":: EntityBlocker Commands ::",
						EnumChatFormatting.YELLOW + "/ebl list " + EnumChatFormatting.WHITE + "-Shows the list of banned entities.",
						EnumChatFormatting.YELLOW + "/ebl list add <Entity Name> " + EnumChatFormatting.WHITE +"-Adds the entity to the blacklist. (Case-sensitive)",
						EnumChatFormatting.YELLOW + "/ebl list del <Entity Name> " + EnumChatFormatting.WHITE + "-Removes the entity from the blacklist. (Case-sensitive)",
						EnumChatFormatting.YELLOW + "/ebl remove " + EnumChatFormatting.WHITE + "-Removes banned entities from the loaded chunks",
						EnumChatFormatting.YELLOW + "/ebl reload " + EnumChatFormatting.WHITE + "-Reloads the config"
				});
				
			} else if (arg0.equals("list")) {
				if (strings.length == 1) {
					String str = "";
					
					for (int i = 0; i < EntityBlocker.entities.size(); i++) {
						str += (EntityBlocker.entities.get(i) + (i < EntityBlocker.entities.size()-1 ? ", " : ""));
					}
					
					sendMsg(sender, new String [] {
							EnumChatFormatting.AQUA + ":: List of banned entities ::",
							str
					});
				} else if (strings.length > 2) {
					if (strings[1].equals("add") || strings[1].equals("del")) {
						boolean remove = strings[1].equals("del");
						boolean b = modifyList(strings[2], remove);
						String msg = "";
						
						if (remove) {
							msg = b ? EnumChatFormatting.GREEN + "Successfully removed " + strings[2] + " from the blacklist" 
									: EnumChatFormatting.RED + "Can't remove " + strings[2] + " from the blacklist!";
						} else {
							msg = b ? EnumChatFormatting.GREEN + "Successfully added " + strings[2] + " to the blacklist" 
									: EnumChatFormatting.RED + "Can't add " + strings[2] + " to the blacklist!";
						}
						
						sendMsg(sender, msg);
					} else {
						sendMsg(sender, EnumChatFormatting.RED + "Invalied arguments! " + EnumChatFormatting.WHITE + moreInfo);
					}
				} else {
					sendMsg(sender, EnumChatFormatting.RED + "Not enough arguments! " + EnumChatFormatting.WHITE + moreInfo);
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
				
				sendMsg(sender, EnumChatFormatting.YELLOW + "Removed " + count + " banned entities!");
			} else if (arg0.equals("reload")) {
				reloadConfig();
				sendMsg(sender, EnumChatFormatting.GREEN + "Successfully Reloaded the config");
			} else {
				sendMsg(sender, EnumChatFormatting.RED + "Invalied arguments! " + EnumChatFormatting.WHITE + moreInfo);
			}
		} else {
			sendMsg(sender, EnumChatFormatting.RED + "Not enough arguments! " + EnumChatFormatting.WHITE + moreInfo);
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
			if ("list".equals(str[0])) {
				if ("add".equals(str[1])) {
					ret = getListOfStringsMatchingLastWord(str, (String[])EntityList.func_151515_b().toArray(new String[0]));
				} else if ("del".equals(str[1])) {
					String[] s = new String[EntityBlocker.entities.size()];
					for (int i = 0; i < s.length; i++)
						s[i] = EntityBlocker.entities.get(i);
					ret = getListOfStringsMatchingLastWord(str, s);
				}
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
