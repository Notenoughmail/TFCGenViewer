package com.notenoughmail.tfcgenviewer.util.custom.rock;

import com.google.common.collect.ImmutableList;
import com.notenoughmail.tfcgenviewer.screen.PreviewGenerationScreen;
import com.notenoughmail.tfcgenviewer.util.MutableRockLayerSettings;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.rock.LooseRockBlock;
import net.dries007.tfc.common.blocks.rock.RockSpikeBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class RockSettingsEditor extends ContainerObjectSelectionList<RockSettingsEditor.Entry> {

    public static final Component
            NAME_HINT = Component.translatable("tfcgenviewer.rock_editor.hint.edit_rock_settings_name").withStyle(ChatFormatting.DARK_GRAY),
            EMPTY_ROCK_NAME = Component.translatable("tfcgenviewer.rock_editor.error.empty_rock_name"),
            RAW_HINT = Component.translatable("tfcgenviewer.rock_editor.hint.raw_selection").withStyle(ChatFormatting.DARK_GRAY),
            HARDENED_HINT = Component.translatable("tfcgenviewer.rock_editor.hint.hardened_selection").withStyle(ChatFormatting.DARK_GRAY),
            GRAVEL_HINT = Component.translatable("tfcgenviewer.rock_editor.hint.gravel_selection").withStyle(ChatFormatting.DARK_GRAY),
            COBBLE_HINT = Component.translatable("tfcgenviewer.rock_editor.hint.cobble_selection").withStyle(ChatFormatting.DARK_GRAY),
            SAND_HINT = Component.translatable("tfcgenviewer.rock_editor.hint.sand_selection").withStyle(ChatFormatting.DARK_GRAY),
            SANDSTONE_HINT = Component.translatable("tfcgenviewer.rock_editor.hint.sandstone_selection").withStyle(ChatFormatting.DARK_GRAY),
            SPIKE_HINT = Component.translatable("tfcgenviewer.rock_editor.hint.spike_selection").withStyle(ChatFormatting.DARK_GRAY),
            LOOSE_HINT = Component.translatable("tfcgenviewer.rock_editor.hint.loose_selection").withStyle(ChatFormatting.DARK_GRAY),
            MOSSY_LOOSE_HINT = Component.translatable("tfcgenviewer.rock_editor.hint.mossy_loose_selection").withStyle(ChatFormatting.DARK_GRAY);

    private MutableRockLayerSettings.MutableRockSettings mrs;
    private final EditBox name;
    private final Font font;
    private final BiPredicate<String, MutableRockLayerSettings.MutableRockSettings> save;
    private final Consumer<Component> errorMessage;

    public RockSettingsEditor(Minecraft minecraft, int width, int height, Font font, BiPredicate<String, MutableRockLayerSettings.MutableRockSettings> save, Consumer<Component> errorMessage) {
        super(minecraft, width, height, 24, height + 24, 20);
        name = new EditBox(font, width, 24, width, 20, NAME_HINT);
        name.setHint(NAME_HINT);
        mrs = new MutableRockLayerSettings.MutableRockSettings();
        this.font = font;
        this.save = save;
        this.errorMessage = errorMessage;
        setRenderBackground(false);
        setRenderSelection(false);
        setRenderTopAndBottom(false);
        addEntry(new NameEntry());
        addEntry(new BlockEntry(b -> mrs.raw = b, () -> mrs.raw).setHint(RAW_HINT));
        addEntry(new BlockEntry(b -> mrs.hardened = b, () -> mrs.hardened).setHint(HARDENED_HINT));
        addEntry(new BlockEntry(b -> mrs.gravel = b, () -> mrs.gravel).setHint(GRAVEL_HINT));
        addEntry(new BlockEntry(b -> mrs.cobble = b, () -> mrs.cobble).setHint(COBBLE_HINT));
        addEntry(new BlockEntry(b -> mrs.sand = b, () -> mrs.sand).setHint(SAND_HINT));
        addEntry(new BlockEntry(b -> mrs.sandstone = b, () -> mrs.sandstone).setHint(SANDSTONE_HINT));
        addEntry(new BlockEntry(
                b -> mrs.spike = b,
                b ->
                        b instanceof RockSpikeBlock ||
                        b.getStateDefinition().getProperties().contains(TFCBlockStateProperties.ROCK_SPIKE_PART),
                () -> mrs.spike,
                RockSettingsDisplay.NO_SPIKE
        ).setHint(SPIKE_HINT));
        addEntry(new BlockEntry(
                b -> mrs.loose = b,
                b ->
                        b instanceof LooseRockBlock ||
                        b.getStateDefinition().getProperties().contains(TFCBlockStateProperties.COUNT_1_3),
                () -> mrs.loose,
                RockSettingsDisplay.NO_LOOSE
        ).setHint(LOOSE_HINT));
        addEntry(new BlockEntry(
                b -> mrs.mossyLoose = b,
                b ->
                        b instanceof LooseRockBlock ||
                        b.getStateDefinition().getProperties().contains(TFCBlockStateProperties.COUNT_1_3),
                () -> mrs.mossyLoose,
                RockSettingsDisplay.NO_MOSSY_LOOSE
        ).setHint(MOSSY_LOOSE_HINT));
        addEntry(new SaveEntry());
    }

    @Override
    protected void renderBackground(GuiGraphics pGuiGraphics) {
        pGuiGraphics.setColor(0.125F, 0.125F, 0.125F, 1.0F);
        pGuiGraphics.blit(Screen.BACKGROUND_LOCATION, x0 + 5, y0, x1 - 5, y1, x1 - x0 - 10, y1 - y0, 32, 32);
        pGuiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    protected int getScrollbarPosition() {
        return x0 + super.getScrollbarPosition() - 12;
    }

    public boolean load(String name, MutableRockLayerSettings.MutableRockSettings mrs) {
        if (isOccupied()) {
            errorMessage.accept(LayerDefinitionEditor.EDITOR_OCCUPIED);
            return false;
        }
        this.name.setValue(name);
        this.mrs = mrs;
        return true;
    }

    public void tick() {
        for (Entry e : children()) {
            e.tick();
        }
    }

    private boolean isOccupied() {
        for (Entry e : children()) {
            if (e.isOccupied()) return true;
        }
        return false;
    }

    protected static abstract class Entry extends ContainerObjectSelectionList.Entry<Entry> {

        void tick() {}

        boolean isOccupied() {
            return false;
        }
    }

    // TODO: Delete button that empties the name and clears the mrs
    private class NameEntry extends Entry {

        @Override
        public void render(GuiGraphics pGuiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pHovering, float pPartialTick) {
            name.setX(pLeft + 2);
            name.setY(pTop);
            name.setWidth(pWidth - 4);
            name.setHeight(pHeight);
            name.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        }

        @Override
        void tick() {
            name.tick();
        }

        @Override
        boolean isOccupied() {
            return !name.getValue().isEmpty();
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of(name);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(name);
        }
    }

    // TODO: How can we tell if this is occupied?
    private class BlockEntry extends Entry {

        private static final int maxLength = ForgeRegistries.BLOCKS.getKeys().stream().mapToInt(rl -> rl.toString().length()).max().orElseThrow(); // Something seriously wrong needs to happen for there not to be a max

        private final BlockSelectionWidget input;

        BlockEntry(Consumer<Block> setBlock, Supplier<Block> getBlock) {
            this(setBlock, null, getBlock, null);
        }

        BlockEntry(Consumer<@Nullable Block> setBlock, @Nullable Predicate<Block> filter, Supplier<@Nullable Block> getBlock, @Nullable Component ifBlockIsNullMessage) {
            input = new BlockSelectionWidget(font, 0, 0, width, 20, getBlock, setBlock, minecraft, filter == null ? b -> b != Blocks.VOID_AIR : filter, ifBlockIsNullMessage);
            input.setMaxLength(maxLength);
        }

        BlockEntry setHint(Component hint) {
            input.setHint(hint);
            return this;
        }

        @Override
        public void render(GuiGraphics pGuiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pHovering, float pPartialTick) {
            input.setX(pLeft);
            input.setY(pTop);
            input.setWidth(pWidth);
            input.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        }

        @Override
        void tick() {
            input.tick();
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of(input);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(input);
        }
    }

    private class SaveEntry extends Entry {

        private final Button saveButton = Button.builder(PreviewGenerationScreen.SAVE, b -> {
            if (name.getValue().isEmpty()) {
                errorMessage.accept(EMPTY_ROCK_NAME);
            } else if (save.test(name.getValue(), mrs)) {
                mrs = new MutableRockLayerSettings.MutableRockSettings();
                name.setValue("");
            }
        }).build();

        @Override
        public void render(GuiGraphics pGuiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pHovering, float pPartialTick) {
            saveButton.setX(pLeft);
            saveButton.setY(pTop);
            saveButton.setHeight(pHeight);
            saveButton.setWidth(pWidth);
            saveButton.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of(saveButton);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(saveButton);
        }
    }
}
