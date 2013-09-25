package se.intem.web.taglib.combined;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import se.intem.web.taglib.combined.CombinedResourceRepository;
import se.intem.web.taglib.combined.ResourceType;

public class CombinedResourceRepositoryTest {

    private CombinedResourceRepository repository;

    @Before
    public void setup() {
        this.repository = CombinedResourceRepository.get();
    }

    @Test
    public void javascript_resource_path_key() {
        assertEquals("/thirdparty/js", repository.createResourcePathKey("thirdparty", ResourceType.js));
    }

    @Test
    public void key_whose_name_contains_slashes() {
        assertEquals("/static/js/thirdparty/js",
                repository.createResourcePathKey("static/js/thirdparty", ResourceType.js));
    }

    @Test
    public void css_resource_path_key() {
        assertEquals("/thirdparty/css", repository.createResourcePathKey("thirdparty", ResourceType.css));
    }
}
