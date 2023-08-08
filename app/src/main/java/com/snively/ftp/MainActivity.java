package com.snively.ftp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.snively.ftp.model.FileTransfer;
import com.snively.ftp.model.FileTransferEndpoint;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static final int DEFAULT_FTP_PORT = 21;

    private static final FileTransferEndpoint DEFAULT_CONFIGURATIONS =
            new FileTransferEndpoint("eu-central-1.sftpcloud.io", "08ea17421f954c00a380571353eb338f",
                    "HzWpsxvoMXBfrSRjR42S2HTqLaNzZ0OL", "root");

    private final ArrayList<FileTransfer> files = new ArrayList<>();

    private final ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {

                    final Intent data = result.getData();
                    if (data == null)
                        return;

                    final String name = getFileName(data.getData());
                    final Uri uri = data.getData();
                    try {
                        final LinearLayout layout = findViewById(R.id.activity_main_linear_layout_files);

                        final LayoutInflater inflater = LayoutInflater.from(this);
                        final View inflatedLayout = inflater.inflate(R.layout.fragment_file_description, layout, false);

                        if (files.size() % 2 == 0)
                            inflatedLayout.setBackgroundResource(R.drawable.underlined_item_style_white);

                        ((TextView) inflatedLayout.findViewById(R.id.fragment_file_description_name)).setText(name);
                        layout.addView(inflatedLayout);

                        files.add(new FileTransfer(name, getContentResolver().openInputStream(uri)));
                    } catch (FileNotFoundException exception) {
                        Toast.makeText(this, "An error occurred while analyzing this file", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final LinearLayout layoutFiles = findViewById(R.id.activity_main_linear_layout_files);
        final View settingsFragment = findViewById(R.id.activity_main_scroll_view_actions_settings_fragment);

        final EditText editTextSettingsAddress = settingsFragment.findViewById(R.id.fragment_transfer_settings_edit_text_address);
        editTextSettingsAddress.setText(DEFAULT_CONFIGURATIONS.getAddress());

        final EditText editTextSettingsUsername = settingsFragment.findViewById(R.id.fragment_transfer_settings_edit_text_username);
        editTextSettingsUsername.setText(DEFAULT_CONFIGURATIONS.getUsername());

        final EditText editTextSettingsPassword = settingsFragment.findViewById(R.id.fragment_transfer_settings_edit_text_password);
        editTextSettingsPassword.setText(DEFAULT_CONFIGURATIONS.getPassword());

        final EditText editTextSettingsDirectory = settingsFragment.findViewById(R.id.fragment_transfer_settings_edit_text_directory);
        editTextSettingsDirectory.setText(DEFAULT_CONFIGURATIONS.getDirectory());

        final Button buttonFileSubmit = findViewById(R.id.activity_main_scroll_view_actions_button_submit);
        buttonFileSubmit.setOnClickListener(view_ -> {
            if (files.size() == 0)
                return;
            for (FileTransfer file : files) {
                if (file == null)
                    continue;
                if (!export(file.getStream(), file.getName(), editTextSettingsDirectory.getText().toString(), editTextSettingsAddress.getText().toString(),
                        editTextSettingsUsername.getText().toString(), editTextSettingsPassword.getText().toString())) {

                    Toast.makeText(this, "An error occurred while exporting " + file.getName(), Toast.LENGTH_SHORT).show();
                }
            }
            files.clear();
            layoutFiles.removeAllViews();
        });

        final Button buttonFileSelection = findViewById(R.id.activity_main_scroll_view_actions_button_select);
        buttonFileSelection.setOnClickListener(view_ -> {

            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent = Intent.createChooser(intent, "Choose File");

            launcher.launch(intent);
        });
    }

    private boolean export(InputStream stream, String name, String directory, String address, String username, String password) {
        try {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

            final FTPClient client = new FTPClient();
            client.connect(address, DEFAULT_FTP_PORT);
            if (client.login(username, password)) {
                client.makeDirectory(address);
                client.setFileType(FTP.ASCII_FILE_TYPE);
                client.storeFile("./" + directory + "/" + name, stream);

                client.logout();
                client.disconnect();
                return true;
            }
        } catch (Exception exception) {
            return false;
        }
        return false;
    }

    @SuppressLint("Range")
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst())
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            }
        }
        if (result == null) {
            result = uri.getPath();
            int last = result.lastIndexOf('/');
            if (last != -1)
                result = result.substring(last + 1);
        }
        return result;
    }
}