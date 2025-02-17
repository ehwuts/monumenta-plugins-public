package com.playmonumenta.plugins.abilities.mage;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.abilities.Ability;
import com.playmonumenta.plugins.abilities.AbilityTrigger;
import com.playmonumenta.plugins.classes.Spells;
import com.playmonumenta.plugins.classes.magic.MagicType;
import com.playmonumenta.plugins.enchantments.SpellDamage;
import com.playmonumenta.plugins.utils.EntityUtils;
import com.playmonumenta.plugins.utils.FastUtils;
import com.playmonumenta.plugins.utils.InventoryUtils;
import com.playmonumenta.plugins.utils.PlayerUtils;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.scheduler.BukkitRunnable;



public class FrostNova extends Ability {
	public static final String NAME = "Frost Nova";
	public static final Spells SPELL = Spells.FROST_NOVA;

	public static final int DAMAGE_1 = 4;
	public static final int DAMAGE_2 = 8;
	public static final int SIZE = 6;
	public static final double SLOW_MULTIPLIER_1 = 0.2;
	public static final int SLOW_PERCENTAGE_1 = (int)(SLOW_MULTIPLIER_1 * 100);
	public static final double SLOW_MULTIPLIER_2 = 0.4;
	public static final int SLOW_PERCENTAGE_2 = (int)(SLOW_MULTIPLIER_2 * 100);
	public static final double REDUCTION_MULTIPLIER = 0.1;
	public static final int REDUCTION_PERCENTAGE = (int)(REDUCTION_MULTIPLIER * 100);
	public static final int DURATION_SECONDS = 4;
	public static final int DURATION_TICKS = DURATION_SECONDS * 20;
	public static final int COOLDOWN_SECONDS = 18;
	public static final int COOLDOWN_TICKS = COOLDOWN_SECONDS * 20;

	private final int mLevelDamage;
	private final double mLevelSlowMultiplier;

	public FrostNova(Plugin plugin, Player player) {
		super(plugin, player, NAME);
		mInfo.mLinkedSpell = SPELL;

		mInfo.mScoreboardId = "FrostNova";
		mInfo.mShorthandName = "FN";
		mInfo.mDescriptions.add(
			String.format(
				"While sneaking, left-clicking with a wand unleashes a frost nova, dealing %s damage to all enemies in a %s-block cube around you, afflicting them with %s%% slowness for %ss, and extinguishing them if they're on fire. Slowness is reduced by %s%% on elites and bosses, and all players in the nova are also extinguished. The damage ignores iframes. Cooldown: %ss.",
				DAMAGE_1,
				SIZE,
				SLOW_PERCENTAGE_1,
				DURATION_SECONDS,
				REDUCTION_PERCENTAGE,
				COOLDOWN_SECONDS
			)
		);
		mInfo.mDescriptions.add(
			String.format(
				"Damage is increased from %s to %s. Base slowness is increased from %s%% to %s%%.",
				DAMAGE_1,
				DAMAGE_2,
				SLOW_PERCENTAGE_1,
				SLOW_PERCENTAGE_2
			)
		);
		mInfo.mCooldown = COOLDOWN_TICKS;
		mInfo.mTrigger = AbilityTrigger.LEFT_CLICK;

		boolean isUpgraded = getAbilityScore() == 2;
		mLevelDamage = isUpgraded ? DAMAGE_2 : DAMAGE_1;
		mLevelSlowMultiplier = isUpgraded ? SLOW_MULTIPLIER_2 : SLOW_MULTIPLIER_1;
	}

	@Override
	public void cast(Action action) {
		putOnCooldown();
		float damage = SpellDamage.getSpellDamage(mPlayer, mLevelDamage);
		for (LivingEntity mob : EntityUtils.getNearbyMobs(mPlayer.getLocation(), SIZE, mPlayer)) {
			EntityUtils.damageEntity(mPlugin, mob, damage, mPlayer, MagicType.ICE, true, mInfo.mLinkedSpell, true, true, true, true);
			if (EntityUtils.isElite(mob) || EntityUtils.isBoss(mob)) {
				EntityUtils.applySlow(mPlugin, DURATION_TICKS, mLevelSlowMultiplier - REDUCTION_MULTIPLIER, mob);
			} else {
				EntityUtils.applySlow(mPlugin, DURATION_TICKS, mLevelSlowMultiplier, mob);
			}

			if (mob.getFireTicks() > 1) {
				mob.setFireTicks(1);
			}
		}

		// Extinguish fire on all nearby players
		for (Player player : PlayerUtils.playersInRange(mPlayer.getLocation(), SIZE)) {
			if (player.getFireTicks() > 1) {
				player.setFireTicks(1);
			}
		}

		World world = mPlayer.getWorld();
		new BukkitRunnable() {
			double mRadius = 0;
			final Location mLoc = mPlayer.getLocation();
			@Override
			public void run() {
				mRadius += 1.25;
				for (double j = 0; j < 360; j += 18) {
					double radian1 = Math.toRadians(j);
					mLoc.add(FastUtils.cos(radian1) * mRadius, 0.15, FastUtils.sin(radian1) * mRadius);
					world.spawnParticle(Particle.CLOUD, mLoc, 1, 0, 0, 0, 0.1);
					world.spawnParticle(Particle.CRIT_MAGIC, mLoc, 8, 0, 0, 0, 0.65);
					mLoc.subtract(FastUtils.cos(radian1) * mRadius, 0.15, FastUtils.sin(radian1) * mRadius);
				}

				if (mRadius >= SIZE + 1) {
					this.cancel();
				}
			}

		}.runTaskTimer(mPlugin, 0, 1);
		Location loc = mPlayer.getLocation().add(0, 1, 0);
		world.playSound(loc, Sound.BLOCK_GLASS_BREAK, 1, 0.65f);
		world.playSound(loc, Sound.BLOCK_GLASS_BREAK, 1, 0.45f);
		world.playSound(loc, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1, 1.25f);
		world.spawnParticle(Particle.CLOUD, loc, 25, 0, 0, 0, 0.35);
		world.spawnParticle(Particle.SPIT, loc, 35, 0, 0, 0, 0.45);
		world.playSound(loc, Sound.BLOCK_GLASS_BREAK, 0.5f, 1f);
	}

	@Override
	public boolean runCheck() {
		return (
			InventoryUtils.isWandItem(
				mPlayer.getInventory().getItemInMainHand()
			)
			&& mPlayer.isSneaking()
		);
	}
}
