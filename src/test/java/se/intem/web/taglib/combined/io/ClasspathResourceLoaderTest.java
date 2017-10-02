package se.intem.web.taglib.combined.io;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class ClasspathResourceLoaderTest {

    private ClasspathResourceLoader loader;

    @Before
    public void setup() {
        this.loader = new ClasspathResourceLoader();
    }

    @Test
    public void should_find_single_file_in_root() {
        assertThat(loader.findOneInClasspath("/bug-rx.js").isPresent(), is(true));
    }

    @Test
    public void should_find_file_in_root_with_many_search() {
        assertThat(loader.findManyInClasspath("/bug-rx.js").isEmpty(), is(false));
    }

    @Test(timeout = 100)
    public void should_find_files_in_directory() {
        assertThat(loader.findManyInClasspath("/META-INF/directory-test.json").isEmpty(), is(false));
    }

}
