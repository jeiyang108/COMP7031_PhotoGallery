package com.example.photogallery;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.content.Intent;
import android.os.Environment;

import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.core.content.ContextCompat;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
public class MainActivity extends AppCompatActivity implements LocationListener {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int SEARCH_ACTIVITY_REQUEST_CODE = 2;
    static final int SETTINGS_ACTIVITY_REQUEST_CODE = 3;

    static final int REQUEST_CAMERA_PERMISSION = 4;

    String mCurrentPhotoPath;
    private ArrayList<String> photos = null;
    private int index = 0;
    private String lastDisplayedPhotoPath; // Store the path of the last displayed photo
    private LocationManager locationManager;
    public static Location lastLoc;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Retrieve location info
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1, this);

        lastLoc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        // Initialize photos based on the last search filter or default if savedInstanceState is null
        if (savedInstanceState != null) {
            // The state is preserved when the activity is destroyed and recreated.
            photos = savedInstanceState.getStringArrayList("photos");
            index = savedInstanceState.getInt("index");
            lastDisplayedPhotoPath = savedInstanceState.getString("lastDisplayedPhotoPath");
        } else {
            photos = findPhotos(new Date(Long.MIN_VALUE), new Date(), "", "", "");
            lastDisplayedPhotoPath = null; // Initialize with no last displayed photo
        }

        if (photos.size() == 0) {
            displayPhoto(null);
        } else {
            displayPhoto(photos.get(index));
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        lastLoc = location;
    }

    @Override
    public void onLocationChanged(@NonNull List<Location> locations) {
        LocationListener.super.onLocationChanged(locations);

        if(locations.size() > 0)
            lastLoc = locations.get(locations.size() - 1);
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the photos, index, and last displayed photo path in the bundle
        outState.putStringArrayList("photos", photos);
        outState.putInt("index", index);
        outState.putString("lastDisplayedPhotoPath", lastDisplayedPhotoPath);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore the photos, index, and last displayed photo path from the bundle
        photos = savedInstanceState.getStringArrayList("photos");
        index = savedInstanceState.getInt("index");
        lastDisplayedPhotoPath = savedInstanceState.getString("lastDisplayedPhotoPath");
    }

    public void takePhoto(View v) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.photogallery.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                // Check if the CAMERA permission is granted
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    // Request the CAMERA permission
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                } else {
                    // Permission is already granted, proceed with opening the Camera app
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            // Check if the CAMERA permission was granted or denied
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted, proceed to open the Camera app
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } else {
                // Permission was denied, handle it accordingly (e.g., show a message)
            }
        }
    }

    public void scrollPhotos(View v) {
        if (photos.size() > 0) {
            String path = photos.get(index);
            String caption = ((EditText) findViewById(R.id.etCaption)).getText().toString();
            String newpath = updatePhoto(path, caption);
            photos.set(index, newpath);
        }
        switch (v.getId()) {
            case R.id.btnPrev:
                if (index > 0) {
                    index--;
                }
                break;
            case R.id.btnNext:
                if (index < (photos.size() - 1)) {
                    index++;
                }
                break;
            default:
                break;
        }
        displayPhoto(photos.size() == 0 ? null: photos.get(index));
    }
    private void displayPhoto(String path) {
        ImageView iv = (ImageView) findViewById(R.id.ivGallery);
        TextView tv = (TextView) findViewById(R.id.tvTimestamp);
        EditText et = (EditText) findViewById(R.id.etCaption);
        TextView tv_location = (TextView) findViewById(R.id.tvLocation);
        if (path == null || path =="") {
            iv.setImageResource(R.mipmap.ic_launcher);
            et.setText("");
            tv.setText("");
            tv_location.setText("");
        } else {
            iv.setImageBitmap(BitmapFactory.decodeFile(path));
            String[] attr = path.split("_");
            et.setText(attr[1]);
            tv.setText(attr[2] + "_" + attr[3]);
            tv_location.setText("(Lat) " + attr[4] + "\n(Lng) " + attr[5]);
        }
    }
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "_caption_" + timeStamp + "_"
                + String.format("%.5f", lastLoc.getLatitude()) + "_" + String.format("%.5f", lastLoc.getLongitude()) + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg",storageDir);
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SEARCH_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date startTimestamp , endTimestamp;
                try {
                    String from = (String) data.getStringExtra("STARTTIMESTAMP");
                    String to = (String) data.getStringExtra("ENDTIMESTAMP");
                    startTimestamp = format.parse(from);
                    endTimestamp = format.parse(to);
                } catch (Exception ex) {
                    startTimestamp = null;
                    endTimestamp = null;
                }
                String keywords = (String) data.getStringExtra("KEYWORDS");
                // Grab the current location details
                var longitude = getIntent().getStringExtra("LONGITUDE");
                var latitude = getIntent().getStringExtra("LATITUDE");
                index = 0;
                photos = findPhotos(startTimestamp, endTimestamp, keywords, longitude, latitude);
                if (photos.size() == 0) {
                    displayPhoto(null);
                } else {
                    displayPhoto(photos.get(index));
                }
            }
        }
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            ImageView mImageView = (ImageView) findViewById(R.id.ivGallery);
            displayPhoto(mCurrentPhotoPath);
            index = 0;
            photos = findPhotos(new Date(Long.MIN_VALUE), new Date(), "", "", "");
        }
    }
    public String updatePhoto(String path, String caption) {
        String[] attr = path.split("_");
        String newPath = attr[0] + "_" + caption + "_" + attr[2] + "_" + attr[3] + "_" + attr[4] + "_" + attr[5]; //37.42200
        File to = new File(newPath);
        File from = new File(path);
        from.renameTo(to);
        return newPath;
    }
    private ArrayList<String> findPhotos(Date startTimestamp, Date endTimestamp, String keywords, String longitude, String latitude) {
        File folder = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath(), "/Android/data/com.example.photogallery/files/Pictures");
        ArrayList<String> photos = new ArrayList<String>();
        File[] fList = folder.listFiles();
        if (fList != null) {
            for (File f : fList) {
                if (((startTimestamp == null && endTimestamp == null) ||
                        (f.lastModified() >= startTimestamp.getTime()
                        && f.lastModified() <= endTimestamp.getTime()))
                    && (keywords == "" || f.getPath().contains(keywords))
                    && ((latitude == "" && longitude == "") || withinDistance(f.getPath(), latitude, longitude))
                )
                    photos.add(f.getPath());
            }
        }
        return photos;
    }

    private boolean withinDistance(String path, String latitude, String longitude) {
        String[] attr = path.split("_");
        double fileLatitude = Double.parseDouble(attr[4]);
        double fileLongitude = Double.parseDouble(attr[5]);
        double searchLatitude = Double.parseDouble(latitude);
        double searchLongitude = Double.parseDouble(longitude);

        float[] result = new float[1];
        Location.distanceBetween(fileLatitude, fileLongitude, searchLatitude, searchLongitude, result);
        boolean isWithin50km = false;

        if (result[0] < 50000) {
            // distance between first and second location is less than 50km
            isWithin50km = true;
        }
        return isWithin50km;
    }

    public void filter(View v) {
        Intent i = new Intent(MainActivity.this, SearchActivity.class);
        startActivityForResult(i, SEARCH_ACTIVITY_REQUEST_CODE);
    }
    public void uploadPhoto(View v) {
    }
    public void editSettings(View v) {
    }

}