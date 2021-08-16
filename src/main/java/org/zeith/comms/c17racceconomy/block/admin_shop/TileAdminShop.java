package org.zeith.comms.c17racceconomy.block.admin_shop;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import org.zeith.comms.c17racceconomy.RaccEconomy;
import org.zeith.comms.c17racceconomy.api.CapabilityRaccEconomy;
import org.zeith.comms.c17racceconomy.api.IRaccPackData;
import org.zeith.comms.c17racceconomy.api.IShopTile;
import org.zeith.comms.c17racceconomy.block.BlocksRP;
import org.zeith.comms.c17racceconomy.net.PacketRequestSync;
import org.zeith.comms.c17racceconomy.utils.ShopMode;
import org.zeith.hammerlib.annotations.RegistryName;
import org.zeith.hammerlib.annotations.SimplyRegister;
import org.zeith.hammerlib.annotations.TileRenderer;
import org.zeith.hammerlib.api.forge.TileAPI;
import org.zeith.hammerlib.api.io.NBTSerializable;
import org.zeith.hammerlib.api.tiles.IContainerTile;
import org.zeith.hammerlib.net.Network;
import org.zeith.hammerlib.net.properties.PropertyItemStack;
import org.zeith.hammerlib.net.properties.PropertyLong;
import org.zeith.hammerlib.net.properties.PropertyString;
import org.zeith.hammerlib.net.properties.PropertyUUID;
import org.zeith.hammerlib.tiles.TileSyncableTickable;
import org.zeith.hammerlib.util.java.DirectStorage;

import java.util.Objects;
import java.util.UUID;

