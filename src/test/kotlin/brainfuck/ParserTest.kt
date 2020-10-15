package brainfuck

import arrow.core.k
import arrow.core.toT
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ParserTest {
    @Test fun incPSucc() {
        assertEquals("" toT Inc(1), incP.parse("+"))
    }

    @Test fun incPUnsucc() {
        assertEquals("-" toT null, incP.parse("-"))
    }

    @Test fun simpleOpSucc() {
        assertEquals("+" toT Inc(-1), simpleOpP.parse("-+"))
    }

    @Test fun simpleLoop() {
        assertEquals("ab" toT Loop(listOf(Inc(1),Right(1), Right(-1))), loopOp.parse("[+><]ab"))
    }

    @Test fun nestedLoop() {
        assertEquals("ab" toT Loop(listOf(Inc(1),Loop(listOf(Right(1))), Right(-1))), loopOp.parse("[+[>]<]ab"))
    }

    @Test fun simpleOperations() {
        assertEquals("" toT listOf(Inc(1), Inc(-1), Right(1)), operations.parse("+->abc"))
    }

    @Test fun loopOperations() {
        assertEquals("" toT listOf(Loop(listOf(Inc(1))),Right(1)) , operations.parse("[+]>abc"))
    }

    @Test fun operationsLoop() {
        assertEquals("" toT listOf(Right(1), Out(1), Loop(listOf(Inc(1)))) , operations.parse(">.[+]abc"))
    }

    @Test fun parseProgram() {
        val innerLoop =  Loop(listOf(Inc(-1)))
        val loop =  Loop(listOf(Inc(1), Inp, Out(1), innerLoop, Right(-1), Right(-1)))
        val init = listOf(Inc(1), Right(1), Right(-1), Inc(-1))
        assertEquals(Program(init + listOf(loop)), parse("garbage   +><-[+,.[  garbage  -   more garbage]<<] garbage"))
    }
}
