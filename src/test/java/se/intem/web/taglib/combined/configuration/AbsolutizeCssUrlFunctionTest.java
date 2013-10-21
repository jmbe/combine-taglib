package se.intem.web.taglib.combined.configuration;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import se.intem.web.taglib.combined.RequestPath;

public class AbsolutizeCssUrlFunctionTest {

    private AbsolutizeCssUrlFunction fn;

    @Before
    public void setup() {
        this.fn = new AbsolutizeCssUrlFunction(new RequestPath("/dir/file.css"));
    }

    @Test
    public void use_parent_dir_for_relative_paths() {
        String apply = fn.apply("image-background: url('image.png')");
        assertEquals("image-background: url('/dir/image.png')", apply);
    }

    @Test
    public void dont_change_remote_urls() {
        String apply = fn.apply("image-background: url('http://a.se/image.png')");
        assertEquals("image-background: url('http://a.se/image.png')", apply);
    }

    @Test
    public void dont_change_absolute_urls() {
        String apply = fn.apply("image-background: url('/keep/image.png')");
        assertEquals("image-background: url('/keep/image.png')", apply);
    }

    @Test
    public void dont_change_data_urls() {
        String apply = fn.apply("image-background: url('data:abcde')");
        assertEquals("image-background: url('data:abcde')", apply);
    }

}
