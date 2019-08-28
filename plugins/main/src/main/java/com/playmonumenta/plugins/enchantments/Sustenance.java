package com.playmonumenta.plugins.enchantments;

import java.util.EnumSet;

import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.enchantments.EnchantmentManager.ItemSlot;
import com.playmonumenta.plugins.utils.EntityUtils;
import com.playmonumenta.plugins.utils.MetadataUtils;

public class Sustenance implements BaseEnchantment{
	private static String PROPERTY_NAME = ChatColor.GRAY + "Sustenance";
	@Override
	public String getProperty() {
		return PROPERTY_NAME;
	}

	@Override
	public EnumSet<ItemSlot> validSlots() {
		return EnumSet.of(ItemSlot.MAINHAND, ItemSlot.OFFHAND, ItemSlot.ARMOR);
	}

	@Override
	public void onKill(Plugin plugin, Player player, int level, Entity target, EntityDeathEvent event) {
		if(target.getLastDamageCause().getCause() == DamageCause.ENTITY_ATTACK && !MetadataUtils.happenedThisTick(plugin, player, EntityUtils.PLAYER_DEALT_CUSTOM_DAMAGE_METAKEY, 0)) {
			player.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 1, 0), 12, 0.4, 0.5, 0.4, 0);
			player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 10f, 0.8f);
			player.setSaturation(player.getSaturation() + level);
		}
	}
}