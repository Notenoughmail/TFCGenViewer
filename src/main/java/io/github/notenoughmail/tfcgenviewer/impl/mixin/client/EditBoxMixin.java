package io.github.notenoughmail.tfcgenviewer.impl.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.notenoughmail.tfcgenviewer.client.options.EditBoxValueSet;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EditBox.class)
public abstract class EditBoxMixin {

    @Shadow
    public abstract int getInnerWidth();

    @WrapOperation(method = "renderWidget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)I"))
    private int tfcgenviewer$DrawHintWithScrollingString(GuiGraphics instance, Font font, Component text, int x, int y, int color, boolean dropShadow, Operation<Integer> original) {
        if (((EditBox) (Object) this) instanceof EditBoxValueSet.ValueEditBox) {
            return instance.drawScrollingString(font, text, x, x + getInnerWidth(), y, color);
        }
        return original.call(instance, font, text, x, y, color, dropShadow);
    }
}
