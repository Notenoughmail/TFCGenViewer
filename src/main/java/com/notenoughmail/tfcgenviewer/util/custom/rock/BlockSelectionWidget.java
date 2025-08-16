package com.notenoughmail.tfcgenviewer.util.custom.rock;

import com.mojang.blaze3d.systems.RenderSystem;
import com.notenoughmail.tfcgenviewer.TFCGenViewer;
import com.notenoughmail.tfcgenviewer.util.GuiElement;
import com.notenoughmail.tfcgenviewer.util.WidgetUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.BelowOrAboveWidgetTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.searchtree.FullTextSearchTree;
import net.minecraft.client.searchtree.RefreshableSearchTree;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BlockSelectionWidget extends EditBox {

    private final Supplier<Block> getter;
    private final Consumer<Block> setter;
    private final Minecraft mc;
    private final RefreshableSearchTree<Block> searchTree;
    @Nullable
    private final Component ifBlockIsNullMessage;
    private List<Block> searchResults;
    private BlockRender[] renderCache = new BlockRender[0];
    private int selectionIndex = -1;

    public BlockSelectionWidget(Font font, int pX, int pY, int pWidth, int pHeight, Supplier<Block> getter, Consumer<Block> setter, Minecraft mc, FullTextSearchTree<Block> searchTree, @Nullable Component ifBlockIsNullMessage) {
        super(font, pX, pY, pWidth, pHeight, CommonComponents.EMPTY);
        this.getter = getter;
        this.setter = setter;
        this.mc = mc;
        this.searchTree = searchTree;
        this.ifBlockIsNullMessage = ifBlockIsNullMessage;
        setResponder(null);
        setValue("");
    }

    @Override
    public void setResponder(@Nullable Consumer<String> pResponder) {
        Consumer<String> res = s -> {
            try {
                final Block prevSearch = searchResults == null || searchResults.isEmpty() || selectionIndex == -1 ? getter.get() : searchResults.get(selectionIndex);
                searchResults = searchTree.search(s);
                if (ifBlockIsNullMessage != null) {
                    searchResults.add(Blocks.VOID_AIR);
                }
                if (!searchResults.isEmpty()) {
                    int index = searchResults.indexOf(prevSearch);
                    if (index != -1) {
                        selectionIndex = index;
                    } else {
                        index = searchResults.indexOf(getter.get());
                        selectionIndex = index == -1 ? 0 : index;
                    }
                } else {
                    selectionIndex = -1;
                }
                updateSuggestionRendering();
            } catch (Exception e) {
                TFCGenViewer.LOGGER.error("Error encountered during value change", e);
            }
        };
        if (pResponder != null) {
            res = res.andThen(pResponder);
        }
        super.setResponder(res);
    }

    // This is only used for drawing the background and the inner width, everything else uses field access
    @Override
    protected boolean isBordered() {
        return false;
    }

    @Override
    public int getInnerWidth() {
        return width - 8;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        if (isFocused()) {
            graphics.fill(getX(), getY(), getX() + width, getY() + height, 0xFFFFFFFF);
            graphics.fill(getX() + 1, getY() + 1, getX() + width - 1, getY() + height - 1, 0xFF000000);
            super.renderWidget(graphics, pMouseX, pMouseY, pPartialTick);
            if (renderCache.length != 0) {
                graphics.pose().pushPose();
                graphics.pose().translate(0.0F, 0.0F, 200F);
                final int suggestionHeight = renderCache.length * 20 + 2;
                if (suggestionsPosition()) {
                    final int top = getY() - suggestionHeight + 1;
                    renderSuggestions(getX(), getX() + width, top - 1, top + suggestionHeight, graphics);
                } else {
                    final int top = getY() + height - 1;
                    renderSuggestions(getX(), getX() + width, top, top + suggestionHeight + 1, graphics);
                }
                graphics.pose().popPose();
            }
        } else {
            // More or less copied from AbstractButton
            graphics.setColor(1.0F, 1.0F, 1.0F, alpha);
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            graphics.blitNineSliced(WIDGETS_LOCATION, getX(), getY(), getWidth(), getHeight(), 20, 4, 200, 20, 0, 46 + (isHovered() ? 40 : 20));
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

            WidgetUtils.renderBlock(getter.get(), getX() + 2, getY(), graphics, font, getX() + width, ifBlockIsNullMessage);
        }
    }

    private void renderSuggestions(int left, int right, int top, int bottom, GuiGraphics graphics) {
        graphics.fill(left, top, right, bottom,  0xFFFFFFFF);
        graphics.fill(left + 1, top + 1, right - 1, bottom - 1, 0xFF000000);
        for (int i = 0 ; i < renderCache.length ; i++) {
            final BlockRender render = renderCache[i];
            final int j = top + 1 + (i * 20);
            if (i % 2 == 0) {
                graphics.fill(left + 1, j, right - 1, j + 20, 0xFF1F1F1F);
            }
            render.render(graphics, left + 3, j, font, right - 3);
        }
    }

    // True -> above
    private boolean suggestionsPosition() {
        assert mc.screen != null;
        return mc.screen.height - (getY() + getHeight()) < getY();
    }

    private void updateSuggestionRendering() {
        renderCache = WidgetUtils.wrapList(searchResults, (block, selected) -> {
            if (block != Blocks.VOID_AIR) {
                return new BlockRender(block, selected);
            }
            assert ifBlockIsNullMessage != null;
            return new BlockRender(null, selected ? ifBlockIsNullMessage.plainCopy().withStyle(ChatFormatting.GOLD) : ifBlockIsNullMessage, true);
        }, selectionIndex, BlockRender[]::new);
    }

    private void nextBlock(boolean up) {
        if (selectionIndex != -1) {
            final int
                    change = (up ? -1 : 1) * (Screen.hasAltDown() ? 5 : 1),
                    newIndex = selectionIndex + change;

            if (newIndex < 0) {
                selectionIndex = searchResults.size() + newIndex; // Since newIndex is negative, it is in essence the count from the top wanted
            } else if (newIndex >= searchResults.size()) {
                selectionIndex = newIndex - searchResults.size();
            } else {
                selectionIndex = newIndex;
            }
        }
        updateSuggestionRendering();
    }

    @Override
    public void setFocused(boolean pFocused) {
        super.setFocused(pFocused);
        if (pFocused) {
            setValue("");
        } else {
            selectionIndex = -1;
        }
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (Screen.hasShiftDown()) {
            return super.keyPressed(pKeyCode, pScanCode, pModifiers);
        }
        if (canConsumeInput()) {
            return switch (pKeyCode) {
                case GLFW.GLFW_KEY_DOWN -> {
                    nextBlock(false);
                    yield true;
                }
                case GLFW.GLFW_KEY_UP -> {
                    nextBlock(true);
                    yield true;
                }
                case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> {
                    if (selectionIndex != -1) {
                        final Block block = searchResults.get(selectionIndex);
                        setter.accept(block == Blocks.VOID_AIR ? null : block);
                        setFocused(false);
                    }
                    yield true;
                }
                case GLFW.GLFW_KEY_ESCAPE -> {
                    setFocused(false);
                    yield true;
                }
                default -> super.keyPressed(pKeyCode, pScanCode, pModifiers);
            };
        }
        return false;
    }

    @Override
    protected ClientTooltipPositioner createTooltipPositioner() {
        final ClientTooltipPositioner c = super.createTooltipPositioner();
        final boolean mod = c instanceof BelowOrAboveWidgetTooltipPositioner;
        // Force the tooltip to be on the opposite side of the suggestions
        return (screenWidth, screenHeight, mouseX, mouseY, tooltipWidth, tooltipHeight) -> {
            final Vector2ic v = c.positionTooltip(screenWidth, screenHeight, mouseX, mouseY, tooltipWidth, tooltipHeight);
            if (mod && v instanceof Vector2i mut) {
                mut.y = getY() + (suggestionsPosition() ? height - 2 : -tooltipHeight + 1);
            }
            return v;
        };
    }

    private record BlockRender(@Nullable ItemStack rendered, Component text, boolean fullWidthText) {

        BlockRender(Block block, boolean selected) {
            this(WidgetUtils.getBlockStack(block), selected ? block.getName().plainCopy().withStyle(ChatFormatting.GOLD) : block.getName(), false);
        }

        void render(GuiGraphics graphics, int x, int y, Font font, int maxX) {
            if (rendered != null) {
                graphics.renderFakeItem(rendered, x, y + 2);
            } else if (!fullWidthText) {
                GuiElement.UNKNOWN.render(graphics, x - 2, y);
            }

            renderScrollingString(graphics, font, text, x + (fullWidthText ? 2 : 22), y + 5, maxX, y + 17, 0xFFFFFFFF);
        }
    }
}
