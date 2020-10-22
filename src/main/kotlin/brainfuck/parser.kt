package brainfuck

import arrow.core.SequenceK
import parsercomb.parser.alternative.alt
import parsercomb.parser.alternative.lazyOrElse
import parsercomb.parser.alternative.many
import parsercomb.parser.alternative.some
import parsercomb.parser.apply.apTap
import parsercomb.parser.apply.followedBy
import parsercomb.parser.apply.mapN
import parsercomb.parser.functor.map
import parsercomb.types.Parser
import parsercomb.types.charParser
import parsercomb.types.eof
import parsercomb.types.noneOf


fun mkParser(c: Char, op: (Int) -> Op) : Parser<Op> = charParser(c).some().map { op(it.toList().size) }

val incP   = mkParser('+', ::Inc)
val decP   = mkParser('-') { Inc(it * -1) }
val leftP  = mkParser('<') { Right(it * -1) }
val rightP = mkParser('>', ::Right)
val inpP   = mkParser(',', ::Inp)
val outP   = mkParser('.', ::Out)
val simpleOpP : Parser<Op> = incP alt decP alt leftP alt rightP alt inpP alt outP

const val validChars = "+-<>,.[]"

val garbage: Parser<SequenceK<Char>> = noneOf(validChars.toSet()).many()


fun opP() : Parser<Op> =
        garbage
        .followedBy(simpleOpP.lazyOrElse { loopOp })
        .apTap(garbage)

val operations : Parser<List<Op>> = opP().some().map {it.toList()}

val loopOp : Parser<Op> = mapN(charParser('['), operations, charParser(']')) { Loop(it.b.toList()) }

val programP : Parser<Program> = operations.apTap(eof).map { Program(it.toList()) }

fun parse(program: String) : Program? = programP.parse(program).b