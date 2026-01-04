package io.github.notenoughmail.tfcgenviewer.api.color;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.notenoughmail.tfcgenviewer.api.GenViewerAPI;
import net.minecraft.util.FastColor.*;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;

import java.util.List;
import java.util.Locale;
import java.util.function.DoubleToIntFunction;
import java.util.function.Function;

import static net.minecraft.util.FastColor.ABGR32.*;

public sealed interface Gradient permits Gradient.Base, Gradient.Data, Gradient.Registry, Gradient.Static, Gradient.FromTo {

    Codec<Gradient> CODEC = Codec.either(
            Base.CODEC,
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

    static DoubleToIntFunction lin(int from, int to) {
        final double
                r0 = lin(red(from)),
                r1 = lin(red(to)),
                g0 = lin(green(from)),
                g1 = lin(green(to)),
                b0 = lin(blue(from)),
                b1 = lin(blue(from));
        return value -> color(
                255,
                delin(Mth.lerp(value, b0, b1)),
                delin(Mth.lerp(value, g0, g1)),
                delin(Mth.lerp(value, r0, r1))
        );
    }

    static DoubleToIntFunction multi(int[] colors) {
        
    }

    static double lin(int channel) {
        return Math.pow((double) (channel & 0xFF) / 0xFF, 2.2D);
    }

    static int delin(double channel) {
        return 0xFF & (int) (0xFF * Math.pow(channel, 1D / 2.2D));
    }

    enum Base implements Gradient, StringRepresentable {
        BLUE(0xFF963232, 0xFFFF8C64),
        GREEN(0xFF006400, 0xFF50C850),
        VOLCANIC(d -> color(
                0xFF,
                0x64,
                delin(d * 0.1264363868D), // 0x64 linearized
                0xC8
        )),
        UPLIFT(d -> color(
                0xFF,
                0xC8,
                delin(d * 0.4607566240D), // 0xB4 linearized
                0xB4
        )),
        RAINFALL(
                0xFF000287,
                0xFF0032FF,
                0xFF00A0FF,
                0xFF78E8FF,
                0xFF0FA00F,
                0xFFD26414,
                0xFFFAB978),
        TEMPERATURE(
                0xFFFF1D00,
                0xFFFFBB00,
                0xFF94FF63,
                0xFF13FFE4,
                0xFF0079FF,
                0xFF0000D1),
        GRAYSCALE(0xFFFFFFFF, 0xFF000000)
        ;

        static final Codec<Base> CODEC = StringRepresentable.fromEnum(Base::values);

        private final String name;
        private final DoubleToIntFunction func;

        Base(int... colors) {
            this(multi(colors));
        }

        Base(int from, int to) {
            this(lin(from, to));
        }

        Base(DoubleToIntFunction func) {
            name = name().toLowerCase(Locale.ROOT);
            this.func = func;
        }

        @Override
        public int applyAsAbgr(double value) {
            return func.applyAsInt(value);
        }

        @Override
        public Type type() {
            return Type.PRESET;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }


    record Data(List<RGB> colors, DoubleToIntFunction baked) implements Gradient {

        Data(List<RGB> colors) {
            this(colors, multi(colors.stream().mapToInt(RGB::abgr).toArray()));
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

    non-sealed abstract class Registry implements Gradient {

        @Override
        public final Type type() {
            return Type.REGISTRY;
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
        PRESET(Base.CODEC),
        LIST(RGB.CODEC.listOf(3, Integer.MAX_VALUE).xmap(Data::new, Data::colors)),
        REGISTRY(GenViewerAPI.GRADIENT_REGISTRY.byNameCodec()),
        STATIC(RGB.CODEC.xmap(Static::new, Static::color)),
        FROM_TO(RecordCodecBuilder.<FromTo>create(i -> i.group(
                RGB.CODEC.fieldOf("from").forGetter(FromTo::from),
                RGB.CODEC.fieldOf("to").forGetter(FromTo::to)
        ).apply(i, FromTo::new)))
        ;

        static final Codec<Type> CODEC = StringRepresentable.fromEnum(Type::values);

        private final String name;
        final MapCodec<Gradient> codec;

        Type(Codec<? extends Gradient> codec) {
            name = name().toLowerCase(Locale.ROOT);
            this.codec = (MapCodec<Gradient>) codec.fieldOf(name);
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
