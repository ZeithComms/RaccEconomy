package org.zeith.comms.c17racceconomy;

import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.tags.ItemTags;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;
import org.zeith.comms.c17racceconomy.block.BlocksRP;
import org.zeith.comms.c17racceconomy.commands.AdminPay;
import org.zeith.comms.c17racceconomy.commands.Balance;
import org.zeith.comms.c17racceconomy.commands.Pay;
import org.zeith.hammerlib.core.adapter.LanguageAdapter;
import org.zeith.hammerlib.event.recipe.RegisterRecipesEvent;

@Mod(RaccEconomy.MOD_ID)
public class RaccEconomy
{
	public static final String MOD_ID = "racceconomy";

	public RaccEconomy()
	{
		MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
		MinecraftForge.EVENT_BUS.addListener(this::addRecipes);
		LanguageAdapter.registerMod(MOD_ID);
	}

	private void registerCommands(RegisterCommandsEvent e)
	{
		Balance.register(e.getDispatcher());
		Pay.register(e.getDispatcher());
		AdminPay.register(e.getDispatcher());
	}

	private void addRecipes(RegisterRecipesEvent e)
	{
		e.shaped()
				.id(BlocksRP.SHOP.getRegistryName())
				.result(BlocksRP.SHOP)
				.shape("gwg", "psp", "pcp")
				.map('g', Tags.Items.INGOTS_GOLD)
				.map('w', Blocks.RED_WOOL)
				.map('p', ItemTags.PLANKS)
				.map('s', Items.SMOOTH_STONE)
				.map('c', Tags.Items.CHESTS_WOODEN)
				.register();
	}
}