package info.privateblog.merl1n.merl1nvision.bluetooth;

import android.os.AsyncTask;
import android.widget.Button;

import info.privateblog.merl1n.merl1nvision.bluetooth.exception.BluetoothException;
import info.privateblog.merl1n.merl1nvision.drawing.ImageHandler;
import info.privateblog.merl1n.merl1nvision.util.Logger;

import java.io.IOException;

/**
 * By Siarhei Charkes in 2016
 * http://privateblog.info
 */
public class ImageGetterTask extends AsyncTask<Void, Void, String> {
    private static final  char[]COMMAND = {'*', 'R', 'D', 'Y', '*'};
    private static final int WIDTH = 320; //640;
    private static final int HEIGHT = 240; //480;

    private StringBuffer stb = new StringBuffer();
    private ImageHandler imageHandler = null;
    private BluetoothConnector connector = null;
    private Logger logger = null;
    private Button getImageButton = null;

    public ImageGetterTask(ImageHandler imageHandler, BluetoothConnector connector, Logger logger, Button getImageButton) {
        this.imageHandler = imageHandler;
        this.connector = connector;
        this.logger = logger;
        this.getImageButton = getImageButton;
    }

    @Override
    protected String doInBackground(Void... noargs) {
        stb.setLength(0);

        int[][]rgb = new int[HEIGHT][WIDTH];
        int[][]rgb2 = new int[WIDTH][HEIGHT];

        stb.append("Getting image\n");

        try {
            readData(rgb, true);
            readData(rgb, false);

            for (int y = 0; y < HEIGHT; y++) {
                for (int x = 0; x < WIDTH; x++) {
                    rgb2[x][y]=rgb[y][x];
                }
            }

            imageHandler.setCurrentImage(rgb2);

            stb.append("Done!!!\n");

            connector.disconnect();

            stb.append("Disconnected\n");

        } catch (Exception e) {
            stb.append("Error: " + e.getMessage() + "\n");
        }

        return stb.toString();
    }

    @Override
    protected void onPostExecute(String result) {
        getImageButton.setEnabled(true);
        logger.logMessage(result);
    }

    private void readData(int[][]rgb, boolean firstSecond) throws IOException, BluetoothException {
        while (!isImageStart(0)) {};

        stb.append("Found image part " + ((firstSecond)?"First":"Second") + "\n");

        boolean flag = true;
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                flag = !flag;
                if (flag == firstSecond) {
                    int temp = connector.read();
                    rgb[y][x] = temp&0xFF;//((temp&0xFF) << 16) | ((temp&0xFF) << 8) | (temp&0xFF);
                }
            }
        }
    }


    private boolean isImageStart(int index) throws BluetoothException, IOException {
        if (index < COMMAND.length) {
            if (COMMAND[index] == connector.read()) {
                return isImageStart(++index);
            } else {
                return false;
            }
        }
        return true;
    }
}
