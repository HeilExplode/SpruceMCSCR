/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonIOException
 *  com.google.gson.JsonParseException
 *  com.google.gson.internal.Streams
 *  com.google.gson.stream.JsonReader
 *  com.mojang.datafixers.DataFixer
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import net.minecraft.FileUtil;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.AdvancementTree;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSelectAdvancementsTabPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.advancements.AdvancementVisibilityEvaluator;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.GameRules;
import org.slf4j.Logger;

public class PlayerAdvancements {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final PlayerList playerList;
    private final Path playerSavePath;
    private AdvancementTree tree;
    private final Map<AdvancementHolder, AdvancementProgress> progress = new LinkedHashMap<AdvancementHolder, AdvancementProgress>();
    private final Set<AdvancementHolder> visible = new HashSet<AdvancementHolder>();
    private final Set<AdvancementHolder> progressChanged = new HashSet<AdvancementHolder>();
    private final Set<AdvancementNode> rootsToUpdate = new HashSet<AdvancementNode>();
    private ServerPlayer player;
    @Nullable
    private AdvancementHolder lastSelectedTab;
    private boolean isFirstPacket = true;
    private final Codec<Data> codec;

    public PlayerAdvancements(DataFixer dataFixer, PlayerList playerList, ServerAdvancementManager serverAdvancementManager, Path path, ServerPlayer serverPlayer) {
        this.playerList = playerList;
        this.playerSavePath = path;
        this.player = serverPlayer;
        this.tree = serverAdvancementManager.tree();
        int n = 1343;
        this.codec = DataFixTypes.ADVANCEMENTS.wrapCodec(Data.CODEC, dataFixer, 1343);
        this.load(serverAdvancementManager);
    }

    public void setPlayer(ServerPlayer serverPlayer) {
        this.player = serverPlayer;
    }

    public void stopListening() {
        for (CriterionTrigger criterionTrigger : BuiltInRegistries.TRIGGER_TYPES) {
            criterionTrigger.removePlayerListeners(this);
        }
    }

    public void reload(ServerAdvancementManager serverAdvancementManager) {
        this.stopListening();
        this.progress.clear();
        this.visible.clear();
        this.rootsToUpdate.clear();
        this.progressChanged.clear();
        this.isFirstPacket = true;
        this.lastSelectedTab = null;
        this.tree = serverAdvancementManager.tree();
        this.load(serverAdvancementManager);
    }

    private void registerListeners(ServerAdvancementManager serverAdvancementManager) {
        for (AdvancementHolder advancementHolder : serverAdvancementManager.getAllAdvancements()) {
            this.registerListeners(advancementHolder);
        }
    }

    private void checkForAutomaticTriggers(ServerAdvancementManager serverAdvancementManager) {
        for (AdvancementHolder advancementHolder : serverAdvancementManager.getAllAdvancements()) {
            Advancement advancement = advancementHolder.value();
            if (!advancement.criteria().isEmpty()) continue;
            this.award(advancementHolder, "");
            advancement.rewards().grant(this.player);
        }
    }

    private void load(ServerAdvancementManager serverAdvancementManager) {
        if (Files.isRegularFile(this.playerSavePath, new LinkOption[0])) {
            try (JsonReader jsonReader = new JsonReader((Reader)Files.newBufferedReader(this.playerSavePath, StandardCharsets.UTF_8));){
                jsonReader.setLenient(false);
                JsonElement jsonElement = Streams.parse((JsonReader)jsonReader);
                Data data = (Data)this.codec.parse((DynamicOps)JsonOps.INSTANCE, (Object)jsonElement).getOrThrow(JsonParseException::new);
                this.applyFrom(serverAdvancementManager, data);
            }
            catch (JsonIOException | IOException throwable) {
                LOGGER.error("Couldn't access player advancements in {}", (Object)this.playerSavePath, (Object)throwable);
            }
            catch (JsonParseException jsonParseException) {
                LOGGER.error("Couldn't parse player advancements in {}", (Object)this.playerSavePath, (Object)jsonParseException);
            }
        }
        this.checkForAutomaticTriggers(serverAdvancementManager);
        this.registerListeners(serverAdvancementManager);
    }

