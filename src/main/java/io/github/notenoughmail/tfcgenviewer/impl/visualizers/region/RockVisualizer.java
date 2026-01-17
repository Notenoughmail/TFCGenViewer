package io.github.notenoughmail.tfcgenviewer.impl.visualizers.region;

import io.github.notenoughmail.tfcgenviewer.api.MutableImage;
import io.github.notenoughmail.tfcgenviewer.api.RegionPointCache;
import io.github.notenoughmail.tfcgenviewer.api.color.ColorDefinition;
import io.github.notenoughmail.tfcgenviewer.api.color.Colors;
import io.github.notenoughmail.tfcgenviewer.api.scale.ImageSize;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IVisualizerType;
import io.github.notenoughmail.tfcgenviewer.api.widget.OptionRequest;
import io.github.notenoughmail.tfcgenviewer.impl.TFCGenViewerRegistration;
import io.github.notenoughmail.tfcgenviewer.impl.TFCRegionVisualizer;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.dries007.tfc.world.region.Region;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public class RockVisualizer implements RegionVisualizerType<RockVisualizer.Options> {

    public static final Component NAME = TFCGenViewerRegistration.regionVisualizerName(TFCGenViewerRegistration.VIZ_ROCK);

    @Override
    public boolean isPermitted(ServerPlayer player) {
        return true;
    }

    @Override
    public void draw(int imageX, int imageY, MutableImage image, int xPos, int zPos, DrawInfo<TFCChunkGenerator, RegionPointCache, TFCRegionVisualizer.Scale, Options> info) {
        final Region.Point point = info.cache().getPoint(imageX, imageY, xPos, zPos);
        final Block raw;
        if (info.options().surface) {
            raw = info.generator()
                    .rockLayerSettings()
                    .sampleAtLayer(point.rock, 0)
                    .raw();
        } else {
            final int surfaceElevation = point.land() ?
                    point.mountain() || point.coastalMountain() ?
                            100 :
                            75 :
                    60;
            raw = info.cache()
                    .getGenerator()
                    .chunkDataGenerator()
                    .generateRock(
                            info.scale().pixelResolutionToBlock(xPos, true),
                            info.options().elevation,
                            info.scale().pixelResolutionToBlock(zPos, true),
                            surfaceElevation,
                            null
                    )
                    .raw();
        }
        final ColorDefinition color = Colors.ROCK_COLORS.getOrUnknown(BuiltInRegistries.BLOCK.getKey(raw));
        color.addTooltip(info);
        image.setPixel(imageX, imageY, color.abgr());
    }

    @Override
    public ResourceLocation id() {
        return TFCGenViewerRegistration.VIZ_ROCK.id();
    }

    @Override
    public Options createOptions(RegistryAccess registryAccess, TFCChunkGenerator generator, ImageSize scale) {
        return new Options();
    }

    @Override
    public void addOptions(OptionRequest optionRequest, Options options) {
        optionRequest.orderBool("tfcgenviewer.option.region_visualizer.rock.surface", true, b -> options.surface = b)
                .withDisplay((c, b) -> b ? CommonComponents.GUI_YES : CommonComponents.GUI_NO)
                .finalizeOrder();
        optionRequest.orderInt("tfcgenviewer.option.region_visualizer.rock.elevation", 75, -64, 320, i -> options.elevation = i)
                .withDisplay(IVisualizerType.Options.genericCaption(i -> Component.literal(Integer.toString(i))))
                .finalizeOrder();
    }

    @Override
    public Component colorKey(RegistryAccess registryAccess, RegionPointCache cache) {
        return Colors.ROCK_COLORS.colorKey();
    }

    @Override
    public Component name() {
        return NAME;
    }

    @Nullable
    @Override
    public Component additionalPreviewInfo(DrawInfo<TFCChunkGenerator, RegionPointCache, TFCRegionVisualizer.Scale, Options> info) {
        return info.options().surface ?
                RegionVisualizerType.super.additionalPreviewInfo(info) :
                Component.translatable(
                        "tfcgenviewer.preview_info.generated_rock",
                        info.cache().visitedRegions(),
                        info.options().elevation
                );
    }

    public static final class Options implements IVisualizerType.Options<Options> {

        boolean surface = true;
        int elevation = 75; // Random guess for 'surface' y-level

        @Override
        public Options copy() {
            final Options ret = new Options();
            ret.surface = surface;
            ret.elevation = elevation;
            return ret;
        }
    }
}
