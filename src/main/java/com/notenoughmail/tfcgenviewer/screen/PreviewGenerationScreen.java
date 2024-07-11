package com.notenoughmail.tfcgenviewer.screen;

import com.notenoughmail.tfcgenviewer.util.ISeedSetter;
import com.notenoughmail.tfcgenviewer.util.ImageBuilder;
import com.notenoughmail.tfcgenviewer.util.SeedValueSet;
import com.notenoughmail.tfcgenviewer.util.VisualizeTask;
import net.dries007.tfc.world.ChunkGeneratorExtension;
import net.dries007.tfc.world.region.RegionGenerator;
import net.dries007.tfc.world.settings.Settings;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
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

    private final CreateWorldScreen parent;
    @Nullable
    private final ChunkGeneratorExtension generator;
    @Nullable
    private RegionGenerator regionGenerator;
    private Settings worldSettings;
    private OptionInstance<VisualizeTask> visualizerTask;
    private long seedInUse;
    private String editorSeed, localSeed;

    // Copied from TFC's create world screen
    private OptionsList options;
    private OptionInstance<Boolean> flatBedrock;
    private OptionInstance<Integer> spawnDist, spawnCenterX, spawnCenterZ, tempScale, rainScale;
    private OptionInstance<Double> tempConst, rainConst, continentalness, grassDensity;

    public PreviewGenerationScreen(CreateWorldScreen parent) {
        super(TITLE);
        this.parent = parent;
        final WorldCreationUiState uiState = parent.getUiState();
        editorSeed = localSeed = uiState.getSeed();
        final WorldCreationContext settings = uiState.getSettings();
        generator = settings.selectedDimensions().overworld() instanceof ChunkGeneratorExtension ext ? ext : null;
        worldSettings = generator == null ? null : generator.settings();
        regionGenerator = getRegionGenerator();
        ImageBuilder.getPreview(); // Initialize the texture
    }

    @Nullable
    private RegionGenerator getRegionGenerator() {
        seedInUse = WorldOptions.parseSeed(localSeed).orElse(WorldOptions.randomSeed());
        return generator == null ? null : new RegionGenerator(worldSettings, new XoroshiroRandomSource(seedInUse));
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pGuiGraphics);
        if (generator != null) {
            renderGeneration(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        } else {
            pGuiGraphics.drawCenteredString(font, INVALID_GENERATOR, width / 2, height / 2 - 50, 0xFFFFFF);
        }
        pGuiGraphics.drawCenteredString(font, title, width / 2, 8, 0xFFFFFF);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    private void renderGeneration(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        options.render(graphics, mouseX, mouseY, partialTick);
        final int preview = Math.min(width / 3 * 2 - 50, height - 50);
        graphics.blit(ImageBuilder.getPreview(), width / 3 + 20, 32, 0, 0, preview, preview, preview, preview);
        graphics.drawCenteredString(font, Component.translatable("tfcgenviewer.preview_world.current_seed", seedInUse), (width / 3) + (preview / 2), height - 14, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        assert minecraft != null;
        minecraft.setScreen(parent);
    }

    @Override
    protected void init() {
        assert minecraft != null;

        if (generator != null) {

            options = new OptionsList(minecraft, width / 3, height, 32, height - 32, 25);

            options.addSmall(
                    flatBedrock = OptionInstance.createBoolean("tfc.create_world.flat_bedrock", worldSettings.flatBedrock(), bool -> {}),
                    spawnDist = kmOpt("tfc.create_world.spawn_distance", 100, 20000, worldSettings.spawnDistance())
            );
            options.addSmall(
                    spawnCenterX = kmOpt("tfc.create_world.spawn_center_x", -20000, 20000, worldSettings.spawnCenterX()),
                    spawnCenterZ = kmOpt("tfc.create_world.spawn_center_z", -20000, 20000, worldSettings.spawnCenterZ())
            );
            options.addSmall(
                    tempScale = kmOpt("tfc.create_world.temperature_scale", 0, 40000, worldSettings.temperatureScale()),
                    rainScale = kmOpt("tfc.create_world.rainfall_scale", 0, 40000, worldSettings.rainfallScale())
            );
            options.addSmall(
                    tempConst = constOpt("tfc.create_world.temperature_constant", worldSettings.temperatureConstant()),
                    rainConst = constOpt("tfc.create_world.rainfall_constant", worldSettings.rainfallConstant())
            );
            options.addSmall(
                    continentalness = pctOpt("tfc.create_world.continentalness", worldSettings.continentalness()),
                    grassDensity = pctOpt("tfc.create_world.grass_density", worldSettings.grassDensity())
            );
            options.addSmall(
                    visualizerTask = new OptionInstance<>("tfcgenviewer.preview_world.visualizer_task", OptionInstance.noTooltip(), (caption, task) -> task.getName(), new OptionInstance.Enum<>(List.of(VisualizeTask.VALUES), VisualizeTask.CODEC), VisualizeTask.RIVERS, task -> {}),
                    new OptionInstance<>("selectWorld.enterSeed", OptionInstance.noTooltip(), (caption, seed) -> Component.literal(seed), new SeedValueSet(font, (s, editBox) -> editorSeed = editBox.getValue(), () -> editorSeed), String.valueOf(seedInUse), s -> {})
            );
            options.addBig(
                    new OptionInstance<>("button.tfcgenviewer.apply", OptionInstance.noTooltip(), (caption, bool) -> caption, OptionInstance.BOOLEAN_VALUES, false, bool -> {}) {
                        @Override
                        public AbstractWidget createButton(Options pOptions, int pX, int pY, int pWidth, Consumer pOnValueChanged) {
                            return Button.builder(APPLY, button -> applyUpdates(true)).bounds(pX, pY, pWidth, 20).build();
                        }
                    }
            );
            addWidget(options);
            applyUpdates(true);
        }

        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> {
            applyUpdates(false);
            minecraft.setScreen(parent);
        }).bounds(8, height - 28, 150, 20).build());
        addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> minecraft.setScreen(parent)).bounds(166, height - 28, 150, 20).build());
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
                ImageBuilder.build(regionGenerator, visualizerTask.get());
            }
        }
    }
}
