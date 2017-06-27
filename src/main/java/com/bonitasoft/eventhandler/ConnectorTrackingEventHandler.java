package com.bonitasoft.eventhandler;

import org.bonitasoft.engine.bpm.flownode.ActivityStates;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.events.model.SHandler;
import org.bonitasoft.engine.events.model.SHandlerExecutionException;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Created by Pablo Alonso de Linaje on 04/05/2017.
 */
public class ConnectorTrackingEventHandler implements SHandler<SEvent> {
    private Logger logger = Logger.getLogger("com.bonitasoft.eventhandler");
    private long tenantId;
    private SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS", Locale.FRANCE);
    private static final String CONNECTOR_INSTANCE_CREATED = "CONNECTOR_INSTANCE_CREATED";
    private static final String CONNECTOR_INSTANCE_DELETED = "CONNECTOR_INSTANCE_DELETED";


    public ConnectorTrackingEventHandler(long tenantId){
        super();
        this.tenantId = tenantId;
    }

    public void execute(SEvent sEvent) throws SHandlerExecutionException {
        Object eventObject = sEvent.getObject();

            if (eventObject instanceof SConnectorInstance) {
                SConnectorInstance connectorInstance = (SConnectorInstance) eventObject;
                try {
                    final Long containerId = connectorInstance.getContainerId();
                    SActivityInstance activityInstance = getTenantServiceAccessor().getActivityInstanceService().getActivityInstance(containerId);
                    final String caseId = "" +activityInstance.getRootProcessInstanceId();

                String output = null;
                final Date startDate =  new Date(System.currentTimeMillis());

                    output = "CONNECTOR TRACKING - " + connectorInstance.getState() +" -  caseId: " + caseId+ " - taskId: "+ activityInstance.getId()+ " - taskName: " + activityInstance.getName() + " - connectorId: " +connectorInstance.getId() + " - connector: " +connectorInstance.getName() +" - type: "+connectorInstance.getConnectorId()+ " - date: " + format.format(startDate);
                    logger.warning(output);
                }catch (Exception e){
                    logger.severe("TASK TRACKING - We have found an issue");
                    e.printStackTrace();
                }

            }







    }

    public boolean isInterested(SEvent sEvent) {

        boolean isInterested = false;

        // Get the object associated with the event
        Object eventObject = sEvent.getObject();

        // Check that event is related to a task
        if (eventObject instanceof SConnectorInstance) {
            isInterested = true;
        }

        return isInterested;
    }

    public String getIdentifier() {
        return UUID.randomUUID().toString();
    }
    private TenantServiceAccessor getTenantServiceAccessor()
            throws SHandlerExecutionException {
        try {
            ServiceAccessorFactory serviceAccessorFactory = ServiceAccessorFactory.getInstance();
            return serviceAccessorFactory.createTenantServiceAccessor(tenantId);
        } catch (Exception e) {
            throw new SHandlerExecutionException(e.getMessage(), null);
        }
    }

    // to convert Milliseconds into DD HH:MM:SS format.
    private String getDateFromMsec(long diffMSec) {
        int left = 0;
        int ss = 0;
        int mm = 0;
        int hh = 0;
        int dd = 0;
        left = (int) (diffMSec / 1000);
        ss = left % 60;
        left = (int) left / 60;
        if (left > 0) {
            mm = left % 60;
            left = (int) left / 60;
            if (left > 0) {
                hh = left % 24;
                left = (int) left / 24;
                if (left > 0) {
                    dd = left;
                }
            }
        }
        String diff = Integer.toString(dd) + " " + Integer.toString(hh) + ":"
                + Integer.toString(mm) + ":" + Integer.toString(ss);
        return diff;

    }
}
