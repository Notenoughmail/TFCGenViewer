package io.github.notenoughmail.tfcgenviewer.langs;

import io.github.notenoughmail.tfcgenviewer.LangProvider;
import io.github.notenoughmail.tfcgenviewer.TFCGenViewer;
import net.minecraft.data.PackOutput;

public class EN_US extends LangProvider {

    public EN_US(PackOutput output) {
        super(output, TFCGenViewer.ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        branch("screen", screen -> {
            screen.branch("preview_world", previewWorld -> {
                previewWorld.add("title", "Previewing %s with %s");
                previewWorld.branch("option", option -> option
                        .add("visualizer_type", "Visualizer")
                        .add("preview_scale", "Preview Size")
                        .add("spawn_overlay", "Spawn Overlay")
                        .branch("x_offset", x -> {
                            x.add("", "X Offset");
                            x.add("tooltip", "The x offset, in km, of the preview's center from (0,0)");
                        })
                        .branch("z_offset", z -> {
                            z.add("", "Z Offset");
                            z.add("tooltip", "The z offset, in km, of the preview's center from (0,0)");
                        }));
            });
        });
        branch("generator", gen -> gen
                .branch("tfc_overworld", tfc -> tfc
                        .add("region", "TFC Overworld (Region Scale)"))
        );
        branch("preview_info", info -> info
                .add("base", "Generated using %s\nDimensions: %2$s x %2$s")
                .add("centered_on", "Centered on (%s,%s)")
                .add("additional_from_visualizer", "Additional information from visualizer:\n%s")
                .add("generated_regions", "Generated %s regions")
                .add("color_key", "Color Key:\n\n%s")
                .add("error", "An error occurred during generation\n\nPlease check the log and report the error"));
        branch("visualizer_type", type -> type
                .add()
        );
    }
}
