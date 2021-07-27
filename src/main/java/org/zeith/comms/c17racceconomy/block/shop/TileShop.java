package org.zeith.comms.c17racceconomy.block.shop;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import org.zeith.comms.c17racceconomy.RaccEconomy;
import org.zeith.comms.c17racceconomy.api.CapabilityRaccEconomy;
import org.zeith.comms.c17racceconomy.api.EconomyAPI;
import org.zeith.comms.c17racceconomy.api.IRaccPackData;
import org.zeith.comms.c17racceconomy.api.IShopTile;
import org.zeith.comms.c17racceconomy.block.BlocksRP;
import org.zeith.comms.c17racceconomy.net.PacketRequestSync;
import org.zeith.comms.c17racceconomy.utils.ShopMode;
import org.zeith.hammerlib.annotations.RegistryName;
import org.zeith.hammerlib.annotations.SimplyRegister;
import org.zeith.hammerlib.annotations.TileRenderer;
import org.zeith.hammerlib.api.forge.TileAPI;
import org.zeith.hammerlib.api.inv.SimpleInventory;
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
public class TileShop
		extends TileSyncableTickable
		implements IContainerTile, IShopTile
{
	@TileRenderer(TESRShop.class)
	@RegistryName("shop")
	public static final TileEntityType<TileShop> SHOP = TileAPI.createType(TileShop.class, BlocksRP.SHOP);

	@NBTSerializable("Storage")
	public final SimpleInventory storage = new SimpleInventory(54);

	@NBTSerializable("Owner")
	private UUID _owner;

	@NBTSerializable("OwnerName")
	private String _ownerName;

	@NBTSerializable("Price")
	private long _price;

	@NBTSerializable("Goods")
	private ItemStack _goods = ItemStack.EMPTY;

	@NBTSerializable("Notifications")
	public boolean notifications;

	public final PropertyUUID owner = new PropertyUUID(DirectStorage.create(n -> _owner = n, () -> _owner));
	public final PropertyString ownerName = new PropertyString(DirectStorage.create(n -> _ownerName = n, () -> _ownerName));
	public final PropertyLong price = new PropertyLong(DirectStorage.create(n -> _price = n, () -> _price));
	public final PropertyItemStack goods = new PropertyItemStack(DirectStorage.create(n -> _goods = n, () -> _goods));

	protected boolean requestedRenderSync = false;

	public TileShop(TileEntityType<?> type)
	{
		super(type);

		this.dispatcher.registerProperty("owner", owner);
		this.dispatcher.registerProperty("ownerName", ownerName);
		this.dispatcher.registerProperty("price", price);
		this.dispatcher.registerProperty("goods", goods);

		this.storage.isStackValid = (i, stack) ->
		{
			ItemStack good = goods.get().copy();
			return ItemStack.isSame(good, stack) && ItemStack.tagMatches(good, stack);
		};
	}

	public TileShop()
	{
		this(SHOP);
	}

	public void sendNotificationToOwner(ITextComponent txt)
	{
		if(notifications && level instanceof ServerWorld)
		{
			ServerPlayerEntity player = level.getServer().getPlayerList().getPlayer(getOwner());
			if(player != null) player.sendMessage(txt, Util.NIL_UUID);
		}
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
		return ownerName.get() == null ? getBlockState().getBlock().getName() : new TranslationTextComponent("block." + RaccEconomy.MOD_ID + ".shop.owned", ownerName.get());
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
		return new ContainerShop(player, windowId, this, canModify(player));
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

		if(mode != null)
		{
			switch(mode)
			{
				case BUY:
				{
					ItemStack good = goods.get().copy();
					int left = good.getCount();

					for(ItemStack stack : storage)
						if(ItemStack.isSame(stack, good) && ItemStack.tagMatches(stack, good))
						{
							left = Math.max(0, left - stack.getCount());
							if(left == 0) break;
						}

					if(left == 0)
					{
						IRaccPackData rpd = CapabilityRaccEconomy.get(player);
						return rpd != null && rpd.getBalance() >= price.get();
					}
				}
				return false;

				case SELL:
				{
					ItemStack good = goods.get().copy();
					int left = good.getCount();
					int toFit = good.getCount();

					for(int i = 0; i < storage.getSlots(); ++i)
					{
						ItemStack inSlot = storage.getItem(i);
						if(inSlot.isEmpty())
							toFit = 0;
						else if(ItemStack.isSame(inSlot, good) && ItemStack.tagMatches(inSlot, good))
							toFit -= Math.min(toFit, inSlot.getMaxStackSize() - inSlot.getCount());
						if(toFit == 0) break;
					}

					for(ItemStack stack : player.inventory.items)
						if(ItemStack.isSame(stack, good) && ItemStack.tagMatches(stack, good))
						{
							left = Math.max(0, left - stack.getCount());
							if(left == 0) break;
						}

					if(left == 0 && toFit == 0)
					{
						IRaccPackData rpd = CapabilityRaccEconomy.get(player);
						return rpd != null;
					}
				}
				return false;
			}
		}

		return false;
	}

	@Override
	public boolean performTrade(ServerPlayerEntity player)
	{
		ShopMode mode = getMode();

		if(mode != null)
		{
			switch(mode)
			{
				case BUY:
				{
					ItemStack good = goods.get().copy();
					int left = good.getCount();

					for(ItemStack stack : storage)
						if(ItemStack.isSame(stack, good) && ItemStack.tagMatches(stack, good))
						{
							left = Math.max(0, left - stack.getCount());
							if(left == 0) break;
						}

					if(left == 0)
					{
						IRaccPackData rpd = CapabilityRaccEconomy.get(player);

						if(rpd != null && rpd.tryTakeBalance(price.get()))
						{
							left = good.getCount();
							for(ItemStack stack : storage)
								if(ItemStack.isSame(stack, good) && ItemStack.tagMatches(stack, good))
								{
									int rem = Math.min(left, stack.getCount());
									stack.shrink(rem);
									left = Math.max(0, left - rem);
									if(left == 0) break;
								}

							ItemStack give = this.goods.get().copy();
							if(!player.addItem(give)) player.drop(give, false);

							long ownerBal = EconomyAPI.getBalance(player.getServer(), owner.get());
							ownerBal += price.get();
							EconomyAPI.setBalance(player.getServer(), owner.get(), ownerBal);

							give = goods.get();

							sendNotificationToOwner(new TranslationTextComponent("block." + RaccEconomy.MOD_ID + ".shop.notification.buy", Integer.toString(give.getCount()), give.getDisplayName(), EconomyAPI.getBalanceText(price.get())));

							return true;
						} else
						{
							player.sendMessage(new TranslationTextComponent("block." + RaccEconomy.MOD_ID + ".shop.no_money.buy"), Util.NIL_UUID);
						}
					} else
					{
						player.sendMessage(new TranslationTextComponent("block." + RaccEconomy.MOD_ID + ".shop.no_stock", goods.get().getDisplayName()), Util.NIL_UUID);
					}
				}
				return false;

				case SELL:
				{
					ItemStack good = goods.get().copy();
					int left = good.getCount();
					int toFit = good.getCount();

					for(int i = 0; i < storage.getSlots(); ++i)
					{
						ItemStack inSlot = storage.getItem(i);
						if(inSlot.isEmpty())
							toFit = 0;
						else if(ItemStack.isSame(inSlot, good) && ItemStack.tagMatches(inSlot, good))
							toFit -= Math.min(toFit, inSlot.getMaxStackSize() - inSlot.getCount());
						if(toFit == 0) break;
					}

					for(ItemStack stack : player.inventory.items)
						if(ItemStack.isSame(stack, good) && ItemStack.tagMatches(stack, good))
						{
							left = Math.max(0, left - stack.getCount());
							if(left == 0) break;
						}

					if(left == 0 && toFit == 0)
					{
						IRaccPackData rpd = CapabilityRaccEconomy.get(player);

						long ownerBal = EconomyAPI.getBalance(player.getServer(), owner.get());
						if(rpd != null && ownerBal >= price.get())
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

							ItemStack give = this.goods.get().copy();
							for(int i = 0; i < storage.getSlots(); ++i)
							{
								ItemStack inSlot = storage.getItem(i);
								if(inSlot.isEmpty())
								{
									storage.setItem(i, give);
									break;
								} else if(ItemStack.isSame(inSlot, good) && ItemStack.tagMatches(inSlot, good))
								{
									int fit;
									inSlot.grow(fit = Math.min(give.getCount(), inSlot.getMaxStackSize() - inSlot.getCount()));
									give.shrink(fit);
									if(give.isEmpty())
										break;
								}
							}

							ownerBal -= price.get();
							EconomyAPI.setBalance(player.getServer(), owner.get(), ownerBal);

							sendNotificationToOwner(new TranslationTextComponent("block." + RaccEconomy.MOD_ID + ".shop.notification.sell", Integer.toString(give.getCount()), give.getDisplayName(), EconomyAPI.getBalanceText(price.get())));

							return true;
						} else
						{
							player.sendMessage(new TranslationTextComponent("block." + RaccEconomy.MOD_ID + ".shop.no_money.sell", new StringTextComponent(ownerName.get() + "")), Util.NIL_UUID);
						}
					} else if(toFit > 0)
					{
						player.sendMessage(new TranslationTextComponent("block." + RaccEconomy.MOD_ID + ".shop.overfilled"), Util.NIL_UUID);
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