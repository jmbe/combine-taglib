package se.intem.web.taglib.combined.node;

import static java.util.Arrays.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class CombineCommentParserTest {

    private InputStream singleline;
    private CombineCommentParser parser;
    private InputStream multiline;
    private InputStream other;
    private InputStream multiple;
    private InputStream bug1;

    @Before
    public void setup() {
        this.singleline = this.getClass().getResourceAsStream("/singleline-dependencies.js");
        this.multiline = this.getClass().getResourceAsStream("/multiline-dependencies.js");
        this.multiple = this.getClass().getResourceAsStream("/multiple-dependencies.js");
        this.other = this.getClass().getResourceAsStream("/combine.json");
        this.bug1 = this.getClass().getResourceAsStream("/bug1.js");
        this.parser = new CombineCommentParser();
    }

    @Test
    public void test_resources_should_exist() {
        assertNotNull(singleline);
        assertNotNull(multiline);
        assertNotNull(multiple);
        assertNotNull(bug1);
        assertNotNull(other);
    }

    @Test
    public void should_find_single_combine_comment() throws IOException {
        ParseResult parsed = parser.findCombineComment(singleline);
        assertThat(parsed.getRequiresList(), is(Arrays.asList("extjs", "angularjs")));
    }

    @Test
    public void should_find_multi_combine_comment() throws IOException {
        List<String> requires = parser.findCombineComment(multiline).getRequiresList();
        assertThat(requires, is(asList("extjs", "angularjs", "jquery")));
    }

    @Test
    public void should_find_singleline_requires() throws IOException {
        List<String> requires = parser.parse(singleline).getRequiresList();
        assertEquals(2, requires.size());
        assertThat(requires, is(Arrays.asList("extjs", "angularjs")));

    }

    @Test
    public void should_find_multiline_requires() throws IOException {
        List<String> requires = parser.parse(multiline).getRequiresList();
        assertEquals(3, requires.size());
        assertThat(requires, is(Arrays.asList("extjs", "angularjs", "jquery")));
    }

    @Test
    public void should_parse_bug1() throws IOException {
        List<String> requires = parser.parse(bug1).getRequiresList();
        assertEquals(4, requires.size());
        assertThat(requires, is(Arrays.asList("a", "b", "c", "d")));
    }

    @Test
    public void should_find_multiple_comments() throws IOException {
        ParseResult parsed = parser.parse(multiple);
        List<String> requires = parsed.getRequiresList();
        assertEquals(3, requires.size());
        assertThat(requires, is(Arrays.asList("extjs", "jquery", "angularjs")));
    }

    @Test
    public void should_return_empty_list_if_no_requires() throws IOException {
        List<String> requires = parser.parse(other).getRequiresList();
        assertEquals(0, requires.size());
    }
}
