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

import se.intem.web.taglib.combined.resources.TokenType;

public class ParseResult {

    /**
     * The whole file contents.
     */
    private StringBuilder content = new StringBuilder();

    private LinkedHashSet<String> requires = Sets.newLinkedHashSet();
    private LinkedHashSet<String> provides = Sets.newLinkedHashSet();
    private LinkedHashSet<String> optionals = Sets.newLinkedHashSet();

    public void addComment(final String comment) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(comment));

        Iterable<String> split = Splitter.on(" ").omitEmptyStrings().split(comment);

        Preconditions.checkArgument("combine".equals(split.iterator().next()), "First token must be combine");

        /* Remove initial combine token */
        Iterable<String> tokens = Iterables.skip(split, 1);

        TokenType tokenType = null;

        for (String token : tokens) {
            if ("@requires".equals(token)) {
                tokenType = TokenType.requires;
            } else if ("@provides".equals(token)) {
                tokenType = TokenType.provides;
            } else if ("@optional".equals(token)) {
                tokenType = TokenType.optional;
            } else if (TokenType.provides.equals(tokenType)) {
                provides.add(token);
            } else if (TokenType.requires.equals(tokenType)) {
                requires.add(token);
            } else if (TokenType.optional.equals(tokenType)) {
                optionals.add(token);
            } else {
                throw new IllegalStateException("Don't know what to do with token '" + token + "'");
            }
        }

    }

    public void addContent(final String line) {
        if (content.length() > 0) {
            content.append(System.lineSeparator());
        }
        content.append(line);
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

    public Iterable<String> getOptionals() {
        return optionals;
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
