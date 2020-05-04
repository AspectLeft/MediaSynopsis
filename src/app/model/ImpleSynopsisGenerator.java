package app.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import app.controller.SynopsisController;
import app.model.Synopsis;
//import app.util.ImageTrasform;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;


public class ImpleSynopsisGenerator extends SynopsisGeneratorBase {
    private class Position {
        int x;
        int y;

        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private class FrameMediaMatch {
        private Media media;
        private double time;
        private Image frame;
        private boolean isImage;

        public FrameMediaMatch(Media media,Image frame) {
            this.media = media;
            this.frame = frame;
            isImage = true;
        }

        public FrameMediaMatch(Media media, Image frame, double time) {
            this.media = media;
            this.frame = frame;
            this.time = time;
            isImage = false;
        }

        public Media getMedia() {
            return media;
        }

        public void setMedia(Media media) {
            this.media = media;
        }

        public double getTime() {
            return time;
        }

        public void setTime(double time) {
            this.time = time;
        }

        public Image getFrame() {
            return frame;
        }

        public void setFrame(Image frame) {
            this.frame = frame;
        }

        public boolean isImage() {
            return isImage;
        }

        public void setImage(boolean isImage) {
            this.isImage = isImage;
        }
    }

    private class SceneChangeThread implements Runnable{
        int pos;
        int frameStride;
        public SceneChangeThread(int pos,int frameStride){
            this.pos = pos;
            this.frameStride = frameStride;
        }

        @Override
        public void run() {
            sceneChange.get(pos).add(0);
            for (int i = 0; i < videoFrameList.get(pos).size()-frameStride; i = i+frameStride) {
                int temp = difference(pos, i,frameStride);

                if (temp > thereshold) {
                    System.out.println(pos+" "+i + ". " + temp);
                    sceneChange.get(pos).add(i);
                    keyFrameNum++;
                }
            }
            sceneChange.get(pos).add(videoFrameList.get(pos).size() - 1);
            keyFrameNum++;
            System.out.println("Finish");
        }
    }

    int width = 352;
    int height = 288;
    int size = 8;
    int k = 16;
    int scale = 4;
    int keyNum = 29;
    volatile int keyFrameNum = 0;
    int thereshold = 6000000;

    volatile ArrayList<ArrayList<Integer>> sceneChange = new ArrayList<>();
    ArrayList<FrameMediaMatch> keyFrame = new ArrayList<>();
    ArrayList<Media> imageList = new ArrayList<>();
    ArrayList<Media> videoList = new ArrayList<>();
    volatile ArrayList<ArrayList<Image>> videoFrameList = new ArrayList<>();


    private int[][][] readImageRGB(int video, int frame) {
        int[][][] result = new int[width][height][3];
        Image image = videoFrameList.get(video).get(frame);

        PixelReader pr = image.getPixelReader();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = pr.getColor(x, y);
                result[x][y][0] = (int) (color.getRed()*255);
                result[x][y][1] = (int) (color.getGreen()*255);
                result[x][y][2] = (int) (color.getBlue()*255);

            }
        }

