package io.github.notenoughmail.tfcgenviewer.api.network;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Predicate;

public class NetworkHolder<T> extends Holder.Reference<T> {

    public static <T> NetworkHolder<T> of(ResourceKey<T> key, T value) {
        return new NetworkHolder<>(key, value);
    }

    public static <T> Codec<Holder<T>> codec(
            ResourceKey<? extends Registry<T>> registry,
            Codec<T> elementCodec,
            Predicate<Holder<T>> serializationFilter,
            T defaultDeserialization
    ) {
        final MapCodec<ResourceKey<T>> keyCodec = ResourceKey.codec(registry).fieldOf("k");
        final MapCodec<T> elm = elementCodec.fieldOf("t");

        return Codec.of(new Encoder<>() {
            @Override
            public <S> DataResult<S> encode(Holder<T> input, DynamicOps<S> ops, S prefix) {
                final RecordBuilder<S> builder = ops.mapBuilder();
                keyCodec.encode(input.getKey(), ops, builder);
                if (serializationFilter.test(input)) {
                    elm.encode(input.value(), ops, builder);
                }
                return builder.build(prefix);
            }
        }, new Decoder<>() {
            @Override
            public <S> DataResult<Pair<Holder<T>, S>> decode(DynamicOps<S> ops, S input) {
                return ops.getMap(input)
                        .flatMap(m -> keyCodec.decode(ops, m)
                                .flatMap(k -> DataResult.success(NetworkHolder.of(
                                        k,
                                        elm.decode(ops, m).mapOrElse(
                                                Function.identity(),
                                                absent -> defaultDeserialization
                                        )))
                                )
                        )
                        .map(h -> Pair.of(h, input));
            }
        });
    }

    protected NetworkHolder(@Nullable ResourceKey<T> key, @Nullable T value) {
        super(Type.INTRUSIVE, Universal.owner(), key, value);
    }
}
