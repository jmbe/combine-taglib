package se.intem.web.taglib.combined.node;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.io.CharStreams;
import com.google.common.io.LineProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

public class CombineCommentParser {

    private ParseResult findCombineComment(final Reader reader, final List<Function<String, String>> transform)
            throws IOException {
        LineProcessor<ParseResult> findCombineComment = new CombineCommentLineProcessor(transform);

        CharStreams.readLines(reader, findCombineComment);

        return findCombineComment.getResult();
    }

    public ParseResult parse(final InputStream input, final List<Function<String, String>> transform)
            throws IOException {
        return findCombineComment(new InputStreamReader(input), transform);
    }

    @VisibleForTesting
    ParseResult parse(final String string) throws IOException {
        return findCombineComment(new StringReader(string), null);
    }

    @VisibleForTesting
    ParseResult parse(final InputStream input) throws IOException {
        return parse(input, null);
    }
}
