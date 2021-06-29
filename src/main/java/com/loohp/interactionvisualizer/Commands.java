package com.loohp.interactionvisualizer;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.loohp.interactionvisualizer.api.InteractionVisualizerAPI.Modules;
import com.loohp.interactionvisualizer.managers.MaterialManager;
import com.loohp.interactionvisualizer.managers.MusicManager;
import com.loohp.interactionvisualizer.managers.PacketManager;
import com.loohp.interactionvisualizer.objectholders.EntryKey;
import com.loohp.interactionvisualizer.updater.Updater;
import com.loohp.interactionvisualizer.updater.Updater.UpdaterResponse;
import com.loohp.interactionvisualizer.utils.ChatColorUtils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class Commands implements CommandExecutor, TabCompleter {
	
	private static InteractionVisualizer plugin = InteractionVisualizer.plugin;

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
				plugin.loadConfig();
				MusicManager.reloadConfig();	
				MaterialManager.reloadConfig();
				sender.sendMessage(ChatColorUtils.translateAlternateColorCodes('&', plugin.getConfiguration().getString("Messages.Reload")));
			} else {
				sender.sendMessage(ChatColorUtils.translateAlternateColorCodes('&', plugin.getConfiguration().getString("Messages.NoPermission")));
			}
			return true;
		}
		
		if (args[0].equalsIgnoreCase("refresh")) {
			if (sender instanceof Player) {
				Player player = (Player) sender;
				if (player.hasPermission("interactionvisualizer.refresh")) {
					Bukkit.getScheduler().runTask(InteractionVisualizer.plugin, () -> PacketManager.reset(player));
				} else {
					sender.sendMessage(ChatColorUtils.translateAlternateColorCodes('&', plugin.getConfiguration().getString("Messages.NoPermission")));
				}
			} else {
				sender.sendMessage(ChatColorUtils.translateAlternateColorCodes('&', plugin.getConfiguration().getString("Messages.Toggle.Console")));
			}
			return true;
		}
		
		if (args[0].equalsIgnoreCase("update")) {
			if (sender.hasPermission("interactionvisualizer.update")) {
				sender.sendMessage(ChatColor.AQUA + "[InteractionVisualizer] InteractionVisualizer written by LOOHP!");
				sender.sendMessage(ChatColor.GOLD + "[InteractionVisualizer] You are running InteractionVisualizer version: " + plugin.getDescription().getVersion());
				Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
					UpdaterResponse version = Updater.checkUpdate();
					if (version.getResult().equals("latest")) {
						if (version.isDevBuildLatest()) {
							sender.sendMessage(ChatColor.GREEN + "[InteractionVisualizer] You are running the latest version!");
						} else {
							Updater.sendUpdateMessage(sender, version.getResult(), version.getSpigotPluginId(), true);
						}
					} else {
						Updater.sendUpdateMessage(sender, version.getResult(), version.getSpigotPluginId());
					}
				});
			} else {
				sender.sendMessage(ChatColorUtils.translateAlternateColorCodes('&', plugin.getConfiguration().getString("Messages.NoPermission")));
			}
			return true;
		}
		
		if (args[0].equalsIgnoreCase("toggle")) {
			if (sender.hasPermission("interactionvisualizer.toggle")) {
				if (args.length == 4) {
					if (!(sender instanceof Player)) {
						sender.sendMessage(ChatColorUtils.translateAlternateColorCodes('&', plugin.getConfiguration().getString("Messages.Toggle.Console")));
						return true;
					}
					Player player = (Player) sender;
					Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
						EntryKey[] entries;
						if (args[2].equalsIgnoreCase("all")) {
							entries = InteractionVisualizer.preferenceManager.getRegisteredEntries().toArray(new EntryKey[0]);
						} else {
							entries = new EntryKey[] {new EntryKey(args[2])};
						}
						boolean value = false;
						if (args[3].equalsIgnoreCase("true")) {
							value = true;
						}
						switch (args[1].toLowerCase()) {
						case "itemstand":
							Toggle.toggle(sender, player, Modules.ITEMSTAND, value, entries);
							break;
						case "itemdrop":
							Toggle.toggle(sender, player, Modules.ITEMDROP, value, entries);
							break;
						case "hologram":
							Toggle.toggle(sender, player, Modules.HOLOGRAM, value, entries);
							break;
						case "all":
							for (Modules modules : Modules.values()) {
								Toggle.toggle(sender, player, modules, value, entries);
							}
							break;
						default:
							sender.sendMessage(ChatColorUtils.translateAlternateColorCodes('&', plugin.getConfiguration().getString("Messages.Toggle.Modes")));
						}
					});
					return true;
				} else if (args.length == 5) {
					if (sender instanceof Player) {
						if (Bukkit.getPlayer(args[4]) != null) {
							if (!Bukkit.getPlayer(args[4]).equals((Player) sender)) {
								if (!sender.hasPermission("interactionvisualizer.toggle.others")) {
									sender.sendMessage(ChatColorUtils.translateAlternateColorCodes('&', plugin.getConfiguration().getString("Messages.NoPermission")));
									return true;
								}
							}
						}
					}
					if (Bukkit.getPlayer(args[4]) == null) {
						sender.sendMessage(ChatColorUtils.translateAlternateColorCodes('&', plugin.getConfiguration().getString("Messages.Toggle.PlayerNotFound")));
						return true;
					}
					Player player = Bukkit.getPlayer(args[4]);
					Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
						EntryKey[] entries;
						if (args[2].equalsIgnoreCase("all")) {
							entries = InteractionVisualizer.preferenceManager.getRegisteredEntries().toArray(new EntryKey[0]);
						} else {
							entries = new EntryKey[] {new EntryKey(args[2])};
						}
						boolean value = false;
						if (args[3].equalsIgnoreCase("true")) {
							value = true;
						}
						switch (args[1].toLowerCase()) {
						case "itemstand":
							Toggle.toggle(sender, player, Modules.ITEMSTAND, value, entries);
							break;
						case "itemdrop":
							Toggle.toggle(sender, player, Modules.ITEMDROP, value, entries);
							break;
						case "hologram":
							Toggle.toggle(sender, player, Modules.HOLOGRAM, value, entries);
							break;
						case "all":
							for (Modules modules : Modules.values()) {
								Toggle.toggle(sender, player, modules, value, entries);
							}
							break;
						default:
							sender.sendMessage(ChatColorUtils.translateAlternateColorCodes('&', plugin.getConfiguration().getString("Messages.Toggle.Modes")));
						}
					});
					return true;
				} else {
					sender.sendMessage(ChatColorUtils.translateAlternateColorCodes('&', plugin.getConfiguration().getString("Messages.Toggle.Usage")));
				}				
				return true;
			}
			sender.sendMessage(ChatColorUtils.translateAlternateColorCodes('&', plugin.getConfiguration().getString("Messages.NoPermission")));
			return true;
		}
		
		if (args[0].equalsIgnoreCase("ethereal")) {
			TextComponent text = new TextComponent("She is Imaginary~~");
			text.setColor(ChatColor.YELLOW);
			TextComponent bone = new TextComponent("\u00a7eNana's Bone\n\u00a77Lost \u00a76In-\u00a7dMaginary~~");
			text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[] {bone}));
			text.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.instagram.com/narliar/"));
			sender.spigot().sendMessage(text);
			return true;
		}
		
		sender.sendMessage(ChatColorUtils.translateAlternateColorCodes('&', Bukkit.spigot().getConfig().getString("messages.unknown-command")));
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> tab = new LinkedList<>();
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
					if (args[1].toLowerCase().equals("itemstand") || args[1].toLowerCase().equals("itemdrop") || args[1].toLowerCase().equals("hologram") || args[1].toLowerCase().equals("all")) {
						for (EntryKey each : InteractionVisualizer.preferenceManager.getRegisteredEntries()) {
							if (each.toSimpleString().toLowerCase().startsWith(args[2].toLowerCase())) {
								tab.add(each.toSimpleString());
							}
						}
						if ("all".startsWith(args[2].toLowerCase())) {
							tab.add("all");
						}
					}
				}
			}
			return tab;
		case 4:
			if (args[0].equalsIgnoreCase("toggle")) {
				if (sender.hasPermission("interactionvisualizer.toggle")) {
					if (args[1].toLowerCase().equals("itemstand") || args[1].toLowerCase().equals("itemdrop") || args[1].toLowerCase().equals("hologram") || args[1].toLowerCase().equals("all")) {					
						if (Boolean.TRUE.toString().toLowerCase().startsWith(args[3].toLowerCase())) {
							tab.add(Boolean.TRUE.toString());
						}
						if (Boolean.FALSE.toString().toLowerCase().startsWith(args[3].toLowerCase())) {
							tab.add(Boolean.FALSE.toString());
						}
					}
				}
			}
			return tab;
		case 5:
			if (args[0].equalsIgnoreCase("toggle")) {
				if (sender.hasPermission("interactionvisualizer.toggle")) {
					if (args[1].toLowerCase().equals("itemstand") || args[1].toLowerCase().equals("itemdrop") || args[1].toLowerCase().equals("hologram")) {
						if (sender.hasPermission("interactionvisualizer.toggle.others")) {
							for (Player each : Bukkit.getOnlinePlayers()) {
								if (each.getName().toLowerCase().startsWith(args[4].toLowerCase())) {
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
