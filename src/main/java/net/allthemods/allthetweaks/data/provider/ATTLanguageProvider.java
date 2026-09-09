package net.allthemods.allthetweaks.data.provider;

import net.neoforged.neoforge.common.data.LanguageProvider;

import net.minecraft.data.PackOutput;

import net.allthemods.allthetweaks.api.ATT;
import net.allthemods.allthetweaks.core.ATTRegistry;

import java.util.Locale;

public class ATTLanguageProvider extends LanguageProvider {
    
    public ATTLanguageProvider(PackOutput output) {
        super(output, ATT.MOD_ID, "en_us");
    }
    
    @Override
    protected void addTranslations() {
        this.add(String.format(Locale.ROOT, "creative_tab.%s", ATT.MOD_ID), "AllTheTweaks");
        
        this.add(ATTRegistry.ATM_TROPHY.get(), "All The Mods Trophy");
        this.add(ATTRegistry.ENDERPEARL_BLOCK.get(), "Ender Pearl Block");
        this.add(ATTRegistry.ATMSTAR_BLOCK.get(), "ATM Star Block");
        this.add(ATTRegistry.GREGSTAR_BLOCK.get(), "GregStar Block");
        this.add(ATTRegistry.NETHERSTAR_BLOCK.get(), "Nether Star Block");
        this.add(ATTRegistry.MINI_END_BLOCK.get(), "Miniature End Portal");
        this.add(ATTRegistry.MINI_EXIT_BLOCK.get(), "Miniature Exit Portal");
        this.add(ATTRegistry.MINI_NETHER_BLOCK.get(), "Miniature Nether Portal");
        
        this.add(ATTRegistry.ATMSTAR.get(), "ATM Star");
        this.add(ATTRegistry.GREGSTAR.get(), "GregStar");
        this.add(ATTRegistry.CATALYST.get(), "AllTheCatalystium");
        this.add(ATTRegistry.ATMSTAR_SHARD.get(), "ATM Star Shard");
        this.add(ATTRegistry.PHIL_FUEL.get(), "Philosopher's Fuel");
        this.add(ATTRegistry.NEXIUM_EMITTER.get(), "Nexium Emitter");
        this.add(ATTRegistry.DRAGON_SOUL.get(), "Dragon Soul");
        this.add(ATTRegistry.WITHER_COMPASS.get(), "Wither's Compass");
        this.add(ATTRegistry.PULSE_BLACK_HOLE.get(), "Pulsating Black Hole");
        this.add(ATTRegistry.OBLIVION_SHARD.get(), "Oblivion Shard");
        this.add(ATTRegistry.IMPROBABLE_PROBABILITY_DEVICE.get(), "Improbable Probability Device");
        this.add(ATTRegistry.DIM_SEED.get(), "Dimensional Seed");
        this.add(ATTRegistry.PATRICK_STAR.get(), "Patrick Star");
        
        this.add("allthetweaks.configuration.rpc_enabled", "Discord Rich Presence Enabled");
        this.add("allthetweaks.configuration.pack", "Pack");
        this.add("allthetweaks.configuration.pack.tooltip", "Identity of this pack: the name it shows under, its links, and its Discord presence.");
        this.add("allthetweaks.configuration.discord.tooltip", "Which Discord application the Rich Presence is published under.");
        this.add("allthetweaks.configuration.window.tooltip", "Icons for the game window.");
        this.add("allthetweaks.configuration.pack.display_name.tooltip", "Shown in the window title and on the main menu branding lines.");
        this.add("allthetweaks.configuration.pack.curseforge_url.tooltip", "Opened by the CurseForge button on the Discord Rich Presence card.");
        this.add("allthetweaks.configuration.pack.discord.application_id.tooltip", "The Discord application the presence is published under. Its name and uploaded art are what players see on the card, so a pack with its own branding needs its own application.");
        this.add("allthetweaks.configuration.pack.discord.large_image.tooltip", "Main image on the presence card. Accepts an asset key uploaded to the Discord application or a public image URL. An unknown key falls back to the application's own icon.");
        this.add("allthetweaks.configuration.pack.discord.small_image.tooltip", "Small icon drawn over the corner of the large image. Accepts an asset key from the Discord application or a public image URL. Blank means no corner icon.");
        this.add("allthetweaks.configuration.pack.discord.idle_messages.tooltip", "Lines shown instead of Main Menu while the player sits in the menu. One is picked at random and swapped for another every minute.");
        this.add("allthetweaks.configuration.pack.window.icon_16.tooltip", "File name inside the allthetweaks config folder. Leave blank to use the bundled icon.");
        this.add("allthetweaks.configuration.pack.window.icon_32.tooltip", "File name inside the allthetweaks config folder. Leave blank to use the bundled icon.");
        this.add("allthetweaks.configuration.rpc_enabled.tooltip", "Publish this pack as your Discord activity. Takes effect without restarting.");
        this.add("allthetweaks.configuration.discord", "Discord");
        this.add("allthetweaks.configuration.window", "Window");
        this.add("allthetweaks.configuration.pack.display_name", "Pack Name");
        this.add("allthetweaks.configuration.pack.curseforge_url", "Pack CurseForge Page");
        this.add("allthetweaks.configuration.pack.discord.application_id", "Discord Application ID");
        this.add("allthetweaks.configuration.pack.discord.large_image", "Discord Main Image");
        this.add("allthetweaks.configuration.pack.discord.small_image", "Discord Corner Icon");
        this.add("allthetweaks.configuration.pack.discord.idle_messages", "Main Menu Idle Messages");
        this.add("allthetweaks.configuration.pack.window.icon_16", "Window Icon (16x16)");
        this.add("allthetweaks.configuration.pack.window.icon_32", "Window Icon (32x32)");
        
        this.add("allthetweaks.valhelsia_core.cosmeticsWardrobe", "Valhelsia Team is selling Capes, violating Commercial Usage Guidelines!");
        this.add("allthetweaks.valhelsia_core.dontbuyCosmetics", "Until this ends, ALL Valhelsia cosmetic features will be disabled in ATM ModPacks.");
        this.add("travelerstitles.allthemodium.the_other", "The Other");
        this.add("travelerstitles.allthemodium.the_other.color", "153333");
    }
}
