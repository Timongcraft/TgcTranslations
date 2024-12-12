package de.timongcraft.tgctranslations.utils;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.FileSystem;
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

    public static Map<String, InputStream> getFileStreams(String folderPath, Logger logger) {
        ClassLoader classLoader = ResourceUtils.class.getClassLoader();

        try {
            URL resourceUrl = classLoader.getResource(folderPath);
            if (resourceUrl != null) {
                try (FileSystem fileSystem = FileSystems.newFileSystem(resourceUrl.toURI(), Collections.emptyMap())) {
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