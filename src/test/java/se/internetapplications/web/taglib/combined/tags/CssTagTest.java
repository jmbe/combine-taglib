package se.internetapplications.web.taglib.combined.tags;

import static org.junit.Assert.*;

import org.junit.Test;

public class CssTagTest {

    @Test
    public void format_path_without_media() {
        String format = new CssTag().format("PATH");
        assertEquals("<link rel=\"stylesheet\" type=\"text/css\" href=\"PATH\" />", format);
    }

    @Test
    public void format_path_with_media() {
        CssTag tag = new CssTag();
        tag.setMedia("MEDIA");
        String format = tag.format("PATH");
        assertEquals("<link rel=\"stylesheet\" type=\"text/css\" href=\"PATH\" media=\"MEDIA\" />", format);
    }

}
