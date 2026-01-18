package me.promasterio.com.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.Argument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import static java.lang.String.join;

import java.util.List;

public class BroadcastCommand extends Command{

    public BroadcastCommand() {
        super("broadcast", "announce");

        setDefaultExecutor(((sender, context) -> {
            sender.sendMessage("You ran /broadcast");
        }));
        var textArg = ArgumentType.StringArray("message");

        addSyntax((sender, context) -> {
            final String[] messageArray = context.get("message");
            final String message = join(" ", messageArray);
            Audiences.players().sendMessage(MiniMessage.miniMessage().deserialize(message));
        }, textArg);
    }
}
