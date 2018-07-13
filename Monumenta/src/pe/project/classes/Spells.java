package pe.project.classes;

import pe.project.Plugin.Classes;

public enum Spells {

	//Mage

	//--Core Abilities
	MANA_LANCE("Mana Lance", false, Classes.MAGE),
	FROST_NOVA("Frost Nova", false, Classes.MAGE),
	PRISMATIC_SHIELD("Prismatic Shield", false, Classes.MAGE),
	MAGMA_SHIELD("Magma Shield", false, Classes.MAGE),
	ARCANE_STRIKE("Arcane Strike", false, Classes.MAGE),

	//--Pyromancer Spec
	INFERNO("Inferno", false, Classes.MAGE),
	ASHEN_HEART("Ashen Heart", false, Classes.MAGE),

	//--Cyromancer Spec.
	BLIZZARD("Blizzard", false, Classes.MAGE),
	COLD_WAVE("Cold Wave", false, Classes.MAGE),
	FROZEN_HEART("Frozen Heart", false, Classes.MAGE),

	//Rogue

	//--Core Abilities
	BY_MY_BLADE("By My Blade", false, Classes.ROGUE),
	ADVANCING_SHADOWS("Advancing Shadows", false, Classes.ROGUE),
	DODGING("Dodging", false, Classes.ROGUE),
	ESCAPE_DEATH("Escape Death", false, Classes.ROGUE),
	SMOKESCREEN("Smokescreen", false, Classes.ROGUE),
	DAGGER_THROW("Dagger Throw", false, Classes.ROGUE),

	//--Swordsage Spec.
	SNAKE_HEAD("Snake Head", false, Classes.ROGUE),
	BLADE_SURGE("Blade Surge", false, Classes.ROGUE),
	BLADE_DANCE("Blade Dance", false, Classes.ROGUE),

	//--Assassin Spec.
	PERFECT_KILL("Perfect Kill", false, Classes.ROGUE),

	//Cleric
	CELESTIAL_BLESSING("Celestial Blessing", false, Classes.CLERIC),
	CELESTIAL_FAKE_1("Celestial Blessing", true, Classes.CLERIC),
	CELESTIAL_FAKE_2("Celestial Blessing", true, Classes.CLERIC),
	CLEANSING("Cleansing Rain", false, Classes.CLERIC),
	CLEANSING_FAKE("Cleansing Rain", true, Classes.CLERIC),
	HEALING("Hand of Light", false, Classes.CLERIC),

	//Scout

	//-- Core Abilities
	VOLLEY("Volley", false, Classes.SCOUT),
	STANDARD_BEARER("Standard Bearer", false, Classes.SCOUT),
	STANDARD_BEARER_FAKE("Standard Bearer", true, Classes.SCOUT),
	EAGLE_EYE("Eagle Eye", false, Classes.SCOUT),

	//--Sniper
	ENCHANTED_ARROW("Enchanted Arrow", false, Classes.SCOUT),

	//Warlock

	//--Core Abilities
	AMPLIFYING("Amplifying Hex", false, Classes.WARLOCK),
	BLASPHEMY("Blasphemous Aura", false, Classes.WARLOCK),
	SOUL_REND("Soul Rend", false, Classes.WARLOCK),
	CONSUMING_FLAMES("Consuming Flames", false, Classes.WARLOCK),
	GRASPING_CLAW("Grasping Claw", false, Classes.WARLOCK),

	//--Reaper
	DARK_ERUPTION("Dark Eruption", false, Classes.WARLOCK),
	PETRIFYING_GLARE("Petrifying Glare", false, Classes.WARLOCK),
	SOULREAPING("Soulreaping", false, Classes.WARLOCK),

	//--Tenebrist
	HYPNOTIC_RAGE("Hypnotic Rage", false, Classes.WARLOCK),
	SABLE_SPHERE("Sable Sphere", false, Classes.WARLOCK),
	BLACK_CLOUDS("Black Clouds", false, Classes.WARLOCK),

	//Warrior
	RIPOSTE("Riposte", false, Classes.WARRIOR),
	DEFENSIVE_LINE("Defensive Line", false, Classes.WARRIOR),

	//Alchemist
	POWER_INJECTION("Power Injection", false, Classes.ALCHEMIST),
	GRUESOME_ALCHEMY("Gruesome Alchemy", false, Classes.ALCHEMIST),
	PUTRID_FUMES("Putrid Fumes", false, Classes.ALCHEMIST),
	CAUSTIC_BLADE("Caustic Blade", false, Classes.ALCHEMIST),
	IRON_TINCTURE("Iron Tincture", false, Classes.ALCHEMIST),
	ENFEEBLING_ELIXIR("Enfeebling Elixir", false, Classes.ALCHEMIST),
	BOMB_ARROW("Unstable Arrows", false, Classes.ALCHEMIST);

	private final String name;
	private final boolean fake;
	private final Classes classes;

	Spells(String name, boolean fake, Classes classes) {
		this.name = name;
		this.fake = fake;
		this.classes = classes;
	}


	public String getName() {
		return name;
	}

	public Classes getBelongingClass() {
		return classes;
	}

	public boolean isFake() {
		return fake;
	}

}