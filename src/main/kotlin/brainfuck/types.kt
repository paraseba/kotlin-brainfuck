package brainfuck

sealed class Op

data class Right(val n: Int) : Op()
data class Inc(val n: Int) : Op()
data class Out(val n: Int) : Op()
object Inp : Op()
data class Loop(val operations: List<Op>) : Op()

data class Program(val operations: List<Op>)

typealias MemOffset = Int

class Machine(val memory: Array<Byte>, val pointer: MemOffset)

