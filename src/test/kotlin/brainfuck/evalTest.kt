package brainfuck

import arrow.Kind
import arrow.core.ForId
import arrow.mtl.State

import arrow.core.Id
import arrow.core.Tuple2
import arrow.core.extensions.id.comonad.extract
import arrow.core.extensions.id.monad.monad
import arrow.core.extensions.sequence.foldable.isEmpty

import arrow.core.fix
import arrow.mtl.ForStateT
import arrow.mtl.StateT
import arrow.mtl.extensions.fx
import arrow.mtl.run
import arrow.mtl.extensions.statet.monad.monad as stateTMonad
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


class EvalTest {

    val squares =
            "++++[>+++++<-]>[<+++++>-]+<+[\n" +
                    "    >[>+>+<<-]++>>[<<+>>-]>>>[-]++>[-]+\n" +
                    "    >>>+[[-]++++++>>>]<<<[[<++++++++<++>>-]+<.<[>----<-]<]\n" +
                    "    <<[>>>>>[>>>[-]+++++++++<[>-<-]+++++++++>[-[<->-]+[<<<]]<[>+<-]>]<<-]<<-\n" +
                    "]"

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
        val machine = Machine(arrayOf(25, 40), 0, StateMachine)
        val program: Program? = parse("[->+<]>.") // add 25 and 40, then print it

        assertNotNull(program)

        val e = eval(State().monad(), machine, program)
        val (memAfter) = e.run(emptyIo).extract()
        assertEquals(listOf(65.toByte()), memAfter.machineOut)
        assert(memAfter.machineIn.isEmpty())
        assertEquals(0, machine.memory[0])
        assertEquals(65, machine.memory[1])
        assertEquals(1, machine.pointer)

    }

    @Test
    fun testHelloWorld() {
        val emptyIo = MemIO(emptySequence(), emptyList())
        val machine = Machine(Array<Byte>(1024, {0} ), 0, StateMachine)
        val program: Program? = parse(helloWorld)

        assertNotNull(program)

        val e = eval(State().monad(), machine, program)
        val (memAfter) = e.run(emptyIo).extract()
        assertEquals("Hello World!\n", memAfter.printOut())
    }

    @Test
    fun testFibonacci() {
        val emptyIo = MemIO(emptySequence(), emptyList())
        val machine = Machine(Array<Byte>(1024) { 0 }, 0, StateMachine)
        val program: Program? = parse(fibonacci)

        assertNotNull(program)

        val e = eval(State().monad(), machine, program)
        val (memAfter) = e.run(emptyIo).extract()
        assertEquals("1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89", memAfter.printOut())
    }
}