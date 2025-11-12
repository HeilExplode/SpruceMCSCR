/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Charsets
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.apache.commons.io.IOUtils
 *  org.slf4j.Logger
 */
package net.minecraft.server.chase;

import com.google.common.base.Charsets;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.net.Socket;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Scanner;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.ChaseCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

public class ChaseClient {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int RECONNECT_INTERVAL_SECONDS = 5;
    private final String serverHost;
    private final int serverPort;
    private final MinecraftServer server;
    private volatile boolean wantsToRun;
    @Nullable
    private Socket socket;
    @Nullable
    private Thread thread;

    public ChaseClient(String string, int n, MinecraftServer minecraftServer) {
        this.serverHost = string;
        this.serverPort = n;
        this.server = minecraftServer;
    }

    public void start() {
        if (this.thread != null && this.thread.isAlive()) {
            LOGGER.warn("Remote control client was asked to start, but it is already running. Will ignore.");
        }
        this.wantsToRun = true;
        this.thread = new Thread(this::run, "chase-client");
        this.thread.setDaemon(true);
        this.thread.start();
    }

    public void stop() {
        this.wantsToRun = false;
        IOUtils.closeQuietly((Socket)this.socket);
        this.socket = null;
        this.thread = null;
    }

    public void run() {
        String string = this.serverHost + ":" + this.serverPort;
        while (this.wantsToRun) {
            try {
                LOGGER.info("Connecting to remote control server {}", (Object)string);
                this.socket = new Socket(this.serverHost, this.serverPort);
                LOGGER.info("Connected to remote control server! Will continuously execute the command broadcasted by that server.");
                try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), Charsets.US_ASCII));){
                    while (this.wantsToRun) {
                        String string2 = bufferedReader.readLine();
                        if (string2 == null) {
                            LOGGER.warn("Lost connection to remote control server {}. Will retry in {}s.", (Object)string, (Object)5);
                            break;
                        }
                        this.handleMessage(string2);
                    }
                }
                catch (IOException iOException) {
                    LOGGER.warn("Lost connection to remote control server {}. Will retry in {}s.", (Object)string, (Object)5);
                }
            }
            catch (IOException iOException) {
                LOGGER.warn("Failed to connect to remote control server {}. Will retry in {}s.", (Object)string, (Object)5);
            }
            if (!this.wantsToRun) continue;
            try {
                Thread.sleep(5000L);
            }
            catch (InterruptedException interruptedException) {}
        }
    }

    private void handleMessage(String string) {
        try (Scanner scanner = new Scanner(new StringReader(string));){
            scanner.useLocale(Locale.ROOT);
            String string2 = scanner.next();
            if ("t".equals(string2)) {
                this.handleTeleport(scanner);
            } else {
                LOGGER.warn("Unknown message type '{}'", (Object)string2);
            }
        }
        catch (NoSuchElementException noSuchElementException) {
            LOGGER.warn("Could not parse message '{}', ignoring", (Object)string);
        }
    }

    private void handleTeleport(Scanner scanner) {
        this.parseTarget(scanner).ifPresent(teleportTarget -> this.executeCommand(String.format(Locale.ROOT, "execute in %s run tp @s %.3f %.3f %.3f %.3f %.3f", teleportTarget.level.location(), teleportTarget.pos.x, teleportTarget.pos.y, teleportTarget.pos.z, Float.valueOf(teleportTarget.rot.y), Float.valueOf(teleportTarget.rot.x))));
    }

    private Optional<TeleportTarget> parseTarget(Scanner scanner) {
        ResourceKey resourceKey = (ResourceKey)ChaseCommand.DIMENSION_NAMES.get((Object)scanner.next());
        if (resourceKey == null) {
            return Optional.empty();
        }
        float f = scanner.nextFloat();
        float f2 = scanner.nextFloat();
        float f3 = scanner.nextFloat();
        float f4 = scanner.nextFloat();
        float f5 = scanner.nextFloat();
        return Optional.of(new TeleportTarget(resourceKey, new Vec3(f, f2, f3), new Vec2(f5, f4)));
    }

    private void executeCommand(String string) {
        this.server.execute(() -> {
            List<ServerPlayer> list = this.server.getPlayerList().getPlayers();
            if (list.isEmpty()) {
                return;
            }
            ServerPlayer serverPlayer = list.get(0);
            ServerLevel serverLevel = this.server.overworld();
            CommandSourceStack commandSourceStack = new CommandSourceStack(serverPlayer, Vec3.atLowerCornerOf(serverLevel.getSharedSpawnPos()), Vec2.ZERO, serverLevel, 4, "", CommonComponents.EMPTY, this.server, serverPlayer);
            Commands commands = this.server.getCommands();
            commands.performPrefixedCommand(commandSourceStack, string);
        });
    }

    static final class TeleportTarget
    extends Record {
        final ResourceKey<Level> level;
        final Vec3 pos;
        final Vec2 rot;

        TeleportTarget(ResourceKey<Level> resourceKey, Vec3 vec3, Vec2 vec2) {
            this.level = resourceKey;
            this.pos = vec3;
            this.rot = vec2;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{TeleportTarget.class, "level;pos;rot", "level", "pos", "rot"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{TeleportTarget.class, "level;pos;rot", "level", "pos", "rot"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{TeleportTarget.class, "level;pos;rot", "level", "pos", "rot"}, this, object);
        }

        public ResourceKey<Level> level() {
            return this.level;
        }

        public Vec3 pos() {
            return this.pos;
        }

        public Vec2 rot() {
            return this.rot;
        }
    }
}

