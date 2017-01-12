package ru.pushkarev.LogsSearcher.schedule;



import javax.ejb.*;
import java.util.logging.Logger;

@Singleton
public class Scheduler {
    private static Logger log = Logger.getLogger(Scheduler.class.getName());

    @EJB
    private FileCleaner fileCleaner;

    @Lock(LockType.READ)
    @Schedule(minute = "*", hour="*", persistent = false)
    public void atSchedule() {
        fileCleaner.ClearCache();
    }
}