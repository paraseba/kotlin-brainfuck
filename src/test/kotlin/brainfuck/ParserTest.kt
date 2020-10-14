package brainfuck

import arrow.core.k
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ParserTest {
    @Test fun incPSucc() {
        assertEquals(Pair(Inc(1), ""), incP.parse("+"))
    }

    @Test fun incPUnsucc() {
        assertEquals(Pair(null, "-"), incP.parse("-"))
    }

    @Test fun simpleOpSucc() {
        assertEquals(Pair(Inc(-1), "+"), simpleOpP.parse("-+"))
    }

    @Test fun simpleLoop() {
        assertEquals(Pair(Loop(listOf(Inc(1),Right(1), Right(-1))), "ab"), loopOp.parse("[+><]ab"))
    }

    @Test fun nestedLoop() {
        assertEquals(Pair(Loop(listOf(Inc(1),Loop(listOf(Right(1))), Right(-1))), "ab"), loopOp.parse("[+[>]<]ab"))
    }

    @Test fun simpleOperations() {
        assertEquals(Pair(listOf(Inc(1), Inc(-1), Right(1)), ""), operations.parse("+->abc"))
    }

    @Test fun loopOperations() {
        assertEquals(Pair(listOf(Loop(listOf(Inc(1))),Right(1)),"") , operations.parse("[+]>abc"))
    }

    @Test fun operationsLoop() {
        assertEquals(Pair(listOf(Right(1), Out(1), Loop(listOf(Inc(1))),),"") , operations.parse(">.[+]abc"))
    }

    @Test fun parseProgram() {
        val innerLoop =  Loop(listOf(Inc(-1)))
        val loop =  Loop(listOf(Inc(1), Inp, Out(1), innerLoop, Right(-1), Right(-1)))
        val init = listOf(Inc(1), Right(1), Right(-1), Inc(-1))
        assertEquals(Program(init + listOf(loop)), parse("garbage   +><-[+,.[  garbage  -   more garbage]<<] garbage"))
    }
}
