package org.zeith.comms.c17racceconomy.api;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.world.storage.FolderName;
import org.zeith.comms.c17racceconomy.RaccEconomy;
import org.zeith.comms.c17racceconomy.impl.RaccEconomyDataImpl;

import java.io.File;
import java.util.UUID;
import java.util.function.Consumer;

public class OfflineRaccEconomyData
{
	public static Reader read(MinecraftServer server, UUID player)
	{
		File playerdata = server.getWorldPath(FolderName.PLAYER_DATA_DIR).toFile();
		File dst = new File(playerdata, player + ".dat");

		CompoundNBT read = null;
		try
		{
			if(dst.exists() && dst.isFile())
				read = CompressedStreamTools.readCompressed(dst);
		} catch(Exception err)
		{
			err.printStackTrace();
			return null;
		}

		if(read == null)
			read = new CompoundNBT();

		Reader theReader = null;

		CompoundNBT ForgeCaps = read.getCompound("ForgeCaps");
		{
			CompoundNBT data = ForgeCaps.getCompound(RaccEconomy.MOD_ID + ":data");
			{
				CompoundNBT allData = read;

				Consumer<Reader> save = reader ->
				{
					ServerPlayerEntity spe = server.getPlayerList().getPlayer(player);

					if(spe != null)
					{
						// Apply to online player
						CapabilityRaccEconomy.get(spe).deserializeNBT(reader.serializeNBT());
					} else
						try
						{
							// Update the variable
							ForgeCaps.put(RaccEconomy.MOD_ID + ":data", reader.serializeNBT());

							// Save
							File file1 = File.createTempFile(player + "-", ".dat", playerdata);
							CompressedStreamTools.writeCompressed(allData, file1);
							File file2 = new File(playerdata, player + ".dat");
							File file3 = new File(playerdata, player + ".dat_old");
							Util.safeReplaceFile(file2, file1, file3);
						} catch(Exception err)
						{
							err.printStackTrace();
						}
				};

				theReader = new Reader(save);
				theReader.deserializeNBT(data);
			}
			ForgeCaps.put(RaccEconomy.MOD_ID + ":data", data);
		}
		read.put("ForgeCaps", ForgeCaps);

		return theReader;
	}

	public static class Reader
			extends RaccEconomyDataImpl
	{
		final Consumer<Reader> save;

		public Reader(Consumer<Reader> save)
		{
			this.save = save;
		}

		@Override
		public void sync()
		{
			save.accept(this);
		}
	}
}