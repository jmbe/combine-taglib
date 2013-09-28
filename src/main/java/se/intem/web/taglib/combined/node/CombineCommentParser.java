package se.intem.web.taglib.combined.node;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import com.google.common.io.LineProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CombineCommentParser {

    private static final Pattern commentStart = Pattern.compile("^/\\*\\*?-?\\s*");
    private static final Pattern continuationStart = Pattern.compile("^\\*\\s+");

    @VisibleForTesting
    ParseResult findCombineComment(final InputStream stream) throws IOException {

        LineProcessor<ParseResult> findCombineComment = new LineProcessor<ParseResult>() {

            private boolean foundStart = false;
            private boolean foundEnd = false;
            /* Found start of any comment, which may turn out to be desired comment. */
            private boolean foundCommentStart = false;

            private ParseResult result = new ParseResult();

            private List<String> current = Lists.newArrayList();

            @Override
            public boolean processLine(String line) throws IOException {

                String contentLine = line;

                line = Strings.nullToEmpty(line).trim();

                if (line.isEmpty() || line.equals("*")) {
                    result.addContent(contentLine);
                    return true;
                }

                boolean foundCommentEnd = line.contains("*/");

                Matcher startmatcher = commentStart.matcher(line);
                String replaced = line;
                if (startmatcher.find()) {
                    foundCommentStart = true;
                    replaced = startmatcher.replaceFirst("");
                } else if (foundCommentStart) {
                    Matcher continuationMatcher = continuationStart.matcher(replaced);
                    if (continuationMatcher.find()) {
                        replaced = continuationMatcher.replaceFirst("");
                    }
                }

                if (foundCommentStart && !foundStart) {
                    if (replaced.startsWith("combine")) {
                        foundStart = true;
                    }
                }

                if (foundStart) {
                    if (foundCommentEnd) {
                        foundEnd = true;

                        /* Remove comment end */
                        int index = replaced.indexOf("*/");

                        current.add(replaced.substring(0, index).trim());
                        contentLine = replaced.substring(index + 2);
                    } else {
                        current.add(replaced.replaceFirst("[\\* /-]+$", ""));
                    }
                }

                if (!foundStart || foundEnd) {
                    result.addContent(contentLine);
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

    public ParseResult parse(final InputStream input) throws IOException {
        return findCombineComment(input);
    }
}
