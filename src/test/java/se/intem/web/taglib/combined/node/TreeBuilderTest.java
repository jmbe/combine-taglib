package se.intem.web.taglib.combined.node;

import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import se.intem.web.taglib.combined.RequestPath;
import se.intem.web.taglib.combined.configuration.ConfigurationItemsCollection;

public class TreeBuilderTest {

    private InputStream stream;
    private TreeBuilder builder;
    private InputStream illegal;
    private InputStream optional;
    private InputStream optionalRequired;

    @Before
    public void setup() {
        this.stream = this.getClass().getResourceAsStream("/combine.json");
        this.illegal = this.getClass().getResourceAsStream("/illegal.js");
        this.optional = this.getClass().getResourceAsStream("/optional.json");
        this.optionalRequired = this.getClass().getResourceAsStream("/optional-required.json");
        this.builder = new TreeBuilder();
    }

    @Test
    public void test_resources_should_exist() {
        assertNotNull(stream);
        assertNotNull(illegal);
        assertNotNull(optional);
        assertNotNull(optionalRequired);
    }

    @Test
    public void parse_configuration_items() throws JsonParseException, JsonMappingException, IOException {
        ConfigurationItemsCollection config = builder.parse(stream);

        assertEquals(2, config.size());

        ConfigurationItem item = config.iterator().next();

        assertEquals(1, item.getRequiresList().size());
        RequestPath link = item.getCss().get(0);
        assertEquals("1.css", link.getPath());
        assertFalse(link.isRemote());
    }

    @Test
    public void build_graph() throws JsonParseException, JsonMappingException, IOException {
        Map<String, ResourceNode> config = builder.build(stream);

        assertEquals(2, config.size());

        ResourceNode angular = config.get("angular");
        assertNotNull(angular);
        assertEquals(1, angular.resolve().size());

        ResourceNode news = config.get("news");
        assertNotNull(news);

        assertEquals(2, news.resolve().size());
        assertTrue(news.resolve().contains(angular));
    }

    @Test
    public void throw_exception_on_missing_dependency() throws JsonParseException, JsonMappingException, IOException {
        try {
            builder.build(illegal);
            fail("Should have thrown exception");
        } catch (IllegalStateException e) {
            assertEquals("Could not find dependency: news requires 'MISSING-DEPENDENCY'", e.getMessage());
        }
    }

    @Test
    public void parse_with_optional_not_required() throws IOException {
        ConfigurationItemsCollection items = builder.parse(optional);
        List<ConfigurationItem> resolved = builder.resolve(items);

        assertEquals("[angular, news]", resolved.toString());
    }

    @Test
    public void parse_when_optional_required() throws IOException {
        ConfigurationItemsCollection items = builder.parse(optionalRequired);
        List<ConfigurationItem> resolved = builder.resolve(items);

        assertEquals("[jquery, angular, news]", resolved.toString());
    }
}
