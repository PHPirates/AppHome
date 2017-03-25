package com.abbyberkers.apphome;

import android.util.Log;
import android.util.SparseArray;


public class TimeToWordsConverter {

    private static final SparseArray<String> MINUTES_MAP = new SparseArray<>();
    private static final SparseArray<String> HOURS_MAP = new SparseArray<>();

    /**
     * Constructor.
     * Creates a map with String values to be able to quickly get the minutes expression.
     */
    TimeToWordsConverter(boolean dutch, boolean words) {

        createArrays(dutch, words);
    }

    /**
     * Returns a string with the time. Eg: "twenty to five".
     * @param digitalTime String with the digital time in the HH:mm format.
     * @return
     */
    String getTimeString(String digitalTime) {

        String[] splitTime = digitalTime.split(":");
        int hour = Integer.valueOf(splitTime[0]);
        int minutes = Integer.valueOf(splitTime[1]);

        String minutesExpression = getMinutesExpression(minutes);
        String timeString;

        if(minutesExpression.equals("uur")) {
            timeString = getHourExpression(hour, minutes) + " " + minutesExpression;
        } else {
           timeString = minutesExpression + " " + getHourExpression(hour, minutes);
        }

        return timeString;
    }

    private String getMinutesExpression(int minutes) {

        int multipleOfFive = (int) (Math.round((double)minutes / 5) * 5);
        return MINUTES_MAP.get(multipleOfFive);
    }

    private String getHourExpression(int hour, int minutes) {
        if(minutes < 17) {
            return HOURS_MAP.get(hour % 12);
        } else {
            return HOURS_MAP.get((hour + 1) % 12);
        }
    }


    private void createArrays(boolean dutch, boolean words) {
        if(dutch) {
            if (words) {
                MINUTES_MAP.put(0, "uur");
                MINUTES_MAP.put(5, "vijf over");
                MINUTES_MAP.put(10, "tien over");
                MINUTES_MAP.put(15, "kwart over");
                MINUTES_MAP.put(20, "tien voor half");
                MINUTES_MAP.put(25, "vijf voor half");
                MINUTES_MAP.put(30, "half");
                MINUTES_MAP.put(35, "vijf over half");
                MINUTES_MAP.put(40, "tien over half");
                MINUTES_MAP.put(45, "kwart voor");
                MINUTES_MAP.put(50, "tien voor");
                MINUTES_MAP.put(55, "vijf voor");
                MINUTES_MAP.put(60, "uur");

                HOURS_MAP.put(0, "twaalf");
                HOURS_MAP.put(1, "een");
                HOURS_MAP.put(2, "twee");
                HOURS_MAP.put(3, "drie");
                HOURS_MAP.put(4, "vier");
                HOURS_MAP.put(5, "vijf");
                HOURS_MAP.put(6, "zes");
                HOURS_MAP.put(7, "zeven");
                HOURS_MAP.put(8, "acht");
                HOURS_MAP.put(9, "negen");
                HOURS_MAP.put(10, "tien");
                HOURS_MAP.put(11, "elf");
            } else {
                MINUTES_MAP.put(0, "uur");
                MINUTES_MAP.put(5, "5 over");
                MINUTES_MAP.put(10, "10 over");
                MINUTES_MAP.put(15, "kwart over");
                MINUTES_MAP.put(20, "10 voor half");
                MINUTES_MAP.put(25, "5 voor half");
                MINUTES_MAP.put(30, "half");
                MINUTES_MAP.put(35, "5 over half");
                MINUTES_MAP.put(40, "10 over half");
                MINUTES_MAP.put(45, "kwart voor");
                MINUTES_MAP.put(50, "10 voor");
                MINUTES_MAP.put(55, "5 voor");
                MINUTES_MAP.put(60, "uur");

                HOURS_MAP.put(0, String.valueOf(12));
                for (int i = 1; i < 12; i++) {
                    HOURS_MAP.put(i, String.valueOf(i));
                }
            }
        } else {
            if (words) {
                MINUTES_MAP.put(0, "o'clock");
                MINUTES_MAP.put(5, "five past");
                MINUTES_MAP.put(10, "ten past");
                MINUTES_MAP.put(15, "quarter past");
                MINUTES_MAP.put(20, "twenty past");
                MINUTES_MAP.put(25, "twenty-five past");
                MINUTES_MAP.put(30, "thirty past");
                MINUTES_MAP.put(35, "twenty-five to");
                MINUTES_MAP.put(40, "twenty to");
                MINUTES_MAP.put(45, "quarter to");
                MINUTES_MAP.put(50, "ten to");
                MINUTES_MAP.put(55, "five to");
                MINUTES_MAP.put(60, "o'clock");

                HOURS_MAP.put(0, "twelve");
                HOURS_MAP.put(1, "one");
                HOURS_MAP.put(2, "two");
                HOURS_MAP.put(3, "three");
                HOURS_MAP.put(4, "four");
                HOURS_MAP.put(5, "five");
                HOURS_MAP.put(6, "six");
                HOURS_MAP.put(7, "seven");
                HOURS_MAP.put(8, "eight");
                HOURS_MAP.put(9, "nine");
                HOURS_MAP.put(10, "ten");
                HOURS_MAP.put(11, "eleven");
            } else {
                MINUTES_MAP.put(0, "o'clock");
                MINUTES_MAP.put(5, "5 past");
                MINUTES_MAP.put(10, "10 past");
                MINUTES_MAP.put(15, "quarter past");
                MINUTES_MAP.put(20, "20 past");
                MINUTES_MAP.put(25, "25 past");
                MINUTES_MAP.put(30, "thirty");
                MINUTES_MAP.put(35, "25 to");
                MINUTES_MAP.put(40, "20 to");
                MINUTES_MAP.put(45, "quarter to");
                MINUTES_MAP.put(50, "10 to");
                MINUTES_MAP.put(55, "5 to");
                MINUTES_MAP.put(60, "o'clock");

                HOURS_MAP.put(0, String.valueOf(12));
                for (int i = 1; i < 12; i++) {
                    HOURS_MAP.put(i, String.valueOf(i));
                }
            }
        }
    }

}
