package imemes.domain.com.imemes;

/*------------------------------

    - iMemes -

    Created by cubycode @2017
    All Rights reserved

--------------------------------*/

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;


public class Home extends AppCompatActivity {


    public static final String EXTRA_ANIMAL_IMAGE_TRANSITION_NAME ="animationImage" ;
    /* Variables */
    MarshMallowPermission mmp = new MarshMallowPermission(this);
    private RecyclerView memeRV;



    @Override
    protected void onStart() {
        super.onStart();
        // Ask permission for Storage
        if(!mmp.checkPermissionForReadExternalStorage()) {
            mmp.requestPermissionForReadExternalStorage();
        }
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Hide ActionBar
        getSupportActionBar().hide();
        // init recyclerView
        memeRV = findViewById(R.id.memes_rv);
        if (memeRV != null) {
            GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(),2);
            memeRV.setLayoutManager(gridLayoutManager);
            memeRV.setItemAnimator(new DefaultItemAnimator());

        }
        // set Adapter
        setAdapter();

        // MARK: - ADD MEME BUTTON ------------------------------------
        Button addButt = findViewById(R.id.hAddMemeButt);
        addButt.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              AlertDialog.Builder builder = new AlertDialog.Builder(Home.this)
                      .setTitle("SELECT SOURCE")
                      .setItems(new CharSequence[]
                                      {"Take a picture",  "Pick from Gallery" },
                              new DialogInterface.OnClickListener() {
                                  public void onClick(DialogInterface dialog, int which) {
                                      switch (which) {

                                          // Open Camera
                                          case 0:
                                              if (!mmp.checkPermissionForCamera()) {
                                                  mmp.requestPermissionForCamera();
                                              } else { openCamera(); }
                                              break;

                                          // Open Gallery
                                          case 1:
                                              if (!mmp.checkPermissionForReadExternalStorage()) {
                                                  mmp.requestPermissionForReadExternalStorage();
                                              } else { openGallery(); }
                                              break;
                                      }}})
                      .setIcon(R.drawable.logo);
              builder.create().show();
         }});




        // Init AdMob Banner
        AdView mAdView =  findViewById(R.id.admobBanner);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    private void setAdapter() {
        memeRV.setAdapter(new MemesAdapter(this, new MemesAdapter.OnImageSelectedListener() {
            @Override
            public void onImageSelected(int pos,ImageView sharedImageView) {



                Intent intent = new Intent(Home.this, CreateMeme.class);
                 intent.putExtra(EXTRA_ANIMAL_IMAGE_TRANSITION_NAME, ViewCompat.getTransitionName(sharedImageView));
                intent.putExtra("memeImageId", Configs.memeImages[pos]);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        Home.this,
                        sharedImageView,
                        ViewCompat.getTransitionName(sharedImageView));

                startActivity(intent, options.toBundle());


            }
        }));
    }


    // IMAGE HANDLING METHODS ------------------------------------------------------------------------
    int CAMERA = 0;
    int GALLERY = 1;
    Uri imageURI;
    File file;


    // OPEN CAMERA
    public void openCamera() {
        Intent intent= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        file = new File(Environment.getExternalStorageDirectory(), "image.jpg");
        imageURI = FileProvider.getUriForFile(getApplicationContext(), getPackageName() + ".provider", file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageURI);
        startActivityForResult(intent, CAMERA);
    }


    // OPEN GALLERY
    public void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), GALLERY);
    }



    // IMAGE PICKED DELEGATE -----------------------------------
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            Bitmap bm = null;

            // Image from Camera
            if (requestCode == CAMERA) {

                try {
                    File f = file;
                    ExifInterface exif = new ExifInterface(f.getPath());
                    int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

                    int angle = 0;
                    if (orientation == ExifInterface.ORIENTATION_ROTATE_90) { angle = 90; }
                    else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) { angle = 180; }
                    else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) { angle = 270; }
                    Log.i("log-", "ORIENTATION: " + orientation);

                    Matrix mat = new Matrix();
                    mat.postRotate(angle);

                    Bitmap bmp = BitmapFactory.decodeStream(new FileInputStream(f), null, null);
                    bm = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), mat, true);
                }
                catch (IOException | OutOfMemoryError e) { Log.i("log-", e.getMessage()); }


                // Image from Gallery
            } else if (requestCode == GALLERY) {
                try { bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                } catch (IOException e) { e.printStackTrace(); }
            }


            // Set image
            assert bm != null;
            Bitmap scaledBm = Configs.scaleBitmapToMaxSize(800, bm);

            // Call method to pass image to Other Activity
            passBitmapToOtherActivity(scaledBm);
        }

    }
    //---------------------------------------------------------------------------------------------








    // MARK: - PASS BITMAP TO OTHER ACTIVITY ------------------------------------------------
    public void passBitmapToOtherActivity(Bitmap bitmap) {

        // Save Bitmap into the Device (to pass it to the other Activity)
        String fileName = "imagePassed";
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            FileOutputStream fo = openFileOutput(fileName, Context.MODE_PRIVATE);
            fo.write(bytes.toByteArray());
            fo.close();

            // Go to CreateMeme
            Intent i = new Intent(Home.this, CreateMeme.class);
            startActivity(i);

        } catch (Exception e) { e.printStackTrace();
            fileName = null;
        }
    }




} //@end
