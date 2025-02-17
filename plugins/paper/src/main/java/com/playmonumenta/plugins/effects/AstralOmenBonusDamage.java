package com.playmonumenta.plugins.effects;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;



public class AstralOmenBonusDamage extends Effect {
	public static final Particle.DustOptions COLOR_PURPLE = AstralOmenStacks.COLOR_PURPLE;

	private final double mAmount;
	private final Player mPlayer;

	public AstralOmenBonusDamage(int duration, double amount, Player player) {
		super(duration);
		mAmount = amount;
		mPlayer = player;
	}

	@Override
	public double getMagnitude() {
		return Math.abs(mAmount);
	}

	@Override
	public boolean entityReceiveDamageEvent(EntityDamageEvent event) {
		if (event instanceof EntityDamageByEntityEvent && mPlayer.equals(((EntityDamageByEntityEvent) event).getDamager())) {
			Entity entity = event.getEntity();
			if (entity instanceof LivingEntity) {
				LivingEntity mob = (LivingEntity) entity;
				World world = mob.getWorld();
				Location loc = mob.getLocation().add(0, 1, 0);
				world.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_CRIT, 1f, 1.25f);
				world.playSound(loc, Sound.ENTITY_SHULKER_SHOOT, 1f, 1.75f);
				world.spawnParticle(Particle.CRIT, loc, 8, 0.25, 0.5, 0.25, 0.4);
				world.spawnParticle(Particle.REDSTONE, loc, 8, 0.2, 0.2, 0.2, 0.1, COLOR_PURPLE);
				event.setDamage(event.getDamage() * (1 + mAmount));
			}
		}
		return true;
	}

	@Override
	public void entityTickEffect(Entity entity, boolean fourHertz, boolean twoHertz, boolean oneHertz) {
		if (fourHertz) {
			World world = entity.getWorld();
			Location loc = entity.getLocation().add(0, 1, 0);
			world.spawnParticle(Particle.REDSTONE, loc, 8, 0.2, 0.2, 0.2, 0.1, COLOR_PURPLE);
		}
	}

	@Override
	public String toString() {
		return String.format("AstralOmenBonusDamage duration:%d player:%s amount:%f", this.getDuration(), mPlayer.getName(), mAmount);
	}
}
