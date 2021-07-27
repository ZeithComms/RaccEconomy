package org.zeith.comms.c17racceconomy.api;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import org.zeith.comms.c17racceconomy.RaccEconomy;
import org.zeith.comms.c17racceconomy.impl.RaccEconomyDataImpl;
import org.zeith.hammerlib.annotations.Setup;

import java.util.Optional;

public class CapabilityRaccEconomy
{
	@CapabilityInject(IRaccPackData.class)
	public static Capability<IRaccPackData> RP_DATA = null;

	@Setup
	private static void setup()
	{
		MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, CapabilityRaccEconomy::attachCaps);
		MinecraftForge.EVENT_BUS.addListener(CapabilityRaccEconomy::clonePlayer);
		MinecraftForge.EVENT_BUS.addListener(CapabilityRaccEconomy::logIn);
		MinecraftForge.EVENT_BUS.addListener(CapabilityRaccEconomy::respawn);

		CapabilityManager.INSTANCE.register(IRaccPackData.class, new Capability.IStorage<IRaccPackData>()
				{
					@Override
					public INBT writeNBT(Capability<IRaccPackData> capability, IRaccPackData instance, Direction side)
					{
						return instance.serializeNBT();
					}

					@Override
					public void readNBT(Capability<IRaccPackData> capability, IRaccPackData instance, Direction side, INBT nbt)
					{
						instance.deserializeNBT((CompoundNBT) nbt);
					}
				},
				RaccEconomyDataImpl::new);
	}

	private static void attachCaps(AttachCapabilitiesEvent<Entity> e)
	{
		if(e.getObject() instanceof PlayerEntity)
			e.addCapability(new ResourceLocation(RaccEconomy.MOD_ID, "data"), RaccEconomyDataImpl.newInstance((PlayerEntity) e.getObject()));
	}

	private static void logIn(PlayerEvent.PlayerLoggedInEvent e)
	{
		PlayerEntity pe = e.getPlayer();
		if(pe instanceof ServerPlayerEntity)
			get(pe).sync();
	}

	private static void respawn(PlayerEvent.PlayerRespawnEvent e)
	{
		PlayerEntity pe = e.getPlayer();
		if(pe instanceof ServerPlayerEntity)
			get(pe).sync();
	}

	private static void clonePlayer(PlayerEvent.Clone e)
	{
		IRaccPackData original = get(e.getOriginal());
		IRaccPackData _new = get(e.getPlayer());

		_new.deserializeNBT(original.serializeNBT());
		_new.sync();
	}

	public static IRaccPackData get(PlayerEntity player)
	{
		return Optional.ofNullable(player).flatMap(pl -> pl.getCapability(RP_DATA).resolve()).orElse(null);
	}
}