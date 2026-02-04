package me.promasterio.com.items;

import me.promasterio.com.events.GunEvents;
import me.promasterio.com.util.ComponentUtil;
import me.promasterio.com.util.SoundsUtil;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.inventory.InventoryItemChangeEvent;
import net.minestom.server.event.player.*;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.PlayerRotationPacket;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;
import net.minestom.server.timer.TaskSchedule;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.LockSupport;

import static net.kyori.adventure.sound.Sound.sound;

public class Guns {

    public static final Tag<String> GUN_TYPE = Tag.String("guns:gun_type");
    public static final Tag<UUID> GUN_ID = Tag.UUID("guns:gun_id");
    public static final Tag<Integer> AMMO = Tag.Integer("guns:gun_ammo").defaultValue(0);
    public static final Tag<Boolean> HAS_GUN_EQUIPPED = Tag.Boolean("guns:has_gun_equipped");
    private static final GlobalEventHandler EVENT_HANDLER = MinecraftServer.getGlobalEventHandler();

    public static void hook(GlobalEventHandler node) {
        node.addListener(PlayerUseItemEvent.class, (event) -> {
            Player player = event.getPlayer();
            ItemStack gun = player.getItemInMainHand();
            if (gun.getTag(GUN_TYPE) == null) return;
            EVENT_HANDLER.call(new GunEvents.GunShootEvent(player, gun));
        });

        node.addListener(PlayerChangeHeldSlotEvent.class, event -> {
            Player player = event.getPlayer();

            ItemStack prev = event.getItemInOldSlot();
            ItemStack next = event.getItemInNewSlot();

            callGunChangeEvent(player, prev, next);
        });

        node.addListener(InventoryItemChangeEvent.class, event -> {
            if (!(event.getInventory() instanceof PlayerInventory inv)) return;

            Optional<Player> opt = inv.getViewers().stream().findFirst();
            if (opt.isEmpty()) return;

            Player player = opt.get();

            int heldSlot = player.getHeldSlot();
            if (event.getSlot() != heldSlot) return;

            ItemStack prevItem = event.getPreviousItem();
            ItemStack newItem = event.getNewItem();

            callGunChangeEvent(player, prevItem, newItem);
        });

        node.addListener(PlayerStartSneakingEvent.class, (event) -> {
            Player player = event.getPlayer();
            if (!isHoldingGun(player)) return;
            EVENT_HANDLER.call(new GunEvents.GunStartAimingEvent(player));
        });

        node.addListener(PlayerStopSneakingEvent.class, (event) -> {
            Player player = event.getPlayer();
            if (!isHoldingGun(player)) return;
            EVENT_HANDLER.call(new GunEvents.GunStopAimingEvent(player));
        });

        node.addListener(PlayerSwapItemEvent.class, (event) -> {
            Player player = event.getPlayer();
            ItemStack gun = player.getItemInMainHand();
            if (gun.getTag(GUN_TYPE) == null) return;
            event.setCancelled(true);
            EVENT_HANDLER.call(new GunEvents.GunReloadEvent(player, gun));
        });
        node.addListener(GunEvents.GunShootEvent.class, Guns::handleGunShot);
        node.addListener(GunEvents.GunEquipEvent.class, Guns::handleGunEquip);
        node.addListener(GunEvents.GunUnequipEvent.class, Guns::handleGunUnequip);
        node.addListener(GunEvents.GunReloadEvent.class, Guns::handleGunReload);

        MinecraftServer.getCommandManager().register(new GiveGun());
    }

    public static boolean isHoldingGun(Player player) {
        return player.getItemInMainHand().getTag(GUN_ID) != null;
    }

    private static void handleGunReload(GunEvents.GunReloadEvent event) {
        Player player = event.getPlayer();
        player.sendMessage("YOU ARE TRYING TO RELOAD UR GUN!");
    }

    private static void callGunChangeEvent(Player player, ItemStack prevItem, ItemStack newItem) {
        UUID oldGunId = prevItem.getTag(GUN_ID);
        UUID newGunId = newItem.getTag(GUN_ID);

        if (oldGunId != null && !oldGunId.equals(newGunId)) EVENT_HANDLER.call(new GunEvents.GunUnequipEvent(player, prevItem));
        if (newGunId != null && !newGunId.equals(oldGunId)) EVENT_HANDLER.call(new GunEvents.GunEquipEvent(player, newItem));
    }

