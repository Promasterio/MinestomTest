package me.promasterio.com;

import me.promasterio.com.commands.CommandInitializer;
import me.promasterio.com.data.SkinUtil;
import me.promasterio.com.events.InventoryEvents;
import me.promasterio.com.events.ServerListHandler;
import me.promasterio.com.items.Guns;
import me.promasterio.com.util.dev.ServerInfoBossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.*;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.AsyncPlayerPreLoginEvent;
import net.minestom.server.event.player.PlayerSkinInitEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.instance.generator.Generator;
import net.minestom.server.instance.generator.UnitModifier;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.world.DimensionType;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import static me.promasterio.com.util.OpenSimplex2S.noise2;

public class Server {
    private static final String authKey = "30daffa0-41c8-4acc-ba0c-db46c446eb43";
    private static final Potion HASTE_POTION = new Potion(PotionEffect.HASTE, 255, Potion.INFINITE_DURATION);
    private static final Potion MINING_FATIGUE_POTION = new Potion(PotionEffect.MINING_FATIGUE, 255, Potion.INFINITE_DURATION);
    private static InstanceContainer instance;

    public static InstanceContainer instance() {
        return instance;
    }

    static void main(String[] args) throws IOException {
        System.setProperty("minestom.chunk-view-distance", "32");
        System.setProperty("minestom.faster-socket-writes", "true");
        System.setProperty("minestom.new-socket-write-lock", "true");
        MinecraftServer server = MinecraftServer.init(new Auth.Velocity(authKey));
        MinecraftServer.setCompressionThreshold(0);

        new ServerInfoBossBar();

        instance = MinecraftServer.getInstanceManager().createInstanceContainer(DimensionType.OVERWORLD);

        instance.setChunkSupplier(LightingChunk::new);

        instance.setGenerator(new DevastedGenerator(Math.round(Math.random() * 9999), 2048));

        CommandInitializer.registerAll();
        SkinUtil.initializeSkins();

        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();

        InventoryEvents.InventoryStackHandler.hook(globalEventHandler);
        ServerListHandler.hook(globalEventHandler);
        Guns.hook(globalEventHandler);

        globalEventHandler.addListener(AsyncPlayerPreLoginEvent.class, event -> {
            if (!Whitelist.isEnabled()) return;
            UUID uuid = event.getGameProfile().uuid();
            if (!Whitelist.isPlayerWhitelisted(uuid)) {
                PlayerConnection connection = event.getConnection();
                connection.kick(Whitelist.KICK_MESSAGE);
            }
        });

        globalEventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            event.setSpawningInstance(instance);
            Player player = event.getPlayer();
            player.setRespawnPoint(new Pos(0, 50, 0));
            player.setGameMode(GameMode.SURVIVAL);
            player.setPermissionLevel(4);
        });
        globalEventHandler.addListener(PlayerSpawnEvent.class, event -> {
            Player player = event.getPlayer();
            player.getAttribute(Attribute.ATTACK_SPEED).setBaseValue(0.0);
            player.getAttribute(Attribute.CAMERA_DISTANCE).setBaseValue(0.0);
            player.addEffect(MINING_FATIGUE_POTION);
            player.addEffect(HASTE_POTION);
            player.setInvisible(true);
            Entity cow = new Entity(EntityType.COW);
            cow.setAutoViewable(false);
            cow.setInstance(instance, player.getPosition());
            cow.addViewer(player);
        });
        globalEventHandler.addListener(PlayerSkinInitEvent.class, event -> {
            event.setSkin(SkinUtil.getSkin("devaster"));
        });

        server.start("127.0.0.1", 25566);
        }

    public static class Whitelist {
        public static final Component KICK_MESSAGE = MiniMessage.miniMessage().deserialize("<red>You are not whitelisted on this server!");
        private static volatile boolean enabled;
        private static final Map<UUID, Player> whitelist = new ConcurrentHashMap<>();
        private Whitelist() {}

        public static void setEnabled(boolean whitelisted) {
            enabled = whitelisted;
        }

        public static boolean isEnabled() {
            return enabled;
        }

        public static void setWhitelisted(boolean whitelisted, Player... players) {
            if (whitelisted) {
                for (Player player : players) whitelist.putIfAbsent(player.getUuid(), player);
            } else {
                for (Player player : players) whitelist.remove(player.getUuid());
            }
        }

        public static boolean isPlayerWhitelisted(Player player) {
            return whitelist.containsKey(player.getUuid());
        }

        public static boolean isPlayerWhitelisted(UUID uuid) {
            return whitelist.containsKey(uuid);
        }

        public static Collection<Player> getWhitelist() {
            return whitelist.values();
        }
    }




    public static class DevastedGenerator implements Generator {
        private final long seed;
        private final double islandSize;

        // block Palettes
        private static final List<Block> beachBlocks = List.of(Block.SAND, Block.SAND, Block.SANDSTONE);
        private static final List<Block> overworldBlocks = List.of(Block.GRASS_BLOCK, Block.GRASS_BLOCK, Block.GRASS_BLOCK, Block.GRASS_BLOCK, Block.GREEN_CONCRETE_POWDER, Block.GREEN_CONCRETE_POWDER, Block.MOSS_BLOCK, Block.GREEN_WOOL);
        private static final List<Block> flowerBlocks = List.of(Block.POPPY, Block.DANDELION, Block.AZURE_BLUET, Block.OXEYE_DAISY);

        public DevastedGenerator(long seed, double islandSize) {
            this.seed = seed;
            this.islandSize = islandSize;
        }

        @Override
        public void generate(GenerationUnit unit) {
            Point start = unit.absoluteStart();
            Point size = unit.size();
            UnitModifier modifier = unit.modifier();
            int startX = start.blockX();
            int startZ = start.blockZ();
            int endX = startX + size.blockX();
            int endZ = startZ + size.blockZ();

            for (int x = startX; x < endX; x++) {
                if (Math.abs(x) > islandSize) continue;
                for (int z = startZ; z < endZ; z++) {
                    if (Math.abs(z) > islandSize) continue;
                    double halfSize = islandSize / 2;
                    double distance = Math.sqrt(x * x + z * z);
                    double distanceFactor = Math.max(0, 1 - distance / halfSize);

                    double terrainHeight = 0;
                    double amplitude = 0.1;
                    double frequency = 0.005;
                    double totalAmplitude = 0;

                    for (int i = 0; i < 4; i++) {
                        terrainHeight += noise2(seed, x * frequency, z * frequency) * amplitude;
                        totalAmplitude += amplitude;
                        amplitude /= 2;
                        frequency *= 2;
                    }
                    ThreadLocalRandom random = ThreadLocalRandom.current();
                    terrainHeight /= totalAmplitude;
                    double flatLandscapeHeight = noise2(seed + 1, x * 0.005, z * 0.005) * 0.5;
                    double blendFactor = Math.max(0, Math.min(1, (flatLandscapeHeight + 1) / 3));
                    double combinedHeight = terrainHeight * (1 - blendFactor) + flatLandscapeHeight * blendFactor;
                    double height = combinedHeight * 40 + 20;
                    height *= distanceFactor;
                    double sandTransitionFactor = Math.max(0, Math.min(1, (distance - (halfSize / 2)) / 100));
                    double sandNoise = noise2(seed + 2, x * 0.05, z * 0.05) * 0.5 + 0.5;
                    double sandProbability = sandTransitionFactor * 0.8 + sandNoise * 0.2;
                    int y = (int) Math.ceil(height);
                    for (int h = -64; h < y; h++) {
                        modifier.setBlock(x, h, z, Block.DIRT);
                    }

                    // water bodies
                    if (y <= 6) {
                        int waterStart = Math.max(y, -63);
                        for (int h = waterStart; h <= 6; h++) {
                            modifier.setBlock(x, h, z, Block.WATER);
                        }

                        // water body ground blocks
                        modifier.setBlock(x, y, z, getRandomBlock(beachBlocks, random));
                        continue;
                    }
                    // creates beaches
                    else if (sandProbability > 0.3) {
                        modifier.setBlock(x, y, z, getRandomBlock(beachBlocks, random));
                        continue;
                    }

                    modifier.setBlock(x, y, z, getRandomBlock(overworldBlocks, random));

                    int chance = random.nextInt(101); // 0 to 100

                    if (chance == 0) {
                        // flowers
                        modifier.setBlock(x, y + 1, z, getRandomBlock(flowerBlocks, random));
                    } else if (chance <= 6) {
                        // tall grass
                        modifier.setBlock(x, y + 1, z, Block.TALL_GRASS.withProperty("half", "lower"));
                        modifier.setBlock(x, y + 2, z, Block.TALL_GRASS.withProperty("half", "upper"));
                    } else if (chance >= 85) {
                        // short grass
                        modifier.setBlock(x, y + 1, z, Block.SHORT_GRASS);
                    }
                }
            }
        }

        private Block getRandomBlock(List<Block> list, ThreadLocalRandom random) {
            return list.get(random.nextInt(list.size()));
        }
    }
}