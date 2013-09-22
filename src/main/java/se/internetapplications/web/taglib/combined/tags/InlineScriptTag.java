package se.internetapplications.web.taglib.combined.tags;

public class InlineScriptTag extends InlineTagSupport {

    @Override
    protected void addContents(final String contents) {
        getConfigurationItems().addInlineScript(contents);
    }

}
