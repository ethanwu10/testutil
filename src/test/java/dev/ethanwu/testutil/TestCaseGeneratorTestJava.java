package dev.ethanwu.testutil;

import kotlin.Pair;
import kotlin.jvm.functions.Function1;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class TestCaseGeneratorTestJava {
    @TestFactory
    Stream<DynamicTest> demo() {
        Random rng = new Random(0);
        //noinspection unchecked
        return TestCaseGenerator.create(
                new Pair<>("unique items", List.of(1, 2, 3, 4)),
                new Pair<>("some duplicate items", List.of(1, 2, 2, 4)),
                new Pair<>("all duplicate items", List.of(1, 1, 1, 1))
        ).join(l -> new ArrayList<>(l)).join(
                new Pair<String, Function1<List<Integer>, List<Integer>>>("", l -> l),
                new Pair<String, Function1<List<Integer>, List<Integer>>>("reversed", l -> { Collections.reverse(l); return l; }),
                new Pair<String, Function1<List<Integer>, List<Integer>>>("shuffled", l -> { Collections.shuffle(l, rng); return l; })
        ).join(l -> {
            List<Integer> sorted = new ArrayList<>(l);
            Collections.sort(sorted);

            for (int i = 0; i < sorted.size() - 1; i++) {
                assertTrue(sorted.get(i) <= sorted.get(i + 1));
            }

            // must return something to satisfy function interface!
            return true;
            // Technically the return should be Unit.INSTANCE, but it does not matter
        }).toDynamicTestStream();
    }

    @Test
    void crossProduct() {
        Set<String> collectedInputs = new HashSet<>();

        @SuppressWarnings("unchecked")
        List<DynamicTest> testStream = TestCaseGenerator.create(
                new Pair<>("a1", "a1"),
                new Pair<>("a2", "a2")
        ).join(
                new Pair<String, Function1<String, String>>("b1", s -> s + "b1"),
                new Pair<String, Function1<String, String>>("b2", s -> s + "b2")
        ).join(collectedInputs::add).toDynamicTestStream().collect(Collectors.toList());

        assertEquals(4, testStream.size());
        assertEquals(
                Set.of("a1 b1", "a1 b2", "a2 b1", "a2 b2"),
                testStream.stream().map(DynamicNode::getDisplayName).collect(Collectors.toSet())
        );

        testStream.forEach(tc -> {
            try {
                tc.getExecutable().execute();
            } catch (Throwable ignored) {
            }
        });

        assertEquals(Set.of("a1b1", "a1b2", "a2b1", "a2b2"), collectedInputs);
    }
}
