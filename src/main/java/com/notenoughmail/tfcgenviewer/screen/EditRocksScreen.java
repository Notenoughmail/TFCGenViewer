package com.notenoughmail.tfcgenviewer.screen;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.DataResult;
import com.notenoughmail.tfcgenviewer.mixin.RockLayerSettingsAccessor;
import com.notenoughmail.tfcgenviewer.util.GuiElement;
import com.notenoughmail.tfcgenviewer.util.MutableRockLayerSettings;
import com.notenoughmail.tfcgenviewer.util.custom.rock.*;
import net.dries007.tfc.world.settings.RockLayerSettings;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.loading.toposort.CyclePresentException;
import net.minecraftforge.fml.loading.toposort.TopologicalSort;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.Consumer;

// The amount of manual juggling of states of various sorts there is in this is honestly concerning, but needs must
// TODO: Prevent esc kicking back to the main menu
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class EditRocksScreen extends Screen {

    public static Component
            TITLE = Component.translatable("tfcgenviewer.rock_editor.title"),
            VALIDATE = Component.translatable("tfcgenviewer.rock_editor.validate"),
            GRAPH = Component.translatable("tfcgenviewer.rock_editor.graph"),
            VALIDATE_SUCCESS = Component.translatable("tfcgenviewer.rock_editor.validate.success"),
            ROCK_SETTINGS_TAB = Component.translatable("tfcgenviewer.rock_editor.tab.rock_settings"),
            LAYER_TYPES_TAB = Component.translatable("tfcgenviewer.rock_editor.tab.layer_types"),
            LAYER_DEFINITIONS_TAB = Component.translatable("tfcgenviewer.rock_editor.tab.layer_definitions"),
            ADD_LAYER_DEFINITION_MAPPING = Component.translatable("tfcgenviewer.rock_editor.add_layer_definition_mapping"),
            EMPTY_LAYER_DEFS = Component.translatable("tfcgenviewer.rock_editor.error.no_layer_definitions"),
            EMPTY_ROCK_SETTINGS = Component.translatable("tfcgenviewer.rock_editor.error.no_rock_settings"),
            EMPTY_BOTTOM_ROCKS = Component.translatable("tfcgenviewer.rock_editor.error.no_bottom_rocks"),
            EMPTY_OCEAN_LAYERS = Component.translatable("tfcgenviewer.rock_editor.error.no_ocean_layer_definitions"),
            EMPTY_VOLCANIC_LAYERS = Component.translatable("tfcgenviewer.rock_editor.error.no_volcanic_layer_definitions"),
            EMPTY_LAND_LAYERS = Component.translatable("tfcgenviewer.rock_editor.error.no_land_layer_definitions"),
            EMPTY_UPLIFT_LAYERS = Component.translatable("tfcgenviewer.rock_editor.error.no_uplift_layer_definitions"),
            CREATE_LAYER_DEFINITION = Component.translatable("tfcgenviewer.rock_editor.create_layer_definition");

    private final TabManager tabManager = new SlightlyImprovedTabManager<>(this::addRenderableWidget, this::removeWidget, this::addRenderableWidget, this::removeWidget);
    @Nullable
    private TabNavigationBar tabNavigationBar;
    @Nullable
    private GridLayout bottomButtons;
    @Nullable
    private Either<RockLayerSettings, DataResult.PartialResult<RockLayerSettings>> built;

    private final PreviewGenerationScreen parent;
    private final RockLayerSettings before;
    private final MutableRockLayerSettings edit;
    @Nullable
    private ExpiringTextWidget messages;

    public EditRocksScreen(PreviewGenerationScreen parent, RockLayerSettings before) {
        super(TITLE);
        this.parent = parent;
        this.before = before;
        edit = MutableRockLayerSettings.init(before);
    }

    private void setMessage(Component message, int time) {
        if (messages != null) {
            removeWidget(messages);
        }
        messages = addRenderableWidget(new ExpiringTextWidget(
                font,
                message,
                time,
                width / 4 * 3
        ));
        messages.centeredOn(width / 2, height / 2);
    }

    private void setMessage(Component message) {
        setMessage(message, 60);
    }

    @Override
    protected void init() {
        tabNavigationBar = TabNavigationBar.builder(tabManager, width).addTabs(
                new SettingsTab(),
                new LayerTypesTab(),
                new LayerDefinitionsTab()
        ).build();
        addRenderableWidget(tabNavigationBar);
        bottomButtons = new GridLayout().columnSpacing(4);
        final GridLayout.RowHelper rowHelper = bottomButtons.createRowHelper(4);
        rowHelper.addChild(Button.builder(PreviewGenerationScreen.APPLY, b -> back(true)).build());
        rowHelper.addChild(Button.builder(VALIDATE, b -> {
            if (validate()) {
                setMessage(VALIDATE_SUCCESS);
            }
        }).build());
        rowHelper.addChild(Button.builder(GRAPH, b -> graph()).build());
        rowHelper.addChild(Button.builder(CommonComponents.GUI_CANCEL, b -> back(false)).build());
        final int buttonWidth = Math.min(150, Math.max(50, (width - 20) / 4));
        bottomButtons.visitWidgets(w -> {
            w.setWidth(buttonWidth);
            w.setTabOrderGroup(1);
            addRenderableWidget(w);
        });
        tabNavigationBar.selectTab(0, false);
        repositionElements();
    }

    @Override
    protected void repositionElements() {
        if (tabNavigationBar != null && bottomButtons != null) {
            tabNavigationBar.setWidth(width);
            tabNavigationBar.arrangeElements();
            bottomButtons.arrangeElements();
            FrameLayout.centerInRectangle(bottomButtons, 0, height - 30, width, 30);
            final int i = tabNavigationBar.getRectangle().bottom();
            tabManager.setTabArea(new ScreenRectangle(0, i, width, bottomButtons.getY() - i));
        }
    }

    @Override
    public void tick() {
        if (messages != null && messages.tick()) {
            removeWidget(messages);
            messages = null;
        }
    }

    private boolean validate() {
        build();
        assert built != null;
        return built.map(settings -> true, partial -> {
            setMessage(Component.translatable("tfcgenviewer.rock_editor.validate.fail", partial.message()));
            return false;
        });
    }

    private void build() {
        if (edit.layerDefs.isEmpty()) {
            err(EMPTY_LAYER_DEFS);
        } else if (edit.rocks.isEmpty()) {
            err(EMPTY_ROCK_SETTINGS);
        } else if (edit.layers.get(LayerType.BOTTOM).isEmpty()) {
            err(EMPTY_BOTTOM_ROCKS);
        } else if (edit.layers.get(LayerType.OCEAN).isEmpty()) {
            err(EMPTY_OCEAN_LAYERS);
        } else if (edit.layers.get(LayerType.VOLCANIC).isEmpty()) {
            err(EMPTY_VOLCANIC_LAYERS);
        } else if (edit.layers.get(LayerType.LAND).isEmpty()) {
            err(EMPTY_LAND_LAYERS);
        } else if (edit.layers.get(LayerType.UPLIFT).isEmpty()) {
            err(EMPTY_UPLIFT_LAYERS);
        } else if (verifyUniqueRawBlocks()) {
            final Map<Block, Set<String>> rawToSetting = new IdentityHashMap<>();
            for (Map.Entry<String, MutableRockLayerSettings.MutableRockSettings> entry : edit.rocks.entrySet()) {
                rawToSetting.computeIfAbsent(entry.getValue().raw, b -> new HashSet<>()).add(entry.getKey());
            }
            rawToSetting
                    .entrySet()
                    .stream()
                    .filter(e -> e.getValue().size() > 1)
                    .findFirst()
                    .ifPresent(e ->
                            err(Component.translatable(
                                    "tfcgenviewer.rock_editor.error.duplicate_raw_rock_blocks",
                                    String.join(", ", e.getValue()),
                                    e.getKey().getName()
                            ))
                    );
        } else {
            @Nullable
            final Component error = sortLayerDefinitions();
            if (error != null) {
                err(error);
            } else {
                built = ((RockLayerSettingsAccessor) (Object) before).tfcgenviewer$processData(edit.build()).get();
            }
        }
    }

    private boolean verifyUniqueRawBlocks() {
        return edit.rocks.values().stream().map(mrs -> mrs.raw).distinct().count() < edit.rocks.size(); // There is a duplicate raw block, this will cause TFC to crash due to some internals expecting unique raw blocks
    }

    private void err(Component err) {
        built = Either.right(new DataResult.PartialResult<>(err::getString, Optional.empty()));
    }

    // TODO: In the future, is there any way this could be done in-game?
    private void graph() {
        if (validate()) {
            StringBuilder url =
                    new StringBuilder("https://notenoughmail.github.io/mc/tools/tfcgv_rock_graph/?version=1.20.1&")
                            .append(joinToQuery("layers", edit.layerDefs.keySet()))
                            .append("&")
                            .append(joinToQuery("ocean_type", edit.layers.get(LayerType.OCEAN)))
                            .append("&")
                            .append(joinToQuery("uplift_type", edit.layers.get(LayerType.UPLIFT)))
                            .append("&")
                            .append(joinToQuery("volcanic_type", edit.layers.get(LayerType.VOLCANIC)))
                            .append("&")
                            .append(joinToQuery("bottom_type", edit.layers.get(LayerType.BOTTOM)))
                            .append("&")
                            .append(joinToQuery("land_type", edit.layers.get(LayerType.LAND)));
            for (MutableRockLayerSettings.MutableLayerData mld : edit.layerDefs.values()) {
                url.append("&").append(joinMapping(mld));
            }
            final String link = url.toString();

            assert minecraft != null;
            minecraft.setScreen(new ConfirmLinkScreen(b -> {
                if (b) {
                    Util.getPlatform().openUri(link);
                }
                minecraft.setScreen(this);
            }, link, true));
        }
    }

    private String encode(String str) {
        str = str.replaceAll("[^a-zA-Z0-9_]+", "_");
        if (str.charAt(0) == '_') str = str.substring(1);
        if (str.charAt(str.length() - 1) == '_') str = str.substring(0, str.length() - 1);
        return isReservedName(str) ? str + "_" : str;
    }

    private String joinToQuery(String type, Collection<String> values) {
        final StringBuilder builder = new StringBuilder(type);
        builder.append("=[");
        for (String str : values) {
            builder.append(encode(str));
            builder.append(';');
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append(']');
        return builder.toString();
    }

    private String joinMapping(MutableRockLayerSettings.MutableLayerData layerData) {
        final StringBuilder builder = new StringBuilder(encode(layerData.id) + "=[");
        for (Map.Entry<String, String> mapping : layerData.mapping.entrySet()) {
            builder.append(encode(mapping.getKey()));
            builder.append('~');
            builder.append(encode(mapping.getValue()));
            builder.append(';');
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append(']');
        return builder.toString();
    }

    private boolean isReservedName(String val) {
        return
                "layers".equals(val) ||
                "ocean_type".equals(val) ||
                "land_type".equals(val) ||
                "volcanic_type".equals(val) ||
                "uplift_type".equals(val) ||
                "bottom_type".equals(val) ||
                "version".equals(val);
    }

    @SuppressWarnings({ "UnstableApiUsage", "unchecked" })
    @Nullable
    private Component sortLayerDefinitions() {
        final MutableGraph<MutableRockLayerSettings.MutableLayerData> graph = GraphBuilder.directed().allowsSelfLoops(false).build();
        for (var mld : edit.layerDefs.values()) {
            for (String layer : mld.mapping.values()) {
                if (!layer.equals("bottom")) {
                    final var req = edit.layerDefs.get(layer);
                    if (req == null) {
                        return Component.translatable("tfcgenviewer.rock_editor.error.unknown_layer_def", layer);
                    }

                    try {
                        graph.putEdge(mld, req);
                    } catch (IllegalArgumentException iae) {
                        return Component.translatable("tfcgenviewer.rock_editor.error.self_referencing_definition.named", mld.id);
                    }
                }
            }
        }

        List<MutableRockLayerSettings.MutableLayerData> defs;
        try {
            defs = TopologicalSort.topologicalSort(graph, null);
        } catch (CyclePresentException e) {
            final StringBuilder message = new StringBuilder();
            e.getCycles().forEach(set -> {
                message.append("\n");
                message.append(String.join(" & ", (Iterable<? extends CharSequence>) set.iterator()));
            });
            return Component.translatable("tfcgenviewer.rock_editor.error.layer_definition_cycle", message.toString());
        }

        edit.layerDefs.clear();
        for (var def : defs) {
            edit.layerDefs.put(def.id, def);
        }

        return null;
    }

    private void back(boolean keepChanges) {
        assert minecraft != null;
        if (keepChanges) {
            // These two if statements should not be merged together
            if (validate()) {
                assert built != null;
                minecraft.setScreen(parent);
                parent.setRocks(built.orThrow());
            }
        } else {
            minecraft.setScreen(parent);
        }
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    class SettingsTab implements Tab, IAmATabWithNonWidgetChildren {

        private final RockSettingsEditor editor = new RockSettingsEditor(
                minecraft,
                width / 2,
                height,
                font,
                this::add,
                EditRocksScreen.this::setMessage
        );
        private final RockSettingsDisplay display = new RockSettingsDisplay(
                minecraft,
                width / 2,
                height,
                edit.rocks,
                font,
                editor::load,
                EditRocksScreen.this::setMessage
        );

        private boolean add(String name, MutableRockLayerSettings.MutableRockSettings mrs) {
            return display.add(name, mrs);
        }

        @Override
        public Component getTabTitle() {
            return ROCK_SETTINGS_TAB;
        }

        @Override
        public void visitChildren(Consumer<AbstractWidget> pConsumer) {}

        @Override
        public void doLayout(ScreenRectangle pRectangle) {
            final int
                    halfScreenWidth = pRectangle.width() / 2,
                    y0 = pRectangle.top() + 12,
                    y1 = pRectangle.bottom() - 12;
            display.updateSize(halfScreenWidth, pRectangle.height(), y0, y1);
            editor.updateSize(halfScreenWidth, pRectangle.height(), y0, y1);
            editor.setLeftPos(halfScreenWidth);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T extends GuiEventListener & Renderable> void visitNonWidgets(Consumer<T> visitor) {
            visitor.accept((T) display); // Yes, these are within the bounds, but a cast is still required
            visitor.accept((T) editor);
        }

        @Override
        public void tick() {
            editor.tick();
        }
    }

    class LayerTypesTab implements Tab, IAmATabWithNonWidgetChildren {

        private LayerType currentlyEditing = LayerType.NONE;
        private final StringWidget editTitle = new StringWidget(width / 2 + 2, 24, width / 2 - 10, font.lineHeight, CommonComponents.EMPTY, font).alignCenter();
        private final LayerTypesEditor editor = new LayerTypesEditor(
                minecraft,
                width / 2,
                height,
                () -> currentlyEditing,
                edit,
                font,
                EditRocksScreen.this::setMessage
        );
        private final SuggestableEditBox input = new SuggestableEditBox(font, width / 2 + 24, height - 46, width / 2 - 30, 16, CommonComponents.EMPTY, Set.of(), minecraft);
        private final LayerTypesDisplay display = new LayerTypesDisplay(
                minecraft,
                width / 2,
                height,
                edit,
                font,
                lt -> {
                    currentlyEditing = lt;
                    input.setSuggestions((currentlyEditing == LayerType.BOTTOM ? edit.rocks : edit.layerDefs).keySet());
                    editor.reload();
                    editTitle.setMessage(Component.translatable("tfcgenviewer.rock_editor.currently_editing_layer", lt.title));
                },
                () -> currentlyEditing
        );
        private final ImageButton addButton = GuiElement.CONFIRM.button(width / 2 + 2, height - 48, b -> {
            final String val = input.getValue();
            if (currentlyEditing != LayerType.NONE && !val.isEmpty() && editor.add(val)) {
                input.setValue("");
            }
        });

        @Override
        public Component getTabTitle() {
            return LAYER_TYPES_TAB;
        }

        @Override
        public void visitChildren(Consumer<AbstractWidget> pConsumer) {
            pConsumer.accept(input);
            pConsumer.accept(addButton);
            pConsumer.accept(editTitle);
        }

        @Override
        public void doLayout(ScreenRectangle pRectangle) {
            final int
                    halfScreenWidth = pRectangle.width() / 2,
                    y0 = pRectangle.top() + 12,
                    y1 = pRectangle.bottom() - 12;
            display.updateSize(halfScreenWidth, pRectangle.height(), y0, y1);
            editor.updateSize(halfScreenWidth, pRectangle.height(), y0 + 12, y1 - 24);
            editor.setLeftPos(halfScreenWidth);
            addButton.setX(halfScreenWidth + 2);
            addButton.setY(y1 - 20);
            input.setX(halfScreenWidth + 24);
            input.setY(y1 - 18);
            input.setWidth(halfScreenWidth - 30);
            editTitle.setX(halfScreenWidth + 2);
            editTitle.setY(y0);
            editTitle.setWidth(halfScreenWidth - 10);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T extends GuiEventListener & Renderable> void visitNonWidgets(Consumer<T> visitor) {
            visitor.accept((T) display);
            visitor.accept((T) editor);
        }

        @Override
        public void tick() {
            input.tick();
        }
    }

    class LayerDefinitionsTab implements Tab, IAmATabWithNonWidgetChildren {

        private final LayerDefinitionEditor editor  = new LayerDefinitionEditor(
                minecraft, width / 2,
                height,
                font,
                this::add,
                EditRocksScreen.this::setMessage,
                edit,
                this::setButtonMessage
        );
        private final LayerDefinitionDisplay display = new LayerDefinitionDisplay(
                minecraft,
                width / 2,
                height,
                font,
                edit,
                editor::accept,
                EditRocksScreen.this::setMessage
        );
        private final Button add = Button.builder(CREATE_LAYER_DEFINITION, b -> editor.add()).build();

        private boolean add(MutableRockLayerSettings.MutableLayerData mld) {
            return display.add(mld);
        }

        private void setButtonMessage(boolean editorIsEmpty) {
            add.setMessage(editorIsEmpty ? CREATE_LAYER_DEFINITION : ADD_LAYER_DEFINITION_MAPPING);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T extends GuiEventListener & Renderable> void visitNonWidgets(Consumer<T> visitor) {
            visitor.accept((T) display);
            visitor.accept((T) editor);
        }

        @Override
        public Component getTabTitle() {
            return LAYER_DEFINITIONS_TAB;
        }

        @Override
        public void visitChildren(Consumer<AbstractWidget> pConsumer) {
            pConsumer.accept(add);
        }

        @Override
        public void doLayout(ScreenRectangle pRectangle) {
            final int
                    halfScreenWidth = pRectangle.width() / 2,
                    y0 = pRectangle.top() + 12,
                    y1 = pRectangle.bottom() - 12;
            display.updateSize(halfScreenWidth, pRectangle.height(), y0, y1);
            editor.updateSize(halfScreenWidth, pRectangle.height(), y0, y1 - 24);
            editor.setLeftPos(halfScreenWidth);
            add.setX(halfScreenWidth + 2);
            add.setY(y1 - 20);
            add.setWidth(halfScreenWidth - 8);
        }

        @Override
        public void tick() {
            editor.tick();
        }
    }
}
