# TimeTracker
## Logger reader
Ensure that the Manifest contains this class as main class
Main-Class: com.bonitasoft.utils.LoggerReader

To execute:
java -jar ProcessTracking.jar "PATH TO THE TRACKER FILE"

After it reads the full file and prints the info you can ask for details of a given case.

## Event Handlers Setup
Edit file tomcat/conf/logging.properties and add the following

handlers = java.util.logging.ConsoleHandler, 1catalina.org.apache.juli.FileHandler, 2localhost.org.apache.juli.FileHandler, 3manager.org.apache.juli.FileHandler, 4host-manager.org.apache.juli.FileHandler, 5bonita.org.apache.juli.FileHandler, 6tracker.org.apache.juli.FileHandler
...
6tracker.org.apache.juli.FileHandler.level = ALL
6tracker.org.apache.juli.FileHandler.directory = ${catalina.base}/logs
6tracker.org.apache.juli.FileHandler.prefix = tracker.
...
com.bonitasoft.eventhandler.handlers = 6tracker.org.apache.juli.FileHandler
com.bonitasoft.eventhandler.level = INFO

Edit file cfg-bonita-events-api-impl-xml and add the following:

<beans xmlns="http://www.springframework.org/schema/beans" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:p="http://www.springframework.org/schema/p"
	xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.0.xsd">

	<bean id="processTrackingEventHandler" class="com.bonitasoft.eventhandler.ProcessTrackingEventHandler">        
        <constructor-arg name="tenantId" value="${tenantId}" />
		<constructor-arg name="showSubProcesses" value="true" />
    </bean>
	<bean id="taskTrackingEventHandler" class="com.bonitasoft.eventhandler.TaskTrackingEventHandler">        
        <constructor-arg name="tenantId" value="${tenantId}" />		
    </bean>
	<bean id="connectorTrackingEventHandler" class="com.bonitasoft.eventhandler.ConnectorTrackingEventHandler">        
        <constructor-arg name="tenantId" value="${tenantId}" />		
    </bean>
	
	<bean id="eventService" class="com.bonitasoft.engine.events.impl.ConfigurableEventServiceImpl">
		<constructor-arg name="handlers">
			 <map>
                <entry key="PROCESSINSTANCE_CREATED" value-ref="processTrackingEventHandler"/>				
				<entry key="PROCESSINSTANCE_DELETED" value-ref="processTrackingEventHandler"/>
				<entry key="ACTIVITYINSTANCE_CREATED" value-ref="taskTrackingEventHandler"/>				
				<entry key="ACTIVITYINSTANCE_STATE_UPDATED" value-ref="taskTrackingEventHandler"/>
				<entry key="CONNECTOR_INSTANCE_STATE_UPDATED" value-ref="connectorTrackingEventHandler"/>

            </map>
		</constructor-arg>
		<constructor-arg name="logger" ref="tenantTechnicalLoggerService" />
	</bean>

</beans>
