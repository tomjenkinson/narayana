package org.jboss.narayana.blacktie.jatmibroker.xatmi.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.narayana.blacktie.jatmibroker.core.conf.ConfigurationException;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Connection;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.ConnectionException;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Response;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Service;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.TPSVCINFO;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.X_OCTET;

public class TPACallService implements Service {
    private static final Logger log = LogManager.getLogger(TPACallService.class);

    public Response tpservice(TPSVCINFO svcinfo) throws ConnectionException, ConfigurationException {
        log.info("testtpacall_service");
        int len = 20;
        X_OCTET toReturn = (X_OCTET) svcinfo.getConnection().tpalloc("X_OCTET", null);
        toReturn.setByteArray("testtpacall_service".getBytes());
        return new Response(Connection.TPSUCCESS, 0, toReturn, 0);
    }
}
