package com.notenoughmail.tfcgenviewer.util.custom;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class ButtonOption extends OptionInstance<Boolean> {

    private final Component text;
    private final Button.OnPress onPress;

    public ButtonOption(String pCaption, Component text, Button.OnPress onPress) {
        super(pCaption, noTooltip(), (caption, bool) -> caption, BOOLEAN_VALUES, false, bool -> {});
        this.text = text;
        this.onPress = onPress;
    }

    @Override
    public AbstractWidget createButton(Options pOptions, int pX, int pY, int pWidth, Consumer<Boolean> pOnValueChanged) {
        return Button.builder(text, onPress).bounds(pX, pY, pWidth, 20).build();
    }
}