    private static void handleGunEquip(GunEvents.GunEquipEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        ItemStack gun = event.getGun();
        player.sendMessage(ComponentUtil.deserialize("<rainbow>OMG YOU EQUIPPED ZE GUN"));
        player.setTag(HAS_GUN_EQUIPPED, true);
        UUID currentGun = gun.getTag(GUN_ID);
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            if (!player.isOnline() || gun.getTag(GUN_ID) != currentGun) return TaskSchedule.stop();

            player.sendActionBar(ComponentUtil.deserialize("<gray>" + player.getItemInMainHand().getTag(AMMO) + " <dark_gray>/ <grey>30"));

            return TaskSchedule.tick(1);
        });
    }

    private static void handleGunUnequip(GunEvents.GunUnequipEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        ItemStack gun = event.getGun();
        player.sendMessage(ComponentUtil.deserialize("<red>BOOO YOU UNEQUIPPED DA GUN"));
        player.setTag(HAS_GUN_EQUIPPED, false);
    }

    private static void handleGunShot(GunEvents.GunShootEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        ItemStack gun = event.getGun();
        int ammo = gun.getTag(AMMO);
        if (ammo <= 0) {
            event.setCancelled(true);
            SoundsUtil.playAtPlayer(player, SoundEvent.UI_BUTTON_CLICK, 2.0f, 2.0f);
            return;
        }
        var inventory = player.getInventory();
        inventory.setItemStack(player.getHeldSlot(), gun.withTag(AMMO, ammo - 1));
        SoundsUtil.playAtPlayer(player, SoundEvent.ENTITY_FIREWORK_ROCKET_BLAST, 2.0f, 1.0f);
        GunCamera.applyRecoil(player, 0.0f, -7.5f, 0.05);
        // wait 2 ticks
        MinecraftServer.getSchedulerManager().scheduleTask(() ->
                    GunCamera.applyRecoil(player, 0.0f, 7.5f, 0.2),
                TaskSchedule.tick(1),
                TaskSchedule.stop()
        );
    }

    private static void handleGunStartAiming(GunEvents.GunStartAimingEvent event) {
        Player player = event.getPlayer();

    }

    public static class GiveGun extends Command {

        public GiveGun() {
            super("givegun");

            setDefaultExecutor((sender, context) -> {
                if (!(sender instanceof Player player)) return;

                ItemStack gun = ItemStack.of(Material.IRON_HORSE_ARMOR)
                        .withCustomName(ComponentUtil.nonItalic("<gradient:#515151:#777777:#838383>Clock 7"))
                        .withTag(GUN_TYPE, "basic")
                        .withTag(GUN_ID, UUID.randomUUID())
                        .withTag(AMMO, 30);

                player.getInventory().addItemStack(gun);
                player.sendMessage("Gun given");
            });
        }
    }

    // Requires -Dminestom.faster-socket-writes=true
    public static class GunCamera {

        private double currentYawOffset = 0;
        private double currentPitchOffset = 0;

        public static void applyRecoil(Player player, float yawDelta, float pitchDelta, double timeSeconds) {
            int packetsPerSecond = Math.max(1, (int) (20 + 124 * Math.pow(0.993, player.getLatency())));
            int frames = Math.max(1, (int) (packetsPerSecond * timeSeconds));
            long periodNs = (long) ((timeSeconds * 1_000_000_000.0) / frames);
            float yawStep = yawDelta / frames;
            float pitchStep = pitchDelta / frames;
            ServerPacket packet = new PlayerRotationPacket(yawStep, true, pitchStep, true);

            startThread("Recoil-" + player.getUsername(), () -> {
                for (int i = 0; i < frames; i++) {
                    player.sendPacket(packet);
                    LockSupport.parkNanos(periodNs);
                }
            });
        }

        private static void startThread(String name, Runnable runnable) {
            Thread.ofVirtual().name(name).start(runnable);
        }

        public void resetCamera(Player player, double durationSeconds) {
            applyRecoil(player, (float) -currentYawOffset, (float) -currentPitchOffset, durationSeconds);
            currentYawOffset = 0;
            currentPitchOffset = 0;
        }

        public double getCurrentYawOffset() {
            return currentYawOffset;
        }

        public double getCurrentPitchOffset() {
            return currentPitchOffset;
        }
    }
}


