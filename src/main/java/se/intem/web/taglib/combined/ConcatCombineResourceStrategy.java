package se.intem.web.taglib.combined;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.intem.web.taglib.combined.configuration.ManagedResource;

public class ConcatCombineResourceStrategy {

    /** Logger for this class. */
    private static final Logger log = LoggerFactory.getLogger(ConcatCombineResourceStrategy.class);

    public long joinPaths(final PrintWriter writer, final List<ManagedResource> realPaths) {
        log.trace("Reading files");

        long timestamp = 0;
        for (ManagedResource realPath : realPaths) {

            try {
                if (realPath.isTimestampSupported()) {
                    timestamp = Math.max(timestamp, realPath.lastModified());
                }

                String contents = CharStreams.toString(CharStreams.newReaderSupplier(realPath.getInputSupplier(),
                        Charsets.UTF_8));

                writer.println(contents);
            } catch (IOException e) {
                throw new RuntimeException("Could not read file " + realPath, e);
            }
        }
        writer.flush();

        return timestamp;
    }

}
