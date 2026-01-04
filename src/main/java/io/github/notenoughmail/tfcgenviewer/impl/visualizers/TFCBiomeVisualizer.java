package io.github.notenoughmail.tfcgenviewer.impl.visualizers;

import io.github.notenoughmail.tfcgenviewer.api.MutableImage;
import io.github.notenoughmail.tfcgenviewer.api.RegionGeneratorCache;
import io.github.notenoughmail.tfcgenviewer.api.scale.IScale;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.ITFCVisualizer;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.dries007.tfc.world.Seed;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.dries007.tfc.world.region.Region;
import net.dries007.tfc.world.region.RegionGenerator;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class TFCBiomeVisualizer implements ITFCVisualizer<RegionGeneratorCache> {

    public static final Component NAME = Component.translatable("tfcgenviewer.preview_world.visualizer_type.biomes");

    @Override
    public boolean isPermitted(ServerPlayer player) {
        return true;
    }

    @Override
    public RegionGeneratorCache createCache(RegistryAccess registryAccess, TFCChunkGenerator generator, IScale scale, long seed) {
        return new RegionGeneratorCache(new RegionGenerator(generator.settings(), Seed.of(seed)), scale);
    }

    @Override
    public void draw(int imageX, int imageY, MutableImage image, int xPos, int zPos, TFCChunkGenerator generator, RegistryAccess registryAccess, Int2ObjectOpenHashMap<Component> colorDescriptors, RegionGeneratorCache cache, IScale scale) {
        final Region.Point point = cache.getPoint(imageX, imageY, xPos, zPos);

    }

    @Override
    public Component colorKey(RegistryAccess registryAccess, RegionGeneratorCache cache) {
        return null;
    }

    @Override
    public Component name() {
        return NAME;
    }
}
