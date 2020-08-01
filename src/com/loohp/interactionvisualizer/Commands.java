package com.loohp.interactionvisualizer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.loohp.interactionvisualizer.InteractionVisualizer.Modules;
import com.loohp.interactionvisualizer.Database.Database;
import com.loohp.interactionvisualizer.Managers.CustomBlockDataManager;
import com.loohp.interactionvisualizer.Managers.EffectManager;
import com.loohp.interactionvisualizer.Managers.EnchantmentManager;
import com.loohp.interactionvisualizer.Managers.MaterialManager;
import com.loohp.interactionvisualizer.Managers.MusicManager;
import com.loohp.interactionvisualizer.Managers.PacketManager;
import com.loohp.interactionvisualizer.Updater.Updater;
import com.loohp.interactionvisualizer.Utils.ChatColorUtils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;

public class Commands implements CommandExecutor, TabCompleter {
	
	private static Plugin plugin = InteractionVisualizer.plugin;

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!label.equalsIgnoreCase("interactionvisualizer") && !label.equalsIgnoreCase("iv")) {
			return true;
		}
		
		if (args.length == 0) {
			sender.sendMessage(ChatColor.AQUA + "[InteractionVisualizer] InteractionVisualizer written by LOOHP!");
			sender.sendMessage(ChatColor.GOLD + "[InteractionVisualizer] Running InteractionVisualizer version: " + plugin.getDescription().getVersion());
			return true;
		}
		
		if (args[0].equalsIgnoreCase("reload")) {
			if (sender.hasPermission("interactionvisualizer.reload")) {
				plugin.reloadConfig();
				InteractionVisualizer.loadConfig();
				EnchantmentManager.reloadConfig();
				EffectManager.reloadConfig();
				MusicManager.reloadConfig();
				CustomBlockDataManager.setup();
				MaterialManager.reloadConfig();
				sender.sendMessage(ChatColorUtils.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.Reload")));
			} else {
				sender.sendMessage(ChatColorUtils.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.NoPermission")));
			}
			return true;
		}
		
		if (args[0].equalsIgnoreCase("refresh")) {
			if (sender instanceof Player) {
				Player player = (Player) sender;
				if (player.hasPermission("interactionvisualizer.refresh")) {
					Bukkit.getScheduler().runTask(InteractionVisualizer.plugin, () -> PacketManager.reset(player));
				} else {
					sender.sendMessage(ChatColorUtils.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.NoPermission")));
				}
			} else {
				sender.sendMessage(ChatColorUtils.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.Toggle.Console")));
			}
			return true;
		}
		
		if (args[0].equalsIgnoreCase("update")) {
			if (sender.hasPermission("interactionvisualizer.update")) {
				sender.sendMessage(ChatColor.AQUA + "[InteractionVisualizer] InteractionVisualizer written by LOOHP!");
				sender.sendMessage(ChatColor.GOLD + "[InteractionVisualizer] You are running InteractionVisualizer version: " + plugin.getDescription().getVersion());
				Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
					String version = Updater.checkUpdate();
					if (version.equals("latest")) {
						sender.sendMessage(ChatColor.GREEN + "[InteractionVisualizer] You are running the latest version!");
					} else {
						Updater.sendUpdateMessage(sender, version);
					}
				});
			} else {
				sender.sendMessage(ChatColorUtils.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.NoPermission")));
			}
			return true;
		}
		
		if (args[0].equalsIgnoreCase("toggle")) {
			if (sender.hasPermission("interactionvisualizer.toggle")) {
				if (args.length == 2) {
					if (!(sender instanceof Player)) {
						sender.sendMessage(ChatColorUtils.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.Toggle.Console")));
						return true;
					}
					Player player = (Player) sender;
					Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
						switch (args[1].toLowerCase()) {
						case "itemstand":
							Toggle.toggle(sender, player, Modules.ITEMSTAND);
							break;
						case "itemdrop":
							Toggle.toggle(sender, player, Modules.ITEMDROP);
							break;
						case "hologram":
							Toggle.toggle(sender, player, Modules.HOLOGRAM);
							break;
						case "all":
							HashMap<Modules, Boolean> info = Database.getPlayerInfo(player);
							boolean toggle = true;
							int truecount = 0;
							for (boolean value : info.values()) {
								truecount = value ? truecount + 1 : truecount;
								if ((double) truecount > ((double) info.size() / 2.0)) {
									toggle = false;
									break;
								}
							}
							for (Entry<Modules, Boolean> entry : info.entrySet()) {
								if (entry.getValue() != toggle) {
									Toggle.toggle(sender, player, entry.getKey());
								}
							}
							break;
						default:
							sender.sendMessage(ChatColorUtils.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.Toggle.Modes")));
						}
					});
					return true;
				} else if (args.length == 3) {
					if (sender instanceof Player) {
						if (Bukkit.getPlayer(args[2]) != null) {
							if (!Bukkit.getPlayer(args[2]).equals((Player) sender)) {
								if (!sender.hasPermission("interactionvisualizer.toggle.others")) {
									sender.sendMessage(ChatColorUtils.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.NoPermission")));
									return true;
								}
							}
						}
					}
					if (Bukkit.getPlayer(args[2]) == null) {
						sender.sendMessage(ChatColorUtils.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.Toggle.PlayerNotFound")));
						return true;
					}
					Player player = Bukkit.getPlayer(args[2]);
					Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
						switch (args[1].toLowerCase()) {
						case "itemstand":
							Toggle.toggle(sender, player, Modules.ITEMSTAND);
							break;
						case "itemdrop":
							Toggle.toggle(sender, player, Modules.ITEMDROP);
							break;
						case "hologram":
							Toggle.toggle(sender, player, Modules.HOLOGRAM);
							break;
						case "all":
							HashMap<Modules, Boolean> info = Database.getPlayerInfo(player);
							boolean toggle = true;
							int truecount = 0;
							for (boolean value : info.values()) {
								truecount = value ? truecount + 1 : truecount;
								if ((double) truecount > ((double) info.size() / 2.0)) {
									toggle = false;
									break;
								}
							}
							for (Entry<Modules, Boolean> entry : info.entrySet()) {
								if (entry.getValue() != toggle) {
									Toggle.toggle(sender, player, entry.getKey());
								}
							}
							break;
						default:
							sender.sendMessage(ChatColorUtils.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.Toggle.Modes")));
						}
					});
					return true;
				} else {
					sender.sendMessage(ChatColorUtils.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.Toggle.Usage")));
				}				
				return true;
			}
			sender.sendMessage(ChatColorUtils.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.NoPermission")));
			return true;
		}
		
		if (args[0].equalsIgnoreCase("ethereal")) {
			TextComponent text = new TextComponent("She is Imaginary~~");
			text.setColor(ChatColor.YELLOW);
			TextComponent bone1 = new TextComponent("Nana's ");
			bone1.setColor(ChatColor.GOLD);
			TranslatableComponent bone2 = new TranslatableComponent("item.minecraft.bone");
			bone2.setColor(ChatColor.YELLOW);
			TextComponent bone3 = new TextComponent("\n§7Lost §6In-§dMaginary~~");
			bone1.addExtra(bone2);
			bone1.addExtra(bone3);
			text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[] {bone1}));
			text.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.instagram.com/narliar/"));
			sender.spigot().sendMessage(text);
			return true;
		}
		
		sender.sendMessage(ChatColorUtils.translateAlternateColorCodes('&', Bukkit.spigot().getConfig().getString("messages.unknown-command")));
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> tab = new LinkedList<String>();
		if (!label.equalsIgnoreCase("interactionvisualizer") && !label.equalsIgnoreCase("iv")) {
			return tab;
		}
		
		switch (args.length) {
		case 0:
			if (sender.hasPermission("interactionvisualizer.reload")) {
				tab.add("reload");
			}
			if (sender.hasPermission("interactionvisualizer.update")) {
				tab.add("update");
			}
			if (sender.hasPermission("interactionvisualizer.toggle")) {
				tab.add("toggle");
			}
			if (sender.hasPermission("interactionvisualizer.refresh")) {
				tab.add("refresh");
			}
			return tab;
		case 1:
			if (sender.hasPermission("interactionvisualizer.reload")) {
				if ("reload".startsWith(args[0].toLowerCase())) {
					tab.add("reload");
				}
			}
			if (sender.hasPermission("interactionvisualizer.update")) {
				if ("update".startsWith(args[0].toLowerCase())) {
					tab.add("update");
				}
			}
			if (sender.hasPermission("interactionvisualizer.toggle")) {
				if ("toggle".startsWith(args[0].toLowerCase())) {
					tab.add("toggle");
				}
			}
			if (sender.hasPermission("interactionvisualizer.refresh")) {
				if ("refresh".startsWith(args[0].toLowerCase())) {
					tab.add("refresh");
				}
			}
			return tab;
		case 2:
			if (args[0].equalsIgnoreCase("toggle")) {
				if (sender.hasPermission("interactionvisualizer.toggle")) {
					if ("itemstand".startsWith(args[1].toLowerCase())) {
						tab.add("itemstand");
					}
					if ("itemdrop".startsWith(args[1].toLowerCase())) {
						tab.add("itemdrop");
					}
					if ("hologram".startsWith(args[1].toLowerCase())) {
						tab.add("hologram");
					}
					if ("all".startsWith(args[1].toLowerCase())) {
						tab.add("all");
					}
				}
			}
			return tab;
		case 3:
			if (args[0].equalsIgnoreCase("toggle")) {
				if (sender.hasPermission("interactionvisualizer.toggle")) {
					if (args[1].toLowerCase().equals("itemstand") || args[1].toLowerCase().equals("itemdrop") || args[1].toLowerCase().equals("hologram")) {
						if (sender.hasPermission("interactionvisualizer.toggle.others")) {
							for (Player each : Bukkit.getOnlinePlayers()) {
								if (each.getName().toLowerCase().startsWith(args[2].toLowerCase())) {
									tab.add(each.getName());
								}
							}
						}
					}
				}
			}
			return tab;
		default:
			return tab;
		}
	}

}
