package com.playmonumenta.bossfights.bosses;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;

import com.playmonumenta.bossfights.BossBarManager;
import com.playmonumenta.bossfights.Plugin;
import com.playmonumenta.bossfights.SpellManager;
import com.playmonumenta.bossfights.spells.SpellBaseLaser;
import com.playmonumenta.bossfights.spells.Spell;
import com.playmonumenta.bossfights.spells.SpellAxtalMeleeMinions;
import com.playmonumenta.bossfights.spells.SpellAxtalSneakup;
import com.playmonumenta.bossfights.spells.SpellAxtalTntThrow;
import com.playmonumenta.bossfights.spells.SpellAxtalWitherAoe;
import com.playmonumenta.bossfights.spells.SpellBlockBreak;
import com.playmonumenta.bossfights.spells.SpellConditionalTeleport;
import com.playmonumenta.bossfights.utils.SerializationUtils;
import com.playmonumenta.bossfights.utils.Utils;

public class CAxtal extends BossAbilityGroup {
	public static final String identityTag = "boss_caxtal";
	public static final int detectionRange = 110;

	LivingEntity mBoss;
	Location mSpawnLoc;
	Location mEndLoc;

	public static BossAbilityGroup deserialize(Plugin plugin, LivingEntity boss) throws Exception {
		String content = SerializationUtils.retrieveDataFromEntity(boss);

		if (content == null || content.isEmpty()) {
			throw new Exception("Can't instantiate " + identityTag + " with no serialized data");
		}

		Gson gson = new Gson();
		JsonObject object = gson.fromJson(content, JsonObject.class);

		if (!(object.has("spawnX") && object.has("spawnY") && object.has("spawnZ") &&
		        object.has("endX") && object.has("endY") && object.has("endZ"))) {
			throw new Exception("Failed to instantiate " + identityTag + ": missing required data element");
		}

		Location spawnLoc = new Location(boss.getWorld(), object.get("spawnX").getAsDouble(),
		                                 object.get("spawnY").getAsDouble(), object.get("spawnZ").getAsDouble());
		Location endLoc = new Location(boss.getWorld(), object.get("endX").getAsDouble(),
		                               object.get("endY").getAsDouble(), object.get("endZ").getAsDouble());

		return new CAxtal(plugin, boss, spawnLoc, endLoc);
	}