    public void save() {
        JsonElement jsonElement = (JsonElement)this.codec.encodeStart((DynamicOps)JsonOps.INSTANCE, (Object)this.asData()).getOrThrow();
        try {
            FileUtil.createDirectoriesSafe(this.playerSavePath.getParent());
            try (BufferedWriter bufferedWriter = Files.newBufferedWriter(this.playerSavePath, StandardCharsets.UTF_8, new OpenOption[0]);){
                GSON.toJson(jsonElement, GSON.newJsonWriter((Writer)bufferedWriter));
            }
        }
        catch (JsonIOException | IOException throwable) {
            LOGGER.error("Couldn't save player advancements to {}", (Object)this.playerSavePath, (Object)throwable);
        }
    }

    private void applyFrom(ServerAdvancementManager serverAdvancementManager, Data data) {
        data.forEach((resourceLocation, advancementProgress) -> {
            AdvancementHolder advancementHolder = serverAdvancementManager.get((ResourceLocation)resourceLocation);
            if (advancementHolder == null) {
                LOGGER.warn("Ignored advancement '{}' in progress file {} - it doesn't exist anymore?", resourceLocation, (Object)this.playerSavePath);
                return;
            }
            this.startProgress(advancementHolder, (AdvancementProgress)advancementProgress);
            this.progressChanged.add(advancementHolder);
            this.markForVisibilityUpdate(advancementHolder);
        });
    }

    private Data asData() {
        LinkedHashMap<ResourceLocation, AdvancementProgress> linkedHashMap = new LinkedHashMap<ResourceLocation, AdvancementProgress>();
        this.progress.forEach((advancementHolder, advancementProgress) -> {
            if (advancementProgress.hasProgress()) {
                linkedHashMap.put(advancementHolder.id(), (AdvancementProgress)advancementProgress);
            }
        });
        return new Data(linkedHashMap);
    }

    public boolean award(AdvancementHolder advancementHolder, String string) {
        boolean bl = false;
        AdvancementProgress advancementProgress = this.getOrStartProgress(advancementHolder);
        boolean bl2 = advancementProgress.isDone();
        if (advancementProgress.grantProgress(string)) {
            this.unregisterListeners(advancementHolder);
            this.progressChanged.add(advancementHolder);
            bl = true;
            if (!bl2 && advancementProgress.isDone()) {
                advancementHolder.value().rewards().grant(this.player);
                advancementHolder.value().display().ifPresent(displayInfo -> {
                    if (displayInfo.shouldAnnounceChat() && this.player.level().getGameRules().getBoolean(GameRules.RULE_ANNOUNCE_ADVANCEMENTS)) {
                        this.playerList.broadcastSystemMessage(displayInfo.getType().createAnnouncement(advancementHolder, this.player), false);
                    }
                });
            }
        }
        if (!bl2 && advancementProgress.isDone()) {
            this.markForVisibilityUpdate(advancementHolder);
        }
        return bl;
    }

    public boolean revoke(AdvancementHolder advancementHolder, String string) {
        boolean bl = false;
        AdvancementProgress advancementProgress = this.getOrStartProgress(advancementHolder);
        boolean bl2 = advancementProgress.isDone();
        if (advancementProgress.revokeProgress(string)) {
            this.registerListeners(advancementHolder);
            this.progressChanged.add(advancementHolder);
            bl = true;
        }
        if (bl2 && !advancementProgress.isDone()) {
            this.markForVisibilityUpdate(advancementHolder);
        }
        return bl;
    }

    private void markForVisibilityUpdate(AdvancementHolder advancementHolder) {
        AdvancementNode advancementNode = this.tree.get(advancementHolder);
        if (advancementNode != null) {
            this.rootsToUpdate.add(advancementNode.root());
        }
    }

    private void registerListeners(AdvancementHolder advancementHolder) {
        AdvancementProgress advancementProgress = this.getOrStartProgress(advancementHolder);
        if (advancementProgress.isDone()) {
            return;
        }
        for (Map.Entry<String, Criterion<?>> entry : advancementHolder.value().criteria().entrySet()) {
            CriterionProgress criterionProgress = advancementProgress.getCriterion(entry.getKey());
            if (criterionProgress == null || criterionProgress.isDone()) continue;
            this.registerListener(advancementHolder, entry.getKey(), entry.getValue());
        }
    }

    private <T extends CriterionTriggerInstance> void registerListener(AdvancementHolder advancementHolder, String string, Criterion<T> criterion) {
        criterion.trigger().addPlayerListener(this, new CriterionTrigger.Listener<T>(criterion.triggerInstance(), advancementHolder, string));
    }

