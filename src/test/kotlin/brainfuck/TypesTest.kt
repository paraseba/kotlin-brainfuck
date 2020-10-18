package brainfuck

import arrow.core.Id
import arrow.core.extensions.id.monad.monad
import arrow.core.fix
import arrow.mtl.State
import arrow.mtl.extensions.fx
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe


class TypesTest : StringSpec({
    "update machine memory" {
        Machine(listOf(0), 0, StateMachine).update {(it+1).toByte()}.peek() shouldBe 1
    }

    "run State effect on machine" {
        val ops : State<MemIO, Byte?> = State.fx(Id.monad()) {
            val b1 = StateMachine.getByte().bind()
            val b2 = StateMachine.getByte().bind()
            StateMachine.putByte(Byte.MAX_VALUE).bind()
            b1?.plus(b2 ?: 0)?.toByte()
        }

        val empty = MemIO(generateSequence(0) {it.inc()} , emptyList())
        val (io, res) = ops.runF(empty).fix().extract()

        io.machineIn.first() shouldBe 2
        io.machineOut shouldBe listOf(127.toByte())
        res shouldBe 1
    }
})