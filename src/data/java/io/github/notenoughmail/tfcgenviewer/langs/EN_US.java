package io.github.notenoughmail.tfcgenviewer.langs;

import io.github.notenoughmail.tfcgenviewer.LangProvider;
import io.github.notenoughmail.tfcgenviewer.TFCGenViewer;
import io.github.notenoughmail.tfcgenviewer.color.ColorProvider;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.minecraft.data.PackOutput;

import java.util.Locale;

public class EN_US extends LangProvider {

    public EN_US(PackOutput output) {
        super(output, TFCGenViewer.ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        branch(TFCGenViewer.ID, mod -> mod
                .branch("configuration", config -> config
                        .add("dingWhenGenerated", "Ding When Complete")
                        .add("displayGenerationProgress", "Display Progress")
                        .add("maxPreviewWidth", "Max Preview Width")
                )
                .branch("option", option -> option
                        .branch("region_visualizer", region -> region
                                .branch("rivers_and_mountains", rivers -> rivers
                                        .branch("sensitivity", sensitivity -> sensitivity
                                                .add("", "Sensitivity")
                                                .add("value", "Sensitivity: %s%%")
                                                .add("tooltip", "The sensitivity of river location calculations, a higher value is fuzzier")
                                        )
                                )
                                .branch("rock", rock -> rock
                                        .branch("mode", surface -> surface
                                                .add("", "Mode")
                                                .add("tooltip", "If the preview should generate the surface rock, or the rock at the specified elevation")
                                                .add("surface", "At Surface")
                                                .add("elevation", "At Elevation")
                                        )
                                        .branch("elevation", elev -> elev
                                                .add("", "Elevation")
                                                .add("tooltip", "The y-level to preview at, only used if the mode is 'At Elevation'")
                                        )
                                )
                        )
                        .add("preview_size", "Size")
                        .add("visualizer_type", "Visualizer")
                )
                .branch("widget", widget -> widget
                        .branch("preview_pane", preview -> preview
                                .branch("narration", narration -> narration
                                        .add("title", "Preview Pane")
                                )
                                .add("hover_pos", "(%s,%s)")
                                .add("no_tooltip", "No tooltip available")
                        )
                )
                .branch("button", button -> button
                        .add("preview", "Preview %s with %s")
                        .add("apply", "Apply")
                        .add("export", "Export Preview")
                        .add("save", "Save")
                        .branch("current_seed", seed -> seed
                                .add("", "Current seed: %s")
                                .add("tooltip", "Click to set as seed and copy to clipboard")
                        )
                )
                .branch("screen", screen -> screen
                        .branch("preview_world", preview -> preview
                                .add("title", "Previewing %s with %s")
                                .branch("option", option -> option
                                        .add("visualizer_type", "Visualizer")
                                        .add("preview_scale", "Preview Size")
                                        .add("spawn_overlay", "Spawn Overlay")
                                        .branch("x_offset", x -> x
                                                .add("", "X Offset")
                                                .add("tooltip", "The x offset, in km, of the preview's center from (0,0)")
                                        )
                                        .branch("z_offset", z -> z
                                                .add("", "Z Offset")
                                                .add("tooltip", "The z offset, in km, of the preview's center from (0,0)")
                                        )
                                )
                        )
                        .branch("multiple_generator_visualizers", multi -> multi
                                .add("title", "Multiple Generator Visualizers Available")
                                .add("entry", "View with: %s")
                        )
                        .branch("view_world", view -> view
                                .add("title", "Viewing %s with %s")
                        )
                )
                .branch("generator", gen -> gen
                        .branch("tfc_overworld", tfc -> tfc
                                .add("region", "TFC (Grid Scale)")
                                .add("chunk", "TFC (Chunk Scale)")
                        )
                )
                .branch("preview_info", info -> info
                        .add("base", "Visualizer used: %1$s\nSize: %2$s x %2$s\nTime elapsed: %3$s seconds")
                        .add("centered_on", "Centered on (%s,%s)")
                        .add("additional_from_visualizer", "Additional information from visualizer:\n%s")
                        .add("generated_regions", "Generated %s regions")
                        .add("generated_rock", "Generated %s regions at y-level %s")
                        .add("generated_rock_chunk", "Generated at y-level %s")
                        .add("color_key", "Color Key:\n%s")
                        .add("generating", "Generating with %s...")
                        .add("error", "An error occurred during generation\n\nPlease check the log and report the error")
                )
                .branch("visualizers", type -> type
                        .branch("region", region -> region
                                .branch("biome", biome -> biome
                                        .add("", "Biomes")
                                        .add("description", "Shows a biome map")
                                )
                                .branch("rock_type", rockType -> rockType
                                        .add("", "Surface Rock Types")
                                        .add("description", "Shows a view of rock type that will generate at the surface")
                                )
                                .branch("rainfall", rain -> rain
                                        .add("", "Rainfall")
                                        .add("description", "Shows the average rainfall over land")
                                )
                                .branch("temperature", temp -> temp
                                        .add("", "Temperature")
                                        .add("description", "Shows the average temperature over land")
                                )
                                .branch("rock", rock -> rock
                                        .add("", "Rocks")
                                        .add("description", "Shows the rock that is likely to generate at a location")
                                )
                                .branch("koppen", koppen -> koppen
                                        .add("", "Köppen Climate Classification")
                                        .add("description", "Shows the Köppen climate classification of the world")
                                )
                                .branch("climate_restricted", climate -> climate
                                        .add("", "Climate Restricted Generation")
                                        .add("description", "Shows the regions where climate-restricted features could spawn")
                                )
                                .branch("biome_altitude", alt -> alt
                                        .add("", "Biome Altitude")
                                        .add("description", "Shows the base biome altitude of the world")
                                )
                                .branch("rivers_and_mountains", river -> river
                                        .add("", "Rivers and Mountains")
                                        .add("description", "Shows the locations of hotspots, inland & coastal mountains, and rivers")
                                )
                        )
                        .branch("chunk", chunk -> chunk
                                .branch("elevation", el -> el
                                        .add("", "Elevation")
                                        .add("description", "The approximate surface elevation of the chunk")
                                )
                                .branch("biome", biome -> biome
                                        .add("", "Biomes")
                                        .add("description", "Shows a biome map")
                                )
                                .branch("koppen", koppen -> koppen
                                        .add("", "Köppen Climate Classification")
                                        .add("description", "Shows the Köppen climate classification of the world")
                                )
                                .branch("rainfall", rain -> rain
                                        .add("", "Rainfall")
                                        .add("description", "Shows the average rainfall over land")
                                )
                                .branch("temperature", temp -> temp
                                        .add("", "Temperature")
                                        .add("description", "Shows the average temperature over land")
                                )
                                .branch("rock", rock -> rock
                                        .add("", "Rocks")
                                        .add("description", "Shows the rock that is likely to generate at a location")
                                )
                                .branch("climate_restricted", climate -> climate
                                        .add("", "Climate Restricted Generation")
                                        .add("description", "Shows the regions where climate-restricted features could spawn")
                                )
                        )
                )
                .branch("gradient", gradient -> gradient
                        .branch("rainfall", rain -> rain
                                .add("", "Rainfall, 0 mm -> 500 mm")
                                .grow(10, i -> "%s to %s mm".formatted(i * 50, 50 + i * 50))
                        )
                        .branch("temperature", temp -> temp
                                .add("", "Temperature, -25 °C -> 35 °C")
                                .grow(12, i -> "%s to %s °C".formatted(-25 + i * 5, -20 + i * 5))
                        )
                        .branch("rock_type", rock -> rock
                                .add("oceanic", "Oceanic Rock")
                                .add("volcanic", "Volcanic Rock")
                                .add("uplift", "Uplift Rock")
                                .add("land", "Land Rock")
                        )
                        .add("ocean", "Ocean")
                        .branch("elevation", elevation -> elevation
                                .branch("low", low -> low
                                        .add("", "Low Elevation: below y 63")
                                        .grow(8, i -> i == 0 ? "y 28 or lower" : "y %s to %s".formatted(23 + i * 5, 28 + i * 5))
                                )
                                .branch("middle", mid -> mid
                                        .add("", "Middle Elevation: y 63 to 103")
                                        .grow(8, i -> "y %s to %s".formatted(63 + i * 5, 68 + i * 5))
                                )
                                .branch("high", high -> high
                                        .add("", "High Elevation: above y 103")
                                        .grow(20, i -> i == 19 ? "y 198 or higher" : "y %s to %s".formatted(103 + i * 5, 108 + i * 5))
                                )
                        )
                )
                .branch("unit", unit -> unit
                        .add("kilometer", "%s km")
                        .add("chunk", "%s chunks")
                )
                .branch("climate_features", climateFeatures -> climateFeatures
                        .add("multiple_present", "Multiple Features:")
                        .add("list_entry", "\n- %s")
                        .add("land", "Land")
                )
                .branch("visualized_feature", feature -> feature
                        .add("kaolin", "Kaolin Clay")
                        .add("coral", "Coral")
                )
                .add("color_key_template", "%s: %s")
                .branch("network", network -> network
                        .branch("view_request", request -> request
                                .branch("response", response -> response
                                        .add("absent", "TFCGenViewer is not present on the server")
                                        .add("fail", "Cannot visualize this world as it is not TFC like")
                                        .add("empty", "There are no known ways to visualize this world")
                                )
                        )
                )
                .branch("key", key -> key
                        .add("open_viewer", "Open World Viewer")
                )
                .branch("narration", narration -> narration
                        .branch("info_pane", infoPane -> infoPane
                                .add("title", "Info Pane")
                        )
                )
                .branch("command", command -> command
                        .add("deny_ancillary", "Unconditionally disabled %s for all players")
                        .add("allow_ancillary", "Conditionally enabled %s for players")
                        .add("set_ancillary", "Set %s to %s")
                        .add("individual_ancillary", "Set spawn drawing to %s, image exporting to %s, and coordinate viewing to %s for %s")
                        .add("remove_individual_ancillary", "Removed ancillary overrides for %s")
                        .add("deny_visualizer_type", "Unconditionally disabled %s for all players")
                        .add("allow_visualizer_type", "Conditionally enabled %s for players")
                        .add("set_visualizer_type", "Set %s to %s")
                        .add("individual_visualizer_type", "Set %s to %s for %s")
                        .add("remove_individual_visualizer_type", "Removed %s override for %s")
                        .add("describe_visualizer_type", "Description of %s [%s] visualizer type:")
                        .branch("query_permissions", query -> query
                                .add("base", "%s has the following permissions")
                                .add("ancillaries", "- May draw spawn: %s\n- May export preview images: %s\n- May see coordinates in preview: %s")
                                .add("no_visualizers", "- May not view any visualizers")
                                .add("visualizer_heading", "Allowed visualizer types:")
                                .add("visualizer_entry", "- %s [%s]")
                                .add("click_to_describe", "Click to describe %s")
                        )
                )
        );
        branch("color", color -> color
                .branch(TFCGenViewer.ID, self -> self
                        .branch("koppen_classification", koppen -> koppen
                                .add("unknown", "Not land")
                        )
                        .branch("rock", rock -> rock
                                .add("unknown", "Unknown Rock")
                        )
                        .branch("rivers_and_mountains", river -> river
                                .add("coastal_mountain", "Coastal Mountain")
                                .add("inland_mountain", "Inland Mountain")
                                .add("hot_spot_age_4", "Oldest hot Spot")
                                .add("hot_spot_age_3", "Old Hot Spot")
                                .add("hot_spot_age_2", "Young hot Spot")
                                .add("hot_spot_age_1", "Active Hot Spot")
                                .add("land", "Land")
                        )
                        .branch("biome_altitude", alt -> alt
                                .add("mountain", "Mountain Elevation")
                                .add("high", "High Elevation")
                                .add("mid", "Mid Elevation")
                                .add("low", "Low Elevation")
                                .add("shallow", "Shallow Ocean")
                                .add("deep", "Deep Ocean")
                                .add("very_deep", "Very Deep Ocean")
                        )
                        .branch("spawn", spawn -> spawn
                                .add("border", "Spawn Region Edge")
                                .add("reticule", "Spawn Region Center")
                        )
                )
        );
        branch("biome", biome -> biome
                .branch(TFCGenViewer.ID, self -> self
                        .add("unknown", "Unknown Biome")
                )
        );
        for (Rock rock : Rock.VALUES) {
            add(ColorProvider.rockKey(rock), capitalizeWord(rock.getSerializedName()));
        }
    }

    private static String capitalizeWord(String singleWord) {
        return Character.toUpperCase(singleWord.charAt(0)) + singleWord.toLowerCase(Locale.ROOT).substring(1);
    }
}
