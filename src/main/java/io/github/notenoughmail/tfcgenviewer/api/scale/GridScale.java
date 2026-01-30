package io.github.notenoughmail.tfcgenviewer.api.scale;

import com.mojang.serialization.Codec;
import net.dries007.tfc.world.region.Units;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * A scale of 1 pixel = 1 grid (128 blocks). See {@link Units}
 */
public enum GridScale implements IScale<GridSize> {
    INSTANCE;

    @Override
    public int blocksPerPixel() {
        return Units.GRID_WIDTH_IN_BLOCK;
    }

    @Override
    public Component formatSize(GridSize size) {
        return size.display();
    }

    @Override
    public GridSize getDefault() {
        return GridSize._3;
    }

    @Override
    public List<GridSize> sizes() {
        return GridSize.SIZES;
    }

    @Override
    public Codec<GridSize> codec() {
        return GridSize.CODEC;
    }
}
