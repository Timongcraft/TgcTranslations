package de.timongcraft.tgctranslations.utils;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ResourceUtils {

    public static Map<String, InputStream> getFileStreams(String folderPath, ClassLoader classLoader, Logger logger) {
        try {
            URL resourceUrl = classLoader.getResource(folderPath);
            if (resourceUrl != null) {
                FileSystem fileSystem = null;
                boolean createdFileSystem = false;
                try {
                    URI resourceUri = resourceUrl.toURI();
                    try {
                        fileSystem = FileSystems.getFileSystem(resourceUri);
                    } catch (FileSystemNotFoundException e) {
                        fileSystem = FileSystems.newFileSystem(resourceUri, Collections.emptyMap());
                        createdFileSystem = true;
                    }

                    Path folderRootPath = fileSystem.getPath(folderPath);
                    try (Stream<Path> paths = Files.walk(folderRootPath, 1)) {
                        return paths.filter(path -> !path.equals(folderRootPath))
                                .map(path -> {
                                    InputStream stream = classLoader.getResourceAsStream(path.toString());
                                    if (stream == null) return null;
                                    return Map.entry(path.getFileName().toString(), stream);
                                })
                                .filter(Objects::nonNull)
                                .collect(Collectors.toMap(
                                        Map.Entry::getKey,
                                        Map.Entry::getValue));
                    }
                } finally {
                    if (createdFileSystem && fileSystem != null) {
                        fileSystem.close();
                    }
                }
            } else {
                logger.log(Level.WARNING, "Unable to find folder " + folderPath + " in resources");
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Unable to read resources files from " + folderPath, e);
        }
        return Collections.emptyMap();
    }

    private ResourceUtils() {}

}