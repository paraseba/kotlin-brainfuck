package parsercomb.types

import arrow.*
import arrow.core.Tuple2
import arrow.core.toT

@higherkind
data class Parser<A>(val parse: (String) -> Tuple2<String, A?>) : ParserOf<A> {
    companion object

    fun combine(y:Parser<A>): Parser<A> =
            Parser {
                val (rest, a) = this.parse(it)
                if (a != null) rest toT a else y.parse(rest)
            }

    fun <B> fmap(f: (A) -> B) : Parser <B> =
    Parser {
        this.parse(it).map {it?.let {f(it)} }
    }
}

val charP : Parser<Char> = Parser {
    if (it.isNotEmpty())
        it.drop(1) toT it[0]
    else
        it toT null
}

fun charParser(c: Char) : Parser<Char> = satisfy(charP) {it == c}

fun noneOf(chars: Set<Char>) : Parser<Char> = satisfy(charP) {! chars.contains(it)}

val eof : Parser<Unit> = Parser {
    if (it.isEmpty())
        it toT Unit
    else
        it toT null
}

fun satisfy(parser: Parser<Char>, predicate: (Char) -> Boolean) : Parser<Char> =
        Parser { s ->
            val (rest, c) = parser.parse(s)
            if (c?.let {predicate(it)} == true) rest toT c else s toT null
            }
