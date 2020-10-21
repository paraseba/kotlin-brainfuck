package brainfuck

import arrow.core.extensions.id.comonad.extract
import arrow.fx.IO
import arrow.fx.extensions.io.monad.monad
import arrow.fx.fix
import arrow.mtl.State
import arrow.mtl.run
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.sequences.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList


class EvalTest : StringSpec({

    val emptyMemory = List(1024) {0.toByte()}.toPersistentList()

    val helloWorld = "++++++++[>++++[>++>+++>+++>+<<<<-]>+>+>->>+[<]<-]>>.>---.+++++++..+++.>>.<-.<.+++.------.--------.>>+.>++."
    val fibonacci = "+++++++++++\n" +
            ">+>>>>++++++++++++++++++++++++++++++++++++++++++++\n" +
            ">++++++++++++++++++++++++++++++++<<<<<<[>[>>>>>>+>\n" +
            "+<<<<<<<-]>>>>>>>[<<<<<<<+>>>>>>>-]<[>++++++++++[-\n" +
            "<-[>>+>+<<<-]>>>[<<<+>>>-]+<[>[-]<[-]]>[<<[>>>+<<<\n" +
            "-]>>[-]]<<]>>>[>>+>+<<<-]>>>[<<<+>>>-]+<[>[-]<[-]]\n" +
            ">[<<+>>[-]]<<<<<<<]>>>>>[+++++++++++++++++++++++++\n" +
            "+++++++++++++++++++++++.[-]]++++++++++<[->-<]>++++\n" +
            "++++++++++++++++++++++++++++++++++++++++++++.[-]<<\n" +
            "<<<<<<<<<<[>>>+>+<<<<-]>>>>[<<<<+>>>>-]<-[>>.>.<<<\n" +
            "[-]]<<[>>+>+<<<-]>>>[<<<+>>>-]<<[<+>-]>[<+>-]<<<-]"

    val head = "+>>>>>>>>>>-[,+[-.----------[[-]>]<->]<]" // taken from http://www.hevanet.com/cristofd/brainfuck/head.b

    "add two numbers in State" {
        val emptyIo = MemIO(emptySequence(), emptyList())
        val machine = Machine(persistentListOf(25, 40), 0, StateMachine)
        val program: Program? = parse("[->+<]>.") // add 25 and 40, then print it

        program.shouldNotBeNull()

        val e = State().monad<MemIO>().eval(machine, program)
        val (ioAfter, machineAfter) = e.run(emptyIo).extract()
        ioAfter.machineOut  shouldBe listOf(65.toByte())
        ioAfter.machineIn.shouldBeEmpty()
        machineAfter.memory shouldBe listOf(0,65).map {it.toByte()}
        machineAfter.pointer shouldBe 1
    }

    "add two numbers in IO" {
        val machine = Machine(persistentListOf(25, 40), 0, IOMachine.std)
        val program: Program? = parse("[->+<]>.") // add 25 and 40, then print it

        program.shouldNotBeNull()

        val e = IO.monad().eval(machine, program)
        val machineAfter = e.fix().unsafeRunSync()
        machineAfter.memory shouldBe listOf(0,65).map {it.toByte()}
        machineAfter.pointer shouldBe 1
    }

    "multiply two numbers in State" {
        val a: Byte = 10
        val b: Byte = 11
        val emptyIo = MemIO(emptySequence(), emptyList())
        val machine = Machine(persistentListOf(a, b, 0, 0), 0, StateMachine)
        // taken from https://www.codingame.com/playgrounds/50426/getting-started-with-brainfuck/multiplication
        val program: Program? = parse("[>[->+>+<<]>[-<+>]<<-]")

        program.shouldNotBeNull()

        val e = State().monad<MemIO>().eval(machine, program)
        val (_, machineAfter) = e.run(emptyIo).extract()
        machineAfter.memory shouldBe listOf(0,b,0,a*b).map {it.toByte()}
    }

    "multiply two numbers in IO" {
        val a: Byte = 10
        val b: Byte = 11
        val machine = Machine(persistentListOf(a, b, 0, 0), 0, IOMachine.std)
        // taken from https://www.codingame.com/playgrounds/50426/getting-started-with-brainfuck/multiplication
        val program: Program? = parse("[>[->+>+<<]>[-<+>]<<-]")

        program.shouldNotBeNull()

        val machineAfter = IO.monad().eval(machine, program).fix().unsafeRunSync()
        println(machineAfter.memory.toList())
        machineAfter.memory shouldBe listOf(0,b,0,a*b).map {it.toByte()}
    }



    "print Hello World in State" {
        val emptyIo = MemIO(emptySequence(), emptyList())
        val machine = Machine(emptyMemory, 0, StateMachine)
        val program: Program? = parse(helloWorld)

        program.shouldNotBeNull()

        val e = State().monad<MemIO>().eval(machine, program)
        val (ioAfter) = e.run(emptyIo).extract()
        ioAfter.printOut() shouldBe "Hello World!\n"
    }

    "print Hello World in IO" {
        val machine = Machine(emptyMemory, 0, IOMachine.std)
        val program: Program? = parse(helloWorld)

        program.shouldNotBeNull()

        val e = IO.monad().eval(machine, program)
        e.fix().unsafeRunSync()
    }

    "print fibonacci in State" {
        val emptyIo = MemIO(emptySequence(), emptyList())
        val machine = Machine(emptyMemory, 0, StateMachine)
        val program: Program? = parse(fibonacci)

        program.shouldNotBeNull()

        val e = State().monad<MemIO>().eval(machine, program)
        val (ioAfter) = e.run(emptyIo).extract()
        ioAfter.printOut() shouldBe "1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89"
    }

    "print fibonacci in IO" {
        val machine = Machine(emptyMemory, 0, IOMachine.std)
        val program: Program? = parse(fibonacci)

        program.shouldNotBeNull()
        IO.monad().eval(machine, program).fix().unsafeRunSync()
    }

    "print 10 lines of input in State" {
        val input = (1..20).joinToString(separator = "\n").toByteArray().asSequence()
        val emptyIo = MemIO(input, emptyList())
        val machine = Machine(emptyMemory, 0, StateMachine)
        val program: Program? = parse(head)

        program.shouldNotBeNull()

        val e = State().monad<MemIO>().eval(machine, program)
        val (ioAfter) = e.run(emptyIo).extract()
        ioAfter.printOut() shouldBe (1..10).joinToString(separator = "\n", postfix = "\n")
    }

    "print 10 lines of input in IO" {
        val input = (1..20).joinToString(separator = "\n").toByteArray().toTypedArray()
        val (iomac, output) = IOMachine.memory(input)
        val machine = Machine(emptyMemory, 0, iomac)
        val program: Program? = parse(head)

        program.shouldNotBeNull()

        IO.monad().eval(machine, program).fix().unsafeRunSync()
        val ioAfter = output.toString()
        ioAfter shouldBe (1..10).joinToString(separator = "\n", postfix = "\n")
    }
})