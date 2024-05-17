package org.acme;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.BoundingBox;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.output.Rectangle;
import ai.djl.opencv.OpenCVImageFactory;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

@Singleton
public class ImageProcessor {

    @ConfigProperty(name = "facedetection.outputfolder")
    String outputFolder;

    public Image overlayImage(Image background, Image foreground, Point location) throws IOException {

        Mat backgroun = (Mat) background.getWrappedImage();
        Mat foregroun = (Mat) foreground.getWrappedImage();

        Mat bg = new Mat();
        Mat fg = new Mat();

        Imgproc.cvtColor(backgroun, bg, Imgproc.COLOR_RGB2RGBA);
        Imgproc.cvtColor(foregroun, fg, Imgproc.COLOR_RGB2RGBA);

        for(int y = (int) Math.max(location.y , 0); y < bg.rows(); ++y){

            int fY = (int) (y - location.y);

            if(fY >= fg.rows())
                break;

            for(int x = (int) Math.max(location.x, 0); x < bg.cols(); ++x){
                int fX = (int) (x - location.x);
                if(fX >= fg.cols()){
                    break;
                }

                double opacity;
                double[] finalPixelValue = new double[4];

                opacity = fg.get(fY , fX)[2];

                finalPixelValue[0] = bg.get(y, x)[0];
                finalPixelValue[1] = bg.get(y, x)[1];
                finalPixelValue[2] = bg.get(y, x)[2];
                finalPixelValue[3] = bg.get(y, x)[3];

                for(int c = 0;  c < bg.channels(); ++c){
                    if(opacity > 0){
                        double foregroundPx =  fg.get(fY, fX)[c];
                        double backgroundPx =  bg.get(y, x)[c];

                        float fOpacity = (float) (opacity / 255);
                        finalPixelValue[c] = ((backgroundPx * ( 1.0 - fOpacity)) + (foregroundPx * fOpacity));
                        if(c==3){
                            finalPixelValue[c] = fg.get(fY,fX)[3];
                        }
                    }
                }
                bg.put(y, x, finalPixelValue);
            }
        }

        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".png", bg, matOfByte);
        byte[] byteArray = matOfByte.toArray();
        //Preparing the Buffered Image
        InputStream in = new ByteArrayInputStream(byteArray);
        return OpenCVImageFactory.getInstance().fromInputStream(in);

    }

    public Image cropImage(Image img, DetectedObjects.DetectedObject result) throws IOException {

        int imageWidth = img.getWidth();
        int imageHeight = img.getHeight();

        BoundingBox box = result.getBoundingBox();

        Rectangle rectangle = box.getBounds();
        int x = (int) (rectangle.getX() * imageWidth);
        int y = (int) (rectangle.getY() * imageHeight);
        int boxWidth = (int) (rectangle.getWidth() * imageWidth);
        int boxHeight = (int) (rectangle.getHeight() * imageHeight);

        int maximum = Math.max(boxWidth, boxHeight);
        int dx = ((maximum - boxWidth) / 2);
        int dy = ((maximum - boxHeight) / 2);

        Image face = img.getSubImage(x - dx,
                y - dy,
                boxWidth + dx,
                boxHeight + dy);

        return face;

    }

    public Point translateToPoint(Image image, Rectangle r) {
        int x = (int) (r.getX() * image.getWidth());
        int y = (int) (r.getY() * image.getHeight());
        return new Point(x, y);
    }

    public void store(String name, Image image) throws IOException {
        java.nio.file.Path outputDir = Paths.get(outputFolder);
        Files.createDirectories(outputDir);
        java.nio.file.Path imagePath = outputDir.resolve(name);
        image.save(Files.newOutputStream(imagePath), "png");
    }
}
