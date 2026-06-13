package io.github.notenoughmail.tfcgenviewer;

import io.github.notenoughmail.tfcgenviewer.api.GenViewerAPI;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IVisualizerType;
import io.github.notenoughmail.tfcgenviewer.viz.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(TFCGenViewer.ID)
public class DevOnlyTFCGV {

    public DevOnlyTFCGV(IEventBus modbus) {
        modbus.addListener(this::tests);
        final DeferredRegister<IVisualizerType<?, ?, ?, ?>> viz = DeferredRegister.create(GenViewerAPI.VISUALIZER_REGISTRY, TFCGenViewer.ID);
        viz.register(modbus);
        viz.register("chunk/profile_chunk_data_generation_time", ChunkTimeToGenerateVisualizer::new);
        viz.register("region/region_and_cell_position", RegionPositionVisualizer::new);
        viz.register("region/new_cache_test", RegionPointExistsVisualizer::new);
        viz.register("region/neighbor_cache_collision", RegionNeighborPointCollisionVisualizer::new);
        viz.register("region/cache_difference", RegionCacheDifferenceVisualizer::new);
    }

    private void tests(FMLLoadCompleteEvent event) {
        final String enabledTest = System.getProperty("tfcgenviewer/test");
        if ("region_cache_collision".equals(enabledTest)) RegionTests.findTFCCacheCollisions();
        if ("region_cache_alternative".equals(enabledTest)) RegionTests.testAlternativeCache();
    }
}
