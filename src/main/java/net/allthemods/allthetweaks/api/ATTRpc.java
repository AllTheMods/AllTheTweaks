package net.allthemods.allthetweaks.api;

import net.allthemods.allthetweaks.client.discord.DiscordRpcManager;

import java.util.function.Supplier;

public final class ATTRpc {

    private ATTRpc() { }

    public static void setDetails(String line) {
        DiscordRpcManager.setDetails(line);
    }

    public static void clearDetails() {
        DiscordRpcManager.clearDetails();
    }

    public static void setState(String line) {
        DiscordRpcManager.setState(line);
    }

    public static void clearState() {
        DiscordRpcManager.clearState();
    }

    public static void setDetailsProvider(Supplier<String> provider) {
        DiscordRpcManager.setDetailsProvider(provider);
    }

    public static void setStateProvider(Supplier<String> provider) {
        DiscordRpcManager.setStateProvider(provider);
    }

    public static void setMenuStateProvider(Supplier<String> provider) {
        DiscordRpcManager.setMenuStateProvider(provider);
    }

    public static void setSmallImageProvider(Supplier<String> provider) {
        DiscordRpcManager.setSmallImageProvider(provider);
    }

    public static void setMenuState(String line) {
        DiscordRpcManager.setMenuState(line);
    }

    public static void clearMenuState() {
        DiscordRpcManager.clearMenuState();
    }

    public static void setSmallImage(String image) {
        DiscordRpcManager.setSmallImage(image);
    }

    public static void clearSmallImage() {
        DiscordRpcManager.clearSmallImage();
    }

    public static void setProgress(int current, int max) {
        DiscordRpcManager.setProgress(current, max);
    }

    public static void clearProgress() {
        DiscordRpcManager.clearProgress();
    }
}
