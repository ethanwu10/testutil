package dev.ethanwu.testutil

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import java.util.stream.Stream
import kotlin.random.Random
import kotlin.streams.toList

internal class TestCaseGeneratorTest {
    @TestFactory
    fun demo(): Stream<DynamicTest> {
        val rng = Random(0)
        return TestCaseGenerator.create(
            Pair("unique items", listOf(1, 2, 3, 4)),
            Pair("some duplicate items", listOf(1, 2, 2, 4)),
            Pair("all duplicate items", listOf(1, 1, 1, 1))
        ).join { it.toMutableList() }.join(
            Pair("", { it }),
            Pair("reversed", { it.reverse(); it }),
            Pair("shuffled", { it.shuffle(rng); it })
        ).execute { l ->
            val sorted = l.toMutableList()
            sorted.sort()
            for (i in 0 until sorted.size - 1) {
                assertTrue(sorted[i] <= sorted[i + 1])
            }
        }
    }

    @Test
    fun `chaining performs a Cartesian product`() {
        val collectedInputs = mutableSetOf<String>()
        val testStream = TestCaseGenerator.create(
            Pair("a1", "a1"),
            Pair("a2", "a2"),
        ).join(
            Pair("b1", { it + "b1" }),
            Pair("b2", { it + "b2" }),
        ).execute(collectedInputs::add).toList()

        assertEquals(4, testStream.size)
        assertEquals(setOf("a1 b1", "a1 b2", "a2 b1", "a2 b2"), testStream.map { it.displayName }.toSet())

        testStream.forEach { it.executable.execute() }

        assertEquals(setOf("a1b1", "a1b2", "a2b1", "a2b2"), collectedInputs)
    }

    @Test
    fun `initial name space is respected`() {
        val testStream = TestCaseGenerator.create("name").join(
            Pair("a", { })
        ).toDynamicTestStream()
        assertEquals(listOf("name a"), testStream.map { it.displayName }.toList())
    }

    @Test
    fun `no name modifier does not add space`() {
        val collectedInputs = mutableSetOf<String>()
        val testStream = TestCaseGenerator.create("a", "a").join(
            Pair("", { it + "b" })
        ).join(
            Pair("c", { it + "c" })
        ).execute(collectedInputs::add).toList()

        assertEquals(listOf("a c"), testStream.map { it.displayName })

        testStream.forEach { it.executable.execute() }

        assertEquals(setOf("abc"), collectedInputs)
    }
}
