package io.github.notenoughmail.tfcgenviewer.api.widget;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import it.unimi.dsi.fastutil.doubles.DoubleConsumer;
import it.unimi.dsi.fastutil.ints.IntConsumer;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.*;

public interface OptionRequest {

    // CYCLE BUTTON
    <T> Order<T> order(String name, T initial, List<T> values, Codec<T> codec, Consumer<T> onChange);

    Order<Boolean> orderBool(String name, boolean initial, BooleanConsumer onChange);

    // SLIDER
    <T extends Comparable<T>> Order<T> order(String name, T initial, T min, T max, Codec<T> codec, ToDoubleFunction<T> toSlider, DoubleFunction<T> fromSlider, Consumer<T> onChange);

    Order<Integer> orderInt(String name, int initial, int min, int max, IntConsumer onChange);

    Order<Double> orderDouble(String name, double initial, double min, double max, DoubleConsumer onChange);

    interface Order<T> {
        Order<T> withTooltip(Function<T, Component> tooltipFactory);
        Order<T> withDisplay(BiFunction<Component, T, Component> captionFactory);
    }
}
