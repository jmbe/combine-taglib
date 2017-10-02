package se.intem.web.taglib.combined.node;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.io.LineProcessor;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CombineCommentLineProcessor implements LineProcessor<ParseResult> {

    private static final Pattern commentBlock = Pattern.compile("/\\*\\*?!?-?\\s*(combine .*?)\\*/");
    private static final Pattern whitespacePrefix = Pattern.compile("^\\s+");

    private ParseResult result = new ParseResult();

    /**
     * Pre-processors for each line in file.
     */
    private List<Function<String, String>> transforms;

    public CombineCommentLineProcessor(final List<Function<String, String>> transforms) {
        this.transforms = transforms;
    }

    @Override
    public boolean processLine(final String contentLine) {

        String line = contentLine;
        if (this.transforms != null) {
            for (Function<String, String> fn : this.transforms) {
                line = fn.apply(line);
            }
        }

        line = Strings.nullToEmpty(line).trim();

        if (line.isEmpty()) {
            result.addContent(contentLine);
            return true;
        }

        // Extract and remove combine comments
        Matcher blockMatcher = commentBlock.matcher(line);
        while (blockMatcher.find()) {
            String combineComment = blockMatcher.group(1);
            result.addComment(combineComment);
            line = blockMatcher.replaceFirst("");
            blockMatcher = commentBlock.matcher(line);
        }

        // Add remaining line as content
        String whitespace = leadingWhitespace(contentLine);
        result.addContent(whitespace + line);

        return true; // continue parsing
    }

    private String leadingWhitespace(String contentLine) {

        Matcher whitespaceMatcher = whitespacePrefix.matcher(contentLine);

        if (whitespaceMatcher.find()) {
            return whitespaceMatcher.group();
        }

        return "";
    }

    @Override
    public ParseResult getResult() {
        return result;
    }
}
