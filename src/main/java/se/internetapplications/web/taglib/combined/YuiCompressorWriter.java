package se.internetapplications.web.taglib.combined;

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.Writer;

import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

public class YuiCompressorWriter extends Writer implements Runnable {

    private Writer writer;
    private PipedReader reader;
    private PipedWriter pipedWriter;

    public YuiCompressorWriter(final Writer writer) {
        this.writer = writer;

        try {
            this.reader = new PipedReader();
            this.pipedWriter = new PipedWriter(this.reader);
        } catch (IOException e) {
            throw new RuntimeException("Could not create pipe.", e);
        }

    }

    @Override
    public void close() throws IOException {
        flush();
        pipedWriter.close();
    }

    @Override
    public void flush() throws IOException {
        pipedWriter.flush();
        writer.flush();

    }

    @Override
    public void write(final char[] cbuf, final int off, final int len)
            throws IOException {
        pipedWriter.write(cbuf, off, len);

    }

    public void run() {

        try {
            final ErrorReporter errorReporter = new RhinoErrorReporter();
            JavaScriptCompressor jsc = new JavaScriptCompressor(reader,
                    errorReporter);
            jsc.compress(writer, 8000, true, true, true, true);
        } catch (EvaluatorException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
