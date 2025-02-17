package com.playmonumenta.plugins.abilities.mage.arcanist;

import java.util.NavigableSet;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.abilities.Ability;
import com.playmonumenta.plugins.classes.Spells;
import com.playmonumenta.plugins.classes.magic.MagicType;
import com.playmonumenta.plugins.effects.AstralOmenBonusDamage;
import com.playmonumenta.plugins.effects.AstralOmenStacks;
import com.playmonumenta.plugins.effects.Effect;
import com.playmonumenta.plugins.enchantments.SpellDamage;
import com.playmonumenta.plugins.events.CustomDamageEvent;
import com.playmonumenta.plugins.utils.EntityUtils;
import com.playmonumenta.plugins.utils.MovementUtils;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;



public class AstralOmen extends Ability {
	public static final String NAME = "Astral Omen";
	public static final Spells SPELL = Spells.ASTRAL_OMEN;
	public static final String STACKS_SOURCE = "AstralOmenStacks";
	public static final String BONUS_DAMAGE_SOURCE = "AstralOmenBonusDamage";
	private static final Particle.DustOptions COLOR_PURPLE = AstralOmenStacks.COLOR_PURPLE;

	public static final int DAMAGE = 6;
	public static final int SIZE = 3;
	public static final double BONUS_MULTIPLIER_1 = 0.2;
	public static final int BONUS_PERCENTAGE_1 = (int)(BONUS_MULTIPLIER_1 * 100);
	public static final double BONUS_MULTIPLIER_2 = 0.3;
	public static final int BONUS_PERCENTAGE_2 = (int)(BONUS_MULTIPLIER_2 * 100);
	public static final double SLOW_MULTIPLIER = 0.1;
	public static final int SLOW_PERCENTAGE = (int)(SLOW_MULTIPLIER * 100);
	public static final int STACK_SECONDS = 10;
	public static final int STACK_TICKS = STACK_SECONDS * 20;
	public static final int BONUS_SECONDS = 8;
	public static final int BONUS_TICKS = BONUS_SECONDS * 20;
	public static final int SLOW_SECONDS = 8;
	public static final int SLOW_TICKS = SLOW_SECONDS * 20;
	public static final float PULL_SPEED = 0.175f;
	public static final int STACK_THRESHOLD = 3;

	private final double mLevelBonusMultiplier;
	private final boolean mDoPull;

	public AstralOmen(Plugin plugin, Player player) {
		super(plugin, player, NAME);
		mInfo.mLinkedSpell = SPELL;

		mInfo.mScoreboardId = "AstralOmen";
		mInfo.mShorthandName = "AO";
		mInfo.mDescriptions.add(
			String.format(
				"Attacking an enemy with a spell marks its fate, giving it an astral omen. If an enemy hits %s omens, its fate is sealed, clearing its omens and causing a magical implosion that deals %s damage to it and all enemies in a %s-block cube around it. It then takes %s%% more damage from you for %ss. An enemy loses all its omens after %ss of it not gaining another omen. The implosion's damage ignores iframes and itself cannot apply omens.",
				STACK_THRESHOLD,
				DAMAGE,
				SIZE,
				BONUS_PERCENTAGE_1,
				BONUS_SECONDS,
				STACK_SECONDS
			)
		);
		mInfo.mDescriptions.add(
			String.format(
				"The implosion now pulls all enemies inwards and afflicts them with %s%% slowness for %ss. Bonus damage taken is increased from %s%% to %s%%.",
				SLOW_PERCENTAGE,
				SLOW_SECONDS,
				BONUS_PERCENTAGE_1,
				BONUS_PERCENTAGE_2
			)
		);

		boolean isUpgraded = getAbilityScore() == 2;
		mLevelBonusMultiplier = isUpgraded ? BONUS_MULTIPLIER_2 : BONUS_MULTIPLIER_1;
		mDoPull = isUpgraded;
	}

	@Override
	public void playerDealtCustomDamageEvent(CustomDamageEvent event) {
		if (event.getSpell() == null || event.getSpell() == mInfo.mLinkedSpell) {
			return;
		}


		LivingEntity target = event.getDamaged();
		NavigableSet<Effect> stacks = mPlugin.mEffectManager.getEffects(target, STACKS_SOURCE);
		int level = stacks == null ? 0 : (int)stacks.last().getMagnitude();

		if (stacks != null) {
			mPlugin.mEffectManager.clearEffects(target, STACKS_SOURCE);
		}

		if (level >= STACK_THRESHOLD - 1) { // Adding 1 more stack would hit threshold, which removes all stacks anyway, so don't bother adding then removing
			World world = target.getWorld();
			float spellDamage = SpellDamage.getSpellDamage(mPlayer, DAMAGE);
			for (LivingEntity enemy : EntityUtils.getNearbyMobs(target.getLocation(), SIZE)) {
				EntityUtils.damageEntity(mPlugin, enemy, spellDamage, mPlayer, MagicType.ARCANE, true, mInfo.mLinkedSpell, true, true, true);
				if (mDoPull) {
					MovementUtils.pullTowards(target, enemy, PULL_SPEED);
					EntityUtils.applySlow(mPlugin, SLOW_TICKS, SLOW_MULTIPLIER, enemy);
				}

				world.spawnParticle(Particle.REDSTONE, enemy.getLocation(), 10, 0.2, 0.2, 0.2, 0.1, COLOR_PURPLE);
			}

			mPlugin.mEffectManager.addEffect(target, BONUS_DAMAGE_SOURCE, new AstralOmenBonusDamage(BONUS_TICKS, mLevelBonusMultiplier, mPlayer));

			world.spawnParticle(Particle.ENCHANTMENT_TABLE, target.getLocation(), 80, 0, 0, 0, 4);
			world.spawnParticle(Particle.REDSTONE, target.getLocation(), 20, 3, 3, 3, 0.1, COLOR_PURPLE);
			world.playSound(target.getLocation(), Sound.ENTITY_BLAZE_HURT, 1.3f, 1.5f);
		} else {
			// Effect implements Comparable in compareTo(), which uses the internal magnitude
			// When EffectManager does addEffect(), it add()s to NavigableSet, which presumably uses compareTo()
			// So, seems effects of the same magnitude will not have multiple added!
			//
			// We just work with 1 effect at all times, of the magnitude (level) we want,
			// which will handle stack decay times appropriately & not have conflicting magnitudes
			mPlugin.mEffectManager.addEffect(target, STACKS_SOURCE, new AstralOmenStacks(STACK_TICKS, ++level));
		}
	}
}