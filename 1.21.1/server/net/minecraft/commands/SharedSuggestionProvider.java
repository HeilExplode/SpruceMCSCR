/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Strings
 *  com.google.common.collect.Lists
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 */
package net.minecraft.commands;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.Level;

public interface SharedSuggestionProvider {
    public Collection<String> getOnlinePlayerNames();

    default public Collection<String> getCustomTabSugggestions() {
        return this.getOnlinePlayerNames();
    }

    default public Collection<String> getSelectedEntities() {
        return Collections.emptyList();
    }

    public Collection<String> getAllTeams();

    public Stream<ResourceLocation> getAvailableSounds();

    public Stream<ResourceLocation> getRecipeNames();

    public CompletableFuture<Suggestions> customSuggestion(CommandContext<?> var1);

    default public Collection<TextCoordinates> getRelevantCoordinates() {
        return Collections.singleton(TextCoordinates.DEFAULT_GLOBAL);
    }

    default public Collection<TextCoordinates> getAbsoluteCoordinates() {
        return Collections.singleton(TextCoordinates.DEFAULT_GLOBAL);
    }

    public Set<ResourceKey<Level>> levels();

    public RegistryAccess registryAccess();

    public FeatureFlagSet enabledFeatures();

    default public void suggestRegistryElements(Registry<?> registry, ElementSuggestionType elementSuggestionType, SuggestionsBuilder suggestionsBuilder) {
        if (elementSuggestionType.shouldSuggestTags()) {
            SharedSuggestionProvider.suggestResource(registry.getTagNames().map(TagKey::location), suggestionsBuilder, "#");
        }
        if (elementSuggestionType.shouldSuggestElements()) {
            SharedSuggestionProvider.suggestResource(registry.keySet(), suggestionsBuilder);
        }
    }

    public CompletableFuture<Suggestions> suggestRegistryElements(ResourceKey<? extends Registry<?>> var1, ElementSuggestionType var2, SuggestionsBuilder var3, CommandContext<?> var4);

    public boolean hasPermission(int var1);

    public static <T> void filterResources(Iterable<T> iterable, String string, Function<T, ResourceLocation> function, Consumer<T> consumer) {
        boolean bl = string.indexOf(58) > -1;
        for (T t : iterable) {
            ResourceLocation resourceLocation = function.apply(t);
            if (bl) {
                String string2 = resourceLocation.toString();
                if (!SharedSuggestionProvider.matchesSubStr(string, string2)) continue;
                consumer.accept(t);
                continue;
            }
            if (!SharedSuggestionProvider.matchesSubStr(string, resourceLocation.getNamespace()) && (!resourceLocation.getNamespace().equals("minecraft") || !SharedSuggestionProvider.matchesSubStr(string, resourceLocation.getPath()))) continue;
            consumer.accept(t);
        }
    }

    public static <T> void filterResources(Iterable<T> iterable, String string, String string2, Function<T, ResourceLocation> function, Consumer<T> consumer) {
        if (string.isEmpty()) {
            iterable.forEach(consumer);
        } else {
            String string3 = Strings.commonPrefix((CharSequence)string, (CharSequence)string2);
            if (!string3.isEmpty()) {
                String string4 = string.substring(string3.length());
                SharedSuggestionProvider.filterResources(iterable, string4, function, consumer);
            }
        }
    }

    public static CompletableFuture<Suggestions> suggestResource(Iterable<ResourceLocation> iterable, SuggestionsBuilder suggestionsBuilder, String string) {
        String string2 = suggestionsBuilder.getRemaining().toLowerCase(Locale.ROOT);
        SharedSuggestionProvider.filterResources(iterable, string2, string, resourceLocation -> resourceLocation, resourceLocation -> suggestionsBuilder.suggest(string + String.valueOf(resourceLocation)));
        return suggestionsBuilder.buildFuture();
    }

