package io.github.notenoughmail.tfcgenviewer.color;

import io.github.notenoughmail.tfcgenviewer.DataManagerProvider;
import io.github.notenoughmail.tfcgenviewer.api.color.ColorGradientDefinition;
import io.github.notenoughmail.tfcgenviewer.api.color.Colors;
import io.github.notenoughmail.tfcgenviewer.api.color.Gradient;
import io.github.notenoughmail.tfcgenviewer.impl.TFCGenViewerRegistration;
import io.github.notenoughmail.tfcgenviewer.impl.visualizers.chunk.ChunkElevationVisualizer;
import io.github.notenoughmail.tfcgenviewer.impl.visualizers.region.RainfallVisualizer;
import io.github.notenoughmail.tfcgenviewer.impl.visualizers.region.RockTypeVisualizer;
import io.github.notenoughmail.tfcgenviewer.impl.visualizers.region.TemperatureVisualizer;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class GradientProvider extends DataManagerProvider {

    public GradientProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, lookup, "Gradients");
    }

    @Override
    protected void make(HolderLookup.Provider lookup) {
        makeFor(Colors.MISC_GRADIENTS, colors -> {
            colors.accept(Colors.OCEAN, simple(
                    TFCGenViewerRegistration.GRAD_DARK_BLUE,
                    "ocean"
            ));
            colors.accept(RainfallVisualizer.RAINFALL, new ColorGradientDefinition(
                    TFCGenViewerRegistration.GRAD_RAINFALL.get(),
                    name("rainfall"),
                    tooltips(10, i -> "tfcgenviewer.gradient.rainfall." + i)
            ));
            colors.accept(TemperatureVisualizer.TEMPERATURE, new ColorGradientDefinition(
                    TFCGenViewerRegistration.GRAD_TEMPERATURE.get(),
                    name("temperature"),
                    tooltips(12, i -> "tfcgenviewer.gradient.temperature." + i)
            ));
            colors.accept(RockTypeVisualizer.UPLIFT, simple(
                    TFCGenViewerRegistration.GRAD_UPLIFT,
                    "rock_type", "uplift"
            ));
            colors.accept(RockTypeVisualizer.LAND, simple(
                    TFCGenViewerRegistration.GRAD_GREEN,
                    "rock_type", "land"
            ));
            colors.accept(RockTypeVisualizer.VOLCANIC, simple(
                    TFCGenViewerRegistration.GRAD_VOLCANIC,
                    "rock_type", "volcanic"
            ));
            colors.accept(RockTypeVisualizer.OCEANIC, simple(
                    TFCGenViewerRegistration.GRAD_BLUE,
                    "rock_type", "oceanic"
            ));
            colors.accept(ChunkElevationVisualizer.LOW, new ColorGradientDefinition(
                    TFCGenViewerRegistration.GRAD_BLUE.get(),
                    name("elevation", "low"),
                    tooltips(8, i -> "tfcgenviewer.gradient.elevation.low." + i)
            ));
            colors.accept(ChunkElevationVisualizer.MID, new ColorGradientDefinition(
                    TFCGenViewerRegistration.GRAD_GREEN.get(),
                    name("elevation", "middle"),
                    tooltips(8, i -> "tfcgenviewer.gradient.elevation.middle." + i)
            ));
            colors.accept(ChunkElevationVisualizer.HIGH, new ColorGradientDefinition(
                    TFCGenViewerRegistration.GRAD_UPLIFT.get(),
                    name("elevation", "high"),
                    tooltips(20, i -> "tfcgenviewer.gradient.elevation.high." + i)
            ));
        });
    }

    private static ColorGradientDefinition simple(
            Supplier<? extends Gradient> gradient,
            String... name
    ) {
        return new ColorGradientDefinition(
                gradient.get(),
                name(name),
                Optional.empty()
        );
    }

    private static Component name(String... name) {
        return Component.translatable("tfcgenviewer.gradient." + String.join(".", name));
    }

    private static Optional<List<Component>> tooltips(int count, IntFunction<String> key) {
        return Optional.of(
                IntStream.range(0, count)
                        .mapToObj(key)
                        .<Component>map(Component::translatable)
                        .toList()
        );
    }
}
