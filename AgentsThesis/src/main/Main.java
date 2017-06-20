package main;

import java.io.IOException;
import agents.AgentCHP1;
import agents.AgentCHP2;
import agents.Boiler;
import agents.Storage;
import agents.Supervisor;
import agents.PredictionAgent;
import agents.HouseholdAgent;
import agents.TcpAgent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

/**
 * This is the main class to run the agents software
 * The agents will be started after this class runs
 * The sniffer agent also runs
 * @author Ming
 *
 */

public class Main {
	static ContainerController home;
    static String GUI="-gui";
    static String PORT="1099";
    static String NOTIFICATION="jade.core.event.NotificationService;jade.core.messaging.TopicManagementService";
    
	public static void main(String[] args) throws IOException {
			
		Runtime rt = Runtime.instance();
		Profile p;
		p = new ProfileImpl();
		p.setParameter(Profile.GUI, GUI);
		p.setParameter(Profile.LOCAL_PORT, PORT);
		p.setParameter(Profile.SERVICES,NOTIFICATION);

		home = rt.createMainContainer(p);			
		rt.setCloseVM(true);
		
		try {
	
			AgentController a = home.createNewAgent("rma","jade.tools.rma.rma", new Object[0]);
			a.start();	
			
			a = home.createNewAgent("sniffer","jade.tools.sniffer.Sniffer", new Object[0]);
			a.start();		
			
			a = home.createNewAgent("Prediction",PredictionAgent.class.getName(), new Object[0]);
			a.start();		
			
			a = home.createNewAgent("chp1",AgentCHP1.class.getName(), new Object[0]);
			a.start();
			
			a = home.createNewAgent("chp2",AgentCHP2.class.getName(), new Object[0]);
			a.start();
			
			a = home.createNewAgent("boiler",Boiler.class.getName(), new Object[0]);
			a.start();
			
			a = home.createNewAgent("storage",Storage.class.getName(), new Object[0]);
			a.start();
			
			a = home.createNewAgent("Supervisor",Supervisor.class.getName(), new Object[0]);
			a.start();
			
			a = home.createNewAgent("Household",HouseholdAgent.class.getName(), new Object[0]);
			a.start();
			
			a = home.createNewAgent("TCP",TcpAgent.class.getName(), new Object[0]);
			a.start();

		} catch (StaleProxyException e) {
			e.printStackTrace();
		}

	}
}
