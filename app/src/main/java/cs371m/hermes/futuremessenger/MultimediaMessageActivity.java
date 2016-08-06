package cs371m.hermes.futuremessenger;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.support.annotation.Nullable;
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

public class MultimediaMessageActivity extends EditTextMessageActivity {

    private static String TAG = "MMSActivity ";
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
    }

//    @Override
//    protected void initializeScheduleButton() {
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Log.d(TAG, "Message Send button pressed.");
//                if (_image_uri != null) {
////                    String path = copyImage(_image_uri);
////                    Log.d(TAG, path);
////                    String saved_path = "/storage/emulated/0/Android/data/cs371m.hermes.futuremessenger/files/PSX_20151124_021724.jpg";
////                    deleteCopiedFile(saved_path);
//                    sendMMS(getNumbersFromContactsSelected(), get_message_text());
//                }
//            }
//        });
//    }

    //TODO: Move method to AlarmReciever
    public void sendMMS(String phonenum, String message) {
        Intent intent = new Intent(Intent.ACTION_SEND);
//        intent.setData(Uri.parse("smsto:" + phonenum));
        intent.putExtra("address", phonenum);
        intent.putExtra("sms_body", message);
        intent.putExtra(Intent.EXTRA_STREAM, _image_uri);
        intent.setType("image/*");
//        intent.setDataAndType(Uri.parse("smsto:" + phonenum), "*/*");

        Log.d("SendMMS", "in sendMMS()");
        if (intent.resolveActivity(getPackageManager()) != null) {
            Log.d("SendMMS", "launching activity");
            startActivity(Intent.createChooser(intent, "Send MMS"));
        }
    }

    @Override
    protected void scheduleMessage(long id, String message, String image_path, int group_flag) {
        /** ORDER OF EVENTS
         * copy image
         * get copied image path
         * get numbers from contacts
         * get message text
         * create/update database entry
         * set an alarm, with database entry id
         */
        Log.d(TAG + "scheduleMsg", "scheduling message");
        //String path = copyImage(_image_uri);
        super.scheduleMessage(id, message, _image_uri.getPath(), group_flag);
    }

    // https://stackoverflow.com/questions/2507898/how-to-pick-an-image-from-gallery-sd-card-for-my-app
    public void selectAttachment(View v) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*"); //TODO: Videos, audio files, etc?
        startActivityForResult(photoPickerIntent, SELECT_IMAGE);
    }

    //SOURCE: https://stackoverflow.com/questions/10854211/android-store-inputstream-in-file
    @Nullable
    private String copyImage(Uri uri) {
        // String state = Environment.getExternalStorageState();
        // String rootExtDir = Environment.getExternalStorageDirectory().toString();
        // Log.d("", rootExtDir);
        if (uri == null) {
            return null;
        }

        boolean success = true;
        String fileName = getFileName(uri);
        File dst_file = new File(getExternalFilesDir(null), fileName);
        try {
            InputStream input = getContentResolver().openInputStream(uri);
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

    public static boolean deleteCopiedFile(String path) {
        File file = new File(path);
        return file.delete();
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
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeStream(imageStream, null, options);

                        /* Tries to "efficiently" get the sample size, but not quite there, I think */
                        ImageButton ib = (ImageButton) findViewById(R.id.button_attachment);

                        //height and width are the minimum size we want of the image
                        options.inSampleSize = calculateInSampleSize(options, ib.getMaxHeight(), ib.getWidth());
                        options.inJustDecodeBounds = false;
                        imageStream = getContentResolver().openInputStream(selectedImage);
                        Bitmap scaledImage = BitmapFactory.decodeStream(imageStream, null, options);

                        ib.setImageBitmap(scaledImage);
                    } catch (FileNotFoundException e) {
                        Log.d(TAG + "onActivityResult", "FILE NOT FOUND");
                    }

                }
        }
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        Log.d(TAG + "image sizes(h, w)", Integer.toString(height) + ", " + Integer.toString(width));
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        Log.d(TAG + "calcSampleSize", Integer.toString(inSampleSize));
        return inSampleSize;
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

    @Override
    protected boolean isNoMessageEntered() {
        boolean prev_result = super.isNoMessageEntered();
        return prev_result && _image_uri == null;
    }
}