    public static CompletableFuture<Suggestions> suggestResource(Stream<ResourceLocation> stream, SuggestionsBuilder suggestionsBuilder, String string) {
        return SharedSuggestionProvider.suggestResource(stream::iterator, suggestionsBuilder, string);
    }

    public static CompletableFuture<Suggestions> suggestResource(Iterable<ResourceLocation> iterable, SuggestionsBuilder suggestionsBuilder) {
        String string = suggestionsBuilder.getRemaining().toLowerCase(Locale.ROOT);
        SharedSuggestionProvider.filterResources(iterable, string, resourceLocation -> resourceLocation, resourceLocation -> suggestionsBuilder.suggest(resourceLocation.toString()));
        return suggestionsBuilder.buildFuture();
    }

    public static <T> CompletableFuture<Suggestions> suggestResource(Iterable<T> iterable, SuggestionsBuilder suggestionsBuilder, Function<T, ResourceLocation> function, Function<T, Message> function2) {
        String string = suggestionsBuilder.getRemaining().toLowerCase(Locale.ROOT);
        SharedSuggestionProvider.filterResources(iterable, string, function, object -> suggestionsBuilder.suggest(((ResourceLocation)function.apply(object)).toString(), (Message)function2.apply(object)));
        return suggestionsBuilder.buildFuture();
    }

    public static CompletableFuture<Suggestions> suggestResource(Stream<ResourceLocation> stream, SuggestionsBuilder suggestionsBuilder) {
        return SharedSuggestionProvider.suggestResource(stream::iterator, suggestionsBuilder);
    }

    public static <T> CompletableFuture<Suggestions> suggestResource(Stream<T> stream, SuggestionsBuilder suggestionsBuilder, Function<T, ResourceLocation> function, Function<T, Message> function2) {
        return SharedSuggestionProvider.suggestResource(stream::iterator, suggestionsBuilder, function, function2);
    }

    public static CompletableFuture<Suggestions> suggestCoordinates(String string, Collection<TextCoordinates> collection, SuggestionsBuilder suggestionsBuilder, Predicate<String> predicate) {
        ArrayList arrayList;
        block4: {
            String[] stringArray;
            block5: {
                block3: {
                    arrayList = Lists.newArrayList();
                    if (!Strings.isNullOrEmpty((String)string)) break block3;
                    for (TextCoordinates textCoordinates : collection) {
                        String string2 = textCoordinates.x + " " + textCoordinates.y + " " + textCoordinates.z;
                        if (!predicate.test(string2)) continue;
                        arrayList.add(textCoordinates.x);
                        arrayList.add(textCoordinates.x + " " + textCoordinates.y);
                        arrayList.add(string2);
                    }
                    break block4;
                }
                stringArray = string.split(" ");
                if (stringArray.length != 1) break block5;
                for (TextCoordinates textCoordinates : collection) {
                    String string3 = stringArray[0] + " " + textCoordinates.y + " " + textCoordinates.z;
                    if (!predicate.test(string3)) continue;
                    arrayList.add(stringArray[0] + " " + textCoordinates.y);
                    arrayList.add(string3);
                }
                break block4;
            }
            if (stringArray.length != 2) break block4;
            for (TextCoordinates textCoordinates : collection) {
                String string4 = stringArray[0] + " " + stringArray[1] + " " + textCoordinates.z;
                if (!predicate.test(string4)) continue;
                arrayList.add(string4);
            }
        }
        return SharedSuggestionProvider.suggest(arrayList, suggestionsBuilder);
    }

