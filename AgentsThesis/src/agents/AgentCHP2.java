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

public class AgentCHP2 extends Agent {

	/**
	 * This is the CHP2 agent which owns the second priority
	 * @Ming
	 */
	private static final long serialVersionUID = 1145383232096899641L;
	public static double Pchp2=2.155;
	
	protected void setup() {
		/** Registration with the DF */
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("CHP2Agent");
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
            addBehaviour(new receiveMessagechp2(this));
            
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
			ACLMessage chp2Error = new ACLMessage(ACLMessage.INFORM);
			//send error message to Supervisor agent
			chp2Error.addReceiver(new AID("Supervisor", AID.ISLOCALNAME));
			chp2Error.setContent("Error2");
			this.send(chp2Error);
		} catch (FIPAException e) {
			e.printStackTrace();
		}		
	}

}


class receiveMessagechp2 extends CyclicBehaviour {

	/**
	 * CHP2 receives messages and classifies the senders
	 */
	private static final long serialVersionUID = 1L;
    private String SenderName;
    private Agent myAgent;
    private double Pd;
    private double Eth;
    private double Pchp1;
    private boolean chp1state;
    
    public receiveMessagechp2(Agent myAgent) {
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
	        	
	        case "chp1" :
	        	String chp1switch=msg.getContent();
	        	chp1state=true;
	        	if(chp1switch.equals("1")) {
	        		Pchp1=AgentCHP1.Pchp1;
	        	}
	        	else {
	        		Pchp1=0;
	        	}        	
	        	break;	

	        }
	        
	        //after collecting the useful values, chp2 will make the decision
			if(Pd!=0 &&Eth!=0 && chp1state!=false){			
	        myAgent.addBehaviour(new CHP2Decision(myAgent, Pd, Eth, Pchp1));
	        Pd=0;
	        Eth=0;
	        chp1state=false;     
	        }     	    	
	    }
	    
	    catch(Exception ex) {
	    	ex.printStackTrace();
	    }	
	    	    
	}	
		
}


class CHP2Decision extends OneShotBehaviour {

	/**
	 * This is the internal decision behaviour which only belongs to the CHP2 agent
	 * The decision algorithm is based on the control strategy
	 */
	
	private static final long serialVersionUID = 1L;
	private int times=Timer.chp2switchtimes;
	private double Pchp1;
	private double Pd;
	private double Eth;
	private int switchoo;
	private ArrayList<AID> al=new ArrayList<AID> ();
	
	
	public CHP2Decision(Agent myAgent, double Pd, double Eth, double Pchp1) {
    	this.myAgent=myAgent;
    	this.Pd=Pd;
    	this.Eth=Eth;
    	this.Pchp1=Pchp1;
	}
	
	
	@Override
	public void action() {
		
		ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
		
		//decide by the switch times
		if (times<4) {
			adddecision();
		}
		else {
			switchoo=0;
		}		
		
		/**
		 * Decision is done and print the operation, send to the storage
		 */
		System.out.println("chp2 operation is "+switchoo);
		al.add(new AID("storage", AID.ISLOCALNAME));
		al.add(new AID("boiler", AID.ISLOCALNAME));
		al.add(new AID("Supervisor", AID.ISLOCALNAME));
		al.add(new AID("TCP", AID.ISLOCALNAME));
		String content=String.valueOf(switchoo);
		msg.setContent(content);
		myAgent.addBehaviour(new SendMessage(msg, myAgent, al));
		Timer timerchp2=new Timer(switchoo);
		timerchp2.GetCHP2Times();		
	}

		
	
	private void adddecision() {
		
		if(Eth>Storage.capacity*Storage.max) {
			if((Eth/Storage.sampt)>Pd){
				switchoo=0;
			}			
			else {
				add1decision();
			}
		}
		else if(Eth<Storage.capacity*Storage.min) {
			add1decision();
		}
		else {
			if(Pchp1>=Pd){
				switchoo=0;			
			}
			else {
				add1decision();
			}		
		}	
	}

	private void add1decision() {

		switchoo=1;

	}
	
}












