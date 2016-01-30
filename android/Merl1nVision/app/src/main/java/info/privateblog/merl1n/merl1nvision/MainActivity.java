package info.privateblog.merl1n.merl1nvision;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import info.privateblog.merl1n.merl1nvision.bluetooth.BluetoothConnector;
import info.privateblog.merl1n.merl1nvision.bluetooth.ImageGetterTask;
import info.privateblog.merl1n.merl1nvision.bluetooth.exception.BluetoothException;
import info.privateblog.merl1n.merl1nvision.drawing.ImageHandler;
import info.privateblog.merl1n.merl1nvision.drawing.SurfaceDrawer;
import info.privateblog.merl1n.merl1nvision.util.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * By Siarhei Charkes in 2016
 * http://privateblog.info
 */
public class MainActivity extends AppCompatActivity implements Logger {
    private final static String MY_NAME = "Merl1nVision";

    private ImageHandler imageHandler = new ImageHandler();
    private SurfaceDrawer surfaceDrawer = new SurfaceDrawer(imageHandler);

    private Map<String, String> mPairedDevicesArrayAdapter = new HashMap<String, String>();
    private BluetoothConnector connector = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connector = new BluetoothConnector();

        Button getImageButton = (Button) findViewById(R.id.getImageButton);

        getImageButton.setOnClickListener(buttonClicker);

        TextView loggerTextViewer = (TextView) findViewById(R.id.textView);
        loggerTextViewer.setMovementMethod(new ScrollingMovementMethod());

        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(surfaceDrawer);

        enableConnect();

        try {
            mPairedDevicesArrayAdapter = BluetoothConnector.getBondedDevices();
            if (mPairedDevicesArrayAdapter.size() > 0) {
                Iterator<String> it = mPairedDevicesArrayAdapter.keySet().iterator();
                while(it.hasNext()) {
                    logMessage("Paired device: " + it.next());
                }
            } else {
                logMessage("No paired devices");
            }
        } catch (BluetoothException e){
            printStackTrace(e);
        }
    }

    private void enableConnect() {
        Button getImageButton = (Button) findViewById(R.id.getImageButton);
        getImageButton.setEnabled(true);
    }
    private void disableConnect() {
        Button getImageButton = (Button) findViewById(R.id.getImageButton);
        getImageButton.setEnabled(false);
    }

    protected void onDestroy() {
        super.onDestroy();
        try {
            connector.disconnect();
        } catch (BluetoothException e) {}
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuItem menuItem = menu.add("Exit");
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem arg0) {
                finish();
                return true;
            }
        });
        return true;
    }


    private View.OnClickListener buttonClicker = new View.OnClickListener() {
        public void onClick(View v) {
            Button button = (Button)v;
            try {
                switch(button.getId()) {
                    //connection
                    case R.id.getImageButton:
                        if (!BluetoothConnector.isSupported()) {
                            logMessage("Bluetooth is not supported");
                            return;
                        }
                        if (!BluetoothConnector.isEnabled()) {
                            logMessage("Bluetooth is not enabled");
                            return;
                        }

                        Map<String, String> temp = new HashMap<String, String>();
                        temp.putAll(mPairedDevicesArrayAdapter);

                        String nxtKey = temp.get(MY_NAME);
                        if (nxtKey != null) {
                            disableConnect();

                            connector.connect(nxtKey);
                            logMessage("Connected");

                            ImageGetterTask task = new ImageGetterTask(
                                                            imageHandler,
                                                            connector,
                                                            MainActivity.this,
                                                            (Button) findViewById(R.id.getImageButton));
                            task.execute();

                        } else {
                            logMessage(MY_NAME + " is not available");
                            enableConnect();
                        }
                    break;
                }
            } catch (Exception e) {
                printStackTrace(e);
            }
        }
    };

    public void logMessage(String message) {
        TextView loggerTextViewer = (TextView) findViewById(R.id.textView);
        loggerTextViewer.append(message + "\n");
    }

    public void printStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        logMessage("StackTrace: " + sw.toString());
    }
}
