package se.intem.web.taglib.combined.tags;

import javax.servlet.jsp.JspException;

import se.intem.web.taglib.combined.node.ResourceParent;

public class CssSourceTag extends SourceTagSupport {

    private String media;

    @Override
    public int doEndTag() throws JspException {
        ((ResourceParent) getParent()).addCss(getPath());
        return EVAL_PAGE;
    }

    public void setMedia(final String media) {
        this.media = media;
    }

    public String getMedia() {
        return media;
    }
}
