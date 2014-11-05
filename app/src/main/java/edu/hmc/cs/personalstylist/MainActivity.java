package edu.hmc.cs.personalstylist;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import edu.hmc.cs.personalstylist.Clothing;

public class MainActivity extends Activity {
    public final static String NAME_MESSAGE = "edu.hmc.cs.personalstylist.nameMessage";
    public final static String WARDROBE_MESSAGE = "edu.hmc.cs.personalstylist.wardrobeMessage";
    public final static String ARTICLE_MESSAGE = "edu.hmc.cs.personalstylist.articleMessage";
    public final static String ARTICLE_NAME = "edu.hmc.cs.personalstylist.articleName";
    public final static String CLOTHING_TYPE = "edu.hmc.cs.personalstylist.clothingType";
    public final static String CLOTHING_COLOR = "edu.hmc.cs.personalstylist.clothingColor";
    public final static String CLOTHING_FORMALITY = "edu.hmc.cs.personalstylist.clothingFormality";
    public final static String CLOTHING_TEMPERATURE = "edu.hmc.cs.personalstylist.clothingTemperature";
//    Wardrobe wardrobe;
    ArrayList<Clothing> wardrobe = new ArrayList<Clothing>();
    Context context;
    String file;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Read wardrobe data
        file = "wardrobeData";
        Gson gson = new Gson();
        String temp="";
        TextView testRead = (TextView) findViewById(R.id.testRead);
        Type clothingList = new TypeToken<ArrayList<Clothing>>() {}.getType();

        try {
            FileInputStream fIn = openFileInput(file);
            int c;
            while( (c = fIn.read()) != -1) {
                temp = temp + Character.toString((char)c);
            }
//            wardrobe = gson.fromJson(temp, Wardrobe.class);
//            TextView testRead = (TextView) findViewById(R.id.testRead);
//            if (wardrobe.wardrobeLength() < 1) {
//                testRead.setText("worked");
//            } else {
//                testRead.setText("did not work");
//            }
//            Toast.makeText(getBaseContext(), "wardrobe found", Toast.LENGTH_LONG).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if ("".equals(temp)) {
            String wardrobeName = gson.toJson(wardrobe, clothingList);
            testRead.setText(wardrobeName);
            try {
                FileOutputStream fOut = openFileOutput(file, context.MODE_PRIVATE);
                fOut.write(wardrobeName.getBytes());
                fOut.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            wardrobe = gson.fromJson(temp, clothingList);
            Clothing first = wardrobe.get(wardrobe.size()-1);
            testRead.setText((CharSequence) first.name);

        }

        Clothing currentArticle;
        for (int i = 0; i < wardrobe.size(); i++) {
            currentArticle = wardrobe.get(i);
            String type = currentArticle.getType();
            if ("shirt".equals(type)) {
                LinearLayout view = (LinearLayout) findViewById(R.id.topLayout);
                Button button = new Button(this);
                button.setText(currentArticle.getName());
                view.addView(button);
            }
            else if ("pants".equals(type)) {
                LinearLayout view = (LinearLayout) findViewById(R.id.bottomLayout);
                Button button = new Button(this);
                button.setText(currentArticle.getName());
                view.addView(button);
            }
            else if ("shoes".equals(type)) {
                LinearLayout view = (LinearLayout) findViewById(R.id.shoeLayout);
                Button button = new Button(this);
                button.setText(currentArticle.getName());
                view.addView(button);
            }
        }






    }

//    public boolean getWardrobe() {
//
//        wardrobeFile = context.getFilesDir();
//
//        if (wardrobeFile.exists()) {
//            Log.d("Hey", "The file already exists");
//        }
//
//        return true;
//    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        else if (id == R.id.action_enter_article) {
            Intent enterArticle = new Intent(this, EnterArticle.class);
            startActivity(enterArticle);
        }
        return super.onOptionsItemSelected(item);
    }


    public void viewWardrobe(View view) {
        Intent viewWardrobe = new Intent(this, DisplayWardrobe.class);

        // Make this refer to something in strings.xml
        viewWardrobe.putExtra(WARDROBE_MESSAGE, "Your Wardrobe");

        startActivity(viewWardrobe);
    }

    public void enterArticle(View view) {
        Intent enterArticle = new Intent(this, EnterArticle.class);

        enterArticle.putExtra(ARTICLE_MESSAGE, "New Article of Clothing");

        startActivity(enterArticle);
    }

    public void chooseOutfit(View view) {
    }
}
