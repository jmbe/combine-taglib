package se.internetapplications.web.taglib.combined.node;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

public class ConfigurationItemTest {

    @Test
    public void should_not_add_requires_for_empty_string() {

        ConfigurationItem ci = new ConfigurationItem();
        assertEquals(0, ci.getRequires().size());
        ci.addRequires("");
        assertEquals(0, ci.getRequires().size());
    }

    @Test
    public void should_split_strings_even_when_setting_collection() {
        ConfigurationItem ci = new ConfigurationItem();

        ci.setRequires(Arrays.asList("angular, jquery-ui"));
        assertEquals(2, ci.getRequires().size());
    }

    @Test
    public void should_remove_duplicate_requires_and_keep_insertion_order() {
        ConfigurationItem ci = new ConfigurationItem();
        ci.addRequires("jquery, angular-ui,jquery");

        assertEquals("Should remove duplicates", 2, ci.getRequires().size());
        assertThat("Should keep insertion order", ci.getRequires(), is(Arrays.asList("jquery", "angular-ui")));
    }

}
