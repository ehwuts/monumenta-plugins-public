package com.playmonumenta.plugins.abilities.cleric;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.abilities.Ability;
import com.playmonumenta.plugins.abilities.AbilityTrigger;
import com.playmonumenta.plugins.classes.Spells;
import com.playmonumenta.plugins.managers.potion.PotionManager.PotionID;
import com.playmonumenta.plugins.utils.PlayerUtils;
import com.playmonumenta.plugins.utils.PotionUtils;

public class CleansingRain extends Ability {

	private static final int CLEANSING_DURATION = 15 * 20;
	private static final int CLEANSING_RESIST_LEVEL = 0;
	private static final int CLEANSING_STRENGTH_LEVEL = 0;
	private static final int CLEANSING_EFFECT_DURATION = 3 * 20;
	private static final int CLEANSING_APPLY_PERIOD = 8;
	private static final int CLEANSING_RADIUS = 4;
	private static final int CLEANSING_1_COOLDOWN = 45 * 20;
	private static final int CLEANSING_2_COOLDOWN = 30 * 20;
	private static final double CLEANSING_ANGLE = 50.0;

	public CleansingRain(Plugin plugin, World world, Random random, Player player) {
		super(plugin, world, random, player);
		mInfo.classId = 3;
		mInfo.specId = -1;
		mInfo.linkedSpell = Spells.CLEANSING;
		mInfo.scoreboardId = "Cleansing";
		mInfo.cooldown = getAbilityScore() == 1 ? CLEANSING_1_COOLDOWN : CLEANSING_2_COOLDOWN;
		mInfo.trigger = AbilityTrigger.RIGHT_CLICK;
	}

	@Override
	public boolean cast() {
		mPlayer.getWorld().playSound(mPlayer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.45f, 0.8f);
		putOnCooldown();

		int cleansing = getAbilityScore();

		// Run cleansing rain here until it finishes
		new BukkitRunnable() {
			int mTicks = 0;
			@Override
			public void run() {
				mPlayer.getWorld().spawnParticle(Particle.WATER_DROP, mPlayer.getLocation().add(0, 2, 0), 150, 2.5, 2, 2.5, 0.001);
				mPlayer.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, mPlayer.getLocation().add(0, 2, 0), 20, 2, 1.5, 2, 0.001);

				for (Player player : PlayerUtils.getNearbyPlayers(mPlayer, CLEANSING_RADIUS, true)) {
					PotionUtils.clearNegatives(mPlugin, player);

					if (player.getFireTicks() > 1) {
						player.setFireTicks(1);
					}

					mPlugin.mPotionManager.addPotion(player, PotionID.APPLIED_POTION, new PotionEffect(PotionEffectType.INCREASE_DAMAGE, CLEANSING_EFFECT_DURATION, CLEANSING_STRENGTH_LEVEL, true, true));
					if (cleansing > 1) {
						mPlugin.mPotionManager.addPotion(player, PotionID.APPLIED_POTION, new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, CLEANSING_EFFECT_DURATION, CLEANSING_RESIST_LEVEL, true, true));
					}
				}

				mTicks += CLEANSING_APPLY_PERIOD;
				if (mTicks > CLEANSING_DURATION) {
					this.cancel();
				}
			}
		}.runTaskTimer(mPlugin, 0, CLEANSING_APPLY_PERIOD);

		return true;
	}

	@Override
	public boolean runCheck() {
		ItemStack offHand = mPlayer.getInventory().getItemInOffHand();
		ItemStack mainHand = mPlayer.getInventory().getItemInMainHand();
		return mPlayer.isSneaking() && mPlayer.getLocation().getPitch() < -CLEANSING_ANGLE &&
		       (mainHand == null || mainHand.getType() != Material.BOW) &&
		       (offHand == null || offHand.getType() != Material.BOW);
	}

}