package com.playmonumenta.plugins.bosses.bosses;

import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.playmonumenta.plugins.bosses.BossBarManager;
import com.playmonumenta.plugins.bosses.SpellManager;
import com.playmonumenta.plugins.bosses.spells.SpellBaseLaser;
import com.playmonumenta.plugins.bosses.spells.SpellChangeFloor;
import com.playmonumenta.plugins.utils.BossUtils;
import com.playmonumenta.plugins.utils.SerializationUtils;

public class Virius extends BossAbilityGroup {
	public static final String identityTag = "boss_virius";
	public static final int detectionRange = 50;

	private final LivingEntity mBoss;
	private final Location mSpawnLoc;
	private final Location mEndLoc;

	public static BossAbilityGroup deserialize(Plugin plugin, LivingEntity boss) throws Exception {
		return SerializationUtils.statefulBossDeserializer(boss, identityTag, (spawnLoc, endLoc) -> {
			return new Virius(plugin, boss, spawnLoc, endLoc);
		});
	}

	@Override
	public String serialize() {
		return SerializationUtils.statefulBossSerializer(mSpawnLoc, mEndLoc);
	}

	public Virius(Plugin plugin, LivingEntity boss, Location spawnLoc, Location endLoc) {
		mBoss = boss;
		mSpawnLoc = spawnLoc;
		mEndLoc = endLoc;
		mBoss.setRemoveWhenFarAway(false);

		mBoss.addScoreboardTag("Boss");

		SpellManager activeSpells = new SpellManager(Arrays.asList(
			new SpellChangeFloor(plugin, mBoss, mSpawnLoc, detectionRange, 3, Material.MAGMA_BLOCK, 800),
			new SpellBaseLaser(plugin, boss, detectionRange, 100, false, false, 160,
						// Tick action per player
						(Player player, int ticks, boolean blocked) -> {
							player.playSound(player.getLocation(), Sound.UI_TOAST_IN, 2, 0.5f + (ticks / 80f) * 1.5f);
							boss.getLocation().getWorld().playSound(boss.getLocation(), Sound.UI_TOAST_IN, 2, 0.5f + (ticks / 80f) * 1.5f);
							if (ticks == 0) {
								boss.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 110, 4), true);
							}
						},
						// Particles generated by the laser
						(Location loc) -> {
							loc.getWorld().spawnParticle(Particle.CLOUD, loc, 1, 0.02, 0.02, 0.02, 0);
							loc.getWorld().spawnParticle(Particle.WATER_SPLASH, loc, 1, 0.04, 0.04, 0.04, 1);
						},
						// Damage generated at the end of the attack
						(Player player, Location loc, boolean blocked) -> {
							loc.getWorld().playSound(loc, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1f, 1.5f);
							loc.getWorld().spawnParticle(Particle.WATER_WAKE, loc, 300, 0.8, 0.8, 0.8, 0);
							if (!blocked) {
								BossUtils.bossDamage(boss, player, 20);
							}
						})
		));

		BossBarManager bossBar = new BossBarManager(plugin, mBoss, detectionRange, BarColor.RED, BarStyle.SOLID, null);

		super.constructBoss(plugin, identityTag, mBoss, activeSpells, null, detectionRange, bossBar);
	}

	@Override
	public void init() {
		int bossTargetHp = 0;
		int playerCount = BossUtils.getPlayersInRangeForHealthScaling(mBoss, detectionRange);
		int hpDelta = 256;
		int armor = (int)(Math.sqrt(playerCount * 2) - 1);
		while (playerCount > 0) {
			bossTargetHp = bossTargetHp + hpDelta;
			hpDelta = hpDelta / 2;
			playerCount--;
		}
		mBoss.getAttribute(Attribute.GENERIC_ARMOR).setBaseValue(armor);
		mBoss.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(bossTargetHp);
		mBoss.setHealth(bossTargetHp);
	}

	@Override
	public void death(EntityDeathEvent event) {
		mEndLoc.getBlock().setType(Material.REDSTONE_BLOCK);
	}
}
