/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 */
package net.minecraft.commands.arguments.item;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.util.parsing.packrat.Atom;
import net.minecraft.util.parsing.packrat.Dictionary;
import net.minecraft.util.parsing.packrat.Term;
import net.minecraft.util.parsing.packrat.commands.Grammar;
import net.minecraft.util.parsing.packrat.commands.ResourceLocationParseRule;
import net.minecraft.util.parsing.packrat.commands.ResourceLookupRule;
import net.minecraft.util.parsing.packrat.commands.StringReaderTerms;
import net.minecraft.util.parsing.packrat.commands.TagParseRule;

public class ComponentPredicateParser {
    public static <T, C, P> Grammar<List<T>> createGrammar(Context<T, C, P> context) {
        Atom atom = Atom.of("top");
        Atom atom2 = Atom.of("type");
        Atom atom3 = Atom.of("any_type");
        Atom atom4 = Atom.of("element_type");
        Atom atom5 = Atom.of("tag_type");
        Atom atom6 = Atom.of("conditions");
        Atom atom7 = Atom.of("alternatives");
        Atom atom8 = Atom.of("term");
        Atom atom9 = Atom.of("negation");
        Atom atom10 = Atom.of("test");
        Atom atom11 = Atom.of("component_type");
        Atom atom12 = Atom.of("predicate_type");
        Atom<ResourceLocation> atom13 = Atom.of("id");
        Atom atom14 = Atom.of("tag");
        Dictionary<StringReader> dictionary = new Dictionary<StringReader>();
        dictionary.put(atom, Term.alternative(Term.sequence(Term.named(atom2), StringReaderTerms.character('['), Term.cut(), Term.optional(Term.named(atom6)), StringReaderTerms.character(']')), Term.named(atom2)), scope -> {
            ImmutableList.Builder builder = ImmutableList.builder();
            ((Optional)scope.getOrThrow(atom2)).ifPresent(arg_0 -> ((ImmutableList.Builder)builder).add(arg_0));
            List list = (List)scope.get(atom6);
            if (list != null) {
                builder.addAll((Iterable)list);
            }
            return builder.build();
        });
        dictionary.put(atom2, Term.alternative(Term.named(atom4), Term.sequence(StringReaderTerms.character('#'), Term.cut(), Term.named(atom5)), Term.named(atom3)), scope -> Optional.ofNullable(scope.getAny(atom4, atom5)));
        dictionary.put(atom3, StringReaderTerms.character('*'), scope -> Unit.INSTANCE);
        dictionary.put(atom4, new ElementLookupRule<T, C, P>(atom13, context));
        dictionary.put(atom5, new TagLookupRule<T, C, P>(atom13, context));
        dictionary.put(atom6, Term.sequence(Term.named(atom7), Term.optional(Term.sequence(StringReaderTerms.character(','), Term.named(atom6)))), scope -> {
            Object t = context.anyOf((List)scope.getOrThrow(atom7));
            return Optional.ofNullable((List)scope.get(atom6)).map(list -> Util.copyAndAdd(t, list)).orElse(List.of(t));
        });
        dictionary.put(atom7, Term.sequence(Term.named(atom8), Term.optional(Term.sequence(StringReaderTerms.character('|'), Term.named(atom7)))), scope -> {
            Object t = scope.getOrThrow(atom8);
            return Optional.ofNullable((List)scope.get(atom7)).map(list -> Util.copyAndAdd(t, list)).orElse(List.of(t));
        });
        dictionary.put(atom8, Term.alternative(Term.named(atom10), Term.sequence(StringReaderTerms.character('!'), Term.named(atom9))), scope -> scope.getAnyOrThrow(atom10, atom9));
        dictionary.put(atom9, Term.named(atom10), scope -> context.negate(scope.getOrThrow(atom10)));
        dictionary.put(atom10, Term.alternative(Term.sequence(Term.named(atom11), StringReaderTerms.character('='), Term.cut(), Term.named(atom14)), Term.sequence(Term.named(atom12), StringReaderTerms.character('~'), Term.cut(), Term.named(atom14)), Term.named(atom11)), (parseState, scope) -> {
            Object t = scope.get(atom12);
            try {
                if (t != null) {
                    Tag tag = (Tag)scope.getOrThrow(atom14);
                    return Optional.of(context.createPredicateTest((ImmutableStringReader)parseState.input(), t, tag));
                }
                Object t2 = scope.getOrThrow(atom11);
                Tag tag = (Tag)scope.get(atom14);
                return Optional.of(tag != null ? context.createComponentTest((ImmutableStringReader)parseState.input(), t2, tag) : context.createComponentTest((ImmutableStringReader)parseState.input(), t2));
            }
            catch (CommandSyntaxException commandSyntaxException) {
                parseState.errorCollector().store(parseState.mark(), (Object)commandSyntaxException);
                return Optional.empty();
            }
        });
        dictionary.put(atom11, new ComponentLookupRule<T, C, P>(atom13, context));
        dictionary.put(atom12, new PredicateLookupRule<T, C, P>(atom13, context));
        dictionary.put(atom14, TagParseRule.INSTANCE);
        dictionary.put(atom13, ResourceLocationParseRule.INSTANCE);
        return new Grammar<List<T>>(dictionary, atom);
    }

