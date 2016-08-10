package cs371m.hermes.futuremessenger;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.OpenableColumns;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
        LinearLayout layout_ib = (LinearLayout) findViewById(R.id.layout_attachment);
        layout_ib.setVisibility(View.VISIBLE);

        // Because picture messages only allow one recipient, update the TextView to be singular.
        TextView recip_tv = (TextView) findViewById(R.id.recipients_tv);
        recip_tv.setText(R.string.recipient_tv_singular);

        Bundle extras = getIntent().getExtras();
        // If editing an existing message, populate the image view with the existing image
        if (extras != null) {
            String uri_path = getIntent().getStringExtra("image_path");
            Log.d(TAG + "onCreate", uri_path);
            setImageButton(Uri.parse(uri_path));
        }
    }

    // Conditional version of the contact chooser button that only allows one recipient
    @Override
    protected void initializeContactChooserButton() {
        CardView choose_contact = (CardView) findViewById(R.id.choose_contact_button);
        choose_contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrently_selected_contacts.size() >= 1)
                    Toast.makeText(getBaseContext(), R.string.restricted_to_one_recipient,
                                   Toast.LENGTH_SHORT).show();
                else {
                    Intent contactPickerIntent =
                            new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                    contactPickerIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                    startActivityForResult(contactPickerIntent, CONTACT_PICKER_REQUEST);
                }
            }
        });
    }

    // Conditional version of the phone number button that only allows one recipient.
    @Override
    protected void initializePhoneNumberButton() {
        CardView add_number = (CardView) findViewById(R.id.enter_number_button);
        add_number.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrently_selected_contacts.size() >= 1)
                    Toast.makeText(getBaseContext(), R.string.restricted_to_one_recipient,
                                   Toast.LENGTH_SHORT).show();
                else {
                    EnterPhoneNumberDialogFragment enterNumFragment =
                            new EnterPhoneNumberDialogFragment();
                    enterNumFragment.show(getFragmentManager(),
                                          getResources().getString(R.string.enter_phone_number));
                }
            }
        });
    }

    /**
     * Schedule the message
     * @param id database id of message; -1 if new message
     * @param message message to be sent
     * @param image_path file path for an image
     * @param group_flag 0 == group message, 1 == individual, ??? == neither
     */
    @Override
    protected void scheduleMessage(long id, String message, String image_path, int group_flag) {
        Log.d(TAG + "scheduleMsg", "scheduling message");
        String uri_string = null;
        if (_image_uri != null) {
            uri_string = _image_uri.toString();
        }
        super.scheduleMessage(id, message, uri_string, MessengerDatabaseHelper.NOT_GROUP_MESSAGE);
    }

    /* This solution was guided by:
       https://stackoverflow.com/questions/2507898/how-to-pick-an-image-from-gallery-sd-card-for-my-app */
    public void selectAttachment(View v) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*"); //TODO: Videos, audio files, etc?
        startActivityForResult(photoPickerIntent, SELECT_IMAGE);
    }

    /**
     * THIS METHOD IS NOT USED
     */
    @Nullable
    private String copyImage(Uri uri) {
        /* Source: https://stackoverflow.com/questions/10854211/android-store-inputstream-in-file
           String state = Environment.getExternalStorageState();
           String rootExtDir = Environment.getExternalStorageDirectory().toString(); */
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

    /**
     * THIS METHOD IS NOT USED
     */
    public static boolean deleteCopiedFile(String path) {
        File file = new File(path);
        return file.delete();
    }

    /**
     * This method is currently not being called because the recipients list is never allowed
     * to grow to a size more than 1 (picture messages can only have one recipient).
     * @return
     */
    @Override
    public int showGroupDialog() {
        onGroupSelected(0); //Always considered a group message
        return 0;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch (requestCode) {
            case SELECT_IMAGE:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = imageReturnedIntent.getData();
                    setImageButton(selectedImage);
                }
        }
    }

    /**
     * Set the image button to display a thumbnail of the specified image.
     * @param image_uri, image to display
     */
    protected void setImageButton(Uri image_uri) {
        _image_uri = image_uri;
        try {
            InputStream imageStream = getContentResolver().openInputStream(image_uri);
            /* Tries to efficiently get the sample size */
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(imageStream, null, options);
            ImageView iv = (ImageView) findViewById(R.id.thumbnail);

            // Height and width are the minimum size we want of the image
            options.inSampleSize = calculateInSampleSize(options, iv.getMaxHeight(), iv.getWidth());
            options.inJustDecodeBounds = false;
            imageStream = getContentResolver().openInputStream(image_uri);
            Bitmap scaledImage = BitmapFactory.decodeStream(imageStream, null, options);

            iv.setImageBitmap(scaledImage);
        } catch (FileNotFoundException e) {
            Log.d(TAG + "setImageButton", "FILE NOT FOUND");
        }
    }

    /**
     * Gets the largest sample size for an image that scales it to a minimum size.
     * @param options, options containing dimensions of the image
     * @param reqWidth, minimum desired width
     * @param reqHeight, minimum desired height
     * @return the sample size of the image
     */
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        Log.d(TAG + "image sizes(h, w)", Integer.toString(height) + ", " + Integer.toString(width));
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            /* Calculate the largest inSampleSize value that is a power of 2 and keeps both
               height and width larger than the requested height and width.  */
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        Log.d(TAG + "calcSampleSize", Integer.toString(inSampleSize));
        return inSampleSize;
    }

    /**
     * Get the file name for a URI
     * @param uri, the URI whose file name is requested
     * @return the file name
     */
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

    /**
     * Ensures that the required fields are completed to ensure that incomplete
     * messages are never scheduled.
     * @return True if all required fields are filled out, False otherwise.
     */
    @Override
    protected boolean isEntryFieldsFilled() {
        boolean result = false;
        if (isNoContactEntered()) {
            Toast.makeText(getApplicationContext(),
                    R.string.no_contacts_entered, Toast.LENGTH_SHORT).show();
        } else if (isImageEmpty()) {
            Toast.makeText(getApplicationContext(),
                    "Must select an image", Toast.LENGTH_SHORT).show();
        } else if (isDateInPast()) {
            Toast.makeText(getApplicationContext(),
                    R.string.bad_date_entered, Toast.LENGTH_SHORT).show();
        } else {
            result = true;
        }
        return result;
    }

    // Ensures the image_uri is not null
    protected boolean isImageEmpty() {
        return _image_uri == null;
    }
}
