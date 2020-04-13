package app.util;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFprobe;

import java.io.IOException;

public class FFmpegCli {
    private static FFmpeg ffmpeg = null;
    private static FFprobe ffprobe = null;

    public static FFmpeg getFFmpeg() throws IOException {
        if (ffmpeg != null) return ffmpeg;
        ffmpeg = new FFmpeg("F:/ffmpeg-20200403-52523b6-win64-static/bin/ffmpeg.exe");
        return ffmpeg;
    }

    public static FFprobe getFFprobe() throws IOException {
        if (ffprobe != null) return ffprobe;
        ffprobe = new FFprobe("F:/ffmpeg-20200403-52523b6-win64-static/bin/ffprobe.exe");
        return ffprobe;
    }
}
