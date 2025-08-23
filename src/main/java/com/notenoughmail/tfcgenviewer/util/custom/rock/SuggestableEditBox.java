package com.notenoughmail.tfcgenviewer.util.custom.rock;

import com.notenoughmail.tfcgenviewer.util.WidgetUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

// TODO: 1.5.0 | Currently, the suggestion index can be outside of
public class SuggestableEditBox extends EditBox {

    private int selectedIndex = -1;
    private List<MutableComponent> allSuggestions;
    private List<MutableComponent> search;
    private Component[] renderCache = new Component[0];
    private final Minecraft mc;

    public SuggestableEditBox(Font pFont, int pX, int pY, int pWidth, int pHeight, Component pMessage, Collection<String> suggestions, Minecraft mc) {
        super(pFont, pX, pY, pWidth, pHeight, pMessage);
        setHint(pMessage);
        allSuggestions = suggestions.stream().sorted().map(Component::literal).toList();
        search = List.copyOf(allSuggestions);
        this.mc = mc;
        setResponder(null);
    }

    @Override
    public void setResponder(@Nullable Consumer<String> responder) {
        Consumer<String> res = s -> updateSuggestionIndex();
        if (responder != null) {
            res = res.andThen(responder);
        }
        super.setResponder(res);
    }

    public void setSuggestions(Collection<String> allSuggestions) {
        this.allSuggestions = allSuggestions.stream().sorted().map(Component::literal).toList();
    }

    private void nudgeIndex(boolean adding) {
        if (selectedIndex != -1) {
            if (adding) {
                selectedIndex++;
            } else {
                selectedIndex--;
            }
            if (selectedIndex < 0) {
                selectedIndex = search.size() - 1;
            } else if (selectedIndex >= search.size()) {
                selectedIndex = 0;
            }
            updateRenderCache();
        }
    }

    private boolean suggestionsPosition() {
        assert mc.screen != null;
        return mc.screen.height - (getY() + getHeight()) < getY();
    }

    @Override
    public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        if (isFocused() && renderCache.length != 0) {
            pGuiGraphics.pose().pushPose();
            pGuiGraphics.pose().translate(0.0F, 0.0F, 200F);
            final int suggestionHeight = renderCache.length * 9 + 2;
            if (suggestionsPosition()) {
                final int top = getY() - suggestionHeight;
                renderSuggestions(getX(), getX() + width, top - 1, top + suggestionHeight, pGuiGraphics);
            } else {
                final int top = getY() + height;
                renderSuggestions(getX(), getX() + width, top, top + suggestionHeight + 1, pGuiGraphics);
            }
            pGuiGraphics.pose().popPose();
        }
    }

    private void renderSuggestions(int left, int right, int top, int bottom, GuiGraphics graphics) {
        graphics.fill(left, top, right, bottom, 0xFFFFFFFF);
        graphics.fill(left + 1, top + 1 , right - 1 , bottom - 1, 0xFF000000);
        for (int i = 0 ; i < renderCache.length ; i++) {
            final Component text = renderCache[i];
            final int j = top + 1 + (i * 9);
            if (i % 2 == 0) {
                graphics.fill(left + 1, j, right - 1, j + 9, 0xFF1F1F1F);
            }
            renderScrollingString(graphics, font, text, left + 2, j, right - 2, j + 9, 0xFFFFFFFF);
        }
    }

    private void updateRenderCache() {
        renderCache = WidgetUtils.wrapList(
                search,
                (m, selected) -> selected ? m.plainCopy().withStyle(ChatFormatting.GOLD) : m,
                selectedIndex,
                Component[]::new
        );
    }

    @Override
    public void setFocused(boolean pFocused) {
        super.setFocused(pFocused);
        if (pFocused) {
            updateSuggestionIndex();
        } else {
            selectedIndex = -1;
        }
    }

    private void updateSuggestionIndex() {
        search = allSuggestions.stream()
                .filter(c -> c.getString().toLowerCase(Locale.ROOT).startsWith(getValue().toLowerCase(Locale.ROOT)))
                .toList();
        if (getValue().isEmpty()) {
            selectedIndex = 0;
        } else {
            search.stream()
                    .findFirst()
                    .ifPresentOrElse(m -> selectedIndex = search.indexOf(m), () -> selectedIndex = -1);
        }
        updateRenderCache();
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (Screen.hasShiftDown()) {
            return super.keyPressed(pKeyCode, pScanCode, pModifiers);
        }
        if (canConsumeInput()) {
            return switch (pKeyCode) {
                case GLFW.GLFW_KEY_DOWN -> {
                    nudgeIndex(true);
                    yield true;
                }
                case GLFW.GLFW_KEY_UP -> {
                    nudgeIndex(false);
                    yield true;
                }
                case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> {
                    if (selectedIndex != -1) {
                        setValue(search.get(selectedIndex).getString());
                        selectedIndex = -1;
                    }
                    setFocused(false);
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
}
