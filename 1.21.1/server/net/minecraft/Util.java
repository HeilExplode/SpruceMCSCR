/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Ticker
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.Iterators
 *  com.google.common.collect.Lists
 *  com.google.common.util.concurrent.MoreExecutors
 *  com.mojang.datafixers.DSL$TypeReference
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.Dynamic
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap
 *  it.unimi.dsi.fastutil.objects.ReferenceImmutableList
 *  it.unimi.dsi.fastutil.objects.ReferenceList
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft;

import com.google.common.base.Ticker;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.MoreExecutors;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceImmutableList;
import it.unimi.dsi.fastutil.objects.ReferenceList;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.CharPredicate;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.ReportType;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.SingleKeyCache;
import net.minecraft.util.TimeSource;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.block.state.properties.Property;
import org.slf4j.Logger;

public class Util {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final int DEFAULT_MAX_THREADS = 255;
    private static final int DEFAULT_SAFE_FILE_OPERATION_RETRIES = 10;
    private static final String MAX_THREADS_SYSTEM_PROPERTY = "max.bg.threads";
    private static final ExecutorService BACKGROUND_EXECUTOR = Util.makeExecutor("Main");
    private static final ExecutorService IO_POOL = Util.makeIoExecutor("IO-Worker-", false);
    private static final ExecutorService DOWNLOAD_POOL = Util.makeIoExecutor("Download-", true);
    private static final DateTimeFormatter FILENAME_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss", Locale.ROOT);
    public static final int LINEAR_LOOKUP_THRESHOLD = 8;
    private static final Set<String> ALLOWED_UNTRUSTED_LINK_PROTOCOLS = Set.of("http", "https");
    public static final long NANOS_PER_MILLI = 1000000L;
    public static TimeSource.NanoTimeSource timeSource = System::nanoTime;
    public static final Ticker TICKER = new Ticker(){

        public long read() {
            return timeSource.getAsLong();
        }
    };
    public static final UUID NIL_UUID = new UUID(0L, 0L);
    public static final FileSystemProvider ZIP_FILE_SYSTEM_PROVIDER = FileSystemProvider.installedProviders().stream().filter(fileSystemProvider -> fileSystemProvider.getScheme().equalsIgnoreCase("jar")).findFirst().orElseThrow(() -> new IllegalStateException("No jar file system provider found"));
    private static Consumer<String> thePauser = string -> {};