    static class ElementLookupRule<T, C, P>
    extends ResourceLookupRule<Context<T, C, P>, T> {
        ElementLookupRule(Atom<ResourceLocation> atom, Context<T, C, P> context) {
            super(atom, context);
        }

        @Override
        protected T validateElement(ImmutableStringReader immutableStringReader, ResourceLocation resourceLocation) throws Exception {
            return ((Context)this.context).forElementType(immutableStringReader, resourceLocation);
        }

        @Override
        public Stream<ResourceLocation> possibleResources() {
            return ((Context)this.context).listElementTypes();
        }
    }

    public static interface Context<T, C, P> {
        public T forElementType(ImmutableStringReader var1, ResourceLocation var2) throws CommandSyntaxException;

        public Stream<ResourceLocation> listElementTypes();

        public T forTagType(ImmutableStringReader var1, ResourceLocation var2) throws CommandSyntaxException;

        public Stream<ResourceLocation> listTagTypes();

        public C lookupComponentType(ImmutableStringReader var1, ResourceLocation var2) throws CommandSyntaxException;

        public Stream<ResourceLocation> listComponentTypes();

        public T createComponentTest(ImmutableStringReader var1, C var2, Tag var3) throws CommandSyntaxException;

        public T createComponentTest(ImmutableStringReader var1, C var2);

        public P lookupPredicateType(ImmutableStringReader var1, ResourceLocation var2) throws CommandSyntaxException;

        public Stream<ResourceLocation> listPredicateTypes();

        public T createPredicateTest(ImmutableStringReader var1, P var2, Tag var3) throws CommandSyntaxException;

        public T negate(T var1);

        public T anyOf(List<T> var1);
    }

    static class TagLookupRule<T, C, P>
    extends ResourceLookupRule<Context<T, C, P>, T> {
        TagLookupRule(Atom<ResourceLocation> atom, Context<T, C, P> context) {
            super(atom, context);
        }

        @Override
        protected T validateElement(ImmutableStringReader immutableStringReader, ResourceLocation resourceLocation) throws Exception {
            return ((Context)this.context).forTagType(immutableStringReader, resourceLocation);
        }

        @Override
        public Stream<ResourceLocation> possibleResources() {
            return ((Context)this.context).listTagTypes();
        }
    }

    static class ComponentLookupRule<T, C, P>
    extends ResourceLookupRule<Context<T, C, P>, C> {
        ComponentLookupRule(Atom<ResourceLocation> atom, Context<T, C, P> context) {
            super(atom, context);
        }

        @Override
        protected C validateElement(ImmutableStringReader immutableStringReader, ResourceLocation resourceLocation) throws Exception {
            return ((Context)this.context).lookupComponentType(immutableStringReader, resourceLocation);
        }

        @Override
        public Stream<ResourceLocation> possibleResources() {
            return ((Context)this.context).listComponentTypes();
        }
    }

    static class PredicateLookupRule<T, C, P>
    extends ResourceLookupRule<Context<T, C, P>, P> {
        PredicateLookupRule(Atom<ResourceLocation> atom, Context<T, C, P> context) {
            super(atom, context);
        }

        @Override
        protected P validateElement(ImmutableStringReader immutableStringReader, ResourceLocation resourceLocation) throws Exception {
            return ((Context)this.context).lookupPredicateType(immutableStringReader, resourceLocation);
        }

        @Override
        public Stream<ResourceLocation> possibleResources() {
            return ((Context)this.context).listPredicateTypes();
        }
    }
}

