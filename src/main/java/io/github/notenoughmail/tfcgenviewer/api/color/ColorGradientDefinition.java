package io.github.notenoughmail.tfcgenviewer.api.color;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;
import java.util.Optional;

public record ColorGradientDefinition(Gradient gradient, Component name, Optional<List<Component>> tooltips) implements IDescribeColor {

    public static final Codec<ColorGradientDefinition> CODEC = RecordCodecBuilder.create(i -> i.group(
            Gradient.CODEC.fieldOf("gradient").forGetter(ColorGradientDefinition::gradient),
            ComponentSerialization.CODEC.fieldOf("name").forGetter(ColorGradientDefinition::name),
            ComponentSerialization.CODEC.listOf()
    ))

    @Override
    public void appendTo(MutableComponent text, boolean end) {

    }

    public int color(double value, Int2ObjectOpenHashMap<Component> tooltips) {
        value = Math.clamp(value, 0D, 1D);
    }
}
