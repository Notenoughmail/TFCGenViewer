package com.notenoughmail.tfcgenviewer.util;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.serialization.Codec;
import net.dries007.tfc.world.region.Region;
import net.dries007.tfc.world.region.RegionGenerator;
import net.minecraft.network.chat.Component;

import java.util.Random;
import java.util.Set;

public enum VisualizeTask {
    BIOMES(name("biomes"), (x, y, task, generator, image) -> {
        
    }, RegionGenerator.Task.CHOOSE_BIOMES),
    RAINFALL(name("rainfall"), (x, y, task, generator, image) -> {

    }, RegionGenerator.Task.ANNOTATE_RAINFALL),
    TEMPERATURE(name("temperature"), (x, y, task, generator, image) -> {
        
    }, RegionGenerator.Task.ANNOTATE_CLIMATE),
    CONTINENTS(name("continents"), (x, y, task, generator, image) -> {
        
    }, RegionGenerator.Task.ADD_ISLANDS, RegionGenerator.Task.ADD_CONTINENTS, RegionGenerator.Task.FLOOD_FILL_SMALL_OCEANS, RegionGenerator.Task.ADD_MOUNTAINS),
    BIOME_ALTITUDE(name("biome_altitude"), (x, y, task, generator, image) -> {
        
    }, RegionGenerator.Task.ANNOTATE_BIOME_ALTITUDE),
    INLAND_HEIGHT(name("inland_height"), (x, y, task, generator, image) -> {
        
    }, RegionGenerator.Task.ANNOTATE_BASE_LAND_HEIGHT),
    ROCKS(name("rocks"), (x, y, task, generator, image) -> {
        final Region.Point point = generator.getOrCreateRegionPoint(x, y);
        final double value = new Random(point.rock >> 2).nextDouble();
        ImageBuilder.setPixel(image, x, y, switch (point.rock & 0b11) {
            case 0 -> ImageBuilder.blue.applyAsInt(value);
            case 1 -> ImageBuilder.VOLCANIC_ROCK.applyAsInt(value);
            case 2 -> ImageBuilder.green.applyAsInt(value);
            case 3 -> ImageBuilder.UPLIFT_ROCK.applyAsInt(value);
            default -> ImageBuilder.CLEAR;
        });
    }, RegionGenerator.Task.CHOOSE_ROCKS),
    RIVERS(name("rivers"), (x, y, task, generator, image) -> {
        
    }, RegionGenerator.Task.ADD_RIVERS_AND_LAKES, RegionGenerator.Task.ADD_CONTINENTS, RegionGenerator.Task.FLOOD_FILL_SMALL_OCEANS, RegionGenerator.Task.ADD_ISLANDS, RegionGenerator.Task.ADD_MOUNTAINS);

    public static final VisualizeTask[] VALUES = values();
    public static final Codec<VisualizeTask> CODEC = Codec.intRange(0, VALUES.length - 1).xmap(b -> VALUES[b], Enum::ordinal);

    private static Component name(String name) {
        return Component.translatable("tfcgenviewer.preview_world.visualizer_task." + name);
    }

    private final Component name;
    private final DrawFunction drawer;
    private final Set<RegionGenerator.Task> parentTasks;

    VisualizeTask(Component name, DrawFunction drawer, RegionGenerator.Task... parentTasks) {
        this.name = name;
        this.drawer = drawer;
        this.parentTasks = Set.of(parentTasks);
    }

    public Component getName() {
        return name;
    }

    public boolean taskApplies(RegionGenerator.Task task) {
        return parentTasks.contains(task);
    }
    
    public void draw(int x, int y, RegionGenerator.Task task, RegionGenerator generator, NativeImage image) {
        if (parentTasks.contains(task)) {
            drawer.draw(x, y, task, generator, image);
        }
    }

    @FunctionalInterface
    interface DrawFunction {
        void draw(int x, int y, RegionGenerator.Task task, RegionGenerator generator, NativeImage image);
    }
}
