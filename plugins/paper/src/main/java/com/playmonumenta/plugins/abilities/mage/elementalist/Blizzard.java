package com.playmonumenta.plugins.abilities.mage.elementalist;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.abilities.Ability;
import com.playmonumenta.plugins.abilities.AbilityManager;
import com.playmonumenta.plugins.abilities.AbilityTrigger;
import com.playmonumenta.plugins.abilities.mage.MagmaShield;
import com.playmonumenta.plugins.abilities.mage.Spellshock;
import com.playmonumenta.plugins.classes.Spells;
import com.playmonumenta.plugins.classes.magic.MagicType;
import com.playmonumenta.plugins.enchantments.SpellDamage;
import com.playmonumenta.plugins.utils.EntityUtils;
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



public class Blizzard extends Ability {
	public static final String NAME = "Blizzard";
	public static final Spells SPELL = Spells.BLIZZARD;

	public static final int DAMAGE_1 = 2;
	public static final int DAMAGE_2 = 4;
	public static final int SIZE_1 = 6;
	public static final int SIZE_2 = 8;
	public static final double SLOW_MULTIPLIER_A_1 = 0.1;
	public static final int SLOW_PERCENTAGE_A_1 = (int)(SLOW_MULTIPLIER_A_1 * 100);
	public static final double SLOW_MULTIPLIER_A_2 = 0.2;
	public static final int SLOW_PERCENTAGE_A_2 = (int)(SLOW_MULTIPLIER_A_2 * 100);
	public static final double SLOW_MULTIPLIER_B_1 = SLOW_MULTIPLIER_A_2;
	public static final int SLOW_PERCENTAGE_B_1 = SLOW_PERCENTAGE_A_2;
	public static final double SLOW_MULTIPLIER_B_2 = 0.3;
	public static final int SLOW_PERCENTAGE_B_2 = (int)(SLOW_MULTIPLIER_B_2 * 100);
	public static final double SLOW_MULTIPLIER_C = 0.4;
	public static final int SLOW_PERCENTAGE_C = (int)(SLOW_MULTIPLIER_C * 100);
	public static final int DAMAGE_INTERVAL = 20;
	public static final int SLOW_INTERVAL = 10;
	public static final int DURATION_SECONDS = 10;
	public static final int DURATION_TICKS = DURATION_SECONDS * 20;
	public static final int SLOW_SECONDS = 5;
	public static final int SLOW_TICKS = SLOW_SECONDS * 20;
	public static final int B_THRESHOLD_SECONDS = 3;
	public static final int B_THRESHOLD = (int)(B_THRESHOLD_SECONDS / (SLOW_INTERVAL / 20d)); // Slowness amount goes up every 0.5s, so if threshold seconds is 3, threshold amount is 6
	public static final int C_THRESHOLD_SECONDS = 6;
	public static final int C_THRESHOLD = (int)(C_THRESHOLD_SECONDS / (SLOW_INTERVAL / 20d));
	public static final int ANGLE = -45; // Looking straight up is -90. This is 45 degrees of pitch allowance
	public static final int COOLDOWN_SECONDS_1 = 30;
	public static final int COOLDOWN_TICKS_1 = COOLDOWN_SECONDS_1 * 20;
	public static final int COOLDOWN_SECONDS_2 = 25;
	public static final int COOLDOWN_TICKS_2 = COOLDOWN_SECONDS_2 * 20;

	private final int mLevelDamage;
	private final int mLevelSize;
	private final double mLevelSlowMultiplierA;
	private final double mLevelSlowMultiplierB;

