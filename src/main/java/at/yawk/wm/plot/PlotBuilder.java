package at.yawk.wm.plot;

import at.yawk.wm.Util;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Array;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import javax.imageio.ImageIO;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yawkat
 */
@Slf4j
public class PlotBuilder {
    private final StringBuilder commandBuilder = new StringBuilder();
    private byte[] data;

    public PlotBuilder command(URL resourceUrl) throws IOException {
        try (InputStream stream = resourceUrl.openStream()) {
            return command(Util.streamToString(stream, 1024));
        }
    }

    public PlotBuilder command(String command) {
        commandBuilder.append(command.replace('\n', ';')).append(';');
        return this;
    }

    public PlotBuilder parameter(String name, Object value) {
        // replace all $name with the value
        String mark = '$' + name;
        String s = parameterToString(value);

        int start = 0;
        while ((start = commandBuilder.indexOf(mark, start)) != -1) {
            int end = start + mark.length();
            commandBuilder.replace(start, end, s);
            start = end;
        }
        return this;
    }

    private static String parameterToString(Object value) {
        if (value instanceof Color) {
            Color c = (Color) value;
            return "rgb \'#" + Integer.toHexString(c.getRGB() & 0xffffff) + '\'';
        }
        return value.toString();
    }

    public PlotBuilder data(byte[] data) {
        this.data = data;
        return this;
    }

    public PlotBuilder dataArray(Object[][] array) {
        return dataArray((Object) array);
    }

    public PlotBuilder dataArray(byte[][] array) {
        return dataArray((Object) array);
    }

    public PlotBuilder dataArray(short[][] array) {
        return dataArray((Object) array);
    }

    public PlotBuilder dataArray(int[][] array) {
        return dataArray((Object) array);
    }

    public PlotBuilder dataArray(long[][] array) {
        return dataArray((Object) array);
    }

    public PlotBuilder dataArray(float[][] array) {
        return dataArray((Object) array);
    }

    public PlotBuilder dataArray(double[][] array) {
        return dataArray((Object) array);
    }

    @SneakyThrows(IOException.class)
    private PlotBuilder dataArray(Object array) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        try (Writer writer = new OutputStreamWriter(stream, StandardCharsets.US_ASCII)) {
            int l1 = Array.getLength(array);
            for (int i = 0; i < l1; i++) {
                Object row = Array.get(array, i);
                int l2 = Array.getLength(row);
                for (int j = 0; j < l2; j++) {
                    if (j != 0) { writer.write('\t'); }
                    writer.write(String.valueOf(Array.get(row, j)));
                }
                writer.write('\n');
            }
        }

        return data(stream.toByteArray());
    }

    public BufferedImage plot(Color backgroundColor, int width, int height) throws IOException, InterruptedException {
        commandBuilder.insert(0,
                              "set term png font 'Source Code Pro,10' size " + width + ',' + height +
                              " transparent;set datafile separator \"\\t\";set object 1 rectangle from screen -0.1,-0" +
                              ".1 to screen 1.1,1.1 fillcolor " + parameterToString(backgroundColor) + " behind;");
        System.out.println(new String(commandBuilder).trim());
        Process process = new ProcessBuilder("gnuplot", "-e", commandBuilder.toString())
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .redirectInput(ProcessBuilder.Redirect.PIPE)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .start();
        try (OutputStream outputStream = process.getOutputStream()) {
            if (data != null) {
                outputStream.write(data);
            }
        }
        int ret = process.waitFor();
        try (InputStream err = process.getErrorStream()) {
            String errorString = Util.streamToString(err, 512);
            if (!errorString.isEmpty()) {
                log.warn("gnuplot stderr: {}", errorString);
            }
        }
        if (ret != 0) {
            throw new IOException("Return code " + ret);
        }
        try (InputStream in = process.getInputStream()) {
            return ImageIO.read(in);
        }
    }
}
