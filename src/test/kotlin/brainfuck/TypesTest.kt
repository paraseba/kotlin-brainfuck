package brainfuck

import arrow.core.Id
import arrow.core.extensions.id.monad.monad
import arrow.core.fix
import arrow.mtl.State
import arrow.mtl.extensions.fx
import org.junit.Test
import kotlin.test.assertEquals


class TypesTest {
    @Test
    fun machineUpdate() {
        assertEquals(1, Machine(listOf(0), 0, StateMachine).update {(it+1).toByte()}.peek())
    }

    @Test
    fun testStateMachine() {
        val ops : State<MemIO, Byte?> = State.fx(Id.monad()) {
            val b1 = StateMachine.getByte().bind()
            val b2 = StateMachine.getByte().bind()
            StateMachine.putByte(Byte.MAX_VALUE).bind()
            b1?.plus(b2 ?: 0)?.toByte()
        }

        fun inc(b: Byte) : Byte = (b + 1).toByte()
        val empty = MemIO(generateSequence(0) {it.inc()} , emptyList())
        val (mem, res) = ops.runF(empty).fix().extract()

        assertEquals(2, mem.machineIn.first())
        assertEquals(1, mem.machineOut.size)
        assertEquals(127, mem.machineOut[0])
        assertEquals(1, res)
    }

}