/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.server;

import com.mojang.logging.LogUtils;
import java.io.PrintStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.SharedConstants;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.selector.options.EntitySelectorOptions;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.server.DebugLoggedPrintStream;
import net.minecraft.server.LoggedPrintStream;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.FireBlock;
import org.slf4j.Logger;

public class Bootstrap {
    public static final PrintStream STDOUT = System.out;
    private static volatile boolean isBootstrapped;
    private static final Logger LOGGER;
    public static final AtomicLong bootstrapDuration;

    public static void bootStrap() {
        if (isBootstrapped) {
            return;
        }
        isBootstrapped = true;
        Instant instant = Instant.now();
        if (BuiltInRegistries.REGISTRY.keySet().isEmpty()) {
            throw new IllegalStateException("Unable to load registries");
        }
        FireBlock.bootStrap();
        ComposterBlock.bootStrap();
        if (EntityType.getKey(EntityType.PLAYER) == null) {
            throw new IllegalStateException("Failed loading EntityTypes");
        }
        EntitySelectorOptions.bootStrap();
        DispenseItemBehavior.bootStrap();
        CauldronInteraction.bootStrap();
        BuiltInRegistries.bootStrap();
        CreativeModeTabs.validate();
        Bootstrap.wrapStreams();
        bootstrapDuration.set(Duration.between(instant, Instant.now()).toMillis());
    }

    private static <T> void checkTranslations(Iterable<T> iterable, Function<T, String> function, Set<String> set) {
        Language language = Language.getInstance();
        iterable.forEach(object -> {
            String string = (String)function.apply(object);
            if (!language.has(string)) {
                set.add(string);
            }
        });
    }

    private static void checkGameruleTranslations(final Set<String> set) {
        final Language language = Language.getInstance();
        GameRules.visitGameRuleTypes(new GameRules.GameRuleTypeVisitor(){

            @Override
            public <T extends GameRules.Value<T>> void visit(GameRules.Key<T> key, GameRules.Type<T> type) {
                if (!language.has(key.getDescriptionId())) {
                    set.add(key.getId());
                }
            }
        });
    }

    public static Set<String> getMissingTranslations() {
        TreeSet<String> treeSet = new TreeSet<String>();
        Bootstrap.checkTranslations(BuiltInRegistries.ATTRIBUTE, Attribute::getDescriptionId, treeSet);
        Bootstrap.checkTranslations(BuiltInRegistries.ENTITY_TYPE, EntityType::getDescriptionId, treeSet);
        Bootstrap.checkTranslations(BuiltInRegistries.MOB_EFFECT, MobEffect::getDescriptionId, treeSet);
        Bootstrap.checkTranslations(BuiltInRegistries.ITEM, Item::getDescriptionId, treeSet);
        Bootstrap.checkTranslations(BuiltInRegistries.BLOCK, Block::getDescriptionId, treeSet);
        Bootstrap.checkTranslations(BuiltInRegistries.CUSTOM_STAT, resourceLocation -> "stat." + resourceLocation.toString().replace(':', '.'), treeSet);
        Bootstrap.checkGameruleTranslations(treeSet);
        return treeSet;
    }

    public static void checkBootstrapCalled(Supplier<String> supplier) {
        if (!isBootstrapped) {
            throw Bootstrap.createBootstrapException(supplier);
        }
    }

    private static RuntimeException createBootstrapException(Supplier<String> supplier) {
        try {
            String string = supplier.get();
            return new IllegalArgumentException("Not bootstrapped (called from " + string + ")");
        }
        catch (Exception exception) {
            IllegalArgumentException illegalArgumentException = new IllegalArgumentException("Not bootstrapped (failed to resolve location)");
            illegalArgumentException.addSuppressed(exception);
            return illegalArgumentException;
        }
    }

    public static void validate() {
        Bootstrap.checkBootstrapCalled(() -> "validate");
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            Bootstrap.getMissingTranslations().forEach(string -> LOGGER.error("Missing translations: {}", string));
            Commands.validate();
        }
        DefaultAttributes.validate();
    }

    private static void wrapStreams() {
        if (LOGGER.isDebugEnabled()) {
            System.setErr(new DebugLoggedPrintStream("STDERR", System.err));
            System.setOut(new DebugLoggedPrintStream("STDOUT", STDOUT));
        } else {
            System.setErr(new LoggedPrintStream("STDERR", System.err));
            System.setOut(new LoggedPrintStream("STDOUT", STDOUT));
        }
    }

    public static void realStdoutPrintln(String string) {
        STDOUT.println(string);
    }

    static {
        LOGGER = LogUtils.getLogger();
        bootstrapDuration = new AtomicLong(-1L);
    }
}

