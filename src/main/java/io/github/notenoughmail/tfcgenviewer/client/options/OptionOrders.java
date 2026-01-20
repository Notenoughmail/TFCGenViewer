package io.github.notenoughmail.tfcgenviewer.client.options;

import com.mojang.serialization.Codec;
import io.github.notenoughmail.tfcgenviewer.api.widget.OptionProvider;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import it.unimi.dsi.fastutil.doubles.DoubleConsumer;
import it.unimi.dsi.fastutil.ints.IntConsumer;
import net.minecraft.client.OptionInstance;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.DoubleFunction;
import java.util.function.ToDoubleFunction;

public record OptionOrders(Consumer<OptionInstance<?>> order) implements OptionProvider {

    @Override
    public <T> Order<T> order(String name, T initial, List<T> values, Codec<T> codec, Consumer<T> onChange) {
        return OptionOrder.list(name, initial, values, codec, onChange, order);
    }

    @Override
    public Order<Boolean> orderBool(String name, boolean initial, BooleanConsumer onChange) {
        return OptionOrder.bool(name, initial, onChange, order);
    }

    @Override
    public <T extends Comparable<T>> Order<T> order(String name, T initial, T min, T max, Codec<T> codec, ToDoubleFunction<T> toSlider, DoubleFunction<T> fromSlider, Consumer<T> onChange) {
        return OptionOrder.comparable(name, initial, min, max, codec, toSlider, fromSlider, onChange, order);
    }

    @Override
    public Order<Integer> orderInt(String name, int initial, int min, int max, IntConsumer onChange) {
        return OptionOrder.integer(name, initial, min, max, onChange, order);
    }

    @Override
    public Order<Double> orderDouble(String name, double initial, double min, double max, DoubleConsumer onChange) {
        return OptionOrder.doub(name, initial, min, max, onChange, order);
    }
}
