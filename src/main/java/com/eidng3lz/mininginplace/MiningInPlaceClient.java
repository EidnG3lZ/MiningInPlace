package com.eidng3lz.mininginplace;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.ConcurrentHashMap;

@Mod(value = MiningInPlace.MODID, dist = Dist.CLIENT)
public class MiningInPlaceClient {
    public static ConcurrentHashMap<Config.ServerConfigs, Object> serverConfigs = new ConcurrentHashMap<>();
    public static final Lazy<KeyMapping> KEY_MAPPING = Lazy.of(
            () -> new KeyMapping(
                    "key.MiningInPlace.temporary_function_control",
                    KeyConflictContext.IN_GAME,
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_GRAVE_ACCENT,
                    "key.categories.misc"
            )
    );
    public static boolean previousTickKeyState = false;
    public static boolean keyState = false;

    public MiningInPlaceClient(ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
