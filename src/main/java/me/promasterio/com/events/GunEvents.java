package me.promasterio.com.events;

import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;

public class GunEvents {
    public static final class GunShootEvent extends CancellableEvent {
        private final Player player;
        private final ItemStack gun;

        public GunShootEvent(Player player, ItemStack gun) {
            this.player = player;
            this.gun = gun;
        }

        public Player getPlayer() { return player; }
        public ItemStack getGun() { return gun; }
    }

    public static final class GunReloadEvent extends CancellableEvent {
        private final Player player;
        private final ItemStack gun;

        public GunReloadEvent(Player player, ItemStack gun) {
            this.player = player;
            this.gun = gun;
        }

        public Player getPlayer() { return player; }
        public ItemStack getGun() { return gun; }
    }

    public static final class GunStartAimingEvent extends CancellableEvent {
        private final Player player;

        public GunStartAimingEvent(Player player) {
            this.player = player;
        }

        public Player getPlayer() { return player; }
    }

    public static final class GunStopAimingEvent extends CancellableEvent {
        private final Player player;

        public GunStopAimingEvent(Player player) {
            this.player = player;
        }

        public Player getPlayer() { return player; }
    }
}
