package ru.pushkarev.LogsSearcher.utils;

import ru.pushkarev.LogsSearcher.type.Searcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class OsUtils {
    private static Logger log = Logger.getLogger(OsUtils.class.getName());

    public static final int runnningOS;
    public static final int WINDOWS = 0;
    public static final int UNIX = 1;

    static {
        runnningOS = determineOS();
    }


    private OsUtils() {
    }

    public static Map<File, List<Integer>> runAndParseOutput(String cmd) {
        Map<File, List<Integer>> filesWithHits = new HashMap<>();

        Process process = null;
        try {
            process = Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("Parsing output for cmd:" + cmd);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Stopwatch stopwatch = new Stopwatch();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            if(!reader.ready()) {
                log.info("findstr returned nothing. cmd:" + cmd);
                return filesWithHits;
            }

            String line;
            File file = null;
            List<Integer> lineNumbers = new ArrayList<>();
            String currentFilePath = null;
            String filePath = null;
            int i = 0;
            while ((line = reader.readLine())!= null) {
                Integer lineNumber = -1;

                // all before : is a filepath, after and before next : is line number
                try {
                    filePath = line.substring(0, line.indexOf(':', 3));  // begin index is > 2 to skip match of "C:\"
                } catch (IndexOutOfBoundsException e) {
                    log.log(Level.WARNING, "findstr returned unexpected string:" + line + "\n" + e.getMessage() + e);
                }
                try {
                    lineNumber = Integer.parseInt(line.substring(filePath.length()+1, line.indexOf(':', filePath.length()+1)));
                } catch (Exception e) {
                    log.log(Level.WARNING, "Failed to parse filename and line number from result line of findstr:" + line + "\n" + e.getMessage() + e);
                }

                if(file == null) {
                    file = new File(filePath);
                    currentFilePath = file.getAbsolutePath();
                }
                // After filename changed - adding File and its lineNumbers list to result Map
                if(!currentFilePath.equals(filePath)) {
                    filesWithHits.put(file, lineNumbers);

                    file = new File(filePath);
                    currentFilePath = file.getAbsolutePath();
                    lineNumbers = new ArrayList<>();
                }
                // adding line numbers while filename is the same
                lineNumbers.add(lineNumber);

                // at the end of cycle, put last list
                if (!reader.ready()) {
                    filesWithHits.put(file, lineNumbers);
                }
                if(++i % 1000000 == 0) {
                    log.info("Still parsing... line :" + i);
                }
            }

        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage() + e);

        }
        log.fine(" took " + stopwatch.stop());

        return filesWithHits;
    }

    public static String buildFindstrCmd(String searchText, Set<File> fileList, boolean isCaseSensitive, boolean isRegExp) {
        String cmd = "findstr /N /P /offline";

        if(!isCaseSensitive) {
            cmd += " /I ";  // case sensitive off
        }

        if(isRegExp) {
            String pattern = RegExUtils.convertToFindstrFormat(searchText);
            cmd += " /R /C:\"" + pattern + "\"";
        } else {
            // /L - use as literal string
            cmd += " /L /C:\"" + searchText + "\"";
        }

        for (File file : fileList) {
            //
            try {
                cmd += " \"" + file.getCanonicalPath() + "\"";
            } catch (IOException e) {
                log.log(Level.WARNING, "Cannot get canonical domainPath for file:" + file.getName() + "\n" + e.getMessage() + e);
            }
        }
        return cmd;
    }

    public static String buildGrepCmd() {
        return "";
    }

    private static int determineOS() {
        if(System.getProperty("os.name").toLowerCase().contains("windows")) {
            return WINDOWS;
        } else {
            return UNIX;
        }
    }


}
