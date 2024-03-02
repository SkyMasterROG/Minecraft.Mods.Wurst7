/*
 *
*/

package net.wurstclient.settings.filters;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.WolfEntity;

public final class FilterWolfsSetting extends EntityFilterCheckbox
{
	public FilterWolfsSetting(String description, boolean checked)
	{
		super("Filter wolfs", description, checked);
	}
	
	@Override
	public boolean test(Entity e)
	{
		return !(e instanceof WolfEntity);
	}
	
	public static FilterWolfsSetting genericCombat(boolean checked)
	{
		return new FilterWolfsSetting("Won't attack wolfs.", checked);
	}
	
	public static FilterWolfsSetting genericVision(boolean checked)
	{
		return new FilterWolfsSetting("Won't show wolfs.", checked);
	}
}
