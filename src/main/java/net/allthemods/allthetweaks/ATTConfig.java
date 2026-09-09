package net.allthemods.allthetweaks;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

import net.allthemods.allthetweaks.pack.PackProfile;

public final class ATTConfig {

    public static final ModConfigSpec.ConfigValue<String> PACK_DISPLAY_NAME;
    public static final ModConfigSpec.ConfigValue<String> PACK_CURSEFORGE_URL;
    public static final ModConfigSpec.LongValue PACK_DISCORD_APPLICATION_ID;
    public static final ModConfigSpec.ConfigValue<String> PACK_DISCORD_LOGO_KEY;
    public static final ModConfigSpec.ConfigValue<String> PACK_ICON_16;
    public static final ModConfigSpec.ConfigValue<String> PACK_ICON_32;
    public static final ModConfigSpec COMMON;

    public static final ModConfigSpec.BooleanValue RPC_ENABLED;
    public static final ModConfigSpec CLIENT;

    private static volatile PackProfile packProfile = PackProfile.DEFAULT;

    static {
        ModConfigSpec.Builder common = new ModConfigSpec.Builder();

        common.push("pack");
        PACK_DISPLAY_NAME = common
                .comment("Pack name shown in the window title and on the branding lines")
                .translation("allthetweaks.configuration.pack.display_name")
                .define("display_name", PackProfile.DEFAULT.displayName());
        PACK_CURSEFORGE_URL = common
                .comment("Target of the CurseForge button on the Discord Rich Presence card")
                .translation("allthetweaks.configuration.pack.curseforge_url")
                .define("curseforge_url", PackProfile.DEFAULT.curseforgeUrl());

        common.push("discord");
        PACK_DISCORD_APPLICATION_ID = common
                .comment("Discord application the Rich Presence connects to")
                .translation("allthetweaks.configuration.pack.discord.application_id")
                .defineInRange("application_id", PackProfile.DEFAULT.applicationId(), 0L, Long.MAX_VALUE);
        PACK_DISCORD_LOGO_KEY = common
                .comment("Asset key of the large image registered on that Discord application")
                .translation("allthetweaks.configuration.pack.discord.logo_key")
                .define("logo_key", PackProfile.DEFAULT.logoKey());
        common.pop();

        common.push("window");
        PACK_ICON_16 = common
                .comment("16x16 window icon, read from the allthetweaks folder next to this file", "Leave blank to use the bundled icon")
                .translation("allthetweaks.configuration.pack.window.icon_16")
                .define("icon_16", PackProfile.DEFAULT.icon16());
        PACK_ICON_32 = common
                .comment("32x32 window icon, read from the allthetweaks folder next to this file", "Leave blank to use the bundled icon")
                .translation("allthetweaks.configuration.pack.window.icon_32")
                .define("icon_32", PackProfile.DEFAULT.icon32());
        common.pop(2);

        COMMON = common.build();

        ModConfigSpec.Builder client = new ModConfigSpec.Builder();
        RPC_ENABLED = client
                .comment("Enable Discord Rich Presence")
                .translation("allthetweaks.configuration.rpc_enabled")
                .define("rpc_enabled", true);
        CLIENT = client.build();
    }

    private ATTConfig() { }

    public static void register(ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, ATTConfig.COMMON);
        container.registerConfig(ModConfig.Type.CLIENT, ATTConfig.CLIENT);
    }

    /**
     * The pack this instance is branded as. Safe to call before the common config is loaded, in
     * which case it reports {@link PackProfile#DEFAULT}.
     */
    public static PackProfile packProfile() {
        return ATTConfig.packProfile;
    }

    public static void refreshPackProfile() {
        if (!ATTConfig.COMMON.isLoaded()) return;

        ATTConfig.packProfile = new PackProfile(
                ATTConfig.PACK_DISPLAY_NAME.get(),
                ATTConfig.PACK_DISCORD_APPLICATION_ID.get(),
                ATTConfig.PACK_CURSEFORGE_URL.get(),
                ATTConfig.PACK_DISCORD_LOGO_KEY.get(),
                ATTConfig.PACK_ICON_16.get(),
                ATTConfig.PACK_ICON_32.get()
        );
    }
}
