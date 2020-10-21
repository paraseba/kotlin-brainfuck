package brainfuck

import arrow.Kind
import arrow.core.extensions.list.foldable.foldM
import arrow.fx.IO
import arrow.fx.extensions.io.monad.monad
import arrow.fx.fix
import arrow.typeclasses.Monad
import kotlinx.collections.immutable.toPersistentList
import java.io.File


fun <M> Monad<M>.processOp(machine: Machine<M>, op: Op): Kind<M, Machine<M>> {
    return when (op) {
        is Inc -> just(machine.update {(it + op.n).toByte() })
        is Right -> just(machine.shift(op.n))
        is Out -> run {
            machine.io.putByte(machine.peek()).mapConst(machine)
        }
        is Inp -> run {
            machine.io.getByte().flatMap { b: Byte? ->
                just(if (b == null) machine else machine.poke(b))
            }
        }

        is Loop -> {
            fun go(machine: Machine<M>): Kind<M, Machine<M>>  = run {
                if(machine.peek() == 0.toByte())
                    just(machine)
                else
                    eval(machine, Program(op.operations)).flatMap {go(it)}
            }
            go(machine)
        }
    }
}

fun <M> Monad<M>.eval(machine: Machine<M>, program: Program): Kind<M, Machine<M>> =
    program.operations.foldM(this, machine, {mac, op -> processOp(mac, op)})

fun evalFile(file: File, memory: Int = 1024*1024): Unit {
    val program = parse(file.readText())
    val machine = Machine(List(memory) {0.toByte()}.toPersistentList(), 0, IOMachine.std)

    if (program != null)
        IO.monad().eval(machine, program).fix().unsafeRunAsync {
            it.fold(
                    {println("Error evaluating program: " + it.localizedMessage + "\n\n" + it.printStackTrace())},
                    {println("Done.")}
            )
        }
    else
        println("Can't parse program")
}