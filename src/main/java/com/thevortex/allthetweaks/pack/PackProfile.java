package com.thevortex.allthetweaks.pack;

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

    public static final PackProfile DEFAULT = PackProfile.ofPackMode("All The Mods 10", 1252859134282432572L, "atm10");

    public static PackProfile ofPackMode(String displayName, long applicationId, String largeImage) {
        return new PackProfile(displayName, applicationId, "", largeImage, "", "", "", List.of());
    }
}
