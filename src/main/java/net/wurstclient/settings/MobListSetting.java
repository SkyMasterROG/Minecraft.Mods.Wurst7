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

//import net.minecraft.block.Block;
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
	private final ArrayList<String> mobIDs = new ArrayList<>();
	private final Map<String, ArrayList<String>> mobMap = new HashMap<>();

	private final String[] defaultValues;
	private final String sPat = ";";
	
	public MobListSetting(String name, String description, String... mobEntries)
	{
		super(name, description);
		
		/*Identifier id = Identifier.tryParse("minecraft:zombie_villager");
		EntityType<?> type = Registry.ENTITY_TYPE.get(id);
		Identifier idET = EntityType.getId(type);
		String typeId =  idET.toString();
		Boolean isValid =  Identifier.isValid("minecraft:zombie_villager");*/

		Arrays.stream(mobEntries).parallel()
			.map(s -> Registry.ENTITY_TYPE.get(Identifier.tryParse(s)))
			.filter(Objects::nonNull).map(s -> EntityType.getId(s).toString()).distinct()
			.sorted().forEachOrdered(s -> mobIDs.add(s));


		//drowned("Drowned", "Trident", net.minecraft.entity.EntityType.DROWNED, net.minecraft.item.Items.TRIDENT)
		String tmp01 = "minecraft:drowned" + sPat + "1" + sPat + "minecraft:trident";
		String tmp02 = "minecraft:squid" + sPat + "0" + sPat;
		String tmp03 = "minecraft:zombie_villager" + sPat + "1" + sPat;
		//String tmp04 = "minecraft:zombie_villager" + sPat + "a";
		//String tmp05 = "minecraft:zombie_villager" + sPat;
		//String tmp06 = "minecraft:non";
		mobEntries = new String[] {tmp01, tmp02, tmp03/*, tmp04, tmp05, tmp06*/};

		for (String mobEntry : mobEntries) {
			String[] entries = toMobValue(mobEntry);

			if (entries.length >= 2) {
				mobMap.put(entries[0], new ArrayList<>());
				for (String e : entries) {
					mobMap.get(entries[0]).add(e);
				}
			}
		}

		defaultValues = mobIDs.toArray(new String[0]);
	}
	
	public List<String> getMobIDs()
	{
		return Collections.unmodifiableList(mobIDs);
	}

	public Map<String, ArrayList<String>> getMobMap()
	{
		return Collections.unmodifiableMap(mobMap);
	}
	
	public void add(EntityType<?> mobType)
	{
		Identifier mobId = EntityType.getId(mobType);
		String mobIdStr = mobId.toString();
		if(Collections.binarySearch(mobIDs, mobIdStr) >= 0)
			return;
		
		mobIDs.add(mobIdStr);
		Collections.sort(mobIDs);
		WurstClient.INSTANCE.saveSettings();
	}

	public void put(EntityType<?> mob, Boolean enabled, Item item)
	{
		Identifier id = EntityType.getId(mob);
		String idS = id.toString();

		if(mobMap.containsKey(idS))
			return;

		String mobValue = toMobValue(mob, enabled);
		mobIDs.add(mobValue);

		mobMap.put(idS, new ArrayList<>());
		mobMap.get(idS).add(idS);
		mobMap.get(idS).add(Integer.toString(enabled ? 1 : 0));

		//Collections.sort(mobIDs);
//		Collections.sort(mobMap);
		//mobMap.;

		WurstClient.INSTANCE.saveSettings();
	}
	
	public void remove(int index)
	{
		if(index < 0 || index >= mobIDs.size())
			return;
		
		mobIDs.remove(index);
		WurstClient.INSTANCE.saveSettings();
	}
	
	public void resetToDefaults()
	{
		mobIDs.clear();
		mobIDs.addAll(Arrays.asList(defaultValues));
		WurstClient.INSTANCE.saveSettings();
	}
	
	public String toMobValue(EntityType<?> mob, Boolean enabled) {
		//String example = "minecraft:drowned" + sPat + "true" + sPat + "minecraft:trident";
		String result = Registry.ENTITY_TYPE.getId(mob).toString() + sPat;
		//result = result + Integer.valueOf(isEnabled ? 1 : 0).toString() + sPat;
		result = result + Integer.toString(enabled ? 1 : 0) + sPat;

		return result;
	}

	public String toMobValue(EntityType<?> mob, Boolean enabled, Item item) {
		//String example = "minecraft:drowned" + sPat + "true" + sPat + "minecraft:trident";
		String result = toMobValue(mob, enabled);
		result = result + Registry.ITEM.getId(item).toString() + sPat;
		
		return result;
	}

	public String[] toMobValue(String mobVaulue) {
		//String example = "minecraft:drowned" + sPat + "true" + sPat + "minecraft:trident";
		String[] result = mobVaulue.split(sPat);

		if (result.length >= 2) {
			boolean flag = Registry.ENTITY_TYPE.containsId(Identifier.tryParse( result[0] ));
			if (flag) {
				try {
					flag = Integer.parseInt(result[1], 10) > 0;
					flag = true;

				} catch (NumberFormatException e) {
					e.printStackTrace();
					flag = false;
				}

				if (flag && (result.length > 2)) {
					flag = Registry.ITEM.containsId(Identifier.tryParse( result[2] ));
				}
			}

			if (flag)
				return result;
		}

		return new String[0];
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
			mobIDs.clear();

			wson.getAllStrings().parallelStream()
				.map(s -> Registry.ENTITY_TYPE.get(Identifier.tryParse(s)))
				.filter(Objects::nonNull).map(s -> EntityType.getId(s).toString()).distinct()
				.sorted().forEachOrdered(s -> mobIDs.add(s));
			
		} catch(JsonException e) {
			e.printStackTrace();
			resetToDefaults();
		}
	}
	
	@Override
	public JsonElement toJson()
	{
		JsonArray json = new JsonArray();
		mobIDs.forEach(s -> json.add(s));
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
}
