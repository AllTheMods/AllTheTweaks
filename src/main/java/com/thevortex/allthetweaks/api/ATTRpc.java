package com.thevortex.allthetweaks.api;

import com.thevortex.allthetweaks.client.discord.DiscordRpcManager;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

public final class ATTRpc {

    private ATTRpc() { }

    public static void setDetails(String line) {
        if (ATTRpc.unavailable()) return;
        DiscordRpcManager.setDetails(line);
    }

    public static void clearDetails() {
        if (ATTRpc.unavailable()) return;
        DiscordRpcManager.clearDetails();
    }

    public static void setState(String line) {
        if (ATTRpc.unavailable()) return;
        DiscordRpcManager.setState(line);
    }

    public static void clearState() {
        if (ATTRpc.unavailable()) return;
        DiscordRpcManager.clearState();
    }

    public static void setSmallImage(String image) {
        if (ATTRpc.unavailable()) return;
        DiscordRpcManager.setSmallImage(image);
    }

    public static void clearSmallImage() {
        if (ATTRpc.unavailable()) return;
        DiscordRpcManager.clearSmallImage();
    }

    public static void setProgress(int current, int max) {
        if (ATTRpc.unavailable()) return;
        DiscordRpcManager.setProgress(current, max);
    }

    public static void clearProgress() {
        if (ATTRpc.unavailable()) return;
        DiscordRpcManager.clearProgress();
    }

    private static boolean unavailable() {
        return FMLEnvironment.dist != Dist.CLIENT;
    }
}
