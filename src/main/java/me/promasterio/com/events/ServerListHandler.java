package me.promasterio.com.events;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.ping.Status;
import net.minestom.server.ping.Status.PlayerInfo;
import net.minestom.server.ping.Status.VersionInfo;
import net.minestom.server.utils.identity.NamedAndIdentified;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static me.promasterio.com.Server.Whitelist;

public class    ServerListHandler {

    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final int MAX_PLAYERS = 100;
    private static final byte[] faviconBytes;
    private static final List<NamedAndIdentified> HOVER = List.of(
            NamedAndIdentified.named("             §e§kWWW     §6§lDevasted.tensorbyte.net     §e§kWWW"),
            NamedAndIdentified.named("                                    §7§oAlpha 1.0"),
            NamedAndIdentified.named(""),
            NamedAndIdentified.named("   §e███████████████"),
            NamedAndIdentified.named("   §e████§0█§e████§0█§e█████  §7Server presented by:"),
            NamedAndIdentified.named("   §e███§0███§e███§0███§e███  §bTensorByte Network"),
            NamedAndIdentified.named("   §e██§0████§e███§0████§e██"),
            NamedAndIdentified.named("   §e█§0█████§e███§0█████§e█  §6Build§7, §aCraft §7and §dTeam up"),
            NamedAndIdentified.named("   §e██§0████§e███§0████§e██  §7To survive §cThe Undead§7!"),
            NamedAndIdentified.named("   §e███████§0█§e███████"),
            NamedAndIdentified.named("   §e███████████████  §eVersion: §31.21+"),
            NamedAndIdentified.named("   §e██████§0███§e██████"),
            NamedAndIdentified.named("   §e██████§0███§e██████  §2§nJoin now!"),
            NamedAndIdentified.named("   §e█████§0█████§e█████"),
            NamedAndIdentified.named("   §e█████§0█████§e█████  §9Discord: §b§ndiscord.gg/c6mVHAFD4x§r   "),
            NamedAndIdentified.named("   §e███████████████"),
            NamedAndIdentified.named("")
    );
    static {
        try {
            faviconBytes = Files.readAllBytes(Path.of("assets/server-icon.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void hook(GlobalEventHandler handler) {
        handler.addListener(ServerListPingEvent.class, event -> {
            int online = MinecraftServer.getConnectionManager().getOnlinePlayerCount();
            boolean whitelisted = Whitelist.isEnabled();
            String whitelistText = whitelisted
                    ? "<red>ᴡʜɪᴛᴇʟɪsᴛᴇᴅ"
                    : "<green>ᴘʟᴀʏ ɴᴏᴡ";

            Component motd = MINI.deserialize("""
<gold>■<yellow>■<gold>■<yellow>■<gold>■<yellow>■<gold>■<yellow>■<gold>■ <yellow><bold>DEVASTED</bold></yellow><gold>.ᴛᴇɴsᴏʀʙʏᴛᴇ.ɴᴇᴛ <gold>■<yellow>■<gold>■<yellow>■<gold>■<yellow>■<gold>■<yellow>■<gold>■
<dark_gray>v0.9.5</dark_gray> <#FFFF00>☢ <bold><#FFFF00>O<#E6FF14>P<#CCFF29>E<#B3FF3D>N <#80FF66>B<#66FF7A>E<#4DFF8F>T<#33FFA3>A <#00FFCC>H<#1AFFB8>A<#33FFA3>S <#66FF7A>A<#80FF66>R<#99FF52>R<#B3FF3D>I<#CCFF29>V<#E6FF14>E<#FFFF00>D</bold> <#FFFF00>☢ %s
""".formatted(whitelistText));
            Status status = Status.builder()
                    .description(motd)
                    .favicon(faviconBytes)
                    //.versionInfo(new VersionInfo("                                                 §9ᴠᴇʀsɪᴏɴ 1.21.10+                                                               §b" + online + "§3/§b" + MAX_PLAYERS, 1))
                    .playerInfo(new PlayerInfo(online, MAX_PLAYERS, HOVER))
                    .build();

            event.setStatus(status);
        });
    }
}
