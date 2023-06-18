package com.project.commonintent;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CONTACT = 1;
    private static final int REQUEST_IMAGE_PICK = 2;

    //***** View of Select and Call
    private Button selectPhoneNumberButton;
    private TextView nameTextView;
    private TextView phoneNumberTextView;
    private Button callButton;
    //View of Select and Call*****


    //***** View of SendEmail
    private Button sendEmailButton;
    private EditText emailAddressEditText;
    private EditText emailSubjectEditText;
    private EditText emailBodyEditText;
    //View of SendEmail*****


    //*****View of SelectPicture
    private GridLayout pictureGridLayout;
    private Button selectPictureButton;
    //View of SelectPicture*****

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        selectPhoneNumberButton = findViewById(R.id.select_phone_number_button);
        nameTextView = findViewById(R.id.name_text_view);
        phoneNumberTextView = findViewById(R.id.phone_number_text_view);
        callButton = findViewById(R.id.call_button);

        sendEmailButton = findViewById(R.id.send_email_button);
        emailAddressEditText=findViewById(R.id.email_address_edit_text);
        emailSubjectEditText=findViewById(R.id.email_subject_edit_text);
        emailBodyEditText=findViewById(R.id.email_body_edit_text);

        pictureGridLayout = findViewById(R.id.picture_grid_layout);
        selectPictureButton = findViewById(R.id.select_picture_button);

        selectPhoneNumberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectContact();
            }
        });
        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = phoneNumberTextView.getText().toString();
                if (!phoneNumber.isEmpty()) {
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse("tel:" + phoneNumber));
                    startActivity(intent);
                }
            }
        });

        sendEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailAddress = emailAddressEditText.getText().toString();
                String emailBody = emailBodyEditText.getText().toString();
                String emailSubject = emailSubjectEditText.getText().toString();
                composeEmail(emailAddress,emailSubject,emailBody,null);
            }
        });

        selectPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

    }

    //*****Functions of Select and Call
    private void selectContact() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, REQUEST_CONTACT);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CONTACT && resultCode == RESULT_OK) {
            Uri contactUri = data.getData();
            String name = getContactName(contactUri);
            String phoneNumber = getContactPhoneNumber(contactUri);

            nameTextView.setText(name);
            phoneNumberTextView.setText(phoneNumber);
        }
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    addImageToGridLayout(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private String getContactName(Uri contactUri) {
        String name = null;
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(contactUri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            cursor.close();
        }
        return name;
    }
    private String getContactPhoneNumber(Uri contactUri) {
        String phoneNumber = null;
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(contactUri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            Cursor phoneCursor = contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                    new String[]{contactId},
                    null);

            if (phoneCursor != null && phoneCursor.moveToFirst()) {
                phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                phoneCursor.close();
            }
            cursor.close();
        }
        return phoneNumber;
    }
    //Functions of Select and Call*****


    //*****Functions of SendEmail
    private void composeEmail(String address, String subject, String body, Uri attachmentUri) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{address});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, body);

        if (attachmentUri != null) {
            intent.putExtra(Intent.EXTRA_STREAM, attachmentUri);
        }

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(Intent.createChooser(intent, "Send Email"));
        }
    }
    //Functions of SendEmail*****


    //*****Functions of SelectPicture
    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, REQUEST_IMAGE_PICK);
    }
    private void addImageToGridLayout(Bitmap bitmap) {
        // Remove the previous ImageView, if any
        if (pictureGridLayout.getChildCount() > 0) {
            pictureGridLayout.removeAllViews();
        }

        // Create a new ImageView and set the bitmap
        ImageView imageView = new ImageView(this);
        imageView.setImageBitmap(bitmap);

        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
        layoutParams.width = GridLayout.LayoutParams.WRAP_CONTENT;
        layoutParams.height = GridLayout.LayoutParams.WRAP_CONTENT;

        pictureGridLayout.addView(imageView, layoutParams);
    }
    //Functions of SelectPicture*****

}