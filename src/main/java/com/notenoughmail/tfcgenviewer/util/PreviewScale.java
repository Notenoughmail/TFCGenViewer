package com.notenoughmail.tfcgenviewer.util;

import com.mojang.blaze3d.platform.NativeImage;
import com.notenoughmail.tfcgenviewer.TFCGenViewer;
import com.notenoughmail.tfcgenviewer.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.Lazy;

import java.util.Objects;
import java.util.function.Supplier;

public enum PreviewScale {
    _0,
    _1,
    _2,
    _3,
    _4,
    _5,
    _6;

    public final int previewSize, lineWidth;
    public final String previewSizeKm;
    public final Component sizeDisplay;
    public final ResourceLocation textureId;
    private final Supplier<DynamicTexture> texture;

    PreviewScale() {
        previewSize = 2 << (ordinal() + 4); // == Math.pow(2, scale + 5)
        lineWidth = previewSize >> 9; // == previewSize(scale) / 128
        previewSizeKm = "%.1f".formatted(previewSize * 128 / 1000F);
        sizeDisplay = Component.translatable("tfcgenviewer.preview_world.km", previewSizeKm);
        textureId = TFCGenViewer.identifier("preview/" + ordinal());
        texture = Lazy.of(() -> {
            try {
                var tex = new DynamicTexture(previewSize, previewSize, false);
                Minecraft.getInstance().getTextureManager().register(textureId, tex);
                return tex;
            } catch (Exception ex) {
                TFCGenViewer.LOGGER.error("Could not make dynamic texture for size %d (scale %d)! Error:\n".formatted(previewSize, ordinal()), ex);
                return null;
            }
        });
    }

    public static final PreviewScale[] VALUES = values();

    public static OptionInstance<PreviewScale> option() {
        return new OptionInstance<>(
                "tfcgenviewer.preview_world.preview_scale",
                OptionInstance.noTooltip(),
                (caption, s) -> Options.genericValueLabel(
                        caption,
                        s.sizeDisplay
                ),
                new OptionInstance.IntRange(0, VALUES.length - 1).xmap(i -> VALUES[i], Enum::ordinal), // TODO: 1.4.1 | Change this to a custom range type to fix 242 km only appearing at the very edge of the slider
                VALUES[Config.defaultPreviewSize.get()],
                s -> {}
        );
    }

    public static void clearPreviews(NativeImage currentImage) {
        for (PreviewScale scale : VALUES) {
            // If someone is really "lucky" the game will attempt to render an image which has been close via this
            // If they decide to change this config mid-generation and it explodes, that's on them tbh
            if (!Config.useThrobber.get() && scale.texture.get().getPixels() == currentImage) continue;
            // Free the previously used images from memory
            scale.texture.get().setPixels(null);
        }
    }

    public void upload(NativeImage image) {
        Objects.requireNonNull(texture, "Tried to upload image to null texture, meaning it failed to initialize earlier");
        clearPreviews(null);
        texture.get().setPixels(image);
        texture.get().upload();
    }
}
