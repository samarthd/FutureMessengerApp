package cs371m.hermes.futuremessenger;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.Calendar;

//TODO: Have these activities extend an MessageActivity, to remove duplicate code
public class MultimediaMessageActivity extends EditTextMessageActivity {

    private static final int SELECT_IMAGE = 200;

    protected Uri _image_uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_edit_text_message);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        LinearLayout layout_ib = (LinearLayout) findViewById(R.id.layout_attachment);
        layout_ib.setVisibility(View.VISIBLE);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // sendMMS("5554;5556", "Hello MMS!");
//                Log.d("click", _image_uri.getPath().toString());
                if (_image_uri != null) {
                    // copyImage();
                    ImageButton ib = (ImageButton) findViewById(R.id.button_attachment);
                    Log.d("Image", "max height: " + Integer.toString(ib.getMaxHeight()));
                    Log.d("Image", "height: " + Integer.toString(ib.getHeight()));
                    Log.d("Image", "height: " + Integer.toString(ib.getMeasuredHeightAndState()));
                }
            }
        });

    }

    //TODO: Move method to AlarmReciever
    public void sendMMS(String phonenum, String message) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mmsto:" + phonenum));
        intent.putExtra("sms_body", message);
        File sdcard = Environment.getExternalStorageDirectory();
        File gif = new File (sdcard, "Download/dancing-banana.gif");
        Log.d("sendMMS", gif.getPath());
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(gif));

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(Intent.createChooser(intent, "Send MMS"));
        }
    }

    // https://stackoverflow.com/questions/2507898/how-to-pick-an-image-from-gallery-sd-card-for-my-app
    public void selectAttachment(View v) {
        //TODO: make it so that we can attach a video, image, or other files?
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, SELECT_IMAGE);
    }

    //SOURCE: https://stackoverflow.com/questions/10854211/android-store-inputstream-in-file
    private String copyImage() {
        // String state = Environment.getExternalStorageState();
        // String rootExtDir = Environment.getExternalStorageDirectory().toString();
        // Log.d("", rootExtDir);
        boolean success = true;
        String fileName = getFileName(_image_uri);
        File dst_file = new File(getExternalFilesDir(null), fileName);
        try {
            InputStream input = getContentResolver().openInputStream(_image_uri);
            OutputStream output = new FileOutputStream(dst_file);

            try {
                byte[] buffer = new byte[1024];
                int read;

                while ((read = input.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                }
                output.flush();
            } catch (Exception e) {
                success = false;
                e.printStackTrace();
            } finally {
                input.close();
                output.close();
            }
        } catch (IOException e) {
            success = false;
            e.printStackTrace();
        }

        if (success) {
            return dst_file.getAbsolutePath();
        } else {
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch (requestCode) {
            case SELECT_IMAGE:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = imageReturnedIntent.getData();
                    _image_uri = selectedImage;
                    try {
                        InputStream imageStream = getContentResolver().openInputStream(selectedImage);
                        Bitmap yourSelectedImage = BitmapFactory.decodeStream(imageStream);
                        //TODO: fix for very large images
                        ImageButton ib = (ImageButton) findViewById(R.id.button_attachment);
                        ib.setImageBitmap(yourSelectedImage);
                    } catch (FileNotFoundException e) {
                        Log.d("onActivityResult", "FILE NOT FOUND");
                    }

                }
        }
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public static void logPrintCalendar(Calendar c, DateFormat df) {
        Log.d("print", df.format(c.getTime()));
    }
}
