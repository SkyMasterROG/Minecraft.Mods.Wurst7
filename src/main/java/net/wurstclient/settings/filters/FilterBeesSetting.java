/*
 *
*/

package net.wurstclient.settings.filters;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.BeeEntity;

public final class FilterBeesSetting extends EntityFilterCheckbox
{
	public FilterBeesSetting(String description, boolean checked)
	{
		super("Filter bees", description, checked);
	}
	
	@Override
	public boolean test(Entity e)
	{
		return !(e instanceof BeeEntity);
	}
	
	public static FilterBeesSetting genericCombat(boolean checked)
	{
		return new FilterBeesSetting("Won't attack bees.", checked);
	}
	
	public static FilterBeesSetting genericVision(boolean checked)
	{
		return new FilterBeesSetting("Won't show bees.", checked);
	}
}
