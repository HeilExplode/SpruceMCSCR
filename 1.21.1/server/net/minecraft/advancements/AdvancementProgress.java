/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 */
package net.minecraft.advancements;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;

public class AdvancementProgress
implements Comparable<AdvancementProgress> {
    private static final DateTimeFormatter OBTAINED_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z", Locale.ROOT);
    private static final Codec<Instant> OBTAINED_TIME_CODEC = ExtraCodecs.temporalCodec(OBTAINED_TIME_FORMAT).xmap(Instant::from, instant -> instant.atZone(ZoneId.systemDefault()));
    private static final Codec<Map<String, CriterionProgress>> CRITERIA_CODEC = Codec.unboundedMap((Codec)Codec.STRING, OBTAINED_TIME_CODEC).xmap(map -> map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> new CriterionProgress((Instant)entry.getValue()))), map -> map.entrySet().stream().filter(entry -> ((CriterionProgress)entry.getValue()).isDone()).collect(Collectors.toMap(Map.Entry::getKey, entry -> Objects.requireNonNull(((CriterionProgress)entry.getValue()).getObtained()))));
    public static final Codec<AdvancementProgress> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)CRITERIA_CODEC.optionalFieldOf("criteria", Map.of()).forGetter(advancementProgress -> advancementProgress.criteria), (App)Codec.BOOL.fieldOf("done").orElse((Object)true).forGetter(AdvancementProgress::isDone)).apply((Applicative)instance, (map, bl) -> new AdvancementProgress(new HashMap<String, CriterionProgress>((Map<String, CriterionProgress>)map))));
    private final Map<String, CriterionProgress> criteria;
    private AdvancementRequirements requirements = AdvancementRequirements.EMPTY;

    private AdvancementProgress(Map<String, CriterionProgress> map) {
        this.criteria = map;
    }

    public AdvancementProgress() {
        this.criteria = Maps.newHashMap();
    }

    public void update(AdvancementRequirements advancementRequirements) {
        Set<String> set = advancementRequirements.names();
        this.criteria.entrySet().removeIf(entry -> !set.contains(entry.getKey()));
        for (String string : set) {
            this.criteria.putIfAbsent(string, new CriterionProgress());
        }
        this.requirements = advancementRequirements;
    }

    public boolean isDone() {
        return this.requirements.test(this::isCriterionDone);
    }

    public boolean hasProgress() {
        for (CriterionProgress criterionProgress : this.criteria.values()) {
            if (!criterionProgress.isDone()) continue;
            return true;
        }
        return false;
    }

    public boolean grantProgress(String string) {
        CriterionProgress criterionProgress = this.criteria.get(string);
        if (criterionProgress != null && !criterionProgress.isDone()) {
            criterionProgress.grant();
            return true;
        }
        return false;
    }

    public boolean revokeProgress(String string) {
        CriterionProgress criterionProgress = this.criteria.get(string);
        if (criterionProgress != null && criterionProgress.isDone()) {
            criterionProgress.revoke();
            return true;
        }
        return false;
    }

    public String toString() {
        return "AdvancementProgress{criteria=" + String.valueOf(this.criteria) + ", requirements=" + String.valueOf(this.requirements) + "}";
    }

    public void serializeToNetwork(FriendlyByteBuf friendlyByteBuf2) {
        friendlyByteBuf2.writeMap(this.criteria, FriendlyByteBuf::writeUtf, (friendlyByteBuf, criterionProgress) -> criterionProgress.serializeToNetwork((FriendlyByteBuf)((Object)friendlyByteBuf)));
    }

    public static AdvancementProgress fromNetwork(FriendlyByteBuf friendlyByteBuf) {
        Map<String, CriterionProgress> map = friendlyByteBuf.readMap(FriendlyByteBuf::readUtf, CriterionProgress::fromNetwork);
        return new AdvancementProgress(map);
    }

    @Nullable
    public CriterionProgress getCriterion(String string) {
        return this.criteria.get(string);
    }

    private boolean isCriterionDone(String string) {
        CriterionProgress criterionProgress = this.getCriterion(string);
        return criterionProgress != null && criterionProgress.isDone();
    }

    public float getPercent() {
        if (this.criteria.isEmpty()) {
            return 0.0f;
        }
        float f = this.requirements.size();
        float f2 = this.countCompletedRequirements();
        return f2 / f;
    }

    @Nullable
    public Component getProgressText() {
        if (this.criteria.isEmpty()) {
            return null;
        }
        int n = this.requirements.size();
        if (n <= 1) {
            return null;
        }
        int n2 = this.countCompletedRequirements();
        return Component.translatable("advancements.progress", n2, n);
    }

    private int countCompletedRequirements() {
        return this.requirements.count(this::isCriterionDone);
    }

    public Iterable<String> getRemainingCriteria() {
        ArrayList arrayList = Lists.newArrayList();
        for (Map.Entry<String, CriterionProgress> entry : this.criteria.entrySet()) {
            if (entry.getValue().isDone()) continue;
            arrayList.add(entry.getKey());
        }
        return arrayList;
    }

    public Iterable<String> getCompletedCriteria() {
        ArrayList arrayList = Lists.newArrayList();
        for (Map.Entry<String, CriterionProgress> entry : this.criteria.entrySet()) {
            if (!entry.getValue().isDone()) continue;
            arrayList.add(entry.getKey());
        }
        return arrayList;
    }

    @Nullable
    public Instant getFirstProgressDate() {
        return this.criteria.values().stream().map(CriterionProgress::getObtained).filter(Objects::nonNull).min(Comparator.naturalOrder()).orElse(null);
    }

    @Override
    public int compareTo(AdvancementProgress advancementProgress) {
        Instant instant = this.getFirstProgressDate();
        Instant instant2 = advancementProgress.getFirstProgressDate();
        if (instant == null && instant2 != null) {
            return 1;
        }
        if (instant != null && instant2 == null) {
            return -1;
        }
        if (instant == null && instant2 == null) {
            return 0;
        }
        return instant.compareTo(instant2);
    }

    @Override
    public /* synthetic */ int compareTo(Object object) {
        return this.compareTo((AdvancementProgress)object);
    }
}

