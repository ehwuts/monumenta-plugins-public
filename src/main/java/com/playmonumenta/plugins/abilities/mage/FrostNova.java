package com.playmonumenta.plugins.abilities.mage;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.abilities.Ability;
import com.playmonumenta.plugins.abilities.AbilityTrigger;
import com.playmonumenta.plugins.classes.Spells;
import com.playmonumenta.plugins.classes.magic.MagicType;
import com.playmonumenta.plugins.utils.EntityUtils;
import com.playmonumenta.plugins.utils.InventoryUtils;
import com.playmonumenta.plugins.utils.PlayerUtils;

public class FrostNova extends Ability {

	private static final float FROST_NOVA_RADIUS = 6.0f;
	private static final int FROST_NOVA_1_DAMAGE = 4;
	private static final int FROST_NOVA_2_DAMAGE = 8;
	private static final int FROST_NOVA_EFFECT_LVL = 2;
	private static final int FROST_NOVA_COOLDOWN = 18 * 20;
	private static final int FROST_NOVA_DURATION = 8 * 20;

	public FrostNova(Plugin plugin, World world, Random random, Player player) {
		super(plugin, world, random, player);
		mInfo.linkedSpell = Spells.FROST_NOVA;
		mInfo.scoreboardId = "FrostNova";
		mInfo.cooldown = FROST_NOVA_COOLDOWN;
		mInfo.trigger = AbilityTrigger.LEFT_CLICK;
	}

	@Override
	public boolean cast() {
		int frostNova = getAbilityScore();
		for (LivingEntity mob : EntityUtils.getNearbyMobs(mPlayer.getLocation(), FROST_NOVA_RADIUS, mPlayer)) {
			int extraDamage = frostNova == 1 ? FROST_NOVA_1_DAMAGE : FROST_NOVA_2_DAMAGE;
			Spellshock.spellDamageMob(mPlugin, mob, extraDamage, mPlayer, MagicType.ICE);

			if (frostNova > 1) {
				if (EntityUtils.isElite(mob) || EntityUtils.isBoss(mob)) {
					mob.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, FROST_NOVA_DURATION, 3, true, false));
				} else {
					mob.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, FROST_NOVA_DURATION, FROST_NOVA_EFFECT_LVL, true, false));
					EntityUtils.applyFreeze(mPlugin, FROST_NOVA_DURATION, mob);
				}
			} else {
				mob.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, FROST_NOVA_DURATION, FROST_NOVA_EFFECT_LVL, true, false));
			}

			if (mob.getFireTicks() > 1) {
				mob.setFireTicks(1);
			}
		}

		// Extinguish fire on all nearby players
		for (Player player : PlayerUtils.getNearbyPlayers(mPlayer.getLocation(), FROST_NOVA_RADIUS)) {
			if (player.getFireTicks() > 1) {
				player.setFireTicks(1);
			}
		}

		PlayerUtils.callAbilityCastEvent(mPlayer, Spells.FROST_NOVA);
		mPlugin.mTimers.AddCooldown(mPlayer.getUniqueId(), Spells.FROST_NOVA, FROST_NOVA_COOLDOWN);

		Location loc = mPlayer.getLocation();
		mWorld.spawnParticle(Particle.SNOW_SHOVEL, loc.add(0, 1, 0), 400, 4, 1, 4, 0.001);
		mWorld.spawnParticle(Particle.CRIT_MAGIC, loc.add(0, 1, 0), 200, 4, 1, 4, 0.001);
		mWorld.playSound(loc, Sound.BLOCK_GLASS_BREAK, 0.5f, 1.0f);
		putOnCooldown();
		return true;
	}

	@Override
	public boolean runCheck() {
		if (mPlayer.isSneaking()) {
			ItemStack mainHand = mPlayer.getInventory().getItemInMainHand();
			return InventoryUtils.isWandItem(mainHand);
		}
		return false;
	}

}