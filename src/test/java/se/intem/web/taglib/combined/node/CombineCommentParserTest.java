package se.intem.web.taglib.combined.node;

import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

import static java.util.Arrays.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class CombineCommentParserTest {

    private InputStream singleline;
    private CombineCommentParser parser;
    private InputStream prefix;
    private InputStream other;
    private InputStream multiple;
    private InputStream multiplePerLine;
    private InputStream nochanges;
    private InputStream bugrx;
    private InputStream bugtrailing;
    private InputStream withinstring;
    private InputStream exclamation;

    @Before
    public void setup() {
        this.singleline = this.getClass().getResourceAsStream("/singleline-dependencies.js");
        this.prefix = this.getClass().getResourceAsStream("/prefix-dependencies.js");
        this.multiple = this.getClass().getResourceAsStream("/multiple-dependencies.js");
        this.multiplePerLine = this.getClass().getResourceAsStream("/multiple-dependencies-per-line.js");
        this.nochanges = this.getClass().getResourceAsStream("/nochanges.js");
        this.other = this.getClass().getResourceAsStream("/combine-test.json");
        this.bugrx = this.getClass().getResourceAsStream("/bug-rx.js");
        this.bugtrailing = this.getClass().getResourceAsStream("/trailing-dependencies.js");
        this.withinstring = this.getClass().getResourceAsStream("/comment-start-within-string.js");
        this.exclamation = this.getClass().getResourceAsStream("/exclamation-comment.js");
        this.parser = new CombineCommentParser();
    }

    @Test
    public void test_resources_should_exist() {
        assertNotNull(singleline);
        assertNotNull(prefix);
        assertNotNull(multiple);
        assertNotNull(multiplePerLine);
        assertNotNull(nochanges);
        assertNotNull(other);
        assertNotNull(bugtrailing);
        assertNotNull(withinstring);
        assertNotNull(exclamation);
    }

    @Test
    public void should_find_single_combine_comment() throws IOException {
        ParseResult parsed = parser.parse(singleline);
        assertThat(parsed.getRequiresList(), is(Arrays.asList("extjs", "angularjs")));
        assertEquals("var code;", parsed.getContents().trim());
    }

    @Test
    public void should_support_code_on_same_line() throws IOException {
        ParseResult parsed = parser.parse(prefix);
        List<String> requires = parsed.getRequiresList();
        assertThat(requires, is(asList("extjs", "angularjs", "jquery")));
        assertEquals("/* unrelated */" + System.lineSeparator() + "var code;", parsed.getContents().trim());
    }

    @Test
    public void should_find_singleline_requires() throws IOException {
        List<String> requires = parser.parse(singleline).getRequiresList();
        assertEquals(2, requires.size());
        assertThat(requires, is(Arrays.asList("extjs", "angularjs")));

    }

    @Test
    public void should_find_multiple_comments() throws IOException {
        ParseResult parsed = parser.parse(multiple);
        List<String> requires = parsed.getRequiresList();
        assertEquals(3, requires.size());
        assertThat(requires, is(Arrays.asList("extjs", "jquery", "angularjs")));
    }

    @Test
    public void should_find_multiple_comments_per_line() throws IOException {
        ParseResult parsed = parser.parse(multiplePerLine);
        List<String> requires = parsed.getRequiresList();
        assertThat(requires, is(Arrays.asList("extjs", "jquery", "angularjs")));
    }

    @Test
    public void should_return_empty_list_if_no_requires() throws IOException {
        List<String> requires = parser.parse(other).getRequiresList();
        assertEquals(0, requires.size());
    }

    @Test
    public void should_find_multiple_requires_in_comment() throws IOException {
        String string = "/* combine @requires extjs @requires angularjs */";
        List<String> requires = parser.parse(string).getRequiresList();
        assertThat(requires, is(Arrays.asList("extjs", "angularjs")));
    }

    @Test
    public void should_find_multiple_requires_and_provides_in_comment() throws IOException {
        String string = "/* combine @requires extjs angularjs @provides tool1 tool2 @requires jquery @provides tool3 */";
        ParseResult result = parser.parse(string);
        List<String> requires = result.getRequiresList();
        assertThat(requires, is(Arrays.asList("extjs", "angularjs", "jquery")));

        List<String> provides = result.getProvidesList();
        assertThat(provides, is(Arrays.asList("tool1", "tool2", "tool3")));
    }

    @Test
    public void should_not_change_js_structure() throws IOException {
        String content = CharStreams.toString(new InputStreamReader(nochanges));

        ParseResult parsed = parser.parse(content);
        assertEquals(content, parsed.getContents());
    }

    @Test
    public void bug_parse_rxjs() throws IOException {
        String content = CharStreams.toString(new InputStreamReader(this.bugrx));

        ParseResult parsed = parser.parse(content);
        assertEquals(content, parsed.getContents());
    }

    @Test
    public void should_find_trailing_comments() throws IOException {
        ParseResult parsed = parser.parse(this.bugtrailing);
        List<String> requires = parsed.getRequiresList();
        assertThat(requires, is(Arrays.asList("radio", "yui3")));
    }

    @Test
    public void comment_start_within_string() throws IOException {
        String content = CharStreams.toString(new InputStreamReader(withinstring));

        ParseResult parsed = parser.parse(content);
        assertEquals(content, parsed.getContents());
    }

    @Test
    public void should_support_exclamation_in_comment_start() throws IOException {
        ParseResult parsed = parser.parse(this.exclamation);
        List<String> requires = parsed.getRequiresList();
        assertThat(requires, is(Arrays.asList("angularjs", "jquery")));
    }
}