    private void unregisterListeners(AdvancementHolder advancementHolder) {
        AdvancementProgress advancementProgress = this.getOrStartProgress(advancementHolder);
        for (Map.Entry<String, Criterion<?>> entry : advancementHolder.value().criteria().entrySet()) {
            CriterionProgress criterionProgress = advancementProgress.getCriterion(entry.getKey());
            if (criterionProgress == null || !criterionProgress.isDone() && !advancementProgress.isDone()) continue;
            this.removeListener(advancementHolder, entry.getKey(), entry.getValue());
        }
    }

    private <T extends CriterionTriggerInstance> void removeListener(AdvancementHolder advancementHolder, String string, Criterion<T> criterion) {
        criterion.trigger().removePlayerListener(this, new CriterionTrigger.Listener<T>(criterion.triggerInstance(), advancementHolder, string));
    }

    public void flushDirty(ServerPlayer serverPlayer) {
        if (this.isFirstPacket || !this.rootsToUpdate.isEmpty() || !this.progressChanged.isEmpty()) {
            HashMap<ResourceLocation, AdvancementProgress> hashMap = new HashMap<ResourceLocation, AdvancementProgress>();
            HashSet<AdvancementHolder> hashSet = new HashSet<AdvancementHolder>();
            HashSet<ResourceLocation> hashSet2 = new HashSet<ResourceLocation>();
            for (AdvancementNode object : this.rootsToUpdate) {
                this.updateTreeVisibility(object, hashSet, hashSet2);
            }
            this.rootsToUpdate.clear();
            for (AdvancementHolder advancementHolder : this.progressChanged) {
                if (!this.visible.contains(advancementHolder)) continue;
                hashMap.put(advancementHolder.id(), this.progress.get(advancementHolder));
            }
            this.progressChanged.clear();
            if (!(hashMap.isEmpty() && hashSet.isEmpty() && hashSet2.isEmpty())) {
                serverPlayer.connection.send(new ClientboundUpdateAdvancementsPacket(this.isFirstPacket, hashSet, hashSet2, hashMap));
            }
        }
        this.isFirstPacket = false;
    }

    public void setSelectedTab(@Nullable AdvancementHolder advancementHolder) {
        AdvancementHolder advancementHolder2 = this.lastSelectedTab;
        this.lastSelectedTab = advancementHolder != null && advancementHolder.value().isRoot() && advancementHolder.value().display().isPresent() ? advancementHolder : null;
        if (advancementHolder2 != this.lastSelectedTab) {
            this.player.connection.send(new ClientboundSelectAdvancementsTabPacket(this.lastSelectedTab == null ? null : this.lastSelectedTab.id()));
        }
    }

    public AdvancementProgress getOrStartProgress(AdvancementHolder advancementHolder) {
        AdvancementProgress advancementProgress = this.progress.get(advancementHolder);
        if (advancementProgress == null) {
            advancementProgress = new AdvancementProgress();
            this.startProgress(advancementHolder, advancementProgress);
        }
        return advancementProgress;
    }

    private void startProgress(AdvancementHolder advancementHolder, AdvancementProgress advancementProgress) {
        advancementProgress.update(advancementHolder.value().requirements());
        this.progress.put(advancementHolder, advancementProgress);
    }

    private void updateTreeVisibility(AdvancementNode advancementNode2, Set<AdvancementHolder> set, Set<ResourceLocation> set2) {
        AdvancementVisibilityEvaluator.evaluateVisibility(advancementNode2, advancementNode -> this.getOrStartProgress(advancementNode.holder()).isDone(), (advancementNode, bl) -> {
            AdvancementHolder advancementHolder = advancementNode.holder();
            if (bl) {
                if (this.visible.add(advancementHolder)) {
                    set.add(advancementHolder);
                    if (this.progress.containsKey(advancementHolder)) {
                        this.progressChanged.add(advancementHolder);
                    }
                }
            } else if (this.visible.remove(advancementHolder)) {
                set2.add(advancementHolder.id());
            }
        });
    }

    record Data(Map<ResourceLocation, AdvancementProgress> map) {
        public static final Codec<Data> CODEC = Codec.unboundedMap(ResourceLocation.CODEC, AdvancementProgress.CODEC).xmap(Data::new, Data::map);

        public void forEach(BiConsumer<ResourceLocation, AdvancementProgress> biConsumer) {
            this.map.entrySet().stream().sorted(Map.Entry.comparingByValue()).forEach((? super T entry) -> biConsumer.accept((ResourceLocation)entry.getKey(), (AdvancementProgress)entry.getValue()));
        }
    }
}