    public static CompletableFuture<Suggestions> suggest2DCoordinates(String string, Collection<TextCoordinates> collection, SuggestionsBuilder suggestionsBuilder, Predicate<String> predicate) {
        ArrayList arrayList;
        block3: {
            block2: {
                arrayList = Lists.newArrayList();
                if (!Strings.isNullOrEmpty((String)string)) break block2;
                for (TextCoordinates textCoordinates : collection) {
                    String string2 = textCoordinates.x + " " + textCoordinates.z;
                    if (!predicate.test(string2)) continue;
                    arrayList.add(textCoordinates.x);
                    arrayList.add(string2);
                }
                break block3;
            }
            String[] stringArray = string.split(" ");
            if (stringArray.length != 1) break block3;
            for (TextCoordinates textCoordinates : collection) {
                String string3 = stringArray[0] + " " + textCoordinates.z;
                if (!predicate.test(string3)) continue;
                arrayList.add(string3);
            }
        }
        return SharedSuggestionProvider.suggest(arrayList, suggestionsBuilder);
    }

    public static CompletableFuture<Suggestions> suggest(Iterable<String> iterable, SuggestionsBuilder suggestionsBuilder) {
        String string = suggestionsBuilder.getRemaining().toLowerCase(Locale.ROOT);
        for (String string2 : iterable) {
            if (!SharedSuggestionProvider.matchesSubStr(string, string2.toLowerCase(Locale.ROOT))) continue;
            suggestionsBuilder.suggest(string2);
        }
        return suggestionsBuilder.buildFuture();
    }

    public static CompletableFuture<Suggestions> suggest(Stream<String> stream, SuggestionsBuilder suggestionsBuilder) {
        String string = suggestionsBuilder.getRemaining().toLowerCase(Locale.ROOT);
        stream.filter(string2 -> SharedSuggestionProvider.matchesSubStr(string, string2.toLowerCase(Locale.ROOT))).forEach(arg_0 -> ((SuggestionsBuilder)suggestionsBuilder).suggest(arg_0));
        return suggestionsBuilder.buildFuture();
    }

    public static CompletableFuture<Suggestions> suggest(String[] stringArray, SuggestionsBuilder suggestionsBuilder) {
        String string = suggestionsBuilder.getRemaining().toLowerCase(Locale.ROOT);
        for (String string2 : stringArray) {
            if (!SharedSuggestionProvider.matchesSubStr(string, string2.toLowerCase(Locale.ROOT))) continue;
            suggestionsBuilder.suggest(string2);
        }
        return suggestionsBuilder.buildFuture();
    }

    public static <T> CompletableFuture<Suggestions> suggest(Iterable<T> iterable, SuggestionsBuilder suggestionsBuilder, Function<T, String> function, Function<T, Message> function2) {
        String string = suggestionsBuilder.getRemaining().toLowerCase(Locale.ROOT);
        for (T t : iterable) {
            String string2 = function.apply(t);
            if (!SharedSuggestionProvider.matchesSubStr(string, string2.toLowerCase(Locale.ROOT))) continue;
            suggestionsBuilder.suggest(string2, function2.apply(t));
        }
        return suggestionsBuilder.buildFuture();
    }

    public static boolean matchesSubStr(String string, String string2) {
        int n = 0;
        while (!string2.startsWith(string, n)) {
            int n2;
            int n3 = string2.indexOf(46, n);
            if (Math.max(n3, n2 = string2.indexOf(95, n)) < 0) {
                return false;
            }
            n = n3 >= 0 && n2 >= 0 ? Math.min(n2, n3) : (n3 >= 0 ? n3 : n2);
            ++n;
        }
        return true;
    }

    public static class TextCoordinates {
        public static final TextCoordinates DEFAULT_LOCAL = new TextCoordinates("^", "^", "^");
        public static final TextCoordinates DEFAULT_GLOBAL = new TextCoordinates("~", "~", "~");
        public final String x;
        public final String y;
        public final String z;

        public TextCoordinates(String string, String string2, String string3) {
            this.x = string;
            this.y = string2;
            this.z = string3;
        }
    }

    public static enum ElementSuggestionType {
        TAGS,
        ELEMENTS,
        ALL;


        public boolean shouldSuggestTags() {
            return this == TAGS || this == ALL;
        }

        public boolean shouldSuggestElements() {
            return this == ELEMENTS || this == ALL;
        }
    }
}

