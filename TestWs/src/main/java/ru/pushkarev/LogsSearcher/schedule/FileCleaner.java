package ru.pushkarev.LogsSearcher.schedule;

import ru.pushkarev.LogsSearcher.type.Request;
import ru.pushkarev.LogsSearcher.utils.Config;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

@Singleton
public class FileCleaner {
    private static Logger log = Logger.getLogger(FileCleaner.class.getName());

    private static final String[] extensions = {".xml",".html",".pdf",".rtf", ".doc"};
    private AtomicBoolean busy = new AtomicBoolean(false);

    private static Set<File> cacheFileList = new HashSet<>();

    @Lock(LockType.READ)
    public void ClearCache() {
        if (!busy.compareAndSet(false, true)) {
            return;
        }
        log.info("Checking cache size");
        monitorCache();
        busy.set(false);

    }

    @Lock(LockType.READ)
    public static File tryGetXmlFromCache(Request request) {

        List<File> matchList = new ArrayList<>();
        for (File file : cacheFileList) {
            String fileName = file.getName();
            Integer hashcode = request.hashCode();

            // find all files with hashcode
            if (fileName.contains(hashcode.toString())) {
                matchList.add(file);
            }

            // find file with required extension (.pdf ...)
            for (File matchFile : matchList) {
                if (matchFile.getName().toLowerCase().endsWith(request.getOutputFormat())) {
                    return matchFile;
                }
            }

           // try to find with .xml extension
            for (File matchFile : matchList) {
                if (matchFile.getName().toLowerCase().endsWith(".xml")) {
                    return matchFile;
                }
            }

        }

        return null;
    }


//    @Schedule(minute = "*", hour="*", persistent = false)
    private void monitorCache() {
        File[] files = Config.getInstance().workingDirectory.toFile().listFiles();

        Arrays.sort(files, new Comparator<File>(){
            public int compare(File f1, File f2)
            {
                return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
            } });

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
                if (file.isFile()) {
                    log.info("Deleting file:" + file.getName());
                    filesTotalSize -= file.length();
                    if (!file.delete()) {
                        log.info("Failed deleting file:" + file.getName());
                        filesTotalSize += file.length();
                    }
                }
            } else break;
        }
    }

    private boolean isOurExtension(String fileName) {
        fileName = fileName.toLowerCase();
        for (String extension : extensions) {
            if(fileName.contains(extension) && fileName.contains("hashcode")) {
                return true;
            }
        }
        return false;
    }
}