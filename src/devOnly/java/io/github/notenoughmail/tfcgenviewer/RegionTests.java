package io.github.notenoughmail.tfcgenviewer;

import net.dries007.tfc.common.blocks.SandstoneBlockType;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.dries007.tfc.common.blocks.soil.SandBlockType;
import net.dries007.tfc.world.Seed;
import net.dries007.tfc.world.region.Region;
import net.dries007.tfc.world.region.RegionGenerator;
import net.dries007.tfc.world.settings.RockLayerSettings;
import net.dries007.tfc.world.settings.RockSettings;
import net.dries007.tfc.world.settings.Settings;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.neoforged.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.dries007.tfc.common.blocks.rock.Rock.*;

public class RegionTests {

    public static void detectCacheCollisions() {
        final int count = 10_000;
        TFCGenViewer.LOGGER.warn("Detecting cache collisions for {} seeds", count);
        record Instance(int num, long seed, int gridX, int gridZ, Region region0, Region region1) {
            static boolean test(RegionGenerator generator, Region iReg, int gridX, int gridZ, int ox, int oz, long seed, int num, List<Instance> feedback) {
                final Region oReg = generator.getOrCreateRegion(gridX + ox, gridZ + oz);
                if (iReg != generator.getOrCreateRegion(gridX, gridZ)) {
                    // There has been a cache collision, overwriting the region in the FastConcurrentCache
                    final Instance inst = new Instance(num, seed, gridX, gridZ, iReg, oReg);
                    TFCGenViewer.LOGGER.warn("{}", inst);
                    feedback.add(inst);
                    return true;
                }
                return false;
            }
        }
        final List<Instance> badSeeds = new ArrayList<>();
        top:
        for (int i = 0 ; i < count ; i++) {
            final long seed = WorldOptions.randomSeed();
            final RegionGenerator regionGenerator = new RegionGenerator(SETTINGS, Seed.of(seed));
            for (int x = -128 ; x < 128 ; x++) {
                for (int z = -128 ; z < 128 ; z++) {
                    final Region iReg = regionGenerator.getOrCreateRegion(x, z);
                    if (
                            Instance.test(regionGenerator, iReg, x, z, 1, 0, seed, i, badSeeds) ||
                            Instance.test(regionGenerator, iReg, x, z, 0, 1, seed, i, badSeeds) ||
                            Instance.test(regionGenerator, iReg, x, z, 1, 1, seed, i, badSeeds)
                    ) {
                        continue top;
                    }
                }
            }
        }
        if (!badSeeds.isEmpty()) {
            TFCGenViewer.LOGGER.error("Encountered {} seeds ({}%) with cache collision problems", badSeeds.size(), badSeeds.size() * 100 / count);
            try (final FileWriter writer = new FileWriter(new File(FMLPaths.getOrCreateGameRelativePath(Path.of("tfcgv_export")).toFile(), "export_%s.csv".formatted(count)))) {
                writer.append("num,seed,gridX,gridZ\n");
                for (Instance i : badSeeds) {
                    writer.append(String.valueOf(i.num()))
                            .append(",")
                            .append(String.valueOf(i.seed()))
                            .append(",")
                            .append(String.valueOf(i.gridX()))
                            .append(",")
                            .append(String.valueOf(i.gridZ()))
                            .append("\n");
                }
            } catch (Exception e) {
                TFCGenViewer.LOGGER.warn("Error encountered while performing csv write", e);
            }
        } else {
            TFCGenViewer.LOGGER.info("Encountered no cache collisions");
        }
    }

    private static final String BOTTOM = "bottom";
    private static final String IGNEOUS_EXTRUSIVE = "igneous_extrusive";
    private static final String IGNEOUS_EXTRUSIVE_X2 = "igneous_extrusive_x2";
    private static final String SEDIMENTARY = "sedimentary";
    private static final String UPLIFT = "uplift";
    private static final String FELSIC = "felsic";
    private static final String INTERMEDIATE = "intermediate";
    private static final String MAFIC = "mafic";
    private static final String MM_LOW_GRADE = "low_grade";
    private static final String MM_HIGH_GRADE = "high_grade";
    private static final String MM_MARBLE = "marble";
    private static final String MM_QUARTZITE = "quartzite";


