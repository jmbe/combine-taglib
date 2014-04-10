package se.intem.web.taglib.combined.node;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ParseResultTest {

    private ParseResult parsed;

    @Before
    public void setup() {
        this.parsed = new ParseResult();
    }

    @Test
    public void should_add_one_requires() {
        parsed.addComment("combine @requires angular");
        assertEquals("angular", parsed.getRequires().iterator().next());
    }

    @Test(expected = IllegalStateException.class)
    public void should_throw_exception_on_misspelled_tokens() {
        parsed.addComment("combine @incorrect angular");
        assertEquals("angular", parsed.getRequires().iterator().next());
    }

}
