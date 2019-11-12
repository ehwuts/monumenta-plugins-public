package com.playmonumenta.plugins.abilities.rogue.assassin;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.abilities.Ability;
import com.playmonumenta.plugins.abilities.AbilityTrigger;
import com.playmonumenta.plugins.classes.Spells;
import com.playmonumenta.plugins.potion.PotionManager.PotionID;
import com.playmonumenta.plugins.utils.EntityUtils;
import com.playmonumenta.plugins.utils.InventoryUtils;
import com.playmonumenta.plugins.utils.LocationUtils;
import com.playmonumenta.plugins.utils.MessagingUtils;

public class BodkinBlitz extends Ability {

	private static final int BODKINBLITZ_1_COOLDOWN = 20 * 20;
	private static final int BODKINBLITZ_2_COOLDOWN = 20 * 15;
	private static final int BODKINBLITZ_1_DAMAGE = 25;
	private static final int BODKINBLITZ_2_DAMAGE = 30;

	private int mLeftClicks = 0;

	public BodkinBlitz(Plugin plugin, World world, Random random, Player player) {
		super(plugin, world, random, player);
		mInfo.linkedSpell = Spells.BODKIN_BLITZ;
		mInfo.scoreboardId = "BodkinBlitz";
		mInfo.cooldown = getAbilityScore() == 1 ? BODKINBLITZ_1_COOLDOWN : BODKINBLITZ_2_COOLDOWN;
		mInfo.trigger = AbilityTrigger.LEFT_CLICK;
	}

	public void cast(Action action) {
		mLeftClicks++;
		new BukkitRunnable() {
			@Override
			public void run() {
				if (mLeftClicks > 0) {
					mLeftClicks--;
				}
				this.cancel();
			}
		}.runTaskLater(mPlugin, 20);
		if (mLeftClicks < 3) {
			return;
		}

		putOnCooldown();

		Vector projectile = mPlayer.getLocation().getDirection().normalize().multiply(0.25);
		Location loc = mPlayer.getLocation();

		mWorld.playSound(loc, Sound.ENTITY_PLAYER_BREATH, 1f, 2f);
		mWorld.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 2f);

