package me.promasterio.com.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.Argument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.inventory.InventoryClickEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import static java.lang.String.join;

import java.util.List;
import net.minestom.server.tag.Tag;

public class GiveCommand extends Command {

    public static final Tag<Integer> MAX_STACK_SIZE = Tag.Integer("max_stack_size").defaultValue(0);

    public GiveCommand() {
        super("give");

        var amountArg = ArgumentType.Integer("amount");
        var itemArg = ArgumentType.ItemStack("item");

        addSyntax(((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            int amount = context.get("amount");
            ItemStack item = ((ItemStack) context.get("item")).withMaxStackSize(99).withTag(MAX_STACK_SIZE, 999).withAmount(amount);

            player.getInventory().setItemStack(player.getHeldSlot(), item);
            player.sendMessage("§aSuccesfuly obtained " + amount + " of " + item.material());
        }), amountArg, itemArg);
    }

}
