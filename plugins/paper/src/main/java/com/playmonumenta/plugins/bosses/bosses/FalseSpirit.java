package com.playmonumenta.plugins.bosses.bosses;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.playmonumenta.plugins.bosses.BossBarManager;
import com.playmonumenta.plugins.bosses.BossBarManager.BossHealthAction;
import com.playmonumenta.plugins.bosses.SpellManager;
import com.playmonumenta.plugins.bosses.spells.Spell;
import com.playmonumenta.plugins.bosses.spells.SpellBlockBreak;
import com.playmonumenta.plugins.bosses.spells.SpellPurgeNegatives;
import com.playmonumenta.plugins.bosses.spells.falsespirit.DamageBlocker;
import com.playmonumenta.plugins.bosses.spells.falsespirit.GatesOfHell;
import com.playmonumenta.plugins.bosses.spells.falsespirit.PassiveSeekingProjectile;
import com.playmonumenta.plugins.bosses.spells.falsespirit.SpellFlamethrower;
import com.playmonumenta.plugins.bosses.spells.falsespirit.SpellForceTwo;
import com.playmonumenta.plugins.bosses.spells.falsespirit.SpellMultiEarthshake;
import com.playmonumenta.plugins.bosses.spells.falsespirit.TriplicateSlash;
import com.playmonumenta.plugins.bosses.spells.falsespirit.UltimateBulletMania;
import com.playmonumenta.plugins.utils.BossUtils;
import com.playmonumenta.plugins.utils.DelvesUtils;
import com.playmonumenta.plugins.utils.EntityUtils;
import com.playmonumenta.plugins.utils.PlayerUtils;
import com.playmonumenta.plugins.utils.SerializationUtils;

public class FalseSpirit extends BossAbilityGroup {
	public static final String identityTag = "boss_falsespirit";
	public static final int detectionRange = 75;
	public static final int meleeRange = 10;

	private final Plugin mPlugin;
	public final LivingEntity mBoss;

	//If delves is enabled in instance, turn on delves mode
	private boolean mDelve = false;

	private final Location mSpawnLoc;
	private final Location mEndLoc;

	private static final int HEALTH_HEALED = 100;

	private static final Particle.DustOptions RED_COLOR = new Particle.DustOptions(Color.fromRGB(200, 0, 0), 1.0f);

	private UltimateBulletMania mMania;
	private GatesOfHell mHell;
	private GatesOfHell mCeilingHell;

	private static final String PORTAL_TAG = "PortalOfHell";
	//WARNING: Ceiling Portal should only have PORTAL_CEILING_TAG as a tag
	public static final String PORTAL_CEILING_TAG = "CeilingPoH";

	//The material that the g round that should damage you is made out of
	private static final EnumSet<Material> mGroundMats = EnumSet.of(
            Material.BLACK_CONCRETE_POWDER,
            Material.GRAY_CONCRETE_POWDER,
            Material.BLACKSTONE,
            Material.BASALT
        );

	public static BossAbilityGroup deserialize(Plugin plugin, LivingEntity boss) throws Exception {
		return SerializationUtils.statefulBossDeserializer(boss, identityTag, (spawnLoc, endLoc) -> {
			return new FalseSpirit(plugin, boss, spawnLoc, endLoc);
		});
	}

	@Override
	public String serialize() {
		return SerializationUtils.statefulBossSerializer(mSpawnLoc, mEndLoc);
	}

