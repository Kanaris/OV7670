package info.privateblog.merl1n.merl1nvision.drawing;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceHolder;

/**
 * By Siarhei Charkes in 2016
 * http://privateblog.info
 */
public class DrawThread extends Thread {
    private boolean runFlag = false;

    private ImageHandler imageHandler;
    private SurfaceHolder holder;

    public DrawThread(SurfaceHolder holder, ImageHandler imageHandler) {
        this.imageHandler = imageHandler;
        this.holder = holder;
    }

    public void setRunning(boolean run) {
        runFlag = run;
    }

    public void run() {
        Canvas canvas = null;

        while (runFlag) {
            try {
                int[][] currentImage = imageHandler.getCurrentImage();

                if (currentImage != null) {
                    Paint paint = new Paint();
                   // paint.setStyle(Paint.Style.FILL);
                   // paint.setStrokeWidth(5);
                   // paint.setStyle(Paint.Style.FILL);

                    int height = currentImage.length;
                    int width = currentImage[0].length;

                    Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
                    Bitmap bmp = Bitmap.createBitmap(width, height, conf);
                    Canvas tempCanvas = new Canvas(bmp);

                    for (int j = 0; j < height; j++) {
                        for (int i = 0; i < width; i++) {
                            int color = currentImage[j][i];
                            paint.setColor(Color.rgb(color, color, color));//currentImage[i][j]);
                            tempCanvas.drawPoint(i, height-j-1, paint);
                        }
                    }

                    canvas = holder.lockCanvas();

                    if (canvas != null) {
                        int newWidth = canvas.getWidth();
                        int newHeight = canvas.getHeight();
                        Bitmap resized = Bitmap.createScaledBitmap(bmp, newWidth, newHeight, true);
                        canvas.drawBitmap(resized, 0, 0, new Paint(Paint.ANTI_ALIAS_FLAG));
                    }
                }
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }
}
