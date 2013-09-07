package se.internetapplications.web.taglib.combined.tags;

import javax.servlet.jsp.tagext.BodyTagSupport;

public abstract class SourceTagSupport extends BodyTagSupport {

    private String path;

    public void setPath(final String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

}
