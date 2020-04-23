package com.loohp.interactionvisualizer;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.loohp.interactionvisualizer.Database.Database;
import com.loohp.interactionvisualizer.Manager.CustomBlockDataManager;
import com.loohp.interactionvisualizer.Manager.EffectManager;
import com.loohp.interactionvisualizer.Manager.EnchantmentManager;
import com.loohp.interactionvisualizer.Manager.PacketManager;
import com.loohp.interactionvisualizer.Manager.TaskManager;
import com.loohp.interactionvisualizer.Updater.Updater;

import net.md_5.bungee.api.ChatColor;

public class Commands implements CommandExecutor, TabCompleter {
	
	private static Plugin plugin = InteractionVisualizer.plugin;

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!label.equalsIgnoreCase("interactionvisualizer") && !label.equalsIgnoreCase("iv")) {
			return true;
		}
		
		if (args.length == 0) {
			sender.sendMessage(ChatColor.AQUA + "InteractionVisualizer written by LOOHP!");
			sender.sendMessage(ChatColor.GOLD + "You are running InteractionVisualizer version: " + plugin.getDescription().getVersion());
			return true;
		}
		
		if (args[0].equalsIgnoreCase("reload")) {
			if (sender.hasPermission("interactionvisualizer.reload")) {
				plugin.reloadConfig();
				InteractionVisualizer.loadConfig();
				EnchantmentManager.reloadConfig();
				EffectManager.reloadConfig();
				CustomBlockDataManager.setup();
				TaskManager.setup();
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.Reload")));
			} else {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.NoPermission")));
			}
			return true;
		}
		
		if (args[0].equalsIgnoreCase("refresh")) {
			if (sender instanceof Player) {
				Player player = (Player) sender;
				if (player.hasPermission("interactionvisualizer.refresh")) {
					Bukkit.getScheduler().runTask(InteractionVisualizer.plugin, () -> PacketManager.reset(player));
				} else {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.NoPermission")));
				}
			} else {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.Toggle.Console")));
			}
			return true;
		}
		
		if (args[0].equalsIgnoreCase("update")) {
			if (sender.hasPermission("interactionvisualizer.update")) {
				sender.sendMessage(ChatColor.AQUA + "[InteractionVisualizer] InteractionVisualizer written by LOOHP!");
				sender.sendMessage(ChatColor.GOLD + "[InteractionVisualizer] You are running InteractionVisualizer version: " + plugin.getDescription().getVersion());
				new BukkitRunnable() {
					public void run() {
						String version = Updater.checkUpdate();
						if (version.equals("latest")) {
							sender.sendMessage(ChatColor.GREEN + "[InteractionVisualizer] You are running the latest version!");
						} else {
							Updater.sendUpdateMessage(version);
						}
					}
				}.runTaskAsynchronously(plugin);
			} else {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.NoPermission")));
			}
			return true;
		}
		
		if (args[0].equalsIgnoreCase("toggle")) {
			if (sender.hasPermission("interactionvisualizer.toggle")) {
				if (args.length == 2) {
					if (!(sender instanceof Player)) {
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.Toggle.Console")));
						return true;
					}
					Player player = (Player) sender;
					new BukkitRunnable() {
						public void run() {
							switch (args[1].toLowerCase()) {
							case "itemstand":
								if (Database.toggleItemStand(player)) {
									sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.Toggle.ToggleOn").replace("%s", args[1].toUpperCase())));
								} else {
									sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.Toggle.ToggleOff").replace("%s", args[1].toUpperCase())));
								}
								break;
							case "itemdrop":
								if (Database.toggleItemDrop(player)) {
									sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.Toggle.ToggleOn").replace("%s", args[1].toUpperCase())));
								} else {
									sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.Toggle.ToggleOff").replace("%s", args[1].toUpperCase())));
								}
								break;
							case "hologram":
								if (Database.toggleHologram(player)) {
									sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.Toggle.ToggleOn").replace("%s", args[1].toUpperCase())));
								} else {
									sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.Toggle.ToggleOff").replace("%s", args[1].toUpperCase())));
								}
								break;
							default:
								sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.Toggle.Modes")));
							}
						}
					}.runTaskAsynchronously(plugin);
					return true;
				} else if (args.length == 3) {
					if (sender instanceof Player) {
						if (Bukkit.getPlayer(args[2]) != null) {
							if (!Bukkit.getPlayer(args[2]).equals((Player) sender)) {
								if (!sender.hasPermission("interactionvisualizer.toggle.others")) {
									sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.NoPermission")));
									return true;
								}
							}
						}
					}
					if (Bukkit.getPlayer(args[2]) == null) {
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.Toggle.PlayerNotFound")));
						return true;
					}
					Player player = Bukkit.getPlayer(args[2]);
					new BukkitRunnable() {
						public void run() {
							switch (args[1].toLowerCase()) {
							case "itemstand":
								if (Database.toggleItemStand(player)) {
									sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.Toggle.ToggleOn").replace("%s", args[1].toUpperCase())));
								} else {
									sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.Toggle.ToggleOff").replace("%s", args[1].toUpperCase())));
								}
								break;
							case "itemdrop":
								if (Database.toggleItemDrop(player)) {
									sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.Toggle.ToggleOn").replace("%s", args[1].toUpperCase())));
								} else {
									sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.Toggle.ToggleOff").replace("%s", args[1].toUpperCase())));
								}
								break;
							case "hologram":
								if (Database.toggleHologram(player)) {
									sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.Toggle.ToggleOn").replace("%s", args[1].toUpperCase())));
								} else {
									sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.Toggle.ToggleOff").replace("%s", args[1].toUpperCase())));
								}
								break;
							default:
								sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.Toggle.Modes")));
							}
						}
					}.runTaskAsynchronously(plugin);
					return true;
				} else {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.Toggle.Usage")));
				}				
				return true;
			}
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.NoPermission")));
			return true;
		}
		
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Bukkit.spigot().getConfig().getString("messages.unknown-command")));
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> tab = new ArrayList<String>();
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
