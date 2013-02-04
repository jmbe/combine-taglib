package se.internetapplications.web.taglib.combined;

import static org.junit.Assert.*;

import org.junit.Test;

import se.internetapplications.web.taglib.combined.CombinedResourceRepository;

public class CombinedResourceRepositoryTest {

    @Test
    public void testCreateScriptPathKey() {
        assertEquals("/thirdparty", CombinedResourceRepository
                .createResourcePathKey("", "thirdparty"));
        assertEquals("/static/js/thirdparty", CombinedResourceRepository
                .createResourcePathKey("/static/js", "thirdparty"));
        assertEquals("/static/js/thirdparty", CombinedResourceRepository
                .createResourcePathKey("/static/js", "thirdparty"));
        assertEquals("/static/js/thirdparty", CombinedResourceRepository
                .createResourcePathKey("static/js/", "thirdparty"));
        assertEquals("/static/js/thirdparty", CombinedResourceRepository
                .createResourcePathKey("/static/js/", "thirdparty"));
    }
}
