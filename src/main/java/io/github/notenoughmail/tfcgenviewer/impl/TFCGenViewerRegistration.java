package io.github.notenoughmail.tfcgenviewer.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.notenoughmail.tfcgenviewer.TFCGenViewer;
import io.github.notenoughmail.tfcgenviewer.api.GenViewerAPI;
import io.github.notenoughmail.tfcgenviewer.api.color.Gradient;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IRegionVisualizerType;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.ITFCChunkVisualizerType;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IVisualizerType;
import io.github.notenoughmail.tfcgenviewer.impl.visualizers.chunk.*;
import io.github.notenoughmail.tfcgenviewer.impl.visualizers.region.*;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.DoubleToIntFunction;
import java.util.function.Supplier;

public class TFCGenViewerRegistration {

    public static void init(IEventBus modBus) {
        VISUALIZERS.register(modBus);
        GRADIENTS.register(modBus);
        DISPATCH_GRADIENTS.register(modBus);
    }

    private static final DeferredRegister<IVisualizerType<?, ?, ?, ?>> VISUALIZERS = DeferredRegister.create(GenViewerAPI.VISUALIZER_REGISTRY, TFCGenViewer.ID);
    private static final DeferredRegister<Gradient.Preset> GRADIENTS = DeferredRegister.create(GenViewerAPI.GRADIENT_REGISTRY, TFCGenViewer.ID);
    private static final DeferredRegister<MapCodec<? extends Gradient.Dispatch>> DISPATCH_GRADIENTS = DeferredRegister.create(GenViewerAPI.DISPATCH_GRADIENT_REGISTRY, TFCGenViewer.ID);

    public static <T> Component idName(ResourceKey<Registry<T>> regKey, Id<? extends T> viz) {
        return Component.translatable(TFCGenViewer.ID + "." + regKey.location().getPath().replace("/", ".") + "." + viz.id().getPath().replace("/", "."));
    }

    public static Component visualizerName(Id<? extends IVisualizerType<?, ?, ?, ?>> viz) {
        return idName(GenViewerAPI.VISUALIZER, viz);
    }

    public static Component visualizerDescription(Id<? extends IVisualizerType<?, ?, ?, ?>> viz) {
        return Component.translatable(TFCGenViewer.ID + "." + GenViewerAPI.VISUALIZER.location().getPath() + "." + viz.id().getPath().replace("/", ".") + ".description");
    }

    public static final Id<BiomeVisualizer> VIZ_BIOME = regionVisualizer("biome", BiomeVisualizer::new);
    public static final Id<RockTypeVisualizer> VIZ_ROCK_TYPE = regionVisualizer("rock_type", RockTypeVisualizer::new);
    public static final Id<RainfallVisualizer> VIZ_RAINFALL = regionVisualizer("rainfall", RainfallVisualizer::new);
    public static final Id<TemperatureVisualizer> VIZ_TEMPERATURE = regionVisualizer("temperature", TemperatureVisualizer::new);
    public static final Id<RockVisualizer> VIZ_ROCK = regionVisualizer("rock", RockVisualizer::new);
    public static final Id<KoppenVisualizer> VIZ_KOPPEN = regionVisualizer("koppen", KoppenVisualizer::new);
    public static final Id<BiomeAltitudeVisualizer> VIZ_BIOME_ALT = regionVisualizer("biome_altitude", BiomeAltitudeVisualizer::new);
    public static final Id<RiversAndMountainsVisualizer> VIZ_RIVERS_AND_MOUNTAINS = regionVisualizer("rivers_and_mountains", RiversAndMountainsVisualizer::new);
    public static final Id<ClimateRestrictedVisualizer> VIZ_CLIMATE_FEATURE = regionVisualizer("climate_restricted", ClimateRestrictedVisualizer::new);

    // TODO: 2.1.0 | See if any of these benefit from parallelization
    public static final Id<ChunkElevationVisualizer> VIZ_CHUNK_ELEVATION = chunkVisualizer("elevation", ChunkElevationVisualizer::new);
    public static final Id<ChunkBiomeVisualizer> VIZ_CHUNK_BIOME = chunkVisualizer("biome", ChunkBiomeVisualizer::new);
    public static final Id<ChunkKoppenVisualizer> VIZ_CHUNK_KOPPEN = chunkVisualizer("koppen", ChunkKoppenVisualizer::new);
    public static final Id<ChunkRainfallVisualizer> VIZ_CHUNK_RAINFALL = chunkVisualizer("rainfall", ChunkRainfallVisualizer::new);
    public static final Id<ChunkTemperatureVisualizer> VIZ_CHUNK_TEMPERATURE = chunkVisualizer("temperature", ChunkTemperatureVisualizer::new);
    public static final Id<ChunkRockVisualizer> VIZ_CHUNK_ROCK = chunkVisualizer("rock", ChunkRockVisualizer::new);
    public static final Id<ChunkClimateRestrictedVisualizer> VIZ_CHUNK_CLIMATE_FEATURE = chunkVisualizer("climate_restricted", ChunkClimateRestrictedVisualizer::new);

