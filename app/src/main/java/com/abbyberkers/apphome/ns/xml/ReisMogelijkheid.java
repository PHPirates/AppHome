package com.abbyberkers.apphome.ns.xml;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Converting the xml tag ReisMogelijkheid and its children we need to an object.
 *
 * strict = false because we do not use all elements from the xml.
 */
@Root(name = "ReisMogelijkheid", strict = false)
public class ReisMogelijkheid {

    /** Scheduled departure time. */
    @Element(name = "GeplandeVertrekTijd")
    public String departureTime;

    /**
     * Delay of departure. This is not required as this
     * tag is only present when there is a delay. */
    @Element(name = "VertrekVertraging", required = false)
    public String departureDelay;

    /** Actual arrival time. */
    @Element(name = "ActueleAankomstTijd")
    public String arrivalTime;

    /** Empty constructor is necessary. */
    public ReisMogelijkheid() {}
}
