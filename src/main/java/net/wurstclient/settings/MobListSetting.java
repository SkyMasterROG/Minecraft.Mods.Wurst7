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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

//import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
//import net.minecraft.util.registry.RegistryKey;
import net.wurstclient.WurstClient;
import net.wurstclient.clickgui.Component;
//import net.wurstclient.clickgui.components.BlockListEditButton;
import net.wurstclient.clickgui.components.MobListEditButton;
import net.wurstclient.keybinds.PossibleKeybind;
//import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.json.JsonException;
import net.wurstclient.util.json.JsonUtils;
import net.wurstclient.util.json.WsonArray;

public final class MobListSetting extends Setting
{
	private final ArrayList<String> mobIDs = new ArrayList<>();
	private final String[] defaultIDs;
	
	public MobListSetting(String name, String description, String... mobs)
	{
		super(name, description);
		
		/*Identifier id = Identifier.tryParse("minecraft:zombie_villager");
		EntityType<?> type = Registry.ENTITY_TYPE.get(id);
		Identifier idET = EntityType.getId(type);
		String typeId =  idET.toString();
		Boolean isValid =  Identifier.isValid("minecraft:zombie_villager");*/

		Arrays.stream(mobs).parallel()
			.map(s -> Registry.ENTITY_TYPE.get(Identifier.tryParse(s)))
			.filter(Objects::nonNull).map(s -> EntityType.getId(s).toString()).distinct()
			.sorted().forEachOrdered(s -> mobIDs.add(s));

		defaultIDs = mobIDs.toArray(new String[0]);
	}
	
	public List<String> getMobIDs()
	{
		return Collections.unmodifiableList(mobIDs);
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
		mobIDs.addAll(Arrays.asList(defaultIDs));
		WurstClient.INSTANCE.saveSettings();
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
		// Can't just list all the blocks here. Would need to change UI to allow
		// user to choose a block after selecting this option.
		// pkb.add(new PossibleKeybind(command + "add dirt",
		// "Add dirt to " + fullName));
		// pkb.add(new PossibleKeybind(command + "remove dirt",
		// "Remove dirt from " + fullName));
		pkb.add(new PossibleKeybind(command + "reset", "Reset " + fullName));
		
		return pkb;
	}
}
