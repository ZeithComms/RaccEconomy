package org.zeith.comms.c17racceconomy.block.admin_shop;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import org.zeith.comms.c17racceconomy.utils.ShopMode;
import org.zeith.hammerlib.api.blocks.IItemGroupBlock;
import org.zeith.hammerlib.api.blocks.IVisuallyDifferentBlock;
import org.zeith.hammerlib.api.forge.ContainerAPI;
import org.zeith.hammerlib.util.java.Cast;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public class BlockAdminShop
		extends ContainerBlock
		implements IItemGroupBlock, IVisuallyDifferentBlock
{
	static final VoxelShape SHAPE = Stream.of(
			box(13, 0, 13, 15, 10, 15),
			box(1, 0, 1, 3, 10, 3),
			box(1, 0, 13, 3, 10, 15),
			box(13, 0, 1, 15, 10, 3),
			box(0, 10, 0, 16, 12, 16),
			box(2, 12, 2, 14, 13, 14)
	).reduce((v1, v2) -> VoxelShapes.join(v1, v2, IBooleanFunction.OR)).get();

	static final VoxelShape RENDER_SHAPE = box(0, 0, 0, 16, 24, 16);

	public BlockAdminShop()
	{
		super(Properties.of(Material.METAL).harvestTool(ToolType.PICKAXE).strength(-1F).dynamicShape().noOcclusion());
	}

	@Override
	public VoxelShape getShape(BlockState p_220053_1_, IBlockReader p_220053_2_, BlockPos p_220053_3_, ISelectionContext p_220053_4_)
	{
		return SHAPE;
	}

	@Override
	public VoxelShape getVisualShape(BlockState p_230322_1_, IBlockReader p_230322_2_, BlockPos p_230322_3_, ISelectionContext p_230322_4_)
	{
		return RENDER_SHAPE;
	}

	@Override
	protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> def)
	{
		def.add(BlockStateProperties.HORIZONTAL_FACING, ShopMode.MODE);
	}

	@Override
	public BlockRenderType getRenderShape(BlockState state)
	{
		return BlockRenderType.MODEL;
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext ctx)
	{
		return defaultBlockState()
				.setValue(BlockStateProperties.HORIZONTAL_FACING, ctx.getHorizontalDirection().getOpposite())
				.setValue(ShopMode.MODE, ShopMode.SELL);
	}

	@Override
	public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult ray)
	{
		Cast.optionally(world.getBlockEntity(pos), TileAdminShop.class)
				.ifPresent(shop ->
				{
					shop.syncNow();
					ContainerAPI.openContainerTile(player, shop);
				});
		return ActionResultType.SUCCESS;
	}

	@Override
	public void setPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack)
	{
		if(entity != null)
		{
			Cast.optionally(world.getBlockEntity(pos), TileAdminShop.class)
					.ifPresent(t -> t.setOwner(entity.getUUID()));
			super.setPlacedBy(world, pos, state, entity, stack);
		} else
			world.destroyBlock(pos, true);
	}

	@Override
	public float getDestroyProgress(BlockState state, PlayerEntity player, IBlockReader world, BlockPos pos)
	{
		return Cast.optionally(world.getBlockEntity(pos), TileAdminShop.class)
				.filter(t -> t.isOwner(player))
				.map(t -> super.getDestroyProgress(state, player, world, pos))
				.orElse(0F);
	}

	@Nullable
	@Override
	public TileEntity newBlockEntity(IBlockReader world)
	{
		return new TileAdminShop();
	}

	@Override
	public ItemGroup getItemGroup()
	{
		return ItemGroup.TAB_REDSTONE;
	}

	@Override
	public BlockState handle(World world, BlockPos pos, BlockState state)
	{
		world.getBlockEntity(pos);
		return state;
	}
}
