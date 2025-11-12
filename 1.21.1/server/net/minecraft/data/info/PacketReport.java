/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 */
package net.minecraft.data.info;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.protocol.configuration.ConfigurationProtocols;
import net.minecraft.network.protocol.game.GameProtocols;
import net.minecraft.network.protocol.handshake.HandshakeProtocols;
import net.minecraft.network.protocol.login.LoginProtocols;
import net.minecraft.network.protocol.status.StatusProtocols;

public class PacketReport
implements DataProvider {
    private final PackOutput output;

    public PacketReport(PackOutput packOutput) {
        this.output = packOutput;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        Path path = this.output.getOutputFolder(PackOutput.Target.REPORTS).resolve("packets.json");
        return DataProvider.saveStable(cachedOutput, this.serializePackets(), path);
    }

    private JsonElement serializePackets() {
        JsonObject jsonObject = new JsonObject();
        Stream.of(HandshakeProtocols.SERVERBOUND_TEMPLATE, StatusProtocols.CLIENTBOUND_TEMPLATE, StatusProtocols.SERVERBOUND_TEMPLATE, LoginProtocols.CLIENTBOUND_TEMPLATE, LoginProtocols.SERVERBOUND_TEMPLATE, ConfigurationProtocols.CLIENTBOUND_TEMPLATE, ConfigurationProtocols.SERVERBOUND_TEMPLATE, GameProtocols.CLIENTBOUND_TEMPLATE, GameProtocols.SERVERBOUND_TEMPLATE).collect(Collectors.groupingBy(ProtocolInfo.Unbound::id)).forEach((connectionProtocol, list) -> {
            JsonObject jsonObject2 = new JsonObject();
            jsonObject.add(connectionProtocol.id(), (JsonElement)jsonObject2);
            list.forEach(unbound -> {
                JsonObject jsonObject2 = new JsonObject();
                jsonObject2.add(unbound.flow().id(), (JsonElement)jsonObject2);
                unbound.listPackets((packetType, n) -> {
                    JsonObject jsonObject2 = new JsonObject();
                    jsonObject2.addProperty("protocol_id", (Number)n);
                    jsonObject2.add(packetType.id().toString(), (JsonElement)jsonObject2);
                });
            });
        });
        return jsonObject;
    }

    @Override
    public String getName() {
        return "Packet Report";
    }
}

