package se.intem.web.taglib.combined.node;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import java.util.List;

public class ParseResult {
    private List<String> comments = Lists.newArrayList();

    public List<String> getComments() {
        return comments;
    }

    public void addComment(final String comment) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(comment));
        comments.add(comment);
    }
}
