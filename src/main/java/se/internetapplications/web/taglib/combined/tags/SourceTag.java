package se.internetapplications.web.taglib.combined.tags;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

public class SourceTag extends TagSupport {

    private String path;

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    @Override
    public int doStartTag() throws JspException {
        // log.debug("Start source");
        return SKIP_BODY;
    }

    @Override
    public int doEndTag() throws JspException {

        ((CombinedTagSupport) getParent()).addSource(getPath());
        // log.debug("end SourceTag");
        return EVAL_PAGE;
    }

}
