package net.allthemods.allthetweaks.client.window;

import net.minecraft.client.Minecraft;

import net.neoforged.fml.loading.FMLPaths;

import net.allthemods.allthetweaks.ATTConfig;
import net.allthemods.allthetweaks.AllTheTweaks;
import net.allthemods.allthetweaks.api.ATT;
import net.allthemods.allthetweaks.pack.PackProfile;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Window;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class ATTWindowModifier {

    private static final String BUNDLED_ICON_16 = String.format("/assets/%s/icons/allthemods_16x16.png", ATT.MOD_ID);
    private static final String BUNDLED_ICON_32 = String.format("/assets/%s/icons/allthemods_32x32.png", ATT.MOD_ID);

    private ATTWindowModifier() { }

    public static void apply() {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> {
            Window window = minecraft.getWindow();
            PackProfile profile = ATTConfig.packProfile();

            minecraft.updateTitle();

            try {
                ATTWindowModifier.setIcon(window, profile);
            } catch (Exception exception) {
                AllTheTweaks.LOGGER.error("Failed to load window icons for {}", profile.displayName(), exception);
            }
        });
    }

    private static void setIcon(Window window, PackProfile profile) throws IOException {
        int platform = GLFW.glfwGetPlatform();
        if (platform != GLFW.GLFW_PLATFORM_WIN32 && platform != GLFW.GLFW_PLATFORM_X11) return;

        List<ByteBuffer> allocated = new ArrayList<>();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            GLFWImage.Buffer icons = GLFWImage.malloc(2, stack);

            ATTWindowModifier.loadIcon(icons, allocated, 0, profile.icon16(), ATTWindowModifier.BUNDLED_ICON_16);
            ATTWindowModifier.loadIcon(icons, allocated, 1, profile.icon32(), ATTWindowModifier.BUNDLED_ICON_32);

            GLFW.glfwSetWindowIcon(window.handle(), icons.position(0));
        } finally {
            for (ByteBuffer buffer : allocated) {
                MemoryUtil.memFree(buffer);
            }
        }
    }

    private static void loadIcon(
            GLFWImage.Buffer icons,
            List<ByteBuffer> allocated,
            int index,
            String configuredName,
            String bundledPath
    ) throws IOException {
        try (InputStream stream = ATTWindowModifier.openIcon(configuredName, bundledPath)) {
            try (NativeImage image = NativeImage.read(stream)) {
                ByteBuffer pixels = MemoryUtil.memAlloc(image.getWidth() * image.getHeight() * 4);
                allocated.add(pixels);

                pixels.asIntBuffer().put(image.getPixelsABGR());

                icons.position(index);
                icons.width(image.getWidth());
                icons.height(image.getHeight());
                icons.pixels(pixels);
            }
        }
    }

    private static InputStream openIcon(String configuredName, String bundledPath) throws IOException {
        Path configured = ATTWindowModifier.resolveInConfigFolder(configuredName);

        if (configured != null) {
            if (Files.isRegularFile(configured)) return Files.newInputStream(configured);
            AllTheTweaks.LOGGER.warn("Window icon {} is missing, falling back to the bundled icon", configured);
        }

        InputStream bundled = ATTWindowModifier.class.getResourceAsStream(bundledPath);
        if (bundled == null) throw new IOException("Missing icon resource: " + bundledPath);
        return bundled;
    }

    private static Path resolveInConfigFolder(String configuredName) {
        if (configuredName.isBlank()) return null;

        Path folder = FMLPaths.CONFIGDIR.get().resolve(ATT.MOD_ID).normalize();

        try {
            Path resolved = folder.resolve(configuredName).normalize();
            if (resolved.startsWith(folder)) return resolved;

            AllTheTweaks.LOGGER.warn("Window icon {} resolves outside {}, falling back to the bundled icon", configuredName, folder);
        } catch (InvalidPathException exception) {
            AllTheTweaks.LOGGER.warn("Window icon {} is not a usable file name, falling back to the bundled icon", configuredName);
        }

        return null;
    }
}
