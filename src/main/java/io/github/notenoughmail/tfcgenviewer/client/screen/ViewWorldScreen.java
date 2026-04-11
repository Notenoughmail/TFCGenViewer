package io.github.notenoughmail.tfcgenviewer.client.screen;

import io.github.notenoughmail.tfcgenviewer.TFCGenViewer;
import io.github.notenoughmail.tfcgenviewer.api.ColorTooltips;
import io.github.notenoughmail.tfcgenviewer.api.DrawParallelism;
import io.github.notenoughmail.tfcgenviewer.api.scale.IScale;
import io.github.notenoughmail.tfcgenviewer.api.scale.ImageSize;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IGeneratorVisualizer;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IVisualizerType;
import io.github.notenoughmail.tfcgenviewer.client.options.OptionOrders;
import io.github.notenoughmail.tfcgenviewer.client.widget.ButtonOption;
import io.github.notenoughmail.tfcgenviewer.client.widget.InfoPane;
import io.github.notenoughmail.tfcgenviewer.client.widget.PreviewPane;
import io.github.notenoughmail.tfcgenviewer.client.widget.SingleColumnOptionsList;
import io.github.notenoughmail.tfcgenviewer.impl.preview.Image;
import io.github.notenoughmail.tfcgenviewer.impl.preview.Preview;
import net.dries007.tfc.world.ChunkGeneratorExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.extensions.ILevelExtension;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class ViewWorldScreen<
        G extends ChunkGeneratorExtension,
        I extends ImageSize,
        S extends IScale<I>,
        V extends IVisualizerType<G, C, S, O>,
        C,
        O extends IVisualizerType.Options<O>
        > extends Screen {

    private final G generator;
    private final IGeneratorVisualizer<G, I, S, V> visualizer;
    private final State state;
    private final RegistryAccess registryAccess;
    private final long worldSeed;
    private final int xOrigin, zOrigin;
    private final boolean allowSpawnDraw, allowExport, allowCoords;

    private final OptionInstance<V> visualizerType;
    private final OptionInstance<I> imageSize;
    private final OptionInstance<Boolean> spawnOverlay;
    private final OptionInstance<Integer> xOffset, zOffset;
    private final ButtonOption apply, export;

    private final PreviewPane previewPane;
    private final InfoPane infoPane;

    private final Button exit;

    private SingleColumnOptionsList options;

    public ViewWorldScreen(
            G generator,
            IGeneratorVisualizer<G, I, S, V> generatorVisualizer,
            List<V> visualizers,
            RegistryAccess registryAccess,
            boolean allowSpawnDraw,
            boolean allowExport,
            boolean allowCoords,
            long worldSeed,
            int xOrigin,
            int zOrigin
    ) {
        super(Component.translatable(
                "tfcgenviewer.screen.view_world.title",
                Component.translatable(Minecraft.getInstance().level.dimension().location().toLanguageKey(ILevelExtension.TRANSLATION_PREFIX)),
                generatorVisualizer.name()
        ));
        this.generator = generator;
        this.visualizer = generatorVisualizer;
        this.registryAccess = registryAccess;
        state = new State();
        this.worldSeed = worldSeed;
        this.xOrigin = xOrigin;
        this.zOrigin = zOrigin;
        this.allowSpawnDraw = allowSpawnDraw;
        this.allowExport = allowExport;
        this.allowCoords = allowCoords;

        final int offset = visualizer.scale().blocksPerPixel() * visualizer.maximumPreviewOffset();
        visualizerType = Preview.visualizerTypeOption(visualizers, v -> onVisualizerChange());
        imageSize = Preview.imageSizeOption(visualizer.scale());
        spawnOverlay = OptionInstance.createBoolean("tfcgenviewer.screen.preview_world.option.spawn_overlay", false, b -> {});
        xOffset = Preview.kmOption("tfcgenviewer.screen.preview_world.option.x_offset", -offset, offset, 0);
        zOffset = Preview.kmOption("tfcgenviewer.screen.preview_world.option.z_offset", -offset, offset, 0);
        apply = new ButtonOption("tfcgenviewer.button.apply", b -> visualize());
        export = new ButtonOption("tfcgenviewer.button.export", b -> {
            if (state.previousImageProcess.state() == Future.State.SUCCESS) {
                try {
                    state.previousImageProcess.get().export();
                } catch (Throwable e) {
                    throw new IllegalStateException("Impossible state!", e);
                }
            }
        });

        previewPane = new PreviewPane(0, 0, () -> font, allowCoords);
        infoPane = new InfoPane(0, 0, 10, 10, () -> font);
        state.createVizOptions(true);

        exit = Button.builder(
                CommonComponents.GUI_DONE,
                b -> getMinecraft().setScreen(null)
        ).build();

        visualize();
    }

    @Override
    public void onClose() {
        super.onClose();
        state.close();
    }

    @Override
    protected void init() {
        final int previewPixels = Math.min(height - 64, (int) (width * TFCGenViewer.maxPreviewWidth.getAsDouble()));

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

        exit.setRectangle(
                previewPixels,
                20,
                leftPreview,
                height - 26
        );
        addRenderableWidget(exit);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 11, 0xFFFFFFFF);
    }

    private void visualize() {
        state.close();
        previewPane.nowProcessing();

        final int xCenterBlocks = xOffset.get() + xOrigin, zCenterBlocks = zOffset.get() + zOrigin;

        final G gen = visualizer.recreateGenerator(generator);
        final V viz = visualizerType.get();
        final O options = IVisualizerType.Options.copy(state.vizOptions);
        final I imageSize = this.imageSize.get();
        final S scale = visualizer.scale();
        final DrawParallelism parallelism = DrawParallelism.of(options, viz, imageSize);
        final C cache = viz.createCache(registryAccess, gen, imageSize, worldSeed, options, parallelism);
        final IVisualizerType.DrawInfo<G, C, S, O> info = new IVisualizerType.DrawInfo<>(
                gen,
                cache,
                registryAccess,
                ColorTooltips.of(parallelism.parallel()),
                imageSize,
                scale,
                options
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
                        gen.settings()
                ),
                parallelism,
                registryAccess,
                allowCoords
        );
    }

    private void onVisualizerChange() {
        state.createVizOptions(false);
    }

    private void populateOptions() {
        options.children().clear();
        options.add(visualizerType);
        visualizerType.get().addOptions(new OptionOrders(options::addDynamic), state.vizOptions);
        options.add(imageSize);
        if (allowSpawnDraw) options.add(spawnOverlay);
        options.add(apply);
        if (allowExport) options.add(export);
    }

    class State {
        O vizOptions;
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
