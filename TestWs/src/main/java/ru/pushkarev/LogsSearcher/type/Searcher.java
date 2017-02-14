package ru.pushkarev.LogsSearcher.type;

import ru.pushkarev.LogsSearcher.utils.*;

import javax.validation.constraints.NotNull;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static ru.pushkarev.LogsSearcher.utils.DateParser.toXMLGregorianCalendar;
import static ru.pushkarev.LogsSearcher.utils.OsUtils.*;

public class Searcher {
    private static Logger log = Logger.getLogger(Searcher.class.getName());

    private Request request;
    private DateParser dateParser;
    private File xmlFile;

    public File getXmlFile() { return xmlFile; }


    public Searcher(Request request) {
        this.request = request;
        xmlFile = Config.getInstance().workingDirectory.resolve(request.getFilename() + ".xml").toFile();
    }

    // we assume that all checks for cached file are made before. So if we run searcher - there is no cached file, we need to do a search.
    public Response run() {
        Stopwatch stopwatch = new Stopwatch();
        Response response = new Response();
        for (Server server : Domain.getTargetServers(request)) {
            log.fine("Searching for server " + server.getName());
            Set<File> logFilesList = selectFilesByDate(server.getLogFilesList());
            if (!logFilesList.isEmpty()) {
                ServerElement serverElement = new ServerElement(server.getName(), readBlocks(searchByOS(logFilesList)));
                if (!serverElement.logBlocks.isEmpty()) {
                    response.addServerElement(serverElement);
                }
            }
        }
        log.info("Searching in files done. " + stopwatch.stop());

        // save to cache
        FileConverter.writeResponseToXML(response, xmlFile);

        response.setSearchTime(stopwatch.getDuration());
        return response;
    }


    private Map<File, List<Integer>> searchByOS(@NotNull Set<File> fileList) {
        return runAndParseOutput(buildCmd(request.getSearchString(), fileList, request.isCaseSensitive(), request.isRegExp()));
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


    private Set<LogBlock> readBlocks(@NotNull Map<File, List<Integer>> filesWithHits) {
        Set<LogBlock> logBlocks = new LinkedHashSet<>();

        if (filesWithHits.isEmpty()) {
            return logBlocks;
        }

        Map<File, List<Integer>> blockStartList = runAndParseOutput(OsUtils.buildCmd("^####", filesWithHits.keySet(), true, true));

        for (File file : filesWithHits.keySet()) {
            log.fine("reading blocks from file " + file.getName());
            Stopwatch stopwatch = new Stopwatch();

            Set<Block> resultBlocks = Block.getBlocksToRead(filesWithHits.get(file), blockStartList.get(file));
            log.finer(resultBlocks.size() + " blocks selected");

            dateParser = new DateParser();  // create new instance for each file. Each Log file may have different time format
            try (BufferedReader reader = new BufferedReader( new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                String line;
                int lineNumber = 1;  // findstr starts count at 1
                int currentHit = 0;

                for (Block block : resultBlocks) {
                    if(!request.isFileRequested() && currentHit++ > request.getMaxMatches()) {
                        log.info("stopped at max matches " + ((100.0/resultBlocks.size()*logBlocks.size())) + " % " );
                        break;
                    }
                    Date block_date = null;
                    // skip lines
                    while(lineNumber < block.getStart()) {
                        reader.readLine();
                        lineNumber++;
                    }
                    // read block
                    StringBuilder buffer = null;
                    while (lineNumber <= block.getEnd() && (line = reader.readLine()) != null) {
                        if(lineNumber++ == block.getStart()) {
                            block_date = extractDateFromBlock(line);
                            // if block doesn't match dates range, skip it
                            if(isDateInInterval(block_date)) {
                                buffer = new StringBuilder(line.length() + 4);
                            } else break;
                        }
                        // continue build block
                        if(buffer.length() > 0) {
                            buffer.append(line);
                            buffer.append("\n");
                        } else {
                            buffer.append(line);
                        }
                    }
                    if (buffer != null) {
                        logBlocks.add(new LogBlock(toXMLGregorianCalendar(block_date), buffer.toString()));
                    }
                }
            } catch (IOException e) {
                log.log(Level.WARNING, " Error at reading " + e.getMessage() + e);
            }
            log.info("Readed " + logBlocks.size() + " blocks from " + file.getName() + " took " + stopwatch.stop());
        }
//        System.gc();
        return logBlocks;
    }

    private Date extractDateFromBlock(String text) {
        Date date;
        String dateTimeString;

        int start = text.indexOf('<') + 1;
        int end = text.indexOf('>');
        if(end != -1) {
            dateTimeString = text.substring(start, end);
        }
        else {
            log.log(Level.WARNING, "Date extracting error from :" + text);
            return null;
        }


        try {
            date = dateParser.parse(dateTimeString);
        } catch (ParseException e) {
            log.log(Level.WARNING, "Date parsing error :" + dateTimeString + e);
            return null;
        }
        return date;
    }

    private boolean isDateInInterval(Date date) {
        if (date == null) return false;
        for (DateInterval dateInterval : request.getDateIntervals()) {
            if (dateInterval.getStart().before(date) && dateInterval.getEnd().after(date)) {
                return true;
            }
        }
        return false;
    }

}
