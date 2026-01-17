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
                .branch("option", option -> option
                        .branch("region_visualizer", region -> region
                                .branch("rivers_and_mountains", rivers -> rivers
                                        .branch("sensitivity", sensitivity -> sensitivity
                                                .add("", "Sensitivity")
                                                .add("value", "Sensitivity: %s%%")
                                        )
                                )
                                .branch("rock", rock -> rock
                                        .add("surface", "At Surface")
                                        .add("elevation", "Elevation")
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
                )
                .branch("screen", screen -> screen
                        .branch("preview_world", previewWorld -> previewWorld
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
                )
                .branch("generator", gen -> gen
                        .branch("tfc_overworld", tfc -> tfc
                                .add("region", "TFC Overworld (Grid Scale)"))
                )
                .branch("preview_info", info -> info
                        .add("base", "Visualizer used: %1$s\nSize: %2$s x %2$s\nTime elapsed: %3$s seconds")
                        .add("centered_on", "Centered on (%s,%s)")
                        .add("additional_from_visualizer", "Additional information from visualizer:\n%s")
                        .add("generated_regions", "Generated %s regions")
                        .add("generated_rock", "Generated %s regions at y-level %s")
                        .add("color_key", "Color Key:\n%s")
                        .add("generating", "Generating with %s...")
                        .add("error", "An error occurred during generation\n\nPlease check the log and report the error")
                )
                .branch("visualizers", type -> type
                        .branch("region", region -> region
                                .add("biome", "Biomes")
                                .add("rock_type", "Surface Rock Types")
                                .add("rainfall", "Rainfall")
                                .add("temperature", "Temperature")
                                .add("rock", "Rocks")
                                .add("koppen", "Köppen Climate Classification")
                                .add("climate_restricted", "Climate Restricted Generation")
                                .add("biome_altitude", "Biome Altitude")
                                .add("rivers_and_mountains", "Rivers and Mountains")
                        )
                )
                .branch("gradient", gradient -> gradient
                        .branch("rainfall", rain -> rain
                                .add("", "Rainfall, 0 mm -> 500 mm")
                                .add("0", "0 to 100 mm")
                                .add("1", "100 to 200 mm")
                                .add("2", "200 to 300 mm")
                                .add("3", "300 to 400 mm")
                                .add("4", "400 to 500 mm")
                        )
                        .branch("temperature", temp -> temp
                                .add("", "Temperature, -25 °C -> 35 °C")
                                .add("0", "-25 to -13 °C")
                                .add("1", "-13 to -1 °C")
                                .add("2", "-1 to 11 °C")
                                .add("3", "11 to 23 °C")
                                .add("4", "23 to 35 °C")
                        )
                        .branch("rock_type", rock -> rock
                                .add("oceanic", "Oceanic Rock")
                                .add("volcanic", "Volcanic Rock")
                                .add("uplift", "Uplift Rock")
                                .add("land", "Land Rock")
                        )
                        .add("ocean", "Ocean")
                )
                .branch("unit", unit -> unit
                        .add("kilometer", "%s km")
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
                        )
                        .branch("biome_altitude", alt -> alt
                                .add("mountain", "Mountain Elevation")
                                .add("high", "High Elevation")
                                .add("mid", "Mid Elevation")
                                .add("low", "Low Elevation")
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
