package org.zeith.comms.c17racceconomy.utils.slots;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class VisibleSlot
		extends Slot
{
	public final ResourceLocation group;
	public int visibleX, visibleY;

	public VisibleSlot(IInventory inventory, int slot, int x, int y, ResourceLocation group, boolean visibleInitially)
	{
		super(inventory, slot, x, y);
		this.visibleX = x;
		this.visibleY = y;
		this.group = group;
		setVisible(visibleInitially);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean isActive()
	{
		return x >= 0 && y >= 0;
	}

	@Override
	public boolean hasItem()
	{
		return x >= 0 && y >= 0 && super.hasItem();
	}

	@Override
	public boolean mayPlace(ItemStack stack)
	{
		return super.mayPlace(stack) && container.canPlaceItem(index, stack);
	}

	public void setVisible(boolean visible)
	{
		if(visible)
		{
			x = visibleX;
			y = visibleY;
		} else
		{
			x = -12800;
			y = -12800;
		}
	}

	public static Predicate<Slot> filterVisibleSlotsInGroup(ResourceLocation group)
	{
		return s -> s instanceof VisibleSlot && Objects.equals(((VisibleSlot) s).group, group);
	}

	public static Consumer<Slot> setVisibilityForAll(boolean visible)
	{
		return s ->
		{
			if(s instanceof VisibleSlot)
				((VisibleSlot) s).setVisible(visible);
		};
	}

	public static void setRelativeCoords(int x, int y, List<Slot> slots)
	{
		int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;

		for(Slot slot : slots)
		{
			minX = Math.min(slot.x, minX);
			minY = Math.min(slot.y, minY);
		}

		int rX = x - minX, rY = y - minY;

		for(Slot slot : slots)
		{
			slot.x += rX;
			slot.y += rY;
		}
	}
}