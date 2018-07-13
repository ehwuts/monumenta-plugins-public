package pe.project.items;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.GameMode;
import org.bukkit.inventory.ItemStack;

import pe.project.Plugin;
import pe.project.utils.LocationUtils;
import pe.project.utils.LocationUtils.LocationType;

public class MonsterEggOverride extends OverrideItem {
	@Override
	public boolean rightClickItemInteraction(Plugin plugin, Player player, Action action, ItemStack item, Block block) {
		// Only allow creative players or players in their plots (in capital and survival) to use spawn eggs
		if ((player.getGameMode() == GameMode.CREATIVE) ||
		    ((player.getGameMode() == GameMode.SURVIVAL) && (LocationUtils.getLocationType(plugin, player) == LocationType.Capital))) {
			return true;
		}
		return false;
	}
}
