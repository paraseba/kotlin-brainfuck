package brainfuck

import arrow.Kind
import arrow.core.extensions.list.foldable.foldM
import arrow.fx.IO
import arrow.fx.extensions.io.monad.monad
import arrow.fx.fix
import arrow.typeclasses.Monad
import java.io.File


fun <M> processOp(MO: Monad<M>, machine: Machine<M>, op: Op): Kind<M, Machine<M>> {
    return when (op) {
        is Inc -> MO.just(machine.update {(it + op.n).toByte() })
        is Right -> MO.just(machine.shift(op.n))
        is Out -> MO.run {
            machine.io.putByte(machine.peek()).mapConst(machine)
        }
        is Inp -> MO.run {
            machine.io.getByte().flatMap { b: Byte? ->
                just(if (b == null) machine else machine.poke(b))
            }
        }

        is Loop -> {
            fun go(machine: Machine<M>): Kind<M, Machine<M>>  = MO.run {
                if(machine.peek() == 0.toByte())
                    just(machine)
                else
                    eval(MO, machine, Program(op.operations)).flatMap {go(it)}
            }
            go(machine)
        }
    }
}

fun <M> eval(MO: Monad<M>, machine: Machine<M>, program: Program): Kind<M, Machine<M>> =
    program.operations.foldM(MO, machine, {mac, op -> processOp(MO, mac, op)})

fun evalFile(file: File, memory: Int = 1024): Unit {
    val program = parse(file.readText())
    val machine = Machine(List(memory) {0}, 0, IOMachine)

    if (program != null)
        eval(IO.monad(), machine, program).fix().unsafeRunAsync {
            it.fold(
                    {println("Error evaluating program: " + it.localizedMessage + "\n\n" + it.printStackTrace())},
                    {println("Done.")}
            )
        }
    else
        println("Can't parse program")

}