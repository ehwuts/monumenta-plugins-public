package pe.project.items;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

import pe.project.Plugin;

public class MonsterEggOverride extends OverrideItem {
	@Override
	public boolean rightClickItemInteraction(Plugin plugin, Player player, Action action, ItemStack item, Block block) {
		//	Are we clicking on a monster spawner?
		return (player.getGameMode() == GameMode.CREATIVE) || (block == null || block.getType() != Material.MOB_SPAWNER);
	}
}