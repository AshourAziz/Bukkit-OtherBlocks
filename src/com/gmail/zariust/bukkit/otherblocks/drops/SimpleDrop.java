	// OtherBlocks - a Bukkit plugin
// Copyright (C) 2011 Robert Sargant
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	 See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.	 If not, see <http://www.gnu.org/licenses/>.

package com.gmail.zariust.bukkit.otherblocks.drops;

import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;

import com.gmail.zariust.bukkit.otherblocks.PlayerWrapper;
import com.gmail.zariust.bukkit.otherblocks.options.DoubleRange;
import com.gmail.zariust.bukkit.otherblocks.options.IntRange;
import com.gmail.zariust.bukkit.otherblocks.options.ShortRange;
import com.gmail.zariust.bukkit.otherblocks.options.action.Action;
import com.gmail.zariust.bukkit.otherblocks.options.drop.DropType;
import com.gmail.zariust.bukkit.otherblocks.options.event.DropEvent;
import com.gmail.zariust.bukkit.otherblocks.options.target.Target;
import com.gmail.zariust.bukkit.otherblocks.options.tool.Agent;

public class SimpleDrop extends CustomDrop
{
	// Actions
	private DropType dropped;
	private DoubleRange quantity;
	private IntRange attackerDamage;
	private ShortRange toolDamage;
	private double dropSpread;
	private MaterialData replacementBlock;
	private List<DropEvent> events;
	private List<String> commands;
	private List<String> messages;
	private Set<Effect> effects;
	
	// Constructors TODO: Expand!?
	public SimpleDrop(Target targ, Action act) {
		super(targ, act);
	}
	
	// Tool Damage
	public ShortRange getToolDamage() {
		return toolDamage;
	}

	public void setToolDamage(ShortRange val) {
		toolDamage = val;
	}

	public void setToolDamage(short val) {
		toolDamage = new ShortRange(val, val);
	}
	
	public void setToolDamage(short low, short high) {
		toolDamage = new ShortRange(low, high);
	}
	
	// Quantity getters and setters
	public DoubleRange getQuantityRange() {
		return quantity;
	}
	
	public void setQuantity(double val) {
		quantity = new DoubleRange(val, val);
	}
	
	public void setQuantity(DoubleRange val) {
		quantity = val;
	}
	
	public void setQuantity(double low, double high) {
		quantity = new DoubleRange(low, high);
	}

	// The drop
	public void setDropped(DropType drop) {
		this.dropped = drop;
	}

	public DropType getDropped() {
		return dropped;
	}

	// The drop spread chance
	public void setDropSpread(double spread) {
		this.dropSpread = spread;
	}
	
	public void setDropSpread(boolean spread) {
		this.dropSpread = spread ? 100.0 : 0.0;
	}

	public double getDropSpreadChance() {
		return dropSpread;
	}

	public boolean getDropSpread() {
		if(dropSpread >= 100.0) return true;
		else if(dropSpread <= 0.0) return false;
		return rng.nextDouble() > dropSpread / 100.0;
	}

	// Attacker Damage
	public IntRange getAttackerDamageRange() {
		return attackerDamage;
	}

	public void setAttackerDamage(int val) {
		attackerDamage = new IntRange(val, val);
	}

	public void setAttackerDamage(IntRange val) {
		attackerDamage = val;
	}
	
	public void setAttackerDamage(int low, int high) {
		attackerDamage = new IntRange(low, high);
	}
	
	// Replacement
	public MaterialData getReplacement() {
		return replacementBlock;
	}
	
	public void setReplacement(MaterialData block) {
		if(!block.getItemType().isBlock()) throw new IllegalArgumentException("replacementblock must be a block");
		replacementBlock = block;
	}

	// Events
	public void setEvents(List<DropEvent> evt) {
		this.events = evt;
	}

	public List<DropEvent> getEvents() {
		return events;
	}

	// Commands
	public void setCommands(List<String> cmd) {
		this.commands = cmd;
	}

