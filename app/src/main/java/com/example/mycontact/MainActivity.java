package com.example.mycontact;

import android.Manifest;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import java.util.HashSet;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CONTACT_PERMISSION = 1;

    private RecyclerView recyclerView;
    private List<Contact> contactList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Button buttonAdd = findViewById(R.id.btnAddContact);
        buttonAdd.setOnClickListener(view -> {
            showAddContactDialog();
        });



        recyclerView = findViewById(R.id.recyclerViewContacts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS},
                    REQUEST_CONTACT_PERMISSION);
        } else {
            loadContacts();
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CONTACT_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadContacts();
            } else {
                Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadContacts() {
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        );

        ArrayList<Contact> contactsList = new ArrayList<>();
        HashSet<String> seenNumbers = new HashSet<>();

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                ));
                String number = cursor.getString(cursor.getColumnIndexOrThrow(
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                )).replaceAll("\\s+", "");
                String contactId = cursor.getString(cursor.getColumnIndexOrThrow(
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                ));



                if (!seenNumbers.contains(number)) {
                    contactsList.add(new Contact(name, number, contactId));
                    seenNumbers.add(number);
                }
            }
            cursor.close();
        }

        // Set adapter
        RecyclerView recyclerView = findViewById(R.id.recyclerViewContacts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ContactAdapter adapter = new ContactAdapter(contactsList, contact -> {
            showUpdateDialog(contact); 
        });

        adapter.setOnContactLongClickListener(contact -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Contact")
                    .setMessage("Are you sure you want to delete " + contact.getName() + "?")
                    .setPositiveButton("Yes", (dialog, which) -> deleteContact(contact.getContactId()))
                    .setNegativeButton("No", null)
                    .show();
        });

        recyclerView.setAdapter(adapter);


    }
    private void showAddContactDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Contact");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText inputName = new EditText(this);
        inputName.setHint("Name");
        layout.addView(inputName);

        final EditText inputPhone = new EditText(this);
        inputPhone.setHint("Phone Number");
        inputPhone.setInputType(InputType.TYPE_CLASS_PHONE);
        layout.addView(inputPhone);

        builder.setView(layout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = inputName.getText().toString();
            String phone = inputPhone.getText().toString();
            createContact(name, phone);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }
    private void createContact(String name, String phone) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());

        // Name
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                .build());

        // Phone
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                .build());

        try {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            Toast.makeText(this, "Contact added", Toast.LENGTH_SHORT).show();
            loadContacts(); // refresh the list
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to add contact", Toast.LENGTH_SHORT).show();
        }
    }
    private void showUpdateDialog(Contact oldContact) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Contact");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText inputName = new EditText(this);
        inputName.setText(oldContact.getName());
        layout.addView(inputName);

        final EditText inputPhone = new EditText(this);
        inputPhone.setText(oldContact.getNumber());
        layout.addView(inputPhone);

        builder.setView(layout);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String newName = inputName.getText().toString();
            String newNumber = inputPhone.getText().toString();
            updateContact(oldContact, newName, newNumber);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }
    private void updateContact(Contact oldContact, String newName, String newNumber) {
        ContentResolver resolver = getContentResolver();
        String where = ContactsContract.Data.DISPLAY_NAME + " = ? AND " +
                ContactsContract.Data.MIMETYPE + " = ?";
        String[] params = new String[]{oldContact.getName(),
                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE};

        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(where, params)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, newName)
                .build());

        // Update number
        String wherePhone = ContactsContract.CommonDataKinds.Phone.NUMBER + " = ?";
        String[] phoneParams = new String[]{oldContact.getNumber()};
        ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(wherePhone, phoneParams)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, newNumber)
                .build());

        try {
            resolver.applyBatch(ContactsContract.AUTHORITY, ops);
            Toast.makeText(this, "Contact updated", Toast.LENGTH_SHORT).show();
            loadContacts();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to update contact", Toast.LENGTH_SHORT).show();
        }
    }
    private void deleteContact(String contactId) {
        try {
            getContentResolver().delete(
                    ContactsContract.RawContacts.CONTENT_URI,
                    ContactsContract.RawContacts.CONTACT_ID + "=?",
                    new String[]{contactId}
            );
            Toast.makeText(this, "Contact deleted", Toast.LENGTH_SHORT).show();
            loadContacts(); // refresh
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to delete contact", Toast.LENGTH_SHORT).show();
        }
    }



}
