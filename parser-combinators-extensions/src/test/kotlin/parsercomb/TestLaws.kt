package parsercomb

import arrow.core.test.laws.FunctorLaws
import parsecomb.types.Parser
import parsecomb.parser.functor.functor

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestLaws {
    @Test fun incPSucc() {
        assertEquals(1,1)
        //FunctorLaws.laws(Parser.functor(),)
    }
}
