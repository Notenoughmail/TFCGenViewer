package com.notenoughmail.tfcgenviewer.util.custom;

import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public record SeedValueSet(Font font, BiConsumer<String, EditBox> seedSetter, Supplier<String> defaultValue, Consumer<Runnable> seedTick) implements OptionInstance.ValueSet<String> {

    public static final Component SEED_LABEL = Component.translatable("selectWorld.enterSeed");
    public static final Component SEED_HINT = Component.translatable("selectWorld.seedInfo").withStyle(ChatFormatting.DARK_GRAY);
    private static final Codec<String> CODEC = Codec.STRING;

    @Override
    public Function<OptionInstance<String>, AbstractWidget> createButton(OptionInstance.TooltipSupplier<String> pTooltipSupplier, Options pOptions, int pX, int pY, int pWidth, Consumer<String> pOnValueChanged) {
        return instance -> {
            final EditBox editor = new EditBox(font, pX, pY, pWidth, 20, SEED_LABEL);
            editor.setResponder(s -> seedSetter.accept(s, editor));
            editor.setValue(defaultValue().get());
            editor.setHint(SEED_HINT);
            seedTick.accept(editor::tick);
            return editor;
        };
    }

    @Override
    public Optional<String> validateValue(String pValue) {
        return Optional.of(pValue);
    }

    @Override
    public Codec<String> codec() {
        return CODEC;
    }
}
