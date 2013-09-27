package se.intem.web.taglib.combined.node;

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

    @Before
    public void setup() {
        this.singleline = this.getClass().getResourceAsStream("/singleline-dependencies.js");
        this.multiline = this.getClass().getResourceAsStream("/multiline-dependencies.js");
        this.multiple = this.getClass().getResourceAsStream("/multiple-dependencies.js");
        this.other = this.getClass().getResourceAsStream("/combine.json");
        this.parser = new CombineCommentParser();
    }

    @Test
    public void test_resources_should_exist() {
        assertNotNull(singleline);
        assertNotNull(multiline);
        assertNotNull(multiple);
        assertNotNull(other);
    }

    @Test
    public void should_find_single_combine_comment() throws IOException {
        String requires = parser.findCombineComment(singleline).get(0);
        assertEquals("combine @requires extjs angularjs", requires);
    }

    @Test
    public void should_find_multi_combine_comment() throws IOException {
        String requires = parser.findCombineComment(multiline).get(0);
        assertEquals("combine @requires extjs angularjs jquery", requires);
    }

    @Test
    public void should_find_singleline_requires() throws IOException {
        List<String> requires = parser.findRequires(singleline);
        assertEquals(2, requires.size());
        assertThat(requires, is(Arrays.asList("extjs", "angularjs")));

    }

    @Test
    public void should_find_multiline_requires() throws IOException {
        List<String> requires = parser.findRequires(multiline);
        assertEquals(3, requires.size());
        assertThat(requires, is(Arrays.asList("extjs", "angularjs", "jquery")));
    }

    @Test
    public void should_find_multiple_comments() throws IOException {
        List<String> requires = parser.findRequires(multiple);
        assertEquals(3, requires.size());
        assertThat(requires, is(Arrays.asList("extjs", "jquery", "angularjs")));
    }

    @Test
    public void should_return_empty_list_if_no_requires() throws IOException {
        List<String> requires = parser.findRequires(other);
        assertEquals(0, requires.size());
    }
}
