package com.thevortex.allthetweaks.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.thevortex.allthetweaks.pack.PackProfile;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = Reference.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class Configuration {
	public static final ModConfigSpec COMMON_SPEC;
	public static final Common COMMON;
	public static final ModConfigSpec CLIENT_SPEC;
	public static final Client CLIENT;

	private static volatile PackProfile packProfile = PackProfile.DEFAULT;

	static {
		final Pair<Common, ModConfigSpec> commonPair = new ModConfigSpec.Builder().configure(Common::new);
		COMMON_SPEC = commonPair.getRight();
		COMMON = commonPair.getLeft();

		final Pair<Client, ModConfigSpec> clientPair = new ModConfigSpec.Builder().configure(Client::new);
		CLIENT_SPEC = clientPair.getRight();
		CLIENT = clientPair.getLeft();
	}

	public static class Common {
		public final ModConfigSpec.IntValue mainmode;
		public final ModConfigSpec.BooleanValue discord;
		public final ModConfigSpec.IntValue majorver;
		public final ModConfigSpec.IntValue minorver;
		public final ModConfigSpec.IntValue minorrevver;

		public final ModConfigSpec.ConfigValue<String> packDisplayName;
		public final ModConfigSpec.ConfigValue<String> packCurseforgeUrl;
		public final ModConfigSpec.LongValue packApplicationId;
		public final ModConfigSpec.ConfigValue<String> packLargeImage;
		public final ModConfigSpec.ConfigValue<String> packSmallImage;
		public final ModConfigSpec.ConfigValue<List<? extends String>> packIdleMessages;
		public final ModConfigSpec.ConfigValue<String> packIcon16;
		public final ModConfigSpec.ConfigValue<String> packIcon32;

		public Common(ModConfigSpec.Builder BUILDER) {
			BUILDER.push("packmode");
			mainmode = BUILDER.comment("ATM = 0 SLOP = 1 SKY = 2 MAGIC = 3 EXPERT = 4 GRAVITAS = 5 LITE = 6 AOA = 7 MON = 8",
					"Supplies the pack identity for anything left blank in the pack section").defineInRange("enable",
					0, 0, 8);
			BUILDER.pop();

			BUILDER.push("discord");
			discord = BUILDER.comment("Enable Discord Rich Presence").define("discord",true);
			BUILDER.pop();

			BUILDER.push("packversionmaj");
			majorver = BUILDER.comment("Pack Release Version Format : X").defineInRange("major",
					1, 0, 32768);
			BUILDER.pop();

			BUILDER.push("packversionmin");
			minorver = BUILDER.comment("Pack Minor Version : X").defineInRange("minor",
					0, 0, 32768);
			BUILDER.pop();

			BUILDER.push("packversionminrev");
			minorrevver = BUILDER.comment("Pack Minor Version Revision : X").defineInRange("minorrev",
					0, 0, 32768);
			BUILDER.pop();

			BUILDER.push("pack");
			packDisplayName = BUILDER
					.comment("Pack name shown in the window title and on the branding lines",
							"Leave blank to take it from the packmode profile")
					.translation("allthetweaks.configuration.pack.display_name")
					.define("display_name", "");
			packCurseforgeUrl = BUILDER
					.comment("Target of the CurseForge button on the Discord Rich Presence card",
							"Leave blank for no CurseForge button")
					.translation("allthetweaks.configuration.pack.curseforge_url")
					.define("curseforge_url", "");

			BUILDER.push("discord");
			packApplicationId = BUILDER
					.comment("Discord application the Rich Presence connects to",
							"Leave at 0 to take it from the packmode profile")
					.translation("allthetweaks.configuration.pack.discord.application_id")
					.defineInRange("application_id", 0L, 0L, Long.MAX_VALUE);
			packLargeImage = BUILDER
					.comment("Asset key or image URL for the main image on the card",
							"Leave blank to take it from the packmode profile")
					.translation("allthetweaks.configuration.pack.discord.large_image")
					.define("large_image", "");
			packSmallImage = BUILDER
					.comment("Asset key or image URL for the small corner icon on the card",
							"Leave blank for no corner icon")
					.translation("allthetweaks.configuration.pack.discord.small_image")
					.define("small_image", "");
			packIdleMessages = BUILDER
					.comment("Lines shown instead of the plain menu line while the player sits in the menu",
							"One is picked at random and swapped for another every minute",
							"Leave empty to always show the plain menu line")
					.translation("allthetweaks.configuration.pack.discord.idle_messages")
					.defineList("idle_messages",
							List.of("Getting coffee", "Making a sandwich", "Reading the changelog", "Looking for the right button", "Doomscrolling"),
							() -> "Staring at the menu",
							Configuration::isIdleMessage);
			BUILDER.pop();

			BUILDER.push("window");
			packIcon16 = BUILDER
					.comment("16x16 window icon, read from the allthetweaks folder next to this file",
							"Leave blank to use the bundled icon")
					.translation("allthetweaks.configuration.pack.window.icon_16")
					.define("icon_16", "");
			packIcon32 = BUILDER
					.comment("32x32 window icon, read from the allthetweaks folder next to this file",
							"Leave blank to use the bundled icon")
					.translation("allthetweaks.configuration.pack.window.icon_32")
					.define("icon_32", "");
			BUILDER.pop(2);
		}
	}

	public static class Client {
		public final ModConfigSpec.BooleanValue rpcEnabled;

		public Client(ModConfigSpec.Builder BUILDER) {
			rpcEnabled = BUILDER
					.comment("Enable Discord Rich Presence for this player",
							"The pack turns it off for everyone with discord.discord in the common config")
					.translation("allthetweaks.configuration.rpc_enabled")
					.define("rpc_enabled", true);
		}
	}

	public static void loadConfig(ModConfigSpec spec, Path path) {
        final CommentedFileConfig configData =
                CommentedFileConfig.builder(path)
                        .sync()
                        .autosave()
                        .writingMode(WritingMode.REPLACE)
                        .build();

        configData.load();

        spec.correct(configData);
		configData.save();
    }

	public static PackProfile packProfile() {
		return Configuration.packProfile;
	}

	public static void refreshPackProfile() {
		if (!Configuration.COMMON_SPEC.isLoaded()) {
			return;
		}

		Configuration.packProfile = Configuration.resolvePackProfile();
	}

	private static PackProfile resolvePackProfile() {
		PackProfile fallback = Configuration.packModeProfile(Configuration.COMMON.mainmode.get());
		long applicationId = Configuration.COMMON.packApplicationId.get();

		return new PackProfile(
				Configuration.orFallback(Configuration.COMMON.packDisplayName.get(), fallback.displayName()),
				applicationId != 0L ? applicationId : fallback.applicationId(),
				Configuration.COMMON.packCurseforgeUrl.get().strip(),
				Configuration.orFallback(Configuration.COMMON.packLargeImage.get(), fallback.largeImage()),
				Configuration.COMMON.packSmallImage.get().strip(),
				Configuration.COMMON.packIcon16.get().strip(),
				Configuration.COMMON.packIcon32.get().strip(),
				Configuration.readIdleMessages()
		);
	}

	private static PackProfile packModeProfile(int mode) {
		return switch (mode) {
			case 1 -> PackProfile.ofPackMode(cfgSLOP.DISPLAY, cfgSLOP.IPCC, cfgSLOP.ATM);
			case 2 -> PackProfile.ofPackMode(cfgSKY.DISPLAY, cfgSKY.IPCC, cfgSKY.ATM);
			case 3 -> PackProfile.ofPackMode(cfgMAGIC.DISPLAY, cfgMAGIC.IPCC, cfgMAGIC.ATM);
			case 4 -> PackProfile.ofPackMode(cfgExpert.DISPLAY, cfgExpert.IPCC, cfgExpert.ATM);
			case 5 -> PackProfile.ofPackMode(cfgGrav.DISPLAY, cfgGrav.IPCC, cfgGrav.ATM);
			case 6 -> PackProfile.ofPackMode(cfgLite.DISPLAY, cfgLite.IPCC, cfgLite.ATM);
			case 7 -> PackProfile.ofPackMode(cfgAoA.DISPLAY, cfgAoA.IPCC, cfgAoA.ATM);
			case 8 -> PackProfile.ofPackMode(cfgMon.DISPLAY, cfgMon.IPCC, cfgMon.ATM);
			default -> PackProfile.ofPackMode(cfgMain.DISPLAY, cfgMain.IPCC, cfgMain.ATM);
		};
	}

	private static String orFallback(String configured, String fallback) {
		String trimmed = configured.strip();

		return trimmed.isEmpty() ? fallback : trimmed;
	}

	private static boolean isIdleMessage(Object value) {
		if (!(value instanceof String entry)) {
			return false;
		}

		return entry.length() >= 2 && entry.length() <= 128;
	}

	private static List<String> readIdleMessages() {
		List<String> messages = new ArrayList<>();

		for (String entry : Configuration.COMMON.packIdleMessages.get()) {
			if (Configuration.isIdleMessage(entry)) {
				messages.add(entry);
			}
		}

		return List.copyOf(messages);
	}

	 @SuppressWarnings("unused")
    @SubscribeEvent
    public static void onLoad(final ModConfigEvent.Loading configEvent) {
        Configuration.refreshPackProfile();
	}
	@SuppressWarnings("unused")
    @SubscribeEvent
    public static void onreLoad(final ModConfigEvent.Reloading configEvent) {
        Configuration.refreshPackProfile();
	}
	}
