package com.notenoughmail.tfcgenviewer.config.color;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.notenoughmail.tfcgenviewer.TFCGenViewer;
import com.notenoughmail.tfcgenviewer.util.CacheableSupplier;
import com.notenoughmail.tfcgenviewer.util.ColorUtil;
import com.notenoughmail.tfcgenviewer.util.IWillAppendTo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class Colors<T extends IWillAppendTo> extends SimpleJsonResourceReloadListener {

    static final Gson GSON = new Gson();

    public static final ResourceLocation UNKNOWN = TFCGenViewer.identifier("unknown");

    public static final Colors<ColorGradientDefinition> Colors = new Colors<>(
            "colors",
            new String[]{
                    "fill_ocean",
                    "rainfall",
                    "temperature"
            },
            new ColorGradientDefinition[] {
                    new ColorGradientDefinition(
                            ColorUtil.blue,
                            Component.translatable("biome.tfc.ocean")
                    ),
                    new ColorGradientDefinition(
                            ColorUtil.climate,
                            Component.translatable("tfcgenviewer.climate.rainfall")
                    ),
                    new ColorGradientDefinition(
                            ColorUtil.climate,
                            Component.translatable("tfcgenviewer.climate.temperature")
                    )
            },
            (json, id, type) -> ColorGradientDefinition.parse(json, id, type, null),
            (isTemp, colors) -> new CacheableSupplier<>(() -> {
                final MutableComponent key = Component.empty();
                (isTemp ? colors.get(2) : colors.get(1)).appendTo(key);
                colors.get(0).appendTo(key, true);
                return key;
            })
    );
    public static final Colors<ColorGradientDefinition> RockTypes = new Colors<>(
            "rock_types",
            new String[] {
                    "oceanic",
                    "volcanic",
                    "land",
                    "uplift"
            },
            new ColorGradientDefinition[] {
                    new ColorGradientDefinition(
                            ColorUtil.blue,
                            Component.translatable("tfcgenviewer.rock_type.oceanic")
                    ),
                    new ColorGradientDefinition(
                            ColorUtil.volcanic,
                            Component.translatable("tfcgenviewer.rock_type.volcanic")
                    ),
                    new ColorGradientDefinition(
                            ColorUtil.green,
                            Component.translatable("tfcgenviewer.rock_type.land")
                    ),
                    new ColorGradientDefinition(
                            ColorUtil.uplift,
                            Component.translatable("tfcgenviewer.rock_type.uplift")
                    )
            },
            (json, id, type) -> ColorGradientDefinition.parse(
                    json,
                    id,
                    type,
                    switch (id.getNamespace()) {
                        case "oceanic" -> ColorUtil.blue;
                        case "volcanic" -> ColorUtil.volcanic;
                        case "land" -> ColorUtil.green;
                        case "uplift" -> ColorUtil.uplift;
                        default -> null;
                    }
            ),
            (i, colors) -> i ? new CacheableSupplier<>(() -> {
                final MutableComponent key = Component.empty();
                colors.get(2).appendTo(key);
                colors.get(0).appendTo(key);
                colors.get(1).appendTo(key);
                colors.get(3).appendTo(key, true);
                return key;
            }) : null
    );
    public static final Colors<ColorDefinition> Spawn = new Colors<>(
            "spawn",
            new String[] {
                    "border",
                    "reticule"
            },
            new ColorDefinition[] {
                    new ColorDefinition(
                            ColorUtil.DARK_GRAY,
                            Component.empty(),
                            0
                    ),
                    new ColorDefinition(
                            ColorUtil.SPAWN_RED,
                            Component.empty(),
                            0
                    )
            },
            (json, id, type) -> {
                final ColorDefinition def = ColorDefinition.parse(
                        json,
                        0xF0000000,
                        "spawn." + type,
                        id,
                        "null"
                );
                return def.color() == 0xF0000000 ? null : def;
            },
            (i, c) -> null
    );
    public static final Colors<ColorDefinition> BiomeAltitudes = new Colors<>(
            "biome_altitude",
            new String[] {
                    "low",
                    "medium",
                    "high",
                    "mountain"
            },
            new ColorDefinition[] {
                    new ColorDefinition(
                            ColorUtil.green.applyAsInt(0),
                            Component.translatable("tfcgenviewer.biome_altitude.low"),
                            0
                    ),
                    new ColorDefinition(
                            ColorUtil.green.applyAsInt(0.333),
                            Component.translatable("tfcgenviewer.biome_altitude.medium"),
                            0
                    ),
                    new ColorDefinition(
                            ColorUtil.green.applyAsInt(0.666),
                            Component.translatable("tfcgenviewer.biome_altitude.high"),
                            0
                    ),
                    new ColorDefinition(
                            ColorUtil.green.applyAsInt(0.999),
                            Component.translatable("tfcgenviewer.biome_altitude.mountain"),
                            0
                    )
            },
            (json, id, type) -> {
                final ColorDefinition def = ColorDefinition.parse(
                        json,
                        0xF0000000,
                        "biome_altitude." + id.getPath(),
                        id,
                        "tfcgenviewer.biome_altitude." + id.getPath()
                );
                return def.color() == 0xF0000000 ? null : def;
            } ,
            (i, colors) -> i ? new CacheableSupplier<>(() -> {
                final MutableComponent key = Component.empty();
                colors.append(key);
                Colors.get(0).appendTo(key, true);
                return key;
            }) : null
    );
    public static final Colors<ColorDefinition> Rivers = new Colors<>(
            "rivers_and_mountains",
            new String[] {
                    "river",
                    "oceanic_volcanic_mountain",
                    "inland_mountain",
                    "lake"
            },
            new ColorDefinition[] {
                    new ColorDefinition(
                            ColorUtil.RIVER_BLUE,
                            Component.translatable("biome.tfc.river"),
                            0
                    ),
                    new ColorDefinition(
                            ColorUtil.VOLCANIC_MOUNTAIN,
                            Component.translatable("tfcgenviewer.rivers.oceanic_volcanic_mountain"),
                            0
                    ),
                    new ColorDefinition(
                            ColorUtil.GRAY,
                            Component.translatable("tfcgenviewer.rivers.inland_mountain"),
                            0
                    ),
                    new ColorDefinition(
                            ColorUtil.SHALLOW_WATER,
                            Component.translatable("biome.tfc.lake"),
                            0
                    )
            },
            (json, id, type) -> {
                final ColorDefinition def = ColorDefinition.parse(
                        json,
                        0xF0000000,
                        "rivers_and_mountains." + type,
                        id,
                        switch (id.getPath()) {
                            case "lake", "river" -> "biome.tfc." + id.getPath();
                            default -> "tfcgenviewer.rivers." + id.getPath();
                        }
                );
                return def.color() == 0xF0000000 ? null : def;
            },
            (i, colors) -> i ? new CacheableSupplier<>(() -> {
                final MutableComponent key = Component.empty();
                colors.append(key);
                BiomeAltitudes.append(key);
                Colors.get(0).appendTo(key, true);
                return key;
            }): null
    );
    public static final Colors<IWillAppendTo> InlandHeight = new Colors<>(
            "inland_height",
            new String[] {
                    "land",
                    "shallow_water",
                    "deep_water",
                    "very_deep_water"
            },
            new IWillAppendTo[] {
                    new ColorGradientDefinition(
                            ColorUtil.green,
                            Component.translatable("tfcgenviewer.inland_height.land")
                    ),
                    new ColorDefinition(
                            ColorUtil.SHALLOW_WATER,
                            Component.translatable("tfcgenviewer.inland_height.shallow_water"),
                            0
                    ),
                    new ColorDefinition(
                            ColorUtil.DEEP_WATER,
                            Component.translatable("tfcgenviewer.inland_height.deep_water"),
                            0
                    ),
                    new ColorDefinition(
                            ColorUtil.VERY_DEEP_WATER,
                            Component.translatable("tfcgenviewer.inland_height.very_deep_water"),
                            0
                    )
            },
            (json, id, type) -> {
                // I forgot this one was mixed
                if (id.getPath().equals("land")) {
                    return ColorGradientDefinition.parse(
                            json,
                            id,
                            "inland_height.land",
                            null
                    );
                } else {
                    final ColorDefinition def = ColorDefinition.parse(
                            json,
                            0xF0000000,
                            "inland_height." + id.getPath(),
                            id,
                            "tfcgenviewer.inland_height." + id.getPath()
                    );
                    return def.color() == 0xF0000000 ? null : def;
                }
            },
            (i, colors) -> i ? new CacheableSupplier<>(() -> {
                final MutableComponent key = Component.empty();
                colors.append(key);
                return key;
            }) : null
    );

    private final String type;
    private final List<String> indexLookup;
    private final T[] values;
    private final TypeFactory<T> definitionFactory;
    private final CacheableSupplier<Component> trueKey, falseKey;

    private Colors(
            String directory,
            String[] handled,
            T[] initialValues,
            TypeFactory<T> definitionFactory,
            BiFunction<Boolean, Colors<T>, CacheableSupplier<Component>> key
    ) {
        super(GSON, "tfcgenviewer/" + directory);
        assert handled.length == initialValues.length;
        type = directory;
        indexLookup = List.of(handled);
        values = initialValues;
        this.definitionFactory = definitionFactory;
        trueKey = key.apply(true, this);
        falseKey = key.apply(false, this);
    }

    public T get(int index) {
        return values[index];
    }

    public Supplier<Component> key(boolean temperature) {
        return temperature ? trueKey : falseKey;
    }

    public Supplier<Component> key() {
        return trueKey;
    }

    public void append(MutableComponent key) {
        for (IWillAppendTo appender : values) {
            appender.appendTo(key);
        }
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> colors, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        TFCGenViewer.LOGGER.debug("Reloading {}", indexLookup);
        colors.forEach((id, json) -> {
            if (id.getNamespace().equals(TFCGenViewer.ID)) {
                if (json.isJsonObject()) {
                    final JsonObject obj = json.getAsJsonObject();
                    final int index = indexLookup.indexOf(id.getPath());
                    if (index != -1) {
                        final T value = definitionFactory.build(obj, id, type);
                        if (value != null) {
                            values[index] = value;
                        }
                    } else {
                        TFCGenViewer.LOGGER.warn("Unknown colors definition type \"{}\", skipping", id.getPath());
                    }
                } else {
                    TFCGenViewer.LOGGER.warn("Color file \"{}\" was not a json object, using previous value", id);
                }
            }
        });
        if (trueKey != null) {
            trueKey.clearCache();
        }
        if (falseKey != null) {
            falseKey.clearCache();
        }
    }

    private interface TypeFactory<F> {
        @Nullable F build(JsonObject json, ResourceLocation id, String type);
    }
}
