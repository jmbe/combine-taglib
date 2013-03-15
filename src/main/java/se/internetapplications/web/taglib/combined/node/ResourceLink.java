package se.internetapplications.web.taglib.combined.node;

import com.google.common.base.Preconditions;

public class ResourceLink {

    private String link;

    public ResourceLink(final String link) {
        Preconditions.checkNotNull(link);
        this.link = link;
    }

    public boolean isRemote() {
        return link.contains("://") || link.startsWith("//");
    }

    public String getLink() {
        return link;
    }

    public void setLink(final String link) {
        this.link = link;
    }

}
