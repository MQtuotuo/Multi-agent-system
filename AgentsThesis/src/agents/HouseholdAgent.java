package agents;

import java.util.ArrayList;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class HouseholdAgent extends Agent{

	/**
	 * This is the household agent which receives heat consumption from the TCP agent
	 * @Ming
	 */
	private static final long serialVersionUID = 1L;
	
	protected void setup() {
		/** Registration with the DF */
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("CHP1Agent");
        sd.setName(getName());
        dfd.setName(getAID());
        dfd.addServices(sd);
        try {
			DFService.register(this,dfd);
			
			//add the internal behaviour to receive messages from TCP and send data to prediction agent
			addBehaviour(new SendConsumption(this));
			
		} catch (FIPAException e) {
			e.printStackTrace();
		}
	}

}


/**
 * This class is used by Household Agent
 * 1)  get Pconsumption from TCP agent
 * 2)  send Pconsumption to Prediction Agent
 * @author Ming
 *
 */

 class SendConsumption extends CyclicBehaviour {
	
	
	String[][] arrayValues = new String[8760][2];
	ArrayList<String> al=new ArrayList<String> ();
   
	
	public SendConsumption(Agent myAgent) {
		super(myAgent);
	}
	  
	private static final long serialVersionUID = 1L;  
	
	@Override
	public void action() {
		
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		ACLMessage msgfromMatlab=myAgent.receive();
		if (msgfromMatlab!=null) {
			msg.setContent(msgfromMatlab.getContent());
			//System.out.println("household receive   "+msgfromMatlab.getContent());
			msg.addReceiver( new AID("Prediction", AID.ISLOCALNAME));	
			myAgent.send(msg);
		}
		else {
			block();
		}
		
	}

}