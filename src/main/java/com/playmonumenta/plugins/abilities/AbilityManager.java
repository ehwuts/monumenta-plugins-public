package com.playmonumenta.plugins.abilities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import com.playmonumenta.plugins.abilities.alchemist.*;
import com.playmonumenta.plugins.abilities.cleric.*;
import com.playmonumenta.plugins.abilities.mage.*;
import com.playmonumenta.plugins.abilities.rogue.*;
import com.playmonumenta.plugins.abilities.scout.*;
import com.playmonumenta.plugins.abilities.warlock.*;
import com.playmonumenta.plugins.abilities.warrior.*;
import com.playmonumenta.plugins.abilities.warrior.berserker.*;
import com.playmonumenta.plugins.classes.Spells;
import com.playmonumenta.plugins.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SplashPotion;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.World;

public class AbilityManager {
	private static AbilityManager mManager = null;

	private Plugin mPlugin;
	private World mWorld;
	private Random mRandom;
	private List<Ability> mReferenceAbilities;
	private Map<UUID, AbilityCollection> mAbilities = new HashMap<UUID, AbilityCollection>();

	//Public manager methods
	//---------------------------------------------------------------------------------------------------------------

	public AbilityManager(Plugin plugin, World world, Random random) {
		mPlugin = plugin;
		mWorld = world;
		mRandom = random;
		mManager = this;

		mReferenceAbilities = Arrays.asList(
		                          // MAGE
		                          new ArcaneStrike(mPlugin, mWorld, mRandom, null),
		                          new ElementalArrows(mPlugin, mWorld, mRandom, null),
		                          new FrostNova(mPlugin, mWorld, mRandom, null),
		                          new MagePassive(mPlugin, mWorld, mRandom, null),
		                          new MagmaShield(mPlugin, mWorld, mRandom, null),
		                          new ManaLance(mPlugin, mWorld, mRandom, null),
		                          new PrismaticShield(mPlugin, mWorld, mRandom, null),
		                          new Spellshock(mPlugin, mWorld, mRandom, null),

		                          // ROGUE
		                          new AdvancingShadows(mPlugin, mWorld, mRandom, null),
		                          new ByMyBlade(mPlugin, mWorld, mRandom, null),
		                          new DaggerThrow(mPlugin, mWorld, mRandom, null),
		                          new Dodging(mPlugin, mWorld, mRandom, null),
		                          new EscapeDeath(mPlugin, mWorld, mRandom, null),
		                          new RoguePassive(mPlugin, mWorld, mRandom, null),
		                          new Smokescreen(mPlugin, mWorld, mRandom, null),
		                          new ViciousCombos(mPlugin, mWorld, mRandom, null),

		                          // SCOUT
		                          new Agility(mPlugin, mWorld, mRandom, null),
		                          new BowMastery(mPlugin, mWorld, mRandom, null),
		                          new Volley(mPlugin, mWorld, mRandom, null),
		                          new Swiftness(mPlugin, mWorld, mRandom, null),
		                          new EagleEye(mPlugin, mWorld, mRandom, null),
		                          new ScoutPassive(mPlugin, mWorld, mRandom, null),

		                          // WARRIOR
		                          new BruteForce(mPlugin, mWorld, mRandom, null),
		                          new CounterStrike(mPlugin, mWorld, mRandom, null),
		                          new DefensiveLine(mPlugin, mWorld, mRandom, null),
		                          new Frenzy(mPlugin, mWorld, mRandom, null),
		                          new Riposte(mPlugin, mWorld, mRandom, null),
		                          new Toughness(mPlugin, mWorld, mRandom, null),
		                          new WarriorPassive(mPlugin, mWorld, mRandom, null),
		                          new WeaponryMastery(mPlugin, mWorld, mRandom, null),
		                          // BERSERKER
		                          new MeteorSlam(mPlugin, mWorld, mRandom, null),
		                          new Psychosis(mPlugin, mWorld, mRandom, null),
		                          new RecklessSwing(mPlugin, mWorld, mRandom, null),

		                          // CLERIC
		                          new Celestial(mPlugin, mWorld, mRandom, null),
		                          new CleansingRain(mPlugin, mWorld, mRandom, null),
		                          new HandOfLight(mPlugin, mWorld, mRandom, null),
		                          new ClericPassive(mPlugin, mWorld, mRandom, null),
		                          new DivineJustice(mPlugin, mWorld, mRandom, null),
		                          new HeavenlyBoon(mPlugin, mWorld, mRandom, null),
		                          new Rejuvenation(mPlugin, mWorld, mRandom, null),
		                          new Sanctified(mPlugin, mWorld, mRandom, null),

		                          //WARLOCK
		                          new AmplifyingHex(mPlugin, mWorld, mRandom, null),
		                          new BlasphemousAura(mPlugin, mWorld, mRandom, null),
		                          new ConsumingFlames(mPlugin, mWorld, mRandom, null),
		                          new CursedWound(mPlugin, mWorld, mRandom, null),
		                          new GraspingClaws(mPlugin, mWorld, mRandom, null),
		                          new SoulRend(mPlugin, mWorld, mRandom, null),
		                          new WarlockPassive(mPlugin, mWorld, mRandom, null),

		                          // ALCHEMIST
		                          new BasiliskPoison(mPlugin, mWorld, mRandom, null),
		                          new UnstableArrows(mPlugin, mWorld, mRandom, null),
		                          new PowerInjection(mPlugin, mWorld, mRandom, null),
		                          new IronTincture(mPlugin, mWorld, mRandom, null),
		                          new GruesomeAlchemy(mPlugin, mWorld, mRandom, null),
		                          new BrutalAlchemy(mPlugin, mWorld, mRandom, null),
		                          new EnfeeblingElixir(mPlugin, mWorld, mRandom, null),
		                          new AlchemistPassive(mPlugin, mWorld, mRandom, null),
		                          new AlchemistPotions(mPlugin, mWorld, mRandom, null)
		                      );
	}

