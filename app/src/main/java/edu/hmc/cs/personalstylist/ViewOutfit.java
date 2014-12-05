package edu.hmc.cs.personalstylist;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;


/**
 * Displays the recommended outfit based on the parameters from the ChooseOutfit screen.
 *
 * Created by davidconnor on 11/6/14.
 */
public class ViewOutfit extends Activity implements View.OnClickListener {

    // Parameters received from ChooseOutfit
    private final static String CLOTHING_FORMALITY = "edu.hmc.cs.personalstylist.clothingFormality";
    private final static String CLOTHING_TEMPERATURE = "edu.hmc.cs.personalstylist.clothingTemperature";

    // Clothing Types
    private final static String LONG_SLEEVE_SHIRT = "Long-sleeve shirt";
    private final static String SHORT_SLEEVE_SHIRT = "Short-sleeve shirt";
    private final static String SLEEVELESS_SHIRT = "Sleeveless shirt";
    private final static String PANTS = "Pants";
    private final static String SHORTS = "Shorts";
    private final static String SKIRT = "Skirt";
    private final static String DRESS_SHOES = "Dress shoes";
    private final static String TENNIS_SHOES = "Tennis shoes";
    private final static String SANDALS = "Sandals";
    private final static ArrayList<String> TOPS = new ArrayList<String>();
    private final static ArrayList<String> BOTTOMS = new ArrayList<String>();
    private final static ArrayList<String> SHOES = new ArrayList<String>();

    // Clothing Colors
    private final static String RED = "Red";
    private final static String BLUE = "Blue";
    private final static String YELLOW = "Yellow";
    private final static String GREEN = "Green";
    private final static String PURPLE = "Purple";
    private final static String ORANGE = "Orange";
    private final static String BLACK = "Black";
    private final static String WHITE = "White";
    private final static String PINK = "Pink";
    private final static String BROWN = "Brown";

    // Data retrieval variables
    ArrayList<Clothing> wardrobe = new ArrayList<Clothing>();
    Context context;
    String file = "wardrobeData";


