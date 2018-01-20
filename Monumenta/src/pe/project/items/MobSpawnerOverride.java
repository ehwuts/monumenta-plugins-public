package pe.project.items;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import pe.project.Constants;
import pe.project.Plugin;

public class MobSpawnerOverride extends OverrideItem {
	@Override
	public boolean blockBreakInteraction(Plugin plugin, Player player, Block block) {
		if ((player.getGameMode() == GameMode.CREATIVE) || _breakable(block)) {
			// Breaking was allowed - remove the metadata associated with the spawner
			if (block.hasMetadata(Constants.SPAWNER_COUNT_METAKEY)) {
				block.removeMetadata(Constants.SPAWNER_COUNT_METAKEY, plugin);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean blockExplodeInteraction(Plugin plugin, Block block) {
		return _breakable(block);
	}

	private boolean _breakable(Block block) {
		Block blockUnder = block.getLocation().add(0, -1, 0).getBlock();
		if (blockUnder != null && blockUnder.getType() == Material.BEDROCK) {
			return false;
		}

		return true;
	}
}