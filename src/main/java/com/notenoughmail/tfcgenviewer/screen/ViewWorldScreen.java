package com.notenoughmail.tfcgenviewer.screen;

import com.mojang.serialization.Lifecycle;
import com.notenoughmail.tfcgenviewer.TFCGenViewer;
import com.notenoughmail.tfcgenviewer.config.Config;
import com.notenoughmail.tfcgenviewer.util.VisualizerType;
import com.notenoughmail.tfcgenviewer.util.custom.ButtonOption;
import com.notenoughmail.tfcgenviewer.util.custom.InfoPane;
import io.github.notenoughmail.tfcgenviewer.client.widget.PreviewPane;
import io.github.notenoughmail.tfcgenviewer.client.widget.SingleColumnOptionsList;
import com.notenoughmail.tfcgenviewer.util.preview.ImageBuilder;
import com.notenoughmail.tfcgenviewer.util.preview.PreviewScale;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.world.chunkdata.RegionChunkDataGenerator;
import net.dries007.tfc.world.region.RegionGenerator;
import net.dries007.tfc.world.settings.Settings;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ViewWorldScreen extends Screen {

    public static final Component TITLE = Component.translatable("tfcgenviewer.view_world.title");

    private final List<VisualizerType> visualizers;
    private final long seed;
    private final RegionChunkDataGenerator generator;
    private final boolean allowExport, coordinatesVisible, seedVisible;
    private final int xCenter, zCenter;
    private final RegistryAccess registryAccess;

    private OptionInstance<PreviewScale> scale;
    private OptionInstance<VisualizerType> visualizerType;
    private PreviewPane viewPane;
    private InfoPane infoPane;

    public ViewWorldScreen(List<VisualizerType> visualizers, long seed, Settings settings, boolean allowExport, boolean coordinatesVisible, boolean seedVisible, int xCenter, int zCenter, Map<ResourceKey<PlacedFeature>, PlacedFeature> serverFeatures, Map<ResourceKey<Biome>, Biome> biomeInformation, Map<TagKey<Biome>, List<ResourceKey<Biome>>> biomeTags) {
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

        final ClientPacketListener connection = Minecraft.getInstance().getConnection();
        assert connection != null; // If someone creates this screen without an active connection 1. What is wrong with you, 2. You're better off recreating this from scratch

        final MappedRegistry<PlacedFeature> featureRegistry = new MappedRegistry<>(Registries.PLACED_FEATURE, Lifecycle.stable());
        serverFeatures.forEach((key, val) -> featureRegistry.register(key, val, Lifecycle.stable()));
        featureRegistry.getOrCreateTag(TFCGenViewer.VISUALIZABLE_FEATURES).bind(Helpers.uncheck(featureRegistry.holders()::toList));

        final MappedRegistry<Biome> biomeRegistry = new MappedRegistry<>(Registries.BIOME, Lifecycle.stable());
        biomeInformation.forEach((key, val) -> biomeRegistry.register(key, val, Lifecycle.stable()));
        biomeRegistry.bindTags(
                TFCGenViewer.ofEntryStream(TFCGenViewer.cast(
                        biomeTags.entrySet().stream()
                                .map(e -> Map.entry(
                                        e.getKey(),
                                        e.getValue().stream()
                                                .map(key -> biomeRegistry.getHolder(key).orElseThrow())
                                                .toList()
                                ))
                ))
        );

        final Map<ResourceKey<? extends Registry<?>>, Registry<?>> map = connection.registryAccess().registries().collect(Collectors.toMap(RegistryAccess.RegistryEntry::key, RegistryAccess.RegistryEntry::value));
        map.put(featureRegistry.key(), featureRegistry);
        map.put(biomeRegistry.key(), biomeRegistry);

        registryAccess = new RegistryAccess.ImmutableRegistryAccess(map).freeze();
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
                scale = PreviewScale.option(),
                visualizerType = VisualizerType.option(visualizers),
                new ButtonOption("button.tfcgenviewer.apply", PreviewGenerationScreen.APPLY, b -> apply())
        );

        if (allowExport) {
            options.add(new ButtonOption("button.tfcgenviewer.export", PreviewGenerationScreen.EXPORT, b -> ImageBuilder.exportImage()));
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
                },
                Config.generationProgress.get() ? viewPane::setProgress : i -> {},
                coordinatesVisible,
                seed,
                registryAccess
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
