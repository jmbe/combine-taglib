package se.intem.web.taglib.combined;

import static org.junit.Assert.*;

import org.junit.Test;

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

    @Test
    public void webjars_should_be_output_as_is_so_treat_as_remote() {
        RequestPath path = new RequestPath("/webjars/bootstrap/2.3.2/css/bootstrap.min.css");
        assertTrue(path.isRemote());
    }
}
