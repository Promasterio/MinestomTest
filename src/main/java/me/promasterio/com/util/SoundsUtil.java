package me.promasterio.com.util;

import me.promasterio.com.Server;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.Player;
import net.minestom.server.sound.SoundEvent;

import static net.kyori.adventure.sound.Sound.sound;

public final class SoundsUtil {
    private SoundsUtil() {}

    public static void playAtPlayer(Player player, SoundEvent event, float volume, float pitch) {
        Server.instance().playSound(sound(event, Sound.Source.PLAYER, volume, pitch), player.getPosition().add(0.0, 0.9, 0.0));
    }

}
