package ru.pushkarev.LogsSearcher.utils;

import ru.pushkarev.LogsSearcher.type.Domain;

import java.nio.file.Path;

/**
 * Created by Opushkarev on 22.12.2016.
 */
public class Config {
    public final static String APP_NAME = "LogsSearcher";
    private static Path workingDirectory = Domain.getPath().resolve("tmp").resolve(APP_NAME);

    public static Path getWorkingDirectory() { return workingDirectory; }
}