	public static AbilityManager getManager() {
		return mManager;
	}

	public void updatePlayerAbilities(Player player) {
		mAbilities.put(player.getUniqueId(), getCurrentAbilities(player));
	}

	public Ability getPlayerAbility(Player player, String scoreboardId) {
		return getPlayerAbilities(player).getAbility(scoreboardId);
	}

	public Ability getPlayerAbility(Player player, Spells spell) {
		return getPlayerAbilities(player).getAbility(spell);
	}

	public String printInfo(Player player) {
		JsonObject object = getPlayerAbilities(player).getAsJsonObject();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJson(object);
	}

	/**
	 * Called whenever a player deals damage.
	 *
	 * All classes that modify player damage should register in this function
	 */
	public void modifyDamage(Player player, EntityDamageByEntityEvent event) {
		Celestial.modifyDamage(player, event);
	}

	//Events
	//---------------------------------------------------------------------------------------------------------------

	public boolean LivingEntityDamagedByPlayerEvent(Player player, EntityDamageByEntityEvent event) {
		for (Ability abil : getPlayerAbilities(player).getAbilities()) {
			if (abil.canCast()) {
				if (!abil.LivingEntityDamagedByPlayerEvent(event)) {
					return false;
				}
			}
		}
		return true;
	}

	public boolean PlayerDamagedEvent(Player player, EntityDamageEvent event) {
		for (Ability abil : getPlayerAbilities(player).getAbilities()) {
			if (abil.canCast()) {
				if (!abil.PlayerDamagedEvent(event)) {
					return false;
				}
			}
		}
		return true;
	}

	public boolean PlayerDamagedByLivingEntityEvent(Player player, EntityDamageByEntityEvent event) {
		for (Ability abil : getPlayerAbilities(player).getAbilities()) {
			if (abil.canCast()) {
				if (!abil.PlayerDamagedByLivingEntityEvent(event)) {
					return false;
				}
			}
		}
		return true;
	}

	public boolean PlayerDamagedByProjectileEvent(Player player, EntityDamageByEntityEvent event) {
		for (Ability abil : getPlayerAbilities(player).getAbilities()) {
			if (abil.canCast()) {
				if (!abil.PlayerDamagedByProjectileEvent(event)) {
					return false;
				}
			}
		}
		return true;
	}

	public boolean LivingEntityShotByPlayerEvent(Player player, Arrow arrow, LivingEntity damagee, EntityDamageByEntityEvent event) {
		for (Ability abil : getPlayerAbilities(player).getAbilities()) {
			if (abil.canCast()) {
				if (!abil.LivingEntityShotByPlayerEvent(arrow, (LivingEntity)damagee, event)) {
					return false;
				}
			}
		}
		return true;
	}

	public boolean PlayerShotArrowEvent(Player player, Arrow arrow) {
		for (Ability abil : getPlayerAbilities(player).getAbilities()) {
			if (abil.canCast()) {
				if (!abil.PlayerShotArrowEvent(arrow)) {
					return false;
				}
			}
		}
		return true;
	}

	public boolean PlayerThrewSplashPotionEvent(Player player, SplashPotion potion) {
		for (Ability abil : getPlayerAbilities(player).getAbilities()) {
			if (abil.canCast()) {
				if (!abil.PlayerThrewSplashPotionEvent(potion)) {
					return false;
				}
			}
		}
		return true;
	}

