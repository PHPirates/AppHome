package com.abbyberkers.apphome;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class BaseClass {

    String response;

    /**
     * convert a time string in ns format to a date object
     *
     * @param nsTime time in ns format
     * @return date object
     */
    private Date convertNSToDate(String nsTime) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        return sdf.parse(nsTime);
    }

    /**
     * convert ns time string to calendar object, uses {@link #convertNSToDate(String)}
     *
     * @param nsTime time in ns format
     * @return calendar object
     */
    Calendar convertNSToCal(String nsTime) throws ParseException {
        Date date = convertNSToDate(nsTime);
        if (date == null) return null;
        Calendar c = new GregorianCalendar();
        c.setTime(date);
        return c;
    }

    /**
     * Convert the instance variables to and from, set by the numberpickers,
     * to a string to be used in {@link AsyncTask ()}
     *
     * @param toFrom the to or from variable
     * @return string of city
     */
    String convertCityToString(City toFrom) {
        if (toFrom == City.OVERLOON) {
            return "Vierlingsbeek";  // Go to Vierlingsbeek station, then take bike to Overloon.
        }
        return toFrom.getString();
    }

    /**
     * @param cityDepTime departure time City.EINDHOVEN/rdaal
     * @param arrivalResponse xml which contains breda departures
     *                        uses response as well
     * @return departure time in breda with that voyage IN NS FORMAT
     */
    String getBredaDepTime(String cityDepTime, String arrivalResponse, City from, City to) { //from and to are needed because of breda troubles (see below)
        if (cityDepTime == null || response == null || arrivalResponse == null) {
            return "getBredaDepTime: one of parameters or response is null";
        } else {
            try {
                //setup parsing
                DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = builderFactory.newDocumentBuilder();
                XPath xPath = XPathFactory.newInstance().newXPath();
                Document xmlDocument = builder.parse(new ByteArrayInputStream(response.getBytes()));

                String arrivalOrDepExpr = "/ReisMogelijkheden/ReisMogelijkheid" +
                        "[GeplandeVertrekTijd[text()='" + cityDepTime + "']]/GeplandeAankomstTijd";

                //deptime is departure time in City.EINDHOVEN
                NodeList arrivalNodeList = (NodeList) xPath.compile(arrivalOrDepExpr).evaluate(
                        xmlDocument, XPathConstants.NODESET);

                if (arrivalNodeList.getLength() == 0) {
                    //there is no arrivalTime
                    return "arr: No arrival time in Breda in response corresponding to departure time given";
                }

                //arrivalNodeList should contain the (hopefully only one) arrival time in Breda
                String bredaArrivalTime = arrivalNodeList.item(0).getFirstChild().getNodeValue();

                //get next departure times
                //arrivalResponse contains xml Breda-RDaal

                //if going to RDaal, means arrival time is in arrivalResponse
                xmlDocument = builder.parse(new ByteArrayInputStream(arrivalResponse.getBytes()));

                //get all breda departure times
                String depExpr = "//GeplandeVertrekTijd";

                NodeList BredaDepNodeList = (NodeList) xPath.compile(depExpr).evaluate(
                        xmlDocument, XPathConstants.NODESET);

                List<String> BredaDepNSTimes = new ArrayList<>();

                //use dep times from xml
                for (int i = 0; i < BredaDepNodeList.getLength(); i++) {
                    BredaDepNSTimes.add(i, BredaDepNodeList.item(i).getFirstChild().getNodeValue());
                }

                //find next departure time in List
                int nextIndex = -1;

                try {
                    //compare with breda arrival time
                    Date BredaArrivalDate = convertNSToDate(bredaArrivalTime);


                    //convert to date to compare
                    for (int i = 0; i < BredaDepNSTimes.size(); i++) {
                        Date nsDate = convertNSToDate(BredaDepNSTimes.get(i));
                        if (nsDate == null || BredaArrivalDate == null) {
                            return "nsDate or BredaArrivalDate null";
                        }
                        if (BredaArrivalDate.before(nsDate)) {
                            //BREDA TROUBLE
                            //i should be second (i+1) next departure time on rdaal-City.EINDHOVEN because
                            // ns selects same intercity you just left again if taking first time,
                            // select the first (i) on City.EINDHOVEN-rdaal

                            if (from == City.ROOSENDAAL && to == City.EINDHOVEN) {
                                nextIndex = i+1;
                            } else {
                                nextIndex = i;
                            }

                            break;
                        }
                    }
                } catch (ParseException e) {
                    Log.e("BC.getBredaDepTime","convertNSToDate failed");
                    return null;
                }

                //                    if (nextIndex == -1) {
                ////                        Log.e("breda ", "departure time mistake ");
                //                    } else {
                if (!(nextIndex == -1) && nextIndex < BredaDepNSTimes.size()) {
                    //depTime becomes next Breda departure Time
                    return BredaDepNSTimes.get(nextIndex);
                } else {
                    Log.e("BC.getBredaDepTime","nextIndex == -1");
                    return null;
                }
            } catch (SAXException | ParserConfigurationException | IOException | XPathExpressionException e) {
//                Log.e("exception caught:",e.getMessage());
                e.printStackTrace();
            }
        }
        Log.e("BC.getBredaDepTime","end reached without result");
        return null;
    }

    /**
     * get arrival time of voyage, given departure time
     * SETS response
     *
     * @param depTime departure time ns-format
     * @param field   do you want delays or arrival times? Should be ns node string
     * @return arrival time IN NS FORMAT
     */
    String getNSStringByDepartureTime(String depTime, String field,
                                      String arrivalResponse, City from, City to) {
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

                if (from == City.EINDHOVEN && to == City.ROOSENDAAL || 
                        from == City.ROOSENDAAL && to == City.EINDHOVEN) { //only viaBreda on this trajectory
                    depTime = getBredaDepTime(depTime, arrivalResponse, from, to);
                    xmlDocument = builder.parse(new ByteArrayInputStream(arrivalResponse.getBytes())); //don't forget to update what to search in... oops
                }

                //now (possibly again) arrival time with depTime.
                // depTime may have been updated to Breda depTime
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
    String convertNSToString(String nsTime, City to, String user) throws ParseException {

        if (nsTime == null) {
            return "No time selected";
        } else {
            Calendar c = convertNSToCal(nsTime);
            if (c == null) return "convertNSToCal returned null";
            if (user.equals("Thomas") && to == City.ROOSENDAAL) {
                //if Thomas going to Rdaal
                //special cycling case for Thomas
                c = addBikeTime(c, 25);  // Add around 25 mintuse bike time for Thomas.

            } else if((user.equals("Abby") && to == City.ROOSENDAAL) 
                    || (user.equals("Thomas") && (to == City.HEEZE || to == City.OVERLOON))) {
                String plainNumberedTime = convertCalendarToString(c);
                // return time written out in English
                return new TimeToWordsConverter(TimeToWordsConverter.Language.ENGLISH,
                        TimeToWordsConverter.TimeType.WORDS).getTimeString(plainNumberedTime);
            } else if (user.equals("Abby") && to == City.OVERLOON) {

                    // Add around 25 minutes bike time for Abby from Vierlingsbeek station to Overloon house.
                    c = addBikeTime(c, 25);

            }
            String plainNumberedTime = convertCalendarToString(c); // time in HH:mm format
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

    /**
     * convert calendar object to string object in HH:mm format
     *
     * @param c calendar object
     * @return string object
     */
    String convertCalendarToString(Calendar c) {
        if (c == null) {
            Log.e("BC.convertC..ToString","null passed");
            return null;
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
            return sdf.format(c.getTime());
        }
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
    Calendar[] getNSDepartures(String response, City from, City to, Context context) {
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

            //if from City.EINDHOVEN to RDaal, select intercities only
            if (from == City.EINDHOVEN && to == City.ROOSENDAAL) {
                //select all departure times where type is Intercity

                // select all ActueleVertrekTijd where the first Reisdeel
                // has a child VervoerType with text Intercity
                String depTimesICExpr = "//ReisMogelijkheid[ReisDeel[1]/" +
                        "VervoerType = 'Intercity']/GeplandeVertrekTijd";
                nodeList = (NodeList) xPath.compile(depTimesICExpr).evaluate(
                        xmlDocument, XPathConstants.NODESET);
                //selecting intercities in breda is done when finding arrival times:
//            } else if (from == RDaal && to == City.EINDHOVEN) {
//                String depTimesICExpr = "//ReisMogelijkheid[ReisDeel[last()]/" +
//                        "VervoerType = 'Intercity']/ActueleVertrekTijd";
//                nodeList = (NodeList) xPath.compile(depTimesICExpr).evaluate(
//                        xmlDocument, XPathConstants.NODESET);
            } else {
                //generate list of departure times corresponding to nrpickers
                //just the departure times where status != niet-mogelijk
                String depTimesExpr = "/ReisMogelijkheden/ReisMogelijkheid" +
                        "[Status[not(text()='NIET-MOGELIJK')]]/GeplandeVertrekTijd";
                nodeList = (NodeList) xPath.compile(depTimesExpr).evaluate(
                        xmlDocument, XPathConstants.NODESET);
            }
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
                Date nsDate = null;
                try {
                    nsDate = convertNSToDate(nsTimes.get(i));
                } catch (ParseException e) {
                    Log.e("BC.getNSDepartures","convertNSToDate("+nsTimes.get(i)+") failed");
                }
                if (nsDate == null) break;
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
                    try {
                        depTimes[i] = convertNSToCal(nsTimes.get(j));
                    } catch (ParseException e) {
                        Log.e("BC.getNSDepartures","convertNSToCal("+nsTimes.get(j)+") failed");
                    }
                }
            }

            return depTimes;

        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
            e.printStackTrace();
        }
        return new Calendar[timesNumber]; //return default, null objects
    }


    /**
     * next departure is used to set alarm to update the widget
     *
     * @param direction direction
     * @return next departure time calendar
     */
    Calendar nextDeparture(City[] direction) {
        //some initialisation
        City from = direction[0];
        City to = direction[1];

        //set update time according to departure times without delay
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        int minutes = cal.get(Calendar.MINUTE);
        int depart = 0;

        //get departure time
        if (from == City.EINDHOVEN) {
            if (to == City.HEEZE) {
                depart = 9;
            } else if (to == City.ROOSENDAAL) {
                depart = 44;
            }
        } else if (from == City.HEEZE) {
            if (to == City.EINDHOVEN || to == City.ROOSENDAAL) {
                depart = 9;
            }
        } else if (from == City.ROOSENDAAL) {
            if (to == City.EINDHOVEN || to == City.HEEZE) {
                depart = 27;
            }
        }

        //set minutes to minutes of next departure
        if (minutes < depart) {
            cal.set(Calendar.MINUTE, depart);
        } else if (depart < minutes && minutes < depart + 30) {
            cal.set(Calendar.MINUTE, depart + 30);
        } else {
            cal.set(Calendar.MINUTE, depart);
            cal.add(Calendar.MINUTE, 60);
        }

        return cal;
    }
}
