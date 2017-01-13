package ru.pushkarev.LogsSearcher.schedule;



import ru.pushkarev.LogsSearcher.utils.Config;

import javax.ejb.*;
import java.util.logging.Logger;

@Singleton
public class Scheduler {
    private static Logger log = Logger.getLogger(Scheduler.class.getName());

    @EJB
    private CacheService cacheService;

    @Lock(LockType.READ)
    @Schedule(minute = "*/2", hour="*", persistent = false)
    public void cacheSchedule() {
        cacheService.cacheMonitor();
    }

    @Lock(LockType.READ)
    @Schedule(minute = "*/1", hour="*", persistent = false)
    public void configAutoReloadSchedule() {
        Config.getInstance().reload();
    }
}