package com.playmonumenta.plugins.integrations.luckperms;

import java.util.LinkedHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.utils.PlayerUtils;
import com.playmonumenta.plugins.utils.ScoreboardUtils;

import io.github.jorelali.commandapi.api.CommandAPI;
import io.github.jorelali.commandapi.api.CommandPermission;
import io.github.jorelali.commandapi.api.arguments.Argument;
import io.github.jorelali.commandapi.api.arguments.EntitySelectorArgument;
import io.github.jorelali.commandapi.api.arguments.EntitySelectorArgument.EntitySelector;

import me.lucko.luckperms.api.LuckPermsApi;

public class PromoteGuild {
	public static void register(Plugin plugin, LuckPermsApi lp) {
		// promoteguild <guildname> <playername>
		CommandPermission perms = CommandPermission.fromString("monumenta.command.promoteguild");

		LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();
		arguments.put("player", new EntitySelectorArgument(EntitySelector.ONE_PLAYER));

		CommandAPI.getInstance().register("promoteguild", perms, arguments, (sender, args) -> {
			run(plugin, lp, sender, (Player) args[0]);
		});
	}

	private static void run(Plugin plugin, LuckPermsApi lp, CommandSender sender, Player player) throws CommandSyntaxException {
		String currentGuildName = LuckPermsIntegration.getGuildName(lp, player);
		if (currentGuildName == null) {
			String err = ChatColor.RED + "You are not in a guild";
			player.sendMessage(err);
			CommandAPI.fail(err);
		}

		// Check for nearby founder
		for (Player p : PlayerUtils.getNearbyPlayers(player, 1, false)) {
			String nearbyPlayerGroup = LuckPermsIntegration.getGuildName(lp, p);
			if (nearbyPlayerGroup != null &&
			    nearbyPlayerGroup.equalsIgnoreCase(currentGuildName) &&
				ScoreboardUtils.getScoreboardValue(p, "Founder") == 1) {

				// Set scores and permissions
				ScoreboardUtils.setScoreboardValue(player, "Founder", 1);

				// Flair (mostly stolen from CreateGuild)
				player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Congratulations! You are now a founder of " + currentGuildName + "!");
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
				                       "execute at " + player.getName()
				                       + "run summon minecraft:firework_rocket ~ ~1 ~ "
				                       + "{LifeTime:0,FireworksItem:{id:firework_rocket,Count:1,tag:{Fireworks:{Explosions:[{Type:1,Colors:[I;16528693],FadeColors:[I;16777215]}]}}}}");
				// All done
				return;
			}
		}
	}
}
