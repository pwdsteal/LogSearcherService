package ru.pushkarev.LogsSearcher.utils;


import javax.validation.constraints.NotNull;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class OsUtils {
    private static final Logger log = Logger.getLogger(OsUtils.class.getName());

    enum OStype {
        WINDOWS, UNIX
    }

    public static final OStype runnningOS = determineOS();

    private OsUtils() {}

    /**
     * Run cmd that searches text pattern in specified files and reads output
     * @return HashMap with files contains List of matching lines
     *  */
    public static Map<File, List<Integer>> runAndParseOutput(ProcessBuilder processBuilder) {
        Map<File, List<Integer>> filesWithHits = new HashMap<>();

        Process process = null;
        try {
            process = processBuilder.start();
        } catch (IOException e) {
            log.log(Level.WARNING, "Failed run cmd : " + e);
        }
        log.info("Parsing output for cmd:" + processBuilder.command());


        Stopwatch stopwatch = new Stopwatch();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;

            File file = null;
            List<Integer> lineNumbers = new ArrayList<>();
            String currentFilePath = null;
            String filePath = null;
            int i = 0;
            while ((line = reader.readLine()) != null) {
                Integer lineNumber = null;

                // all before : is a filepath, after and before next : is line number
                try {
                    filePath = line.substring(0, line.indexOf(':', 3));  // begin index is > 2 to skip match of "C:\"
                } catch (IndexOutOfBoundsException e) {
                    log.log(Level.WARNING, "unexpected string:" + line + "\n" + e);
                }
                try {
                    lineNumber = Integer.valueOf(line.substring(filePath.length()+1, line.indexOf(':', filePath.length()+1)));
                } catch (Exception e) {
                    log.log(Level.WARNING, "Failed to parse filename and line number from result line :" + line + "\n" + e);
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
        log.fine(" took " + stopwatch.stop());

        log.info("files with hits: " + filesWithHits.size());
        return filesWithHits;
    }

    public static List<String> buildFindstrCmd(@NotNull String searchString, @NotNull Set<File> fileList, boolean isCaseSensitive, boolean isRegExp) {
        List<String> cmdList = new ArrayList<>();

        cmdList.add("findstr");
        cmdList.add("/OFF");  // search files
        cmdList.add("/N");  // print line number

        if(!isCaseSensitive) {
            cmdList.add("/I");  // case sensitive off
        }

        if(isRegExp) {
            String pattern = RegExUtils.convertToFindstrFormat(searchString);
            cmdList.add("/R");  // use as a RegExp
            cmdList.add("/C:" + '"' + pattern + '"');  // TODO Escape " character in pattern (cause in a bad cmd line ~ like sql injection)
        } else {
            cmdList.add("/L");  // use as literal string
            cmdList.add("/C:" + '"' + searchString + '"');
        }

        for (File file : fileList) {
            try {
                cmdList.add(file.getCanonicalPath());
            } catch (IOException e) {
                log.log(Level.WARNING, "Cannot get canonical domainPath for file:" + file.getName() + "\n" + e.getMessage() + e);
            }
        }

        // add empty filepath to avoid providing only one link which cause findstr not to print filename in in the output
        if (fileList.size() == 1) {
            cmdList.add("\"\"");
        }

        return cmdList;
    }


    public static List<String> buildGrepCmd(String searhString, Set<File> fileList, boolean isCaseSensitive) {
        List<String> cmdList = new ArrayList<>();

        cmdList.add("grep" );
        cmdList.add("-n"); // print line numbers
        cmdList.add("-o"); // print only matching word instead of full string
        cmdList.add("-H"); // print full filename path

        if (!isCaseSensitive) {
            cmdList.add("-i"); // case sensitive off
        }

        cmdList.add( searhString  );

        for (File file : fileList) {
            try {
                cmdList.add(file.getCanonicalPath());
            } catch (IOException e) {
                log.log(Level.WARNING, "Cannot get canonical domainPath for file:" + file.getName() + "\n" + e.getMessage() + e);
            }
        }

        return cmdList;
    }

    private static OStype determineOS() {
        if(System.getProperty("os.name").toLowerCase().contains("windows")) {
            return OStype.WINDOWS;
        } else {
            return OStype.UNIX;
        }
    }

    public static ProcessBuilder buildCmd(String searchString, Set<File> fileList, boolean isCaseSensitive, boolean isRegExp) {
        ProcessBuilder processBuilder = null;
        switch (runnningOS) {
            case WINDOWS:
                processBuilder = new ProcessBuilder(buildFindstrCmd(searchString, fileList, isCaseSensitive, isRegExp));
                break;
            case UNIX:
                processBuilder = new ProcessBuilder(buildGrepCmd(searchString, fileList, isCaseSensitive));
                break;
        }
        return processBuilder;
    }


}
