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


    private OsUtils() {}

    /**
     * Run cmd that searches text pattern in specified files and reads output
     * @return HashMap with files contains List of matching lines
     *  */
    public static Map<File, List<Integer>> runAndParseOutput(List<String> cmd) {
        Map<File, List<Integer>> filesWithHits = new HashMap<>();

        if (null == cmd) {
            log.info("returned empty filesWithHits");
            return filesWithHits;
        }

        Process process = null;
        try {
            process = Runtime.getRuntime().exec(cmd.toArray(new String[0]));
        } catch (IOException e) {
            log.log(Level.WARNING, "Failed run cmd" + e);
        }
        log.info("Parsing output for cmd:" + cmd);
//        try {
//            log.info("Wait for exit value " + process.waitFor());
//        } catch (InterruptedException e) {
//            log.info("Wait for exception ");
//        }


        Stopwatch stopwatch = new Stopwatch();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;

            while ((line = errorReader.readLine())!= null) {
                log.info("Error Stream:");
                log.info(line);
            }

            File file = null;
            List<Integer> lineNumbers = new ArrayList<>();
            String currentFilePath = null;
            String filePath = null;
            int i = 0;

            while ((line = reader.readLine())!= null) {
                Integer lineNumber = null;

                // all before : is a filepath, after and before next : is line number
                try {
                    filePath = line.substring(0, line.indexOf(':', 3));  // begin index is > 2 to skip match of "C:\"
                } catch (IndexOutOfBoundsException e) {
                    log.log(Level.WARNING, "unexpected string:" + line + "\n" + e.getMessage() + e);
                }
                try {
                    lineNumber = Integer.parseInt(line.substring(filePath.length()+1, line.indexOf(':', filePath.length()+1)));
                } catch (Exception e) {
                    log.log(Level.WARNING, "Failed to parse filename and line number from result line :" + line + "\n" + e.getMessage() + e);
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
                if (null != lineNumber) {
                    lineNumbers.add(lineNumber);
                }

                // at the end of cycle, put last list
                if (!reader.ready()) {
                    filesWithHits.put(file, lineNumbers);
                }
                if(++i % 1000000 == 0) {
                    log.info("Still parsing... line :" + i);
                }
            }
//            process.waitFor();

        } catch (IOException e) {
            log.log(Level.SEVERE, "Exception at reading process output. "  + e.getMessage() + e);

        }
        log.info(" took " + stopwatch.stop());

        return filesWithHits;
    }

    public static String buildFindstrCmd(String searchString, Set<File> fileList, boolean isCaseSensitive, boolean isRegExp) {
        // cannot search with no files
        if (fileList.isEmpty()) {
            return null;
        }

        String cmd = "findstr /N /P /offline";

        if(!isCaseSensitive) {
            cmd += " /I ";  // case sensitive off
        }

        if(isRegExp) {
            String pattern = RegExUtils.convertToFindstrFormat(searchString);
            cmd += " /R /C:\"" + pattern + "\"";
        } else {
            // /L - use as literal string
            cmd += " /L /C:\"" + searchString + "\"";
        }

        for (File file : fileList) {
            //
            try {
                cmd += " \"" + file.getCanonicalPath() + "\"";
            } catch (IOException e) {
                log.log(Level.WARNING, "Cannot get canonical domainPath for file:" + file.getName() + "\n" + e.getMessage() + e);
            }
        }

        // add empty filepath to avoid providing only one link which cause findstr not to print filename in in the output
        if (fileList.size() == 1) {
            cmd += " \"\"";
        }

        return cmd;
    }


    public static List<String> buildGrepCmdAlt(String searhString, Set<File> fileList, boolean isCaseSensitive) {
        if (fileList.isEmpty()) {
            return null;
        }

        List<String> cmdList = new ArrayList<>();
        cmdList.add("grep" );
        cmdList.add("-n"); // print line numbers
        cmdList.add("-o"); // print only matching word instead of full string
        cmdList.add("-H"); // print full filename path

        if (!isCaseSensitive) {
            cmdList.add("-i"); // case sensitive off
        }
        cmdList.add('"' + searhString + '"');

        // add files
        for (File file : fileList) {
            try {
                cmdList.add(file.getCanonicalPath());
            } catch (IOException e) {
                log.log(Level.WARNING, "Cannot get canonical domainPath for file:" + file.getName() + "\n" + e.getMessage() + e);
            }
        }

        return cmdList;
    }

    private static int determineOS() {
        if(System.getProperty("os.name").toLowerCase().contains("windows")) {
            return WINDOWS;
        } else {
            return UNIX;
        }
    }

    public static List<String> buildCmd(String searchString, Set<File> fileList, boolean isCaseSensitive, boolean isRegExp) {
        List<String> cmd = null;

        if(runnningOS == WINDOWS) {
            cmd = new ArrayList<>();
            cmd.add(buildFindstrCmd(searchString, fileList, isCaseSensitive, isRegExp));
        } else {
            cmd = buildGrepCmdAlt(searchString, fileList, isCaseSensitive);
        }

        return cmd;
    }


}