	public Blizzard(Plugin plugin, Player player) {
		super(plugin, player, NAME);
		mInfo.mLinkedSpell = SPELL;

		mInfo.mScoreboardId = NAME;
		mInfo.mShorthandName = "Bl";
		mInfo.mDescriptions.add(
			String.format(
				"While sneaking and looking upwards, right-clicking with a wand creates an aura of ice and snow, dealing %s damage to all enemies in a %s-block cube around you every second for %ss. The aura chills enemies in it, afflicting them with %s%% slowness for %ss. After %ss in the aura, the slowness is increased to %s%%, and after %ss, to %s%%. Bosses cannot reach the last tier of slowness and players in the aura are extinguished if they're on fire. The damage ignores iframes and cannot apply but can trigger %s. You can no longer cast %s while looking upwards. Cooldown: %ss.",
				DAMAGE_1,
				SIZE_1,
				DURATION_SECONDS,
				SLOW_PERCENTAGE_A_1,
				SLOW_SECONDS,
				B_THRESHOLD_SECONDS,
				SLOW_PERCENTAGE_B_1,
				C_THRESHOLD_SECONDS,
				SLOW_PERCENTAGE_C,
				Spellshock.NAME,
				MagmaShield.NAME,
				COOLDOWN_SECONDS_1
			) // Damage interval of 20 ticks hardcoded to say "every second"
		);
		mInfo.mDescriptions.add(
			String.format(
				"Damage is increased from %s to %s. Aura size is increased from %s to %s blocks. Base slowness is increased from %s%% to %s%%, and from %s%% to %s%% for the second tier. Cooldown is reduced from %ss to %ss.",
				DAMAGE_1,
				DAMAGE_2,
				SIZE_1,
				SIZE_2,
				SLOW_PERCENTAGE_A_1,
				SLOW_PERCENTAGE_A_2,
				SLOW_PERCENTAGE_B_1,
				SLOW_PERCENTAGE_B_2,
				COOLDOWN_SECONDS_1,
				COOLDOWN_SECONDS_2
			)
		);
		mInfo.mTrigger = AbilityTrigger.RIGHT_CLICK;

		boolean isUpgraded = getAbilityScore() == 2;
		mInfo.mCooldown = isUpgraded ? COOLDOWN_TICKS_2 : COOLDOWN_TICKS_1;

		mLevelDamage = isUpgraded ? DAMAGE_2 : DAMAGE_1;
		mLevelSize = isUpgraded ? SIZE_2 : SIZE_1;
		mLevelSlowMultiplierA = isUpgraded ? SLOW_MULTIPLIER_A_2 : SLOW_MULTIPLIER_A_1;
		mLevelSlowMultiplierB = isUpgraded ? SLOW_MULTIPLIER_B_2 : SLOW_MULTIPLIER_B_1;
	}

	private boolean mActive = false;
	private final Map<UUID, Integer> mAffected = new HashMap<>();

	@Override
	public void cast(Action action) {
		if (mActive) {
			return;
		}

		putOnCooldown();
		mActive = true;

		World world = mPlayer.getWorld();
		world.playSound(mPlayer.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1, 2);
		world.playSound(mPlayer.getLocation(), Sound.BLOCK_GLASS_BREAK, 1, 0.75f);

		float spellDamage = SpellDamage.getSpellDamage(mPlayer, mLevelDamage);
		new BukkitRunnable() {
			int mTicks = 0;

			@Override
			public void run() {
				Location loc = mPlayer.getLocation();
				List<LivingEntity> mobs = EntityUtils.getNearbyMobs(loc, mLevelSize, mPlayer);
				mTicks++;
				if (mTicks % SLOW_INTERVAL == 0) {
					for (Player p : PlayerUtils.playersInRange(loc, mLevelSize)) {
						if (p.getFireTicks() > 1) {
							p.setFireTicks(1);
						}
					}
					for (LivingEntity mob : mobs) {
						if (!mAffected.containsKey(mob.getUniqueId())) {
							EntityUtils.applySlow(mPlugin, SLOW_TICKS, mLevelSlowMultiplierA, mob);
							mAffected.put(mob.getUniqueId(), 1);
						} else {
							int duration = mAffected.get(mob.getUniqueId());
							mAffected.put(mob.getUniqueId(), ++duration);

							if (duration >= C_THRESHOLD && !EntityUtils.isBoss(mob)) {
								EntityUtils.applySlow(mPlugin, SLOW_TICKS, SLOW_MULTIPLIER_C, mob);
							} else if (duration >= B_THRESHOLD) {
								EntityUtils.applySlow(mPlugin, SLOW_TICKS, mLevelSlowMultiplierB, mob);
							}
						}
					}
				}

				if (mTicks % DAMAGE_INTERVAL == 0) {
					for (LivingEntity mob : mobs) {
						EntityUtils.damageEntity(mPlugin, mob, spellDamage, mPlayer, MagicType.ICE, true, mInfo.mLinkedSpell, false, true, true, true);
					}
				}

				world.spawnParticle(Particle.SNOWBALL, loc, 6, 2, 2, 2, 0.1);
				world.spawnParticle(Particle.CLOUD, loc, 4, 2, 2, 2, 0.05);
				world.spawnParticle(Particle.CLOUD, loc, 3, 0.1, 0.1, 0.1, 0.15);
				if (
					mTicks >= DURATION_TICKS
					|| AbilityManager.getManager().getPlayerAbility(mPlayer, Blizzard.class) == null
					|| !mPlayer.isValid() // Ensure player is not dead, is still online?
				) {
					this.cancel();
					mAffected.clear();
					mActive = false;
				}
			}
		}.runTaskTimer(mPlugin, 0, 1);
	}

	@Override
	public boolean runCheck() {
		return (
			InventoryUtils.isWandItem(
				mPlayer.getInventory().getItemInMainHand()
			)
			&& mPlayer.isSneaking()
			&& mPlayer.getLocation().getPitch() < ANGLE
		);
	}
}
