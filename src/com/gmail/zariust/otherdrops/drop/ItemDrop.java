// OtherDrops - a Bukkit plugin
// Copyright (C) 2011 Robert Sargant, Zarius Tularial, Celtic Minstrel
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

package com.gmail.zariust.otherdrops.drop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.gmail.zariust.common.CommonEnchantments;
import com.gmail.zariust.common.CommonEntity;
import com.gmail.zariust.common.CommonMaterial;
import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.data.Data;
import com.gmail.zariust.otherdrops.data.ItemData;
import com.gmail.zariust.otherdrops.options.DoubleRange;
import com.gmail.zariust.otherdrops.options.IntRange;
import com.gmail.zariust.otherdrops.subject.Target;

public class ItemDrop extends DropType {
	private Material material;
	private Data durability;
	private IntRange quantity;
	private int rolledQuantity;
	private Map<Enchantment, IntRange> enchantments;
	public ItemDrop(Material mat) {
		this(mat, 100.0);
	}
	
	public ItemDrop(Material mat, int data) {
		this(mat, data, 100.0);
	}
	
	public ItemDrop(IntRange amount, Material mat) {
		this(amount, mat, 100.0, null);
	}

	public ItemDrop(IntRange amount, Material mat, int data) {
		this(amount, mat, data, 100.0, null, "");
	}

	public ItemDrop(ItemStack stack) {
		this(stack, 100.0);
	}
	
	public ItemDrop(Material mat, double percent) {
		this(mat, 0, percent);
	}
	
	public ItemDrop(Material mat, int data, double percent) {
		this(mat == null ? null : new ItemStack(mat, 1, (short) data), percent);
	}
	
	public ItemDrop(IntRange amount, Material mat, double percent, Map<Enchantment, IntRange> enchantment, String loreName) {
		this(amount, mat, 0, percent, enchantment, "");
		
		this.loreName = loreName;
	}

	public ItemDrop(IntRange amount, Material mat, double percent, Map<Enchantment, IntRange> enchantment) {
		this(amount, mat, 0, percent, enchantment, "");
	}
	
	public ItemDrop(IntRange amount, Material mat, int data, double percent, Map<Enchantment, IntRange> enchantment, String loreName) {
		this(amount, mat, new ItemData(data), percent, enchantment, loreName);
	}
	
	public ItemDrop(ItemStack stack, double percent) {
		this(new IntRange(stack == null ? 1 : stack.getAmount()), stack == null ? null : stack.getType(), stack == null ? null : new ItemData(stack), percent, null, "");
	}
	
	public ItemDrop(IntRange amount, Material mat, Data data, double percent, Map<Enchantment, IntRange> enchPass, String loreName) { // Rome
		super(DropCategory.ITEM, percent);
		quantity = amount;
		material = mat;
		durability = data;
		this.enchantments = enchPass;
		this.loreName = loreName;
	}

	/** Return an ItemStack that represents this item
	 * @return
	 */
	public ItemStack getItem() {
		return getItem((short)durability.getData());
	}
	
	public ItemStack getItem(short data) {
		rolledQuantity = quantity.getRandomIn(OtherDrops.rng);
		ItemStack stack = new ItemStack(material, rolledQuantity, data);
		stack = CommonEnchantments.applyEnchantments(stack, enchantments);
		setItemMeta(stack);
		return stack;
	}

	@Override
	protected DropResult performDrop(Target source, Location where, DropFlags flags) {
		DropResult dropResult = DropResult.getFromOverrideDefault(this.overrideDefault);		
		if(material == null || quantity.getMax() == 0) return dropResult;
		
		ItemStack stack = getItem(processTHISdata(source)); // get the item stack with relevant enchantments and/or metadata
		int count = 1; // if DropSpread is false we drop a single (multi-item) stack
		
		if(flags.spread) { // if DropSpread is true, then		
			stack.setAmount(1); // set amount to 1 as we're going to drop single items one by one
			count = rolledQuantity; // set #times to drop = #items to be dropped
		}
		
		while(count-- > 0) dropResult.addWithoutOverride(drop(where, stack, flags.naturally));
		
		setLoreName(dropResult.getDropped());
		return dropResult;
	}

