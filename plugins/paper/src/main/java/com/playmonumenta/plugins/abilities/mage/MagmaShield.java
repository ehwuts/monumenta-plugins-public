package com.playmonumenta.plugins.abilities.mage;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.abilities.Ability;
import com.playmonumenta.plugins.abilities.AbilityManager;
import com.playmonumenta.plugins.abilities.AbilityTrigger;
import com.playmonumenta.plugins.abilities.mage.elementalist.Blizzard;
import com.playmonumenta.plugins.classes.Spells;
import com.playmonumenta.plugins.classes.magic.MagicType;
import com.playmonumenta.plugins.enchantments.SpellDamage;
import com.playmonumenta.plugins.utils.EntityUtils;
import com.playmonumenta.plugins.utils.FastUtils;
import com.playmonumenta.plugins.utils.InventoryUtils;
import com.playmonumenta.plugins.utils.MovementUtils;
import com.playmonumenta.plugins.utils.VectorUtils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;



public class MagmaShield extends Ability {
	public static final String NAME = "Magma Shield";
	public static final Spells SPELL = Spells.MAGMA_SHIELD;

	public static final int DAMAGE_1 = 6;
	public static final int DAMAGE_2 = 12;
	public static final int SIZE = 6;
	public static final int FIRE_SECONDS = 4;
	public static final int FIRE_TICKS = FIRE_SECONDS * 20;
	public static final float KNOCKBACK = 0.5f;
	public static final double DOT_ANGLE = 0.33;
	public static final double ANGLE = Blizzard.ANGLE; // Looking up is -90. This is 40 degrees of pitch allowance
	public static final int COOLDOWN_SECONDS = 12;
	public static final int COOLDOWN_TICKS = COOLDOWN_SECONDS * 20;

	private final int mLevelDamage;

	private boolean mLookUpRestriction = false;

	public MagmaShield(Plugin plugin, Player player) {
		super(plugin, player, NAME);
		mInfo.mLinkedSpell = SPELL;

		mInfo.mScoreboardId = "Magma";
		mInfo.mShorthandName = "MS";
		mInfo.mDescriptions.add(
			String.format(
				"While sneaking, right-clicking with a wand summons a torrent of flames, dealing %s damage to all enemies in front of you within a %s-block cube around you, setting them on fire for %ss, and knocking them away. The damage ignores iframes. Cooldown: %ss.",
				DAMAGE_1,
				SIZE,
				FIRE_SECONDS,
				COOLDOWN_SECONDS
			)
		);
		mInfo.mDescriptions.add(
			String.format(
				"Damage is increased from %s to %s.",
				DAMAGE_1,
				DAMAGE_2
			)
		);
		mInfo.mCooldown = COOLDOWN_TICKS;
		mInfo.mTrigger = AbilityTrigger.RIGHT_CLICK;

		mLevelDamage = getAbilityScore() == 2 ? DAMAGE_2 : DAMAGE_1;

		Bukkit.getScheduler().runTask(plugin, () -> {
			if (player != null) {
				Blizzard blizzard = AbilityManager.getManager().getPlayerAbility(mPlayer, Blizzard.class);
				mLookUpRestriction = blizzard != null; // If Blizzard is not null, player has Blizzard, require restriction
			}
		});
	}

	@Override
	public void cast(Action action) {
		putOnCooldown();

		float damage = SpellDamage.getSpellDamage(mPlayer, mLevelDamage);
		Vector playerDir = mPlayer.getEyeLocation().getDirection().setY(0).normalize();
		for (LivingEntity mob : EntityUtils.getNearbyMobs(mPlayer.getLocation(), SIZE, mPlayer)) {
			Vector toMobVector = mob.getLocation().toVector().subtract(mPlayer.getLocation().toVector()).setY(0).normalize();
			if (playerDir.dot(toMobVector) > DOT_ANGLE) {
				EntityUtils.damageEntity(mPlugin, mob, damage, mPlayer, MagicType.FIRE, true, mInfo.mLinkedSpell, true, true, true);
				EntityUtils.applyFire(mPlugin, FIRE_TICKS, mob, mPlayer);
				MovementUtils.knockAway(mPlayer, mob, KNOCKBACK);
			}
		}

		World world = mPlayer.getWorld();
		world.spawnParticle(Particle.SMOKE_LARGE, mPlayer.getLocation(), 15, 0.05, 0.05, 0.05, 0.1);
		new BukkitRunnable() {
			final Location mLoc = mPlayer.getLocation();
			double mRadius = 0;

			@Override
			public void run() {
				if (mRadius == 0) {
					mLoc.setDirection(mPlayer.getLocation().getDirection().setY(0).normalize());
				}
				Vector vec;
				mRadius += 1.25;
				for (double degree = 30; degree <= 150; degree += 10) {
					double radian1 = Math.toRadians(degree);
					vec = new Vector(FastUtils.cos(radian1) * mRadius, 0.125, FastUtils.sin(radian1) * mRadius);
					vec = VectorUtils.rotateXAxis(vec, -mLoc.getPitch());
					vec = VectorUtils.rotateYAxis(vec, mLoc.getYaw());

					Location l = mLoc.clone().add(0, 0.1, 0).add(vec);
					world.spawnParticle(Particle.FLAME, l, 2, 0.15, 0.15, 0.15, 0.15);
					world.spawnParticle(Particle.SMOKE_NORMAL, l, 3, 0.15, 0.15, 0.15, 0.1);
				}

				if (mRadius >= SIZE + 1) {
					this.cancel();
				}
			}

		}.runTaskTimer(mPlugin, 0, 1);

		world.playSound(mPlayer.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1f, 0.75f);
		world.playSound(mPlayer.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 1f, 1.25f);
		world.playSound(mPlayer.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1f, 0.5f);
	}

	@Override
	public boolean runCheck() {
		if (InventoryUtils.isWandItem(
			mPlayer.getInventory().getItemInMainHand()
		)) {
			boolean lookingTooHigh = false;
			if (mLookUpRestriction) {
				// Only check if looking too high, if have restriction. Otherwise never looking too high (false)
				lookingTooHigh = mPlayer.getLocation().getPitch() < ANGLE;
			}
			return mPlayer.isSneaking() && !lookingTooHigh;
		}
		return false;
	}
}
