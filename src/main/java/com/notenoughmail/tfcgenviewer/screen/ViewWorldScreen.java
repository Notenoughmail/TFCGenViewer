package com.notenoughmail.tfcgenviewer.screen;

import com.notenoughmail.tfcgenviewer.config.Config;
import com.notenoughmail.tfcgenviewer.util.ImageBuilder;
import com.notenoughmail.tfcgenviewer.util.VisualizerType;
import com.notenoughmail.tfcgenviewer.util.custom.InfoPane;
import com.notenoughmail.tfcgenviewer.util.custom.PreviewPane;
import com.notenoughmail.tfcgenviewer.util.custom.SingleColumnOptionsList;
import net.dries007.tfc.world.chunkdata.RegionChunkDataGenerator;
import net.dries007.tfc.world.region.RegionGenerator;
import net.dries007.tfc.world.settings.Settings;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ViewWorldScreen extends Screen {

    public static final Component TITLE = Component.translatable("tfcgenviewer.view_world.title");

    private final List<VisualizerType> visualizers;
    private final long seed;
    private final RegionChunkDataGenerator generator;
    private final boolean allowExport, coordinatesVisible, seedVisible;
    private final int xCenter, zCenter;

    private OptionInstance<Integer> scale;
    private OptionInstance<VisualizerType> visualizerType;
    private PreviewPane viewPane;
    private InfoPane infoPane;

    public ViewWorldScreen(List<VisualizerType> visualizers, long seed, Settings settings, boolean allowExport, boolean coordinatesVisible, boolean seedVisible, int xCenter, int zCenter) {
        super(TITLE);
        this.visualizers = visualizers;
        this.seed = seed;
        final RandomSource genRandom = new XoroshiroRandomSource(seed);
        final RegionGenerator regionGen = new RegionGenerator(settings, genRandom);
        generator = RegionChunkDataGenerator.create(seed, settings.rockLayerSettings(), regionGen);
        this.allowExport = allowExport;
        this.coordinatesVisible = coordinatesVisible;
        this.seedVisible = seedVisible;
        this.xCenter = xCenter;
        this.zCenter = zCenter;
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        renderDirtBackground(pGuiGraphics);
        pGuiGraphics.drawCenteredString(font, title, width / 2, 8, 0xFFFFFF);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    @Override
    protected void init() {
        assert minecraft != null;
        final int previewPixels = Math.min(height - 64, width / 2);

        final SingleColumnOptionsList options = new SingleColumnOptionsList(minecraft, (width - previewPixels) / 2 -10, height, 32, height - 32, 25);

        options.add(
                scale = new OptionInstance<>(
                        "tfcgenviewer.preview_world.preview_scale",
                        OptionInstance.noTooltip(),
                        (cation, scale) -> Options.genericValueLabel(
                                cation,
                                Component.translatable(
                                        "tfcgenviewer.preview_world.km",
                                        ImageBuilder.previewSizeKm(scale)
                                )
                        ),
                        new OptionInstance.IntRange(0, 6),
                        Config.defaultPreviewSize.get(),
                        scale -> {}
                ),
                visualizerType = new OptionInstance<>(
                        "tfcgenviewer.preview_world.visualizer_type",
                        OptionInstance.noTooltip(),
                        (caption, task) -> task.getName(),
                        new OptionInstance.Enum<>(visualizers, VisualizerType.CODEC),
                        visualizers.contains(VisualizerType.RIVERS) ? VisualizerType.RIVERS : visualizers.get(0),
                        task -> {}
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
                    public AbstractWidget createButton(Options pOptions, int pX, int pY, int pWidth, Consumer<Boolean> pOnValueChanged) {
                        return Button.builder(PreviewGenerationScreen.APPLY, button -> apply()).bounds(pX, pY, pWidth, 20).build();
                    }
                }
        );

        if (allowExport) {
            options.add(new OptionInstance<>(
                    "button.tfcgenviewer.export",
                    OptionInstance.noTooltip(),
                    (caption, bool) -> caption,
                    OptionInstance.BOOLEAN_VALUES,
                    false,
                    bool -> {}
            ) {
                @Override
                public AbstractWidget createButton(Options pOptions, int pX, int pY, int pWidth, Consumer<Boolean> pOnValueChanged) {
                    return Button.builder(PreviewGenerationScreen.EXPORT, button -> ImageBuilder.exportImage()).bounds(pX, pY, pWidth, 20).build();
                }
            });
        }

        if (seedVisible) {
            options.add(new OptionInstance<>(
                    "button.tfcgenviewer.current_seed",
                    OptionInstance.noTooltip(),
                    (caption, bool) -> caption,
                    OptionInstance.BOOLEAN_VALUES,
                    false,
                    bool -> {}
            ) {
                @Override
                public AbstractWidget createButton(Options pOptions, int pX, int pY, int pWidth, Consumer<Boolean> pOnValueChanged) {
                    final MultiLineTextWidget text = new MultiLineTextWidget(pX, pY, Component.translatable("button.tfcgenviewer.current_seed", seed), font);
                    text.setMaxWidth(pWidth);
                    return text;
                }
            });
        }

        addRenderableWidget(options);

        final int previewLeftEdge = (width - previewPixels) / 2;
        addRenderableWidget(viewPane = new PreviewPane(previewLeftEdge, (height - previewPixels) / 2, previewPixels, font, coordinatesVisible));


        final int rightPos = (width + previewPixels) / 2 + 10;
        addRenderableWidget(infoPane = new InfoPane(rightPos, 32, width - rightPos - 10, height - 64, Component.empty(), font, PreviewGenerationScreen.COMPASS, 64));

        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> {
            minecraft.setScreen(null);
            ImageBuilder.cancelAndClearPreviews();
        }).bounds(previewLeftEdge, height - 28, previewPixels, 20).build());

        apply();
    }

    private void apply() {
        ImageBuilder.build(
                generator,
                visualizerType.get(),
                xCenter,
                zCenter,
                false,
                0, 0, 0,
                scale.get(),
                info -> {
                    infoPane.setMessage(info.rightInfo());
                    viewPane.setInfo(info);
                }
        );
    }

    @Override
    public void tick() {
        super.tick();
        if (viewPane != null) {
            viewPane.tick();
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        ImageBuilder.cancelAndClearPreviews();
    }
}
