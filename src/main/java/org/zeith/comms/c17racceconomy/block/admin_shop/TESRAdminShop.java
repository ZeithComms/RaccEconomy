package org.zeith.comms.c17racceconomy.block.admin_shop;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;
import org.zeith.hammerlib.client.render.tile.ITESR;

public class TESRAdminShop
		implements ITESR<TileAdminShop>
{
	@Override
	public void render(TileAdminShop tile, float partial, MatrixStack matrix, IRenderTypeBuffer buf, int lighting, int overlay, TileEntityRendererDispatcher renderer)
	{
		tile.onRender();

		ItemRenderer ir = Minecraft.getInstance().getItemRenderer();

		Direction dir = tile.getDirection();
		ItemStack stack = tile.goods.get();

		if(dir != null && !stack.isEmpty())
		{
			float rotation = (tile.ticksExisted + partial) / 20F;
			float floating = (MathHelper.sin(rotation) + 1) / 2F;

			matrix.translate(0.5F, 0.95F + floating * 0.15F, 0.5F);
			matrix.mulPose(Vector3f.YP.rotation(rotation));

			World world = tile.getLevel();
			int i;
			if(world != null) i = WorldRenderer.getLightColor(world, tile.getBlockPos().relative(dir));
			else i = 15728880;

			ir.render(stack, ItemCameraTransforms.TransformType.GROUND, false, matrix, buf, i, overlay, ir.getModel(stack, tile.getLevel(), null));
		}
	}
}
