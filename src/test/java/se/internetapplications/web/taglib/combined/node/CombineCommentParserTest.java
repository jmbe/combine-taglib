package se.internetapplications.web.taglib.combined.node;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class CombineCommentParserTest {

    private InputStream single;
    private CombineCommentParser parser;
    private InputStream multi;
    private InputStream other;

    @Before
    public void setup() {
        this.single = this.getClass().getResourceAsStream("/singleline-dependencies.js");
        this.multi = this.getClass().getResourceAsStream("/multiline-dependencies.js");
        this.other = this.getClass().getResourceAsStream("/combine.json");
        this.parser = new CombineCommentParser();
    }

    @Test
    public void test_resources_should_exist() {
        assertNotNull(single);
        assertNotNull(multi);
        assertNotNull(other);
    }

    @Test
    public void should_find_single_combine_comment() throws IOException {
        String requires = parser.findCombineComment(single);
        assertEquals("combine @requires extjs angularjs", requires);
    }

    @Test
    public void should_find_multi_combine_comment() throws IOException {
        String requires = parser.findCombineComment(multi);
        assertEquals("combine @requires extjs angularjs jquery", requires);
    }

    @Test
    public void should_find_singleline_requires() throws IOException {
        List<String> requires = parser.findRequires(single);
        assertEquals(2, requires.size());
        assertThat(requires, is(Arrays.asList("extjs", "angularjs")));

    }

    @Test
    public void should_find_multiline_requires() throws IOException {
        List<String> requires = parser.findRequires(multi);
        assertEquals(3, requires.size());
        assertThat(requires, is(Arrays.asList("extjs", "angularjs", "jquery")));
    }

    @Test
    public void should_return_empty_list_if_no_requires() throws IOException {
        List<String> requires = parser.findRequires(other);
        assertEquals(0, requires.size());
    }
}
