package net.allthemods.allthetweaks.client.discord;

import net.minecraft.client.Minecraft;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import net.allthemods.allthetweaks.ATTConfig;
import net.allthemods.allthetweaks.AllTheTweaks;
import net.allthemods.allthetweaks.pack.PackProfile;
import net.allthemods.allthetweaks.proxy.BCCProxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fun.crashsystem.jdrpc.DiscordIPC;
import fun.crashsystem.jdrpc.activity.Activity;
import fun.crashsystem.jdrpc.activity.ActivityType;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public final class DiscordRpcManager {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DiscordRpcManager.class);
    private static final Object LOCK = new Object();
    private static final Object CLIENT_LOCK = new Object();
    
    private static final String PARTY_ID = "allthetweaks";
    private static final String DISCORD_URL = "https://discord.gg/allthemods";
    private static final long RECONNECT_DELAY_MS = 10_000L;
    private static final int REFRESH_INTERVAL_TICKS = 20;
    private static final long IDLE_ROLL_INTERVAL_MS = 60_000L;
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(task -> {
        Thread thread = new Thread(task, "AllTheTweaks-DiscordRpc");
        thread.setDaemon(true);
        return thread;
    });
    private static final AtomicBoolean workerScheduled = new AtomicBoolean();
    
    private static boolean started;
    private static boolean shutdownHookRegistered;
    private static boolean updateRequested;
    private static boolean closeRequested;
    private static int ticksSinceRefresh;
    private static boolean deferredToSimpleRpc;
    private static boolean refreshFailureLogged;
    private static boolean connectLogged;
    private static boolean connectFailureLogged;
    private static volatile Progress progress;
    private static volatile String details;
    private static volatile String stateText;
    private static volatile String menuState;
    private static volatile Supplier<String> detailsProvider;
    private static volatile Supplier<String> stateProvider;
    private static volatile Supplier<String> menuStateProvider;
    private static volatile Supplier<String> smallImageProvider;
    private static volatile String smallImage;
    private static long startTime;
    private static long nextReconnectAt;
    private static long idleRolledAt;
    private static String idleMessage;
    
    private static DiscordIPC client;
    private static PackProfile pack = PackProfile.DEFAULT;
    private static RPCState state = RPCState.STARTING;
    private static Snapshot lastSnapshot;
    
    private DiscordRpcManager() { }
    
    public static void start() {
        if (ModList.get().isLoaded("simplerpc")) {
            DiscordRpcManager.deferredToSimpleRpc = true;
            DiscordRpcManager.LOGGER.info("Simple RPC is installed, leaving Discord Rich Presence to it");
            return;
        }

        synchronized (DiscordRpcManager.LOCK) {
            if (DiscordRpcManager.started) {
                DiscordRpcManager.LOGGER.debug("Cannot start, already started");
                return;
            }
            
            if (!DiscordRpcManager.shutdownHookRegistered) {
                Runtime.getRuntime().addShutdownHook(new Thread(DiscordRpcManager::shutdown, "AllTheTweaks-DiscordRpcShutdown"));
                DiscordRpcManager.shutdownHookRegistered = true;
            }
        }
        
        DiscordRpcManager.refreshFromConfig();
        DiscordRpcManager.LOGGER.info("AllTheTweaks Discord RPC Manager started");
    }
    
    public static void setDetails(String line) {
        DiscordRpcManager.details = DiscordRpcManager.clampLine(line);
    }

    private static String clampLine(String line) {
        String trimmed = line == null ? "" : line.strip();

        if (trimmed.length() < 2) return null;

        return trimmed.length() > 128 ? trimmed.substring(0, 128) : trimmed;
    }

    public static void clearDetails() {
        DiscordRpcManager.details = null;
    }

    public static void setState(String line) {
        DiscordRpcManager.stateText = DiscordRpcManager.clampLine(line);
    }

    public static void clearState() {
        DiscordRpcManager.stateText = null;
    }

    public static void setDetailsProvider(Supplier<String> provider) {
        DiscordRpcManager.detailsProvider = provider;
    }

    public static void setStateProvider(Supplier<String> provider) {
        DiscordRpcManager.stateProvider = provider;
    }

    public static void setMenuStateProvider(Supplier<String> provider) {
        DiscordRpcManager.menuStateProvider = provider;
    }

    public static void setSmallImageProvider(Supplier<String> provider) {
        DiscordRpcManager.smallImageProvider = provider;
    }

    private static void pollProviders() {
        if (DiscordRpcManager.detailsProvider != null) {
            DiscordRpcManager.details = DiscordRpcManager.clampLine(DiscordRpcManager.provide(DiscordRpcManager.detailsProvider));
        }

        if (DiscordRpcManager.stateProvider != null) {
            DiscordRpcManager.stateText = DiscordRpcManager.clampLine(DiscordRpcManager.provide(DiscordRpcManager.stateProvider));
        }

        if (DiscordRpcManager.menuStateProvider != null) {
            DiscordRpcManager.menuState = DiscordRpcManager.clampLine(DiscordRpcManager.provide(DiscordRpcManager.menuStateProvider));
        }

        if (DiscordRpcManager.smallImageProvider != null) {
            String image = DiscordRpcManager.provide(DiscordRpcManager.smallImageProvider);
            DiscordRpcManager.smallImage = image == null ? null : image.strip();
        }
    }

    private static String provide(Supplier<String> provider) {
        try {
            return provider.get();
        } catch (Exception exception) {
            DiscordRpcManager.LOGGER.debug("Discord RPC value provider failed", exception);
            return null;
        }
    }

    public static void setMenuState(String line) {
        DiscordRpcManager.menuState = DiscordRpcManager.clampLine(line);
    }

    public static void clearMenuState() {
        DiscordRpcManager.menuState = null;
    }

    public static void setSmallImage(String image) {
        DiscordRpcManager.smallImage = image == null ? "" : image.strip();
    }

    public static void clearSmallImage() {
        DiscordRpcManager.smallImage = null;
    }

    public static void setProgress(int current, int max) {
        DiscordRpcManager.progress = new Progress(Math.max(0, current), Math.max(0, max));
    }

    public static void clearProgress() {
        DiscordRpcManager.progress = null;
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        if (++DiscordRpcManager.ticksSinceRefresh < DiscordRpcManager.REFRESH_INTERVAL_TICKS) return;

        DiscordRpcManager.ticksSinceRefresh = 0;

        try {
            DiscordRpcManager.refreshFromConfig();
        } catch (Exception exception) {
            if (!DiscordRpcManager.refreshFailureLogged) {
                DiscordRpcManager.refreshFailureLogged = true;
                DiscordRpcManager.LOGGER.warn("Discord RPC refresh failed, further failures are logged at debug", exception);
            } else {
                DiscordRpcManager.LOGGER.debug("Discord RPC refresh failed", exception);
            }
        }
    }
    
    public static void refreshFromConfig() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.isSameThread()) DiscordRpcManager.refreshFromConfigOnRenderThread();
        else minecraft.execute(DiscordRpcManager::refreshFromConfigOnRenderThread);
    }

    private static void refreshFromConfigOnRenderThread() { // Peak name
        if (!DiscordRpcManager.isConfiguredEnabled()) {
            boolean teardown;

            synchronized (DiscordRpcManager.LOCK) {
                teardown = DiscordRpcManager.started;

                if (teardown) {
                    DiscordRpcManager.started = false;
                    DiscordRpcManager.lastSnapshot = null;
                    DiscordRpcManager.nextReconnectAt = 0L;
                    DiscordRpcManager.startTime = 0L;
                    DiscordRpcManager.closeRequested = true;
                }
            }

            if (teardown) DiscordRpcManager.scheduleUpdate();
            return;
        }

        DiscordRpcManager.pollProviders();

        PackProfile nextPack = DiscordRpcManager.getConfiguredPack();
        RPCState nextState = RPCState.resolve();

        synchronized (DiscordRpcManager.LOCK) {
            if (!DiscordRpcManager.started) {
                DiscordRpcManager.started = true;
                DiscordRpcManager.startTime = Instant.now().getEpochSecond();
                DiscordRpcManager.nextReconnectAt = 0L;
                DiscordRpcManager.lastSnapshot = null;
            }

            if (DiscordRpcManager.pack.applicationId() != nextPack.applicationId()) {
                DiscordRpcManager.closeRequested = true;
                DiscordRpcManager.nextReconnectAt = 0L;
                DiscordRpcManager.lastSnapshot = null;
            } else if (!DiscordRpcManager.pack.equals(nextPack)) {
                DiscordRpcManager.lastSnapshot = null;
            }

            DiscordRpcManager.pack = nextPack;

            if (nextState == RPCState.MAIN_MENU) {
                long now = System.currentTimeMillis();
                List<String> messages = nextPack.idleMessages();

                if (messages.isEmpty()) {
                    DiscordRpcManager.idleMessage = null;
                } else if (DiscordRpcManager.idleRolledAt == 0L) {
                    DiscordRpcManager.idleRolledAt = now;
                } else if (now - DiscordRpcManager.idleRolledAt >= DiscordRpcManager.IDLE_ROLL_INTERVAL_MS) {
                    DiscordRpcManager.idleMessage = DiscordRpcManager.rollIdleMessage(messages);
                    DiscordRpcManager.idleRolledAt = now;
                }
            } else {
                DiscordRpcManager.idleMessage = null;
                DiscordRpcManager.idleRolledAt = 0L;
            }

            if (DiscordRpcManager.state != nextState) {
                DiscordRpcManager.state = nextState;
                DiscordRpcManager.lastSnapshot = null;
            }
        }

        DiscordRpcManager.scheduleUpdate();
    }

    private static void shutdown() {
        synchronized (DiscordRpcManager.LOCK) {
            DiscordRpcManager.started = false;
            DiscordRpcManager.lastSnapshot = null;
            DiscordRpcManager.nextReconnectAt = 0L;
            DiscordRpcManager.startTime = 0L;
            DiscordRpcManager.closeRequested = false;
        }
        
        synchronized (DiscordRpcManager.CLIENT_LOCK) {
            DiscordRpcManager.closeClient();
        }
    }
    
    private static void scheduleUpdate() {
        synchronized (DiscordRpcManager.LOCK) {
            DiscordRpcManager.updateRequested = true;
        }
        
        if (!DiscordRpcManager.workerScheduled.compareAndSet(false, true)) {
            return;
        }
        
        try {
            DiscordRpcManager.EXECUTOR.execute(DiscordRpcManager::doWork);
        } catch (RejectedExecutionException exception) {
            DiscordRpcManager.workerScheduled.set(false);
            DiscordRpcManager.LOGGER.debug("Discord RPC worker rejected update", exception);
        }
    }
    
    private static void doWork() {
        try {
            while (true) {
                synchronized (DiscordRpcManager.LOCK) {
                    DiscordRpcManager.updateRequested = false;
                }
                
                DiscordRpcManager.update();
                
                synchronized (DiscordRpcManager.LOCK) {
                    if (!DiscordRpcManager.updateRequested) {
                        return;
                    }
                }
            }
        } finally {
            DiscordRpcManager.workerScheduled.set(false);
            
            synchronized (DiscordRpcManager.LOCK) {
                if (DiscordRpcManager.updateRequested) {
                    DiscordRpcManager.scheduleUpdate();
                }
            }
        }
    }
    
    private static void update() {
        Snapshot snapshot;
        boolean shouldClose;
        
        synchronized (DiscordRpcManager.LOCK) {
            shouldClose = DiscordRpcManager.closeRequested;
            DiscordRpcManager.closeRequested = false;
            
            if (!DiscordRpcManager.started) {
                snapshot = null;
            } else {
                long now = System.currentTimeMillis();
                if (!shouldClose && now < DiscordRpcManager.nextReconnectAt) {
                    return;
                }
                
                snapshot = DiscordRpcManager.createSnapshot();
            }
        }
        
        if (shouldClose) {
            synchronized (DiscordRpcManager.CLIENT_LOCK) {
                DiscordRpcManager.closeClient();
            }
        }
        
        if (snapshot == null) return;
        if (!DiscordRpcManager.connect(snapshot)) return;
        
        synchronized (DiscordRpcManager.LOCK) {
            if (!DiscordRpcManager.isCurrentSnapshot(snapshot)) return;
            if (snapshot.equals(DiscordRpcManager.lastSnapshot)) return;
        }
        
        try {
            Activity activity = DiscordRpcManager.buildActivity(snapshot);
            
            synchronized (DiscordRpcManager.CLIENT_LOCK) {
                if (DiscordRpcManager.client == null || !DiscordRpcManager.client.isConnected()) return;
                DiscordRpcManager.client.setActivity(activity);
            }
            
            synchronized (DiscordRpcManager.LOCK) {
                if (DiscordRpcManager.started) {
                    DiscordRpcManager.lastSnapshot = snapshot;
                }
            }
        } catch (Exception exception) {
            DiscordRpcManager.handleFailure(exception);
        }
    }
    
    private static String rollIdleMessage(List<String> messages) {
        String previous = DiscordRpcManager.idleMessage;
        int index = previous == null ? -1 : messages.indexOf(previous);

        if (index < 0 || messages.size() == 1) {
            return messages.get(ThreadLocalRandom.current().nextInt(messages.size()));
        }

        int rolled = ThreadLocalRandom.current().nextInt(messages.size() - 1);
        return messages.get(rolled < index ? rolled : rolled + 1);
    }

    private static String resolveSmallImage(RPCState currentState, PackProfile currentPack) {
        if (currentState == RPCState.PLAYING && DiscordRpcManager.smallImage != null) {
            return DiscordRpcManager.smallImage;
        }

        return currentPack.smallImage();
    }

    private static String resolveState(RPCState currentState, String mods) {
        if (currentState == RPCState.PLAYING && DiscordRpcManager.stateText != null) {
            return DiscordRpcManager.stateText;
        }

        if (currentState == RPCState.MAIN_MENU) {
            if (DiscordRpcManager.menuState != null) return DiscordRpcManager.menuState;

            String version = DiscordRpcManager.packVersion();
            if (version != null) return version;
        }

        return mods;
    }

    private static String packVersion() {
        if (!AllTheTweaks.BCC) return null;

        String version = BCCProxy.getVersion();
        if (version == null || version.isBlank() || version.equals("N/A")) return null;

        return "v" + version;
    }

    private static String resolveDetails(RPCState currentState) {
        if (currentState == RPCState.MAIN_MENU && DiscordRpcManager.idleMessage != null) {
            return DiscordRpcManager.idleMessage;
        }

        if (currentState == RPCState.PLAYING && DiscordRpcManager.details != null) {
            return DiscordRpcManager.details;
        }

        return currentState.display();
    }

    private static Snapshot createSnapshot() {
        PackProfile currentPack = DiscordRpcManager.pack;
        RPCState currentState = DiscordRpcManager.state;
        Progress currentProgress = DiscordRpcManager.progress;
        String mods = ModList.get().getMods().size() + " Mods";

        if (currentProgress != null
                && (currentState != RPCState.PLAYING || currentProgress.current() == 0)) {
            currentProgress = null;
        }

        return new Snapshot(
                currentPack.applicationId(),
                DiscordRpcManager.resolveDetails(currentState),
                DiscordRpcManager.resolveState(currentState, mods),
                currentPack.curseforgeUrl(),
                currentPack.largeImage(),
                DiscordRpcManager.resolveSmallImage(currentState, currentPack),
                currentPack.displayName(),
                mods,
                currentProgress,
                DiscordRpcManager.startTime
        );
    }
    
    private static boolean connect(Snapshot snapshot) {
        synchronized (DiscordRpcManager.CLIENT_LOCK) {
            if (DiscordRpcManager.client != null && DiscordRpcManager.client.isConnected()) {
                return true;
            }
            
            try {
                DiscordRpcManager.closeClient();
                
                DiscordRpcManager.client = DiscordIPC.create(snapshot.applicationId());
                DiscordRpcManager.client.connect();
                
                synchronized (DiscordRpcManager.LOCK) {
                    DiscordRpcManager.nextReconnectAt = 0L;
                    DiscordRpcManager.lastSnapshot = null;
                }

                if (!DiscordRpcManager.connectLogged) {
                    DiscordRpcManager.connectLogged = true;
                    DiscordRpcManager.LOGGER.info("Discord Rich Presence connected as {} (application {})",
                            snapshot.displayName(), snapshot.applicationId());
                }
                return true;
            } catch (Exception exception) {
                synchronized (DiscordRpcManager.LOCK) {
                    DiscordRpcManager.nextReconnectAt = System.currentTimeMillis() + DiscordRpcManager.RECONNECT_DELAY_MS;
                    DiscordRpcManager.lastSnapshot = null;
                }
                
                DiscordRpcManager.closeClient();

                if (!DiscordRpcManager.connectFailureLogged) {
                    DiscordRpcManager.connectFailureLogged = true;
                    DiscordRpcManager.LOGGER.warn("Discord Rich Presence unavailable, retrying every {} seconds, further failures are logged at debug: {}",
                            DiscordRpcManager.RECONNECT_DELAY_MS / 1000L, exception.getMessage());
                } else {
                    DiscordRpcManager.LOGGER.debug("Discord RPC unavailable: {}", exception.getMessage());
                }

                DiscordRpcManager.LOGGER.trace("Discord RPC connection failure stack trace", exception);
                return false;
            }
        }
    }
    
    private static boolean isCurrentSnapshot(Snapshot snapshot) {
        if (!DiscordRpcManager.started) return false;

        return snapshot.equals(DiscordRpcManager.createSnapshot());
    }
    
    private static Activity buildActivity(Snapshot snapshot) {
        Activity.Builder builder = new Activity.Builder()
                .setType(ActivityType.PLAYING)
                .setDetails(snapshot.details())
                .setState(snapshot.state())
                .setLargeImage(snapshot.largeImage(), snapshot.displayName())
                .setStartTimestamp(snapshot.startTime())
                .addButton("CurseForge", snapshot.curseforgeUrl())
                .addButton("Discord", DiscordRpcManager.DISCORD_URL);

        if (!snapshot.smallImage().isBlank()) {
            builder.setSmallImage(snapshot.smallImage(), snapshot.mods());
        }

        Progress currentProgress = snapshot.progress();
        if (currentProgress != null) {
            builder.setParty(DiscordRpcManager.PARTY_ID, currentProgress.current(), currentProgress.max());
        }

        return builder.build();
    }
    
    private static void handleFailure(Exception exception) {
        synchronized (DiscordRpcManager.CLIENT_LOCK) {
            DiscordRpcManager.closeClient();
        }
        
        synchronized (DiscordRpcManager.LOCK) {
            DiscordRpcManager.lastSnapshot = null;
            DiscordRpcManager.nextReconnectAt = System.currentTimeMillis() + DiscordRpcManager.RECONNECT_DELAY_MS;
        }
        
        DiscordRpcManager.LOGGER.debug("Discord RPC update failed: {}", exception.getMessage());
        DiscordRpcManager.LOGGER.trace("Discord RPC update failure stack trace", exception);
    }
    
    private static void closeClient() {
        DiscordIPC currentClient = DiscordRpcManager.client;
        DiscordRpcManager.client = null;
        if (currentClient == null) return;
        
        try {
            currentClient.clearActivity();
        } catch (Exception exception) {
            DiscordRpcManager.LOGGER.debug("Failed to clear Discord RPC activity", exception);
        }
        
        try {
            currentClient.close();
        } catch (Exception exception) {
            DiscordRpcManager.LOGGER.debug("Failed to close Discord RPC client", exception);
        }
    }
    
    private static boolean isConfiguredEnabled() {
        if (DiscordRpcManager.deferredToSimpleRpc) return false;

        return ATTConfig.RPC_ENABLED.get();
    }
    
    private static PackProfile getConfiguredPack() {
        return ATTConfig.packProfile();
    }

    private record Progress(int current, int max) { }

    private record Snapshot(
            long applicationId,
            String details,
            String state,
            String curseforgeUrl,
            String largeImage,
            String smallImage,
            String displayName,
            String mods,
            Progress progress,
            long startTime
    ) { }
}
