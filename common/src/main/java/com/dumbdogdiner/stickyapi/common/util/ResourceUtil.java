package com.dumbdogdiner.stickyapi.common.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class ResourceUtil {
    public void copyJarResource(Class<?> pluginClass, String resourcePath, String baseExternalPath) throws IOException {
        copyJarResource(pluginClass, resourcePath, baseExternalPath, true);
    }

    public void copyJarResource(Class<?> pluginClass, String resourcePath, String baseExternalPath, boolean replace) throws IOException {
        ReadableByteChannel inChannel = Channels.newChannel(pluginClass.getResourceAsStream(resourcePath));

        String outPath = (baseExternalPath + resourcePath).replace("/", System.getProperty("path.separator")).replace("\\", System.getProperty("path.separator"));
        File outFile = new File(outPath);
        if (replace || !outFile.exists()) {
            FileOutputStream outStream = new FileOutputStream(outFile);

            outStream.getChannel().transferFrom(inChannel, 0, Long.MAX_VALUE);
            outStream.flush();
            outStream.close();
        }

        inChannel.close();
    }
}
