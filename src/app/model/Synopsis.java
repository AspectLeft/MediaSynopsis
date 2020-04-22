package app.model;

import javafx.scene.image.Image;

import java.util.List;

public class Synopsis {
    // The information needed for navigating from the synopsis image
    public static class MediaCoordinate {
        int x, y;
        public Media media;
        public double time;

        // @param time : the percentage position of a video
        MediaCoordinate(int x, int y, Media media, double time) {
            this.x = x;
            this.y = y;
            this.media = media;
            this.time = time;
        }
    }

    public Image synopsisImage;
    List<MediaCoordinate> coordinateList;

    public Synopsis(final Image synopsisImage, final List<MediaCoordinate> coordinateList) {
        this.synopsisImage = synopsisImage;
        this.coordinateList = coordinateList;
    }

    public MediaCoordinate closestMedia(int x, int y) {
        if (coordinateList.isEmpty()) return null;
        MediaCoordinate best = coordinateList.get(0);
        int minDist = dist(x, y, best.x, best.y);
        int d;
        for (MediaCoordinate coordinate: coordinateList) {
            d = dist(x, y, coordinate.x, coordinate.y);
            if (d < minDist) {
                best = coordinate;
                minDist = d;
            }
        }
        return best;
    }

    private static int dist(int x1, int y1, int x2, int y2) {
        return square(x1 - x2) + square(y1 - y2);
    }

    private static int square(int v) {
        return v * v;
    }
}
