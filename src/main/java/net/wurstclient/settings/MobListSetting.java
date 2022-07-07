/*
 * Copyright (c) 2014-2022 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.wurstclient.WurstClient;
import net.wurstclient.clickgui.Component;
import net.wurstclient.clickgui.components.MobListEditButton;
import net.wurstclient.keybinds.PossibleKeybind;
import net.wurstclient.util.json.JsonException;
import net.wurstclient.util.json.JsonUtils;
import net.wurstclient.util.json.WsonArray;

public final class MobListSetting extends Setting
{
	private final ArrayList<MobValue> mobValues = new ArrayList<>();

	private final String[] defaultValues;
	
	public MobListSetting(String name, String description, String... mobValues)
	{
		super(name, description);

		try {
			Arrays.stream(mobValues).parallel()
				.map(e -> new MobValue(e))
				.filter(Objects::nonNull)
				.filter(e -> e.isValid())
				.forEachOrdered(e -> this.mobValues.add(e));

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//defaultValues = this.mobValues.toArray(new String[0]);
		//this.mobValues.forEach(e -> json.add(e.getString()));
		String tmp01 = this.mobValues.toString();
		this.defaultValues = new String[this.mobValues.size()];
		for (int i = 0; i < this.mobValues.size(); i++) {
			this.defaultValues[i] = this.mobValues.get(i).getString();
		}
	}
	
	public List<MobValue> getMobValues()
	{
		return Collections.unmodifiableList(this.mobValues);
	}
	
	public MobValue add(EntityType<?> mobType)
	{
		Identifier id = EntityType.getId(mobType);
		String idS = id.toString();
//		if(Collections.binarySearch(mobValues, idS) >= 0)
//			return;
		
		MobValue value = new MobValue(idS);
		this.mobValues.add(value);
		WurstClient.INSTANCE.saveSettings();

		return value;
	}

	public MobValue set(int index, MobValue value)
	{
		if ((index < 0) || (null == value))
			return null;

		this.mobValues.set(index, value);
		WurstClient.INSTANCE.saveSettings();

		return value;
	}
	
	public void remove(int index)
	{
		if(index < 0 || index >= this.mobValues.size())
			return;
		
		this.mobValues.remove(index);
		WurstClient.INSTANCE.saveSettings();
	}
	
	public void resetToDefaults()
	{
		mobValues.clear();

		ArrayList<MobValue> values = new ArrayList<>();
		Arrays.stream(defaultValues).parallel()
			.map(e -> new MobValue(e))
			.forEachOrdered(e -> values.add(e));

		this.mobValues.addAll(values);
		WurstClient.INSTANCE.saveSettings();
	}

	public Boolean isEnabled(EntityType<?> mobType)
	{
		for (MobValue value : this.mobValues) {
			if(value.isEnabled()){
				EntityType<?> type = Registry.ENTITY_TYPE.get(value.getMobId());
				if (mobType == type) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public Component getComponent()
	{
		return new MobListEditButton(this);
	}
	
	@Override
	public void fromJson(JsonElement json)
	{
		try {
			WsonArray wson = JsonUtils.getAsArray(json);
			this.mobValues.clear();

			wson.getAllStrings().parallelStream()
				.map(e -> new MobValue(e))
				.filter(Objects::nonNull)
				.filter(e -> e.isValid())
				.forEachOrdered(e -> this.mobValues.add(e));
			
		} catch(JsonException e) {
			e.printStackTrace();
			resetToDefaults();
		}
	}
	
	@Override
	public JsonElement toJson()
	{
		JsonArray json = new JsonArray();
		this.mobValues.forEach(e -> json.add(e.getString()));
		return json;
	}
	
	@Override
	public Set<PossibleKeybind> getPossibleKeybinds(String featureName)
	{
		String fullName = featureName + " " + getName();
		
		String command = ".moblist " + featureName.toLowerCase() + " ";
		command += getName().toLowerCase().replace(" ", "_") + " ";
		
		LinkedHashSet<PossibleKeybind> pkb = new LinkedHashSet<>();
		pkb.add(new PossibleKeybind(command + "reset", "Reset " + fullName));
		
		return pkb;
	}

	public class MobValue
	{
		private Identifier mobId, itemId;
		private boolean enabled, valid, itemB;

		private final int mobIdx = 0, enabledIdx = 1, itemIdx = 2;
		private final String sPat = ";";


		public MobValue (String mobVaulue) {
			this.mobId = null;
			this.valid = false;

			this.enabled = false;

			this.itemId = null;
			this.itemB = false;

			//String example = "minecraft:drowned" + sPat + "true" + sPat + "minecraft:trident";
			String[] result = mobVaulue.split(sPat);

			if (result.length >= 1) {
				this.mobId = Identifier.tryParse(result[this.mobIdx]);
				this.valid = Identifier.isValid(result[this.mobIdx]) &&
					Registry.ENTITY_TYPE.containsId(this.mobId);

				if (result.length > 1) {
					try {
						enabled = Integer.parseInt(result[enabledIdx], 10) > 0;
					}
					catch (NumberFormatException e) {
					}
				}

				if (result.length > 2) {
					this.itemId = Identifier.tryParse(result[itemIdx]);
					this.itemB = Registry.ITEM.containsId(this.itemId);
					if (!Identifier.isValid(result[this.itemIdx]) || !this.itemB )
						this.itemId = null;
				}
			}
		}

		public Identifier getMobId()
		{
			return this.mobId;
		}

		public Identifier getItemId()
		{
			return this.itemId;
		}

		public Boolean isEnabled()
		{
			return this.enabled && this.valid;
		}

		public Boolean isValid()
		{
			return this.valid;
		}

		public Boolean hasItem()
		{
			return this.itemB;
		}

		public void set(Boolean enabled)
		{
			this.enabled = enabled && this.valid;
		}

		public void set(Boolean enabled, Identifier itemId)
		{
			set(enabled);

			this.itemId = itemId;
			this.itemB = Registry.ITEM.containsId(this.itemId);
			if (!this.itemB )
				this.itemId = null;
		}

		public String toMobValue(EntityType<?> mob, Boolean enabled) {
			//String example = "minecraft:drowned" + sPat + "true" + sPat + "minecraft:trident";
			String result = Registry.ENTITY_TYPE.getId(mob).toString() + sPat;
			result = result + Integer.toString(enabled ? 1 : 0) + sPat;
	
			return result;
		}
	
		public String toMobValue(EntityType<?> mob, Boolean enabled, Item item) {
			//String example = "minecraft:drowned" + sPat + "true" + sPat + "minecraft:trident";
			String result = toMobValue(mob, enabled);
			result = result + Registry.ITEM.getId(item).toString() + sPat;
			
			return result;
		}
		
		public String getString()
		{
			String result = null;

			if (null != this.mobId) {
				result = this.mobId.toString() + sPat;
				result = result + Integer.toString(this.enabled ? 1 : 0) + sPat;

				if (null != this.itemId) {
					result = result + this.itemId.toString() + sPat;
				}
			}

			return result;
		}

		/*public String[] toArray()
		{
			return ;
		}*/

		@Override
		public String toString()
		{
			if (null != this.mobId)
				return this.mobId.toString();

			return super.toString();
		}
	}
}
