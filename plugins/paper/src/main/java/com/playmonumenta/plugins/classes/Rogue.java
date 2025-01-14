package com.playmonumenta.plugins.classes;

import org.bukkit.entity.Player;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.abilities.rogue.AdvancingShadows;
import com.playmonumenta.plugins.abilities.rogue.ByMyBlade;
import com.playmonumenta.plugins.abilities.rogue.DaggerThrow;
import com.playmonumenta.plugins.abilities.rogue.Dodging;
import com.playmonumenta.plugins.abilities.rogue.EscapeDeath;
import com.playmonumenta.plugins.abilities.rogue.Skirmisher;
import com.playmonumenta.plugins.abilities.rogue.Smokescreen;
import com.playmonumenta.plugins.abilities.rogue.ViciousCombos;

import com.playmonumenta.plugins.abilities.rogue.assassin.BodkinBlitz;
import com.playmonumenta.plugins.abilities.rogue.assassin.CloakAndDagger;
import com.playmonumenta.plugins.abilities.rogue.assassin.CoupDeGrace;

import com.playmonumenta.plugins.abilities.rogue.swordsage.BladeDance;
import com.playmonumenta.plugins.abilities.rogue.swordsage.DeadlyRonde;
import com.playmonumenta.plugins.abilities.rogue.swordsage.WindWalk;



public class Rogue extends PlayerClass {

	Rogue(Plugin plugin, Player player) {
		mAbilities.add(new AdvancingShadows(plugin, player));
		mAbilities.add(new ByMyBlade(plugin, player));
		mAbilities.add(new DaggerThrow(plugin, player));
		mAbilities.add(new Dodging(plugin, player));
		mAbilities.add(new EscapeDeath(plugin, player));
		mAbilities.add(new Skirmisher(plugin, player));
		mAbilities.add(new Smokescreen(plugin, player));
		mAbilities.add(new ViciousCombos(plugin, player));
		mClass = 4;

		mSpecOne.mAbilities.add(new BladeDance(plugin, player));
		mSpecOne.mAbilities.add(new DeadlyRonde(plugin, player));
		mSpecOne.mAbilities.add(new WindWalk(plugin, player));
		mSpecOne.mSpecQuestScoreboard = "Quest103c";
		mSpecOne.mSpecialization = 7;
		mSpecOne.mSpecName = "Swordsage";

		mSpecTwo.mAbilities.add(new BodkinBlitz(plugin, player));
		mSpecTwo.mAbilities.add(new CloakAndDagger(plugin, player));
		mSpecTwo.mAbilities.add(new CoupDeGrace(plugin, player));
		mSpecTwo.mSpecQuestScoreboard = "Quest103j";
		mSpecTwo.mSpecialization = 8;
		mSpecTwo.mSpecName = "Assassin";

	}
}
