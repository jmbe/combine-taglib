package se.intem.web.taglib.combined;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YahooCombineResourceStrategy {

    /** Logger for this class. */
    private static final Logger log = LoggerFactory.getLogger(YahooCombineResourceStrategy.class);

    public long yuiCompressPaths(final PrintWriter writer, final List<String> realPaths) throws IOException,
            InterruptedException {
        YuiCompressorWriter yuiWriter = new YuiCompressorWriter(writer);

        log.info("Starting compressor thread");
        Thread compressorThread = new Thread(yuiWriter);
        compressorThread.start();

        log.info("Reading files");

        long timestamp = 0;
        for (String realPath : realPaths) {

            try {
                File file = new File(realPath);
                timestamp = Math.max(timestamp, file.lastModified());
                String contents = Files.toString(file, Charsets.UTF_8);
                yuiWriter.write(contents);
            } catch (IOException e) {
                throw new RuntimeException("Could not read file " + realPath, e);
            }
        }
        yuiWriter.flush();
        yuiWriter.close();

        compressorThread.join();
        return timestamp;
    }

}
