package com.bonitasoft.eventhandler;

import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionReadException;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
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
public class ProcessTrackingEventHandler implements SHandler<SEvent> {
    private Logger logger = Logger.getLogger("com.bonitasoft.eventhandler");
    private long tenantId;
    private boolean showSubProcesses;
    private SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.FRANCE);
    private static final String PROCESSINSTANCE_CREATED = "PROCESSINSTANCE_CREATED";

    public ProcessTrackingEventHandler(long tenantId, boolean showSubProcesses){
        super();
        this.tenantId = tenantId;
        this.showSubProcesses = showSubProcesses;
    }

    public void execute(SEvent sEvent) throws SHandlerExecutionException {
        Object eventObject = sEvent.getObject();

            if (eventObject instanceof SProcessInstance) {
                SProcessInstance processInstance = (SProcessInstance) eventObject;
                final Date startDate =  new Date(processInstance.getStartDate());
                String id = ""+processInstance.getId();
                try {
                    final SProcessDefinition processDefinition = getTenantServiceAccessor().getProcessDefinitionService().getProcessDefinition(processInstance.getProcessDefinitionId());
                    String root ="process";
                    if(showSubProcesses ){
                        if(processInstance.getId() == processInstance.getRootProcessInstanceId()) {
                            root = "MainProcess";
                        }else {
                            root = "SubProcess";
                            id = "Main("+processInstance.getRootProcessInstanceId()+") Sub("+id+")";
                        }
                    }
                    String output = null;
                    if(sEvent.getType().equals(PROCESSINSTANCE_CREATED)) {
                        output = "PROCESS TRACKING - START - "+root+": "+processDefinition.getName() + " "+ processDefinition.getVersion() +" - caseId: " + id + " - startDate: " + format.format(startDate);
                    }else {
                        final Long endDateLong = processInstance.getEndDate();
                        if (endDateLong != null && endDateLong != 0){
                            Date endDate = new Date(endDateLong);
                            Long diff = endDateLong - processInstance.getStartDate();
                            output = "PROCESS TRACKING - END - "+root+": "+processDefinition.getName() + " "+ processDefinition.getVersion() +" - caseId: " + id + " - startDate: " + format.format(startDate) + " - endDate: " + format.format(endDate) + " - took: "+getDateFromMsec(diff);
                        }
                    }
                    if(output != null)
                        logger.warning(output);
                } catch (SProcessDefinitionNotFoundException e) {
                    logger.severe("PROCESS TRACKING - ProcessDefinition with id "+ processInstance.getProcessDefinitionId() + " not found");
                } catch (SProcessDefinitionReadException e) {
                    logger.severe("PROCESS TRACKING - Unable to read ProcessDefinition with id "+ processInstance.getProcessDefinitionId());
                }
            }







    }

    public boolean isInterested(SEvent sEvent) {

        boolean isInterested = false;

        // Get the object associated with the event
        Object eventObject = sEvent.getObject();

        // Check that event is related to a task
        if (eventObject instanceof SProcessInstance) {
            SProcessInstance processInstance = (SProcessInstance) eventObject;
            if(showSubProcesses || processInstance.getId() == processInstance.getRootProcessInstanceId()) {
                if (sEvent.getType().equals(PROCESSINSTANCE_CREATED)) {
                    isInterested = true;
                } else {
                    final Long endDateLong = processInstance.getEndDate();
                    if (endDateLong != null && endDateLong != 0) {
                        isInterested = true;
                    }
                }
            }
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
