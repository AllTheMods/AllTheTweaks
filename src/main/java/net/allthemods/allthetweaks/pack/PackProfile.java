package net.allthemods.allthetweaks.pack;

/**
 * Identity of the modpack this instance is branded as. Every field is supplied by the
 * {@code pack} section of the common config, so a pack only needs a config change to brand itself.
 */
public record PackProfile(
        String displayName,
        long applicationId,
        String curseforgeUrl,
        String logoKey,
        String icon16,
        String icon32
) {

    /**
     * Values used until the common config is loaded, and the defaults written into a fresh config.
     */
    public static final PackProfile DEFAULT = new PackProfile(
            "All The Mods 11",
            1495847949358465084L,
            "https://www.curseforge.com/minecraft/modpacks/all-the-mods-11",
            "icon",
            "",
            ""
    );
}
