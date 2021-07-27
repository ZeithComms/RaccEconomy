package org.zeith.comms.c17racceconomy.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;
import org.zeith.comms.c17racceconomy.api.CapabilityRaccEconomy;
import org.zeith.comms.c17racceconomy.api.IRaccPackData;
import org.zeith.comms.c17racceconomy.utils.NFU;

public class AdminPay
{
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(
				Commands.literal("adminpay")
						.requires(cs -> cs.hasPermission(2))
						.then(Commands.argument("player", EntityArgument.player())
								.then(Commands.argument("amount", LongArgumentType.longArg())
										.executes(cs ->
										{
											ServerPlayerEntity receiver = EntityArgument.getPlayer(cs, "player");
											IRaccPackData receiverData = CapabilityRaccEconomy.get(receiver);

											long amount = LongArgumentType.getLong(cs, "amount");

											if(amount > 0L)
											{
												receiverData.addBalance(amount);
												cs.getSource().sendSuccess(new TranslationTextComponent("command.adminpay.add", NFU.balance2String(amount), receiver.getName(), receiverData.getBalanceText()), true);
												return 1;
											}

											if(amount < 0L)
											{
												long taken = receiverData.takeBalance(-amount);
												cs.getSource().sendSuccess(new TranslationTextComponent("command.adminpay.take", NFU.balance2String(taken), receiver.getName(), receiverData.getBalanceText()), true);
												return 1;
											}

											return 0;
										})
								)
						)
		);
	}
}