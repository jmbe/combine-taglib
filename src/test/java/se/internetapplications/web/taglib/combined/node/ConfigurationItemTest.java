package se.internetapplications.web.taglib.combined.node;

import static org.junit.Assert.*;

import org.junit.Test;

public class ConfigurationItemTest {

    @Test
    public void test() {

        ConfigurationItem ci = new ConfigurationItem();
        assertEquals(0, ci.getRequires().size());
        ci.setRequires("");
        assertEquals(0, ci.getRequires().size());
    }

}
