/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Streams
 */
package net.minecraft.gametest.framework;

import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.gametest.framework.GameTestBatch;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GameTestRegistry;
import net.minecraft.gametest.framework.GameTestRunner;
import net.minecraft.gametest.framework.RetryOptions;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.server.level.ServerLevel;

public class GameTestBatchFactory {
    private static final int MAX_TESTS_PER_BATCH = 50;

    public static Collection<GameTestBatch> fromTestFunction(Collection<TestFunction> collection, ServerLevel serverLevel) {
        Map<String, List<TestFunction>> map = collection.stream().collect(Collectors.groupingBy(TestFunction::batchName));
        return map.entrySet().stream().flatMap(entry -> {
            String string = (String)entry.getKey();
            List list2 = (List)entry.getValue();
            return Streams.mapWithIndex(Lists.partition((List)list2, (int)50).stream(), (list, l) -> GameTestBatchFactory.toGameTestBatch(list.stream().map(testFunction -> GameTestBatchFactory.toGameTestInfo(testFunction, 0, serverLevel)).toList(), string, l));
        }).toList();
    }

    public static GameTestInfo toGameTestInfo(TestFunction testFunction, int n, ServerLevel serverLevel) {
        return new GameTestInfo(testFunction, StructureUtils.getRotationForRotationSteps(n), serverLevel, RetryOptions.noRetries());
    }

    public static GameTestRunner.GameTestBatcher fromGameTestInfo() {
        return GameTestBatchFactory.fromGameTestInfo(50);
    }

    public static GameTestRunner.GameTestBatcher fromGameTestInfo(int n) {
        return collection -> {
            Map<String, List<GameTestInfo>> map = collection.stream().filter(Objects::nonNull).collect(Collectors.groupingBy(gameTestInfo -> gameTestInfo.getTestFunction().batchName()));
            return map.entrySet().stream().flatMap(entry -> {
                String string = (String)entry.getKey();
                List list2 = (List)entry.getValue();
                return Streams.mapWithIndex(Lists.partition((List)list2, (int)n).stream(), (list, l) -> GameTestBatchFactory.toGameTestBatch(List.copyOf(list), string, l));
            }).toList();
        };
    }

    public static GameTestBatch toGameTestBatch(Collection<GameTestInfo> collection, String string, long l) {
        Consumer<ServerLevel> consumer = GameTestRegistry.getBeforeBatchFunction(string);
        Consumer<ServerLevel> consumer2 = GameTestRegistry.getAfterBatchFunction(string);
        return new GameTestBatch(string + ":" + l, collection, consumer, consumer2);
    }
}

