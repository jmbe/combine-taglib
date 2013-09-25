package se.intem.web.taglib.combined.tags;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import com.google.common.collect.Lists;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import se.intem.web.taglib.combined.node.ConfigurationItem;
import se.intem.web.taglib.combined.tags.ConfigurationItemsCollection;

public class ConfigurationItemsCollectionTest {

    private ConfigurationItemsCollection items;
    private ConfigurationItem a;
    private ConfigurationItem b;
    private ConfigurationItem c;

    @Before
    public void setup() {
        this.items = new ConfigurationItemsCollection();
        this.a = new ConfigurationItem();
        a.setName("A");
        this.b = new ConfigurationItem();
        b.setName("B");
        this.c = new ConfigurationItem();
        c.setName("C");
    }

    @Test(expected = IllegalStateException.class)
    public void should_detect_duplicate_resource_name() {
        items.add(a);
        items.add(a);
    }

    @Test
    public void should_maintain_insertion_order() {
        items.add(c);
        items.add(a);
        items.add(b);

        List<ConfigurationItem> actual = Lists.newArrayList(items);
        List<ConfigurationItem> expected = Lists.newArrayList(c, a, b);

        assertThat(actual, equalTo(expected));
    }

}
