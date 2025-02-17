package com.playmonumenta.plugins.bosses.bosses;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.playmonumenta.plugins.bosses.BossBarManager;
import com.playmonumenta.plugins.bosses.BossBarManager.BossHealthAction;
import com.playmonumenta.plugins.bosses.SpellManager;
import com.playmonumenta.plugins.bosses.spells.Spell;
import com.playmonumenta.plugins.bosses.spells.SpellBaseLaser;
import com.playmonumenta.plugins.bosses.spells.SpellBlockBreak;
import com.playmonumenta.plugins.bosses.spells.SpellConditionalTeleport;
import com.playmonumenta.plugins.bosses.spells.SpellGenericCharge;
import com.playmonumenta.plugins.bosses.spells.SpellTpSwapPlaces;
import com.playmonumenta.plugins.utils.BossUtils;
import com.playmonumenta.plugins.utils.PlayerUtils;
import com.playmonumenta.plugins.utils.SerializationUtils;

public class Varcosa extends BossAbilityGroup {
	public static final String identityTag = "boss_varcosa";
	public static final int detectionRange = 110;

	private final Location mSpawnLoc;
	private final Location mEndLoc;

	public static BossAbilityGroup deserialize(Plugin plugin, LivingEntity boss) throws Exception {
		return SerializationUtils.statefulBossDeserializer(boss, identityTag, (spawnLoc, endLoc) -> {
			return new Varcosa(plugin, boss, spawnLoc, endLoc);
		});
	}

	@Override
	public String serialize() {
		return SerializationUtils.statefulBossSerializer(mSpawnLoc, mEndLoc);
	}

