package io.github.notenoughmail.tfcgenviewer.api.color;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IVisualizerType;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record ColorDefinition(@ApiStatus.Internal RGB color, Component name, int sort, @ApiStatus.Internal Optional<Component> tooltip) implements Comparable<ColorDefinition>, IDescribeColor {

    public static final Codec<ColorDefinition> CODEC = RecordCodecBuilder.create(i -> i.group(
            RGB.CODEC.fieldOf("color").forGetter(ColorDefinition::color),
            ComponentSerialization.CODEC.fieldOf("name").forGetter(ColorDefinition::name),
            Codec.INT.lenientOptionalFieldOf("sort", 100).forGetter(ColorDefinition::sort),
            ComponentSerialization.CODEC.optionalFieldOf("tooltip").forGetter(ColorDefinition::tooltip)
    ).apply(i, ColorDefinition::new));

    public static ColorDefinition of(int r, int g, int b, Component name, int sort, @Nullable Component tooltip) {
        return new ColorDefinition(new RGB(r, g, b), name, sort, Optional.ofNullable(tooltip));
    }

    public static ColorDefinition of(int r, int g, int b, Component name, @Nullable Component tooltip) {
        return of(r, g, b, name, 100, tooltip);
    }

    @Override
    public int compareTo(@NotNull ColorDefinition o) {
        final int sorted = Integer.compare(sort, o.sort);
        return sorted == 0 ? name.getString().compareTo(o.name.getString()) : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ColorDefinition def) {
            return color == def.color && name.equals(def.name) && tooltip.equals(def.tooltip);
        }
        return false;
    }

    public Component getTooltip() {
        return tooltip.orElse(name);
    }

    public int abgr() {
        return color.abgr();
    }

    public int argb() {
        return color.argb();
    }

    @Override
    public void appendTo(MutableComponent text, boolean end) {
        text.append(Component.translatable(
                "tfcgenviewer.preview_world.color_key_template",
                colorBlock(argb()),
                name
        ));
        if (!end) text.append(CommonComponents.NEW_LINE);
    }

    public void addTooltip(Int2ObjectOpenHashMap<Component> tooltips) {
        tooltips.putIfAbsent(abgr(), getTooltip());
    }

    public void addTooltip(IVisualizerType.DrawInfo<?, ?, ?, ?> info) {
        addTooltip(info.colorDescriptors());
    }
}
