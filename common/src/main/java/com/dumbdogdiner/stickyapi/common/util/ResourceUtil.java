package com.dumbdogdiner.stickyapi.common.util;

import com.dumbdogdiner.stickyapi.common.ServerVersion;
import lombok.experimental.UtilityClass;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;


/**
 * Utility class to help with common tasks concerning JAR resources
 */
@UtilityClass
public class ResourceUtil {

    /**
     * Convenience overload method that assumes replace is true
     * @see #copyJarResource(Class, String, String, boolean)
     */
    public void copyJarResource(Class<?> jarClass, String resourcePath, String baseExternalPath) throws IOException {
        copyJarResource(jarClass, resourcePath, baseExternalPath, true);
    }

    /**
     * Method to copy a resource from a JAR to a specified external path
     * @param jarClass The class within the jar that the resource resides
     * @param resourcePath The path to the resource in the jar
     * @param baseExternalPath The base path where the resource should be extracted
     * @param replace If the file should be replaced if it exists
     * @throws IOException If the resource is not found or if some issue occurs during File IO
     */
    public void copyJarResource(Class<?> jarClass, String resourcePath, String baseExternalPath, boolean replace) throws IOException {
        ReadableByteChannel inChannel = Channels.newChannel(jarClass.getResourceAsStream(resourcePath));

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

    /**
     * Convenience method for bukkit plugins (ONLY) that saves a jar resource to the plugin's data folder
     * @param plugin the {@link JavaPlugin} where the resource resides
     * @param resourcePath the path to the inbuilt resource
     * @param replace If the file should be replaced if it exists
     * @see JavaPlugin#saveResource(String, boolean)
     */
    public void copyJarResource(JavaPlugin plugin, String resourcePath, boolean replace){
        if(!ServerVersion.isBukkit())
            throw new UnsupportedOperationException("This method is only supported on bukkit. Please use ResourceUtil#copyJarResource(Class, String, String, boolean)");

        plugin.saveResource(resourcePath, replace);
    }
}
