package agents;


import java.util.ArrayList;
import behaviours.SendMessage;
import behaviours.Timer;
import jade.core.AID;
import jade.core.Agent;
import jade.core.ServiceException;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.messaging.TopicManagementHelper;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class AgentCHP1 extends Agent {

	/**
	 * This is the CHP1 agent which owns the first priority
	 * @Ming
	 */
	private static final long serialVersionUID = 1145383232096899641L;
	public static double Pchp1=2.155; // the maximum value of CHP1 generation power
	
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
               	
        	//register the topic -- 'Prediction'
            TopicManagementHelper topicHelper = (TopicManagementHelper) getHelper(TopicManagementHelper.SERVICE_NAME);
            final AID topic = topicHelper.createTopic("Prediction");
            topicHelper.register(topic);  
       
            //add the internal behaviour to receive messages
            addBehaviour(new CHP1ReceiveMessage(this));

        }
        
        catch (FIPAException e) {
        	System.err.println(getLocalName()+" registration with DF unsucceeded. Reason: "+e.getMessage());
            doDelete();
        	} 
        catch (ServiceException ex) {
                System.err.println("Agent "+getLocalName()+": ERROR registering to topic \"Prediction\"");
            } 	
	}
	
	protected void takeDown() {
		try {
			DFService.deregister(this);
			ACLMessage chp1Error = new ACLMessage(ACLMessage.INFORM);
			//send error message to Supervisor agent
			chp1Error.addReceiver(new AID("Supervisor", AID.ISLOCALNAME));
			chp1Error.setContent("Error1");
			this.send(chp1Error);
		} catch (FIPAException e) {
			e.printStackTrace();
		}		
	}
}



class CHP1ReceiveMessage extends CyclicBehaviour {

	/**
	 * CHP1 receives messages and classifies the senders
	 */
	private static final long serialVersionUID = 1L;
    private String SenderName;
    private Agent myAgent;
    private double Pd;
    private double Eth;
    
    public CHP1ReceiveMessage(Agent myAgent) {
    	this.myAgent=myAgent;
    }
    

	@Override
	public void action() {
		
		ACLMessage msg=myAgent.receive();
		
		if (msg==null) {
			block();
			return;
		}
	    try {
	    	SenderName=msg.getSender().getLocalName();
	    
	        switch(SenderName) {
	        	        
	        case "Prediction":
	        	Pd=Double.parseDouble(msg.getContent());
	        	break;
	        	
	        case "storage" : 
	        	Eth=Double.parseDouble(msg.getContent());
	        	break;     
	        }

	    }
	    catch(Exception ex) {
	    	ex.printStackTrace();
	    }	
	    
	    if(Eth!=0 && Pd!=0) {
	    	//After collecting the data, add the decision behaviour
	    	myAgent.addBehaviour(new CHP1Decision(myAgent, Eth));
	    	Eth=0;
	    	Pd=0;
	    	}
	    }
}


class CHP1Decision extends OneShotBehaviour {

	/**
	 * This is the internal decision behaviour which only belongs to the CHP1 agent
	 * The decision algorithm is based on the control strategy
	 */
	
	private static final long serialVersionUID = 1L;
	private Agent myAgent;
	private double Eth;
	private int switchoo;
	private int times=Timer.chp1switchtimes;
	
	ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
	private ArrayList<AID> al=new ArrayList<AID> ();
	
	public CHP1Decision(Agent myAgent, double Eth) {
		this.myAgent=myAgent;
		this.Eth=Eth;
		
	}

	@Override
	public void action() {
		
		//decide by the switch times
		if (times<4) {
			adddecision();
		}
		else {
			switchoo=0;
		}		
		
		/**
		 * Decision is done and print the operation, send to chp2
		 */
		Timer timerchp1=new Timer(switchoo);
		timerchp1.GetCHP1Times();
		String content=String.valueOf(switchoo);
		msg.setContent(content);
		al.add(new AID("chp2", AID.ISLOCALNAME));
		al.add(new AID("boiler", AID.ISLOCALNAME));
		al.add(new AID("storage", AID.ISLOCALNAME));
		al.add(new AID("Supervisor", AID.ISLOCALNAME));
		al.add(new AID("TCP", AID.ISLOCALNAME));
		myAgent.addBehaviour(new SendMessage(msg, myAgent, al));
			
	}

	private void adddecision() {
		
		if(Eth<Storage.capacity*Storage.min) {
			switchoo=1;
		}
		else if(Eth>Storage.capacity*Storage.max) {
			switchoo=0;
		}
		else {
			switchoo=1;		
		}		
	}
	
}






