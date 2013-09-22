package se.internetapplications.web.taglib.combined.node;

import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import se.internetapplications.web.taglib.combined.RequestPath;
import se.internetapplications.web.taglib.combined.tags.ConfigurationItemsCollection;

public class TreeBuilderTest {

    private InputStream stream;
    private TreeBuilder builder;
    private InputStream illegal;

    @Before
    public void setup() {
        this.stream = this.getClass().getResourceAsStream("/combine.js");
        this.illegal = this.getClass().getResourceAsStream("/illegal.js");
        this.builder = new TreeBuilder();
    }

    @Test
    public void test_resources_should_exist() {
        assertNotNull(stream);
        assertNotNull(illegal);
    }

    @Test
    public void parse_configuration_items() throws JsonParseException, JsonMappingException, IOException {
        ConfigurationItemsCollection config = builder.parse(stream);

        assertEquals(2, config.size());

        ConfigurationItem item = config.iterator().next();

        assertEquals(1, item.getRequires().size());
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
}
