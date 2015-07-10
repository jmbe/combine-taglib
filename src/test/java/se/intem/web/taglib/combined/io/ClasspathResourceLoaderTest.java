package se.intem.web.taglib.combined.io;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ClasspathResourceLoaderTest {

    private ClasspathResourceLoader loader;

    @Before
    public void setup() {
        this.loader = new ClasspathResourceLoader();
    }

    @Test
    public void should_find_single_file_in_root() {
        assertThat(loader.findOneInClasspath("/bug1.js").isPresent(), is(true));
    }

    @Test
    public void should_find_file_in_root_with_many_search() {
        assertThat(loader.findManyInClasspath("/bug1.js").isEmpty(), is(false));
    }

    @Test(timeout = 100)
    public void should_find_files_in_directory() {
        assertThat(loader.findManyInClasspath("/META-INF/directory-test.json").isEmpty(), is(false));
    }

}
