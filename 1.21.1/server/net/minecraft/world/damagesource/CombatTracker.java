/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  javax.annotation.Nullable
 */
package net.minecraft.world.damagesource;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.CombatEntry;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DeathMessageType;
import net.minecraft.world.damagesource.FallLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class CombatTracker {
    public static final int RESET_DAMAGE_STATUS_TIME = 100;
    public static final int RESET_COMBAT_STATUS_TIME = 300;
    private static final Style INTENTIONAL_GAME_DESIGN_STYLE = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://bugs.mojang.com/browse/MCPE-28723")).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("MCPE-28723")));
    private final List<CombatEntry> entries = Lists.newArrayList();
    private final LivingEntity mob;
    private int lastDamageTime;
    private int combatStartTime;
    private int combatEndTime;
    private boolean inCombat;
    private boolean takingDamage;

    public CombatTracker(LivingEntity livingEntity) {
        this.mob = livingEntity;
    }

    public void recordDamage(DamageSource damageSource, float f) {
        this.recheckStatus();
        FallLocation fallLocation = FallLocation.getCurrentFallLocation(this.mob);
        CombatEntry combatEntry = new CombatEntry(damageSource, f, fallLocation, this.mob.fallDistance);
        this.entries.add(combatEntry);
        this.lastDamageTime = this.mob.tickCount;
        this.takingDamage = true;
        if (!this.inCombat && this.mob.isAlive() && CombatTracker.shouldEnterCombat(damageSource)) {
            this.inCombat = true;
            this.combatEndTime = this.combatStartTime = this.mob.tickCount;
            this.mob.onEnterCombat();
        }
    }

    private static boolean shouldEnterCombat(DamageSource damageSource) {
        return damageSource.getEntity() instanceof LivingEntity;
    }

    private Component getMessageForAssistedFall(Entity entity, Component component, String string, String string2) {
        ItemStack itemStack;
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            v0 = livingEntity.getMainHandItem();
        } else {
            v0 = itemStack = ItemStack.EMPTY;
        }
        if (!itemStack.isEmpty() && itemStack.has(DataComponents.CUSTOM_NAME)) {
            return Component.translatable(string, this.mob.getDisplayName(), component, itemStack.getDisplayName());
        }
        return Component.translatable(string2, this.mob.getDisplayName(), component);
    }

    private Component getFallMessage(CombatEntry combatEntry, @Nullable Entity entity) {
        DamageSource damageSource = combatEntry.source();
        if (damageSource.is(DamageTypeTags.IS_FALL) || damageSource.is(DamageTypeTags.ALWAYS_MOST_SIGNIFICANT_FALL)) {
            FallLocation fallLocation = Objects.requireNonNullElse(combatEntry.fallLocation(), FallLocation.GENERIC);
            return Component.translatable(fallLocation.languageKey(), this.mob.getDisplayName());
        }
        Component component = CombatTracker.getDisplayName(entity);
        Entity entity2 = damageSource.getEntity();
        Component component2 = CombatTracker.getDisplayName(entity2);
        if (component2 != null && !component2.equals(component)) {
            return this.getMessageForAssistedFall(entity2, component2, "death.fell.assist.item", "death.fell.assist");
        }
        if (component != null) {
            return this.getMessageForAssistedFall(entity, component, "death.fell.finish.item", "death.fell.finish");
        }
        return Component.translatable("death.fell.killer", this.mob.getDisplayName());
    }

    @Nullable
    private static Component getDisplayName(@Nullable Entity entity) {
        return entity == null ? null : entity.getDisplayName();
    }

    public Component getDeathMessage() {
        if (this.entries.isEmpty()) {
            return Component.translatable("death.attack.generic", this.mob.getDisplayName());
        }
        CombatEntry combatEntry = this.entries.get(this.entries.size() - 1);
        DamageSource damageSource = combatEntry.source();
        CombatEntry combatEntry2 = this.getMostSignificantFall();
        DeathMessageType deathMessageType = damageSource.type().deathMessageType();
        if (deathMessageType == DeathMessageType.FALL_VARIANTS && combatEntry2 != null) {
            return this.getFallMessage(combatEntry2, damageSource.getEntity());
        }
        if (deathMessageType == DeathMessageType.INTENTIONAL_GAME_DESIGN) {
            String string = "death.attack." + damageSource.getMsgId();
            MutableComponent mutableComponent = ComponentUtils.wrapInSquareBrackets(Component.translatable(string + ".link")).withStyle(INTENTIONAL_GAME_DESIGN_STYLE);
            return Component.translatable(string + ".message", this.mob.getDisplayName(), mutableComponent);
        }
        return damageSource.getLocalizedDeathMessage(this.mob);
    }

    @Nullable
    private CombatEntry getMostSignificantFall() {
        CombatEntry combatEntry = null;
        CombatEntry combatEntry2 = null;
        float f = 0.0f;
        float f2 = 0.0f;
        for (int i = 0; i < this.entries.size(); ++i) {
            float f3;
            CombatEntry combatEntry3 = this.entries.get(i);
            CombatEntry combatEntry4 = i > 0 ? this.entries.get(i - 1) : null;
            DamageSource damageSource = combatEntry3.source();
            boolean bl = damageSource.is(DamageTypeTags.ALWAYS_MOST_SIGNIFICANT_FALL);
            float f4 = f3 = bl ? Float.MAX_VALUE : combatEntry3.fallDistance();
            if ((damageSource.is(DamageTypeTags.IS_FALL) || bl) && f3 > 0.0f && (combatEntry == null || f3 > f2)) {
                combatEntry = i > 0 ? combatEntry4 : combatEntry3;
                f2 = f3;
            }
            if (combatEntry3.fallLocation() == null || combatEntry2 != null && !(combatEntry3.damage() > f)) continue;
            combatEntry2 = combatEntry3;
            f = combatEntry3.damage();
        }
        if (f2 > 5.0f && combatEntry != null) {
            return combatEntry;
        }
        if (f > 5.0f && combatEntry2 != null) {
            return combatEntry2;
        }
        return null;
    }

    public int getCombatDuration() {
        if (this.inCombat) {
            return this.mob.tickCount - this.combatStartTime;
        }
        return this.combatEndTime - this.combatStartTime;
    }

    public void recheckStatus() {
        int n;
        int n2 = n = this.inCombat ? 300 : 100;
        if (this.takingDamage && (!this.mob.isAlive() || this.mob.tickCount - this.lastDamageTime > n)) {
            boolean bl = this.inCombat;
            this.takingDamage = false;
            this.inCombat = false;
            this.combatEndTime = this.mob.tickCount;
            if (bl) {
                this.mob.onLeaveCombat();
            }
            this.entries.clear();
        }
    }
}

