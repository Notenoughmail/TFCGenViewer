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
import org.lwjgl.glfw.GLFW;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class SuggestableEditBox extends EditBox {

    private int selectedIndex = -1;
    private List<MutableComponent> suggestions;
    private final Minecraft mc;

    public SuggestableEditBox(Font pFont, int pX, int pY, int pWidth, int pHeight, Component pMessage, Collection<String> suggestions, Minecraft mc) {
        super(pFont, pX, pY, pWidth, pHeight, pMessage);
        setHint(pMessage);
        this.suggestions = suggestions.stream().sorted().map(Component::literal).toList();
        this.mc = mc;
    }

    public void setSuggestions(Collection<String> suggestions) {
        this.suggestions = suggestions.stream().sorted().map(Component::literal).toList();
    }

    private void nudgeIndex(boolean adding) {
        if (selectedIndex != -1) {
            if (adding) {
                selectedIndex++;
            } else {
                selectedIndex--;
            }
            if (selectedIndex < 0) {
                selectedIndex = suggestions.size() - 1;
            } else if (selectedIndex >= suggestions.size()) {
                selectedIndex = 0;
            }
        }
    }

    private boolean suggestionsPosition() {
        assert mc.screen != null;
        return mc.screen.height - (getY() + getHeight()) < getY();
    }

    @Override
    public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        if (isFocused()) {
            final List<Component> suggestionsToRender = suggestionsAroundIndex();
            if (!suggestionsToRender.isEmpty()) {
                pGuiGraphics.pose().pushPose();
                pGuiGraphics.pose().translate(0.0F, 0.0F, 200F);
                final int suggestionHeight = suggestionsToRender.size() * 9 + 2;
                if (suggestionsPosition()) {
                    final int top = getY() - suggestionHeight;
                    renderSuggestions(getX(), getX() + width, top - 1, top + suggestionHeight, suggestionsToRender, pGuiGraphics);
                } else {
                    final int top = getY() + height;
                    renderSuggestions(getX(), getX() + width, top, top + suggestionHeight + 1, suggestionsToRender, pGuiGraphics);
                }
                pGuiGraphics.pose().popPose();
            }
        }
    }

    private void renderSuggestions(int left, int right, int top, int bottom, List<Component> suggestions, GuiGraphics graphics) {
        graphics.fill(left, top, right, bottom, 0xFFFFFFFF);
        graphics.fill(left + 1, top + 1 , right - 1 , bottom - 1, 0xFF000000);
        for (int i = 0 ; i < suggestions.size() ; i++) {
            final Component text = suggestions.get(i);
            final int j = top + 1 + (i * 9);
            renderScrollingString(graphics, font, text, left + 2, j, right - 2, j + 9, 0xFFFFFFFF);
        }
    }

    private List<Component> suggestionsAroundIndex() {
        return WidgetUtils.wrapList(suggestions, (m, selected) -> selected ? m.plainCopy().withStyle(ChatFormatting.GOLD) : m, selectedIndex);
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
        if (getValue().isEmpty()) {
            selectedIndex = 0;
        } else {
            suggestions.stream()
                    .filter(c -> c.getString().toLowerCase(Locale.ROOT).startsWith(getValue().toLowerCase(Locale.ROOT)))
                    .findFirst()
                    .ifPresentOrElse(m -> selectedIndex = suggestions.indexOf(m), () -> selectedIndex = 0);
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
                    nudgeIndex(true);
                    yield true;
                }
                case GLFW.GLFW_KEY_UP -> {
                    nudgeIndex(false);
                    yield true;
                }
                case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> {
                    if (selectedIndex != -1) {
                        setValue(suggestions.get(selectedIndex).getString());
                        setFocused(false);
                        selectedIndex = -1;
                    }
                    yield true;
                }
                default -> super.keyPressed(pKeyCode, pScanCode, pModifiers);
            };
        }
        return false;
    }

    @Override
    public boolean charTyped(char pCodePoint, int pModifiers) {
        final boolean t = super.charTyped(pCodePoint, pModifiers);
        if (t) updateSuggestionIndex();
        return t;
    }
}