	public Varcosa(Plugin plugin, LivingEntity boss, Location spawnLoc, Location endLoc) {
		super(plugin, identityTag, boss);
		mSpawnLoc = spawnLoc;
		mEndLoc = endLoc;
		boss.setRemoveWhenFarAway(false);

		boss.addScoreboardTag("Boss");

		SpellManager activeSpells = new SpellManager(Arrays.asList(
			new SpellGenericCharge(plugin, boss, detectionRange, 15.0F),
			new SpellTpSwapPlaces(plugin, boss, 5),
			new SpellBaseLaser(plugin, boss, detectionRange, 100, false, false, 160,

					// Tick action per player
					(Player player, int ticks, boolean blocked) -> {
						player.playSound(player.getLocation(), Sound.UI_TOAST_IN, 2, 0.5f + (ticks / 80f) * 1.5f);
						boss.getLocation().getWorld().playSound(boss.getLocation(), Sound.UI_TOAST_IN, 2, 0.5f + (ticks / 80f) * 1.5f);

						if (ticks == 0) {
							boss.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 110, 4));
						}
					},

					// Particles generated by the laser
					(Location loc) -> {
						loc.getWorld().spawnParticle(Particle.CLOUD, loc, 1, 0.02, 0.02, 0.02, 0);
						loc.getWorld().spawnParticle(Particle.FLAME, loc, 1, 0.04, 0.04, 0.04, 1);
					},

					// Damage generated at the end of the attack
					(Player player, Location loc, boolean blocked) -> {
						loc.getWorld().playSound(loc, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1f, 1.5f);
						loc.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, loc, 300, 0.8, 0.8, 0.8, 0);

						if (!blocked) {
							BossUtils.bossDamage(boss, player, 30);
							// Shields don't stop fire!
							player.setFireTicks(4 * 20);
						}
					})));

		List<Spell> passiveSpells = Arrays.asList(
			new SpellBlockBreak(boss),
			// Teleport the boss to spawnLoc if he is stuck in bedrock
			new SpellConditionalTeleport(boss, spawnLoc, b -> b.getLocation().getBlock().getType() == Material.BEDROCK ||
			                                                   b.getLocation().add(0, 1, 0).getBlock().getType() == Material.BEDROCK ||
			                                                   b.getLocation().getBlock().getType() == Material.LAVA)
		);

		Map<Integer, BossHealthAction> events = new HashMap<Integer, BossHealthAction>();
		events.put(100, (mob) -> {
			PlayerUtils.executeCommandOnNearbyPlayers(spawnLoc, detectionRange, "tellraw @s [\"\",{\"text\":\"" + ChatColor.GOLD + "[Captain Varcosa] " + ChatColor.WHITE + "Yarharhar! Thank ye fer comin’ and seein’ me, but now this will be ye grave as well!\",\"color\":\"purple\"}]");
		});
		events.put(50, (mob) -> {
			PlayerUtils.executeCommandOnNearbyPlayers(spawnLoc, detectionRange, "tellraw @s [\"\",{\"text\":\"" + ChatColor.GOLD + "[Captain Varcosa] " + ChatColor.WHITE + "I will hang ye out to dry!\",\"color\":\"purple\"}]");
		});
		events.put(25, (mob) -> {
			PlayerUtils.executeCommandOnNearbyPlayers(spawnLoc, detectionRange, "tellraw @s [\"\",{\"text\":\"" + ChatColor.GOLD + "[Captain Varcosa] " + ChatColor.WHITE + "Yarharhar! Do ye feel it as well? That holy fleece? It be waitin’ fer me!\",\"color\":\"purple\"}]");
		});
		events.put(10, (mob) -> {
			PlayerUtils.executeCommandOnNearbyPlayers(spawnLoc, detectionRange, "tellraw @s [\"\",{\"text\":\"" + ChatColor.GOLD + "[Captain Varcosa] " + ChatColor.WHITE + "I be too close ter be stoppin’ now! Me greed will never die!\",\"color\":\"purple\"}]");
		});
		BossBarManager bossBar = new BossBarManager(plugin, boss, detectionRange, BarColor.RED, BarStyle.SEGMENTED_10, events);

		super.constructBoss(activeSpells, passiveSpells, detectionRange, bossBar);
	}

	@Override
	public void init() {
		int bossTargetHp = 1500;

		int armor = (0);

		mBoss.getAttribute(Attribute.GENERIC_ARMOR).setBaseValue(armor);
		mBoss.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(bossTargetHp);
		mBoss.setHealth(bossTargetHp);

		//launch event related spawn commands
		PlayerUtils.executeCommandOnNearbyPlayers(mBoss.getLocation(), detectionRange, "effect give @s minecraft:blindness 2 2");
		PlayerUtils.executeCommandOnNearbyPlayers(mBoss.getLocation(), detectionRange, "title @s title [\"\",{\"text\":\"Captain Varcosa\",\"color\":\"purple\",\"bold\":true}]");
		PlayerUtils.executeCommandOnNearbyPlayers(mBoss.getLocation(), detectionRange, "title @s subtitle [\"\",{\"text\":\"The Legendary Pirate King\",\"color\":\"red\",\"bold\":true}]");
		PlayerUtils.executeCommandOnNearbyPlayers(mBoss.getLocation(), detectionRange, "playsound minecraft:entity.wither.spawn master @s ~ ~ ~ 10 0.7");
	}

	@Override
	public void death(EntityDeathEvent event) {
		PlayerUtils.executeCommandOnNearbyPlayers(mBoss.getLocation(), detectionRange, "playsound minecraft:entity.enderdragon.death master @s ~ ~ ~ 100 0.8");
		PlayerUtils.executeCommandOnNearbyPlayers(mBoss.getLocation(), detectionRange, "tellraw @s [\"\",{\"text\":\"" + ChatColor.GOLD + "[Captain Varcosa] " + ChatColor.WHITE + "Ye thought I be the one in control here? Yarharhar! N’argh me lad, I merely be its pawn! But now me soul can rest, and ye will be its next meal! Yarharhar!\",\"color\":\"purple\"}]");
		mEndLoc.getBlock().setType(Material.REDSTONE_BLOCK);
	}

	//Reduce damage taken for each player by a percent
	@Override
	public void bossDamagedByEntity(EntityDamageByEntityEvent event) {
		double damage = event.getDamage();
		switch (BossUtils.getPlayersInRangeForHealthScaling(mBoss, detectionRange)) {
			case 2:
				damage *= .7; //2142 hp
				break;
			case 3:
				damage *= .6; //2500 hp
				break;
			case 4:
				damage *= .55; //2730 hp
				break;
			case 5:
				damage *= .5; //3000 hp
				break;
			case 6:
				damage *= .475; //3157 hp
				break;
			case 7:
				damage *= .45; //3333 hp
				break;
			case 8:
				damage *= .44; //3409 hp
				break;
			default:
				damage *= 1;
				break;
		}
		event.setDamage(damage);

	}
}
