package se.intem.web.taglib.combined.node;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.LineProcessor;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CombineCommentLineProcessor implements LineProcessor<ParseResult> {

    private static final Pattern commentStart = Pattern.compile("^/\\*\\*?-?\\s*");
    private static final Pattern continuationStart = Pattern.compile("^\\*\\s+");

    private boolean foundStart = false;
    private boolean foundEnd = false;

    /* Found start of any comment, which may turn out to be desired comment. */
    private boolean foundCommentStart = false;

    private ParseResult result = new ParseResult();

    private List<String> current = Lists.newArrayList();

    private List<String> currentComment = Lists.newArrayList();

    @Override
    public boolean processLine(String line) throws IOException {

        String contentLine = line;

        line = Strings.nullToEmpty(line).trim();

        if (line.isEmpty()) {
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
            } else {
                currentComment.add(contentLine);
            }
        }

        boolean checkRemaining = false;
        if (foundStart) {
            if (foundCommentEnd) {
                foundEnd = true;

                /* Remove comment end */
                int index = replaced.indexOf("*/");

                current.add(replaced.substring(0, index).trim());
                contentLine = replaced.substring(index + 2);
                checkRemaining = true;
            } else {
                current.add(replaced.replaceFirst("[\\* /-]+$", ""));
            }
        }

        boolean addContent = !foundCommentStart && (!foundStart || foundEnd);

        if (foundEnd) {
            String comment = Joiner.on(" ").skipNulls().join(current).trim();
            result.addComment(comment);
            current = Lists.newArrayList();
            /* Reset state */
            foundStart = false;
            foundEnd = false;
            foundCommentStart = false;
            currentComment = Lists.newArrayList();
        } else if (foundCommentEnd) {
            for (String string : currentComment) {
                result.addContent(string);
            }
            currentComment = Lists.newArrayList();

            foundCommentStart = false;
        }

        if (checkRemaining) {
            if (!contentLine.isEmpty()) {
                processLine(contentLine);
            }
        } else if (addContent) {
            result.addContent(contentLine);
        }

        return true;
    }

    @Override
    public ParseResult getResult() {
        return result;
    }
}
