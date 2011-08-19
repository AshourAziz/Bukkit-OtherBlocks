package com.gmail.zariust.bukkit.otherblocks.options.tool;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.gmail.zariust.bukkit.otherblocks.drops.AbstractDrop;
import com.gmail.zariust.bukkit.otherblocks.options.ConfigOnly;
import com.gmail.zariust.bukkit.otherblocks.options.MaterialOption;
import com.gmail.zariust.bukkit.otherblocks.options.drop.ItemType;

@ConfigOnly(PlayerAgent.class)
public class ToolAgent implements Agent, MaterialOption {
	private Integer id, data;
	
	public ToolAgent() {
		this((Material) null);
	}
	
	public ToolAgent(Material tool) {
		this(tool, null);
	}
	
	public ToolAgent(Material tool, Integer d) {
		this(tool == null ? null : tool.getId(), d);
	}
	
	public ToolAgent(Integer tool, Integer type) {
		id = tool;
		data = type;
	}
	
	public ToolAgent(ItemStack item) {
		this(item == null ? null : item.getTypeId(), item == null ? null : (int) item.getDurability());
	}

	private boolean isEqual(ToolAgent tool) {
		if(tool == null) return false;
		return id == tool.id && data == tool.data;
	}
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof ToolAgent)) return false;
		ToolAgent tool = (ToolAgent) other;
		return isEqual(tool);
	}

	@Override
	public boolean matches(Agent other) {
		if(!(other instanceof PlayerAgent)) return false;
		PlayerAgent tool = (PlayerAgent) other;
		if(id == null) return true;
		else if(data == null) return id == tool.getMaterialId();
		else return isEqual(tool.getTool());
	}
	
	@Override
	public Material getMaterial() {
		return Material.getMaterial(id);
	}

	@Override
	public int getMaterialId() {
		return id == null ? -1 : id;
	}

	@Override
	public int hashCode() {
		return AbstractDrop.hashCode(ItemType.PLAYER, id == null ? 0 : id, data == null ? 0 : data);
	}

	@Override
	public int getData() {
		return data == null ? -1 : data;
	}

	@Override
	public ItemType getType() {
		return ItemType.PLAYER;
	}

	@Override public void damage(int amount) {}

	@Override public void damageTool(short amount) {}

	@Override public void damageTool() {}
}