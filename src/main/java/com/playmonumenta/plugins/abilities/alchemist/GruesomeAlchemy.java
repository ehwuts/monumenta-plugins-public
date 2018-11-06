package com.playmonumenta.plugins.abilities.alchemist;

import com.playmonumenta.plugins.abilities.Ability;
import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.utils.EntityUtils;

import java.util.Collection;
import java.util.Random;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.World;

public class GruesomeAlchemy extends Ability {
	private static final int GRUESOME_ALCHEMY_DURATION = 8 * 20;
	private static final int GRUESOME_ALCHEMY_VULN = 4; //25%
	private static final int GRUESOME_ALCHEMY_SLOW = 2;
	public static final String GRUESOME_ALCHEMY_SCOREBOARD = "GruesomeAlchemy";

	public GruesomeAlchemy(Plugin plugin, World world, Random random, Player player) {
		super(plugin, world, random, player);
		mInfo.classId = 5;
		mInfo.specId = -1;
		mInfo.scoreboardId = GRUESOME_ALCHEMY_SCOREBOARD;
	}

	@Override
	public boolean PlayerSplashPotionEvent(Collection<LivingEntity> affectedEntities, ThrownPotion potion, PotionSplashEvent event) {
		if (potion.hasMetadata("AlchemistPotion")) {
			if (affectedEntities != null && !affectedEntities.isEmpty()) {
				int gruesomeAlchemy = getAbilityScore();
				for (LivingEntity entity : affectedEntities) {
					if (EntityUtils.isHostileMob(entity)) {
						entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, GRUESOME_ALCHEMY_DURATION, GRUESOME_ALCHEMY_SLOW, false, true));
						if (gruesomeAlchemy > 1) {
							entity.addPotionEffect(new PotionEffect(PotionEffectType.UNLUCK, GRUESOME_ALCHEMY_DURATION, GRUESOME_ALCHEMY_VULN, false, true));
						}
					}
				}
			}
		}
		return true;
	}
}
