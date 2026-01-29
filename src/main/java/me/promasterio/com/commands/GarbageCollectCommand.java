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

public class GarbageCollectCommand extends Command {

    public GarbageCollectCommand() {
        super("garbagecollect", "gc");

        setDefaultExecutor(((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            System.gc();
            player.sendMessage("Garbage Collected Successfully!");
        }));
    }

}
