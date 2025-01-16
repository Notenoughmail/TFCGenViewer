package com.notenoughmail.tfcgenviewer.util.custom.rock;

import com.google.common.collect.ImmutableList;
import com.notenoughmail.tfcgenviewer.TFCGenViewer;
import com.notenoughmail.tfcgenviewer.util.MutableRockLayerSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class RockSettingsDisplay extends ContainerObjectSelectionList<RockSettingsDisplay.SettingsHolder> {

    public static final ResourceLocation GUI_ELEMENTS = TFCGenViewer.identifier("textures/gui/common_gui_elements.png");

    public static Component
            NO_SPIKE = Component.translatable("tfcgenviewer.rock_editor.no_spike"),
            NO_LOOSE = Component.translatable("tfcgenviewer.rock_editor.no_loose"),
            NO_MOSSY_LOOSE = Component.translatable("tfcgenviewer.rock_editor.no_mossy_loose"),
            CONFIRM  = Component.translatable("tfcgenviewer.rock_editor.confirm");

    private final Map<String, MutableRockLayerSettings.MutableRockSettings> rockSettings;
    private final Font font;
    private final BiConsumer<String, MutableRockLayerSettings.MutableRockSettings> toEditor;

    public RockSettingsDisplay(Minecraft minecraft, int width, int height, Map<String, MutableRockLayerSettings.MutableRockSettings> rockSettings, Font font, BiConsumer<String, MutableRockLayerSettings.MutableRockSettings> toEditor) {
        super(minecraft, width, height, 24, height + 24, 188);
        this.rockSettings = rockSettings;
        this.font = font;
        this.toEditor = toEditor;
        rockSettings.forEach((n, mrs) -> addEntry(new SettingsHolder(n, mrs)));
        setRenderBackground(false);
        setRenderSelection(false);
        setRenderTopAndBottom(false);
    }

    public boolean add(String name, MutableRockLayerSettings.MutableRockSettings mrs) {
        if (rockSettings.containsKey(name)) {
            return true;
        }

        rockSettings.put(name, mrs);
        addEntry(new SettingsHolder(name, mrs));
        return false;
    }

    @Override
    protected void renderBackground(GuiGraphics pGuiGraphics) {
        pGuiGraphics.setColor(0.125F, 0.125F, 0.125F, 1.0F);
        pGuiGraphics.blit(Screen.BACKGROUND_LOCATION, x0 + 5, y0, x1 - 5, y1, x1 - x0 - 10, y1 - y0, 32, 32);
        pGuiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public NarratableEntry.NarrationPriority narrationPriority() {
        return isFocused() ? NarrationPriority.FOCUSED : NarrationPriority.NONE;
    }

    @Nullable
    public static ItemStack getBlockStack(Block block) {
        if (block instanceof LiquidBlock liquid) {
            final Item bucket = liquid.getFluid().getSource().getBucket();
            if (bucket != null && bucket != Items.AIR) {
                return bucket.getDefaultInstance();
            }
        }
        try {
            final Item item = block.asItem(); // The inserted forge call for extensions can throw an error, joy
            if (item != null && item != Items.AIR) {
                return item.getDefaultInstance();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @Override
    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() - 12;
    }

    class SettingsHolder extends ContainerObjectSelectionList.Entry<SettingsHolder> {

        private final MutableRockLayerSettings.MutableRockSettings mrs;
        private final Button delete, edit;
        private final Component title;
        private final Block[] simpleRenders;

        public SettingsHolder(String rockName, MutableRockLayerSettings.MutableRockSettings mrs) {
            this.mrs = mrs;
            delete = new ImageButton(0, 0, 20, 20, 0, 0, 20, GUI_ELEMENTS, 64, 64, b -> {
                rockSettings.remove(rockName);
                removeEntry(this);
                setScrollAmount(getScrollAmount());
            });
            delete.setTooltip(Tooltip.create(Component.translatable("tfcgenviewer.rock_editor.delete_tooltip.named", rockName)));
            edit = new ImageButton(0, 0, 20, 20, 20, 0, 20, GUI_ELEMENTS, 64, 64, b -> {
                rockSettings.remove(rockName);
                removeEntry(this);
                toEditor.accept(rockName, mrs);
                setScrollAmount(getScrollAmount());
            });
            edit.setTooltip(Tooltip.create(Component.translatable("tfcgenviewer.rock_editor.edit_tooltip", rockName)));
            title = Component.literal(rockName);
            simpleRenders = new Block[] {
                    mrs.raw,
                    mrs.hardened,
                    mrs.gravel,
                    mrs.cobble,
                    mrs.sand,
                    mrs.sandstone
            };
        }

        @Override
        public void render(GuiGraphics graphics, int index, int y, int x, int w, int h, int mouseX, int mouseY, boolean pHovering, float pPartialTick) {
            delete.setX(x);
            delete.setY(y);
            delete.render(graphics, mouseX, mouseY, pPartialTick);
            edit.setX(x + 24);
            edit.setY(y);
            edit.render(graphics, mouseX, mouseY, pPartialTick);
            final int
                    textX = x + 24,
                    blockX = x + 2;
            text(title, textX + 24, y + 2, graphics);
            y += 21;
            for (Block block : simpleRenders) {
                renderBlock(block, blockX, y, graphics);
                y += 18;
            }
            if (mrs.spike == null) {
                text(NO_SPIKE, textX, y + 2, graphics);
            } else {
                renderBlock(mrs.spike, blockX, y, graphics);
            }
            y += 18;
            if (mrs.loose == null) {
                text(NO_LOOSE, textX, y + 2, graphics);
            } else {
                renderBlock(mrs.loose, blockX, y, graphics);
            }
            y += 18;
            if (mrs.mossyLoose == null) {
                text(NO_MOSSY_LOOSE, textX, y + 2, graphics);
            } else {
                renderBlock(mrs.mossyLoose, blockX, y , graphics);
            }
        }

        private void text(Component text, int x, int y, GuiGraphics graphics) {
            graphics.drawString(font, text, x, y + 3, 0xFFFFFFFF);
        }

        private void renderBlock(Block block, int x, int y, GuiGraphics graphics) {
            @Nullable
            final ItemStack stack = getBlockStack(block);
            if (stack != null) {
                graphics.renderFakeItem(stack, x, y + 3);
            } else {
                graphics.blit(GUI_ELEMENTS, x ,y, 0, 0, 40, 20, 20, 64, 64);
            }
            text(block.getName(), x + 22, y + 4, graphics);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of(delete, edit);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(delete, edit);
        }
    }
}
