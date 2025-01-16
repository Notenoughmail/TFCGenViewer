package com.notenoughmail.tfcgenviewer.screen;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.DataResult;
import com.notenoughmail.tfcgenviewer.mixin.RockLayerSettingsAccessor;
import com.notenoughmail.tfcgenviewer.util.MutableRockLayerSettings;
import com.notenoughmail.tfcgenviewer.util.custom.rock.*;
import net.dries007.tfc.world.settings.RockLayerSettings;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.function.Consumer;

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
            ADD_LAYER_DEFINITION = Component.translatable("tfcgenviewer.rock_editor.add_layer_definition"),
            EMPTY_LAYER_DEFS = Component.translatable("tfcgenviewer.rock_editor.error.no_layer_definitions"),
            EMPTY_ROCK_SETTINGS = Component.translatable("tfcgenviewer.rock_editor.error.no_rock_settings"),
            EMPTY_BOTTOM_ROCKS = Component.translatable("tfcgenviewer.rock_editor.error.no_bottom_rocks"),
            EMPTY_OCEAN_LAYERS = Component.translatable("tfcgenviewer.rock_editor.error.no_ocean_layer_definitions"),
            EMPTY_VOLCANIC_LAYERS = Component.translatable("tfcgenviewer.rock_editor.error.no_volcanic_layer_definitions"),
            EMPTY_LAND_LAYERS = Component.translatable("tfcgenviewer.rock_editor.error.no_land_layer_definitions"),
            EMPTY_UPLIFT_LAYERS = Component.translatable("tfcgenviewer.rock_editor.error.no_uplift_layer_definitions");

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
        } else {
            built = ((RockLayerSettingsAccessor) (Object) before).tfcgenviewer$processData(edit.build()).get();
        }
    }

    private void err(Component err) {
        built = Either.right(new DataResult.PartialResult<>(err::getString, Optional.empty()));
    }

    // TODO: Implement
    private void graph() {
        if (validate()) {

        } else {

        }
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
                editor::load
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
        private final LayerTypesDisplay display = new LayerTypesDisplay(
                minecraft,
                width / 2,
                height,
                edit,
                font,
                lt -> {
                    currentlyEditing = lt;
                    editor.reload();
                    editTitle.setMessage(Component.translatable("tfcgenviewer.rock_editor.currently_editing_layer", lt.title));
                },
                () -> currentlyEditing
        );
        private final EditBox input = new EditBox(font, width / 2 + 24, height - 46, width / 2 - 30, 16, CommonComponents.EMPTY);
        private final ImageButton addButton = new ImageButton(width / 2 + 2, height - 48, 20, 20, 40 ,0, 20, RockSettingsDisplay.GUI_ELEMENTS, 64, 64, b -> {
            final String val = input.getValue();
            // TODO: Sanitize input values so they won't break the graphing site
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
                edit
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
        private final Button add = Button.builder(ADD_LAYER_DEFINITION, b -> editor.add()).build();

        private boolean add(MutableRockLayerSettings.MutableLayerData mld) {
            return display.add(mld);
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
