package se.intem.web.taglib.combined.node;

import com.google.common.io.CharStreams;
import com.google.common.io.LineProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

public class CombineCommentParser {

    private ParseResult findCombineComment(final Reader reader) throws IOException {
        LineProcessor<ParseResult> findCombineComment = new CombineCommentLineProcessor();

        CharStreams.readLines(reader, findCombineComment);

        return findCombineComment.getResult();
    }

    public ParseResult parse(final InputStream input) throws IOException {
        return findCombineComment(new InputStreamReader(input));
    }

    public ParseResult parse(final String string) throws IOException {
        return findCombineComment(new StringReader(string));
    }
}
