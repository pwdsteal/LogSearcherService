/**
 * Created by Opushkarev on 23.12.2016.
 */
public class Stopwatch {

    private long startTime;

    public Stopwatch() {
        start();
    }

    public void start() {
        startTime = System.currentTimeMillis();
    }

    public long getDuration() {
        return System.currentTimeMillis() - startTime;
    }

    public String stop() {
        return getDuration() + " ms";
    }
}
