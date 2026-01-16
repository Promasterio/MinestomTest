package me.promasterio.com;

import me.promasterio.com.util.OpenSimplex2S;
import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.instance.generator.Generator;
import net.minestom.server.world.DimensionType;

import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Server {
    public static void main(String[] args) {
        System.setProperty("minestom.chunk-view-distance", "64");
        MinecraftServer server = MinecraftServer.init(new Auth.Online());
        new ServerInfoBossBar();
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        InstanceContainer instance = instanceManager.createInstanceContainer(DimensionType.OVERWORLD);

        instance.setChunkSupplier(LightingChunk::new);

        // Set the custom generator using the seed and island size from your script
        // Seed: 69, IslandSize: 2000 (Example, adjust as needed)
        instance.setGenerator(new DevastedGenerator(0, 2048));

        MinecraftServer.getGlobalEventHandler()
                .addListener(AsyncPlayerConfigurationEvent.class, event -> {
                    event.setSpawningInstance(instance);
                    Player player = event.getPlayer();
                    player.setRespawnPoint(new Pos(0, 50, 0));
                    player.setGameMode(GameMode.CREATIVE);
                    player.setPermissionLevel(4);
                });

        server.start("127.0.0.1", 25565);
    }


    public static class DevastedGenerator implements Generator {

        private final long seed;
        private final double islandSize;

        // Block Palettes
        private final List<Block> beachBlocks = List.of(Block.SAND, Block.SAND, Block.SANDSTONE);
        private final List<Block> overworldBlocks = List.of(Block.GRASS_BLOCK, Block.GRASS_BLOCK, Block.GRASS_BLOCK, Block.GRASS_BLOCK, Block.GREEN_CONCRETE_POWDER, Block.GREEN_CONCRETE_POWDER, Block.MOSS_BLOCK, Block.GREEN_WOOL);
        private final List<Block> flowerBlocks = List.of(Block.POPPY, Block.DANDELION, Block.AZURE_BLUET, Block.OXEYE_DAISY);

        public DevastedGenerator(long seed, double islandSize) {
            this.seed = seed;
            this.islandSize = islandSize;
        }

        @Override
        public void generate(GenerationUnit unit) {
            Point start = unit.absoluteStart();
            Point size = unit.size();

            var modifier = unit.modifier();

            int startX = start.blockX();
            int startZ = start.blockZ();
            int endX = startX + size.blockX();
            int endZ = startZ + size.blockZ();

            // Iterate through X and Z columns
            for (int x = startX; x < endX; x++) {
                for (int z = startZ; z < endZ; z++) {

                    // Island Boundary Check
                    if (Math.abs(x) > islandSize || Math.abs(z) > islandSize) {
                        continue;
                    }

                    // Call the custom noise function
                    List<Double> terrainInfo = OpenSimplex2S.terraingen(seed, islandSize, x, z);

                    // index 0 is Y level, index 1 is Sand Probability
                    double yRaw = terrainInfo.get(0);
                    double sandProbability = terrainInfo.get(1);
                    int y = (int) Math.ceil(yRaw);

                    // 1. Fill stone/dirt from bottom (-64) up to y (exclusive)
                    for (int h = -64; h < y; h++) {
                        modifier.setBlock(x, h, z, Block.DIRT);
                    }

                    ThreadLocalRandom random = ThreadLocalRandom.current();

                    // WATER / BEACH LOGIC (if ceil(y) <= 6)
                    if (y <= 6) {
                        // Fill water from current ground level up to 6 (inclusive)
                        // Skript: clamp({_y},6,-63) to 6
                        int waterStart = Math.max(y, -63);
                        for (int h = waterStart; h <= 6; h++) {
                            modifier.setBlock(x, h, z, Block.WATER);
                        }

                        // Set ground block (underwater/beach floor)
                        modifier.setBlock(x, y, z, getRandomBlock(beachBlocks, random));
                        continue;
                    }
                    // HIGH SAND PROBABILITY LOGIC
                    else if (sandProbability > 0.3) {
                        modifier.setBlock(x, y, z, getRandomBlock(beachBlocks, random));
                        continue;
                    }

                    // OVERWORLD LAND LOGIC
                    modifier.setBlock(x, y, z, getRandomBlock(overworldBlocks, random));

                    // VEGETATION
                    int chance = random.nextInt(101); // 0 to 100

                    if (chance == 0) {
                        // Flowers
                        modifier.setBlock(x, y + 1, z, getRandomBlock(flowerBlocks, random));
                    } else if (chance >= 1 && chance <= 6) {
                        // Tall Grass (Double plant)
                        modifier.setBlock(x, y + 1, z, Block.TALL_GRASS.withProperty("half", "lower"));
                        modifier.setBlock(x, y + 2, z, Block.TALL_GRASS.withProperty("half", "upper"));
                    } else if (chance >= 85) {
                        // Short Grass
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