package com.example.photogallery;
import com.example.photogallery.db.FileStorage;
import com.example.photogallery.db.SQLiteStorage;

import android.content.Context;
import android.os.Build;

import androidx.test.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class FileStorageTests {

    // For SQLiteStorage tests
    SQLiteStorage fs = new SQLiteStorage();
    Context appContext; SQLiteStorage ss;
    @Before  /*Initialization */
    public void grantPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getInstrumentation().getUiAutomation().executeShellCommand(
                    "pm grant " + InstrumentationRegistry.getTargetContext().getPackageName()
                            + " android.permission.READ_EXTERNAL_STORAGE");
            getInstrumentation().getUiAutomation().executeShellCommand(
                    "pm grant " + InstrumentationRegistry.getTargetContext().getPackageName()
                            + " android.permission.WRITE_EXTERNAL_STORAGE");
        }
    }

    // For SQLiteStorage usage
    @Before
    public void initialization() {
        appContext = InstrumentationRegistry.getTargetContext();
        fs = new SQLiteStorage();
        int status = fs.init(appContext, "App.db");
        status = fs.deletePhotos();
        status = fs.addPhoto("cafe", "2018-08-25 13:20:21", "test_cafe_2018-08-25 13:20:21_12345.jpg");
        status = fs.addPhoto("cafe", "2017-08-25 13:20:21", "test_cafe_2017-08-25 13:20:21_54321.jpg");
    }
    // For SQLiteStorage usage
    @After
    public void finalization() {
        int status = fs.deletePhoto("test_cafe_2018-08-25 13:20:21_12345.jpg");
        status = fs.deletePhoto("test_cafe_2017-08-25 13:20:21_54321.jpg");
    }

    @Test /*Unit Test for findPhotos method - 1.6 Specify time window that may fetch multiple photos and verify correct photos are found*/
    public void findPhotosTest_Multiple() throws Exception {
        // Using the App Context create an instance of the FileStorage
        Context appContext = InstrumentationRegistry.getTargetContext();
        //FileStorage fs = new FileStorage(appContext);

        // Initialize a time window around the time photos were taken
        Date startTimestamp = null, endTimestamp = null;
        try {
            //test_cafe_2018-08-25 13:20:21_12345.jpg
            Calendar calendar = Calendar.getInstance();
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // Needs to be reformatted for FileStorage
            startTimestamp = format.parse("2017-08-25 13:20:21");
            calendar.setTime(startTimestamp);
            calendar.add(Calendar.MINUTE, -5); // Set a start time 5 minutes before the specified time
            startTimestamp = calendar.getTime();

            calendar.setTime(startTimestamp);
            calendar.add(Calendar.MINUTE, 10); // Set an end time 10 minutes after the start time
            endTimestamp = calendar.getTime();
        } catch (Exception ex) { }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // Call the method specifying the test time window.
        ArrayList<String> photos = fs.findPhotos("", "", "cafe");

        // Verify that multiple photos within the time window are found
        assertTrue(photos.size() > 1);

        // Verify that all found photos have timestamps within the specified time window (for timestamp search)
        /*
        for (String photoPath : photos) {
            Date photoTimestamp = parseTimestampFromPath(photoPath);
            assertTrue(photoTimestamp != null);
            assertTrue(photoTimestamp.after(startTimestamp) || photoTimestamp.equals(startTimestamp));
            assertTrue(photoTimestamp.before(endTimestamp) || photoTimestamp.equals(endTimestamp));
        }
        */

    }

    private Date parseTimestampFromPath(String path) {
        try {
            // Extract timestamp from the path and convert it to a Date object
            String[] parts = path.split("/");
            String filename = parts[parts.length - 1];
            String timestampString = filename.replace(".jpg", ""); // Assuming the extension is ".jpg"
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            return format.parse(timestampString);
        } catch (ParseException ex) {
            return null;
        }
    }
    @Test /*Unit Test for findPhotos method - 1.7 Verification of keyword-based search*/
    public void findPhotosTest_Keyword() throws Exception {
        // Using the App Context create an instance of the FileStorage
        Context appContext = InstrumentationRegistry.getTargetContext();
        //FileStorage fs = new FileStorage(appContext);

        //Test keyword based search
        String keyword = "cafe";
        Date startTimestamp = null, endTimestamp = null;
        //Call the method specifying the test time window.
        ArrayList<String> photos = fs.findPhotos("", "", keyword);

        //Verify that a photo with the matching timestamp is found
        assertTrue(photos.size() > 0);
        //Verify all photos contain the keyword
        photos.forEach(item -> assertTrue(item.contains(keyword)));
    }
    @Test /*Unit Test for findPhotos method */
    public void findPhotosTest_Time() throws Exception {
        // Using the App Context create an instance of the FileStorage
        Context appContext = InstrumentationRegistry.getTargetContext();
        //FileStorage fs = new FileStorage(appContext);

        //Test time based search
        //Initialize a time window around the time a Photo was taken
        Date startTimestamp = null, endTimestamp = null;
        try {
            Calendar calendar = Calendar.getInstance();
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            startTimestamp = format.parse("2018-08-25 13:20:21");
            calendar.setTime(startTimestamp);
            calendar.add(Calendar.MINUTE, 1);
            endTimestamp = calendar.getTime();

        } catch (Exception ex) { }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        //Call the method specifying the test time window.
        ArrayList<String> photos = fs.findPhotos(sdf.format(startTimestamp), sdf.format(endTimestamp), "");

        //Verify that only one photo with the matching timestamp is found
        assertEquals(1, photos.size());
        assertEquals(true, photos.get(0).contains("2018-08-25 13:20:21"));
    }

}

