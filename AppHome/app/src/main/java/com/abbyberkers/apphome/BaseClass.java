package com.abbyberkers.apphome;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

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
    public static final int EHV = 0;
    public static final int Heeze = 1;
    public static final int RDaal = 2;
    final int timesNumber = 7; //number of departure times to be shown in numberpicker

    //string of cities to be used in the main activity (widget is not generic)
    public static final String[] cities = {"Eindhoven", "Heeze", "Roosendaal", "Schiphol"};

    String response;

    /**
     * convert a time string in ns format to a date object
     *
     * @param nsTime time in ns format
     * @return date object
     */
    public Date convertNSToDate(String nsTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            return sdf.parse(nsTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date(); //default
    }

    /**
     * convert ns time string to calendar object, uses {@link #convertNSToDate(String)}
     *
     * @param nsTime time in ns format
     * @return calendar object
     */
    public Calendar convertNSToCal(String nsTime) {
        Date date = convertNSToDate(nsTime);
        Calendar c = new GregorianCalendar();
        c.setTime(date);
        return c;
    }

    /**
     * Communicates with ASyncTask using the instance variables to, from and response
     * also in WidgetProvider
     *
     * @return Calendar[] with five current departures, default is five null objects
     */
    public Calendar[] getNSDepartures(String response, int from, int to, Context context) {
        if (response == null) {
            response = "No response from NS or first time";
        }
        try {

            //create java DOM xml parser
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder;
            builder = builderFactory.newDocumentBuilder();

            //parse xml with the DOM parser
            Document xmlDocument = builder.parse(new ByteArrayInputStream(response.getBytes()));

            //create XPath object
            XPath xPath = XPathFactory.newInstance().newXPath();

            NodeList nodeList;

            //if from EHV to RDaal, select intercities only
            if (from == EHV && to == RDaal) {
                //select all departure times where type is Intercity

                // select all ActueleVertrekTijd where the first Reisdeel
                // has a child VervoerType with text Intercity
                String depTimesICExpr = "//ReisMogelijkheid[ReisDeel[1]/" +
                        "VervoerType = 'Intercity']/ActueleVertrekTijd";
                nodeList = (NodeList) xPath.compile(depTimesICExpr).evaluate(
                        xmlDocument, XPathConstants.NODESET);
//            } else if (from == RDaal && to == EHV) {
//                String depTimesICExpr = "//ReisMogelijkheid[ReisDeel[last()]/" +
//                        "VervoerType = 'Intercity']/ActueleVertrekTijd";
//                nodeList = (NodeList) xPath.compile(depTimesICExpr).evaluate(
//                        xmlDocument, XPathConstants.NODESET);
            } else {
                //generate list of departure times corresponding to nrpickers
                //just the departure times where status != niet-mogelijk
                String depTimesExpr = "/ReisMogelijkheden/ReisMogelijkheid" +
                        "[Status[not(text()='NIET-MOGELIJK')]]/ActueleVertrekTijd";
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

            //find next departure time in List
            int nextIndex = -1;
            //convert to date to compare
            for (int i = 0; i < nsTimes.size(); i++) {
                Date nsDate = convertNSToDate(nsTimes.get(i));
                if (current.before(nsDate)) {
                    nextIndex = i; //index of next departure time.
                    break;
                }
            }

            //nstimes contains all ns departure times in ns-text format

            if (nsTimes.size() < timesNumber) {
                Log.e("nstimes size is ", Integer.toString(nsTimes.size()));
                Toast.makeText(context, "Warning, due to NS messing up, results may be inaccurate",
                        Toast.LENGTH_LONG).show();
            }

            Calendar[] depTimes = new Calendar[timesNumber];

            if (nextIndex < timesNumber / 2) { //if index is lower than what it should be
                if (nextIndex == -1) {
                    Log.e("nextIndex", "no next departure time!");
                } else {
                    Log.e("nextIndex", "is too small: " + Integer.toString(nextIndex));
                }
            } else {
                //index is index of next dept time of all the xml deptimes in nsTimes
                //get departure times around next time
                for (int i = 0; i < depTimes.length; i++) {
                    if (nextIndex - timesNumber / 2 + i < nsTimes.size()) {
                        //if not out of bounds... (happens when ns returns <timesNumber times total)
                        depTimes[i] = convertNSToCal(nsTimes.get(nextIndex - timesNumber / 2 + i));
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
    public Calendar nextDeparture(int[] direction) {
        //some initialisation
        int from = direction[0];
        int to = direction[1];

        //set update time according to departure times without delay
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        int minutes = cal.get(Calendar.MINUTE);
        int depart = 0;

        //get departure time
        if (from == EHV) {
            if (to == Heeze) {
                depart = 4;
            } else if (to == RDaal) {
                depart = 1;
            }
        } else if (from == Heeze) {
            if (to == EHV || to == RDaal) {
                depart = 15;
            }
        } else if (from == RDaal) {
            if (to == EHV || to == Heeze) {
                depart = 20;
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

    /**
     * Convert the instance variables to and from, set by the numberpickers,
     * to a string to be used in {@link AsyncTask ()}
     *
     * @param toFrom the to or from variable
     * @return string of city
     */
    public String convertCityToString(int toFrom) {
        return cities[toFrom];
    }


    /**
     * get arrival time of voyage, given departure time
     * SETS response
     *
     * @param depTime departure time ns-format
     * @param field   do you want delays or arrival times? Should be ns node string
     * @return arrival time
     */
    public String getNSStringByDepartureTime(String depTime, String field,
                                             String arrivalResponse, int from, int to) {
        if (depTime == null) {
            return null;
        } else {
            String arrivalTime = "+0";
            if (response == null) {
                response = "No response from NS or first time";
            }
            try {
                //create java DOM xml parser
                DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder;
                builder = builderFactory.newDocumentBuilder();

                //create XPath object
                XPath xPath = XPathFactory.newInstance().newXPath();

                String arrivalOrDepExpr;


                //parse xml with the DOM parser
                Document xmlDocument = builder.parse(new ByteArrayInputStream(response.getBytes()));

                if (from == EHV && to == RDaal) {
                    arrivalOrDepExpr = "/ReisMogelijkheden/ReisMogelijkheid" +
                            "[ActueleVertrekTijd[text()='" + depTime + "']]/ActueleAankomstTijd";

                    //deptime is departure time in EHV
                    //get new deptime, the first departure time in Breda
                    // which is later than the arrival time
                    NodeList arrivalNodeList = (NodeList) xPath.compile(arrivalOrDepExpr).evaluate(
                            xmlDocument, XPathConstants.NODESET);

                    if (arrivalNodeList.getLength() == 0) {
                        //there is no arrivalTime
                        return "arr: No breda arrival time.";
                    }

                    //arrivalNodeList should contain the (hopefully only one) arrival time in Breda
                    String bredaArrivalTime = arrivalNodeList.item(0).getFirstChild().getNodeValue();

                    //get next departure times
                    //arrivalResponse contains xml Breda-RDaal

                    //if going to RDaal, means arrival time is in arrivalResponse
                    xmlDocument = builder.parse(new ByteArrayInputStream(arrivalResponse.getBytes()));

                    //get all breda departure times
                    String depExpr = "//ActueleVertrekTijd";

                    NodeList BredaDepNodeList = (NodeList) xPath.compile(depExpr).evaluate(
                            xmlDocument, XPathConstants.NODESET);

                    List<String> BredaDepNSTimes = new ArrayList<>();

                    //use dep times from xml
                    for (int i = 0; i < BredaDepNodeList.getLength(); i++) {
                        BredaDepNSTimes.add(i, BredaDepNodeList.item(i).getFirstChild().getNodeValue());
                    }

                    //compare with breda arrival time
                    Date BredaArrivalDate = convertNSToDate(bredaArrivalTime);

                    //find next departure time in List
                    int nextIndex = -1;
                    //convert to date to compare
                    for (int i = 0; i < BredaDepNSTimes.size(); i++) {
                        Date nsDate = convertNSToDate(BredaDepNSTimes.get(i));
                        if (BredaArrivalDate.before(nsDate)) {
                            nextIndex = i; //i is index of next departure time.
                            break;
                        }
                    }

                    if (nextIndex == -1) {
                        Log.e("breda ", "departure time mistake ");
                    } else {
                        //depTime becomes next Breda departure Time
                        depTime = BredaDepNSTimes.get(nextIndex);
                    }
                }

                //now (possibly again) arrival time with depTime.
                // depTime may have been updated to Breda depTime
                arrivalOrDepExpr = "/ReisMogelijkheden" +
                        "/ReisMogelijkheid[ActueleVertrekTijd[text()='" + depTime + "']]/" + field;

                NodeList nodeList = (NodeList) xPath.compile(arrivalOrDepExpr).evaluate(
                        xmlDocument, XPathConstants.NODESET);

                if (nodeList.getLength() == 0) {
                    //there is no arrivalTime
                    return "No delays.";
                }
                //set arrivalTime using the arrivalTime found
                arrivalTime = nodeList.item(0).getFirstChild().getNodeValue();

            } catch (ParserConfigurationException | SAXException |
                    IOException | XPathExpressionException e) {
                e.printStackTrace();
            }

            return arrivalTime;
        }
    }

    /**
     * convert ns-format to HH:mm
     *
     * @param nsTime ns time
     * @return string
     */
    public String convertNSToString(String nsTime, int from, int to) {
        if (nsTime == null) {
            return "No time selected";
        } else {
            Calendar c = convertNSToCal(nsTime);
            if (from == EHV && to == RDaal) { //if going to Rdaal
                //round time to nearest ten minutes
                int unroundedMinutes = c.get(Calendar.MINUTE);
                int mod = unroundedMinutes % 10;
                c.add(Calendar.MINUTE, 20); //add 20 minutes for bike time
                c.add(Calendar.MINUTE, mod < 5 ? -mod : (10 - mod));
            }
            return convertCalendarToString(c);
        }
    }

    /**
     * convert calendar object to string object in HH:mm format
     *
     * @param c calendar object
     * @return string object
     */
    public String convertCalendarToString(Calendar c) {
        if (c == null) {
            return "No time selected";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
            return sdf.format(c.getTime());
        }
    }
}
