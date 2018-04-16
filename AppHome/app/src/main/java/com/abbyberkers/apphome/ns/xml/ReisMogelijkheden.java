package com.abbyberkers.apphome.ns.xml;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Converting the xml tag "ReisMogelijkheden" to an object.
 */
@Root(name = "ReisMogelijkheden")
public class ReisMogelijkheden {

    /**
     * There are multiple "ReisMogelijkheid" tags inside a
     * "ReisMogelijkheden" tag, so we wrap them in a list.
     * inline = true because "ReisMogelijkheid" is a direct child
     * of "ReisMogelijkheden".
     */
    @ElementList(inline = true)
    public List<ReisMogelijkheid> journeys;

    /** Empty constructor is necessary. */
    public ReisMogelijkheden() {}
}
