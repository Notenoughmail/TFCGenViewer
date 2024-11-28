package com.notenoughmail.tfcgenviewer.config.color;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.notenoughmail.tfcgenviewer.TFCGenViewer;
import com.notenoughmail.tfcgenviewer.util.ColorUtil;
import com.notenoughmail.tfcgenviewer.util.IWillAppendTo;
import net.dries007.tfc.util.RegisteredDataManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.notenoughmail.tfcgenviewer.util.ColorUtil.*;

public class Colors<T extends IWillAppendTo> extends RegisteredDataManager<T> {

    public static final ResourceLocation UNKNOWN = TFCGenViewer.identifier("unknown");

    public static final Colors<ColorDefinition> Color = new Colors<>(ColorDefinition::parse, id -> new ColorDefinition(
            ColorUtil.randomColor(),
            Component.translatable("tfcgenviewer.could_not_parse.color", id.toString()),
            1000
    ), "colors", "TFCGenViewer Color", () -> {
        RainKey.clearCache();
        TempKey.clearCache();
        BiomeAltKey.clearCache();
        InlandHeightKey.clearCache();
        RiverKey.clearCache();
        RockTypeKey.clearCache();
    });
    public static final Colors<ColorGradientDefinition> Gradient = new Colors<>(ColorGradientDefinition::parse, id -> new ColorGradientDefinition(
            ColorUtil.linearGradient(
                    ColorUtil.randomColor(),
                    ColorUtil.randomColor()
            ),
            Component.translatable("tfcgenviewer.could_not_parse.gradient", id.toString())
    ), "gradients", "TFCGenViewer Gradient", () -> {});

    // Colors
    public static final Supplier<ColorDefinition> SPAWN_BORDER = color("spawn/border");
    public static final Supplier<ColorDefinition> SPAWN_RETICULE = color("spawn/reticule");
    public static final Supplier<ColorDefinition> RM_INLAND_MOUNTAIN = color("rivers_and_mountains/inland_mountain");
    public static final Supplier<ColorDefinition> RM_LAKE = color("rivers_and_mountains/lake");
    public static final Supplier<ColorDefinition> RM_OCEANIC_VOLCANIC_MOUNTAINS = color("rivers_and_mountains/oceanic_volcanic_mountain");
    public static final Supplier<ColorDefinition> RM_RIVER = color("rivers_and_mountains/river");
    public static final Supplier<ColorDefinition> IH_DEEP = color("inland_height/deep_water");
    public static final Supplier<ColorDefinition> IH_SHALLOW = color("inland_height/shallow_water");
    public static final Supplier<ColorDefinition> IH_VERY_DEEP = color("inland_height/very_deep_water");
    public static final Supplier<ColorDefinition> BA_LOW = color("biome_altitude/low");
    public static final Supplier<ColorDefinition> BA_MEDIUM = color("biome_altitude/medium");
    public static final Supplier<ColorDefinition> BA_HIGH = color("biome_altitude/high");
    public static final Supplier<ColorDefinition> BA_MOUNTAIN = color("biome_altitude/mountain");

    // Color Gradients
    public static final Supplier<ColorGradientDefinition> IH_LAND = gradient("inland_height");
    public static final Supplier<ColorGradientDefinition> FILL_OCEAN = gradient("fill_ocean");
    public static final Supplier<ColorGradientDefinition> RAINFALL = gradient("rainfall");
    public static final Supplier<ColorGradientDefinition> TEMPERATURE = gradient("temperature");
    public static final Supplier<ColorGradientDefinition> RT_LAND = gradient("rock_type/land");
    public static final Supplier<ColorGradientDefinition> RT_OCEANIC = gradient("rock_type/oceanic");
    public static final Supplier<ColorGradientDefinition> RT_UPLIFT = gradient("rock_type/uplift");
    public static final Supplier<ColorGradientDefinition> RT_VOLCANIC = gradient("rock_type/volcanic");

    private final Runnable onApply;

    public Colors(BiFunction<ResourceLocation, JsonObject, T> factory, Function<ResourceLocation, T> fallbackFactory, String domain, String typeName, Runnable onApply) {
        super(factory, fallbackFactory, TFCGenViewer.identifier(domain), typeName);
        this.onApply = onApply;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> elements, ResourceManager resourceManager, ProfilerFiller profiler) {
        super.apply(elements, resourceManager, profiler);
        onApply.run();
    }

    private static Supplier<ColorDefinition> color(String path) {
        return Color.register(TFCGenViewer.identifier(path));
    }

    private static Supplier<ColorGradientDefinition> gradient(String path) {
        return Gradient.register(TFCGenViewer.identifier(path));
    }
}
