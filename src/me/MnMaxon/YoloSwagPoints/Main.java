package me.MnMaxon.YoloSwagPoints;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin implements Listener {
	public static String dataFolder;
	public static Main plugin;

	@Override
	public void onEnable() {
		plugin = this;
		dataFolder = this.getDataFolder().getAbsolutePath();

		if (!new File(Main.dataFolder).exists())
			new File(Main.dataFolder).mkdirs();
		if (!new File(dataFolder + "/Config.yml").exists()) {
			setupConfig();
			cfgSetter("Rewards.1.Item1.Item", "APPLE");
			cfgSetter("Rewards.1.Item1.Amount", "2");
			cfgSetter("Rewards.5.Item1.Item", "DIAMOND");
			cfgSetter("Rewards.5.Item1.Amount", "1");
			cfgSetter("Rewards.5.Item2.Item", "APPLE");
			cfgSetter("Rewards.5.Item2.Amount", "5");
		} else
			setupConfig();
		Config.Load(dataFolder + "/Data");
		getServer().getPluginManager().registerEvents(this, this);
	}

	public YamlConfiguration setupConfig() {
		cfgSetter("Message.Wait", "You need to wait @1 more minutes to do this.  You have @2 Yolo Swag Points.");
		cfgSetter("Message.Point", "You got 1 point!  You now have @2 Yolo Swag Points.");
		cfgSetter("Message.Reward", "As a reward, you have recieved @3!");
		cfgSetter("Wait_Time", 25);
		return Config.Load(dataFolder + "/Config.yml");
	}

	public void cfgSetter(String path, Object value) {
		YamlConfiguration cfg = Config.Load(dataFolder + "/Config.yml");
		if (cfg.get(path) == null)
			cfg.set(path, value);
		Config.Save(cfg, dataFolder + "/Config.yml");
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return false;
		}
		Player p = (Player) sender;
		if (args.length == 0) {
			YamlConfiguration cfg = setupConfig();
			YamlConfiguration data = Config.Load(dataFolder + "/Data");
			boolean good;
			int time = (Integer.parseInt(new SimpleDateFormat("HH").format(Calendar.getInstance().getTime())) * 60)
					+ Integer.parseInt(new SimpleDateFormat("mm").format(Calendar.getInstance().getTime()));
			int timeLeft = time - (cfg.getInt("Wait_Time") + data.getInt(p.getName() + ".Time"));
			if (data.get(p.getName()) == null) {
				good = true;
				data.set(p.getName() + ".Points", 1);
				data.set(p.getName() + ".Time", time);
			} else {
				if (timeLeft <= 0) {
					good = true;
					data.set(p.getName() + ".Points", data.getInt(p.getName() + ".Points") + 1);
					data.set(p.getName() + ".Time", time);
				} else
					good = false;
			}
			if (good) {
				p.sendMessage(ChatColor.GREEN
						+ cfg.getString("Message.Point").replaceAll("@2",
								data.getInt(p.getName() + ".Points") + "".replaceAll("@1", timeLeft + "")));
				if (cfg.get("Rewards." + data.getInt(p.getName() + ".Points")) != null) {
					String rewardString = "";
					boolean first = true;
					for (int i = 0; i < 100; i++)
						if (cfg.get("Rewards." + data.getInt(p.getName() + ".Points") + ".Item" + i + ".Item") != null
								&& cfg.get("Rewards." + data.getInt(p.getName() + ".Points") + ".Item" + i + ".Amount") != null)
							if (Material.matchMaterial(cfg.getString("Rewards." + data.getInt(p.getName() + ".Points")
									+ ".Item" + i + ".Item")) != null) {
								ItemStack is = new MaterialData(Material.matchMaterial(cfg.getString("Rewards."
										+ data.getInt(p.getName() + ".Points") + ".Item" + i + ".Item"))).toItemStack();
								is.setAmount(cfg.getInt("Rewards." + data.getInt(p.getName() + ".Points") + ".Item" + i
										+ ".Amount"));
								p.getInventory().addItem(is);
								if (first)
									rewardString = rewardString
											+ cfg.getInt("Rewards." + data.getInt(p.getName() + ".Points") + ".Item"
													+ i + ".Amount")
											+ " "
											+ cfg.getString(
													"Rewards." + data.getInt(p.getName() + ".Points") + ".Item" + i
															+ ".Item").toLowerCase() + "(s)";
								else
									rewardString = rewardString
											+ ", "
											+ cfg.getInt("Rewards." + data.getInt(p.getName() + ".Points") + ".Item"
													+ i + ".Amount")
											+ " "
											+ cfg.getString(
													"Rewards." + data.getInt(p.getName() + ".Points") + ".Item" + i
															+ ".Item").toLowerCase() + "(s)";
								first = false;
							}
					if (rewardString != "")
						p.sendMessage(ChatColor.GREEN + cfg.getString("Message.Reward").replaceAll("@3", rewardString));
				}
			} else
				p.sendMessage(ChatColor.RED
						+ cfg.getString("Message.Wait").replaceAll("@2", data.getInt(p.getName() + ".Points") + "")
								.replaceAll("@1", timeLeft + ""));
			Config.Save(data, dataFolder + "/Data");
		} else {
			Bukkit.broadcastMessage(ChatColor.DARK_RED + "Use like " + ChatColor.DARK_AQUA + "/YoloSwag "
					+ ChatColor.DARK_RED + "or " + ChatColor.DARK_AQUA + "/YS");
		}
		return false;
	}
}