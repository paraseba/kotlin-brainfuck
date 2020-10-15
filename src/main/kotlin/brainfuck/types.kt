package brainfuck

import arrow.Kind
import arrow.core.Id
import arrow.mtl.StatePartialOf
import arrow.mtl.State
import arrow.core.extensions.id.monad.monad
import arrow.mtl.extensions.fx


sealed class Op

data class Right(val n: Int) : Op()
data class Inc(val n: Int) : Op()
data class Out(val n: Int) : Op()
object Inp : Op()
data class Loop(val operations: List<Op>) : Op()

data class Program(val operations: List<Op>)

typealias MemOffset = Int

class Machine<M>(val memory: Array<Byte>, var pointer: MemOffset, val io: MachineIO<M>)

interface MachineIO<M> {
    fun putByte(b: Byte): Kind<M, Unit>
    fun getByte(): Kind<M, Byte?>
}

data class MemIO(val machineIn: Sequence<Byte>, val machineOut: List<Byte>) {
    fun printOut() : String = String(machineOut.toByteArray())
}

object StateMachine : MachineIO<StatePartialOf<MemIO>> {
    override fun putByte(b: Byte): State<MemIO, Unit> =
        State().modify { MemIO(it.machineIn, it.machineOut + b) }

    override fun getByte(): State<MemIO, Byte?> = State.fx(Id.monad()) {
        val mem = State().get<MemIO>().bind()
        State().set(MemIO(machineIn = mem.machineIn.drop(1), mem.machineOut)).bind()
        mem.machineIn.first() //fixme stdin closed
    }
}
