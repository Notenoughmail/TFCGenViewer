package io.github.notenoughmail.tfcgenviewer.api.color;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IVisualizerType;
import io.github.notenoughmail.tfcgenviewer.impl.ColorTooltips;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;
import java.util.Optional;

public record ColorGradientDefinition(Gradient gradient, Component name, Optional<List<Component>> tooltips) implements DescribableColor {

    public static final Codec<ColorGradientDefinition> CODEC = RecordCodecBuilder.create(i -> i.group(
            Gradient.CODEC.fieldOf("gradient").forGetter(ColorGradientDefinition::gradient),
            ComponentSerialization.CODEC.fieldOf("name").forGetter(ColorGradientDefinition::name),
            ComponentSerialization.CODEC.listOf().optionalFieldOf("tooltips").forGetter(ColorGradientDefinition::tooltips)
    ).apply(i, ColorGradientDefinition::new));

    private static final double[] SAMPLES = { 0D, 0.2D, 0.4D, 0.6D, 0.8D, 1D };

    @Override
    public void appendTo(MutableComponent text, boolean end) {
        final MutableComponent colors = Component.empty();
        for (double d : SAMPLES) {
            colors.append(colorBlock(gradient.applyAsArgb(d)));
        }
        text.append(Component.translatable(
                "tfcgenviewer.color_key_template",
                colors,
                name
        ));
        if (!end) text.append(CommonComponents.NEW_LINE);
    }

    public int color(double value, ColorTooltips tooltips) {
        value = Math.clamp(value, 0D, 1D);
        final int color = gradient.applyAsAbgr(value);
        if (!tooltips.containsKey(color)) {
            final double v = value;
            tooltips.put(color, this.tooltips.map(l -> switch (l.size()) {
                case 0 -> null;
                case 1 -> l.getFirst();
                case 2 -> v > 0.5D ? l.getLast() : l.getFirst();
                default -> l.get(Gradient.index(v, l.size()));
            }).orElse(name));
        }
        return color;
    }

    public int color(double value, IVisualizerType.DrawInfo<?, ?, ?, ?> info) {
        return color(value, info.colorTooltips());
    }
}
