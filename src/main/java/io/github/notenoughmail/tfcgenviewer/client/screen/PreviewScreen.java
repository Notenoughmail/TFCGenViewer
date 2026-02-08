package io.github.notenoughmail.tfcgenviewer.client.screen;

import io.github.notenoughmail.tfcgenviewer.api.ColorTooltips;
import io.github.notenoughmail.tfcgenviewer.api.scale.IScale;
import io.github.notenoughmail.tfcgenviewer.api.scale.ImageSize;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IGeneratorVisualizer;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IVisualizerType;
import io.github.notenoughmail.tfcgenviewer.client.TFCGenViewerClient;
import io.github.notenoughmail.tfcgenviewer.client.options.EditBoxValueSet;
import io.github.notenoughmail.tfcgenviewer.client.options.OptionOrders;
import io.github.notenoughmail.tfcgenviewer.client.widget.ButtonOption;
import io.github.notenoughmail.tfcgenviewer.client.widget.InfoPane;
import io.github.notenoughmail.tfcgenviewer.client.widget.PreviewPane;
import io.github.notenoughmail.tfcgenviewer.client.widget.SingleColumnOptionsList;
import io.github.notenoughmail.tfcgenviewer.impl.ISeedSetter;
import io.github.notenoughmail.tfcgenviewer.impl.preview.Image;
import io.github.notenoughmail.tfcgenviewer.impl.preview.Preview;
import net.dries007.tfc.world.ChunkGeneratorExtension;
import net.dries007.tfc.world.settings.RockLayerSettings;
import net.dries007.tfc.world.settings.Settings;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.neoforged.neoforge.common.extensions.ILevelExtension;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class PreviewScreen<
        G extends ChunkGeneratorExtension,
        I extends ImageSize,
        S extends IScale<I>,
        V extends IVisualizerType<G, C, S, O>,
        C,
        O extends IVisualizerType.Options<O>
        > extends Screen {

    public static final Component SAVE = Component.translatable("tfcgenviewer.button.save");

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
    private final OptionInstance<?>[] intransientOptionsBefore, intransientOptionsAfter;

    private final PreviewPane previewPane;
    private final InfoPane infoPane;

    private final Button seedButton, saveButton, cancelButton;

    private transient EditBox seedBox;

    private SingleColumnOptionsList options;

    public PreviewScreen(
            G generator,
            IGeneratorVisualizer<G, I, S, V> visualizer,
            CreateWorldScreen parent,
            ResourceKey<LevelStem> dimension
    ) {
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

        final Settings settings = generator.settings();
        final int offset = visualizer.scale().blocksPerPixel() * visualizer.maximumPreviewOffset();
        intransientOptionsBefore = new OptionInstance[] {
                flatBedrock = OptionInstance.createBoolean("tfc.create_world.flat_bedrock", settings.flatBedrock(), b -> {}),
                spawnDist = Preview.kmOption("tfc.create_world.spawn_distance", 100, 20_000, settings.spawnDistance()),
                spawnCenterX = Preview.kmOption("tfc.create_world.spawn_center_x", -20_000, 20_000, settings.spawnCenterX()),
                spawnCenterZ = Preview.kmOption("tfc.create_world.spawn_center_z", -20_000, 20_000, settings.spawnCenterZ()),
                tempScale = Preview.kmOption("tfc.create_world.temperature_scale", 0, 40_000, settings.temperatureScale()),
                rainScale = Preview.kmOption("tfc.create_world.rainfall_scale", 0, 40_000, settings.rainfallScale()),
                tempConst = constOption("tfc.create_world.temperature_constant", settings.temperatureConstant()),
                rainConst = constOption("tfc.create_world.rainfall_constant", settings.rainfallConstant()),
                continentalness = pctOption("tfc.create_world.continentalness", settings.continentalness()),
                grassDensity = pctOption("tfc.create_world.grass_density", settings.continentalness()),
                finiteContinents = OptionInstance.createBoolean("tfc.create_world.finite_continents", settings.finiteContinents(), b -> {}),
                visualizerType = Preview.visualizerTypeOption(visualizer.allVisualizers(), v -> onVisualizerChange())
        };
        intransientOptionsAfter = new OptionInstance[] {
                imageSize = Preview.imageSizeOption(visualizer.scale()),
                spawnOverlay = OptionInstance.createBoolean("tfcgenviewer.screen.preview_world.option.spawn_overlay", false, b -> {}),
                xOffset = Preview.kmOption("tfcgenviewer.screen.preview_world.option.x_offset", -offset, offset, 0),
                zOffset = Preview.kmOption("tfcgenviewer.screen.preview_world.option.z_offset", -offset, offset, 0),
                seed = new OptionInstance<>(
                        "selectWorld.enterSeed",
                        OptionInstance.noTooltip(),
                        (c, seed) -> Component.literal(seed),
                        new EditBoxValueSet(
                                () -> font,
                                editBox -> seedBox = editBox
                        ),
                        parent.getUiState().getSeed(),
                        s -> {}
                ),
                new ButtonOption("tfcgenviewer.button.apply", b -> {
                    applySettingsLocal();
                    visualize();
                }),
                // It would be so nice to disable the button while an image is generating
                new ButtonOption("tfcgenviewer.button.export", b -> {
                    if (state.previousImageProcess.state() == Future.State.SUCCESS) {
                        try {
                            state.previousImageProcess.get().export();
                        } catch (Throwable e) {
                            throw new IllegalStateException("Impossible state!", e);
                        }
                    }
                })
        };

        previewPane = new PreviewPane(0, 0, () -> font, true);
        infoPane = new InfoPane(0, 0, 10, 10, () -> font);
        state.createVizOptions(true);

        seedButton = Button
                .builder(CommonComponents.EMPTY, b -> {
                    final String seed = String.valueOf(state.genSeed);
                    getMinecraft().keyboardHandler.setClipboard(seed);
                    this.seed.set(seed);
                    seedBox.setValue(seed);
                })
                .tooltip(Tooltip.create(Component.translatable("tfcgenviewer.button.current_seed.tooltip")))
                .build();
        saveButton = Button.builder(SAVE, b -> {
            applySettings();
            getMinecraft().setScreen(this.parent);
        }).build();
        cancelButton = Button.builder(CommonComponents.GUI_CANCEL, b -> getMinecraft().setScreen(this.parent)).build();

        visualize();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void onClose() {
        super.onClose();
        getMinecraft().setScreen(parent);
        state.close();
    }

    @Override
    protected void init() {
        final int previewPixels = Math.min(height - 64, (int) (width * TFCGenViewerClient.maxPreviewWidth.getAsDouble()));

        options = new SingleColumnOptionsList(getMinecraft(), width, height, 32, 25);
        options.setScrollBarOffset(-8);
        populateOptions();
        final int leftPreview = (width - previewPixels) / 2;
        options.setRectangle(
                leftPreview - 6,
                height - 64,
                0,
                32
        );
        options.clampScrollAmount();
        addRenderableWidget(options);

        previewPane.setRectangle(
                previewPixels,
                previewPixels,
                leftPreview,
                (height - previewPixels) / 2
        );
        addRenderableWidget(previewPane);

        infoPane.setRectangle(
                leftPreview - 12,
                height - 64,
                leftPreview + previewPixels + 6,
                32
        );
        infoPane.fontAvailable(visualizerType.get());
        addRenderableWidget(infoPane);

        seedButton.setRectangle(
                previewPixels,
                20,
                leftPreview,
                height - 26
        );
        addRenderableWidget(seedButton);
        saveButton.setRectangle(
                leftPreview - 8,
                20,
                2,
                height - 26
        );
        addRenderableWidget(saveButton);
        cancelButton.setRectangle(
                leftPreview - 8,
                20,
                leftPreview + previewPixels + 6,
                height - 26
        );
        addRenderableWidget(cancelButton);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 11, 0xFFFFFFFF);
    }

    private void visualize() {
        state.close();
        previewPane.nowProcessing();

        final int xCenterBlocks = xOffset.get(), zCenterBlocks = zOffset.get();
        state.genSeed = parseSeed();
        seedButton.setMessage(Component.translatable("tfcgenviewer.button.current_seed", state.genSeed));

        final G gen = visualizer.recreateGenerator(generator);
        final V viz = visualizerType.get();
        final C cache = viz.createCache(registryAccess, gen, imageSize.get(), state.genSeed);
        final I imageSize = this.imageSize.get();
        final S scale = visualizer.scale();
        final IVisualizerType.DrawInfo<G, C, S, O> info = new IVisualizerType.DrawInfo<>(
                gen,
                cache,
                registryAccess,
                new ColorTooltips(),
                imageSize,
                scale,
                IVisualizerType.Options.copy(state.vizOptions)
        );

        final Image image = new Image(imageSize.sizeInPixels());
        state.previousImage = image;

        state.previousImageProcess = Preview.draw(
                image,
                imageSize,
                info,
                viz,
                xCenterBlocks,
                zCenterBlocks,
                visualizer.id(),
                previewPane,
                infoPane,
                Preview.SpawnInfo.of(
                        spawnOverlay,
                        spawnCenterX,
                        spawnCenterZ,
                        spawnDist
                ),
                registryAccess,
                true
        );
    }

    private void applySettings() {
        applySettingsLocal();
        originalGenerator.applySettings(s -> generator.settings());
        parent.getUiState().setSeed(seed.get());
        if (parent.tabNavigationBar != null) {
            for (Tab tab : parent.tabNavigationBar.tabs) {
                if (tab instanceof ISeedSetter setter) {
                    setter.tfcgenviewer$SetSeed(seed.get());
                }
            }
        }
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
    }

    private void applyRocks(RockLayerSettings rocks) {
        generator.applySettings(s -> new Settings(
                s.flatBedrock(),
                s.spawnDistance(),
                s.spawnCenterX(),
                s.spawnCenterZ(),
                s.temperatureScale(),
                s.temperatureConstant(),
                s.rainfallScale(),
                s.rainfallConstant(),
                rocks,
                s.continentalness(),
                s.grassDensity(),
                s.finiteContinents()
        ));
    }

    private long parseSeed() {
        return WorldOptions.parseSeed(seed.get()).orElse(WorldOptions.randomSeed());
    }

    private void onVisualizerChange() {
        state.createVizOptions(false);
    }

    private void populateOptions() {
        options.children().clear();
        options.add(intransientOptionsBefore);
        visualizerType.get().addOptions(new OptionOrders(options::addWithBackground), state.vizOptions);
        options.add(intransientOptionsAfter);
    }

    private class State {

        O vizOptions;
        long genSeed;
        Image previousImage = null;
        CompletableFuture<Preview.ImageReturn> previousImageProcess = CompletableFuture.completedFuture(null);

        void createVizOptions(boolean initial) {
            vizOptions = visualizerType.get().createOptions(registryAccess);
            if (!initial) {
                populateOptions();
            }
        }

        void close() {
            previousImageProcess.cancel(true);
            if (previousImage != null) {
                previousImage.close();
                previousImage = null;
            }
        }
    }
}
