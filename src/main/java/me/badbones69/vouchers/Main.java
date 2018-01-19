package me.badbones69.vouchers;

import me.badbones69.vouchers.api.FireworkDamageAPI;
import me.badbones69.vouchers.api.Version;
import me.badbones69.vouchers.api.Voucher;
import me.badbones69.vouchers.api.Vouchers;
import me.badbones69.vouchers.controlers.GUI;
import me.badbones69.vouchers.controlers.VoucherClick;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;

public class Main extends JavaPlugin implements Listener {

	public static SettingsManager settings = SettingsManager.getInstance();

	@Override
	public void onEnable() {
		settings.setup(this);
		if(!settings.getData().contains("Players")) {
			settings.getData().set("Players.Clear", null);
			settings.saveData();
		}
		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		Bukkit.getServer().getPluginManager().registerEvents(new VoucherClick(), this);
		Bukkit.getServer().getPluginManager().registerEvents(new GUI(), this);
		try {
			if(Version.getCurrentVersion().comparedTo(Version.v1_11_R1) >= 0) {
				Bukkit.getServer().getPluginManager().registerEvents(new FireworkDamageAPI(this), this);
			}
		}catch(Exception e) {
		}
		Vouchers.load();
		try {
			new MCUpdate(this, true);
		}catch(IOException e) {
		}
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLable, String[] args) {
		if(commandLable.equalsIgnoreCase("Voucher") || commandLable.equalsIgnoreCase("Vouch")) {
			if(args.length == 0) {
				Bukkit.dispatchCommand(sender, "voucher help");
				return true;
			}else {
				if(args[0].equalsIgnoreCase("Help")) {
					if(!Methods.hasPermission(sender, "Access")) return true;
					sender.sendMessage(Methods.color("&8- &6/Voucher Help &3Lists all the commands for vouchers."));
					sender.sendMessage(Methods.color("&8- &6/Voucher Types &3Lists all types of vouchers and codes."));
					sender.sendMessage(Methods.color("&8- &6/Voucher Redeem <Code> &3Allows player to redeem a voucher code."));
					sender.sendMessage(Methods.color("&8- &6/Voucher Give <Type> [Amount] [Player] [Arguments] &3Gives a player a voucher."));
					sender.sendMessage(Methods.color("&8- &6/Voucher GiveAll <Type> [Amount] [Arguments] &3Gives all players a voucher."));
					sender.sendMessage(Methods.color("&8- &6/Voucher Open [Page] &3Opens a GUI so you can get vouchers easy."));
					sender.sendMessage(Methods.color("&8- &6/Voucher Reload &3Reloadeds the config.yml."));
					return true;
				}
				if(args[0].equalsIgnoreCase("Open")) {
					if(!Methods.hasPermission(sender, "Admin")) return true;
					if(args.length >= 2) {
						if(Methods.isInt(args[1])) {
							GUI.openGUI((Player) sender, Integer.parseInt(args[1]));
							return true;
						}
					}
					GUI.openGUI((Player) sender, 1);
					return true;
				}
				if(args[0].equalsIgnoreCase("Types") || args[0].equalsIgnoreCase("List")) {
					if(!Methods.hasPermission(sender, "Admin")) return true;
					String voucher = "";
					String codes = "";
					for(String vo : settings.getConfig().getConfigurationSection("Vouchers").getKeys(false)) {
						voucher += Methods.color("&a" + vo + "&8, ");
					}
					for(String co : settings.getCode().getConfigurationSection("Codes").getKeys(false)) {
						codes += Methods.color("&a" + co + "&8, ");
					}
					voucher = voucher.substring(0, voucher.length() - 2);
					codes = codes.substring(0, codes.length() - 2);
					sender.sendMessage(Methods.color("&e&lVouchers:&f " + voucher));
					sender.sendMessage(Methods.color("&e&lVoucher Codes:&f " + codes));
					return true;
				}
				if(args[0].equalsIgnoreCase("Reload")) {
					if(!Methods.hasPermission(sender, "Admin")) return true;
					settings.reloadConfig();
					settings.reloadData();
					settings.reloadCode();
					settings.setup(this);
					if(!settings.getData().contains("Players")) {
						settings.getData().set("Players.Clear", null);
						settings.saveData();
					}
					Vouchers.load();
					sender.sendMessage(Methods.getPrefix() + Methods.color(settings.getMsgs().getString("Messages.Config-Reload")));
					return true;
				}
				if(args[0].equalsIgnoreCase("Redeem")) {
					if(!Methods.hasPermission(sender, "Redeem")) return true;
					if(args.length >= 2) {
						String code = args[1];
						if(!(sender instanceof Player)) {
							sender.sendMessage(Methods.getPrefix() + Methods.color(settings.getMsgs().getString("Messages.Not-A-Player")));
							return true;
						}
						Player player = (Player) sender;
						if(!Methods.isRealCode(player, code)) return true;
						if(!Methods.isCodeEnabled(player, code)) return true;
						if(!Methods.hasCodePerm(player, code)) return true;
						Methods.codeRedeem(player, code);
						return true;
					}
					sender.sendMessage(Methods.getPrefix() + Methods.color("&c/Voucher Redeem <Code>"));
					return true;
				}
				if(args[0].equalsIgnoreCase("Give")) {// /Voucher 0Give 1<Type> 2[Amount] 3[Player] 4[Arguments]
					if(!Methods.hasPermission(sender, "Admin")) return true;
					if(args.length == 1) {
						if(!(sender instanceof Player)) {
							sender.sendMessage(Methods.getPrefix() + Methods.color(settings.getMsgs().getString("Messages.Not-A-Player")));
							return true;
						}
					}
					if(args.length > 1) {
						String name = sender.getName();
						if(!Vouchers.isVoucherName(args[1])) {
							sender.sendMessage(Methods.getPrefix() + Methods.color(Main.settings.getMsgs().getString("Messages.Not-A-Voucher")));
							return true;
						}
						Voucher voucher = Vouchers.getVoucher(args[1]);
						int amount = 1;
						if(args.length >= 3) {
							if(!Methods.isInt(sender, args[2])) return true;
							amount = Integer.parseInt(args[2]);
						}
						if(args.length >= 4) {
							name = args[3];
							if(!Methods.isOnline(sender, name)) return true;
						}
						Player player = Bukkit.getPlayer(name);
						if(args.length >= 5) {
							player.getInventory().addItem(voucher.buildItem(args[4], amount));
						}else {
							player.getInventory().addItem(voucher.buildItem(amount));
						}
						player.updateInventory();
						sender.sendMessage(Methods.getPrefix() + Methods.color(settings.getMsgs().getString("Messages.Given-A-Voucher").replace("%Player%", player.getName()).replace("%player%", player.getName()).replace("%Voucher%", voucher.getName()).replace("%voucher%", voucher.getName())));
						return true;
					}
					sender.sendMessage(Methods.getPrefix() + Methods.color("&c/Voucher Give <Type> [Amount] [Player] [Arguments]"));
					return true;
				}
				if(args[0].equalsIgnoreCase("GiveAll")) {// /Voucher 0GiveAll 1<Type> 2[Amount] 3[Arguments]
					if(!Methods.hasPermission(sender, "Admin")) return true;
					if(args.length == 1) {
						if(!(sender instanceof Player)) {
							sender.sendMessage(Methods.getPrefix() + Methods.color(settings.getMsgs().getString("Messages.Not=A-Player")));
							return true;
						}
					}
					if(args.length > 1) {
						if(!Vouchers.isVoucherName(args[1])) {
							sender.sendMessage(Methods.getPrefix() + Methods.color(Main.settings.getMsgs().getString("Messages.Not-A-Voucher")));
							return true;
						}
						Voucher voucher = Vouchers.getVoucher(args[1]);
						int amount = 1;
						if(args.length >= 3) {
							if(!Methods.isInt(sender, args[2])) return true;
							amount = Integer.parseInt(args[2]);
						}
						for(Player player : Bukkit.getServer().getOnlinePlayers()) {
							if(args.length >= 4) {
								player.getInventory().addItem(voucher.buildItem(args[3], amount));
							}else {
								player.getInventory().addItem(voucher.buildItem(amount));
							}
							player.updateInventory();
						}
						sender.sendMessage(Methods.getPrefix() + Methods.color(settings.getMsgs().getString("Messages.Given-All-Players-Voucher").replace("%Voucher%", voucher.getName()).replace("%voucher%", voucher.getName())));
						return true;
					}
					sender.sendMessage(Methods.getPrefix() + Methods.color("&c/Voucher GiveAll <Type> [Amount] [Arguments]"));
					return true;
				}
			}
			sender.sendMessage(Methods.getPrefix() + Methods.color("&cPlease do /Voucher Help for more Information."));
			return true;
		}
		return false;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		final Player player = e.getPlayer();
		new BukkitRunnable() {
			@Override
			public void run() {
				if(player.getName().equals("BadBones69")) {
					player.sendMessage(Methods.color("&8[&bVouchers&8]: " + "&7This server is running your Vouchers Plugin. " + "&7It is running version &av" + Bukkit.getServer().getPluginManager().getPlugin("Vouchers").getDescription().getVersion() + "&7."));
				}
				if(player.isOp()) {
					if(settings.getConfig().contains("Settings.Updater")) {
						if(settings.getConfig().getBoolean("Settings.Updater")) {
							Methods.hasUpdate(player);
						}
					}else {
						Methods.hasUpdate(player);
					}
				}
			}
		}.runTaskLaterAsynchronously(this, 20);
	}

}