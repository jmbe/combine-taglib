package se.internetapplications.web.taglib.combined.node;

import static org.junit.Assert.*;

import org.junit.Test;

public class ConfigurationItemTest {

    @Test
    public void should_not_add_requires_for_empty_string() {

        ConfigurationItem ci = new ConfigurationItem();
        assertEquals(0, ci.getRequires().size());
        ci.addRequires("");
        assertEquals(0, ci.getRequires().size());
    }

}
