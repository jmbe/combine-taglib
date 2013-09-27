package se.intem.web.taglib.combined.node;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.CharStreams;
import com.google.common.io.LineProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashSet;
import java.util.List;

public class CombineCommentParser {

    @VisibleForTesting
    ParseResult findCombineComment(final InputStream stream) throws IOException {

        LineProcessor<ParseResult> findCombineComment = new LineProcessor<ParseResult>() {

            private boolean foundStart = false;
            private boolean foundEnd = false;
            private boolean foundCommentStart = false;

            private ParseResult result = new ParseResult();

            private List<String> current = Lists.newArrayList();

            @Override
            public boolean processLine(String line) throws IOException {
                line = Strings.nullToEmpty(line).trim();

                boolean foundCommentEnd = line.contains("*/");

                if (line.equals("*")) {
                    return true;
                }

                if (line.startsWith("/* combine")) {
                    foundStart = true;
                    /* Remove comment start */
                    line = line.substring(3);
                } else if (line.startsWith("/*")) {
                    /* Some comment has started. Unknown if this comment contains combine. */
                    foundCommentStart = true;
                } else if (foundCommentStart && line.startsWith("* combine")) {
                    foundStart = true;
                    /* Remove comment start */
                    line = line.substring(2);

                } else if (foundCommentStart && !foundCommentEnd && line.startsWith("*")) {
                    line = line.substring(1);
                }

                if (foundStart) {
                    if (foundCommentEnd) {
                        foundEnd = true;
                        /* Remove comment end */
                        current.add(line.substring(0, line.indexOf("*/")).trim());
                    } else {
                        current.add(line.trim());
                    }
                }

                if (foundEnd) {
                    String comment = Joiner.on(" ").skipNulls().join(current).trim();
                    result.addComment(comment);
                    current = Lists.newArrayList();
                    /* Reset state */
                    foundStart = false;
                    foundEnd = false;
                    foundCommentStart = false;
                }

                return true;
            }

            @Override
            public ParseResult getResult() {
                return result;

            }

        };

        CharStreams.readLines(new InputStreamReader(stream), findCombineComment);

        return findCombineComment.getResult();
    }

    public List<String> findRequires(final InputStream input) throws IOException {
        LinkedHashSet<String> result = Sets.newLinkedHashSet();

        String start = "combine @requires";
        ParseResult parseResult = findCombineComment(input);

        List<String> comments = parseResult.getComments();
        for (String comment : comments) {
            if (!comment.startsWith(start)) {
                continue;
            }

            Iterable<String> split = Splitter.on(" ").omitEmptyStrings().split(comment.substring(start.length()));
            for (String require : split) {
                result.add(require);
            }
        }

        return Lists.newArrayList(result);

    }
}
