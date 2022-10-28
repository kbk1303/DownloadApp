package zbc.dk.krik.downloadapp;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Build;
import android.os.Environment;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class DownloadService extends IntentService {

    private int result = Activity.RESULT_CANCELED;
    public static final String URL = "urlpath";
    public static final String FILENAME = "filename";
    public static final String FILEPATH = "filepath";
    public static final String RESULT = "result";
    public static final String NOTIFICATION = "zbc.dk.krik.downloadapp";
    private InputStream stream = null;
    private FileOutputStream fos = null;
    private  File output = null;

    public DownloadService() {
        super("DownloadService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String urlPath = intent.getStringExtra(URL);
        String fileName = intent.getStringExtra(FILENAME);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
            output = new File(this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                    fileName);
        }
        else {
            output = new File(Environment.getExternalStorageDirectory(),
                    fileName);
        }
        if (output.exists()) {
            output.delete();
        }


        new Thread(() -> {
            try {

                java.net.URL url = new URL(urlPath);
                stream = url.openConnection().getInputStream();
                InputStreamReader reader = new InputStreamReader(stream);
                fos = new FileOutputStream(output.getPath());
                int next = -1;
                while ((next = reader.read()) != -1) {
                    fos.write(next);
                }
                // successfully finished
                result = Activity.RESULT_OK;

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            publishResults(output.getAbsolutePath(), result);
        }).start();

    }

    private void publishResults(String outputPath, int result) {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(FILEPATH, outputPath);
        intent.putExtra(RESULT, result);
        sendBroadcast(intent);
    }
}