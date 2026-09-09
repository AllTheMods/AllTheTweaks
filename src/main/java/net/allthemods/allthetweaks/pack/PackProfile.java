package net.allthemods.allthetweaks.pack;

import java.util.List;

public record PackProfile(
        String displayName,
        long applicationId,
        String curseforgeUrl,
        String largeImage,
        String smallImage,
        String icon16,
        String icon32,
        List<String> idleMessages
) {

    public static final PackProfile DEFAULT = new PackProfile(
            "All The Mods 11",
            1495847949358465084L,
            "https://www.curseforge.com/minecraft/modpacks/all-the-mods-11",
            "icon",
            "",
            "",
            "",
            List.of()
    );
}
