package brainfuck

import arrow.core.SequenceK
import parsercomb.parser.alternative.*
import parsercomb.parser.apply.apTap
import parsercomb.parser.apply.followedBy
import parsercomb.types.*
import parsercomb.parser.functor.mapConst
import parsercomb.parser.apply.mapN
import parsercomb.parser.functor.map


fun mkParser(c: Char, op: Op) : Parser<Op> = charParser(c).mapConst(op)

val incP   = mkParser('+', Inc(1))
val decP   = mkParser('-', Inc(-1))
val leftP  = mkParser('<', Right(-1))
val rightP = mkParser('>', Right(1))
val inpP   = mkParser(',', Inp)
val outP   = mkParser('.', Out(1))
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