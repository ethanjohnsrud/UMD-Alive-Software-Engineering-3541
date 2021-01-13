package com.example.cs4532.umdalive;

import android.annotation.SuppressLint;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.Random;

public class ColorDiagram {
    // Member variables
    private static String[] mColors = {
            "#39add1", // light blue
            "#3079ab", // dark blue
            "#c25975", // mauve
            "#e15258", // red
            "#f9845b", // orange
            "#838cc7", // lavender
            "#7d669e", // purple
            "#53bbb4", // aqua
            "#51b46d", // green
            "#637a91", // dark gray
    };

    //Would be better to add to user profile, may come back to later.
    private static ArrayList<String> userNames = new ArrayList<String>();

    private static ArrayList<Integer> userColors = new ArrayList<Integer>();

    public static int matchColor(String user) {
//        Integer color = Color.parseColor("#e0ab18");
        Integer color = getColor();
        Boolean userMatch = false;

        if (user != null) {
            try {
                    for (int i = 0; i < userNames.size(); i++) {
                        if (userNames.get(i).equals(user)) {
                            userMatch = true;
                            color = userColors.get(i);
                            System.out.println("***SETTING COLOR: " + user + " *-* " + color.toString());
                        }
                    }
                    System.out.println("***NEW***SETTING COLOR: " + user + " *-* " + color.toString());
                }catch(NullPointerException exe){
                    System.out.println("***NEW-NullPointerException-***SETTING COLOR: " + user + " *-* " + color.toString());
                }
            if (!userMatch) {
                userNames.add(user); //adds to end of list
    //            color = getColor();
                userColors.add(color);
                System.out.println("USERS: " + userNames);
                System.out.println("COLORS: " + userColors);
            }
        }
        return color;
    }




    // Method
    public static int getColor() {
        String color = "";

        // Randomly select a color
        Random randomGenerator = new Random(); // Construct a new Random number generator
        int randomNumber = randomGenerator.nextInt(mColors.length);

        color = mColors[randomNumber];
        @SuppressLint("Range") int colorAsInt = Color.parseColor(color);

        return colorAsInt;
//        return Color.parseColor("#e15258");
    }
}
