package me.promasterio.com.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;

import java.util.List;

public class CommandInitializer {

    private static final List<Command> commands = List.of(
            new BroadcastCommand()
    );

    public static void registerAll() {
        var commandManager = MinecraftServer.getCommandManager();
        commands.forEach(commandManager::register);
    }
}
