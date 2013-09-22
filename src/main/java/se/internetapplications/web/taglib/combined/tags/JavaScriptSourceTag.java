package se.internetapplications.web.taglib.combined.tags;

import javax.servlet.jsp.JspException;

import se.internetapplications.web.taglib.combined.node.ResourceParent;

public class JavaScriptSourceTag extends SourceTagSupport {

    @Override
    public int doEndTag() throws JspException {
        ((ResourceParent) getParent()).addJavascript(getPath());
        return EVAL_PAGE;
    }
}
