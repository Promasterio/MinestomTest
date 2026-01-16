package me.promasterio.com;

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
import net.minestom.server.instance.generator.UnitModifier;
import net.minestom.server.world.DimensionType;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static me.promasterio.com.util.OpenSimplex2S.noise2;

public class Server {
    public static void main(String[] args) {
        System.setProperty("minestom.chunk-view-distance", "32");
        MinecraftServer server = MinecraftServer.init(new Auth.Online());
        new ServerInfoBossBar();
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        InstanceContainer instance = instanceManager.createInstanceContainer(DimensionType.OVERWORLD);

        instance.setChunkSupplier(LightingChunk::new);

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

        // block Palettes
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
                    // fill with dirt
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
                    } else if (chance >= 1 && chance <= 6) {
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