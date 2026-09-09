package com.thevortex.allthetweaks;

import com.thevortex.allthetweaks.blocks.TweakBlocks;
import com.thevortex.allthetweaks.client.discord.DiscordRpcManager;
import com.thevortex.allthetweaks.config.Configuration;
import com.thevortex.allthetweaks.config.Reference;
import com.thevortex.allthetweaks.proxy.MFProxy;
import com.thevortex.allthetweaks.proxy.MyCons;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.embeddedt.modernfix.core.ModernFixMixinPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;

import java.io.IOException;
import java.util.Optional;

@Mod(AllTheTweaks.MODID)
public class AllTheTweaks
{
    public static final String MODID = "allthetweaks";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static boolean configFire = false;
    private static Runnable runnableCallback;
    public static boolean BCC;
    public static int ModsLoaded;
    public static Optional<? extends ModContainer> mfContainer;
    public static boolean brandingModernFix = false;
    public AllTheTweaks(IEventBus modEventBus, ModContainer modContainer) {
        MixinBootstrap.init();

        modContainer.registerConfig(ModConfig.Type.COMMON, Configuration.COMMON_SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, Configuration.CLIENT_SPEC);
        Configuration.loadConfig(Configuration.COMMON_SPEC, FMLPaths.CONFIGDIR.get().resolve("allthetweaks-common.toml"));
     
        // Register ourselves for server and other game events we are interested in
        TweakBlocks.BLOCKS.register(modEventBus);
        TweakBlocks.ITEMS.register(modEventBus);
        TweakBlocks.CREATIVE_TABS.register(modEventBus);
        if(ModList.get().isLoaded("bcc")){
            BCC = true;
        } else {
            BCC = false;
        }
        ModsLoaded = ModList.get().size();
        mfContainer = ModList.get().getModContainerById("modernfix");
        if (ModList.get().isLoaded("modernfix")) {
            brandingModernFix = MFProxy.brandingEnabled();
        }

        //NeoForge.EVENT_BUS.register(Configuration.class);
        //NeoForge.EVENT_BUS.register(Events.class);

        modEventBus.addListener(this::postLoadEvent);
    }

    private void postLoadEvent(FMLLoadCompleteEvent event){
        if (runnableCallback != null) {
            runnableCallback.run();
        }
    }

    public static void setRunnableCallback(Runnable callback) {
        runnableCallback = callback;
    }

    @EventBusSubscriber(modid = Reference.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static class ClientProxy {

        @SubscribeEvent
        public static void setupClient(FMLClientSetupEvent evt) {
            NeoForge.EVENT_BUS.addListener(DiscordRpcManager::onClientTick);
            evt.enqueueWork(DiscordRpcManager::start);
            evt.enqueueWork(ClientProxy::applyWindowAppearance);
        }

        @SubscribeEvent
        public static void onConfigReloading(ModConfigEvent.Reloading event) {
            if (event.getConfig().getSpec() == Configuration.COMMON_SPEC) {
                Configuration.refreshPackProfile();
                ClientProxy.applyWindowAppearance();
            }

            DiscordRpcManager.refreshFromConfig();
        }

        private static void applyWindowAppearance() {
            Minecraft.getInstance().updateTitle();

            try {
                MyCons.setWindowIcon();
            } catch (IOException e) {
                AllTheTweaks.LOGGER.error("Failed to set window icon", e);
            }
        }
    }
}
    

