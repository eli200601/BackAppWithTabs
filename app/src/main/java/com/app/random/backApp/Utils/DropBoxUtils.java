package com.app.random.backApp.Utils;

import android.util.Log;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.users.FullAccount;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;


public class DropBoxUtils {

    private static final String ACCESS_TOKEN = "IX3BYWTlckcAAAAAAAAWrotq4dXR729l4GzxW_15Yxc2_i6p4jRl24X3IyAR5bmJ";
    private static final String TAG = "DropBoxUtils";

    public static void main() throws DbxException, IOException {
        // Create Dropbox client
        DbxRequestConfig config = new DbxRequestConfig("dropbox/java-tutorial", "en_US");
        DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);

        // Get current account info
        FullAccount account = client.users().getCurrentAccount();
        System.out.println(account.getName().getDisplayName());
        Log.d(TAG, "account_name: " + account.getName().getDisplayName());

        // Get files and folder metadata from Dropbox root directory
        ListFolderResult result = client.files().listFolder("");
        while (true) {
            for (Metadata metadata : result.getEntries()) {
//                System.out.println(metadata.getPathLower());
                Log.d(TAG, "metadata: " + metadata.getPathLower());
            }

            if (!result.getHasMore()) {
                break;
            }

            result = client.files().listFolderContinue(result.getCursor());
        }

        // Upload "test.txt" to Dropbox
        File root = new File("test.txt");

        FileWriter writer = new FileWriter(root);
        writer.append("NewValues\n\nHello");
        writer.flush();
        writer.close();

        InputStream in = new FileInputStream("test.txt");
        in.close();
        FileMetadata metadata = client.files().uploadBuilder("/test.txt").uploadAndFinish(in);



        }
    }

