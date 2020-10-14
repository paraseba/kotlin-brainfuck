package parsecomb.types

import arrow.*

@higherkind
data class Parser<A>(val parse: (String) -> Pair<A?, String>) : ParserOf<A> {
    companion object

    fun combine(y:Parser<A>): Parser<A> =
            Parser {
                val (a,rest) = this.parse(it)
                if (a != null)  Pair(a, rest) else y.parse(rest)
            }

    fun <B> fmap(f: (A) -> B) : Parser <B> =
    Parser {
        val (t, rest) = this.parse(it)
        if (t != null)
            Pair(f(t), rest)
        else
            Pair(t, rest)
    }
}

val charP : Parser<Char> = Parser {
    if (it.isNotEmpty())
        Pair(it[0], it.drop(1))
    else
        Pair(null, it)
}

fun charParser(c: Char) : Parser<Char> = satisfy(charP) {it == c}
fun noneOf(chars: Set<Char>) : Parser<Char> = satisfy(charP) {! chars.contains(it)}
val eof : Parser<Unit> = Parser {
    if (it.isEmpty())
        Pair(Unit, it)
    else
        Pair(null, it)
}

fun satisfy(parser: Parser<Char>, predicate: (Char) -> Boolean) : Parser<Char> =
        Parser { s ->
            val (c, rest) = parser.parse(s)
            if (c?.let {predicate(it)} == true) Pair(c, rest) else Pair(null, s)
            }
