package se.intem.web.taglib.combined.configuration;

import com.google.common.base.Strings;

public class InlineContent implements SupportsConditional {

    private String contents;

    /* Use conditional from ConfigurationItem? */
    private String conditional;

    public InlineContent(final String contents) {
        this(contents, null);
    }

    public InlineContent(final String contents, final String conditional) {
        this.contents = clean(contents);
        this.conditional = conditional;
    }

    private String clean(final String content) {
        return Strings.nullToEmpty(content).replaceAll("</?(script|style)[^>]*>", "");
    }

    public String getContents() {
        return contents;
    }

    public void setConditional(final String conditional) {
        this.conditional = conditional;
    }

    public String getConditional() {
        return conditional;
    }

    public boolean hasConditional() {
        return !Strings.nullToEmpty(this.conditional).trim().isEmpty();
    }

}
