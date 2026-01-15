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
                                                .add("value", "Sensitivity: %s%")
                                        )
                                )
                                .branch("rock", rock -> rock
                                        .add("surface", "At surface")
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
                        .add("base", "Generated using %s\nDimensions: %2$s x %2$s")
                        .add("centered_on", "Centered on (%s,%s)")
                        .add("additional_from_visualizer", "Additional information from visualizer:\n%s")
                        .add("generated_regions", "Generated %s regions")
                        .add("color_key", "Color Key:\n\n%s")
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
                        .add("ocean", "Ocean")
                )
                .branch("unit", unit -> unit
                        .add("kilometer", "%s km")
                )
                .add("color_key_template", "%s: %s")
        );
        for (Rock rock : Rock.VALUES) {
            add(ColorProvider.rockKey(rock), capitalizeWord(rock.getSerializedName()));
        }
    }

    private static String capitalizeWord(String singleWord) {
        return Character.toUpperCase(singleWord.charAt(0)) + singleWord.toLowerCase(Locale.ROOT).substring(1);
    }
}
