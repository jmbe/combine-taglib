package se.intem.web.taglib.combined.node;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
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
    private LinkedHashSet<String> provides = Sets.newLinkedHashSet();

    public void addComment(final String comment) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(comment));

        Iterable<String> split = Splitter.on(" ").omitEmptyStrings().split(comment);

        Preconditions.checkArgument("combine".equals(split.iterator().next()), "First token must be combine");

        /* Remove initial combine token */
        Iterable<String> tokens = Iterables.skip(split, 1);

        boolean addingProvides = false;

        for (String token : tokens) {
            if ("@requires".equals(token)) {
                addingProvides = false;
            } else if ("@provides".equals(token)) {
                addingProvides = true;
            } else if (addingProvides) {
                provides.add(token);
            } else {
                requires.add(token);
            }
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

    public Iterable<String> getProvides() {
        return provides;
    }

    @VisibleForTesting
    List<String> getRequiresList() {
        return Lists.newArrayList(requires);
    }

    @VisibleForTesting
    List<String> getProvidesList() {
        return Lists.newArrayList(provides);
    }

}