	public List<String> getCommands() {
		return commands;
	}

	// Messages
	public void setMessages(List<String> msg) {
		this.messages = msg;
	}

	public String getRandomMessage(double amount) {
		String msg = messages.get(rng.nextInt(messages.size()));
		msg = msg.replace("%q", Double.toString(amount));
		// TODO: Colour codes
		return msg;
	}

	public String getRandomMessage(int amount) {
		if(messages == null || messages.isEmpty()) return null;
		String msg = messages.get(rng.nextInt(messages.size()));
		msg = msg.replace("%q", Integer.toString(amount));
		// TODO: Colour codes
		return msg;
	}

	// Effects
	public void setEffects(Set<Effect> sfx) {
		this.effects = sfx;
	}

	public Set<Effect> getEffects() {
		return effects;
	}

	@Override
	public void run() {
		// We need a player for some things.
		Player who = null;
		if(event.getAgent() instanceof Player) who = (Player) event.getAgent();
		// We also need the location
		Location location = event.getLocation();
		// Effects first
		for(Effect effect : effects) {
			// TODO: Data, radius
			location.getWorld().playEffect(location, effect, 0);
		}
		// Now events TODO
		// Then the actual drop; if it's deny, the event is cancelled
		// Note that deny WILL NOT WORK with delay; if you try to do that,
		// the default drop will most likely drop. In fact, delay along with drop in general
		// may have unexpected effects.
		boolean dropNaturally = true; // TODO: How to make this specifiable in the config?
		boolean spreadDrop = getDropSpread();
		double amount = quantity.getRandomIn(rng);
		dropped.drop(location, amount, who, dropNaturally, spreadDrop, rng);
		// Send a message, if any
		if(who != null) {
			String msg = getRandomMessage(amount);
			if(msg != null) who.sendMessage(msg);
		}
		// Run commands, if any
		if(commands != null) {
			for(String command : commands) {
				boolean suppress = false;
				Boolean override = false;
				// Five possible prefixes (slash is optional in all of them)
				//   "/" - Run the command as the player, and send them any result messages
				//  "/!" - Run the command as the player, but send result messages to the console
				//  "/*" - Run the command as the player with op override, and send them any result messages
				// "/!*" - Run the command as the player with op override, but send result messages to the console
				//  "/$" - Run the command as the console, but send the player any result messages
				// "/!$" - Run the command as the console, but send result messages to the console
				if(who != null) command = command.replaceAll("%p", who.getName());
				if(command.startsWith("/")) command = command.substring(1);
				if(command.startsWith("!")) {
					command = command.substring(1);
					suppress = true;
				}
				if(command.startsWith("*")) {
					command = command.substring(1);
					override = true;
				} else if(command.startsWith("$")) {
					command = command.substring(1);
					override = null;
				}
				CommandSender from;
				if(who == null || override == null) from = new ConsoleCommandSender(Bukkit.getServer());
				else from = new PlayerWrapper(who, override, suppress);
				Bukkit.getServer().dispatchCommand(from, command);
			}
		}
		// Replacement block
		if(replacementBlock != null) {
			Block toReplace = location.getBlock();
			toReplace.setType(replacementBlock.getItemType());
			toReplace.setData(replacementBlock.getData());
		}
		Agent used = event.getTool();
		// Tool damage
		if(toolDamage != null) {
			short damage = toolDamage.getRandomIn(rng);
			used.damageTool(damage);
		} else used.damageTool();
		// Attacker damage
		if(attackerDamage != null) {
			int damage = attackerDamage.getRandomIn(rng);
			used.damage(damage);
		}
		// And finally, events
		for(DropEvent evt : events) {
			evt.executeAt(location);
		}
	}

	@Override
	public String getLogMessage() {
		StringBuilder log = new StringBuilder();
		log.append(quantity);
		log.append("x " + dropped);
		if(replacementBlock != null) log.append(", leaving " + replacementBlock.getItemType() + ",");
		return super.getLogMessage().replace("%d", log.toString());
	}
}