	/** Sets any relevant metadata on the item (currently only leather armor color
	 * @param stack
	 */
	private void setItemMeta(ItemStack stack) {
		if (((ItemData)durability).itemMeta != null) {
			stack = ((ItemData)durability).itemMeta.setOn(stack);
		}
	}

	/** Check if data is THIS (-1) and get "self-data" accordingly
	 * 
	 * @param source
	 * @return data as a short (for use in an ItemStack)
	 */
	private short processTHISdata(Target source) {
		int itemData = durability.getData();
		if (itemData == -1) { // ie. itemData = THIS
			String[] dataSplit = source.toString().split("@");
			if (material.toString().equalsIgnoreCase("monster_egg")) { // spawn egg
				EntityType creatureType = CommonEntity.getCreatureEntityType(dataSplit[0]);
				if (creatureType != null) itemData = creatureType.getTypeId();
			} else {
				if (dataSplit.length > 1) itemData = ItemData.parse(material, dataSplit[1].replaceAll("SHEARED/", "")).getData(); // for wool, logs, etc
			}
			if (itemData == -1) itemData = 0; // reset to default data if we weren't able to parse anything else
		}
		return (short)itemData;
	}

	/** Sets lore name and (soon to be) description on the spawned item(s)
	 *  
	 * @param dropResult
	 */
	private void setLoreName(List<Entity> entityList) {
		if (entityList != null && !(loreName.isEmpty())) {
			for (Entity ent : entityList) {
				Item is = (Item)ent;
				ItemMeta im = is.getItemStack().getItemMeta();

				im.setDisplayName(loreName);
				is.getItemStack().setItemMeta(im);
			}
		}
	}

	public static DropType parse(String drop, String defaultData, IntRange amount, double chance) {
		//drop = drop.toUpperCase();
		String state = defaultData;
		String loreName = "";
		String[] split = drop.split("@", 2);
		drop = split[0];

		Map <Enchantment, IntRange> enchPass = new HashMap<Enchantment, IntRange>();

		if(split.length > 1) {
			state = split[1];
			String[] split2 = state.split("!", 2);
			state = split2[0];
			if (split2.length > 1) {
				
					String enchantment = split2[1];
					
					String[] split3 = enchantment.split("~");
					enchantment = split3[0];

					enchPass = CommonEnchantments.parseEnchantments(enchantment);
					//OtherDrops.logInfo(enchPass.keySet().toString());

					if (split3.length > 1) {
						loreName = split3[1];
					}
				
			}
		}

		Material mat = null;
		try {
			int dropInt = Integer.parseInt(drop);
			mat = Material.getMaterial(dropInt);
		} catch(NumberFormatException e) {
			mat = CommonMaterial.matchMaterial(drop);
		}
		if (mat == null) return null;


		// Parse data, which could be an integer or an appropriate enum name
		try {
			int d = Integer.parseInt(state);
			return new ItemDrop(amount, mat, d, chance, enchPass, loreName);
		} catch(NumberFormatException e) {}
		Data data = null;
		try {
			data = ItemData.parse(mat, state);
		} catch(IllegalArgumentException e) {
			Log.logWarning(e.getMessage());
			return null;
		}
		if(data != null) return new ItemDrop(amount, mat, data, chance, enchPass, loreName);
		return new ItemDrop(amount, mat, chance, enchPass, loreName);
	}

	@Override
	public String getName() {
		if (material == null) return "DEFAULT";
		String ret = material.toString();
		// TODO: Will durability ever be null, or will it just be 0?
		if(durability != null) {
			String dataString = durability.get(material);
			if (dataString != null) ret += (dataString.isEmpty()) ? "" : "@" + durability.get(material);
		}
		return ret;
	}

	@Override
	public double getAmount() {
		return rolledQuantity;
	}

	@Override
	public DoubleRange getAmountRange() {
		return quantity.toDoubleRange();
	}

	public Material getMaterial() {
		return material;
	}
}
