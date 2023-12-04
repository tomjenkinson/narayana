/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.oasis_open.docs.ws_tx.wscoor._2006._06;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.WebEndpoint;
import jakarta.xml.ws.WebServiceClient;
import jakarta.xml.ws.WebServiceFeature;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.1.2-hudson-182-RC1
 * Generated source version: 2.1
 * 
 */
@WebServiceClient(name = "ActivationService", targetNamespace = "http://docs.oasis-open.org/ws-tx/wscoor/2006/06", wsdlLocation = "wsdl/wscoor-activation-binding.wsdl")
public class ActivationService
    extends Service
{

    private final static URL ACTIVATIONSERVICE_WSDL_LOCATION;
    private final static Logger logger = Logger.getLogger(org.oasis_open.docs.ws_tx.wscoor._2006._06.ActivationService.class.getName());

    static {
        URL url = null;
        try {
            URL baseUrl;
            baseUrl = org.oasis_open.docs.ws_tx.wscoor._2006._06.ActivationService.class.getResource("");
            url = new URL(baseUrl, "wsdl/wscoor-activation-binding.wsdl");
        } catch (MalformedURLException e) {
            logger.warning("Failed to create URL for the wsdl Location: 'wsdl/wscoor-activation-binding.wsdl', retrying as a local file");
            logger.warning(e.getMessage());
        }
        ACTIVATIONSERVICE_WSDL_LOCATION = url;
    }

    public ActivationService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public ActivationService() {
        super(ACTIVATIONSERVICE_WSDL_LOCATION, new QName("http://docs.oasis-open.org/ws-tx/wscoor/2006/06", "ActivationService"));
    }

    /**
     * 
     * @return
     *     returns ActivationPortType
     */
    @WebEndpoint(name = "ActivationPortType")
    public ActivationPortType getActivationPortType() {
        return super.getPort(new QName("http://docs.oasis-open.org/ws-tx/wscoor/2006/06", "ActivationPortType"), ActivationPortType.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link jakarta.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns ActivationPortType
     */
    @WebEndpoint(name = "ActivationPortType")
    public ActivationPortType getActivationPortType(WebServiceFeature... features) {
        return super.getPort(new QName("http://docs.oasis-open.org/ws-tx/wscoor/2006/06", "ActivationPortType"), ActivationPortType.class, features);
    }

}
