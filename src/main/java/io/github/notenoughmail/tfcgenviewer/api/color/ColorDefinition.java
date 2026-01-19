package io.github.notenoughmail.tfcgenviewer.api.color;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IVisualizerType;
import io.github.notenoughmail.tfcgenviewer.impl.ColorTooltips;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FastColor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * A named color with a tooltip that may be sorted
 * @param name The name of the color
 * @param sort The sort position if the color is used in a {@link io.github.notenoughmail.tfcgenviewer.api.color.manager.ColorManager ColorManager}'s default color key
 */
public record ColorDefinition(@ApiStatus.Internal RGB color, Component name, int sort, @ApiStatus.Internal Optional<Component> tooltip) implements Comparable<ColorDefinition>, DescribableColor {

    public static final int DEFAULT_SORT = 100;

    public static final Codec<ColorDefinition> CODEC = RecordCodecBuilder.create(i -> i.group(
            RGB.CODEC.fieldOf("color").forGetter(ColorDefinition::color),
            ComponentSerialization.CODEC.fieldOf("name").forGetter(ColorDefinition::name),
            Codec.INT.lenientOptionalFieldOf("sort", DEFAULT_SORT).forGetter(ColorDefinition::sort),
            ComponentSerialization.CODEC.optionalFieldOf("tooltip").forGetter(ColorDefinition::tooltip)
    ).apply(i, ColorDefinition::new));

    public static ColorDefinition of(int r, int g, int b, Component name, int sort, @Nullable Component tooltip) {
        return new ColorDefinition(new RGB(r, g, b), name, sort, Optional.ofNullable(tooltip));
    }

    public static ColorDefinition of(int r, int g, int b, Component name, @Nullable Component tooltip) {
        return of(r, g, b, name, DEFAULT_SORT, tooltip);
    }

    public static ColorDefinition of(int r, int g, int b, Component name, int sort) {
        return of(r, g, b, name, sort, null);
    }

    public static ColorDefinition of(int r, int g, int b, Component name) {
        return of(r, g, b, name, DEFAULT_SORT);
    }

    @Override
    public int compareTo(@NotNull ColorDefinition o) {
        final int sorted = Integer.compare(sort, o.sort);
        return sorted == 0 ? name.getString().compareTo(o.name.getString()) : sorted;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ColorDefinition def) {
            return abgr() == def.abgr() && name.equals(def.name) && tooltip.equals(def.tooltip);
        }
        return false;
    }

    /**
     * Get the tooltip of the color, should be used instead of {@link #tooltip()}
     */
    public Component getTooltip() {
        return tooltip.orElse(name);
    }

    /**
     * The color in packed ABGR form, with an alpha of 255
     */
    public int abgr() {
        return color.abgr();
    }

    /**
     * The color in packed ABGR form, with the specified alpha
     * @param forcedAlpha The alpha value in the range [0, 255]
     */
    public int abgr(int forcedAlpha) {
        return FastColor.ABGR32.color(forcedAlpha, abgr());
    }

    /**
     * The color in packed ARGB form, with a nalpha of 255
     */
    public int argb() {
        return color.argb();
    }

    @Override
    public void appendTo(MutableComponent text, boolean end) {
        text.append(Component.translatable(
                "tfcgenviewer.color_key_template",
                colorBlock(argb()),
                name
        ));
        if (!end) text.append(CommonComponents.NEW_LINE);
    }

    /**
     * Add this color to the given {@link ColorTooltips}
     */
    public void addTooltip(ColorTooltips tooltips) {
        tooltips.putIfAbsent(abgr(), getTooltip());
    }

    /**
     * Add this color to the color tooltips of the given {@link io.github.notenoughmail.tfcgenviewer.api.visualizer.IVisualizerType.DrawInfo DrawInfo}
     */
    public void addTooltip(IVisualizerType.DrawInfo<?, ?, ?, ?> info) {
        addTooltip(info.colorTooltips());
    }
}
