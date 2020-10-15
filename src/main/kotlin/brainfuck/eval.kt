package brainfuck

import arrow.Kind
import arrow.core.extensions.list.foldable.traverse_
import arrow.typeclasses.Monad



fun <M> processOp(MO: Monad<M>, machine: brainfuck.Machine<M>, op: Op): Kind<M, Unit> =
    when (op) {
        is Inc -> {
            machine.memory[machine.pointer] = (machine.memory[machine.pointer] + op.n).toByte()
            MO.just(Unit)
        }

        is Right -> {
            machine.pointer += op.n
            MO.just(Unit)
        }

        is Out -> {
            machine.io.putByte(machine.memory[machine.pointer])
        }

        is Inp -> {
            MO.run {
                machine.io.getByte().flatMap { b: Byte? ->
                    b?.let { machine.memory[machine.pointer] = it }
                    MO.just(Unit)
                }
            }
        }
        is Loop -> {
            fun go (ops: List<Op>) : Kind<M, Unit> =
                if (machine.memory[machine.pointer] != 0.toByte())
                    MO.run {
                        op.operations.traverse_(MO) {processOp(MO, machine, it)}.followedBy(go(ops))
                    }
                else
                    MO.just(Unit)

            go(op.operations)
        }
    }


fun <M> eval(MO: Monad<M>, machine: Machine<M>, program: Program): Kind<M, Unit> =
    program.operations.traverse_(MO) { processOp(MO, machine, it)}