    private static final RockLayerSettings ROCK_SETTINGS = RockLayerSettings.decode(new RockLayerSettings.Data(
            Arrays.stream(Rock.values()).collect(Collectors.toMap(
                    Rock::getSerializedName,
                    rock -> {
                        final var b = TFCBlocks.ROCK_BLOCKS.get(rock);
                        return new RockSettings(
                                b.get(BlockType.RAW).get(),
                                b.get(BlockType.HARDENED).get(),
                                b.get(BlockType.GRAVEL).get(),
                                b.get(BlockType.COBBLE).get(),
                                TFCBlocks.SAND.get(SandBlockType.BROWN).get(),
                                TFCBlocks.SANDSTONE.get(SandBlockType.BROWN).get(SandstoneBlockType.RAW).get(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty()
                        );
                    }
            )),
            Stream.of(GNEISS, SCHIST, DIORITE, GRANITE, GABBRO).map(Rock::getSerializedName).toList(),
            List.of(
                    layerOf(FELSIC, Map.of(GRANITE, BOTTOM)),
                    layerOf(INTERMEDIATE, Map.of(DIORITE, BOTTOM)),
                    layerOf(MAFIC, Map.of(GABBRO, BOTTOM)),
                    layerOf(IGNEOUS_EXTRUSIVE, Map.of(
                            RHYOLITE, FELSIC,
                            ANDESITE, INTERMEDIATE,
                            DACITE, INTERMEDIATE,
                            BASALT, MAFIC
                    )),
                    layerOf(IGNEOUS_EXTRUSIVE_X2, Map.of(
                            RHYOLITE, IGNEOUS_EXTRUSIVE,
                            ANDESITE, IGNEOUS_EXTRUSIVE,
                            DACITE, IGNEOUS_EXTRUSIVE,
                            BASALT, IGNEOUS_EXTRUSIVE
                    )),
                    layerOf(MM_HIGH_GRADE, Map.of(
                            SCHIST, BOTTOM,
                            GNEISS, BOTTOM
                    )),
                    layerOf(MM_LOW_GRADE, Map.of(
                            PHYLLITE, MM_HIGH_GRADE,
                            SLATE, MM_HIGH_GRADE
                    )),
                    layerOf(MM_MARBLE, Map.of(MARBLE, BOTTOM)),
                    layerOf(MM_QUARTZITE, Map.of(QUARTZITE, BOTTOM)),
                    layerOf(SEDIMENTARY, Map.of(
                            SHALE, MM_LOW_GRADE,
                            CLAYSTONE, MM_LOW_GRADE,
                            CONGLOMERATE, MM_LOW_GRADE,
                            LIMESTONE, MM_MARBLE,
                            DOLOMITE, MM_MARBLE,
                            CHALK, MM_MARBLE,
                            CHERT, MM_QUARTZITE
                    )),
                    layerOf(UPLIFT, Map.of(
                            SLATE, MM_HIGH_GRADE,
                            PHYLLITE, MM_HIGH_GRADE,
                            MARBLE, BOTTOM,
                            QUARTZITE, BOTTOM,
                            DIORITE, MM_LOW_GRADE,
                            GRANITE, MM_LOW_GRADE,
                            GABBRO, MM_LOW_GRADE
                    ))
            ),
            List.of(IGNEOUS_EXTRUSIVE),
            List.of(IGNEOUS_EXTRUSIVE, SEDIMENTARY),
            List.of(IGNEOUS_EXTRUSIVE, IGNEOUS_EXTRUSIVE_X2),
            List.of(SEDIMENTARY, UPLIFT)
    )).getOrThrow();
    private static final Settings SETTINGS = new Settings(false, 4_000, 0, 0, 20_000, 0, 20_000, 0, ROCK_SETTINGS, 0.5f, 0.5f, false);

    private static RockLayerSettings.LayerData layerOf(String layer, Map<Rock, String> layers) {
        return new RockLayerSettings.LayerData(layer, TFCGenViewer.mapFromEntries(layers.entrySet().stream().map(e -> Map.entry(e.getKey().getSerializedName(), e.getValue())), null));
    }
}
