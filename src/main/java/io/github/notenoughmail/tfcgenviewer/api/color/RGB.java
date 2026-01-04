package io.github.notenoughmail.tfcgenviewer.api.color;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.FastColor;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public record RGB(int[] storage) {

    static final Codec<RGB> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.intRange(0, 255).fieldOf("r").forGetter(RGB::r),
            Codec.intRange(0, 255).fieldOf("g").forGetter(RGB::g),
            Codec.intRange(0, 255).fieldOf("b").forGetter(RGB::b)
    ).apply(i, RGB::new));

    public RGB(int r, int g, int b) {
        this(new int[]{r, g, b, 0, -1, -1});
    }

    int r() {
        return storage[0];
    }

    int g() {
        return storage[1];
    }

    int b() {
        return storage[2];
    }

    void compute() {
        if (storage[3] == 0) {
            storage[4] = FastColor.ABGR32.color(255, b(), g(), r());
            storage[5] = FastColor.ARGB32.color(255, r(), g(), b());
        }
    }

    int abgr() {
        compute();
        return storage[4];
    }

    int argb() {
        compute();
        return storage[5];
    }
}