    // On Activity start...
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Populate ArrayLists
        TOPS.add(LONG_SLEEVE_SHIRT);
        TOPS.add(SHORT_SLEEVE_SHIRT);
        TOPS.add(SLEEVELESS_SHIRT);
        BOTTOMS.add(PANTS);
        BOTTOMS.add(SHORTS);
        BOTTOMS.add(SKIRT);
        SHOES.add(DRESS_SHOES);
        SHOES.add(TENNIS_SHOES);
        SHOES.add(SANDALS);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_outfits);

        Intent outfitChoose = getIntent();

        // Read wardrobe data
        Gson gson = new Gson();
        String temp="";
        Type clothingList = new TypeToken<ArrayList<Clothing>>() {}.getType();

        try {
            FileInputStream fIn = openFileInput(file);
            int c;
            while( (c = fIn.read()) != -1) {
                temp = temp + Character.toString((char)c);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Checks for empty wardrobe
        if ("".equals(temp) || "[]".equals(temp)) {
            String wardrobeName = gson.toJson(wardrobe, clothingList);
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
        }

        // User-selected parameters
        String formPref = outfitChoose.getStringExtra(CLOTHING_FORMALITY);
        String tempPref = outfitChoose.getStringExtra(CLOTHING_TEMPERATURE);

        // Reduce displayed wardrobe to viable options based on parameters
        Choose outfitChoice = new Choose(wardrobe);
        ArrayList<Clothing> newWardrobe = outfitChoice.viableClothing(formPref, tempPref);

        // Populate scrollers with wardrobe
        Clothing currentArticle;
        for (int i = 0; i < newWardrobe.size(); i++) {
            currentArticle = newWardrobe.get(i);
            String type = currentArticle.getType();

            ImageButton button = createImageButton(currentArticle);

            if (TOPS.contains(type)) {
                LinearLayout view = (LinearLayout) findViewById(R.id.chooseTopLayout);
                view.addView(button);
            }
            else if (BOTTOMS.contains(type)) {
                LinearLayout view = (LinearLayout) findViewById(R.id.chooseBottomLayout);
                view.addView(button);
            }
            else if (SHOES.contains(type)) {
                LinearLayout view = (LinearLayout) findViewById(R.id.chooseShoeLayout);
                view.addView(button);
            }
        }

        // Display algorithm-suggested outfit
        DisplaySuggestion(outfitChoice.RecommendedOutfits(newWardrobe, formPref, tempPref));

        initializeScrollViews();

    }

    /**
     * Creates the scrolling views that store clothing items.
     *
     * Three scrolling views are created and initialized, for shirts, pants, and shoes.
     */
    private void initializeScrollViews() {
        MyScrollView topScroller = (MyScrollView) findViewById(R.id.topScrollerChoose);
        MyScrollView bottomScroller = (MyScrollView) findViewById(R.id.bottomScrollerChoose);
        final MyScrollView shoeScroller = (MyScrollView) findViewById(R.id.shoeScrollerChoose);

        LinearLayout topLayout = (LinearLayout) findViewById(R.id.chooseTopLayout);
        LinearLayout bottomLayout = (LinearLayout) findViewById(R.id.chooseBottomLayout);
        LinearLayout shoeLayout = (LinearLayout) findViewById(R.id.chooseShoeLayout);

        initializeOneScrollView(topScroller, topLayout);
        initializeOneScrollView(bottomScroller, bottomLayout);
        initializeOneScrollView(shoeScroller, shoeLayout);

        Display mDisplay = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        mDisplay.getSize(size);
        int width = size.x;

        topLayout.setPadding(width, 0, width, 0);
        bottomLayout.setPadding(width, 0, width, 0);
        shoeLayout.setPadding(width, 0, width, 0);

        initialCenter(topScroller, topLayout);
        initialCenter(bottomScroller, bottomLayout);
        initialCenter(shoeScroller, shoeLayout);
    }

    /**
     * Sets a MyScrollView object to automatically center itself after scrolling.
     *
     * When the user stops scrolling through this view, the clothing item nearest to the center
     * snaps to the center.
     *
     * @param myView A view that should center clothing items.
     * @param myLayout The LinearLayout contained in the view.
     */
    private void initializeOneScrollView(final MyScrollView myView, final LinearLayout myLayout) {
        myView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    myView.startScrollerTask();
                }
                return false;
            }
        });
        myView.setOnScrollStoppedListener(new MyScrollView.OnScrollStoppedListener() {
            @Override
            public void onScrollStopped() {
                myView.center(myLayout);
            }
        });
    }

    /**
     * Sets a MyScrollView object to initialize with an item centered.
     *
     * @param myView A view that should center clothing items.
     * @param myLayout The LinearLayout contained in the view.
     */
    private void initialCenter(final MyScrollView myView, final LinearLayout myLayout) {
        myView.post(new Runnable() {
            public void run() {
                myView.center(myLayout);
            }
        });
    }



    /**
     * Turns an article of clothing into a button.
     *
     * Creates an ImageButton with an image chosen based on the details of the clothing article. If
     * there is no suitable article, display a placeholder icon.
     *
     * @param currentArticle The article of clothing to be represented by a button.
     * @return The ImageButton for currentArticle.
     */
    private ImageButton createImageButton(Clothing currentArticle) {

        ImageButton button = new ImageButton(this);

        // Associate the clothing with the ImageButton
        button.setTag(currentArticle);

        String type = currentArticle.getType();
        String color = currentArticle.getColor();

        if (LONG_SLEEVE_SHIRT.equals(type)) {
            if (RED.equals(color)) {
                button.setImageResource(R.drawable.longsleeveshirt_red);
            }
            else if (YELLOW.equals(color)) {
                button.setImageResource(R.drawable.longsleeveshirt_yellow);
            }
            else if (BLUE.equals(color)) {
                button.setImageResource(R.drawable.longsleeveshirt_blue);
            }
            else if (GREEN.equals(color)) {
                button.setImageResource(R.drawable.longsleeveshirt_green);
            }
            else if (ORANGE.equals(color)) {
                button.setImageResource(R.drawable.longsleeveshirt_orange);
            }
            else if (PURPLE.equals(color)) {
                button.setImageResource(R.drawable.longsleeveshirt_purple);
            }
            else if (BLACK.equals(color)) {
                button.setImageResource(R.drawable.longsleeveshirt_black);
            }
            else if (WHITE.equals(color)) {
                button.setImageResource(R.drawable.longsleeveshirt_white);
            }
            else if (PINK.equals(color)) {
                button.setImageResource(R.drawable.longsleeveshirt_pink);
            }
            else if (BROWN.equals(color)) {
                button.setImageResource(R.drawable.longsleeveshirt_brown);
            } else {
                button.setImageResource(R.drawable.question);
            }
        }
        else if (SHORT_SLEEVE_SHIRT.equals(type)) {
            if (RED.equals(color)) {
                button.setImageResource(R.drawable.shortsleeveshirt_red);
            }
            else if (YELLOW.equals(color)) {
                button.setImageResource(R.drawable.shortsleeveshirt_yellow);
            }
            else if (BLUE.equals(color)) {
                button.setImageResource(R.drawable.shortsleeveshirt_blue);
            }
            else if (GREEN.equals(color)) {
                button.setImageResource(R.drawable.shortsleeveshirt_green);
            }
            else if (ORANGE.equals(color)) {
                button.setImageResource(R.drawable.shortsleeveshirt_orange);
            }
            else if (PURPLE.equals(color)) {
                button.setImageResource(R.drawable.shortsleeveshirt_purple);
            }
            else if (BLACK.equals(color)) {
                button.setImageResource(R.drawable.shortsleeveshirt_black);
            }
            else if (WHITE.equals(color)) {
                button.setImageResource(R.drawable.shortsleeveshirt_white);
            }
            else if (PINK.equals(color)) {
                button.setImageResource(R.drawable.shortsleeveshirt_pink);
            }
            else if (BROWN.equals(color)) {
                button.setImageResource(R.drawable.shortsleeveshirt_brown);
            } else {
                button.setImageResource(R.drawable.question);
            }
        }
        else if (SLEEVELESS_SHIRT.equals(type)) {
            if (RED.equals(color)) {
                button.setImageResource(R.drawable.sleevelessshirt_red);
            }
            else if (YELLOW.equals(color)) {
                button.setImageResource(R.drawable.sleevelessshirt_yellow);
            }
            else if (BLUE.equals(color)) {
                button.setImageResource(R.drawable.sleevelessshirt_blue);
            }
            else if (GREEN.equals(color)) {
                button.setImageResource(R.drawable.sleevelessshirt_green);
            }
            else if (ORANGE.equals(color)) {
                button.setImageResource(R.drawable.sleevelessshirt_orange);
            }
            else if (PURPLE.equals(color)) {
                button.setImageResource(R.drawable.sleevelessshirt_purple);
            }
            else if (BLACK.equals(color)) {
                button.setImageResource(R.drawable.sleevelessshirt_black);
            }
            else if (WHITE.equals(color)) {
                button.setImageResource(R.drawable.sleevelessshirt_white);
            }
            else if (PINK.equals(color)) {
                button.setImageResource(R.drawable.sleevelessshirt_pink);
            }
            else if (BROWN.equals(color)) {
                button.setImageResource(R.drawable.sleevelessshirt_brown);
            } else {
                button.setImageResource(R.drawable.question);
            }
        }
        else if (PANTS.equals(type)) {
            if (RED.equals(color)) {
                button.setImageResource(R.drawable.longpants_red);
            }
            else if (YELLOW.equals(color)) {
                button.setImageResource(R.drawable.longpants_yellow);
            }
            else if (BLUE.equals(color)) {
                button.setImageResource(R.drawable.longpants_blue);
            }
            else if (GREEN.equals(color)) {
                button.setImageResource(R.drawable.longpants_green);
            }
            else if (ORANGE.equals(color)) {
                button.setImageResource(R.drawable.longpants_orange);
            }
            else if (PURPLE.equals(color)) {
                button.setImageResource(R.drawable.longpants_purple);
            }
            else if (BLACK.equals(color)) {
                button.setImageResource(R.drawable.longpants_black);
            }
            else if (WHITE.equals(color)) {
                button.setImageResource(R.drawable.longpants_white);
            }
            else if (PINK.equals(color)) {
                button.setImageResource(R.drawable.longpants_pink);
            }
            else if (BROWN.equals(color)) {
                button.setImageResource(R.drawable.longpants_brown);
            } else {
                button.setImageResource(R.drawable.question);
            }
        }
        else if (SHORTS.equals(type)) {
            if (RED.equals(color)) {
                button.setImageResource(R.drawable.shortpants_red);
            }
            else if (YELLOW.equals(color)) {
                button.setImageResource(R.drawable.shortpants_yellow);
            }
            else if (BLUE.equals(color)) {
                button.setImageResource(R.drawable.shortpants_blue);
            }
            else if (GREEN.equals(color)) {
                button.setImageResource(R.drawable.shortpants_green);
            }
            else if (ORANGE.equals(color)) {
                button.setImageResource(R.drawable.shortpants_orange);
            }
            else if (PURPLE.equals(color)) {
                button.setImageResource(R.drawable.shortpants_purple);
            }
            else if (BLACK.equals(color)) {
                button.setImageResource(R.drawable.shortpants_black);
            }
            else if (WHITE.equals(color)) {
                button.setImageResource(R.drawable.shortpants_white);
            }
            else if (PINK.equals(color)) {
                button.setImageResource(R.drawable.shortpants_pink);
            }
            else if (BROWN.equals(color)) {
                button.setImageResource(R.drawable.shortpants_brown);
            } else {
                button.setImageResource(R.drawable.question);
            }
        }
        else if (SKIRT.equals(type)) {
            if (RED.equals(color)) {
                button.setImageResource(R.drawable.skirtpants_red);
            }
            else if (YELLOW.equals(color)) {
                button.setImageResource(R.drawable.skirtpants_yellow);
            }
            else if (BLUE.equals(color)) {
                button.setImageResource(R.drawable.skirtpants_blue);
            }
            else if (GREEN.equals(color)) {
                button.setImageResource(R.drawable.skirtpants_green);
            }
            else if (ORANGE.equals(color)) {
                button.setImageResource(R.drawable.skirtpants_orange);
            }
            else if (PURPLE.equals(color)) {
                button.setImageResource(R.drawable.skirtpants_purple);
            }
            else if (BLACK.equals(color)) {
                button.setImageResource(R.drawable.skirtpants_black);
            }
            else if (WHITE.equals(color)) {
                button.setImageResource(R.drawable.skirtpants_white);
            }
            else if (PINK.equals(color)) {
                button.setImageResource(R.drawable.skirtpants_pink);
            }
            else if (BROWN.equals(color)) {
                button.setImageResource(R.drawable.skirtpants_brown);
            } else {
                button.setImageResource(R.drawable.question);
            }
        }
        else if (DRESS_SHOES.equals(type)) {
            if (RED.equals(color)) {
                button.setImageResource(R.drawable.dressshoes_red);
            }
            else if (YELLOW.equals(color)) {
                button.setImageResource(R.drawable.dressshoes_yellow);
            }
            else if (BLUE.equals(color)) {
                button.setImageResource(R.drawable.dressshoes_blue);
            }
            else if (GREEN.equals(color)) {
                button.setImageResource(R.drawable.dressshoes_green);
            }
            else if (ORANGE.equals(color)) {
                button.setImageResource(R.drawable.dressshoes_orange);
            }
            else if (PURPLE.equals(color)) {
                button.setImageResource(R.drawable.dressshoes_purple);
            }
            else if (BLACK.equals(color)) {
                button.setImageResource(R.drawable.dressshoes_black);
            }
            else if (WHITE.equals(color)) {
                button.setImageResource(R.drawable.dressshoes_white);
            }
            else if (PINK.equals(color)) {
                button.setImageResource(R.drawable.dressshoes_pink);
            }
            else if (BROWN.equals(color)) {
                button.setImageResource(R.drawable.dressshoes_brown);
            } else {
                button.setImageResource(R.drawable.question);
            }
        }
        else if (TENNIS_SHOES.equals(type)) {
            if (RED.equals(color)) {
                button.setImageResource(R.drawable.tennisshoes_red);
            }
            else if (YELLOW.equals(color)) {
                button.setImageResource(R.drawable.tennisshoes_yellow);
            }
            else if (BLUE.equals(color)) {
                button.setImageResource(R.drawable.tennisshoes_blue);
            }
            else if (GREEN.equals(color)) {
                button.setImageResource(R.drawable.tennisshoes_green);
            }
            else if (ORANGE.equals(color)) {
                button.setImageResource(R.drawable.tennisshoes_orange);
            }
            else if (PURPLE.equals(color)) {
                button.setImageResource(R.drawable.tennisshoes_purple);
            }
            else if (BLACK.equals(color)) {
                button.setImageResource(R.drawable.tennisshoes_black);
            }
            else if (WHITE.equals(color)) {
                button.setImageResource(R.drawable.tennisshoes_white);
            }
            else if (PINK.equals(color)) {
                button.setImageResource(R.drawable.tennisshoes_pink);
            }
            else if (BROWN.equals(color)) {
                button.setImageResource(R.drawable.tennisshoes_brown);
            } else {
                button.setImageResource(R.drawable.question);
            }
        }
        else { // SANDALS
            if (RED.equals(color)) {
                button.setImageResource(R.drawable.sandalshoes_red);
            }
            else if (YELLOW.equals(color)) {
                button.setImageResource(R.drawable.sandalshoes_yellow);
            }
            else if (BLUE.equals(color)) {
                button.setImageResource(R.drawable.sandalshoes_blue);
            }
            else if (GREEN.equals(color)) {
                button.setImageResource(R.drawable.sandalshoes_green);
            }
            else if (ORANGE.equals(color)) {
                button.setImageResource(R.drawable.sandalshoes_orange);
            }
            else if (PURPLE.equals(color)) {
                button.setImageResource(R.drawable.sandalshoes_purple);
            }
            else if (BLACK.equals(color)) {
                button.setImageResource(R.drawable.sandalshoes_black);
            }
            else if (WHITE.equals(color)) {
                button.setImageResource(R.drawable.sandalshoes_white);
            }
            else if (PINK.equals(color)) {
                button.setImageResource(R.drawable.sandalshoes_pink);
            }
            else if (BROWN.equals(color)) {
                button.setImageResource(R.drawable.sandalshoes_brown);
            } else {
                button.setImageResource(R.drawable.question);
            }
        }

        // Set up the ImageButton to look nice
        button.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
        button.setScaleType(ImageButton.ScaleType.FIT_CENTER);
        button.setBackgroundColor(Color.TRANSPARENT);


        int padding = (int) 2.5 * button.getPaddingLeft();
        button.setPadding(padding, 0, padding, 0);

        button.setOnClickListener(this);

        return button;
    }


    // Create menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.view_outfit, menu);
        return true;
    }


    // Gives the user an obvious return-to-wardrobe option
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_return_to_wardrobe) {
            Intent backToWardrobe = new Intent(this, MainActivity.class);
            backToWardrobe.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Makes sure the back button works
            startActivity(backToWardrobe);
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * Display an outfit based on the suggestion of the clothing-choosing algorithm.
     *
     * @param suggestion The algorithmically-generated list of clothing suggestions.
     */
    private void DisplaySuggestion(ArrayList<Clothing> suggestion) {

        ImageButton topButton = createImageButton(suggestion.get(0));
        ImageButton bottomButton = createImageButton(suggestion.get(1));
        ImageButton shoeButton = createImageButton(suggestion.get(2));

        LinearLayout view = (LinearLayout) findViewById(R.id.suggestions);
        view.addView(topButton);
        view.addView(bottomButton);
        view.addView(shoeButton);
    }

    // When an ImageButton is selected, show drop-down list
    @Override
    public void onClick(View v) {
        ImageButton b = (ImageButton) v;
        Clothing article = (Clothing) b.getTag();

        PopupMenu popup = new PopupMenu(this, b);

        popup.getMenu().add("Name: " + article.getName());

        // get data
        CharSequence formality = "Formality: " + article.getFormality();
        popup.getMenu().add(formality);

        CharSequence temperature = "Temperature: " + article.getTemperature();
        popup.getMenu().add(temperature);



        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.article_options, popup.getMenu());
        popup.show();
    }

    /**
     * Judges the currently-centered outfit (upon button press) for appropriateness in style and
     * color. Displays a check for good, X for bad.
     *
     * @param v
     */
    public void judge(View v) {
        MyScrollView topScroll = (MyScrollView) findViewById(R.id.topScrollerChoose);
        MyScrollView bottomScroll = (MyScrollView) findViewById(R.id.bottomScrollerChoose);
        MyScrollView shoeScroll = (MyScrollView) findViewById(R.id.shoeScrollerChoose);

        LinearLayout topLayout = (LinearLayout) findViewById(R.id.chooseTopLayout);
        LinearLayout bottomLayout = (LinearLayout) findViewById(R.id.chooseBottomLayout);
        LinearLayout shoeLayout = (LinearLayout) findViewById(R.id.chooseShoeLayout);

        boolean nonemptyTop = (topLayout.getChildCount() > 0);
        boolean nonemptyBottom = (bottomLayout.getChildCount() > 0);
        boolean nonemptyShoe = (shoeLayout.getChildCount() > 0);

        ImageView checkX = (ImageView) findViewById(R.id.checkX);

        if (nonemptyBottom & nonemptyTop & nonemptyShoe) {
            Clothing top = topScroll.getCenterItem(topLayout);
            Clothing bottom = bottomScroll.getCenterItem(bottomLayout);
            Clothing shoe = shoeScroll.getCenterItem(shoeLayout);

            Choose choose = new Choose(wardrobe);

            if (choose.judgeOutfit(top, bottom, shoe)) {
                checkX.setImageResource(R.drawable.checkmark_48);
            } else {
                checkX.setImageResource(R.drawable.facepalm_50);
            }
        } else {
            checkX.setImageResource(R.drawable.exclamation);
        }
    }
}