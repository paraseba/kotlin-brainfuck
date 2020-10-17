package brainfuck

import arrow.Kind
import arrow.core.ForId
import arrow.mtl.State

import arrow.core.Id
import arrow.core.Tuple2
import arrow.core.extensions.id.comonad.extract
import arrow.core.extensions.id.monad.monad
import arrow.core.extensions.list.foldable.traverse_
import arrow.core.extensions.sequence.foldable.isEmpty

import arrow.core.fix
import arrow.fx.IO
import arrow.fx.extensions.fx
import arrow.fx.extensions.io.monad.monad
import arrow.fx.fix
import arrow.mtl.ForStateT
import arrow.mtl.StateT
import arrow.mtl.extensions.fx
import arrow.mtl.run
import arrow.mtl.extensions.statet.monad.monad as stateTMonad
import org.junit.Test
import kotlin.system.measureTimeMillis
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


class EvalTest {

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

    @Test
    fun testAddition() {
        val emptyIo = MemIO(emptySequence(), emptyList())
        val machine = Machine(listOf(25, 40), 0, StateMachine)
        val program: Program? = parse("[->+<]>.") // add 25 and 40, then print it

        assertNotNull(program)

        val e = eval(State().monad(), machine, program)
        val (ioAfter, machineAfter) = e.run(emptyIo).extract()
        assertEquals(listOf(65.toByte()), ioAfter.machineOut)
        assert(ioAfter.machineIn.isEmpty())
        assertEquals(0, machineAfter.memory[0])
        assertEquals(65, machineAfter.memory[1])
        assertEquals(1, machineAfter.pointer)
    }

    @Test
    fun testAdditionIO() {
        val machine = Machine(listOf(25, 40), 0, IOMachine)
        val program: Program? = parse("[->+<]>.") // add 25 and 40, then print it

        assertNotNull(program)

        val e = eval(IO.monad(), machine, program)
        val machineAfter = e.fix().unsafeRunSync()
        assertEquals(0, machineAfter.memory[0])
        assertEquals(65, machineAfter.memory[1])
        assertEquals(1, machineAfter.pointer)
    }

    @Test
    fun testMultiplication() {
        val a: Byte = 10
        val b: Byte = 11
        val emptyIo = MemIO(emptySequence(), emptyList())
        val machine = Machine(listOf(a, b, 0, 0), 0, StateMachine)
        // taken from https://www.codingame.com/playgrounds/50426/getting-started-with-brainfuck/multiplication
        val program: Program? = parse("[>[->+>+<<]>[-<+>]<<-]")

        assertNotNull(program)

        val e = eval(State().monad(), machine, program)
        val (_, machineAfter) = e.run(emptyIo).extract()
        assertEquals((a*b).toByte(), machineAfter.memory[3])
    }

    @Test
    fun testMultiplicationIO() {
        val a: Byte = 10
        val b: Byte = 11
        val machine = Machine(listOf(a, b, 0, 0), 0, IOMachine)
        // taken from https://www.codingame.com/playgrounds/50426/getting-started-with-brainfuck/multiplication
        val program: Program? = parse("[>[->+>+<<]>[-<+>]<<-]")

        assertNotNull(program)

        val machineAfter = eval(IO.monad(), machine, program).fix().unsafeRunSync()
        println(machineAfter.memory.toList())
        assertEquals((a*b).toByte(), machineAfter.memory[3])
    }



    @Test
    fun testHelloWorld() {
        val emptyIo = MemIO(emptySequence(), emptyList())
        val machine = Machine(List(1024) {0}, 0, StateMachine)
        val program: Program? = parse(helloWorld)

        assertNotNull(program)

        val e = eval(State().monad(), machine, program)
        val (ioAfter) = e.run(emptyIo).extract()
        assertEquals("Hello World!\n", ioAfter.printOut())
    }

    @Test
    fun testHelloWorldIO() {
        val machine = Machine(List(1024) {0}, 0, IOMachine)
        val program: Program? = parse(helloWorld)

        assertNotNull(program)

        val e = eval(IO.monad(), machine, program)
        e.fix().unsafeRunSync()
    }


    @Test
    fun testFibonacci() {
        val emptyIo = MemIO(emptySequence(), emptyList())
        val machine = Machine(List(1024) { 0 }, 0, StateMachine)
        val program: Program? = parse(fibonacci)

        assertNotNull(program)

        val e = eval(State().monad(), machine, program)
        val (ioAfter) = e.run(emptyIo).extract()
        assertEquals("1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89", ioAfter.printOut())
    }

    @Test
    fun testFibonacciIO() {
        val machine = Machine(List(1024) { 0 }, 0, IOMachine)
        val program: Program? = parse(fibonacci)

        assertNotNull(program)

        eval(IO.monad(), machine, program).fix().unsafeRunSync()
    }


    @Test
    fun machineIOTest() {
        val res = IO.fx {
            !IOMachine.putByte(65)
            !IOMachine.putByte(66)
            !IOMachine.putByte(32)
            42
        }.unsafeRunSync()
        assertEquals(42, res)
    }
}