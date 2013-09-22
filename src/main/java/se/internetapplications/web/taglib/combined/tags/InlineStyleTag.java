package se.internetapplications.web.taglib.combined.tags;

public class InlineStyleTag extends InlineTagSupport {

    @Override
    protected void addContents(final String contents) {
        getConfigurationItems().addInlineStyle(contents);
    }

}