@SimplyRegister
public class TileAdminShop
		extends TileSyncableTickable
		implements IContainerTile, IShopTile
{
	@TileRenderer(TESRAdminShop.class)
	@RegistryName("admin_shop")
	public static final TileEntityType<TileAdminShop> ADMIN_SHOP = TileAPI.createType(TileAdminShop.class, BlocksRP.ADMIN_SHOP);

	@NBTSerializable("Owner")
	private UUID _owner;

	@NBTSerializable("OwnerName")
	private String _ownerName;

	@NBTSerializable("Price")
	private long _price;

	@NBTSerializable("Goods")
	private ItemStack _goods = ItemStack.EMPTY;

	public final PropertyUUID owner = new PropertyUUID(DirectStorage.create(n -> _owner = n, () -> _owner));
	public final PropertyString ownerName = new PropertyString(DirectStorage.create(n -> _ownerName = n, () -> _ownerName));
	public final PropertyLong price = new PropertyLong(DirectStorage.create(n -> _price = n, () -> _price));
	public final PropertyItemStack goods = new PropertyItemStack(DirectStorage.create(n -> _goods = n, () -> _goods));

	protected boolean requestedRenderSync = false;

	public TileAdminShop(TileEntityType<?> type)
	{
		super(type);

		this.dispatcher.registerProperty("owner", owner);
		this.dispatcher.registerProperty("ownerName", ownerName);
		this.dispatcher.registerProperty("price", price);
		this.dispatcher.registerProperty("goods", goods);
	}

	public TileAdminShop()
	{
		this(ADMIN_SHOP);
	}

	public void onRender()
	{
		if(!requestedRenderSync)
		{
			requestedRenderSync = true;
			Network.sendToServer(new PacketRequestSync(worldPosition));
		}
	}

	@Override
	public void update()
	{
		if(isOnServer() && level instanceof ServerWorld)
		{
			ServerWorld sw = (ServerWorld) level;
			if(_owner != null)
			{
				GameProfile profile = sw.getServer().getProfileCache().get(_owner);
				if(profile != null && profile.isComplete())
					ownerName.set(profile.getName());
			}
		}
	}

	public UUID getOwner()
	{
		return owner.get();
	}

	public void setOwner(UUID owner)
	{
		this.owner.set(owner);
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return ownerName.get() == null ? getBlockState().getBlock().getName() : new TranslationTextComponent("block." + RaccEconomy.MOD_ID + ".admin_shop.owned", ownerName.get());
	}

	public boolean isOwner(PlayerEntity e)
	{
		return Objects.equals(this.owner.get(), e.getUUID());
	}

	@Override
	public boolean canModify(PlayerEntity player)
	{
		return isOwner(player);
	}

	@Override
	public Container openContainer(PlayerEntity player, int windowId)
	{
		return new ContainerAdminShop(player, windowId, this, canModify(player));
	}

	@Override
	public ShopMode getMode()
	{
		BlockState state = level.getBlockState(worldPosition);
		return state.getProperties().contains(ShopMode.MODE) ? state.getValue(ShopMode.MODE) : null;
	}

	@Override
	public boolean canPerformTrade(PlayerEntity player)
	{
		ShopMode mode = getMode();

		if(goods.get().isEmpty()) return false;

		if(mode != null)
		{
			switch(mode)
			{
				case BUY:
				{
					IRaccPackData rpd = CapabilityRaccEconomy.get(player);
					return rpd != null && rpd.getBalance() >= price.get();
				}

				case SELL:
				{
					ItemStack good = goods.get().copy();
					int left = good.getCount();

					for(ItemStack stack : player.inventory.items)
						if(ItemStack.isSame(stack, good) && ItemStack.tagMatches(stack, good))
						{
							left = Math.max(0, left - stack.getCount());
							if(left == 0) break;
						}

					return left == 0;
				}
			}
		}

		return false;
	}

	@Override
	public boolean performTrade(ServerPlayerEntity player)
	{
		ShopMode mode = getMode();

		if(goods.get().isEmpty()) return false;

		if(mode != null)
		{
			switch(mode)
			{
				case BUY:
				{
					IRaccPackData rpd = CapabilityRaccEconomy.get(player);

					if(rpd != null && rpd.tryTakeBalance(price.get()))
					{
						ItemStack give = this.goods.get().copy();
						if(!player.addItem(give)) player.drop(give, false);
						return true;
					}
				}
				return false;

				case SELL:
				{
					ItemStack good = goods.get().copy();
					int left = good.getCount();

					for(ItemStack stack : player.inventory.items)
						if(ItemStack.isSame(stack, good) && ItemStack.tagMatches(stack, good))
						{
							left = Math.max(0, left - stack.getCount());
							if(left == 0) break;
						}

					if(left == 0)
					{
						IRaccPackData rpd = CapabilityRaccEconomy.get(player);

						if(rpd != null)
						{
							left = good.getCount();
							for(ItemStack stack : player.inventory.items)
								if(ItemStack.isSame(stack, good) && ItemStack.tagMatches(stack, good))
								{
									int rem = Math.min(left, stack.getCount());
									stack.shrink(rem);
									left = Math.max(0, left - rem);
									if(left == 0) break;
								}

							rpd.addBalance(price.get());

							return true;
						}
					}
				}
				return false;
			}
		}

		return false;
	}

	public void setMode(ShopMode mode)
	{
		if(getMode() == null) return;

		BlockState state = getBlockState().setValue(ShopMode.MODE, mode);
		if(!level.getBlockState(worldPosition).equals(state))
		{
			level.removeBlockEntity(worldPosition);
			level.setBlock(worldPosition, state, 3);
			clearCache();
			clearRemoved();
			level.setBlockEntity(worldPosition, this);
		}
	}

	public Direction getDirection()
	{
		BlockState state = level.getBlockState(worldPosition);
		return state.getProperties().contains(BlockStateProperties.HORIZONTAL_FACING) ? state.getValue(BlockStateProperties.HORIZONTAL_FACING) : null;
	}

	@Override
	public void setGoods(ServerPlayerEntity player, ItemStack stack)
	{
		if(isOwner(player))
			goods.set(stack);
	}

	@Override
	public void setPrice(ServerPlayerEntity player, long price)
	{
		if(isOwner(player))
			this.price.set(price);
	}
}