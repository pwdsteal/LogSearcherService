package ru.pushkarev.LogsSearcher.type;

import ru.pushkarev.LogsSearcher.utils.DateParser;
import ru.pushkarev.LogsSearcher.utils.OsUtils;
import ru.pushkarev.LogsSearcher.utils.Stopwatch;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Logger;

import static ru.pushkarev.LogsSearcher.utils.DateParser.toXMLGregorianCalendar;
import static ru.pushkarev.LogsSearcher.utils.OsUtils.runAndParseOutput;

public class Searcher {
    private static Logger log = Logger.getLogger(Searcher.class.getName());

    private Request request;
    private DateParser dateParser;

    public Searcher(Request query) {
        this.request = query;
    }


    public Response run() {
        Stopwatch stopwatch = new Stopwatch();

        Response response = new Response();
        for (Server server : request.getTargetServers()) {
            log.warning("Search for server " + server.getName());
            Set<File> logFilesList = selectFilesByDate(server.getLogFilesList());
            if (!logFilesList.isEmpty()) {
                ServerElement serverElement = new ServerElement(server.getName(), readBlocks(searchByOS(logFilesList)));
                response.addServerElement(serverElement);
            }
        }
        log.warning("Searching complete.");
        response.setSearchTime(stopwatch.getDuration());
        return response;
    }

    private Map<File, List<Integer>> searchByOS(Set<File> fileList) {
        String cmd;

        if(OsUtils.runnningOS == OsUtils.WINDOWS) {
            cmd = OsUtils.buildFindstrCmd(request.getSearchString(), fileList, request.isCaseSensitive(), request.isRegExp());
        } else {
            cmd = OsUtils.buildGrepCmd();
        }

        return runAndParseOutput(cmd);
    }


    private Set<File> selectFilesByDate(Set<File> fileList) {
        if (request.getDateIntervals().isEmpty()) {
            return fileList;
        }

        Set<File> selectedFileList = new HashSet<>();
        for (File file : fileList) {
            BasicFileAttributes attr = null;
            try {
                attr = Files.readAttributes(Paths.get(file.getAbsolutePath()), BasicFileAttributes.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Date creationTime = new Date(attr.creationTime().toMillis());
            Date modifiedTime = new Date(attr.lastModifiedTime().toMillis());
            for(DateInterval dateInterval : request.getDateIntervals()) {
                if (dateInterval.getStart().before(modifiedTime) &&
                        dateInterval.getEnd().after(creationTime)) {
                    selectedFileList.add(file);
                    break;
                }
            }
        }
        return selectedFileList;
    }


    private Set<LogBlock> readBlocks(Map<File, List<Integer>> filesWithHits) {
        Map<File, List<Integer>> blockStartList = runAndParseOutput(OsUtils.buildFindstrCmd("####", filesWithHits.keySet(), true, false));
        Set<LogBlock> logBlocks = new LinkedHashSet<>();

        for (File file : filesWithHits.keySet()) {
            log.warning("reading blocks from file " + file.getName());
            Stopwatch stopwatch = new Stopwatch();

            Set<Block> resultBlocks = Block.getBlocksToRead(filesWithHits.get(file), blockStartList.get(file));
            log.warning(resultBlocks.size() + " blocks selected");

            dateParser = new DateParser();  // create new instance for each file. Each Log file may have different time format
            try (BufferedReader reader = new BufferedReader( new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

                String line;
                int lineNumber = 1;  // findstr starts count at 1
                int currentHit = 0;

                for (Block block : resultBlocks) {
                    if(currentHit++ > request.getMaxMatches()) {
                        log.warning("stopped at max matches " + ((100.0/resultBlocks.size()*logBlocks.size())) + " % " );
                        break;
                    }
                    Date block_date = null;
                    // skip lines
                    while(lineNumber < block.getStart()) {
                        reader.readLine();
                        lineNumber++;
                    }
                    // read block
                    StringBuilder buffer = new StringBuilder();
                    try {
                        while (lineNumber <= block.getEnd() && (line = reader.readLine()) != null) {
                            if(lineNumber++ == block.getStart()) {
                                block_date = extractDateFromBlock(line);
                            }

                            // if block doesn't match dates range, skip it
                            if(!isDateInInterval(block_date)) {
                                break;
                            }
                            // continue build block
                            if(buffer.length() > 0) {
                                buffer.append(line);
                                buffer.append("\n");
                            } else {
                                buffer.append(line);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    logBlocks.add(new LogBlock(toXMLGregorianCalendar(block_date), buffer.toString()));
                }
            } catch (IOException e) {
                log.severe(" Error at reading file " + e.getMessage() + e);
            }
            log.warning("Readed " + logBlocks.size() + " blocks took " + stopwatch.stop());
        }
        return logBlocks;
    }

    private Date extractDateFromBlock(String text) {
        Date date = null;
        String dateTimeString = null;
        try {
            dateTimeString = text.substring(text.indexOf('<')+1, text.indexOf('>'));
        } catch (Exception e) {
            log.warning("Date extracting error :" + text + e.getMessage());
        }

        try {
            date = dateParser.parse(dateTimeString);
        } catch (ParseException e) {
            log.warning("Date parsing error :" + e.getMessage());
        }
        return date;
    }

    private boolean isDateInInterval(Date date) {
            for (DateInterval dateInterval : request.getDateIntervals()) {
                if (date.after(dateInterval.getStart()) && date.before(dateInterval.getEnd())) {
                    return true;
                }
            }
        return false;
    }

}
