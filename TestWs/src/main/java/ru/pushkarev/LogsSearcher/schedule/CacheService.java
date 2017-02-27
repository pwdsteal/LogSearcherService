package ru.pushkarev.LogsSearcher.schedule;

import ru.pushkarev.LogsSearcher.type.Request;
import ru.pushkarev.LogsSearcher.utils.Config;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

@Singleton
public class CacheService {
    private static final Logger log = Logger.getLogger(CacheService.class.getName());

    private static final String[] extensions = {".xml",".html",".pdf",".rtf", ".doc"};
    private final AtomicBoolean busy = new AtomicBoolean(false);

    private static final Set<File> cacheFileList = new HashSet<>();

    public static void addFileToCache(File file) {
        cacheFileList.add(file);
    }

    @Lock(LockType.READ)
    public void cacheMonitor() {
        if (!busy.compareAndSet(false, true)) {
            return;
        }
        maintainCache();
        busy.set(false);
    }

    @Lock(LockType.READ)
    public static File tryFindCachedFile(Request request) {
        String hashcode = Integer.toString(request.hashCode());

        List<File> matchList = new ArrayList<>();
        for (File file : cacheFileList) {
            if (null == file) continue;

            String fileName = file.getName();
            // find all files with hashcode
            if (file.exists() && fileName.contains(hashcode)) {
                matchList.add(file);
            }
        }

        // find file with required extension (.pdf ...)
        if (request.isFileRequested()) {
            for (File matchFile : matchList) {
                if (matchFile.getName().toLowerCase().endsWith(request.getOutputFormat())) {
                    return matchFile;
                }
            }
        }

        // try to find with .xml extension
        for (File matchFile : matchList) {
            if (matchFile.getName().toLowerCase().endsWith(".xml")) {
                return matchFile;
            }
        }

        return null;
    }



    @Lock(LockType.WRITE)
    private void maintainCache() {
        File[] files = Config.getInstance().workingDirectory.toFile().listFiles();
        Arrays.sort(files != null ? files : new File[0], (a, b) -> Long.compare(a.lastModified(), b.lastModified()));

        long filesTotalSize = 0;
        for (File file : files) {
            if (file.isFile()) {
                filesTotalSize += file.length();
                cacheFileList.add(file);
            }
        }

        long allowedSpace = Config.getInstance().getAllowedSpaceMbytes() * 1024 * 1024L;

        for (File file : files) {
            if (filesTotalSize > allowedSpace) {
                if (file.isFile() && isOurExtension(file.getName())) {
                    log.info("Deleting file:" + file.getName());
                    filesTotalSize -= file.length();
                    cacheFileList.remove(file);
                    if (!file.delete()) {
                        log.info("Failed deleting file:" + file.getName());
                        filesTotalSize += file.length();
                        cacheFileList.add(file);
                    }
                }
            } else break;
        }
    }

    private boolean isOurExtension(String fileName) {
        fileName = fileName.toLowerCase();
        for (String extension : extensions) {
            if(fileName.endsWith(extension) && fileName.contains("hashcode")) {
                return true;
            }
        }
        return false;
    }
}