package com.notenoughmail.tfcgenviewer.screen;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.DataResult;
import com.notenoughmail.tfcgenviewer.mixin.RockLayerSettingsAccessor;
import com.notenoughmail.tfcgenviewer.util.ColorUtil;
import com.notenoughmail.tfcgenviewer.util.RockLayerSettingsBuilder;
import com.notenoughmail.tfcgenviewer.util.custom.ExpiringTextWidget;
import com.notenoughmail.tfcgenviewer.util.custom.IAmATabWithNonWidgetChildren;
import com.notenoughmail.tfcgenviewer.util.custom.RockSettingsEditor;
import com.notenoughmail.tfcgenviewer.util.custom.SlightlyImprovedTabManager;
import net.dries007.tfc.world.settings.RockLayerSettings;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
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
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class EditRocksScreen extends Screen {

    public static Component
            TITLE = Component.translatable("tfcgenviewer.rock_editor.title"),
            VALIDATE = Component.translatable("tfcgenviewer.rock_editor.validate"),
            GRAPH = Component.translatable("tfcgenviewer.rock_editor.graph"),
            VALIDATE_SUCCESS = Component.translatable("tfcgenviewer.rock_editor.validate.success"),
            VALIDATE_SUCCESS_SPECIAL = Component.translatable("tfcgenviewer.rock_editor.validate.success_special"),
            ROCK_SETTINGS = Component.translatable("tfcgenviewer.rock_editor.rock_settings");

    private final TabManager tabManager = new SlightlyImprovedTabManager<>(this::addRenderableWidget, this::removeWidget, this::addRenderableWidget, this::removeWidget);
    @Nullable
    private TabNavigationBar tabNavigationBar;
    @Nullable
    private GridLayout bottomButtons;
    @Nullable
    private Either<RockLayerSettings, DataResult.PartialResult<RockLayerSettings>> built;

    private final PreviewGenerationScreen parent;
    private final RockLayerSettings before;
    private final RockLayerSettingsBuilder edit;
    @Nullable
    private ExpiringTextWidget validationMessage;

    public EditRocksScreen(PreviewGenerationScreen parent, RockLayerSettings before) {
        super(TITLE);
        this.parent = parent;
        this.before = before;
        edit = RockLayerSettingsBuilder.init(before);
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    @Override
    public void onClose() {
        assert minecraft != null;
        if (validate()) {
            assert built != null;
            minecraft.setScreen(parent);
            parent.setRocks(built.orThrow());
        }
    }

    @Override
    public void removed() {
        if (validate()) {
            assert built != null;
            parent.setRocks(built.orThrow());
        }
    }

    @Override
    protected void init() {
        tabNavigationBar = TabNavigationBar.builder(tabManager, width).addTabs(
                new SettingsTab()
        ).build();
        addRenderableWidget(tabNavigationBar);
        // TODO : For some reason the backgrounds of the buttons do not render, onl the text is visible | Find out why
        bottomButtons = new GridLayout().columnSpacing(4);
        final GridLayout.RowHelper rowHelper = bottomButtons.createRowHelper(4);
        rowHelper.addChild(Button.builder(PreviewGenerationScreen.APPLY, b -> back(true)).build());
        rowHelper.addChild(Button.builder(VALIDATE, b -> {
            if (validate()) {
                validationMessage = addRenderableWidget(new ExpiringTextWidget(
                        this,
                        font,
                        ColorUtil.COLOR_GENERATOR.nextFloat() > 0.95F ?
                                VALIDATE_SUCCESS_SPECIAL :
                                VALIDATE_SUCCESS,
                        60));
            }
        }).build());
        rowHelper.addChild(Button.builder(GRAPH, b -> graph()).build());
        rowHelper.addChild(Button.builder(CommonComponents.GUI_CANCEL, b -> back(false)).build());
        bottomButtons.visitWidgets(w -> {
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
            FrameLayout.centerInRectangle(bottomButtons, 0, height - 36, width, 36);
            final int i = tabNavigationBar.getRectangle().bottom();
            tabManager.setTabArea(new ScreenRectangle(0, i, width, bottomButtons.getY() - i));
        }
    }

    @Override
    public void tick() {
        if (validationMessage != null && validationMessage.tick()) {
            removeWidget(validationMessage);
            validationMessage = null;
        }
    }

    private boolean validate() {
        build();
        assert built != null;
        return built.map(settings -> true, partial -> {
            validationMessage = addRenderableWidget(new ExpiringTextWidget(
                    this,
                    font,
                    Component.translatable("tfcgenviewer.rock_editor.validate.fail", partial.message()),
                    60
            ));
            return false;
        });
    }

    private void build() {
        built = ((RockLayerSettingsAccessor) (Object) before).tfcgenviewer$processData(edit.build()).get();
    }

    private void graph() {
        if (validate()) {

        } else {

        }
    }

    private void back(boolean keepChanges) {
        assert minecraft != null;
        if (keepChanges) {
            // These two if statements cannot be merged together
            if (validate()) {
                assert built != null;
                minecraft.setScreen(parent);
                parent.setRocks(built.orThrow());
            }
        } else {
            minecraft.setScreen(parent);
        }
    }

    class SettingsTab implements Tab, IAmATabWithNonWidgetChildren {

        private final RockSettingsEditor editor = new RockSettingsEditor(minecraft, width, height, edit.rocks, font);

        @Override
        public Component getTabTitle() {
            return ROCK_SETTINGS;
        }

        @Override
        public void visitChildren(Consumer<AbstractWidget> pConsumer) {}

        @Override
        public void doLayout(ScreenRectangle pRectangle) {}

        @SuppressWarnings("unchecked")
        @Override
        public <T extends GuiEventListener & Renderable> void visitNonWidgets(Consumer<T> visitor) {
            visitor.accept((T) editor); // Yes, the editor is within the bounds, but a cast is still required
        }
    }
}
