package io.github.notenoughmail.tfcgenviewer.client.screen;

import com.mojang.serialization.Codec;
import io.github.notenoughmail.tfcgenviewer.api.widget.OptionRequest;
import io.github.notenoughmail.tfcgenviewer.client.options.EditBoxValueSet;
import io.github.notenoughmail.tfcgenviewer.client.options.OptionOrder;
import io.github.notenoughmail.tfcgenviewer.client.widget.SingleColumnOptionsList;
import io.github.notenoughmail.tfcgenviewer.impl.preview.Image;
import io.github.notenoughmail.tfcgenviewer.api.scale.IScale;
import io.github.notenoughmail.tfcgenviewer.api.scale.ImageSize;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IGeneratorVisualizer;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IVisualizerType;
import io.github.notenoughmail.tfcgenviewer.impl.preview.Preview;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import it.unimi.dsi.fastutil.doubles.DoubleConsumer;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntConsumer;
import net.dries007.tfc.world.ChunkGeneratorExtension;
import net.dries007.tfc.world.settings.Settings;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.neoforged.neoforge.common.extensions.ILevelExtension;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.*;

public class PreviewScreen<
        G extends ChunkGeneratorExtension,
        I extends ImageSize,
        S extends IScale<I>,
        V extends IVisualizerType<G, C, S, O>,
        C,
        O extends IVisualizerType.Options<O>
        > extends Screen {

    // Copied from CreateTFCWorldScreen
    private static OptionInstance<Double> constOption(String caption, double defaultValue) {
        return new OptionInstance<>(
                caption,
                OptionInstance.noTooltip(),
                (text, value) -> (value > 0.49 && value < 0.51) ?
                        Options.genericValueLabel(text, CommonComponents.OPTION_OFF) :
                        Component.translatable("options.percent_value", text, (int)((value - 0.5) * 200.0)),
                OptionInstance.UnitDouble.INSTANCE,
                (1.0 + defaultValue) * 0.5,
                d -> {}
        );
    }

    private static OptionInstance<Double> pctOption(String caption, double defaultValue) {
        return new OptionInstance<>(
                caption,
                OptionInstance.noTooltip(),
                (text, value) -> Component.translatable("options.percent_value", text, (int)((value - 0.5) * 200.0)),
                OptionInstance.UnitDouble.INSTANCE,
                defaultValue,
                d -> {}
        );
    }

    private static OptionInstance<Integer> kmOption(String caption, int min, int max, int defaultValue) {
        return new OptionInstance<>(
                caption,
                OptionInstance.cachedConstantTooltip(Component.translatable(caption + ".tooltip")),
                (text, value) -> Options.genericValueLabel(
                        text,
                        Component.translatable("tfc.settings.km", String.format("%.1f", value / 1000.0))
                ),
                new OptionInstance.IntRange(min, max),
                defaultValue,
                i -> {}
        );
    }

    private final G generator, originalGenerator;
    private final IGeneratorVisualizer<G, I, S, V> visualizer;
    private final CreateWorldScreen parent;
    private final State state;
    private final RegistryAccess registryAccess;

    private final OptionInstance<V> visualizerType;
    private final OptionInstance<I> imageSize;
    private final OptionInstance<Boolean> flatBedrock, finiteContinents, spawnOverlay;
    private final OptionInstance<Integer> spawnDist, spawnCenterX, spawnCenterZ, tempScale, rainScale, xOffset, zOffset;
    private final OptionInstance<Double> tempConst, rainConst, continentalness, grassDensity;
    private final OptionInstance<String> seed;
    private final OptionInstance<?>[] intransientOptions;

    private final SingleColumnOptionsList options;

    public PreviewScreen(G generator, IGeneratorVisualizer<G, I, S, V> visualizer, CreateWorldScreen parent, ResourceKey<LevelStem> dimension) {
        super(Component.translatable(
                "tfcgenviewer.screen.preview_world.title",
                Component.translatable(dimension.location().toLanguageKey(ILevelExtension.TRANSLATION_PREFIX)),
                visualizer.name()
        ));

        originalGenerator = generator;
        this.generator = visualizer.recreateGenerator(generator);
        if (this.generator == generator) throw new IllegalArgumentException("Generator Visualizers must recreate generators! %s [%s] doe not".formatted(visualizer, visualizer.getClass().getSimpleName()));
        this.visualizer = visualizer;
        this.parent = parent;
        state = new State();
        registryAccess = parent.getUiState().getSettings().worldgenLoadContext();
        visualizerType = visualizerType(visualizer, v -> onVisualizerChange());
        imageSize = imageSize(visualizer.scale(), i -> state.recreateVizOptions());

        state.updateSeed(parent.getUiState().getSeed());
        state.recreateVizOptions();

        final Settings settings = generator.settings();
        final int offset = visualizer.scale().blocksPerPixel() * visualizer.maximumPreviewOffset();
        intransientOptions = new OptionInstance[] {
                flatBedrock = OptionInstance.createBoolean("tfc.create_world.flat_bedrock", settings.flatBedrock(), b -> {
                }),
                spawnDist = kmOption("tfc.create_world.spawn_distance", 100, 20_000, settings.spawnDistance()),
                spawnCenterX = kmOption("tfc.create_world.spawn_center_x", -20_000, 20_000, settings.spawnCenterX()),
                spawnCenterZ = kmOption("tfc.create_world.spawn_center_z", -20_000, 20_000, settings.spawnCenterZ()),
                tempScale = kmOption("tfc.create_world.temperature_scale", 0, 40_000, settings.temperatureScale()),
                rainScale = kmOption("tfc.create_world.rainfall_scale", 0, 40_000, settings.rainfallScale()),
                tempConst = constOption("tfc.create_world.temperature_constant", settings.temperatureConstant()),
                rainConst = constOption("tfc.create_world.rainfall_constant", settings.rainfallConstant()),
                continentalness = pctOption("tfc.create_world.continentalness", settings.continentalness()),
                grassDensity = pctOption("tfc.create_world.grass_density", settings.continentalness()),
                finiteContinents = OptionInstance.createBoolean("tfc.create_world.finite_continents", settings.finiteContinents(), b -> {
                }),
                spawnOverlay = OptionInstance.createBoolean("tfcgenviewer.screen.preview_world.option.spawn_overlay", false, b -> {
                }),
                xOffset = kmOption("tfcgenviewer.screen.preview_world.option.x_offset", -offset, offset, 0),
                zOffset = kmOption("tfcgenviewer.screen.preview_world.option.z_offset", -offset, offset, 0),
                seed = new OptionInstance<>(
                        "selectWorld.enterSeed",
                        OptionInstance.noTooltip(),
                        (c, seed) -> Component.literal(seed),
                        new EditBoxValueSet(
                                font,
                                editBox -> {
                                }
                        ),
                        String.valueOf(state.seed),
                        s -> {}
                )
                // Apply
                // Export
        };
        options = new SingleColumnOptionsList(getMinecraft(), width, height, 32, 25);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void onClose() {
        super.onClose();
        getMinecraft().setScreen(parent);
    }

    @Override
    protected void init() {
        final int previewPixels = Math.min(height - 64, width / 2);

        populateOptions();
        options.updateSizeAndPosition(
                (width - previewPixels) / 2 - 10,
                height - 64,
                32
        );
        addRenderableWidget(options);
    }

    private void visualize() {
        state.previousImage.cancel(true);

        final boolean spawnOverlay = this.spawnOverlay.get();
        final int xCenterBlocks = this.xOffset.get(), zCenterBlocks = this.zOffset.get();
        final V viz = visualizerType.get();
        final C cache = viz.createCache(registryAccess, generator, imageSize.get(), state.genSeed = state.seed);
        final I imageSize = this.imageSize.get();
        final S scale = visualizer.scale();
        final IVisualizerType.DrawInfo<G, C, S, O> info = new IVisualizerType.DrawInfo<>(
                generator,
                cache,
                registryAccess,
                new Int2ObjectOpenHashMap<>(),
                imageSize,
                scale,
                IVisualizerType.Options.copy(state.vizOptions)
        );
        final Image image = new Image(imageSize.sizeInPixels());

        state.previousImage = Preview.draw(
                image,
                imageSize,
                info,
                viz,
                xCenterBlocks,
                zCenterBlocks
        ).thenAccept(ret -> {
            final MutableComponent vizInfo = Component.translatable("tfcgenviewer.preview_info.base", viz.name(), visualizer.scale().formatSize(imageSize));
            vizInfo.append(CommonComponents.NEW_LINE)
                    .append(Component.translatable("tfcgenviewer.preview_info.centered_on", xCenterBlocks, zCenterBlocks))
                    .append(CommonComponents.NEW_LINE)
                    .append(CommonComponents.NEW_LINE);
            if (ret.millis() == -1L) {
                vizInfo.append(Preview.ON_ERROR);
            } else {
                final Component additional = viz.previewInfo(info);
                if (additional != null) {
                    vizInfo.append(Component.translatable("tfcgenviewer.preview_info.additional_from_visualizer", additional))
                            .append(CommonComponents.NEW_LINE)
                            .append(CommonComponents.NEW_LINE);
                }
                vizInfo.append(Component.translatable("tfcgenviewer.preview_info.color_key", viz.colorKey(registryAccess, cache)));
            }
            // Update info & preview panes
        });
    }

    private void applySettings() {
        applySettingsLocal();
        originalGenerator.applySettings(s -> generator.settings());
    }

    private void applySettingsLocal() {
        generator.applySettings(s -> new Settings(
                flatBedrock.get(),
                spawnDist.get(),
                spawnCenterX.get(),
                spawnCenterZ.get(),
                0.49 < tempConst.get() && tempConst.get() < 0.51 ? tempScale.get() : 0,
                (float) (tempConst.get() * 2.0 - 1.0),
                0.49 < rainConst.get() && rainConst.get() < 0.51 ? rainScale.get() : 0,
                (float) (rainConst.get() * 2.0 - 1.0),
                s.rockLayerSettings(),
                continentalness.get().floatValue(),
                grassDensity.get().floatValue(),
                finiteContinents.get()
        ));
        state.recreateVizOptions();
        state.updateSeed(seed.get());
    }

    private void onVisualizerChange() {
        state.recreateVizOptions();
        populateOptions();
    }

    private void populateOptions() {
        options.children().clear();
        options.add(intransientOptions);
        visualizerType.get().addOptions(new OptionOrders(s -> options.add(s.get())), state.vizOptions);
    }

    private class State {

        O vizOptions;
        long seed, genSeed;
        CompletableFuture<Void> previousImage = CompletableFuture.completedFuture(null);

        void updateSeed(String seed) {
            this.seed = WorldOptions.parseSeed(seed).orElse(WorldOptions.randomSeed());
        }

        void recreateVizOptions() {
            vizOptions = visualizerType.get().createOptions(
                    registryAccess,
                    generator,
                    imageSize.get()
            );
        }
    }

    private record OptionOrders(Consumer<Supplier<OptionInstance<?>>> order) implements OptionRequest {

        private <T> Order<T> order(OptionOrder<T> order) {
            this.order.accept(order::get);
            return order;
        }

        @Override
        public <T> Order<T> order(String name, T initial, List<T> values, Codec<T> codec, Consumer<T> onChange) {
            return order(OptionOrder.list(name, initial, values, codec, onChange));
        }

        @Override
        public Order<Boolean> orderBool(String name, boolean initial, BooleanConsumer onChange) {
            return order(OptionOrder.bool(name, initial, onChange));
        }

        @Override
        public <T extends Comparable<T>> Order<T> order(String name, T initial, T min, T max, Codec<T> codec, ToDoubleFunction<T> toSlider, DoubleFunction<T> fromSlider, Consumer<T> onChange) {
            return order(OptionOrder.comparable(name, initial, min, max, codec, toSlider, fromSlider, onChange));
        }

        @Override
        public Order<Integer> orderInt(String name, int initial, int min, int max, IntConsumer onChange) {
            return order(OptionOrder.integer(name, initial, min, max, onChange));
        }

        @Override
        public Order<Double> orderDouble(String name, double initial, double min, double max, DoubleConsumer onChange) {
            return order(OptionOrder.doub(name, initial, min, max, onChange));
        }
    }

    public static <V extends IVisualizerType<?, ?, ?, ?>> OptionInstance<V> visualizerType(IGeneratorVisualizer<?, ?, ?, V> visualizer, Consumer<V> onChange) {
        return new OptionInstance<>(
                "tfcgenviewer.screen.preview_world.option.visualizer_type",
                OptionInstance.noTooltip(),
                (caption, viz) -> viz.name(),
                new OptionInstance.Enum<>(visualizer.allVisualizers(), visualizer.visualizerCodec()),
                visualizer.allVisualizers().getFirst(),
                onChange
        );
    }

    public static <I extends ImageSize> OptionInstance<I> imageSize(IScale<I> scale, Consumer<I> onChange) {
        return new OptionInstance<>(
                "tfcgenviewer.screen.preview_world.option.preview_size",
                OptionInstance.noTooltip(),
                (caption, i) -> Options.genericValueLabel(
                        caption,
                        scale.formatSize(i)
                ),
                new OptionInstance.Enum<>(scale.sizes(), scale.codec()),
                scale.getDefault(),
                onChange
        );
    }
}
