package se.internetapplications.web.taglib.combined.node;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;

public class ResourceLink {

    private String link;

    static final Predicate<ResourceLink> isRemote = new Predicate<ResourceLink>() {

        public boolean apply(final ResourceLink item) {
            return item.isRemote();

        }
    };

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