    public static final Id<Gradient.Preset> GRAD_BLUE = gradient("blue", Gradient.lin(0xFF963232, 0xFFFF8C64));
    public static final Id<Gradient.Preset> GRAD_DARK_BLUE = gradient("dark_blue", Gradient.lin(0xFF752222, 0xFFFF2200));
    public static final Id<Gradient.Preset> GRAD_GREEN = gradient("green", Gradient.lin(0xFF006400, 0xFF50C850));
    public static final Id<Gradient.Preset> GRAD_VOLCANIC = gradient("volcanic", d -> FastColor.ABGR32.color(
            0xFF,
            0x64,
            Gradient.delin(d * 0.1264363868D), // 0x64 linearized
            0xC8
    ));
    public static final Id<Gradient.Preset> GRAD_UPLIFT = gradient("uplift", d -> FastColor.ABGR32.color(
            0xFF,
            0xC8,
            Gradient.delin(d * 0.4607566240D), // 0xB4 linearized
            0xB4
    ));
    public static final Id<Gradient.Preset> GRAD_RAINFALL = gradient("rainfall", Gradient.multiLin(
            0xFF000287,
            0xFF0032FF,
            0xFF00A0FF,
            0xFF78E8FF,
            0xFF0FA00F,
            0xFFD26414,
            0xFFFAB978
    ));
    public static final Id<Gradient.Preset> GRAD_TEMPERATURE = gradient("temperature", Gradient.multiLin(
            0xFFFF1D00,
            0xFFFFBB00,
            0xFF94FF63,
            0xFF13FFE4,
            0xFF0079FF,
            0xFF0000D1
    ));
    public static final Id<Gradient.Preset> GRAD_GRAYSCALE = gradient("grayscale", d -> {
        final int c = Gradient.delin(d);
        return FastColor.ABGR32.color(0xFF, c, c, c);
    });

    public static final Id<MapCodec<HueWheel>> HUE_WHEEL = register(DISPATCH_GRADIENTS, "hue_wheel", () -> HueWheel.CODEC);

    private static <T extends IRegionVisualizerType<?, ?>> Id<T> regionVisualizer(String name, Supplier<T> supplier) {
        return visualizer("region/" + name, supplier);
    }

    private static <T extends ITFCChunkVisualizerType<?, ?>> Id<T> chunkVisualizer(String name, Supplier<T> supplier) {
        return visualizer("chunk/" + name, supplier);
    }

    private static <T extends IVisualizerType<?, ?, ?, ?>> Id<T> visualizer(String name, Supplier<T> supplier) {
        return register(VISUALIZERS, name, supplier);
    }

    private static Id<Gradient.Preset> gradient(String name, DoubleToIntFunction gradient) {
        return register(GRADIENTS, name, () -> gradient::applyAsInt);
    }

    private static <T, R extends T> Id<R> register(DeferredRegister<T> register, String name, Supplier<R> supplier) {
        final DeferredHolder<T, R> holder = register.register(name, supplier);
        return new Id<>(holder.getId(), holder);
    }

    public record Id<T>(ResourceLocation id, Supplier<T> val) implements Supplier<T> {
        @Override
        public T get() {
            return val.get();
        }
    }

    public record HueWheel(double offset, boolean reverse, float saturation, float value) implements Gradient.Dispatch {

        public static final MapCodec<HueWheel> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Codec.doubleRange(-1F, 1F).optionalFieldOf("offset", 0D).forGetter(HueWheel::offset),
                Codec.BOOL.optionalFieldOf("reverse", false).forGetter(HueWheel::reverse),
                Codec.floatRange(0F, 1F).fieldOf("saturation").forGetter(HueWheel::saturation),
                Codec.floatRange(0F, 1F).fieldOf("value").forGetter(HueWheel::value)
        ).apply(i, HueWheel::new));

        @Override
        public MapCodec<HueWheel> codec() {
            return CODEC;
        }

        @Override
        public int applyAsAbgr(double value) {
            final int rgb = applyAsArgb(value);
            return FastColor.ABGR32.color(
                    255,
                    FastColor.ARGB32.blue(rgb),
                    FastColor.ARGB32.green(rgb),
                    FastColor.ARGB32.red(rgb)
            );
        }

        @Override
        public int applyAsArgb(double h) {
            h += offset;
            h = Mth.positiveModulo(h, 1D);
            if (reverse) h = 1D - h;
            return Mth.hsvToArgb((float) h, saturation, value, 255);
        }
    }
}
