package se.intem.web.taglib.combined.tags.angular;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

public class NgTemplateTag extends BodyTagSupport {

    private String name;

    protected void println(final String output) throws JspException {
        try {
            pageContext.getOut().println(output);
        } catch (IOException e) {
            throw new JspException(e);
        }
    }

    @Override
    public int doStartTag() throws JspException {
        println(String.format("<script type=\"text/ng-template\" id=\"%s\">", this.name));
        return EVAL_BODY_INCLUDE;
    }

    @Override
    public int doEndTag() throws JspException {
        println("</script>");
        return EVAL_PAGE;
    }

    public void setName(final String name) {
        this.name = name;
    }
}
