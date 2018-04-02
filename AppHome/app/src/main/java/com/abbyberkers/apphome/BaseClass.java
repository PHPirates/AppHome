package com.abbyberkers.apphome;

import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import static com.abbyberkers.apphome.converters.CalendarKt.calendarToString;
import static com.abbyberkers.apphome.converters.CalendarKt.nsToCalendar;
import static com.abbyberkers.apphome.converters.CalendarKt.nsToDate;

public class BaseClass {

    String response;

    /**
     *
     * @param nsTime time in ns format
     * @return calendar object
     */
    @Deprecated
    Calendar convertNSToCal(String nsTime) {
        Date date = nsToDate(nsTime);
        Calendar c = new GregorianCalendar();
        c.setTime(date);
        return c;
    }

    /**
     * get arrival time of voyage, given departure time
     * SETS response
     *
     * @param depTime departure time ns-format
     * @param field   do you want delays or arrival times? Should be ns node string
     * @return arrival time IN NS FORMAT
     */
    String getNSStringByDepartureTime(String depTime, String field) {
        if (depTime == null) {
            Log.e("BC.getNSStringBy...","depTime == null");
            return null;
        } else {
            String arrivalTime = "+0";
            if (response == null) {
                response = "No response from NS or first time";
            }
            try {
                //setup parsing
                DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = builderFactory.newDocumentBuilder();
                XPath xPath = XPathFactory.newInstance().newXPath();
                Document xmlDocument = builder.parse(new ByteArrayInputStream(response.getBytes()));

                // arrival time with depTime.
                String arrivalOrDelayExpr = "/ReisMogelijkheden" +
                        "/ReisMogelijkheid[GeplandeVertrekTijd[text()='" + depTime + "']]/" + field;
                //(it is named so because the value depends on the field given)


                NodeList nodeList = (NodeList) xPath.compile(arrivalOrDelayExpr).evaluate(
                        xmlDocument, XPathConstants.NODESET);

                if (nodeList.getLength() == 0) {
                    //there is no arrivalTime or delay found (no delay can be expected, hence the message which the user will see
                    Log.e("BC.getNSStringByDep","no arrival time or delay found");
                    return "Geen vertraging vandaag!";
                }
                //set arrivalTime using the arrivalTime found
                arrivalTime = nodeList.item(0).getFirstChild().getNodeValue();

            } catch (ParserConfigurationException | SAXException |
                    IOException | XPathExpressionException e) {
                e.printStackTrace();
                Log.e("BC.getNSStringByDep","Parsing failed");
            }

            return arrivalTime;
        }
    }

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
            Calendar c = nsToCalendar(nsTime);
            if (user.equals("Thomas") && to == City.ROOSENDAAL) {
                //if Thomas going to Rdaal
                //special cycling case for Thomas
                c = addBikeTime(c, 25);  // Add around 25 mintuse bike time for Thomas.

            } else if((user.equals("Abby") && to == City.ROOSENDAAL)
                    || (user.equals("Thomas") && (to == City.HEEZE || to == City.OVERLOON))) {
                String plainNumberedTime = calendarToString(c);
                // return time written out in English
                return new TimeToWordsConverter(TimeToWordsConverter.Language.ENGLISH,
                        TimeToWordsConverter.TimeType.WORDS).getTimeString(plainNumberedTime);
            } else if (user.equals("Abby") && to == City.OVERLOON) {
                    // Add around 25 minutes bike time for Abby from Vierlingsbeek station to Overloon house.
                    c = addBikeTime(c, 25);
            }
            String plainNumberedTime = calendarToString(c); // time in HH:mm format
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


    // --------------------------------------------------------------------------------
    // Widget stuff
    // --------------------------------------------------------------------------------

    /**
     * Communicates with ASyncTask using the instance variables to, from and response
     * also in WidgetProvider
     *
     * @return Calendar[] with five current departures, default is five null objects
     */
    Calendar[] getNSDepartures(String response) {
        if (response == null) {
            response = "No response from NS or first time";
        }
        int timesNumber = 9;
        try {

            //setup parsing
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            XPath xPath = XPathFactory.newInstance().newXPath();
            Document xmlDocument = builder.parse(new ByteArrayInputStream(response.getBytes()));

            NodeList nodeList;

            //generate list of departure times corresponding to nrpickers
            //just the departure times where status != niet-mogelijk
            String depTimesExpr = "/ReisMogelijkheden/ReisMogelijkheid" +
                    "[Status[not(text()='NIET-MOGELIJK')]]/GeplandeVertrekTijd";
            nodeList = (NodeList) xPath.compile(depTimesExpr).evaluate(
                    xmlDocument, XPathConstants.NODESET);
            List<String> nsTimes = new ArrayList<>();

            //use dep times from xml
            for (int i = 0; i < nodeList.getLength(); i++) {
                nsTimes.add(i, nodeList.item(i).getFirstChild().getNodeValue());
            }

            //get current time
            Date current = new Date();

            //find next departure time in List (next as in 'the first departure time from now() )
            int nextIndex = -1;
            //convert to date to compare
            for (int i = 0; i < nsTimes.size(); i++) {
                Date nsDate = nsToDate(nsTimes.get(i));
                if (current.before(nsDate)) {
                    nextIndex = i; //index of next departure time.
                    break;
                }
            }

            //nstimes contains all ns departure times in ns-text format

            if (nsTimes.size() < timesNumber) {
//                Log.e("nstimes size is ", Integer.toString(nsTimes.size()));
//                Toast.makeText(context, "Warning, due to NS messing up, results may be inaccurate",
//                        Toast.LENGTH_LONG).show();
                Log.e("BC.getNSDepartures","nsTimes.size < timesNumber");
            }

            Calendar[] depTimes = new Calendar[timesNumber];

            //index is index of next dept time of all the xml deptimes in nsTimes
            //get departure times around next time
            for (int i = 0; i < depTimes.length; i++) { //for all null values in depTimes to be replaced
                int j = nextIndex - timesNumber / 2 + i; //calculate index for times around next departure time
                //say with aiming for 5 times, this would be the two times before the next time,
                //the next time and the two after = 5 times around next time
                if ( (j < nsTimes.size() ) && (j >= 0) ) { //todo leaving out j>=0 crashes the app without any error instead of say an IOOBE
                    //if not out of bounds... (happens when ns returns <timesNumber times in total)
                    // in that case fills up with nulls at both ends, is converted to spaces in currentDepartures()
                    depTimes[i] = nsToCalendar(nsTimes.get(j));
                }
            }

            return depTimes;

        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
            e.printStackTrace();
        }
        return new Calendar[timesNumber]; //return default, null objects
    }


}
