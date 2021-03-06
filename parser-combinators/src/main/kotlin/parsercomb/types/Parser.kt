package parsercomb.types

import arrow.core.Tuple2
import arrow.core.toT
import arrow.higherkind


@higherkind
data class Parser<A>(val parse: (String) -> Tuple2<String, A?>) : ParserOf<A> {
    companion object

    fun <B> convert(onFail: (String) -> Tuple2<String, B?>, onSuccess: (Tuple2<String, A>) -> Tuple2<String, B?> ): Parser<B> = Parser {
        val (rest, a) = parse(it)
        if (a == null) onFail(rest) else onSuccess(rest toT a)
    }

    fun combine(y:Parser<A>): Parser<A> =
            convert(onFail = {y.parse(it)}, onSuccess = {it} )

    fun <B> fmap(f: (A) -> B) : Parser <B> =
            convert(onFail = {it toT null}, onSuccess = { it.map(f) })
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

fun <T> satisfy(parser: Parser<T>, predicate: (T) -> Boolean) : Parser<T> =
        Parser { s ->
            val (rest, c) = parser.parse(s)
            if (c?.let {predicate(it)} == true) rest toT c else s toT null
            }
