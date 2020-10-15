package parsercomb

import arrow.Kind
import arrow.core.*
import arrow.extension
import arrow.typeclasses.*

import parsercomb.types.*


@extension
interface ParserFunctor : Functor<ForParser> {
    override fun <A, B> ParserOf<A>.map(f: (A) -> B): Parser<B> = fix().fmap(f)
}

@extension
interface ParserApply : Apply<ForParser>, ParserFunctor {
    override fun <A, B> ParserOf<A>.ap(ff: Kind<ForParser, (A) -> B>): Parser<B> =
            Parser {
                val (rest, a) = this.fix().parse(it)
                if (a != null)
                    ff.fix().fmap { f -> f(a) }.parse(rest)
                else rest toT null
            }
}

@extension
interface ParserApplicative: Applicative<ForParser>, ParserApply {
    override fun <A> just(a: A): Parser<A> = Parser { it toT a }
    override fun <A, B> ParserOf<A>.map(f: (A) -> B): Parser<B> = fix().fmap(f)
}

@extension
interface ParserSelective: Selective<ForParser>, ParserApplicative {
    override fun <A, B> Kind<ForParser, Either<A, B>>.select(f: Kind<ForParser, (A) -> B>): Parser<B> {
        return Parser {
            val (rest, ff) = f.fix().parse(it)
            if (ff == null)
                rest toT null
            else {
                fun toB(eab: Either<A,B>) : B = when (eab) {
                    is Either.Left -> ff(eab.a)
                    is Either.Right -> eab.b
                }
                this.fix().map(::toB).fix().parse(rest)
            }
        }
    }
}

@extension
interface ParserSemigroupK: SemigroupK<ForParser> {
    override fun <A> Kind<ForParser, A>.combineK(y: Kind<ForParser, A>): Parser<A> = fix().combine(y.fix())
}

@extension
interface ParserMonoidK: MonoidK<ForParser>, ParserSemigroupK {
    override fun <A> empty(): Parser<A> = Parser { it toT null}
}

@extension
interface ParserAlternative: Alternative<ForParser>, ParserApplicative, ParserMonoidK {
    override fun <A> Kind<ForParser, A>.orElse(b: Kind<ForParser, A>): Parser<A> =
            this.combineK(b)

    override fun <A> Kind<ForParser, A>.lazyOrElse(b: () -> Kind<ForParser, A>): Kind<ForParser, A> =
        Parser {
            val (rest, a) = this.fix().parse(it)
            if (a != null)
                rest toT a
            else
                b().fix().parse(rest)
        }


    override fun <A> Kind<ForParser, A>.combineK(y: Kind<ForParser, A>): Parser<A> = fix().combine(y.fix())

    override fun <A> Kind<ForParser, A>.some(): Kind<ForParser, SequenceK<A>> =
        Parser {
            val (rest, a) = this.fix().parse(it)
            if (a == null)
                rest toT null
            else
                mapN(just(a), this.many()) {(sequenceOf(it.a) + it.b).k()}.fix().parse(rest)
        }
}