        return result;

    }

    private int difference(int video, int frame,int frameStride) {
        int diff = 0;
        int[][][] frame1 = readImageRGB(video, frame);
        int[][][] frame2 = readImageRGB(video, frame + frameStride);

        for (int i = 0; i < width / size; i++) {
            for (int j = 0; j < height / size; j++) {
                int min = Integer.MAX_VALUE;

                // visited[i][j] = true;
                for (int p = i * size - k; p < i * size + k; p++) {
                    if (p < 0 || p + size >= width)
                        continue;
                    for (int q = j * size - k; q < j * size + k; q++) {
                        if (q < 0 || q + size >= height)
                            continue;
                        int count = 0;
                        // if (visited[p][q])
                        // continue;
                        for (int m = 0; m < size; m++) {
                            for (int n = 0; n < size; n++) {
                                int temp = (int) Math.sqrt(Math
                                        .pow(frame1[i * size + m][j * size + n][0] - frame2[p + m][q + n][0], 2)
                                        + Math.pow(frame1[i * size + m][j * size + n][1] - frame2[p + m][q + n][1], 2)
                                        + Math.pow(frame1[i * size + m][j * size + n][2] - frame2[p + m][q + n][2], 2));
                                if (count + temp < 0)
                                    count = Integer.MAX_VALUE;
                                else
                                    count += temp;
                            }
                        }

                        if (count < min)
                            min = count;
                    }
                }
                if (diff + min < 0)
                    return Integer.MAX_VALUE;
                else
                    diff += min;

            }
        }

        return diff;
    }

    private void findFrameChange(int video,int frameStride) {
        ArrayList<Integer> tempFrameChange = new ArrayList<>();
        tempFrameChange.add(0);
        for (int i = 0; i < videoFrameList.get(video).size()-frameStride; i = i+frameStride) {
            int temp = difference(video, i,frameStride);

            if (temp > thereshold) {
                System.out.println(i + ". " + temp);
                // if(sceneChange.size()>0&&i-sceneChange.get(sceneChange.size()-1)>10)
                tempFrameChange.add(i);
                keyFrameNum++;
            }
        }
        tempFrameChange.add(videoFrameList.get(video).size() - 1);

        sceneChange.add(tempFrameChange);

    }

    private void findKeyFrame() {
        System.out.println(keyNum+" "+keyFrameNum+" "+imageList.size());
        if (imageList.size() > keyNum -keyFrameNum) {
            int num = keyNum-keyFrameNum;
            for (int i = 0; i < num; i++) {
                keyFrame.add(new FrameMediaMatch(imageList.get(i),imageList.get(i).getImage()));
                --keyNum;
            }
        } else {

            for (Media image : imageList) {
                keyFrame.add(new FrameMediaMatch(image,image.getImage()));
                --keyNum;
            }
        }
        System.out.println("AfterImage"+keyNum+" "+keyFrameNum+" "+imageList.size());
        if (keyNum <= 0) return;

        if (keyNum > keyFrameNum) {

            int numPerScene = (keyNum / keyFrameNum) + 1;
            for (int i = 0; i < videoFrameList.size(); i++) {
                ArrayList<Image> list = videoFrameList.get(i);
                ArrayList<Integer> change = sceneChange.get(i);
                for (int j = 1; j < change.size(); j++) {
                    for (double k = 1.0; k <= numPerScene; k++) {

                        double pos = (k / (numPerScene + 1)) * (change.get(j)-change.get(j - 1) )+change.get(j-1);

                        keyFrame.add(new FrameMediaMatch(videoList.get(i), list.get((int) pos), (pos*100) / list.size()));
                        keyNum--;
                    }
                    keyFrameNum--;
                    if(keyNum<=keyFrameNum)numPerScene = 1;
                }
            }
        } else if (keyNum == keyFrameNum) {

            for (int i = 0; i < videoFrameList.size(); i++) {
                ArrayList<Image> list = videoFrameList.get(i);
                ArrayList<Integer> change = sceneChange.get(i);
                for (int j = 1; j < change.size(); j++) {
                    double pos = ((double) change.get(j - 1) + change.get(j)) / 2;
                    keyFrame.add(new FrameMediaMatch(videoList.get(i), list.get((int) pos), (pos*100) / list.size()));
                }
            }
        } else if (keyNum > videoFrameList.size()) {

            int pos = 0;
            while (keyNum > 0) {
                ArrayList<Image> list = videoFrameList.get(pos);
                if (keyNum > videoFrameList.size() - pos) {
                    ArrayList<Integer> change = sceneChange.get(pos);
                    for (int j = 1; j < change.size(); j++) {
                        double p = ((double) change.get(j - 1) + change.get(j)) / 2;
                        keyFrame.add(new FrameMediaMatch(videoList.get(pos), list.get((int) p), (p*100) / list.size()));
                        keyNum--;
                        if (keyNum <= videoFrameList.size() - pos - 1) break;
                    }
                } else {
                    keyFrame.add(new FrameMediaMatch(videoList.get(pos), list.get(list.size() / 2), 50.0));
                    keyNum--;
                }
                pos++;
            }
        } else if (keyNum <= videoFrameList.size()) {

            for (int i = 0; i < videoFrameList.size(); i++) {
                ArrayList<Image> list = videoFrameList.get(i);
                keyFrame.add(new FrameMediaMatch(videoList.get(i), list.get(list.size() / 2), 50.0));
                keyNum--;
                if (keyNum <= 0) break;
            }
        }
    }

    public Synopsis genSynopsisImage() {
        keyNum = 29;
        int w = (int) ((width / scale) * (0.5 * (keyNum - 1)));
        int h = height * 3 / (2 * scale);

        //System.out.println(keyFrame.size());
        int[][][] result = new int[w][h][3];
        int[][][][] images = new int[keyNum][width / scale][height / scale][3];
        for (int i = 0; i < keyNum; i++) {
            images[i] = reader(i, 1.0 / scale);
        }
        // Create boundary
        int[] bound = new int[w];
        int h1 = h / 3;
        int h2 = h * 2 / 3;
        int dis = width / (scale * 4);
        int count = 0;
        for (int i = 0; i < w; i++) {
            if (count == 0) {
                for (int j = 0; j < dis; j++) {
                    if (i >= w)
                        break;
                    bound[i++] = h1;
                }
                i--;
                count++;
            } else if (count % 2 == 0) {
                for (int j = 0; j < 2 * dis; j++) {
                    if (i >= w)
                        break;
                    bound[i++] = h1;
                }
                i--;
                count++;
            } else {
                for (int j = 0; j < 2 * dis; j++) {
                    if (i >= w)
                        break;
                    bound[i++] = h2;
                }
                i--;
                count++;
            }
        }
        List<Synopsis.MediaCoordinate> coordinateList = new ArrayList<>();
        WritableImage writableImage = new WritableImage(w, h);
        PixelWriter pixelWriter = writableImage.getPixelWriter();
        Set<Integer> set = new HashSet<>();

        System.out.println("Writing");
        // draw image onto result image
        for (int i = 0; i < keyFrame.size(); i++) {
            int x = (int) (((double) (i - 1) / 2) * (width / scale));
            // int x = (i/2)*(width/scale)+(i%2)*(width/scale)/2;
            int y = i % 2 == 0 ? h / 3 : 0;
            for (int p = 0; p < width / scale; p++) {
                for (int q = 0; q < height / scale; q++) {
                    int index = (y+q)*w+x+p;
                    if (x + p < 0 || x + p >= w)
                        continue;
                    if (i % 2 == 0) {
                        if (y + q >= bound[x + p]) {
                            result[x + p][y + q][0] = images[i][p][q][0];
                            result[x + p][y + q][1] = images[i][p][q][1];
                            result[x + p][y + q][2] = images[i][p][q][2];

                            if(!set.contains(index)){
                                //System.out.println(keyFrame.get(i).getTime());
                                Synopsis.MediaCoordinate mc = new Synopsis.MediaCoordinate(x+p,y+q,keyFrame.get(i).getMedia(),keyFrame.get(i).getTime());
                                coordinateList.add(mc);
                                set.add(index);
                            }
                        }
                    } else {

                        if (y + q <= bound[x + p]) {
                            result[x + p][y + q][0] = images[i][p][q][0];
                            result[x + p][y + q][1] = images[i][p][q][1];
                            result[x + p][y + q][2] = images[i][p][q][2];

                            if(!set.contains(index)){
                                //System.out.println(keyFrame.get(i).getTime());
                                Synopsis.MediaCoordinate mc = new Synopsis.MediaCoordinate(x+p,y+q,keyFrame.get(i).getMedia(),keyFrame.get(i).getTime());
                                coordinateList.add(mc);
                                set.add(index);
                            }
                        }
                    }
                }
            }
        }

        System.out.println("Handling boundary");
        // handle boundary
        // horizontal
        int c = 0;
        int half = 0;
        for (int i = 1; i < w; i++) {
            c = i / 44;
            half = i / (width / (4 * scale));
            if (c % 2 == 0) {
                if (half % 2 == 0) {
                    for (int j = 0; j <= 10; j++) {
                        double per = (double) j / 10;

                        result[i][bound[i] + j][0] = (int) (images[c][(i + 44) % 88][j][0] * per
                                + images[c + 1][i % 88][bound[i] + j][0] * (1 - per));
                        result[i][bound[i] + j][1] = (int) (images[c][(i + 44) % 88][j][1] * per
                                + images[c + 1][i % 88][bound[i] + j][1] * (1 - per));
                        result[i][bound[i] + j][2] = (int) (images[c][(i + 44) % 88][j][2] * per
                                + images[c + 1][i % 88][bound[i] + j][2] * (1 - per));
                    }
                } else {
                    for (int j = -10; j < 0; j++) {
                        double per = (double) (-j) / 10;

                        result[i][bound[i]
                                + j][0] = (int) (images[c][(i + 44) % 88][bound[i] + j - h / 3][0] * (1 - per)
                                + images[c + 1][i % 88][bound[i] + j][0] * per);
                        result[i][bound[i]
                                + j][1] = (int) (images[c][(i + 44) % 88][bound[i] + j - h / 3][1] * (1 - per)
                                + images[c + 1][i % 88][bound[i] + j][1] * per);
                        result[i][bound[i]
                                + j][2] = (int) (images[c][(i + 44) % 88][bound[i] + j - h / 3][2] * (1 - per)
                                + images[c + 1][i % 88][bound[i] + j][2] * per);
                    }

                }

            } else {
                if (half % 2 == 0) {
                    for (int j = -10; j < 0; j++) {
                        double per = (double) (-j) / 10;

                        result[i][bound[i]
                                + j][0] = (int) (images[c + 1][(i + 44) % 88][bound[i] + j - h / 3][0] * (1 - per)
                                + images[c][i % 88][bound[i] + j][0] * per);
                        result[i][bound[i]
                                + j][1] = (int) (images[c + 1][(i + 44) % 88][bound[i] + j - h / 3][1] * (1 - per)
                                + images[c][i % 88][bound[i] + j][1] * per);
                        result[i][bound[i]
                                + j][2] = (int) (images[c + 1][(i + 44) % 88][bound[i] + j - h / 3][2] * (1 - per)
                                + images[c][i % 88][bound[i] + j][2] * per);
                    }
                } else {
                    for (int j = 0; j <= 10; j++) {
                        double per = (double) j / 10;

                        result[i][bound[i] + j][0] = (int) (images[c + 1][(i + 44) % 88][j][0] * per
                                + images[c][i % 88][bound[i] + j][0] * (1 - per));
                        result[i][bound[i] + j][1] = (int) (images[c + 1][(i + 44) % 88][j][1] * per
                                + images[c][i % 88][bound[i] + j][1] * (1 - per));
                        result[i][bound[i] + j][2] = (int) (images[c + 1][(i + 44) % 88][j][2] * per
                                + images[c][i % 88][bound[i] + j][2] * (1 - per));
                    }

                }
            }

        }

        System.out.println("Creating result");
        for(int i = 0;i<w;i++){
            for(int j = 0;j<h;j++){
                Color color = Color.rgb(result[i][j][0],result[i][j][1],result[i][j][2]);
                pixelWriter.setColor(i,j,color);
            }
        }


        /*
         * c = -1; // vertical for (int i = 1; i < w; i++) {
         *
         * if (i % 176 == 0) { for (int j = 0; j < h/3; j++) { int part = 1; for (int k
         * = -10; k < 10; k += 2) { double per = part++ / 10; result[i + k][j][0] =
         * (int) (images[c][width / scale + (k - 10) / 2][j][0] * (1 - per) + images[c +
         * 2][(10 + k) / 2][j][0] * per); result[i + k + 1][j][0] = (int)
         * (images[c][width / scale + (k - 10) / 2][j][0] * (1 - per) + images[c +
         * 2][(10 + k) / 2][j][0] * per); result[i + k][j][1] = (int) (images[c][width /
         * scale + (k - 10) / 2][j][1] * (1 - per) + images[c + 2][(10 + k) / 2][j][1] *
         * per); result[i + k + 1][j][1] = (int) (images[c][width / scale + (k - 10) /
         * 2][j][1] * (1 - per) + images[c + 2][(10 + k) / 2][j][1] * per); result[i +
         * k][j][2] = (int) (images[c][width / scale + (k - 10) / 2][j][2] * (1 - per) +
         * images[c + 2][(10 + k) / 2][j][2] * per); result[i + k + 1][j][2] = (int)
         * (images[c][width / scale + (k - 10) / 2][j][2] * (1 - per) + images[c +
         * 2][(10 + k) / 2][j][2] * per); } } } else if (i % 88 == 0) { for (int j = 2 *
         * h / 3; j < h; j++) { int part = 1; for (int k = -10; k < 10; k += 2) { double
         * per = part++ / 10; //System.out.println(i+" "+j+" "+k+" "+(j - h / 3));
         * result[i + k][j][0] = (int) (images[c][width / scale + (k - 10) / 2][j - h /
         * 3][0] * (1 - per) + images[c + 2][(10 + k) / 2][j - h / 3][0] * per);
         * result[i + k + 1][j][0] = (int) (images[c][width / scale + (k - 10) / 2][j -
         * h / 3][0] * (1 - per) + images[c + 2][(10 + k) / 2][j - h / 3][0] * per);
         * result[i + k][j][1] = (int) (images[c][width / scale + (k - 10) / 2][j - h /
         * 3][1] * (1 - per) + images[c + 2][(10 + k) / 2][j - h / 3][1] * per);
         * result[i + k + 1][j][1] = (int) (images[c][width / scale + (k - 10) / 2][j -
         * h / 3][1] * (1 - per) + images[c + 2][(10 + k) / 2][j - h / 3][1] * per);
         * result[i + k][j][2] = (int) (images[c][width / scale + (k - 10) / 2][j - h /
         * 3][2] * (1 - per) + images[c + 2][(10 + k) / 2][j - h / 3][2] * per);
         * result[i + k + 1][j][2] = (int) (images[c][width / scale + (k - 10) / 2][j -
         * h / 3][2] * (1 - per) + images[c + 2][(10 + k) / 2][j - h / 3][2] * per); } }
         * }
         *
         * if (bound[i] != bound[i - 1]) { c++; } }
         */




        return new Synopsis(writableImage,coordinateList);
    }

    private int[][][] reader(int pos, double scale) {
        int w = (int) (width * scale);
        int h = (int) (height * scale);
        int[][][] temp = new int[w][h][3];
        Image image = keyFrame.get(pos).getFrame();
        PixelReader pixel = image.getPixelReader();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                // byte a = 0;
                Position ind = calPos(x, y, scale);
                Color c = pixel.getColor(ind.x, ind.y);

                temp[x][y][0] = (int) (c.getRed()*255);
                temp[x][y][1] = (int) (c.getGreen()*255);
                temp[x][y][2] = (int) (c.getBlue()*255);
            }
        }

        return temp;
    }

    private Position calPos(int x_new, int y_new, double scale) {
        int x = (int) (x_new / scale);
        int y = (int) (y_new / scale);
        return new Position(x, y);
    }

    public static int byteToInt(byte b) {
        // return (int)b;
        return b & 0xFF;
    }

    private void addImage(Media imageMedia) {
        imageList.add(imageMedia);
    }

    private void addVideo(Media videoMedia) {
        List<Image> frameList = videoMedia.getFrames();
        videoList.add(videoMedia);
        videoFrameList.add((ArrayList<Image>) frameList);
    }

    @Override
    public Synopsis generate(List<Media> mediaList) {
        // TODO Auto-generated method stub
        int frameStride = 30;

        for (Media media : mediaList) {
            if (media.getIsImage()) {
                addImage(media);
            } else if (media.getIsVideo()) {
                addVideo(media);
            }
        }
        System.out.println("Start finding frame change");
        /*
        for(int i = 0;i<videoList.size();i++){
            findFrameChange(i,frameStride);
        }*/

        Thread[] threads = new Thread[videoList.size()];
        for(int i = 0;i<videoList.size();i++) {
            sceneChange.add(new ArrayList<Integer>());
            SceneChangeThread t = new SceneChangeThread(i,frameStride);
            Thread thread = new Thread(t);
            threads[i] = thread;
            thread.start();
        }
        try {
            for(int i = 0;i<videoList.size();i++) {
                threads[i].join();
            }
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        /*
        ArrayList<Integer> list = new ArrayList<>();
        list.add(0);
        list.add(44);
        list.add(387);
        list.add(594);
        list.add(854);
        list.add(991);
        list.add(1283);
        list.add(1392);
        list.add(1450);
        list.add(1799);
        keyFrameNum = 9;
        sceneChange.add(list);*/
        System.out.println("Finish finding frame change");
        findKeyFrame();
        System.out.println("Finish finding keyframe");

        return genSynopsisImage();
    }
}
