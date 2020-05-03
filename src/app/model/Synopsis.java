package app.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.io.*;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class Synopsis {
    // The information needed for navigating from the synopsis image
    public static class MediaCoordinate {
        private int x, y;
        public double time;
        public String fileName;

        public transient Media media;
        // @param time : the percentage position of a video
        MediaCoordinate(int x, int y, Media media, double time) {
            this.x = x;
            this.y = y;
            this.media = media;
            this.time = time;
            this.fileName = media.getFileName();
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

    public void save(File dir) {
        if (!dir.isDirectory()) return;

        try {
            File imageFile = new File(dir.getPath() + "/synopsis.jpg/");
            if (!imageFile.exists()) imageFile.createNewFile();
            ImageIO.write(SwingFXUtils.fromFXImage(this.synopsisImage, null), "png", imageFile);

            FileWriter jsonFile = new FileWriter(dir.getPath() + "/.coordinate");
            Gson gson = new Gson();
            String json = gson.toJson(this.coordinateList);
            System.out.println(json);
            jsonFile.write(json);
            jsonFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Synopsis load(File dir, Map<String, Media> mediaMap) {
        if (!dir.isDirectory()) return null;
        try {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<MediaCoordinate>>(){}.getType();
            JsonReader jsonReader = new JsonReader(new FileReader(dir.getPath() + "/.coordinate"));
            List<MediaCoordinate> coordinateList = gson.fromJson(jsonReader, listType);
            for (MediaCoordinate coordinate: coordinateList) {
                coordinate.media = mediaMap.get(coordinate.fileName);
            }
            Image synopsisImage = new Image(dir.toURI().toString() + "/synopsis.jpg");
            return new Synopsis(synopsisImage, coordinateList);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
