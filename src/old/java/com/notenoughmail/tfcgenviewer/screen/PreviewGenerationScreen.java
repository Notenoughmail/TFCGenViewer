package com.notenoughmail.tfcgenviewer.screen;

import com.notenoughmail.tfcgenviewer.util.custom.*;
import io.github.notenoughmail.tfcgenviewer.client.widget.ButtonOption;
import io.github.notenoughmail.tfcgenviewer.client.widget.InfoPane;
import io.github.notenoughmail.tfcgenviewer.client.widget.PreviewPane;
import io.github.notenoughmail.tfcgenviewer.client.widget.SingleColumnOptionsList;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.dries007.tfc.world.chunkdata.RegionChunkDataGenerator;
import net.dries007.tfc.world.region.RegionGenerator;
import net.dries007.tfc.world.settings.RockLayerSettings;
import net.dries007.tfc.world.settings.Settings;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@Deprecated
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PreviewGenerationScreen extends Screen {

    public static final Component
            TITLE = Component.translatable("tfcgenviewer.preview_world.title"),
            INVALID_GENERATOR = Component.translatable("tfcgenviewer.preview_world.invalid_generator"),
            APPLY = Component.translatable("button.tfcgenviewer.apply"),
            SAVE = Component.translatable("button.tfcgenviewer.save"),
            EDIT_ROCKS = Component.translatable("button.tfcgenviewer.edit_rocks");
    public static final ResourceLocation COMPASS = null;

    private final CreateWorldScreen parent;
    @Nullable
    private final TFCChunkGenerator generator;
    private final RegistryAccess registryAccess;
    @Nullable
    private RegionChunkDataGenerator regionGenerator;
    private Settings worldSettings;
    private long seedInUse;
    private String editorSeed, localSeed;
    private Button seedbutton;
    private InfoPane infoPane;
    private PreviewPane previewPane;

    // TODO: 1.21.1 | Rework to support registering other subclasses of CGEs
    public PreviewGenerationScreen(CreateWorldScreen parent) {
        super(TITLE);
        this.parent = parent;
        final WorldCreationUiState uiState = parent.getUiState();
        editorSeed = localSeed = uiState.getSeed();
        final WorldCreationContext settings = uiState.getSettings();
        generator = settings.selectedDimensions().overworld() instanceof TFCChunkGenerator ext ? ext : null;
        worldSettings = generator == null ? null : generator.settings();
        regionGenerator = getRegionGenerator();
        registryAccess = parent.getUiState().getSettings().worldgenLoadContext();
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
        super.onClose();
        assert minecraft != null;
        minecraft.setScreen(parent);
    }

    @Override
    public void removed() {

    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    protected void init() {
        assert minecraft != null;
        final int previewPixels = Math.min(height - 64, width / 2);

        if (generator != null) {

            final SingleColumnOptionsList options = new SingleColumnOptionsList(minecraft, (width - previewPixels) / 2 - 10, height, 32, height - 32, 25);

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
            }).bounds((width - previewPixels) / 2 - 90, height - 28, 80, 20).build());
            addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> {
                minecraft.setScreen(parent);
            }).bounds((width + previewPixels) / 2 + 10, height - 28, 80, 20).build());
        } else {
            addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, button -> minecraft.setScreen(parent)).bounds(width / 2 - 30, height - 28, 60, 20).build());
        }
    }

    private void applyUpdates(boolean local) {
    }

    public void setRocks(RockLayerSettings rocks) {
        this.rocks = rocks;
    }
}
