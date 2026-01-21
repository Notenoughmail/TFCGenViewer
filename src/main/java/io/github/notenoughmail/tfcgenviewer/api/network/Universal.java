package io.github.notenoughmail.tfcgenviewer.api.network;

import io.github.notenoughmail.tfcgenviewer.TFCGenViewer;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

import java.util.Optional;

public final class Universal {
    public static <T> HolderGetter<T> getter() {
        return TFCGenViewer.cast(GETTER);
    }

    public static <T> HolderOwner<T> owner() {
        return TFCGenViewer.cast(OWNER);
    }

    private static final HolderGetter<?> GETTER = new HolderGetter<>() {
        @Override
        public Optional<Holder.Reference<Object>> get(ResourceKey<Object> resourceKey) {
            return Optional.empty();
        }

        @Override
        public Optional<HolderSet.Named<Object>> get(TagKey<Object> tagKey) {
            return Optional.empty();
        }
    };

    private static final HolderOwner<?> OWNER = new HolderOwner<>() {
        @Override
        public boolean canSerializeIn(HolderOwner<Object> owner) {
            return true;
        }
    };

}
