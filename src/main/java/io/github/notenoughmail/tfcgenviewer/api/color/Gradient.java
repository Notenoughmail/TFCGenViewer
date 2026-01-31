package io.github.notenoughmail.tfcgenviewer.api.color;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.notenoughmail.tfcgenviewer.TFCGenViewer;
import io.github.notenoughmail.tfcgenviewer.api.GenViewerAPI;
import io.github.notenoughmail.tfcgenviewer.impl.RGB;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;

import java.util.List;
import java.util.Locale;
import java.util.function.DoubleToIntFunction;
import java.util.function.Function;

import static net.minecraft.util.FastColor.ABGR32.*;

public sealed interface Gradient permits Gradient.Data, Gradient.Preset, Gradient.Static, Gradient.FromTo, Gradient.Dispatch {

    Codec<Gradient> CODEC = Codec.either(
            Preset.CODEC,
            Type.CODEC.dispatch(
                    Gradient::type,
                    t -> t.codec
            )
    ).xmap(
            e -> e.map(Function.identity(), Function.identity()),
            Either::right
    );

    int applyAsAbgr(double value);

    Type type();

    default int applyAsArgb(double value) {
        final int abgr = applyAsAbgr(value);
        return ARGB32.color(
                red(abgr),
                green(abgr),
                blue(abgr)
        );
    }

    static int index(double d, int maxExclusive) {
        return Mth.clamp(Mth.floor(d * maxExclusive), 0, maxExclusive - 1);
    }

    static DoubleToIntFunction lin(int from, int to) {
        final double
                r0 = lin(red(from)),
                r1 = lin(red(to)),
                g0 = lin(green(from)),
                g1 = lin(green(to)),
                b0 = lin(blue(from)),
                b1 = lin(blue(to));
        return value -> color(
                255,
                delin(Mth.lerp(value, b0, b1)),
                delin(Mth.lerp(value, g0, g1)),
                delin(Mth.lerp(value, r0, r1))
        );
    }

    static DoubleToIntFunction multiLin(int... colors) {
        final double[][] lin = new double[colors.length][3];
        for (int i = 0 ; i < colors.length ; i++) {
            final double[] l = lin[i];
            l[0] = lin(red(colors[i]));
            l[1] = lin(green(colors[i]));
            l[2] = lin(blue(colors[i]));
        }
        final int segments = colors.length - 1;
        return d -> {
            final int i = index(d, segments);
            final double[] l0 = lin[i], l1 = lin[i + 1];
            final double delta = (d * segments) % 1;
            return color(
                    255,
                    delin(Mth.lerp(delta, l0[2], l1[2])),
                    delin(Mth.lerp(delta, l0[1], l1[1])),
                    delin(Mth.lerp(delta, l0[0], l1[0]))
            );
        };
    }

    static double lin(int channel) {
        return Math.pow((double) (channel & 0xFF) / 0xFF, 2.2D);
    }

    static int delin(double channel) {
        return 0xFF & (int) (0xFF * Math.pow(channel, 1D / 2.2D));
    }

    record Data(List<RGB> colors, DoubleToIntFunction baked) implements Gradient {

        Data(List<RGB> colors) {
            this(colors, multiLin(colors.stream().mapToInt(RGB::abgr).toArray()));
        }

        @Override
        public int applyAsAbgr(double value) {
            return baked.applyAsInt(value);
        }

        @Override
        public Type type() {
            return Type.LIST;
        }
    }

    non-sealed interface Preset extends Gradient {

        Codec<Preset> CODEC = GenViewerAPI.GRADIENT_REGISTRY.byNameCodec();

        @Override
        default Type type() {
            return Type.PRESET;
        }
    }

    non-sealed interface Dispatch extends Gradient {

        MapCodec<? extends Dispatch> codec();

        @Override
        default Type type() {
            return Type.DISPATCH;
        }
    }

    record Static(RGB color) implements Gradient {

        @Override
        public int applyAsAbgr(double value) {
            return color.abgr();
        }

        @Override
        public int applyAsArgb(double value) {
            return color.argb();
        }

        @Override
        public Type type() {
            return Type.STATIC;
        }
    }

    record FromTo(RGB from, RGB to, DoubleToIntFunction baked) implements Gradient {

        public FromTo(RGB from, RGB to) {
            this(from, to, lin(from.abgr(), to.abgr()));
        }

        @Override
        public int applyAsAbgr(double value) {
            return baked.applyAsInt(value);
        }

        @Override
        public Type type() {
            return Type.FROM_TO;
        }
    }

    enum Type implements StringRepresentable {
        LIST(RGB.CODEC.listOf(3, Integer.MAX_VALUE).xmap(Data::new, Data::colors).fieldOf("colors")),
        PRESET(Preset.CODEC.fieldOf("preset")),
        STATIC(RGB.CODEC.xmap(Static::new, Static::color).fieldOf("color")),
        FROM_TO(RecordCodecBuilder.<FromTo>mapCodec(i -> i.group(
                RGB.CODEC.fieldOf("from").forGetter(FromTo::from),
                RGB.CODEC.fieldOf("to").forGetter(FromTo::to)
        ).apply(i, FromTo::new))),
        DISPATCH(GenViewerAPI.DISPATCH_GRADIENT_REGISTRY.byNameCodec()
                .dispatchMap("dispatch_type", Dispatch::codec, Function.identity())
        )
        ;

        static final Codec<Type> CODEC = StringRepresentable.fromEnum(Type::values);

        private final String name;
        final MapCodec<Gradient> codec;

        Type(MapCodec<? extends Gradient> codec) {
            name = name().toLowerCase(Locale.ROOT);
            this.codec = TFCGenViewer.cast(codec);
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
