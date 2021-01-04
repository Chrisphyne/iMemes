package imemes.domain.com.imemes;

/*------------------------------

    - iMemes -

    Created by cubycode @2017
    All Rights reserved

--------------------------------*/

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;

public class CreateMeme extends AppCompatActivity {

    /* Views */
    ImageView memeImg;
    TextView topTxt;
    TextView bottomTxt;
    EditText topEditTxt;
    EditText bottomEditTxt;
    RelativeLayout cropView;
    ProgressDialog pd;



    /* Variables */
    int memeImageId;
    MarshMallowPermission mmp = new MarshMallowPermission(this);





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_meme);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Hide ActionBar
        getSupportActionBar().hide();



        // Init Views
        topTxt = findViewById(R.id.topTxt);
        memeImg = findViewById(R.id.memeImg);
        bottomTxt = findViewById(R.id.bottomTxt);
        topEditTxt = (EditText)findViewById(R.id.topEditTxt);
        bottomEditTxt = (EditText)findViewById(R.id.bottomEditTxt);
        cropView = (RelativeLayout)findViewById(R.id.cropView);

        //-----------------------------------------------------------------




        // Get image ID form Home.java & display image
        memeImageId = getIntent().getIntExtra("memeImageId", 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String imageTransitionName = getIntent().getStringExtra(Home.EXTRA_ANIMAL_IMAGE_TRANSITION_NAME);
            memeImg.setTransitionName(imageTransitionName);
        }
        if (memeImageId == 0) {
            try {
                // Get Bitmap from Home.java
                Bitmap bitmap = BitmapFactory.decodeStream(CreateMeme.this.openFileInput("imagePassed"));
                memeImg.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) { e.printStackTrace(); }

        } else {
            memeImg.setImageResource(memeImageId);
        }



        // Set BebasNeue font to all Text views (the BebasNeue font file is into main/asssets/font fodler)
        Typeface type = Typeface.createFromAsset(getAssets(),"fonts/BebasNeue.ttf");
        topTxt.setTypeface(type);
        bottomTxt.setTypeface(type);
        topEditTxt.setTypeface(type);
        bottomEditTxt.setTypeface(type);



        // topTxt gets text from topEditTxt
        topEditTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable mEdit)  {
                topTxt.setText(mEdit.toString());
            }
           public void beforeTextChanged(CharSequence s, int start, int count, int after){}
           public void onTextChanged(CharSequence s, int start, int before, int count){}
        });
        // bottomTxt gets text from topEditTxt
        bottomEditTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable mEdit)  {
                bottomTxt.setText(mEdit.toString());
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count){}
        });





        // MARK: - BACK BUTTON ------------------------------------
        Button backButt = findViewById(R.id.cmBackButt);
        backButt.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) { finish(); }});




        // MARK: - SHARE BUTTON ------------------------------------
        Button shButt = findViewById(R.id.cmShareButt);
        shButt.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              // Init a ProgressDialog
              pd = new ProgressDialog(CreateMeme.this);
              pd.setTitle(R.string.app_name);
              pd.setMessage("Preparing image for sharing...");
              pd.setIndeterminate(false);
              pd.setIcon(R.drawable.logo);
              pd.show();

              cropView.postDelayed(new Runnable() {
                  @Override
                  public void run() {
                      takeScreenshotOfCropView();
                  }
              }, 1000);
         }});



    } //@end onCreate()






    // TAKE SCREENSHOT OF THE cropView (RelativeLayout) ------------------------------
    public void takeScreenshotOfCropView() {

        if (!mmp.checkPermissionForReadExternalStorage()) {
            mmp.requestPermissionForReadExternalStorage();
        } else {

            View v = cropView;
            v.setDrawingCacheEnabled(true);
            Bitmap bmp = Bitmap.createBitmap(v.getDrawingCache());
            v.setDrawingCacheEnabled(false);
            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            File directory = cw.getDir(R.string.app_name + "", Context.MODE_PRIVATE);
            File filePath = new File(directory, "image.jpg");
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(filePath);
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();

                // Call Load image
                loadImageFromStorage(filePath.toString());

            } catch (IOException e) { e.printStackTrace(); }
        }

    }



    // LOAD IMAGE FOR SHARING
    private void loadImageFromStorage(String path) {
        try {
            File f = new File(path);
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            ImageView shareImg = findViewById(R.id.shareImg);
            shareImg.setImageBitmap(b);

            // Call shareImage method
            shareImage();
        }
        catch (FileNotFoundException e)  { e.printStackTrace(); }
    }



    // MARK: - SHARE THE EDITED IMAGE ---------------------------------------
    public void shareImage() {
        pd.dismiss();

        ImageView img =  findViewById(R.id.shareImg);
        Bitmap bitmap = ((BitmapDrawable)img.getDrawable()).getBitmap();
        Uri uri = getImageUri(CreateMeme.this, bitmap);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(intent, "Share Meme on..."));
    }


    // Method to get URI of the eventImage
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "MyMeme", null);
        if (path!=null) {
            return Uri.parse(path);
        }else {
            return null;
        }

    }




}//@end