    public static <K, V> Collector<Map.Entry<? extends K, ? extends V>, ?, Map<K, V>> toMap() {
        return Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue);
    }

    public static <T> Collector<T, ?, List<T>> toMutableList() {
        return Collectors.toCollection(Lists::newArrayList);
    }

    public static <T extends Comparable<T>> String getPropertyName(Property<T> property, Object object) {
        return property.getName((Comparable)object);
    }

    public static String makeDescriptionId(String string, @Nullable ResourceLocation resourceLocation) {
        if (resourceLocation == null) {
            return string + ".unregistered_sadface";
        }
        return string + "." + resourceLocation.getNamespace() + "." + resourceLocation.getPath().replace('/', '.');
    }

    public static long getMillis() {
        return Util.getNanos() / 1000000L;
    }

    public static long getNanos() {
        return timeSource.getAsLong();
    }

    public static long getEpochMillis() {
        return Instant.now().toEpochMilli();
    }

    public static String getFilenameFormattedDateTime() {
        return FILENAME_DATE_TIME_FORMATTER.format(ZonedDateTime.now());
    }

    private static ExecutorService makeExecutor(String string) {
        Object object;
        int n = Mth.clamp(Runtime.getRuntime().availableProcessors() - 1, 1, Util.getMaxThreads());
        if (n <= 0) {
            object = MoreExecutors.newDirectExecutorService();
        } else {
            AtomicInteger atomicInteger = new AtomicInteger(1);
            object = new ForkJoinPool(n, forkJoinPool -> {
                ForkJoinWorkerThread forkJoinWorkerThread = new ForkJoinWorkerThread(forkJoinPool){

                    @Override
                    protected void onTermination(Throwable throwable) {
                        if (throwable != null) {
                            LOGGER.warn("{} died", (Object)this.getName(), (Object)throwable);
                        } else {
                            LOGGER.debug("{} shutdown", (Object)this.getName());
                        }
                        super.onTermination(throwable);
                    }
                };
                forkJoinWorkerThread.setName("Worker-" + string + "-" + atomicInteger.getAndIncrement());
                return forkJoinWorkerThread;
            }, Util::onThreadException, true);
        }
        return object;
    }

    private static int getMaxThreads() {
        String string = System.getProperty(MAX_THREADS_SYSTEM_PROPERTY);
        if (string != null) {
            try {
                int n = Integer.parseInt(string);
                if (n >= 1 && n <= 255) {
                    return n;
                }
                LOGGER.error("Wrong {} property value '{}'. Should be an integer value between 1 and {}.", new Object[]{MAX_THREADS_SYSTEM_PROPERTY, string, 255});
            }
            catch (NumberFormatException numberFormatException) {
                LOGGER.error("Could not parse {} property value '{}'. Should be an integer value between 1 and {}.", new Object[]{MAX_THREADS_SYSTEM_PROPERTY, string, 255});
            }
        }
        return 255;
    }

    public static ExecutorService backgroundExecutor() {
        return BACKGROUND_EXECUTOR;
    }

    public static ExecutorService ioPool() {
        return IO_POOL;
    }

    public static ExecutorService nonCriticalIoPool() {
        return DOWNLOAD_POOL;
    }

    public static void shutdownExecutors() {
        Util.shutdownExecutor(BACKGROUND_EXECUTOR);
        Util.shutdownExecutor(IO_POOL);
    }

    private static void shutdownExecutor(ExecutorService executorService) {
        boolean bl;
        executorService.shutdown();
        try {
            bl = executorService.awaitTermination(3L, TimeUnit.SECONDS);
        }
        catch (InterruptedException interruptedException) {
            bl = false;
        }
        if (!bl) {
            executorService.shutdownNow();
        }
    }

    private static ExecutorService makeIoExecutor(String string, boolean bl) {
        AtomicInteger atomicInteger = new AtomicInteger(1);
        return Executors.newCachedThreadPool(runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName(string + atomicInteger.getAndIncrement());
            thread.setDaemon(bl);
            thread.setUncaughtExceptionHandler(Util::onThreadException);
            return thread;
        });
    }

    public static void throwAsRuntime(Throwable throwable) {
        throw throwable instanceof RuntimeException ? (RuntimeException)throwable : new RuntimeException(throwable);
    }

    private static void onThreadException(Thread thread, Throwable throwable) {
        Util.pauseInIde(throwable);
        if (throwable instanceof CompletionException) {
            throwable = throwable.getCause();
        }
        if (throwable instanceof ReportedException) {
            ReportedException reportedException = (ReportedException)throwable;
            Bootstrap.realStdoutPrintln(reportedException.getReport().getFriendlyReport(ReportType.CRASH));
            System.exit(-1);
        }
        LOGGER.error(String.format(Locale.ROOT, "Caught exception in thread %s", thread), throwable);
    }

    @Nullable
    public static Type<?> fetchChoiceType(DSL.TypeReference typeReference, String string) {
        if (!SharedConstants.CHECK_DATA_FIXER_SCHEMA) {
            return null;
        }
        return Util.doFetchChoiceType(typeReference, string);
    }

    @Nullable
    private static Type<?> doFetchChoiceType(DSL.TypeReference typeReference, String string) {
        Type type;
        block2: {
            type = null;
            try {
                type = DataFixers.getDataFixer().getSchema(DataFixUtils.makeKey((int)SharedConstants.getCurrentVersion().getDataVersion().getVersion())).getChoiceType(typeReference, string);
            }
            catch (IllegalArgumentException illegalArgumentException) {
                LOGGER.error("No data fixer registered for {}", (Object)string);
                if (!SharedConstants.IS_RUNNING_IN_IDE) break block2;
                throw illegalArgumentException;
            }
        }
        return type;
    }

    public static Runnable wrapThreadWithTaskName(String string, Runnable runnable) {
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            return () -> {
                Thread thread = Thread.currentThread();
                String string2 = thread.getName();
                thread.setName(string);
                try {
                    runnable.run();
                }
                finally {
                    thread.setName(string2);
                }
            };
        }
        return runnable;
    }

    public static <V> Supplier<V> wrapThreadWithTaskName(String string, Supplier<V> supplier) {
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            return () -> {
                Thread thread = Thread.currentThread();
                String string2 = thread.getName();
                thread.setName(string);
                try {
                    Object t = supplier.get();
                    return t;
                }
                finally {
                    thread.setName(string2);
                }
            };
        }
        return supplier;
    }

    public static <T> String getRegisteredName(Registry<T> registry, T t) {
        ResourceLocation resourceLocation = registry.getKey(t);
        if (resourceLocation == null) {
            return "[unregistered]";
        }
        return resourceLocation.toString();
    }

    public static <T> Predicate<T> allOf(List<? extends Predicate<T>> list) {
        return switch (list.size()) {
            case 0 -> object -> true;
            case 1 -> list.get(0);
            case 2 -> list.get(0).and(list.get(1));
            default -> {
                Predicate[] var1_1 = (Predicate[])list.toArray(Predicate[]::new);
                yield object -> {
                    for (Predicate predicate : var1_1) {
                        if (predicate.test(object)) continue;
                        return false;
                    }
                    return true;
                };
            }
        };
    }

    public static <T> Predicate<T> anyOf(List<? extends Predicate<T>> list) {
        return switch (list.size()) {
            case 0 -> object -> false;
            case 1 -> list.get(0);
            case 2 -> list.get(0).or(list.get(1));
            default -> {
                Predicate[] var1_1 = (Predicate[])list.toArray(Predicate[]::new);
                yield object -> {
                    for (Predicate predicate : var1_1) {
                        if (!predicate.test(object)) continue;
                        return true;
                    }
                    return false;
                };
            }
        };
    }

    public static <T> boolean isSymmetrical(int n, int n2, List<T> list) {
        if (n == 1) {
            return true;
        }
        int n3 = n / 2;
        for (int i = 0; i < n2; ++i) {
            for (int j = 0; j < n3; ++j) {
                T t;
                int n4 = n - 1 - j;
                T t2 = list.get(j + i * n);
                if (t2.equals(t = list.get(n4 + i * n))) continue;
                return false;
            }
        }
        return true;
    }

    public static OS getPlatform() {
        String string = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if (string.contains("win")) {
            return OS.WINDOWS;
        }
        if (string.contains("mac")) {
            return OS.OSX;
        }
        if (string.contains("solaris")) {
            return OS.SOLARIS;
        }
        if (string.contains("sunos")) {
            return OS.SOLARIS;
        }
        if (string.contains("linux")) {
            return OS.LINUX;
        }
        if (string.contains("unix")) {
            return OS.LINUX;
        }
        return OS.UNKNOWN;
    }

    public static URI parseAndValidateUntrustedUri(String string) throws URISyntaxException {
        URI uRI = new URI(string);
        String string2 = uRI.getScheme();
        if (string2 == null) {
            throw new URISyntaxException(string, "Missing protocol in URI: " + string);
        }
        String string3 = string2.toLowerCase(Locale.ROOT);
        if (!ALLOWED_UNTRUSTED_LINK_PROTOCOLS.contains(string3)) {
            throw new URISyntaxException(string, "Unsupported protocol in URI: " + string);
        }
        return uRI;
    }

    public static Stream<String> getVmArguments() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        return runtimeMXBean.getInputArguments().stream().filter(string -> string.startsWith("-X"));
    }

    public static <T> T lastOf(List<T> list) {
        return list.get(list.size() - 1);
    }

    public static <T> T findNextInIterable(Iterable<T> iterable, @Nullable T t) {
        Iterator<T> iterator = iterable.iterator();
        T t2 = iterator.next();
        if (t != null) {
            T t3 = t2;
            while (true) {
                if (t3 == t) {
                    if (!iterator.hasNext()) break;
                    return iterator.next();
                }
                if (!iterator.hasNext()) continue;
                t3 = iterator.next();
            }
        }
        return t2;
    }

    public static <T> T findPreviousInIterable(Iterable<T> iterable, @Nullable T t) {
        Iterator<T> iterator = iterable.iterator();
        T t2 = null;
        while (iterator.hasNext()) {
            T t3 = iterator.next();
            if (t3 == t) {
                if (t2 != null) break;
                t2 = (T)(iterator.hasNext() ? Iterators.getLast(iterator) : t);
                break;
            }
            t2 = t3;
        }
        return t2;
    }

    public static <T> T make(Supplier<T> supplier) {
        return supplier.get();
    }

    public static <T> T make(T t, Consumer<? super T> consumer) {
        consumer.accept(t);
        return t;
    }

    public static <V> CompletableFuture<List<V>> sequence(List<? extends CompletableFuture<V>> list) {
        if (list.isEmpty()) {
            return CompletableFuture.completedFuture(List.of());
        }
        if (list.size() == 1) {
            return list.get(0).thenApply(List::of);
        }
        CompletableFuture<Void> completableFuture = CompletableFuture.allOf(list.toArray(new CompletableFuture[0]));
        return completableFuture.thenApply(void_ -> list.stream().map(CompletableFuture::join).toList());
    }

    public static <V> CompletableFuture<List<V>> sequenceFailFast(List<? extends CompletableFuture<? extends V>> list) {
        CompletableFuture completableFuture = new CompletableFuture();
        return Util.fallibleSequence(list, completableFuture::completeExceptionally).applyToEither((CompletionStage)completableFuture, Function.identity());
    }

    public static <V> CompletableFuture<List<V>> sequenceFailFastAndCancel(List<? extends CompletableFuture<? extends V>> list) {
        CompletableFuture completableFuture = new CompletableFuture();
        return Util.fallibleSequence(list, throwable -> {
            if (completableFuture.completeExceptionally((Throwable)throwable)) {
                for (CompletableFuture completableFuture2 : list) {
                    completableFuture2.cancel(true);
                }
            }
        }).applyToEither((CompletionStage)completableFuture, Function.identity());
    }

    private static <V> CompletableFuture<List<V>> fallibleSequence(List<? extends CompletableFuture<? extends V>> list, Consumer<Throwable> consumer) {
        ArrayList arrayList = Lists.newArrayListWithCapacity((int)list.size());
        CompletableFuture[] completableFutureArray = new CompletableFuture[list.size()];
        list.forEach(completableFuture -> {
            int n = arrayList.size();
            arrayList.add(null);
            completableFutureArray[n] = completableFuture.whenComplete((object, throwable) -> {
                if (throwable != null) {
                    consumer.accept((Throwable)throwable);
                } else {
                    arrayList.set(n, object);
                }
            });
        });
        return CompletableFuture.allOf(completableFutureArray).thenApply(void_ -> arrayList);
    }

    public static <T> Optional<T> ifElse(Optional<T> optional, Consumer<T> consumer, Runnable runnable) {
        if (optional.isPresent()) {
            consumer.accept(optional.get());
        } else {
            runnable.run();
        }
        return optional;
    }

    public static <T> Supplier<T> name(Supplier<T> supplier, Supplier<String> supplier2) {
        return supplier;
    }

    public static Runnable name(Runnable runnable, Supplier<String> supplier) {
        return runnable;
    }

    public static void logAndPauseIfInIde(String string) {
        LOGGER.error(string);
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            Util.doPause(string);
        }
    }

    public static void logAndPauseIfInIde(String string, Throwable throwable) {
        LOGGER.error(string, throwable);
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            Util.doPause(string);
        }
    }

    public static <T extends Throwable> T pauseInIde(T t) {
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            LOGGER.error("Trying to throw a fatal exception, pausing in IDE", t);
            Util.doPause(t.getMessage());
        }
        return t;
    }

    public static void setPause(Consumer<String> consumer) {
        thePauser = consumer;
    }

    private static void doPause(String string) {
        boolean bl;
        Instant instant = Instant.now();
        LOGGER.warn("Did you remember to set a breakpoint here?");
        boolean bl2 = bl = Duration.between(instant, Instant.now()).toMillis() > 500L;
        if (!bl) {
            thePauser.accept(string);
        }
    }

    public static String describeError(Throwable throwable) {
        if (throwable.getCause() != null) {
            return Util.describeError(throwable.getCause());
        }
        if (throwable.getMessage() != null) {
            return throwable.getMessage();
        }
        return throwable.toString();
    }

    public static <T> T getRandom(T[] TArray, RandomSource randomSource) {
        return TArray[randomSource.nextInt(TArray.length)];
    }

    public static int getRandom(int[] nArray, RandomSource randomSource) {
        return nArray[randomSource.nextInt(nArray.length)];
    }

    public static <T> T getRandom(List<T> list, RandomSource randomSource) {
        return list.get(randomSource.nextInt(list.size()));
    }

    public static <T> Optional<T> getRandomSafe(List<T> list, RandomSource randomSource) {
        if (list.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Util.getRandom(list, randomSource));
    }

    private static BooleanSupplier createRenamer(final Path path, final Path path2) {
        return new BooleanSupplier(){

            @Override
            public boolean getAsBoolean() {
                try {
                    Files.move(path, path2, new CopyOption[0]);
                    return true;
                }
                catch (IOException iOException) {
                    LOGGER.error("Failed to rename", (Throwable)iOException);
                    return false;
                }
            }

            public String toString() {
                return "rename " + String.valueOf(path) + " to " + String.valueOf(path2);
            }
        };
    }

    private static BooleanSupplier createDeleter(final Path path) {
        return new BooleanSupplier(){

            @Override
            public boolean getAsBoolean() {
                try {
                    Files.deleteIfExists(path);
                    return true;
                }
                catch (IOException iOException) {
                    LOGGER.warn("Failed to delete", (Throwable)iOException);
                    return false;
                }
            }

            public String toString() {
                return "delete old " + String.valueOf(path);
            }
        };
    }

    private static BooleanSupplier createFileDeletedCheck(final Path path) {
        return new BooleanSupplier(){

            @Override
            public boolean getAsBoolean() {
                return !Files.exists(path, new LinkOption[0]);
            }

            public String toString() {
                return "verify that " + String.valueOf(path) + " is deleted";
            }
        };
    }

    private static BooleanSupplier createFileCreatedCheck(final Path path) {
        return new BooleanSupplier(){

            @Override
            public boolean getAsBoolean() {
                return Files.isRegularFile(path, new LinkOption[0]);
            }

            public String toString() {
                return "verify that " + String.valueOf(path) + " is present";
            }
        };
    }

    private static boolean executeInSequence(BooleanSupplier ... booleanSupplierArray) {
        for (BooleanSupplier booleanSupplier : booleanSupplierArray) {
            if (booleanSupplier.getAsBoolean()) continue;
            LOGGER.warn("Failed to execute {}", (Object)booleanSupplier);
            return false;
        }
        return true;
    }

    private static boolean runWithRetries(int n, String string, BooleanSupplier ... booleanSupplierArray) {
        for (int i = 0; i < n; ++i) {
            if (Util.executeInSequence(booleanSupplierArray)) {
                return true;
            }
            LOGGER.error("Failed to {}, retrying {}/{}", new Object[]{string, i, n});
        }
        LOGGER.error("Failed to {}, aborting, progress might be lost", (Object)string);
        return false;
    }

    public static void safeReplaceFile(Path path, Path path2, Path path3) {
        Util.safeReplaceOrMoveFile(path, path2, path3, false);
    }

    public static boolean safeReplaceOrMoveFile(Path path, Path path2, Path path3, boolean bl) {
        if (Files.exists(path, new LinkOption[0]) && !Util.runWithRetries(10, "create backup " + String.valueOf(path3), Util.createDeleter(path3), Util.createRenamer(path, path3), Util.createFileCreatedCheck(path3))) {
            return false;
        }
        if (!Util.runWithRetries(10, "remove old " + String.valueOf(path), Util.createDeleter(path), Util.createFileDeletedCheck(path))) {
            return false;
        }
        if (!Util.runWithRetries(10, "replace " + String.valueOf(path) + " with " + String.valueOf(path2), Util.createRenamer(path2, path), Util.createFileCreatedCheck(path)) && !bl) {
            Util.runWithRetries(10, "restore " + String.valueOf(path) + " from " + String.valueOf(path3), Util.createRenamer(path3, path), Util.createFileCreatedCheck(path));
            return false;
        }
        return true;
    }

    public static int offsetByCodepoints(String string, int n, int n2) {
        int n3 = string.length();
        if (n2 >= 0) {
            for (int i = 0; n < n3 && i < n2; ++i) {
                if (!Character.isHighSurrogate(string.charAt(n++)) || n >= n3 || !Character.isLowSurrogate(string.charAt(n))) continue;
                ++n;
            }
        } else {
            for (int i = n2; n > 0 && i < 0; ++i) {
                if (!Character.isLowSurrogate(string.charAt(--n)) || n <= 0 || !Character.isHighSurrogate(string.charAt(n - 1))) continue;
                --n;
            }
        }
        return n;
    }

    public static Consumer<String> prefix(String string, Consumer<String> consumer) {
        return string2 -> consumer.accept(string + string2);
    }

    public static DataResult<int[]> fixedSize(IntStream intStream, int n) {
        int[] nArray = intStream.limit(n + 1).toArray();
        if (nArray.length != n) {
            Supplier<String> supplier = () -> "Input is not a list of " + n + " ints";
            if (nArray.length >= n) {
                return DataResult.error(supplier, (Object)Arrays.copyOf(nArray, n));
            }
            return DataResult.error(supplier);
        }
        return DataResult.success((Object)nArray);
    }

    public static DataResult<long[]> fixedSize(LongStream longStream, int n) {
        long[] lArray = longStream.limit(n + 1).toArray();
        if (lArray.length != n) {
            Supplier<String> supplier = () -> "Input is not a list of " + n + " longs";
            if (lArray.length >= n) {
                return DataResult.error(supplier, (Object)Arrays.copyOf(lArray, n));
            }
            return DataResult.error(supplier);
        }
        return DataResult.success((Object)lArray);
    }

    public static <T> DataResult<List<T>> fixedSize(List<T> list, int n) {
        if (list.size() != n) {
            Supplier<String> supplier = () -> "Input is not a list of " + n + " elements";
            if (list.size() >= n) {
                return DataResult.error(supplier, list.subList(0, n));
            }
            return DataResult.error(supplier);
        }
        return DataResult.success(list);
    }

    public static void startTimerHackThread() {
        Thread thread = new Thread("Timer hack thread"){

            @Override
            public void run() {
                try {
                    while (true) {
                        Thread.sleep(Integer.MAX_VALUE);
                    }
                }
                catch (InterruptedException interruptedException) {
                    LOGGER.warn("Timer hack thread interrupted, that really should not happen");
                    return;
                }
            }
        };
        thread.setDaemon(true);
        thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
        thread.start();
    }

    public static void copyBetweenDirs(Path path, Path path2, Path path3) throws IOException {
        Path path4 = path.relativize(path3);
        Path path5 = path2.resolve(path4);
        Files.copy(path3, path5, new CopyOption[0]);
    }

    public static String sanitizeName(String string, CharPredicate charPredicate) {
        return string.toLowerCase(Locale.ROOT).chars().mapToObj(n -> charPredicate.test((char)n) ? Character.toString((char)n) : "_").collect(Collectors.joining());
    }

    public static <K, V> SingleKeyCache<K, V> singleKeyCache(Function<K, V> function) {
        return new SingleKeyCache<K, V>(function);
    }

    public static <T, R> Function<T, R> memoize(final Function<T, R> function) {
        return new Function<T, R>(){
            private final Map<T, R> cache = new ConcurrentHashMap();

            @Override
            public R apply(T t) {
                return this.cache.computeIfAbsent(t, function);
            }

            public String toString() {
                return "memoize/1[function=" + String.valueOf(function) + ", size=" + this.cache.size() + "]";
            }
        };
    }

    public static <T, U, R> BiFunction<T, U, R> memoize(final BiFunction<T, U, R> biFunction) {
        return new BiFunction<T, U, R>(){
            private final Map<Pair<T, U>, R> cache = new ConcurrentHashMap();

            @Override
            public R apply(T t, U u) {
                return this.cache.computeIfAbsent(Pair.of(t, u), pair -> biFunction.apply(pair.getFirst(), pair.getSecond()));
            }

            public String toString() {
                return "memoize/2[function=" + String.valueOf(biFunction) + ", size=" + this.cache.size() + "]";
            }
        };
    }

    public static <T> List<T> toShuffledList(Stream<T> stream, RandomSource randomSource) {
        ObjectArrayList objectArrayList = (ObjectArrayList)stream.collect(ObjectArrayList.toList());
        Util.shuffle(objectArrayList, randomSource);
        return objectArrayList;
    }

    public static IntArrayList toShuffledList(IntStream intStream, RandomSource randomSource) {
        int n;
        IntArrayList intArrayList = IntArrayList.wrap((int[])intStream.toArray());
        for (int i = n = intArrayList.size(); i > 1; --i) {
            int n2 = randomSource.nextInt(i);
            intArrayList.set(i - 1, intArrayList.set(n2, intArrayList.getInt(i - 1)));
        }
        return intArrayList;
    }

    public static <T> List<T> shuffledCopy(T[] TArray, RandomSource randomSource) {
        ObjectArrayList objectArrayList = new ObjectArrayList((Object[])TArray);
        Util.shuffle(objectArrayList, randomSource);
        return objectArrayList;
    }

    public static <T> List<T> shuffledCopy(ObjectArrayList<T> objectArrayList, RandomSource randomSource) {
        ObjectArrayList objectArrayList2 = new ObjectArrayList(objectArrayList);
        Util.shuffle(objectArrayList2, randomSource);
        return objectArrayList2;
    }

    public static <T> void shuffle(List<T> list, RandomSource randomSource) {
        int n;
        for (int i = n = list.size(); i > 1; --i) {
            int n2 = randomSource.nextInt(i);
            list.set(i - 1, list.set(n2, list.get(i - 1)));
        }
    }

    public static <T> CompletableFuture<T> blockUntilDone(Function<Executor, CompletableFuture<T>> function) {
        return Util.blockUntilDone(function, CompletableFuture::isDone);
    }

    public static <T> T blockUntilDone(Function<Executor, T> function, Predicate<T> predicate) {
        int n;
        LinkedBlockingQueue linkedBlockingQueue = new LinkedBlockingQueue();
        T t = function.apply(linkedBlockingQueue::add);
        while (!predicate.test(t)) {
            try {
                Runnable runnable = (Runnable)linkedBlockingQueue.poll(100L, TimeUnit.MILLISECONDS);
                if (runnable == null) continue;
                runnable.run();
            }
            catch (InterruptedException interruptedException) {
                LOGGER.warn("Interrupted wait");
                break;
            }
        }
        if ((n = linkedBlockingQueue.size()) > 0) {
            LOGGER.warn("Tasks left in queue: {}", (Object)n);
        }
        return t;
    }

    public static <T> ToIntFunction<T> createIndexLookup(List<T> list) {
        int n = list.size();
        if (n < 8) {
            return list::indexOf;
        }
        Object2IntOpenHashMap object2IntOpenHashMap = new Object2IntOpenHashMap(n);
        object2IntOpenHashMap.defaultReturnValue(-1);
        for (int i = 0; i < n; ++i) {
            object2IntOpenHashMap.put(list.get(i), i);
        }
        return object2IntOpenHashMap;
    }

    public static <T> ToIntFunction<T> createIndexIdentityLookup(List<T> list) {
        int n = list.size();
        if (n < 8) {
            ReferenceImmutableList referenceImmutableList = new ReferenceImmutableList(list);
            return arg_0 -> ((ReferenceList)referenceImmutableList).indexOf(arg_0);
        }
        Reference2IntOpenHashMap reference2IntOpenHashMap = new Reference2IntOpenHashMap(n);
        reference2IntOpenHashMap.defaultReturnValue(-1);
        for (int i = 0; i < n; ++i) {
            reference2IntOpenHashMap.put(list.get(i), i);
        }
        return reference2IntOpenHashMap;
    }

    public static <A, B> Typed<B> writeAndReadTypedOrThrow(Typed<A> typed, Type<B> type, UnaryOperator<Dynamic<?>> unaryOperator) {
        Dynamic dynamic = (Dynamic)typed.write().getOrThrow();
        return Util.readTypedOrThrow(type, (Dynamic)unaryOperator.apply(dynamic), true);
    }

    public static <T> Typed<T> readTypedOrThrow(Type<T> type, Dynamic<?> dynamic) {
        return Util.readTypedOrThrow(type, dynamic, false);
    }

    public static <T> Typed<T> readTypedOrThrow(Type<T> type, Dynamic<?> dynamic, boolean bl) {
        DataResult dataResult = type.readTyped(dynamic).map(Pair::getFirst);
        try {
            if (bl) {
                return (Typed)dataResult.getPartialOrThrow(IllegalStateException::new);
            }
            return (Typed)dataResult.getOrThrow(IllegalStateException::new);
        }
        catch (IllegalStateException illegalStateException) {
            CrashReport crashReport = CrashReport.forThrowable(illegalStateException, "Reading type");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Info");
            crashReportCategory.setDetail("Data", dynamic);
            crashReportCategory.setDetail("Type", type);
            throw new ReportedException(crashReport);
        }
    }

    public static <T> List<T> copyAndAdd(List<T> list, T t) {
        return ImmutableList.builderWithExpectedSize((int)(list.size() + 1)).addAll(list).add(t).build();
    }

    public static <T> List<T> copyAndAdd(T t, List<T> list) {
        return ImmutableList.builderWithExpectedSize((int)(list.size() + 1)).add(t).addAll(list).build();
    }

    public static <K, V> Map<K, V> copyAndPut(Map<K, V> map, K k, V v) {
        return ImmutableMap.builderWithExpectedSize((int)(map.size() + 1)).putAll(map).put(k, v).buildKeepingLast();
    }

    public static enum OS {
        LINUX("linux"),
        SOLARIS("solaris"),
        WINDOWS("windows"){

            @Override
            protected String[] getOpenUriArguments(URI uRI) {
                return new String[]{"rundll32", "url.dll,FileProtocolHandler", uRI.toString()};
            }
        }
        ,
        OSX("mac"){

            @Override
            protected String[] getOpenUriArguments(URI uRI) {
                return new String[]{"open", uRI.toString()};
            }
        }
        ,
        UNKNOWN("unknown");

        private final String telemetryName;

        OS(String string2) {
            this.telemetryName = string2;
        }

        public void openUri(URI uRI) {
            try {
                Process process = AccessController.doPrivileged(() -> Runtime.getRuntime().exec(this.getOpenUriArguments(uRI)));
                process.getInputStream().close();
                process.getErrorStream().close();
                process.getOutputStream().close();
            }
            catch (IOException | PrivilegedActionException exception) {
                LOGGER.error("Couldn't open location '{}'", (Object)uRI, (Object)exception);
            }
        }

        public void openFile(File file) {
            this.openUri(file.toURI());
        }

        public void openPath(Path path) {
            this.openUri(path.toUri());
        }

        protected String[] getOpenUriArguments(URI uRI) {
            String string = uRI.toString();
            if ("file".equals(uRI.getScheme())) {
                string = string.replace("file:", "file://");
            }
            return new String[]{"xdg-open", string};
        }

        public void openUri(String string) {
            try {
                this.openUri(new URI(string));
            }
            catch (IllegalArgumentException | URISyntaxException exception) {
                LOGGER.error("Couldn't open uri '{}'", (Object)string, (Object)exception);
            }
        }

        public String telemetryName() {
            return this.telemetryName;
        }
    }
}

