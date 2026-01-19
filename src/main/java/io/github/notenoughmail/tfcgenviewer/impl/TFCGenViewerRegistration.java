package io.github.notenoughmail.tfcgenviewer.impl;

import io.github.notenoughmail.tfcgenviewer.TFCGenViewer;
import io.github.notenoughmail.tfcgenviewer.api.GenViewerAPI;
import io.github.notenoughmail.tfcgenviewer.api.color.Gradient;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IRegionVisualizerType;
import io.github.notenoughmail.tfcgenviewer.impl.visualizers.region.*;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.DoubleToIntFunction;
import java.util.function.Supplier;

public class TFCGenViewerRegistration {

    public static void init(IEventBus modBus) {
        REGION_VISUALIZERS.register(modBus);
        GRADIENTS.register(modBus);
    }

    private static final DeferredRegister<IRegionVisualizerType<?, ?>> REGION_VISUALIZERS = DeferredRegister.create(GenViewerAPI.TFC_REGION_VISUALIZER_REGISTRY, TFCGenViewer.ID);
    private static final DeferredRegister<Gradient.Preset> GRADIENTS = DeferredRegister.create(GenViewerAPI.GRADIENT_REGISTRY, TFCGenViewer.ID);

    public static <T> Component visualizerName(ResourceKey<Registry<T>> regKey, Id<? extends T> viz) {
        return Component.translatable(TFCGenViewer.ID + "." + regKey.location().getPath().replace("/", ".") + "." + viz.id().getPath().replace("/", "."));
    }

    public static Component regionVisualizerName(Id<? extends IRegionVisualizerType<?, ?>> viz) {
        return visualizerName(GenViewerAPI.TFC_REGION_VISUALIZER, viz);
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

    public static final Id<Gradient.Preset> GRAD_BLUE = gradient("blue", Gradient.lin(0xFF963232, 0xFFFF8C64));
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

    private static <T extends IRegionVisualizerType<?, ?>> Id<T> regionVisualizer(String name, Supplier<T> supplier) {
        return register(REGION_VISUALIZERS, name, supplier);
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
}
