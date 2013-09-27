package se.intem.web.taglib.combined.node;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.LinkedHashSet;
import java.util.List;

public class ParseResult {

    /**
     * The whole file contents.
     */
    private StringBuilder content = new StringBuilder();

    private LinkedHashSet<String> requires = Sets.newLinkedHashSet();

    public void addComment(final String comment) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(comment));

        String start = "combine @requires";

        if (!comment.startsWith(start)) {
            return;
        }

        Iterable<String> split = Splitter.on(" ").omitEmptyStrings().split(comment.substring(start.length()));
        for (String require : split) {
            requires.add(require);
        }

    }

    public void addContent(final String line) {
        content.append(line + "\r\n");
    }

    public String getContents() {
        return content.toString();
    }

    public Iterable<String> getRequires() {
        return requires;
    }

    @VisibleForTesting
    public List<String> getRequiresList() {
        return Lists.newArrayList(requires);
    }

}
