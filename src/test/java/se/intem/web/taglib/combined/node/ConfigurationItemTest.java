package se.intem.web.taglib.combined.node;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

public class ConfigurationItemTest {

    private ConfigurationItem ci;

    @Before
    public void setup() {
        this.ci = new ConfigurationItem();
    }

    @Test
    public void should_not_add_requires_for_empty_string() {

        assertEquals(0, ci.getRequiresList().size());
        ci.addRequires("");
        assertEquals(0, ci.getRequiresList().size());
    }

    @Test
    public void should_split_strings_even_when_setting_collection() {
        ci.setRequires(Arrays.asList("angular, jquery-ui"));
        assertEquals(2, ci.getRequiresList().size());
    }

    @Test
    public void should_split_on_whitespace_and_comma() {
        ci.setRequires(Arrays.asList("angular, jquery-ui extjs angular-ui"));
        assertEquals(4, ci.getRequiresList().size());
    }

    @Test
    public void should_remove_duplicate_requires_and_keep_insertion_order() {
        ci.addRequires("jquery, angular-ui,jquery");

        assertEquals("Should remove duplicates", 2, ci.getRequiresList().size());
        assertThat("Should keep insertion order", ci.getRequiresList(), is(Arrays.asList("jquery", "angular-ui")));
    }

    @Test
    public void configuration_item_without_resources_is_empty() {
        ci.addRequires("requires does not count as empty");
        assertTrue(ci.isEmpty());
    }

    @Test
    public void should_remove_extra_if_in_conditional() {
        ci.setConditional("if lt IE 9");
        assertEquals("lt IE 9", ci.getConditional());
    }

    @Test(expected = IllegalArgumentException.class)
    public void fail_when_adding_css_as_js() {
        ci.addJavascript("some.css");
    }

    @Test(expected = IllegalArgumentException.class)
    public void fail_when_adding_js_as_css() {
        ci.addCss("some.js");
    }

}
