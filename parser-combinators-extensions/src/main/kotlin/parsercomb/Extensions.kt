package parsecomb

import arrow.Kind
import arrow.core.*
import arrow.core.extensions.eq
import arrow.extension
import arrow.typeclasses.*
import parsecomb.parser.semigroupK.semigroupK


import parsecomb.types.*


@extension
interface ParserFunctor : Functor<ForParser> {
    override fun <A, B> ParserOf<A>.map(f: (A) -> B): Parser<B> = fix().fmap(f)
}

@extension
interface ParserApply : Apply<ForParser>, ParserFunctor {
    override fun <A, B> ParserOf<A>.ap(ff: Kind<ForParser, (A) -> B>): Parser<B> =
            Parser {
                val (a, rest) = this.fix().parse(it)
                if (a != null)
                    ff.fix().fmap { f -> f(a) }.parse(rest)
                else Pair(null, rest)
            }

    override fun <A, B> Kind<ForParser, A>.apEval(ff: Eval<Kind<ForParser, (A) -> B>>): Eval<Kind<ForParser, B>> = ff.flatMap {ffp ->
        Eval.later {Parser {
            val (a, rest) = this.fix().parse(it)
            if (a != null)
                ffp.fix().fmap {f -> f(a)}.parse(rest)
            else Pair(null, rest)
        }}
    }

}

@extension
interface ParserApplicative: Applicative<ForParser>, ParserApply {
    override fun <A> just(a: A): Parser<A> = Parser { Pair(a, it) }
    override fun <A, B> ParserOf<A>.map(f: (A) -> B): Parser<B> = fix().fmap(f)
}

@extension
interface ParserSelective: Selective<ForParser>, ParserApplicative {
    override fun <A, B> Kind<ForParser, Either<A, B>>.select(f: Kind<ForParser, (A) -> B>): Parser<B> {
        return Parser {
            val (ff, rest) = f.fix().parse(it)
            if (ff == null)
                Pair(null, rest)
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
    override fun <A> empty(): Parser<A> = Parser {Pair(null, it)}
}

@extension
interface ParserAlternative: Alternative<ForParser>, ParserApplicative, ParserMonoidK {
    override fun <A> Kind<ForParser, A>.orElse(b: Kind<ForParser, A>): Parser<A> =
            this.combineK(b)

    override fun <A> Kind<ForParser, A>.lazyOrElse(b: () -> Kind<ForParser, A>): Kind<ForParser, A> =
        Parser {
            val (a,rest) = this.fix().parse(it)
            if (a != null)
                Pair(a, rest)
            else
                b().fix().parse(rest)
        }


    override fun <A> Kind<ForParser, A>.combineK(y: Kind<ForParser, A>): Parser<A> = fix().combine(y.fix())

    override fun <A> Kind<ForParser, A>.some(): Kind<ForParser, SequenceK<A>> =
        Parser {
            val (a,rest) = this.fix().parse(it)
            if (a == null)
                Pair(null, rest)
            else
                mapN(just(a), this.many()) {(sequenceOf(it.a) + it.b).k()}.fix().parse(rest)
        }


    /*override fun <A> ParserOf<A>.lazyOrElse(b: () -> ParserOf<A>): Parser<A> {

    }*/
}
