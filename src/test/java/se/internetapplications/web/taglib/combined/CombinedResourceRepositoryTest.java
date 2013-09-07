package se.internetapplications.web.taglib.combined;

import static org.junit.Assert.*;
import static se.internetapplications.web.taglib.combined.CombinedResourceRepository.*;

import org.junit.Test;

public class CombinedResourceRepositoryTest {

    @Test
    public void testCreateScriptPathKey() {
        assertEquals("/thirdparty", createResourcePathKey("thirdparty"));
        assertEquals("/static/js/thirdparty", createResourcePathKey("static/js/thirdparty"));
    }
}
