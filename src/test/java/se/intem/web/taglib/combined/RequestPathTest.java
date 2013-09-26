package se.intem.web.taglib.combined;

import static org.junit.Assert.*;

import org.junit.Test;

import se.intem.web.taglib.combined.RequestPath;

public class RequestPathTest {

    @Test
    public void toString_should_return_path() {
        RequestPath path = new RequestPath("//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js");
        assertEquals("//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js", path.getPath());
    }

    @Test
    public void should_detect_remote_paths_without_protocol() {
        RequestPath path = new RequestPath("//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js");
        assertTrue(path.isRemote());
    }
}
