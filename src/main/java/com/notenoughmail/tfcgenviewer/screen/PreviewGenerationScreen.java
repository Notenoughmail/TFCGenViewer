package com.notenoughmail.tfcgenviewer.screen;

import com.notenoughmail.tfcgenviewer.TFCGenViewer;
import com.notenoughmail.tfcgenviewer.config.Config;
import com.notenoughmail.tfcgenviewer.util.ISeedSetter;
import com.notenoughmail.tfcgenviewer.util.ImageBuilder;
import com.notenoughmail.tfcgenviewer.util.VisualizerType;
import com.notenoughmail.tfcgenviewer.util.custom.InfoPane;
import com.notenoughmail.tfcgenviewer.util.custom.PreviewPane;
import com.notenoughmail.tfcgenviewer.util.custom.SeedValueSet;
import com.notenoughmail.tfcgenviewer.util.custom.SingleColumnOptionsList;
import net.dries007.tfc.world.ChunkGeneratorExtension;
import net.dries007.tfc.world.chunkdata.RegionChunkDataGenerator;
import net.dries007.tfc.world.region.RegionGenerator;
import net.dries007.tfc.world.settings.Settings;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PreviewGenerationScreen extends Screen {

    public static final Component TITLE = Component.translatable("tfcgenviewer.preview_world.title");
    public static final Component INVALID_GENERATOR = Component.translatable("tfcgenviewer.preview_world.invalid_generator");
    public static final Component APPLY = Component.translatable("button.tfcgenviewer.apply");
    public static final Component SAVE = Component.translatable("button.tfcgenviewer.save");
    public static final Component EXPORT = Component.translatable("button.tfcgenviewer.export");
    public static final ResourceLocation COMPASS = TFCGenViewer.identifier("textures/gui/compass.png");

    // Taken from TFC's create world screen
    private static OptionInstance<Integer> kmOpt(String key, int min, int max, int defaultValue) {
        return new OptionInstance<>(key, OptionInstance.cachedConstantTooltip(Component.translatable(key + ".tooltip")), (text, value) -> Options.genericValueLabel(text, Component.translatable("tfc.settings.km", String.format("%.1f", value / 1000.0))), new OptionInstance.IntRange(min, max), defaultValue, value -> {});
    }

    private static OptionInstance<Double> constOpt(String key, double defaultValue) {
        return new OptionInstance<>(key, OptionInstance.noTooltip(),
                (text, value) -> (value > 0.49 && value < 0.51) ?
                        Options.genericValueLabel(text, CommonComponents.OPTION_OFF) :
                        Component.translatable("options.percent_value", text, (int)((value - 0.5) * 200.0)),
                OptionInstance.UnitDouble.INSTANCE, (1.0 + defaultValue) * 0.5, value -> {});
    }

    private static OptionInstance<Double> pctOpt(String key, double defaultValue) {
        return new OptionInstance<>(key, OptionInstance.noTooltip(),
                (text, value) -> Component.translatable("options.percent_value", text, (int)((value - 0.5) * 200.0)),
                OptionInstance.UnitDouble.INSTANCE, defaultValue, value -> {});
    }

    private static OptionInstance<Integer> offsetOption(String key) {
        return new OptionInstance<>(
                key,
                OptionInstance.cachedConstantTooltip(Component.translatable(key + ".tooltip")),
                (text, offset) -> Options.genericValueLabel(
                    text,
                    Component.translatable(
                            "tfcgenviewer.preview_world.km",
                            "%.2f".formatted((128 * offset) / 1000F)
                    )
                ),
                new OptionInstance.IntRange(-175, 175),
                0,
                value -> {}
        );
    }

    private final CreateWorldScreen parent;
    @Nullable
    private final ChunkGeneratorExtension generator;
    @Nullable
    private RegionChunkDataGenerator regionGenerator;
    private Settings worldSettings;
    private OptionInstance<VisualizerType> visualizerType;
    private long seedInUse;
    private String editorSeed, localSeed;
    private Button seedbutton;
    private InfoPane infoPane;
    private Runnable seedTick;

    private OptionInstance<Boolean> flatBedrock, spawnOverlay;
    private OptionInstance<Integer> spawnDist, spawnCenterX, spawnCenterZ, tempScale, rainScale, xOffset, zOffset, previewScale;
    private OptionInstance<Double> tempConst, rainConst, continentalness, grassDensity;
    private PreviewPane previewPane;

    public PreviewGenerationScreen(CreateWorldScreen parent) {
        super(TITLE);
        this.parent = parent;
        final WorldCreationUiState uiState = parent.getUiState();
        editorSeed = localSeed = uiState.getSeed();
        final WorldCreationContext settings = uiState.getSettings();
        generator = settings.selectedDimensions().overworld() instanceof ChunkGeneratorExtension ext ? ext : null;
        worldSettings = generator == null ? null : generator.settings();
        regionGenerator = getRegionGenerator();
    }

    @Nullable
    private RegionChunkDataGenerator getRegionGenerator() {
        seedInUse = WorldOptions.parseSeed(localSeed).orElse(WorldOptions.randomSeed());
        if (seedbutton != null) {
            seedbutton.setMessage(Component.translatable("button.tfcgenviewer.current_seed", seedInUse));
        }
        if (generator != null) {
            final RandomSource random = new XoroshiroRandomSource(seedInUse);
            final RegionGenerator region = new RegionGenerator(worldSettings, random);
            return RegionChunkDataGenerator.create(random.nextLong(), worldSettings.rockLayerSettings(), region);
        }
        return null;
    }

    @Override
    public void tick() {
        super.tick();
        if (seedTick != null) {
            seedTick.run();
        }
        if (previewPane != null) {
            previewPane.tick();
        }
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pGuiGraphics);
        if (generator == null) {
            pGuiGraphics.drawCenteredString(font, INVALID_GENERATOR, width / 2, height / 2 - 50, 0xFFFFFF);
        }
        pGuiGraphics.drawCenteredString(font, title, width / 2, 8, 0xFFFFFF);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    @Override
    public void onClose() {
        assert minecraft != null;
        minecraft.setScreen(parent);
    }

    @Override
    protected void init() {
        assert minecraft != null;
        final int previewPixels = Math.min(height - 64, width / 2);

        if (generator != null) {

            final SingleColumnOptionsList options = new SingleColumnOptionsList(minecraft, (width - previewPixels) / 2 - 10, height, 32, height - 32, 25);

            options.add(
                    // Copied from TFC's create world screen
                    flatBedrock = OptionInstance.createBoolean("tfc.create_world.flat_bedrock", worldSettings.flatBedrock(), bool -> {}),
                    spawnDist = kmOpt("tfc.create_world.spawn_distance", 100, 20000, worldSettings.spawnDistance()),
                    spawnCenterX = kmOpt("tfc.create_world.spawn_center_x", -20000, 20000, worldSettings.spawnCenterX()),
                    spawnCenterZ = kmOpt("tfc.create_world.spawn_center_z", -20000, 20000, worldSettings.spawnCenterZ()),
                    tempScale = kmOpt("tfc.create_world.temperature_scale", 0, 40000, worldSettings.temperatureScale()),
                    tempConst = constOpt("tfc.create_world.temperature_constant", worldSettings.temperatureConstant()),
                    rainScale = kmOpt("tfc.create_world.rainfall_scale", 0, 40000, worldSettings.rainfallScale()),
                    rainConst = constOpt("tfc.create_world.rainfall_constant", worldSettings.rainfallConstant()),
                    continentalness = pctOpt("tfc.create_world.continentalness", worldSettings.continentalness()),
                    grassDensity = pctOpt("tfc.create_world.grass_density", worldSettings.grassDensity()),
                    spawnOverlay = OptionInstance.createBoolean("tfcgenviewer.preview_world.spawn_overlay", false),
                    previewScale = new OptionInstance<>(
                            "tfcgenviewer.preview_world.preview_scale",
                            OptionInstance.noTooltip(),
                            (caption, scale) -> Options.genericValueLabel(
                                    caption,
                                    Component.translatable(
                                            "tfcgenviewer.preview_world.km",
                                            ImageBuilder.previewSizeKm(scale)
                                    )
                            ),
                            new OptionInstance.IntRange(0, 6),
                            Config.defaultPreviewSize.get(),
                            scale -> {}
                    ),
                    xOffset = offsetOption("tfcgenviewer.preview_world.x_offset"),
                    zOffset = offsetOption("tfcgenviewer.preview_world.z_offset"),
                    visualizerType = new OptionInstance<>(
                            "tfcgenviewer.preview_world.visualizer_type",
                            OptionInstance.noTooltip(),
                            (caption, task) -> task.getName(),
                            new OptionInstance.Enum<>(List.of(VisualizerType.VALUES), VisualizerType.CODEC),
                            VisualizerType.RIVERS,
                            task -> {}
                    ),
                    new OptionInstance<>(
                            "selectWorld.enterSeed",
                            OptionInstance.noTooltip(),
                            (caption, seed) -> Component.literal(seed),
                            new SeedValueSet(
                                    font,
                                    s -> editorSeed = s,
                                    () -> editorSeed,
                                    tick -> seedTick = tick
                            ),
                            String.valueOf(seedInUse),
                            s -> {}
                    ),
                    new OptionInstance<>(
                            "button.tfcgenviewer.apply",
                            OptionInstance.noTooltip(),
                            (caption, bool) -> caption,
                            OptionInstance.BOOLEAN_VALUES,
                            false,
                            bool -> {}
                    ) {
                        @Override
                        public AbstractWidget createButton(Options pOptions, int pX, int pY, int pWidth, Consumer pOnValueChanged) {
                            return Button.builder(APPLY, button -> applyUpdates(true)).bounds(pX, pY, pWidth, 20).build();
                        }
                    },
                    new OptionInstance<>(
                            "button.tfcgenviewer.export",
                            OptionInstance.noTooltip(),
                            (caption, bool) -> caption,
                            OptionInstance.BOOLEAN_VALUES,
                            false,
                            bool -> {}
                    ) {
                        @Override
                        public AbstractWidget createButton(Options pOptions, int pX, int pY, int pWidth, Consumer pOnValueChanged) {
                            return Button.builder(EXPORT, button -> ImageBuilder.exportImage()).bounds(pX, pY, pWidth, 20).build();
                        }
                    }
            );
            addRenderableWidget(options);

            final int previewLeftEdge = (width - previewPixels) / 2;
            addRenderableWidget(previewPane = new PreviewPane(previewLeftEdge, (height - previewPixels) / 2, previewPixels, font, true));

            final int rightPos = (width + previewPixels) / 2 + 10;
            addRenderableWidget(infoPane = new InfoPane(rightPos, 32, width - rightPos - 10, height - 64, Component.empty(), font, COMPASS, 64));

            seedbutton = Button
                    .builder(Component.translatable("button.tfcgenviewer.current_seed", seedInUse), button -> minecraft.keyboardHandler.setClipboard(String.valueOf(seedInUse)))
                    .tooltip(Tooltip.create(Component.translatable("button.tfcgenviewer.current_seed.tooltip")))
                    .bounds(previewLeftEdge, height - 28, previewPixels, 20)
                    .build();

            addRenderableWidget(seedbutton);
            applyUpdates(true);

            addRenderableWidget(Button.builder(SAVE, button -> {
                applyUpdates(false);
                minecraft.setScreen(parent);
                ImageBuilder.cancelAndClearPreviews();
            }).bounds((width - previewPixels) / 2 - 90, height - 28, 80, 20).build());
            addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> {
                minecraft.setScreen(parent);
                ImageBuilder.cancelAndClearPreviews();
            }).bounds((width + previewPixels) / 2 + 10, height - 28, 80, 20).build());
        } else {
            addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, button -> minecraft.setScreen(parent)).bounds(width / 2 - 30, height - 28, 60, 20).build());
        }
    }

    private void applyUpdates(boolean local) {
        if (generator != null) {
              worldSettings = new Settings(
                    flatBedrock.get(),
                    spawnDist.get(),
                    spawnCenterX.get(),
                    spawnCenterZ.get(),
                    0.49 < tempConst.get() && tempConst.get() < 0.51 ? tempScale.get() : 0,
                    (float) (tempConst.get() * 2.0 - 1.0),
                    0.49 < rainConst.get() && rainConst.get() < 0.51 ? rainScale.get() : 0,
                    (float) (rainConst.get() * 2.0 -1.0),
                    worldSettings.rockLayerSettings(),
                    continentalness.get().floatValue(),
                    grassDensity.get().floatValue()
            );
            localSeed = editorSeed;
            if (!local) {
                generator.applySettings(old -> worldSettings); // Rock layers are not changed on this screen, so should be fine
                parent.getUiState().setSeed(localSeed);
                if (parent.tabNavigationBar != null) {
                    parent.tabNavigationBar.tabs.forEach(tab -> {
                        if (tab instanceof ISeedSetter setter) {
                            setter.tfcgenviewer$SetSeed(localSeed);
                        }
                    });
                }
            } else {
                regionGenerator = getRegionGenerator();
                assert regionGenerator != null;
                ImageBuilder.build(
                        regionGenerator,
                        visualizerType.get(),
                        xOffset.get(),
                        zOffset.get(),
                        spawnOverlay.get(),
                        spawnDist.get(),
                        spawnCenterX.get(),
                        spawnCenterZ.get(),
                        previewScale.get(),
                        info -> {
                            infoPane.setMessage(info.rightInfo());
                            previewPane.setInfo(info);
                        }
                );
            }
        }
    }
}
