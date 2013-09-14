package se.internetapplications.web.taglib.combined;

import static org.junit.Assert.*;
import static se.internetapplications.web.taglib.combined.CombinedResourceRepository.*;

import org.junit.Test;

public class CombinedResourceRepositoryTest {

    @Test
    public void javascript_resource_path_key() {
        assertEquals("/thirdparty/js", createResourcePathKey("thirdparty", ResourceType.js));
    }

    @Test
    public void key_whose_name_contains_slashes() {
        assertEquals("/static/js/thirdparty/js", createResourcePathKey("static/js/thirdparty", ResourceType.js));
    }

    @Test
    public void css_resource_path_key() {
        assertEquals("/thirdparty/css", createResourcePathKey("thirdparty", ResourceType.css));
    }
}