	public void EntityDeathEvent(Player player, EntityDeathEvent event, boolean shouldGenDrops) {
		for (Ability abil : getPlayerAbilities(player).getAbilities()) {
			if (abil.canCast()) {
				abil.EntityDeathEvent(event, shouldGenDrops);
			}
		}
	}

	public void PlayerItemHeldEvent(Player player, ItemStack mainHand, ItemStack offHand) {
		for (Ability abil : getPlayerAbilities(player).getAbilities()) {
			if (abil.canCast()) {
				abil.PlayerItemHeldEvent(mainHand, offHand);
			}
		}
	}

	public void PlayerRespawnEvent(Player player) {
		for (Ability abil : getPlayerAbilities(player).getAbilities()) {
			if (abil.canCast()) {
				abil.PlayerRespawnEvent();
			}
		}
	}

	public void ProjectileHitEvent(Player player, ProjectileHitEvent event, Arrow arrow) {
		for (Ability abil : getPlayerAbilities(player).getAbilities()) {
			if (abil.canCast()) {
				abil.ProjectileHitEvent(event, arrow);
			}
		}
	}

	public void PlayerExtendedSneakEvent(Player player) {
		for (Ability abil : getPlayerAbilities(player).getAbilities()) {
			if (abil.canCast()) {
				abil.PlayerExtendedSneakEvent();
			}
		}
	}

	public boolean PlayerSplashPotionEvent(Player player, Collection<LivingEntity> affectedEntities,
	                                       ThrownPotion potion, PotionSplashEvent event) {
		boolean re = true;
		for (Ability abil : getPlayerAbilities(player).getAbilities()) {
			if (abil.canCast()) {
				if (!abil.PlayerSplashPotionEvent(affectedEntities, potion, event)) {
					re = false;
				}
			}
		}
		return re;
	}

	public boolean PlayerSplashedByPotionEvent(Player player, Collection<LivingEntity> affectedEntities,
	                                           ThrownPotion potion, PotionSplashEvent event) {
		boolean re = true;
		for (Ability abil : getPlayerAbilities(player).getAbilities()) {
			if (abil.canCast()) {
				if (!abil.PlayerSplashedByPotionEvent(affectedEntities, potion, event)) {
					re = false;
				}
			}
		}
		return re;
	}

	public void PeriodicTrigger(Player player, boolean twoHertz, boolean oneSecond, boolean twoSeconds, boolean fourtySeconds, boolean sixtySeconds, int originalTime) {
		for (Ability abil : getPlayerAbilities(player).getAbilities()) {
			if (abil.canCast()) {
				abil.PeriodicTrigger(twoHertz, oneSecond, twoSeconds, fourtySeconds, sixtySeconds, originalTime);
			}
		}
	}

	public void PlayerInteractEvent(Player player, Action action, ItemStack itemInHand, Material blockClicked) {
		for (Ability abil : getPlayerAbilities(player).getAbilities()) {
			AbilityInfo info = abil.getInfo();
			if (info.trigger != null) {
				if (info.trigger == AbilityTrigger.LEFT_CLICK) {
					if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
						if (abil.runCheck() && !abil.isOnCooldown()) {
							if (abil.cast()) {
								abil.putOnCooldown();
							}
						}
					}
				} else if (info.trigger == AbilityTrigger.RIGHT_CLICK) {
					if (action == Action.RIGHT_CLICK_AIR
					    || (action == Action.RIGHT_CLICK_BLOCK && !blockClicked.isInteractable())) {
						if (abil.runCheck() && !abil.isOnCooldown()) {
							abil.cast();
						}
					}
				}
			}
		}
	}

	public void setupClassPotionEffects(Player player) {
		for (Ability abil : getPlayerAbilities(player).getAbilities()) {
			abil.setupClassPotionEffects();
		}
	}

	//---------------------------------------------------------------------------------------------------------------

	//Private methods
	private AbilityCollection getCurrentAbilities(Player player) {
		List<Ability> abilities = new ArrayList<Ability>();
		try {
			for (Ability ab : mReferenceAbilities) {
				if (ab.canUse(player)) {
					Class[] constructorTypes = new Class[4];
					constructorTypes[0] = Plugin.class;
					constructorTypes[1] = World.class;
					constructorTypes[2] = Random.class;
					constructorTypes[3] = Player.class;

					Ability newAbility = ab.getClass().getDeclaredConstructor(constructorTypes).newInstance(mPlugin, mWorld, mRandom, player);
					abilities.add(newAbility);
				}
			}
		} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | java.lang.reflect.InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new AbilityCollection(abilities);
	}

	private AbilityCollection getPlayerAbilities(Player player) {
		if (!mAbilities.containsKey(player.getUniqueId())) {
			updatePlayerAbilities(player);
		}
		return mAbilities.get(player.getUniqueId());
	}

	//---------------------------------------------------------------------------------------------------------------
}