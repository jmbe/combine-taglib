package se.internetapplications.web.taglib.combined.node;

import com.google.common.collect.Lists;

import java.util.List;

public class ConfigurationItem {

    private String name;

    private List<String> requires = Lists.newArrayList();
    private List<ResourceLink> js = Lists.newArrayList();
    private List<ResourceLink> css = Lists.newArrayList();

    public List<String> getRequires() {
        return requires;
    }

    public void setRequires(final List<String> requires) {
        this.requires = requires;
    }

    public List<ResourceLink> getJs() {
        return js;
    }

    public void setJs(final List<ResourceLink> js) {
        this.js = js;
    }

    public List<ResourceLink> getCss() {
        return css;
    }

    public void setCss(final List<ResourceLink> css) {
        this.css = css;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

}
