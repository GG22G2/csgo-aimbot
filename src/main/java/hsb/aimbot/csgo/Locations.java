package hsb.aimbot.csgo;

import hsb.aimbot.csgo.utils.Time;

import java.lang.foreign.MemorySegment;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * @author
 * @date 2022/2/3 17:38
 */
public class Locations {

    ArrayBlockingQueue<CaptureRecord> detectQueue = new ArrayBlockingQueue<>(1);

    volatile Location[] locations = new Location[20];
    volatile int count = 0;
    volatile long ignoreCount;
    volatile long ignoreTime;

    public volatile long captureTime = -1;

    public Locations() {
        for (int i = 0; i < locations.length; i++) {
            locations[i] = new Location();
        }
    }

    public static class CaptureRecord {
        public int time;
        public MemorySegment gpuPoint;

        public boolean detectTarget;

        public CaptureRecord(MemorySegment gpuPoint, int time) {
            this.time = time;
            this.gpuPoint = gpuPoint;
            this.detectTarget = false;
        }
    }

    public void addCapture(MemorySegment address, int captureTime) {
        try {
            detectQueue.put(new CaptureRecord(address, captureTime));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public CaptureRecord nextCapture() throws InterruptedException {
        return detectQueue.take();
    }



    public synchronized void update(int screenX, int screenY, float[] rects, long captureTime) {
        if (ignoreCount > 0) {
            return;
        }
        int t = 0;
        for (int i = 0, j = 0; j < 120; i++, j += 6) {
            if (rects[j + 2] <= 0) {
                break;
            }
            float conf = rects[j + 4];
            if (conf < 0.7) {
                continue;
            }
            t++;
            Location location = locations[i];
            location.x = (int) ((screenX + rects[j]) * Config.scale);
            location.y = (int) ((screenY + rects[j + 1]) * Config.scale);
            location.width = (int) (rects[j + 2] * Config.scale);
            location.height = (int) (rects[j + 3] * Config.scale);
            location.conf = rects[j + 4];
            location.classId = (int) rects[j + 5];
            location.centerX = (location.x + location.width / 2);
            location.centerY = (location.y + location.height / 2);
        }
        count = t;
        this.captureTime = captureTime;
    }

    public synchronized Location minDistance(int x, int y) {
        int len = count;
        double min = Double.MAX_VALUE;
        Location minL = null;
        for (int i = 0; i < len; i++) {
            Location location = locations[i];
            double v = Math.pow(location.x - x, 2) + Math.pow(location.y - y, 2);
            if (v < min) {
                min = v;
                minL = location;
            }
        }
        return minL;
    }


    public synchronized Location minDistance(int x, int y, int classId) {
        if (classId == Config.CT_AND_T) {
            return minDistance(x, y);
        }
        int len = count;
        double min = Double.MAX_VALUE;
        Location minL = null;
        for (int i = 0; i < len; i++) {
            Location location = locations[i];
            if (location.classId != classId) {
                continue;
            }
            double v = Math.pow(location.x - x, 2) + Math.pow(location.y - y, 2);
            if (v < min) {
                min = v;
                minL = location;
            }
        }
        return minL;
    }

    public Location[] getLocations(){
        return locations;
    }

    public int getCount() {
        return count;
    }

    public synchronized void clear() {
        count = 0;
    }


    public boolean isIgnore(long curTime, boolean update) {
        if (curTime < ignoreTime) {
            return true;
        }
        if (ignoreCount > 0) {
            if (update) {
                ignoreCount--;
            }
            return true;
        }

        return false;
    }


    public synchronized void ingore(int ignoreCount) {
        this.ignoreCount = ignoreCount;
        ignoreTime = Time.getTime();
    }


}
