package org.example.csgo;

import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * @author
 * @date 2022/2/13 12:54
 */
public class Tracks {

    public static class Track {
        public long time;
        public int x;
        public int y;
        public long button;

        public Track() {
        }

        public Track(long time, int x, int y, long ulButtons) {
            this.time = time;
            this.x = x;
            this.y = y;
            button = ulButtons;
        }

        @Override
        public String toString() {
            return "Track{" +
                    "time=" + time +
                    ", x=" + x +
                    ", y=" + y +
                    '}';
        }
    }


    final private int count = 300;

    AtomicReferenceArray<Track> xyBuffer = new AtomicReferenceArray<Track>(count);

    volatile int updateCount = 0;
    int position = 0;
    volatile int first;

    public void updateTrack(long time, int x, int y, long ulButtons) {
        xyBuffer.set(position, new Track(time, x, y, ulButtons));
        synchronized (this) {
            first = position;
            updateCount++;
        }
        position += 1;
        if (position == count) {
            position = 0;
        }

    }

    public int[] getTrack() {
        int[] xyTrack = new int[count];
        for (int i = 0, j = first; i < count; i += 2) {
            Track track = xyBuffer.get(j);
            xyTrack[i] = track.x;
            xyTrack[i + 1] = track.y;
            j += 1;
            if (j == count) {
                j = 0;
            }
        }
        return xyTrack;
    }


    public Track lastTrack() {

        Track track;
        int x = 0, y = 0;
        int c = 0;
        int first = 0;
        c = updateCount;
        if (c > 0) {
            synchronized (this) {
                c = updateCount;
                first = this.first;
                updateCount = 0;
            }
        }


        track = xyBuffer.get(first);
        x = track.x;
        y = track.y;

        if (c == 0) {
            x = 0;
            y = 0;
            return new Track(0, x, y, 0);
        } else if (c > 1) {
            int i = first - 1;
            c--;
            while (c > 0) {

                if (i < 0) {
                    i = count - 1;
                }
                Track temp = xyBuffer.get(i);
                x += temp.x;
                y += temp.y;
                i--;
                c--;
            }
        }


        return new Track(track.time, x, y, track.button);
    }


    public Track[] historyTracks(int hCount) {
        Track[] tracks = new Track[hCount];

        for (int i = 0, j = first; i < hCount; i++) {
            tracks[i] = xyBuffer.get(j);
            j--;
            if (j == -1) {
                j = count - 1;
            }

        }
        return tracks;
    }


    public Track moveDistanceFromTime(long startTime) {
        int count = this.count;
        Track last = xyBuffer.get(first);

        int first;
        synchronized (this) {
            first = this.first;
            updateCount = 0;
        }

        int len = Math.abs((last.time - startTime)) > 40 ? 40 : (int) (Math.abs((last.time - startTime)));

        int i = first - len;

        if (i < 0) {
            i = count + i;
        }

        int xMove = 0;
        int yMove = 0;

        while (true) {
            Track t = xyBuffer.get(i);
            if (t != null) {
                if (t.time > startTime) {
                    break;
                }
            }
            i++;
            if (i == count) {
                i = 0;
            }
        }

        while (i != first) {
            Track t = xyBuffer.get(i++);
            xMove += t.x;
            yMove += t.y;
            if (i == count) {
                i = 0;
            }
        }


      //  return new Track(last.time, xMove, yMove, last.button);
        return new Track(0, 0, 0, 0);
    }


}
