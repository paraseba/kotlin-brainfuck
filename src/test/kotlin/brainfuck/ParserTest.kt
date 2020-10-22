package brainfuck

import arrow.core.toT
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ParserTest : StringSpec({

    "incP parses + to Inc(1)" {
        incP.parse("+") shouldBe ("" toT Inc(1))
    }

    "incP can't parse -" {
        incP.parse("-") shouldBe ("-" toT null)
    }

    "simpleOpP parses -+" {
        simpleOpP.parse("-+") shouldBe ("+" toT Inc(-1))
    }

    "loop can parse with residual" {
        loopOp.parse("[+><]ab") shouldBe ("ab" toT Loop(listOf(Inc(1), Right(1), Right(-1))))
    }

    "loopOp can parse nested loops" {
        loopOp.parse("[+[>]<]ab") shouldBe ("ab" toT Loop(listOf(Inc(1), Loop(listOf(Right(1))), Right(-1))))
    }

    "operations parses multiple simple ones" {
        operations.parse("+->abc") shouldBe ("" toT listOf(Inc(1), Inc(-1), Right(1)))
    }

    "operations parses multiplicities" {
        operations.parse("+++---...,,,>>><<<") shouldBe ("" toT listOf(Inc(3), Inc(-3), Out(3), Inp(3), Right(3), Right(-3)))
    }


    "operations parses loops" {
        operations.parse(">.[+]abc") shouldBe ("" toT listOf(Right(1), Out(1), Loop(listOf(Inc(1)))))
    }

    "full parser skips garbage" {
        val innerLoop = Loop(listOf(Inc(-1)))
        val loop = Loop(listOf(Inc(1), Inp(1), Out(1), innerLoop, Right(-2)))
        val init = listOf(Inc(1), Right(1), Right(-1), Inc(-1))
        parse("garbage   +><-[+,.[  garbage  -   more garbage]<<] garbage") shouldBe Program(init + listOf(loop))
    }
})