	public FalseSpirit(Plugin plugin, LivingEntity boss, Location spawnLoc, Location endLoc) {
		super(plugin, identityTag, boss);

		mPlugin = plugin;
		mBoss = boss;
		mSpawnLoc = spawnLoc;
		mEndLoc = endLoc;

		//Look to see if delves is enabled
		List<Player> players = PlayerUtils.playersInRange(mBoss.getLocation(), 100);
		for (Player player : players) {
			if (DelvesUtils.getDelveInfo(player).getDepthPoints() > 0) {
				mDelve = true;

				break;
			}
		}

		mMania = new UltimateBulletMania(boss, plugin);

		List<LivingEntity> portals = new ArrayList<>(24);
		LivingEntity ceilingPortal = null;
		for (Entity e : EntityUtils.getNearbyMobs(spawnLoc, 75, EnumSet.of(EntityType.ARMOR_STAND))) {
			if (e instanceof LivingEntity) {
				Set<String> tags = e.getScoreboardTags();
				for (String tag : tags) {
					switch (tag) {
						case PORTAL_TAG:
							portals.add((LivingEntity) e);
							break;
						case PORTAL_CEILING_TAG:
							ceilingPortal = (LivingEntity) e;
							break;
						default:
							break;
					}
				}
			}
		}

		mHell = new GatesOfHell(plugin, boss, portals, 1);
		mCeilingHell = new GatesOfHell(plugin, boss, new ArrayList<>(Arrays.asList(ceilingPortal)), 5);

		int multiEarthshakeDuration = 50;

		int passiveCooldown = 20 * 8;
		double passiveSpeed = .25;

		if (mDelve) {
			multiEarthshakeDuration = 30;

			passiveCooldown = 20 * 6;
			passiveSpeed = .3;
		}

		SpellManager phase1Spells = new SpellManager(Arrays.asList(
				new SpellForceTwo(plugin, boss, 5, 20 * 2),
				new TriplicateSlash(plugin, boss)
				));

		SpellManager activeSpells = new SpellManager(Arrays.asList(
				new SpellForceTwo(plugin, boss, 5, 20 * 2),
				new TriplicateSlash(plugin, boss),
				new SpellMultiEarthshake(plugin, boss, 1, multiEarthshakeDuration, mDelve, mSpawnLoc),
				new SpellFlamethrower(plugin, boss, mDelve)
				));



		List<Spell> passiveSpells = Arrays.asList(
				new SpellPurgeNegatives(boss, 20 * 5),
				new DamageBlocker(plugin, boss, mHell, mCeilingHell),
				new SpellBlockBreak(boss, 2, 3, 2),
				new PassiveSeekingProjectile(plugin, boss, passiveCooldown, passiveSpeed, mDelve)
			);

		Map<Integer, BossHealthAction> events = new HashMap<Integer, BossHealthAction>();

		events.put(85, mBoss -> {
			mHell.run();
			PlayerUtils.executeCommandOnNearbyPlayers(mSpawnLoc, detectionRange, "tellraw @s [\"\",{\"text\":\"The Gates open! Come forth, nithlings!\",\"color\":\"dark_red\"}]");
			PlayerUtils.executeCommandOnNearbyPlayers(mSpawnLoc, detectionRange, "tellraw @s [\"\",{\"text\":\"[Bhairavi]\",\"color\":\"gold\"},{\"text\":\" Quickly! Kill those creatures! They will charge the Spear with power and let you claim it!\",\"color\":\"white\"}]");
		});

		events.put(70, mBoss -> {
			mHell.run();
			PlayerUtils.executeCommandOnNearbyPlayers(mSpawnLoc, detectionRange, "tellraw @s [\"\",{\"text\":\"The Gates open once more! Come forth, nithlings!\",\"color\":\"dark_red\"}]");
			PlayerUtils.executeCommandOnNearbyPlayers(mSpawnLoc, detectionRange, "tellraw @s [\"\",{\"text\":\"[Bhairavi]\",\"color\":\"gold\"},{\"text\":\" Quickly! Kill those creatures! They will charge the Spear with power and let you claim it!\",\"color\":\"white\"}]");
		});

		events.put(66, mBoss -> {
			mMania.setSpeed(4);
			mMania.run();

			//Prevents other moves from being used during bullet world
			changePhase(null, null, null);
			new BukkitRunnable() {
				@Override
				public void run() {
					changePhase(activeSpells, passiveSpells, null);
				}
			}.runTaskLater(mPlugin, mMania.cooldownTicks());

			PlayerUtils.executeCommandOnNearbyPlayers(mSpawnLoc, detectionRange, "tellraw @s [\"\",{\"text\":\"I am more than the nithlings from my gates. The magic of Hallud flows through me. Battle me and perish.\",\"color\":\"dark_red\"}]");
		});

		events.put(33, mBoss -> {
			mMania.setSpeed(2);
			mMania.run();

			//Prevents other moves from being used during bullet world
			changePhase(null, null, null);
			new BukkitRunnable() {
				@Override
				public void run() {
					changePhase(activeSpells, passiveSpells, null);
				}
			}.runTaskLater(mPlugin, mMania.cooldownTicks());

			PlayerUtils.executeCommandOnNearbyPlayers(mSpawnLoc, detectionRange, "tellraw @s [\"\",{\"text\":\"Despite your efforts, nothingness is what you will return to. The magic of Midat is with me.\",\"color\":\"dark_red\"}]");
		});

		events.put(30, mBoss -> {
			mHell.run();
			PlayerUtils.executeCommandOnNearbyPlayers(mSpawnLoc, detectionRange, "tellraw @s [\"\",{\"text\":\"The Gates are opened! Come forth, nithlings!\",\"color\":\"dark_red\"}]");
			PlayerUtils.executeCommandOnNearbyPlayers(mSpawnLoc, detectionRange, "tellraw @s [\"\",{\"text\":\"[Bhairavi]\",\"color\":\"gold\"},{\"text\":\" Quickly! Kill those creatures! They will charge the Spear with power and let you claim it!\",\"color\":\"white\"}]");
		});

		events.put(20, mBoss -> {
			mHell.run();

			PlayerUtils.executeCommandOnNearbyPlayers(mSpawnLoc, detectionRange, "tellraw @s [\"\",{\"text\":\"The Gates open once again! Come forth, nithlings!\",\"color\":\"dark_red\"}]");
			PlayerUtils.executeCommandOnNearbyPlayers(mSpawnLoc, detectionRange, "tellraw @s [\"\",{\"text\":\"[Bhairavi]\",\"color\":\"gold\"},{\"text\":\" Quickly! Kill those creatures! They will charge the Spear with power and let you claim it!\",\"color\":\"white\"}]");
		});

		//Last one is the ceiling one
		events.put(10, mBoss -> {
			mCeilingHell.run();
			mMania.run();

			//Prevents other moves from being used during bullet world
			changePhase(null, null, null);
			new BukkitRunnable() {
				@Override
				public void run() {
					changePhase(activeSpells, passiveSpells, null);
				}
			}.runTaskLater(mPlugin, mMania.cooldownTicks());

			PlayerUtils.executeCommandOnNearbyPlayers(mSpawnLoc, detectionRange, "tellraw @s [\"\",{\"text\":\"The Final Gate opens. Meet your demise.\",\"color\":\"dark_red\"}]");
			PlayerUtils.executeCommandOnNearbyPlayers(mSpawnLoc, detectionRange, "tellraw @s [\"\",{\"text\":\"[Bhairavi]\",\"color\":\"gold\"},{\"text\":\" Quickly! Kill those creatures! They will charge the Spear with power and let you claim it!\",\"color\":\"white\"}]");
		});

		BossBarManager bossBar = new BossBarManager(plugin, boss, detectionRange, BarColor.WHITE, BarStyle.SEGMENTED_10, events);
		constructBoss(phase1Spells, passiveSpells, detectionRange, bossBar, 20 * 10);

		new BukkitRunnable() {
			int mTicks = 0;
			@Override
			public void run() {
				if (mBoss.isDead() || !mBoss.isValid()) {
					this.cancel();
				}

				if (mSpawnLoc.distance(mBoss.getLocation()) > meleeRange) {
					mTicks += 10;

					if (mTicks >= 20 * 5) {
						teleport(mSpawnLoc);
						mTicks = 0;
					}
				} else {
					mTicks = 0;
				}
			}
		}.runTaskTimer(mPlugin, 0, 10);

		new BukkitRunnable() {
			@Override
			public void run() {
				if (mBoss.isDead() || !mBoss.isValid()) {
					this.cancel();
				}

				//Damage players below the arena
				List<Player> players = PlayerUtils.playersInRange(mSpawnLoc, detectionRange);
				players.removeIf(p -> p.getGameMode() == GameMode.SPECTATOR);
				for (Player p : players) {
					if (p.getLocation().getY() <= 3 && mGroundMats.contains(p.getLocation().add(0, -1, 0).getBlock().getType())) {
						Vector vel = p.getVelocity();
						BossUtils.bossDamagePercent(mBoss, p, 0.1);
						p.setVelocity(vel);

						p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_HURT_ON_FIRE, SoundCategory.PLAYERS, 1, 0.5f);
						p.spawnParticle(Particle.FLAME, p.getLocation(), 10, 0.5, 0.25, 0.5, 0.2);
					}
				}
			}
		}.runTaskTimer(mPlugin, 0, 20);