		new BukkitRunnable() {
			Location tpLoc = loc;
			int j = 0;

			@Override
			public void run() {
				// Fire projectile.
				for (int i = 0; i < 6; i ++) {
					loc.add(projectile);

					if (!loc.getBlock().getType().isSolid() && !loc.clone().add(0, 1, 0).getBlock().getType().isSolid()) {
						tpLoc = loc;
					} else if (!loc.getBlock().getType().isSolid() && !loc.clone().add(0, -1, 0).getBlock().getType().isSolid()) {
						tpLoc = loc.clone().add(0, -1, 0);
					} else if (!loc.clone().add(0, 1, 0).getBlock().getType().isSolid() && !loc.clone().add(0, 2, 0).getBlock().getType().isSolid()) {
						tpLoc = loc.clone().add(0, 1, 0);
					} else {
						j = 8;
					}

					mWorld.spawnParticle(Particle.FALLING_DUST, loc.clone().add(0, 1, 0), 5, 0.15, 0.45, 0.1, Bukkit.createBlockData("gray_concrete"));
					mWorld.spawnParticle(Particle.CRIT, loc.clone().add(0, 1, 0), 4, 0.25, 0.5, 0.25, 0);
					mWorld.spawnParticle(Particle.SMOKE_NORMAL, loc.clone().add(0, 1, 0), 5, 0.15, 0.45, 0.15, 0.01);

				}
				j++;

				// Teleport player
				if (j >= 4) {
					mPlayer.teleport(tpLoc);

					mWorld.playSound(tpLoc, Sound.BLOCK_ENDER_CHEST_OPEN, 1f, 2f);
					mWorld.playSound(tpLoc, Sound.ITEM_TRIDENT_RETURN, 1f, 0.8f);
					mWorld.playSound(tpLoc, Sound.ITEM_TRIDENT_THROW, 1f, 0.5f);
					mWorld.playSound(tpLoc, Sound.ITEM_TRIDENT_HIT, 1f, 1f);
					mWorld.playSound(tpLoc, Sound.ENTITY_PHANTOM_HURT, 1f, 0.75f);
					mWorld.playSound(tpLoc, Sound.ENTITY_BLAZE_SHOOT, 1f, 1f);

					mWorld.spawnParticle(Particle.SMOKE_LARGE, tpLoc.clone().add(0, 1, 0), 30, 0.25, 0.5, 0.25, 0.18);
					mWorld.spawnParticle(Particle.SMOKE_LARGE, tpLoc.clone().add(0, 1, 0), 15, 0.25, 0.5, 0.25, 0.04);
					mWorld.spawnParticle(Particle.SPELL_WITCH, tpLoc.clone().add(0, 1, 0), 15, 0.5, 0.5, 0.5, 0);
					mWorld.spawnParticle(Particle.SMOKE_NORMAL, tpLoc.clone().add(0, 1, 0), 50, 0.75, 0.5, 0.75, 0.05);
					mWorld.spawnParticle(Particle.CRIT, tpLoc.clone().add(0, 1, 0), 25, 1, 1, 1, 0.3);

					mPlugin.mPotionManager.addPotion(mPlayer, PotionID.ABILITY_SELF, new PotionEffect(PotionEffectType.FAST_DIGGING, 5, 19, true, false));

					LivingEntity target = EntityUtils.getNearestHostile(mPlayer, 3);

					// Damage enemy, if applicable
					if (target != null) {
						Location beamLoc = mPlayer.getLocation().add(0, 1.5, 0);
						Location enemyLoc = target.getLocation().add(0, 1.5, 0);
						Vector beam = LocationUtils.getDirectionTo(enemyLoc, beamLoc).multiply(0.2);

						for (int i = 0; i < 15; i ++) {
							beamLoc.add(beam);

							mWorld.spawnParticle(Particle.SMOKE_LARGE, beamLoc, 3, 0.2, 0.2, 0.2, 0.01);
							mWorld.spawnParticle(Particle.CRIT, beamLoc, 1, 0.2, 0.2, 0.2, 0.2);
							mWorld.spawnParticle(Particle.SPELL_MOB, beamLoc, 2, 0.2, 0.2, 0.2, 0);

							if (beamLoc.distance(enemyLoc) < 0.4) {
								break;
							}
						}

						mWorld.spawnParticle(Particle.SMOKE_LARGE, enemyLoc, 15, 0.25, 0.5, 0.25, 0.1);
						mWorld.spawnParticle(Particle.SMOKE_NORMAL, enemyLoc, 20, 0.25, 0.5, 0.25, 0.07);
						mWorld.spawnParticle(Particle.SPELL_WITCH, enemyLoc, 8, 0.45, 0.5, 0.45, 0);
						mWorld.spawnParticle(Particle.SWEEP_ATTACK, enemyLoc, 5, 0.5, 0.6, 0.5, 0);
						mWorld.spawnParticle(Particle.CRIT, enemyLoc, 40, 0.25, 0.5, 0.25, 0.4);

						mWorld.playSound(enemyLoc, Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.7f, 2f);
						mWorld.playSound(enemyLoc, Sound.BLOCK_ANVIL_LAND, 0.6f, 2f);

						if (getAbilityScore() > 1) {
							mPlugin.mTimers.removeCooldown(mPlayer.getUniqueId(), Spells.BY_MY_BLADE);
							MessagingUtils.sendActionBarMessage(mPlugin, mPlayer, "By My Blade cooldown reset!");
						}
						int damage = getAbilityScore() == 1 ? BODKINBLITZ_1_DAMAGE : BODKINBLITZ_2_DAMAGE;
						EntityUtils.damageEntity(mPlugin, target, damage, mPlayer);
					}
					this.cancel();
				}
			}
		}.runTaskTimer(mPlugin, 0, 1);
	}

	@Override
	public boolean runCheck() {
		ItemStack mainHand = mPlayer.getInventory().getItemInMainHand();
		ItemStack offHand = mPlayer.getInventory().getItemInOffHand();
		return InventoryUtils.isSwordItem(mainHand) && InventoryUtils.isSwordItem(offHand);
	}
}