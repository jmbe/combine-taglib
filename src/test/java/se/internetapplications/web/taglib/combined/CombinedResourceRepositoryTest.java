package se.internetapplications.web.taglib.combined;

import static org.junit.Assert.*;

import org.junit.Test;

import se.internetapplications.web.taglib.combined.CombinedResourceRepository;

public class CombinedResourceRepositoryTest {

    @Test
    public void testCreateScriptPathKey() {
        assertEquals("/thirdparty", CombinedResourceRepository
                .createScriptPathKey("", "thirdparty"));
        assertEquals("/static/js/thirdparty", CombinedResourceRepository
                .createScriptPathKey("/static/js", "thirdparty"));
        assertEquals("/static/js/thirdparty", CombinedResourceRepository
                .createScriptPathKey("/static/js", "thirdparty"));
        assertEquals("/static/js/thirdparty", CombinedResourceRepository
                .createScriptPathKey("static/js/", "thirdparty"));
        assertEquals("/static/js/thirdparty", CombinedResourceRepository
                .createScriptPathKey("/static/js/", "thirdparty"));
    }
}
