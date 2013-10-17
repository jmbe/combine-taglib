package se.intem.web.taglib.combined.tags;

import static org.junit.Assert.*;

import org.junit.Test;

import se.intem.web.taglib.combined.RequestPath;

public class LayoutCssTagTest {

    @Test
    public void format_path_without_media() {
        String format = new LayoutCssTag().format(new RequestPath("PATH"), null);
        assertEquals("<link rel=\"stylesheet\" type=\"text/css\" href=\"PATH\" />", format);
    }

    @Test
    public void format_path_with_media() {
        LayoutCssTag tag = new LayoutCssTag();
        tag.setMedia("MEDIA");
        String format = tag.format(new RequestPath("PATH"), null);
        assertEquals("<link rel=\"stylesheet\" type=\"text/css\" href=\"PATH\" media=\"MEDIA\" />", format);
    }

}
