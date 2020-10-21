package brainfuck

import arrow.Kind
import arrow.core.Id
import arrow.core.Tuple2
import arrow.core.extensions.id.monad.monad
import arrow.core.toT
import arrow.fx.ForIO
import arrow.fx.IO
import arrow.mtl.State
import arrow.mtl.StatePartialOf
import arrow.mtl.extensions.fx
import kotlinx.collections.immutable.PersistentList
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream


sealed class Op

data class Right(val n: Int) : Op()
data class Inc(val n: Int) : Op()
data class Out(val n: Int) : Op()
object Inp : Op()
data class Loop(val operations: List<Op>) : Op()

data class Program(val operations: List<Op>)

typealias MemOffset = Int

data class Machine<M>(val memory: PersistentList<Byte>, val pointer: MemOffset, val io: MachineIO<M>) {

    fun update(f : (Byte) -> Byte): Machine<M> =
            copy(memory = memory.set(pointer, f(peek())))

    fun shift(offset: MemOffset) : Machine<M> = copy(pointer = pointer + offset)

    fun peek(): Byte = memory[pointer]
    fun poke(value: Byte) : Machine<M> = update {value}
}

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
        val mem = !State().get<MemIO>()
        !State().set(MemIO(machineIn = mem.machineIn.drop(1), mem.machineOut))
        mem.machineIn.first() //fixme stdin closed
    }
}

class IOMachine(val inStream: InputStream, val outStream: OutputStream) : MachineIO<ForIO> {
    companion object {
        val std = IOMachine(System.`in`, System.`out`)

        fun memory(inputArray: Array<Byte>) : Tuple2<IOMachine, ByteArrayOutputStream> {
            val output = ByteArrayOutputStream()
            val input = ByteArrayInputStream(inputArray.toByteArray())
            return IOMachine(input, output) toT output
        }
    }

    override fun putByte(b: Byte): IO<Unit> = IO  { outStream.write(b.toInt()) }

    override fun getByte(): IO<Byte?> = IO {
        val b = inStream.read()
        if (b == -1)
            null
        else
            b.toByte()
    }
}
