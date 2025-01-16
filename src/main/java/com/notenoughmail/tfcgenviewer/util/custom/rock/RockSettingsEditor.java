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
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
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
            BLOCK_ID_HINT = Component.translatable("tfcgenviewer.rock_editor.hint.block_id").withStyle(ChatFormatting.DARK_GRAY),
            BLOCK_ID_HINT_OPTIONAL = Component.translatable("tfcgenviewer.rock_editor.hint.block_id.optional").withStyle(ChatFormatting.DARK_GRAY),
            SAVE_CHANGE = Component.translatable("tfcgenviewer.rock_editor.save_new_block"),
            EMPTY_ROCK_NAME = Component.translatable("tfcgenviewer.rock_editor.error.empty_rock_name"),
            SPIKE_FILTER = Component.translatable("tfcgenviewer.rock_editor.error.spike_block_restriction"),
            LOOSE_FILTER = Component.translatable("tfcgenviewer.rock_editor.error.loose_block_restriction");

    private static final String[] ROCK_BLOCKS = new String[] { "raw", "hardened", "gravel", "cobble", "sand", "sandstone", "spike", "loose", "mossy_loose" };
    public static final Component[] ROCK_BLOCK_NAMES = new Component[9];

    static {
        for (int i = 0 ; i < 9 ; i++) {
            ROCK_BLOCK_NAMES[i] = Component.translatable("tfcgenviewer.rock_editor.rock_block." + ROCK_BLOCKS[i]);
        }
    }

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
        addEntry(new BlockEntry(b -> mrs.raw = b, () -> mrs.raw, ROCK_BLOCK_NAMES[0]));
        addEntry(new BlockEntry(b -> mrs.hardened = b, () -> mrs.hardened, ROCK_BLOCK_NAMES[1]));
        addEntry(new BlockEntry(b -> mrs.gravel = b, () -> mrs.gravel, ROCK_BLOCK_NAMES[2]));
        addEntry(new BlockEntry(b -> mrs.cobble = b, () -> mrs.cobble, ROCK_BLOCK_NAMES[3]));
        addEntry(new BlockEntry(b -> mrs.sand = b, () -> mrs.sand, ROCK_BLOCK_NAMES[4]));
        addEntry(new BlockEntry(b -> mrs.sandstone = b, () -> mrs.sandstone, ROCK_BLOCK_NAMES[5]));
        addEntry(new BlockEntry(
                b -> mrs.spike = b,
                b ->
                        b instanceof RockSpikeBlock ||
                        b.getStateDefinition().getProperties().contains(TFCBlockStateProperties.ROCK_SPIKE_PART),
                () -> mrs.spike,
                SPIKE_FILTER,
                RockSettingsDisplay.NO_SPIKE,
                ROCK_BLOCK_NAMES[6]
        ));
        addEntry(new BlockEntry(
                b -> mrs.loose = b,
                b ->
                        b instanceof LooseRockBlock ||
                        b.getStateDefinition().getProperties().contains(TFCBlockStateProperties.COUNT_1_3),
                () -> mrs.loose,
                LOOSE_FILTER,
                RockSettingsDisplay.NO_LOOSE,
                ROCK_BLOCK_NAMES[7]
        ));
        addEntry(new BlockEntry(
                b -> mrs.mossyLoose = b,
                b ->
                        b instanceof LooseRockBlock ||
                        b.getStateDefinition().getProperties().contains(TFCBlockStateProperties.COUNT_1_3),
                () -> mrs.mossyLoose,
                LOOSE_FILTER,
                RockSettingsDisplay.NO_MOSSY_LOOSE,
                ROCK_BLOCK_NAMES[8]
        ));
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

    public void load(String name, MutableRockLayerSettings.MutableRockSettings mrs) {
        this.name.setValue(name);
        this.mrs = mrs;
    }

    public void tick() {
        for (Entry e : children()) {
            e.tick();
        }
    }

    protected static abstract class Entry extends ContainerObjectSelectionList.Entry<Entry> {

        void tick() {}
    }

    private class NameEntry extends Entry {

        @Override
        public void render(GuiGraphics pGuiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pHovering, float pPartialTick) {
            name.setX(pLeft);
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
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of(name);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(name);
        }
    }

    private class BlockEntry extends Entry {

        private static final int maxLength = ForgeRegistries.BLOCKS.getKeys().stream().mapToInt(rl -> rl.toString().length()).max().orElseThrow(); // Something seriously wrong needs to happen for there not to be a max

        private final ImageButton edit, confirm;
        private boolean editing;
        private final EditBox input;
        @Nullable
        private final Component ifBlockIsNullMessage;
        private final Supplier<@Nullable Block> blockGetter;

        BlockEntry(Consumer<Block> setBlock, Supplier<Block> getBlock, Component rockType) {
            this(setBlock, null, getBlock, null, null, rockType);
        }

        BlockEntry(Consumer<@Nullable Block> setBlock, @Nullable Predicate<Block> filter, Supplier<@Nullable Block> getBlock, @Nullable Component filterFailMessage, @Nullable Component ifBlockIsNullMessage, Component rockType) {
            editing = false;
            final Component c = ifBlockIsNullMessage == null ? BLOCK_ID_HINT_OPTIONAL : BLOCK_ID_HINT;
            input = new EditBox(font, 0, 0, width, 18, c);
            input.setMaxLength(maxLength);
            input.setHint(c);
            edit = new ImageButton(0, 0, 20, 20, 20 ,0, 20, RockSettingsDisplay.GUI_ELEMENTS, 64, 64, b -> {
                editing = !editing;
                if (getBlock.get() != null) {
                    input.setValue(ForgeRegistries.BLOCKS.getKey(getBlock.get()).toString());
                } else {
                    input.setValue("");
                }
            });
            edit.setTooltip(Tooltip.create(Component.translatable("tfcgenviewer.rock_editor.change_block", rockType)));
            confirm = new ImageButton(0, 0, 20, 20, 40, 0, 20, RockSettingsDisplay.GUI_ELEMENTS, 64, 64, b -> {
                final String inputValue = input.getValue();
                final boolean firstCheck = filter == null && inputValue.isEmpty(); // Use the filter as a proxy for being a spike or loose block as they may have an empty input to signify its absence
                if (firstCheck || !ResourceLocation.isValidResourceLocation(inputValue)) {
                    errorMessage.accept(Component.translatable("tfcgenviewer.rock_editor.error.invalid_id", inputValue));
                } else {
                    if (inputValue.isEmpty()) {
                        setBlock.accept(null);
                        editing = false;
                    } else {
                        final ResourceLocation id = ResourceLocation.tryParse(inputValue);
                        assert id != null; // Shouldn't be null since the validity was checked above
                        @Nullable
                        final Block block = ForgeRegistries.BLOCKS.getValue(id);
                        if (block == null || block == Blocks.AIR) {
                            errorMessage.accept(Component.translatable("tfcgenviewer.rock_editor.error.unknown_block", inputValue));
                        } else if(filter != null && !filter.test(block)) {
                            errorMessage.accept(filterFailMessage);
                        } else {
                            setBlock.accept(block);
                            editing = false;
                            setFocused(false);
                        }
                    }
                }
            });
            confirm.setTooltip(Tooltip.create(SAVE_CHANGE));
            this.ifBlockIsNullMessage = ifBlockIsNullMessage;
            blockGetter = getBlock;
        }

        @Override
        public void render(GuiGraphics pGuiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pHovering, float pPartialTick) {
            edit.setX(pLeft + 2);
            edit.setY(pTop);
            edit.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
            if (editing) {
                confirm.active = true;
                confirm.setX(pLeft + 24);
                confirm.setY(pTop);
                confirm.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
                input.active = true;
                input.setX(pLeft + 46);
                input.setY(pTop);
                input.setWidth(pWidth - 48);
                input.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
            } else {
                confirm.active = false;
                input.active = false;
                @Nullable
                final Block block = blockGetter.get();
                if (block != null) {
                    renderBlock(block, pLeft + 24, pTop, pGuiGraphics);
                } else {
                    assert ifBlockIsNullMessage != null;
                    pGuiGraphics.drawString(font, ifBlockIsNullMessage, pLeft + 46, pTop + 6, 0xFFFFFFFF);
                }
            }
        }

        private void renderBlock(Block block, int x, int y, GuiGraphics graphics) {
            @Nullable
            final ItemStack stack = RockSettingsDisplay.getBlockStack(block);
            if (stack != null) {
                graphics.renderFakeItem(stack, x + 2, y + 3);
            } else {
                graphics.blit(RockSettingsDisplay.GUI_ELEMENTS, x ,y, 0, 0, 40, 20, 20, 64, 64);
            }
            graphics.drawString(font, block.getName(), x + 22, y + 8, 0xFFFFFFFF);
        }

        @Override
        void tick() {
            if (editing) input.tick();
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return editing ? ImmutableList.of(edit, confirm, input) : ImmutableList.of(edit);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return editing ? ImmutableList.of(edit, confirm, input) : ImmutableList.of(edit);
        }
    }

    private class SaveEntry extends Entry {

        private final Button saveButton = Button.builder(PreviewGenerationScreen.SAVE, b -> {
            if (name.getValue().isEmpty()) {
                errorMessage.accept(EMPTY_ROCK_NAME);
            } else if (save.test(name.getValue(), mrs)) {
                errorMessage.accept(Component.translatable("tfcgenviewer.rock_editor.error.rock_already_exists", name.getValue()));
            } else {
                mrs = new MutableRockLayerSettings.MutableRockSettings();
                name.setValue("");
            }
        }).build();

        @Override
        public void render(GuiGraphics pGuiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pHovering, float pPartialTick) {
            saveButton.setX(pLeft);
            saveButton.setY(pTop);
            saveButton.setHeight(pHeight);
            saveButton.setWidth(pWidth - 4);
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
