package se.intem.web.taglib.combined.configuration;

import static org.junit.Assert.*;

import com.google.common.base.Optional;

import org.junit.Before;
import org.junit.Test;

import se.intem.web.taglib.combined.configuration.CombineJsonConfiguration;
import se.intem.web.taglib.combined.configuration.ConfigurationItemsCollection;

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
        assertEquals(2, configuration.get().size());
    }

    @Test
    public void should_reuse_unmodified_configuration() {
        Optional<ConfigurationItemsCollection> first = loader.readConfiguration();
        assertTrue(first.isPresent());

        Optional<ConfigurationItemsCollection> second = loader.readConfiguration();

        assertSame(first, second);
    }

}
