package se.internetapplications.web.taglib.combined.node;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import com.google.common.io.LineProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class JavaScriptParser {

    /* Maximum number of lines to read to find requires. */
    private static final int MAX_LINES = 20;

    @VisibleForTesting
    String findCombineComment(final InputStream stream) throws IOException {

        LineProcessor<String> findCombineComment = new LineProcessor<String>() {

            private int lines = 0;
            private boolean foundStart = false;
            private boolean foundEnd = false;
            private boolean foundCommentStart = false;

            List<String> result = Lists.newArrayList();

            @Override
            public boolean processLine(String line) throws IOException {
                line = Strings.nullToEmpty(line).trim();
                if (!foundStart && lines++ > MAX_LINES) {
                    return false;
                }

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
                        result.add(line.substring(0, line.indexOf("*/")).trim());
                    } else {
                        result.add(line.trim());
                    }
                }

                return !foundEnd;
            }

            @Override
            public String getResult() {
                return Joiner.on(" ").skipNulls().join(result).trim();
            }

        };

        CharStreams.readLines(new InputStreamReader(stream), findCombineComment);

        return findCombineComment.getResult();
    }

    public List<String> findRequires(final InputStream single) throws IOException {

        String comment = findCombineComment(single);
        String start = "combine @requires";
        Preconditions.checkArgument(comment.startsWith(start));

        Iterable<String> split = Splitter.on(" ").omitEmptyStrings().split(comment.substring(start.length()));
        return Lists.newArrayList(split);

    }
}
