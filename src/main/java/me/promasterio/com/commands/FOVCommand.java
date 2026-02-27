package me.promasterio.com.commands;

import me.promasterio.com.util.camera.CameraManager;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;

public class FOVCommand extends Command{

    public FOVCommand() {
        super("zoom", "fov");

        var textArg = ArgumentType.Float("zoom");

        addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            final float zoom = context.get("zoom");
            CameraManager.get(player).setZoom(zoom);
        }, textArg);
    }
}