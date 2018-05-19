package com.abbyberkers.apphome.ns.xml;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import static com.abbyberkers.apphome.ns.XMLprocessingKt.maximumDelay;

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

    @Element(name = "AankomstVertraging", required = false)
    public String arrivalDelay;

    /** Actual arrival time. */
    @Element(name = "ActueleAankomstTijd")
    public String arrivalTime;

    @Element(name = "Status")
    public String status;

    /** Empty constructor is necessary. */
    public ReisMogelijkheid() {}

    public String delay() {
        return maximumDelay(arrivalDelay, departureDelay);
    }
}
