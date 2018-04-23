package com.abbyberkers.apphome;

import java.util.Calendar;
import static com.abbyberkers.apphome.converters.CalendarKt.toCalendar;

public class BaseClass {

    /**
     * convert ns-format to HH:mm
     * asks for user to add bike time for Thomas/Abby
     * @param nsTime ns time
     * @return string
     */
    String convertNSToString(String nsTime, City to, String user) {

        if (nsTime == null) {
            return "No time selected";
        } else {
            Calendar c = toCalendar(nsTime);
            if (user.equals("Thomas") && to == City.ROOSENDAAL) {
                //if Thomas going to Rdaal
                //special cycling case for Thomas
                c = addBikeTime(c, 25);  // Add around 25 mintuse bike time for Thomas.

            } else if((user.equals("Abby") && to == City.ROOSENDAAL)
                    || (user.equals("Thomas") && (to == City.HEEZE || to == City.OVERLOON))) {
                String plainNumberedTime = c.toString();
                // return time written out in English
                return new TimeToWordsConverter(TimeToWordsConverter.Language.ENGLISH,
                        TimeToWordsConverter.TimeType.WORDS).getTimeString(plainNumberedTime);
            } else if (user.equals("Abby") && to == City.OVERLOON) {
                    // Add around 25 minutes bike time for Abby from Vierlingsbeek station to Overloon house.
                    c = addBikeTime(c, 25);
            }
            String plainNumberedTime = c.toString(); // time in HH:mm format
            // return the time written out
            return new TimeToWordsConverter(TimeToWordsConverter.Language.DUTCH,
                    TimeToWordsConverter.TimeType.WORDS).getTimeString(plainNumberedTime);
        }
    }

    Calendar addBikeTime(Calendar c, int bikeTime) {
        //round time to nearest x minutes
        int unRoundedMinutes = c.get(Calendar.MINUTE);
        c.add(Calendar.MINUTE, bikeTime); //add minutes for bike time
        int mod = 5; // modulo this much
        int rounded = unRoundedMinutes % mod;
        c.add(Calendar.MINUTE, rounded < (mod / 2) ? -rounded : (mod - rounded));
        return c;
    }
}