	public CAxtal(Plugin plugin, LivingEntity boss, Location spawnLoc, Location endLoc) {
		mBoss = boss;
		mSpawnLoc = spawnLoc;
		mEndLoc = endLoc;

		SpellManager activeSpells = new SpellManager(Arrays.asList(
			new SpellAxtalWitherAoe(plugin, mBoss, 13, 4),
			new SpellAxtalMeleeMinions(plugin, mBoss, 10, 3, 3, 20, 12),
			new SpellAxtalSneakup(plugin, mBoss),
			new SpellAxtalTntThrow(plugin, mBoss, 5, 15),
			new SpellBaseLaser(plugin, boss, 60, 140, false, true,
			                   // Tick action per player
			                   (Player player, int ticks, boolean blocked) -> {
			                       player.playSound(player.getLocation(), Sound.UI_TOAST_IN, 2, 0.5f + ((float)ticks / 100f) * 1.5f);
			                       boss.getLocation().getWorld().playSound(boss.getLocation(), Sound.UI_TOAST_IN, 2, 0.5f + ((float)ticks / 100f) * 1.5f);
			                       player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 2, 0.5f + ((float)ticks / 100f) * 1.5f);
			                       boss.getLocation().getWorld().playSound(boss.getLocation(), Sound.UI_TOAST_IN, 2, 0.5f + ((float)ticks / 100f) * 1.5f);
			                   },
			                   // Particles generated by the laser
			                   (Location loc) -> {
			                       loc.getWorld().spawnParticle(Particle.SMOKE_NORMAL, loc, 1, 0.02, 0.02, 0.02, 0);
			                       loc.getWorld().spawnParticle(Particle.SMOKE_LARGE, loc, 1, 0.02, 0.02, 0.02, 0);
			                       loc.getWorld().spawnParticle(Particle.SPELL_MOB, loc, 1, 0.02, 0.02, 0.02, 1);
			                   },
			                   // TNT generated at the end of the attack
			                   (Player player, Location loc, boolean blocked) -> {
			                       Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "summon tnt " + loc.getX() + " " + loc.getY() + " " + loc.getZ() + " {Fuse:1}");
			                       Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "summon tnt " + loc.getX() + " " + loc.getY() + " " + loc.getZ() + " {Fuse:3}");
			                       Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "summon tnt " + loc.getX() + " " + loc.getY() + " " + loc.getZ() + " {Fuse:5}");
			                   })
		));
		List<Spell> passiveSpells = Arrays.asList(
			new SpellBlockBreak(mBoss),
			// Teleport the boss to spawnLoc if he gets too far away from where he spawned
			new SpellConditionalTeleport(mBoss, spawnLoc, b -> spawnLoc.distance(b.getLocation()) > 80),
			// Teleport the boss to spawnLoc if he is stuck in bedrock
			new SpellConditionalTeleport(mBoss, spawnLoc, b -> b.getLocation().getBlock().getType() == Material.BEDROCK ||
			                                                   b.getLocation().add(0, 1, 0).getBlock().getType() == Material.BEDROCK ||
			                                                   b.getLocation().getBlock().getType() == Material.LAVA)
		);

		Map<Integer, String> events = new HashMap<Integer, String>();
		events.put(100, Utils.getExecuteCommandOnNearbyPlayers(spawnLoc, detectionRange, "tellraw @s [\"\",{\"text\":\"At last, the keys are collected. I can be free finally...\",\"color\":\"dark_red\"}]"));
		events.put(50,  Utils.getExecuteCommandOnNearbyPlayers(spawnLoc, detectionRange, "tellraw @s [\"\",{\"text\":\"PLEASE. KILL ME. KAUL HOLDS ONTO MY MIND, BUT I YEARN FOR FREEDOM.\",\"color\":\"dark_red\"}]"));
		events.put(25,  Utils.getExecuteCommandOnNearbyPlayers(spawnLoc, detectionRange, "tellraw @s [\"\",{\"text\":\"YOU ARE CLOSE. END THIS. END THE REVERIE!\",\"color\":\"dark_red\"}]"));
		events.put(10,  Utils.getExecuteCommandOnNearbyPlayers(spawnLoc, detectionRange, "tellraw @s [\"\",{\"text\":\"My servant is nearly dead. You dare to impose your will on the jungle?\",\"color\":\"dark_green\"}]"));
		BossBarManager bossBar = new BossBarManager(boss, detectionRange, BarColor.RED, BarStyle.SEGMENTED_10, events);

		super.constructBoss(plugin, identityTag, mBoss, activeSpells, passiveSpells, detectionRange, bossBar);
	}

	@Override
	public void init() {
		int bossTargetHp = 0;
		int player_count = Utils.playersInRange(mBoss.getLocation(), detectionRange).size();
		int hp_del = 1024;
		int armor = (int)(Math.sqrt(player_count * 2) - 1);
		while (player_count > 0) {
			bossTargetHp = bossTargetHp + hp_del;
			hp_del = hp_del / 2;
			player_count--;
		}
		mBoss.getAttribute(Attribute.GENERIC_ARMOR).setBaseValue(armor);
		mBoss.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(bossTargetHp);
		mBoss.setHealth(bossTargetHp);

		//launch event related spawn commands
		Utils.executeCommandOnNearbyPlayers(mBoss.getLocation(), detectionRange, "effect @s minecraft:blindness 2 2");
		Utils.executeCommandOnNearbyPlayers(mBoss.getLocation(), detectionRange, "title @s title [\"\",{\"text\":\"C'Axtal\",\"color\":\"dark_red\",\"bold\":true}]");
		Utils.executeCommandOnNearbyPlayers(mBoss.getLocation(), detectionRange, "title @s subtitle [\"\",{\"text\":\"The Soulspeaker\",\"color\":\"red\",\"bold\":true}]");
		Utils.executeCommandOnNearbyPlayers(mBoss.getLocation(), detectionRange, "playsound minecraft:entity.wither.spawn master @s ~ ~ ~ 10 0.7");
	}

	@Override
	public void death() {
		Utils.executeCommandOnNearbyPlayers(mBoss.getLocation(), detectionRange, "playsound minecraft:entity.enderdragon.death master @s ~ ~ ~ 100 0.8");
		Utils.executeCommandOnNearbyPlayers(mBoss.getLocation(), detectionRange, "tellraw @s [\"\",{\"text\":\"It ends at last... Is this what freedom feels like?..\",\"color\":\"dark_red\"}]");
		mEndLoc.getBlock().setType(Material.REDSTONE_BLOCK);
	}

	@Override
	public String serialize() {
		Gson gson = new GsonBuilder().create();
		JsonObject root = new JsonObject();

		root.addProperty("spawnX", mSpawnLoc.getX());
		root.addProperty("spawnY", mSpawnLoc.getY());
		root.addProperty("spawnZ", mSpawnLoc.getZ());
		root.addProperty("endX", mEndLoc.getX());
		root.addProperty("endY", mEndLoc.getY());
		root.addProperty("endZ", mEndLoc.getZ());

		return gson.toJson(root);
	}
}
