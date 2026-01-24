package io.github.notenoughmail.tfcgenviewer.client.screen;

import io.github.notenoughmail.tfcgenviewer.api.GenViewerAPI;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IGeneratorVisualizer;
import io.github.notenoughmail.tfcgenviewer.client.widget.SelectionList;
import io.github.notenoughmail.tfcgenviewer.impl.ImplAPI;
import io.github.notenoughmail.tfcgenviewer.impl.network.packet.ViewRequestPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MultipleVisualizerScreen extends Screen {

    public static final Component TITLE = Component.translatable("tfcgenviewer.screen.multiple_generator_visualizers.title");

    private final Button[] buttons;

    public MultipleVisualizerScreen(Set<ResourceLocation> generatorVisualizers) {
        super(TITLE);
        buttons = generatorVisualizers.stream()
                .map(id -> {
                    final IGeneratorVisualizer<?, ?, ?, ?> genViz = ImplAPI.GEN_IDS.get(id);
                    return Button.builder(Component.translatable("tfcgenviewer.screen.multiple_generator_visualizers.entry", genViz.name()), b -> {
                        PacketDistributor.sendToServer(new ViewRequestPacket(
                                Set.of(id),
                                genViz.allVisualizers()
                                        .stream()
                                        .map(GenViewerAPI.VISUALIZER_REGISTRY::getKey)
                                        .collect(Collectors.toSet())
                        ));
                    }).build();
                })
                .toArray(Button[]::new);
    }

    @Override
    protected void init() {
        addRenderableWidget(new Buttons(getMinecraft(), width, height - 64, 32, 20, buttons));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 11, 0xFFFFFFFF);
    }

    private static class Buttons extends SelectionList<Entry> {

        public Buttons(Minecraft minecraft, int width, int height, int y, int itemHeight, Button... buttons) {
            super(minecraft, width, height, y, itemHeight);
            for (Button b : buttons) {
                addEntry(new MultipleVisualizerScreen.Entry(b));
            }
        }

        @Override
        public int getRowWidth() {
            return 220;
        }
    }

    private static class Entry extends ContainerObjectSelectionList.Entry<Entry> {

        private final List<Button> instance;

        Entry(Button button) {
            instance = List.of(button);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return instance;
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            final Button button = instance.getFirst();
            button.setRectangle(
                    width,
                    height,
                    left,
                    top
            );
            button.render(graphics, mouseX, mouseY, partialTick);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return instance;
        }
    }
}
