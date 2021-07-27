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

public class Pay
{
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(
				Commands.literal("pay")
						.then(Commands.argument("player", EntityArgument.player())
								.then(Commands.argument("amount", LongArgumentType.longArg(1L))
										.executes(cs ->
										{
											ServerPlayerEntity sender = cs.getSource().getPlayerOrException();
											ServerPlayerEntity receiver = EntityArgument.getPlayer(cs, "player");
											long amount = LongArgumentType.getLong(cs, "amount");

											if(sender.getUUID().equals(receiver.getUUID()))
											{
												cs.getSource().sendFailure(new TranslationTextComponent("command.pay.fail.self"));
												return 0;
											}

											IRaccPackData senderData = CapabilityRaccEconomy.get(sender);
											IRaccPackData receiverData = CapabilityRaccEconomy.get(receiver);

											if(senderData.tryTakeBalance(amount))
											{
												receiverData.addBalance(amount);
												cs.getSource().sendSuccess(new TranslationTextComponent("command.pay", NFU.balance2String(amount), sender.getName()), true);
												return 1;
											}

											cs.getSource().sendFailure(new TranslationTextComponent("command.pay.fail.not_enough", sender.getName()));
											return 0;
										})
								)
						)
		);
	}
}