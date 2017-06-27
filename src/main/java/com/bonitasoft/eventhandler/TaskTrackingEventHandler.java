package com.bonitasoft.eventhandler;

import org.bonitasoft.engine.bpm.flownode.ActivityStates;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionReadException;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
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
public class TaskTrackingEventHandler implements SHandler<SEvent> {
    private Logger logger = Logger.getLogger("com.bonitasoft.eventhandler");
    private long tenantId;

    private SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS", Locale.FRANCE);
    private static final String ACTIVITYINSTANCE_CREATED = "ACTIVITYINSTANCE_CREATED";

    public TaskTrackingEventHandler(long tenantId){
        super();
        this.tenantId = tenantId;

    }

    public void execute(SEvent sEvent) throws SHandlerExecutionException {
        Object eventObject = sEvent.getObject();

            if (eventObject instanceof SActivityInstance) {
                SActivityInstance activityInstance = (SActivityInstance) eventObject;
                try {

                final String caseId = ""+activityInstance.getRootContainerId();
                String output = null;
                if(sEvent.getType().equals(ACTIVITYINSTANCE_CREATED)) {
                    final Date startDate =  new Date(System.currentTimeMillis());
                    output = "TASK TRACKING - START -  caseId: " + caseId+ " - taskId: "+ activityInstance.getId() + " - taskName: " + activityInstance.getName()+ " - date: " + format.format(startDate);


                }else{
                    final Date startDate =  new Date(activityInstance.getLastUpdateDate());
                    final Date endDate =  new Date(activityInstance.getReachedStateDate());
                    if(activityInstance.getStateName().equals(ActivityStates.COMPLETED_STATE)) {
                        output = "TASK TRACKING - END -  caseId: " + caseId+ " - taskId: "+ activityInstance.getId()+ " - taskName: " + activityInstance.getName() + " - date: " + format.format(startDate) +
                                " - Current State: " + activityInstance.getStateId() + ":" + activityInstance.getStateName() +
                                " - Previous State: " + (activityInstance.getPreviousStateId() == 37?"37:executing":activityInstance.getPreviousStateId());

                    }else{
                        if(activityInstance.getPreviousStateId() != 0) {
                            output = "TASK TRACKING - OTHER -  caseId: " + caseId + " - taskId: " + activityInstance.getId()+ " - taskName: " + activityInstance.getName() + " - startDate: " + format.format(startDate) + " - endDate: " + format.format(endDate) +
                                    " - Current State: " + activityInstance.getStateId() + ":" + activityInstance.getStateName() +
                                    " - Previous State: " + (activityInstance.getPreviousStateId() == 37 ? "37:executing" : activityInstance.getPreviousStateId());
                        }
                    }
                }
                    if (output != null)
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
        if (eventObject instanceof SActivityInstance) {
            SActivityInstance activityInstance = (SActivityInstance) eventObject;
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
