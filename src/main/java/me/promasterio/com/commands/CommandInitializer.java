package me.promasterio.com.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;

import java.util.List;

public class CommandInitializer {

    public static void registerAll() {
        var commandManager = MinecraftServer.getCommandManager();

        commandManager.register(new BroadcastCommand());
        commandManager.register(new GiveCommand());
        commandManager.register(new GarbageCollectCommand());
    }
}
