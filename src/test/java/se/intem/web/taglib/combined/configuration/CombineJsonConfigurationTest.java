package se.intem.web.taglib.combined.configuration;

import static org.junit.Assert.*;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import se.intem.web.taglib.combined.node.ConfigurationItem;

public class CombineJsonConfigurationTest {

    private CombineJsonConfiguration loader;

    @Before
    public void setup() {
        this.loader = new CombineJsonConfiguration();
    }

    @Test
    public void should_find_configuration_on_classpath() {
        Optional<ConfigurationItemsCollection> configuration = loader.readConfiguration();
        assertTrue(configuration.isPresent());
        assertEquals(3, configuration.get().size());
    }

    @Test
    public void should_reuse_unmodified_configuration() {
        Optional<ConfigurationItemsCollection> first = loader.readConfiguration();
        assertTrue(first.isPresent());

        Optional<ConfigurationItemsCollection> second = loader.readConfiguration();

        assertSame(first, second);
    }

    @Test
    public void should_find_conditional() {
        Optional<ConfigurationItemsCollection> configuration = loader.readConfiguration();

        List<ConfigurationItem> items = Lists.newArrayList(configuration.get().iterator());
        ConfigurationItem third = items.get(2);
        assertEquals("IE lt 10", third.getConditional());
    }

}
