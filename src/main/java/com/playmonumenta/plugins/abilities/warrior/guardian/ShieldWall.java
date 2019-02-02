package com.playmonumenta.plugins.abilities.warrior.guardian;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.abilities.Ability;
import com.playmonumenta.plugins.abilities.AbilityTrigger;
import com.playmonumenta.plugins.classes.Spells;
import com.playmonumenta.plugins.utils.EntityUtils;
import com.playmonumenta.plugins.utils.MovementUtils;
import com.playmonumenta.plugins.utils.VectorUtils;

/*
 * Shield Wall: Blocking and then blocking again within 0.25s 
 * Creates a 180 degree arc of particles with a height of 5 blocks 
 * and width of 4 blocks in front of the user, blocking all enemy 
 * projectiles and dealing 6 damage to enemies who pass through the 
 * wall. The shield lasts 8/10 seconds. At level 2, this shield knocks 
 * back enemies as well. (Ghast fireballs explode on the wall) 
 * Cooldown: 30/20 seconds
 */
public class ShieldWall extends Ability {

	public ShieldWall(Plugin plugin, World world, Random random, Player player) {
		super(plugin, world, random, player);
		mInfo.scoreboardId = "ShieldWall";
		mInfo.cooldown = getAbilityScore() == 1 ? 30 : 20;
		mInfo.linkedSpell = Spells.SHIELD_WALL;
		mInfo.trigger = AbilityTrigger.RIGHT_CLICK;
	}

	@Override
	public boolean cast() {
		int time = getAbilityScore() == 1 ? 20 * 8 : 20 * 10;
		boolean knockback = getAbilityScore() == 1 ? false : true;
		new BukkitRunnable() {
			int t = 0;
			boolean active = false;
			boolean primed = false;
			Location loc = mPlayer.getLocation();
			List<BoundingBox> boxes = new ArrayList<BoundingBox>();
			boolean hitboxes = false;

			@Override
			public void run() {
				t++;

				if (!mPlayer.isHandRaised() && !mPlayer.isBlocking() && !active) {
					primed = true;
				}

				if (primed && !active) {
					if (mPlayer.isHandRaised() || mPlayer.isBlocking()) {
						active = true;
						t = 0;
						mWorld.playSound(mPlayer.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 1.5f);
						mWorld.playSound(mPlayer.getLocation(), Sound.ENTITY_IRON_GOLEM_HURT, 1, 0.8f);
						mWorld.spawnParticle(Particle.FIREWORKS_SPARK, mPlayer.getLocation(), 70, 0, 0, 0, 0.3f);

					}
				}
				if (active) {
					Vector vec;
					for (int y = 0; y < 5; y++) {
						for (double degree = 0; degree < 180; degree += 10) {
							double radian1 = Math.toRadians(degree);
							vec = new Vector(Math.cos(radian1) * 4, y, Math.sin(radian1) * 4);
							vec = VectorUtils.rotateXAxis(vec, 0);
							vec = VectorUtils.rotateYAxis(vec, loc.getYaw());

							Location l = loc.clone().add(vec);
							mWorld.spawnParticle(Particle.SPELL_INSTANT, l, 1, 0, 0, 0, 0);
							if (!hitboxes) {
								boxes.add(BoundingBox.of(l.clone().subtract(0.6, 0, 0.6),
										l.clone().add(0.6, 5, 0.6)));
							}
						}
						hitboxes = true;
					}

					for (BoundingBox box : boxes) {
						for (Entity e : mWorld.getNearbyEntities(box)) {
							Location eLoc = e.getLocation();
							if (e instanceof Projectile) {
								Projectile proj = (Projectile) e;
								if (!(proj.getShooter() instanceof Player)) {
									proj.remove();
									mWorld.spawnParticle(Particle.FIREWORKS_SPARK, eLoc, 5, 0, 0, 0, 0.25f);
									mWorld.playSound(eLoc, Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 075, 1.5f);
								}
							} else if (knockback && EntityUtils.isHostileMob(e)) {
								LivingEntity le = (LivingEntity) e;
								MovementUtils.KnockAway(loc, le, 0.3f);
								mWorld.spawnParticle(Particle.EXPLOSION_NORMAL, eLoc, 50, 0, 0, 0, 0.35f);
								mWorld.playSound(eLoc, Sound.ENTITY_GENERIC_EXPLODE, 1, 1f);
							}
						}
					}
				}
				if (t >= time) {
					this.cancel();
					boxes.clear();
				}

				if (t > 5 && !active)
					this.cancel();
			}

		}.runTaskTimer(mPlugin, 0, 1);
		return true;
	}

	@Override
	public boolean runCheck() {
		ItemStack mHand = mPlayer.getInventory().getItemInMainHand();
		ItemStack oHand = mPlayer.getInventory().getItemInOffHand();
		return mHand.getType() == Material.SHIELD || oHand.getType() == Material.SHIELD;
	}

}