		PlayerUtils.executeCommandOnNearbyPlayers(mSpawnLoc, detectionRange, "tellraw @s [\"\",{\"text\":\"I am deeper than the power of Malkus... I shall take you into the nothingness from which you came.\",\"color\":\"dark_red\"}]");

		PlayerUtils.executeCommandOnNearbyPlayers(mSpawnLoc, detectionRange, "title @s title [\"\",{\"text\":\"False Spirit\",\"color\":\"red\",\"bold\":true}]");
		PlayerUtils.executeCommandOnNearbyPlayers(mSpawnLoc, detectionRange, "title @s subtitle [\"\",{\"text\":\"Remnant of Olive\",\"color\":\"dark_red\",\"bold\":true}]");
		PlayerUtils.executeCommandOnNearbyPlayers(mSpawnLoc, detectionRange, "playsound minecraft:entity.wither.spawn master @s ~ ~ ~ 10 0.75");

	}

	@Override
	public void bossLaunchedProjectile(ProjectileLaunchEvent event) {
		if (event.getEntity() instanceof Fireball) {
			Fireball ball = (Fireball) event.getEntity();
			ball.setIsIncendiary(false);
			ball.setYield(0f);
			ball.setFireTicks(0);
			mBoss.getWorld().playSound(mBoss.getLocation(), Sound.ENTITY_BLAZE_SHOOT, SoundCategory.HOSTILE, 1, 1);
		}
	}


	@Override
	public void bossDamagedEntity(EntityDamageByEntityEvent event) {
		//If player dies, heal health
		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			if (event.getFinalDamage() > player.getHealth()) {
				World world = mBoss.getWorld();
				Location loc = mBoss.getLocation();

				//Heal code
				double hp = mBoss.getHealth() + HEALTH_HEALED;
				double max = mBoss.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
				if (hp >= max) {
					mBoss.setHealth(max);
				} else {
					mBoss.setHealth(hp);
				}
				world.playSound(loc, Sound.ENTITY_ILLUSIONER_CAST_SPELL, SoundCategory.HOSTILE, 1, 1.25f);
				world.playSound(loc, Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED,SoundCategory.HOSTILE,  1, 2f);
				world.spawnParticle(Particle.REDSTONE, loc.clone().add(0, 1, 0), 5, 0.15, 0.15, 0.15, RED_COLOR);
			}
		}
	}

	@Override
	public void bossDamagedByEntity(EntityDamageByEntityEvent event) {
		//Do not take damage to the gate closer trident
		if (event.getEntity() instanceof Trident) {
			Trident trident = (Trident) event.getEntity();
			ItemMeta im = trident.getItemStack().getItemMeta();

			if (im != null && im.getDisplayName().contains("Gate Closer")) {
				event.setCancelled(true);
				return;
			}
		}
	}

	@Override
	public void nearbyPlayerDeath(PlayerDeathEvent event) {
		new BukkitRunnable() {
			@Override
			public void run() {
				for (Item item : mBoss.getWorld().getNearbyEntitiesByType(Item.class, event.getEntity().getLocation(), 10)) {
					item.setInvulnerable(true);
				}
			}
		}.runTaskLater(mPlugin, 0);
	}

	@Override
	public boolean hasNearbyPlayerDeathTrigger() {
		return true;
	}


	//Teleport with special effects
	private void teleport(Location loc) {
		World world = loc.getWorld();
		world.playSound(mBoss.getLocation(), Sound.ENTITY_WITHER_SHOOT, SoundCategory.HOSTILE, 1, 0f);
		world.spawnParticle(Particle.FIREWORKS_SPARK, mBoss.getLocation().add(0, 1, 0), 70, 0.25, 0.45, 0.25, 0.15);
		world.spawnParticle(Particle.CLOUD, mBoss.getLocation().add(0, 1, 0), 35, 0.1, 0.45, 0.1, 0.15);
		world.spawnParticle(Particle.EXPLOSION_NORMAL, mBoss.getLocation(), 25, 0.2, 0, 0.2, 0.1);
		mBoss.teleport(loc);
		world.playSound(mBoss.getLocation(), Sound.ENTITY_WITHER_SHOOT, SoundCategory.HOSTILE, 1, 0f);
		world.spawnParticle(Particle.FIREWORKS_SPARK, mBoss.getLocation().add(0, 1, 0), 70, 0.25, 0.45, 0.25, 0.15);
		world.spawnParticle(Particle.SMOKE_LARGE, mBoss.getLocation().add(0, 1, 0), 35, 0.1, 0.45, 0.1, 0.15);
		world.spawnParticle(Particle.EXPLOSION_NORMAL, mBoss.getLocation(), 25, 0.2, 0, 0.2, 0.1);
	}

	@Override
	public void death(EntityDeathEvent event) {

		changePhase(null, null, null);
		mBoss.setHealth(100);
		mBoss.setInvulnerable(true);
		mBoss.setAI(false);
		mBoss.setGravity(false);
		mBoss.setPersistent(true);
		mBoss.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * 1000, 10));
		teleport(mSpawnLoc);
		World world = mBoss.getWorld();

		event.setCancelled(true);
		event.setReviveHealth(100);

		PlayerUtils.executeCommandOnNearbyPlayers(mSpawnLoc, detectionRange, "tellraw @s [\"\",{\"text\":\"The Tree of Life calls. I only wish to answer...\",\"color\":\"dark_red\"}]");

		new BukkitRunnable() {
			int mTicks = 0;
			@Override
			public void run() {
				if (mTicks >= 20 * 5) {
					mBoss.getWorld().playSound(mBoss.getLocation(), Sound.ENTITY_BLAZE_DEATH, SoundCategory.HOSTILE, 10, 0);

					this.cancel();
					mBoss.remove();

					new BukkitRunnable() {
						@Override
						public void run() {
							PlayerUtils.executeCommandOnNearbyPlayers(mBoss.getLocation(), detectionRange, "playsound minecraft:ui.toast.challenge_complete master @s ~ ~ ~ 100 0.8");
							PlayerUtils.executeCommandOnNearbyPlayers(mBoss.getLocation(), detectionRange, "title @s title [\"\",{\"text\":\"VICTORY\",\"color\":\"red\",\"bold\":true}]");
							PlayerUtils.executeCommandOnNearbyPlayers(mBoss.getLocation(), detectionRange, "title @s subtitle [\"\",{\"text\":\"False Spirit, Remnant of Olive\",\"color\":\"dark_red\",\"bold\":true}]");
							mEndLoc.getBlock().setType(Material.REDSTONE_BLOCK);
						}
					}.runTaskLater(mPlugin, 20 * 3);
				}

				if (mTicks % 10 == 0) {
					world.playSound(mBoss.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 1, 0);
				}

				world.spawnParticle(Particle.EXPLOSION_LARGE, mBoss.getLocation(), 1, 1, 1, 1);

				mTicks += 2;
			}
		}.runTaskTimer(mPlugin, 0, 2);
	}

	@Override
	public void init() {
		int bossTargetHp = 0;
		int playerCount = PlayerUtils.playersInRange(mBoss.getLocation(), detectionRange).size();
		int hpDel = 3012;
		int armor = (int)(Math.sqrt(playerCount * 2) - 1);

		/*
		 * New boss mechanic: The more players there are,
		 * the less invulnerability frames/no damage ticks it has.
		 * Note: A normal mob's maximum NoDamageTicks is 20, with 10 being when it can be damaged.
		 * It's really weird, but regardless, remember that its base will always be 20.
		 */
		int noDamageTicksTake = playerCount / 3;
		if (noDamageTicksTake > 5) {
			noDamageTicksTake = 5;
		}
		mBoss.setMaximumNoDamageTicks(mBoss.getMaximumNoDamageTicks() - noDamageTicksTake);
		bossTargetHp = (int) (hpDel * (1 + (1 - 1/Math.E) * Math.log(playerCount)));
		mBoss.getAttribute(Attribute.GENERIC_ARMOR).setBaseValue(armor);
		mBoss.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(bossTargetHp);
		mBoss.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(detectionRange);
		mBoss.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(1);
		mBoss.setHealth(bossTargetHp);

		mBoss.setPersistent(true);

	}
}
