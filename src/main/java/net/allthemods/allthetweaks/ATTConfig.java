package net.allthemods.allthetweaks;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

import net.allthemods.allthetweaks.pack.PackProfile;

import java.util.ArrayList;
import java.util.List;

public final class ATTConfig {

    public static final ModConfigSpec.ConfigValue<String> PACK_DISPLAY_NAME;
    public static final ModConfigSpec.ConfigValue<String> PACK_CURSEFORGE_URL;
    public static final ModConfigSpec.LongValue PACK_DISCORD_APPLICATION_ID;
    public static final ModConfigSpec.ConfigValue<String> PACK_DISCORD_LARGE_IMAGE;
    public static final ModConfigSpec.ConfigValue<String> PACK_DISCORD_SMALL_IMAGE;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> PACK_DISCORD_IDLE_MESSAGES;
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
        PACK_DISCORD_LARGE_IMAGE = common
                .comment("Asset key or image URL for the main image on the card")
                .translation("allthetweaks.configuration.pack.discord.large_image")
                .define("large_image", PackProfile.DEFAULT.largeImage());
        PACK_DISCORD_SMALL_IMAGE = common
                .comment("Asset key or image URL for the small icon in the card's corner", "Leave blank for no corner icon")
                .translation("allthetweaks.configuration.pack.discord.small_image")
                .define("small_image", PackProfile.DEFAULT.smallImage());
        PACK_DISCORD_IDLE_MESSAGES = common
                .comment("Lines shown instead of \"Main Menu\" while the player sits in the menu",
                        "One is picked at random and swapped for another every minute",
                        "Leave empty to always show the plain menu line")
                .translation("allthetweaks.configuration.pack.discord.idle_messages")
                .defineList("idle_messages",
                        List.of("Getting coffee", "Making a sandwich", "Reading the changelog", "Looking for the right button", "Doomscrolling"),
                        () -> "Staring at the menu",
                        ATTConfig::isIdleMessage);
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

    public static PackProfile packProfile() {
        return ATTConfig.packProfile;
    }

    private static boolean isIdleMessage(Object value) {
        if (!(value instanceof String entry)) return false;

        return entry.length() >= 2 && entry.length() <= 128;
    }

    private static List<String> readIdleMessages() {
        List<String> messages = new ArrayList<>();

        for (String entry : ATTConfig.PACK_DISCORD_IDLE_MESSAGES.get()) {
            if (ATTConfig.isIdleMessage(entry)) messages.add(entry);
        }

        return List.copyOf(messages);
    }

    public static void refreshPackProfile() {
        if (!ATTConfig.COMMON.isLoaded()) return;

        ATTConfig.packProfile = new PackProfile(
                ATTConfig.PACK_DISPLAY_NAME.get(),
                ATTConfig.PACK_DISCORD_APPLICATION_ID.get(),
                ATTConfig.PACK_CURSEFORGE_URL.get(),
                ATTConfig.PACK_DISCORD_LARGE_IMAGE.get(),
                ATTConfig.PACK_DISCORD_SMALL_IMAGE.get(),
                ATTConfig.PACK_ICON_16.get(),
                ATTConfig.PACK_ICON_32.get(),
                ATTConfig.readIdleMessages()
        );
    }
}
