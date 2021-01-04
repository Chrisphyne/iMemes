package imemes.domain.com.imemes;

/*------------------------------

    - iMemes -

    Created by cubycode @2017
    All Rights reserved

--------------------------------*/

import android.app.Activity;
import android.app.Application;
import android.graphics.Bitmap;
import android.os.Bundle;



public class Configs extends Application {


        // IMPORTANT: THE ORDER OF THESE IMAGES MUST BE THE SAME AS THE BELOW memeTitles ARRAY
        public static int[] memeImages =  {
                R.drawable.m1,
                R.drawable.m2,
                R.drawable.m3,
                R.drawable.m4,
                R.drawable.m5,
                R.drawable.m6,
                R.drawable.m7,
                R.drawable.m8,
                R.drawable.m9,
                R.drawable.m10,
                R.drawable.m11,

        };


        public static String[] memeTitles = {
                "White face",               //m1
                "Black girl",               //m2
                "Actor",                    //m3
                "Burning house and baby",   //m4
                "Old lady",                 //m5
                "Sea-dog",                  //m6
                "Bad luck Brian",           //m7
                "Angry Kid",                //m8
                "Star Trek",                //m9
                "Wizard",                   //m10
                "Morpheus - Matrix",        //m11

        };




        // MARK: - SCALE BITMAP TO MAX SIZE
        public static Bitmap scaleBitmapToMaxSize(int maxSize, Bitmap bm) {
                int outWidth;
                int outHeight;
                int inWidth = bm.getWidth();
                int inHeight = bm.getHeight();
                if(inWidth > inHeight){
                    outWidth = maxSize;
                    outHeight = (inHeight * maxSize) / inWidth;
                } else {
                    outHeight = maxSize;
                    outWidth = (inWidth * maxSize) / inHeight;
                }
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(bm, outWidth, outHeight, false);
                return resizedBitmap;
        }




}// @end
