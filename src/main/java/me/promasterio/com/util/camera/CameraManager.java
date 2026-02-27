package me.promasterio.com.util.camera;

import net.minestom.server.entity.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CameraManager {
    private static final Map<Player, PlayerCamera> cameras = new ConcurrentHashMap<>();

    public static PlayerCamera get(Player player) {
        return cameras.get(player);
    }

    public static void create(Player player) {
        cameras.put(player, new PlayerCamera(player));
    }

    public static void remove(Player player) {
        cameras.remove(player);
    }
}
