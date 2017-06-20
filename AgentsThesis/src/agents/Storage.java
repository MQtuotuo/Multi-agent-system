package agents;

import java.util.ArrayList;
import behaviours.SendMessage;
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

public class Storage extends Agent {

	/**
	 * This is the storage agent which owns the third priority
	 * @Ming
	 */
	private static final long serialVersionUID = 1145383232096899641L;
	public static double sampt=1;        //sampling hour: 1h
	public static double capacity=10.10; //the storage capacity
    public static double min=0.3; //the minimum SOC
    public static double max=0.7; //the maximum SOC
  
	
	protected void setup() {
		/** Registration with the DF */
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("StorageAgent");
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
            addBehaviour(new receiveMessageStorage(this));
            
        }
        
        catch (FIPAException e) {
        	System.err.println(getLocalName()+" registration with DF unsucceeded. Reason: "+e.getMessage());
            doDelete();
        	} 
        
        catch (ServiceException ex) {
                System.err.println("Agent "+getLocalName()+": ERROR registering to topic \"Prediction\"");
            } 		
	}
}


class receiveMessageStorage extends CyclicBehaviour {

	/**
	 * Storage receives messages and classifies the senders
	 */
	private static final long serialVersionUID = 1L;
    private String SenderName, Eth;
    private double Ecth;
    private Agent myAgent;
    private double Pd, Pchp1, Pchp2;
    boolean chp1state, chp2state;
    private ArrayList<AID> al=new ArrayList<AID> ();
    
    public receiveMessageStorage(Agent myAgent) {
    	this.myAgent=myAgent;
    }
    

	@Override
	public void action() {
		
		ACLMessage msg=myAgent.receive();
		ACLMessage msgeth=new ACLMessage(ACLMessage.INFORM);	
			
		if (msg==null) {
			block();
			return;
		}
	    try {
	    	
	    	SenderName=msg.getSender().getLocalName();
	    	
	        switch(SenderName) {
	        
	        //receive capacity from TCP agent
	        case "TCP":
	        	 Eth=msg.getContent();
	        	 Ecth=Double.valueOf(Eth);
	        	 al.add(new AID("chp1", AID.ISLOCALNAME));
		         al.add(new AID("chp2", AID.ISLOCALNAME));
		         msgeth.setContent(Eth);
		         myAgent.addBehaviour(new SendMessage(msgeth, myAgent, al));
		         System.out.println("now my capacity is          "+Eth);
		         System.out.println(".................................storage Eth");
	        	 break;
	        
	        //receive Prediction from Prediction agent
	        case "Prediction":
	        	Pd=Double.parseDouble(msg.getContent());
	        	System.out.println("The prediction is "+Pd);
	        	break;
	        	
	        //Pchp1 
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
	        
	        //Pchp2
	        case "chp2" :
	        	
	        	String chp2switch=msg.getContent();
	        	chp2state=true;
	        	if(chp2switch.equals("1")) {
	        		Pchp2=AgentCHP2.Pchp2;
	        	}
	        	else {
	        		Pchp2=0;
	        	}        	
	        	break;    
	        	
	        
	        }    
	        
            //when receiving chp2 operation, then make storage operation
	        if(SenderName.equals("chp2")) {	        
	        	myAgent.addBehaviour(new StorageDecision(myAgent, Pd, Pchp1, Pchp2, Ecth));        	
	        	Pd=0;Pchp1=0;Pchp2=0;Ecth=0;
	            chp1state=false;
		        chp2state=false;
	        }      
	    }
	        
	    catch(Exception ex) {
	    	ex.printStackTrace();
	    }
	}


	
	

class StorageDecision extends OneShotBehaviour {

	/**
	 * This is the internal decision behaviour which only belongs to the storage agent
	 * The decision algorithm is based on the control strategy
	 */
	
	private static final long serialVersionUID = 1L;
	private double Pch, Ecth;
	private double Pd, Pchp1, Pchp2;
	ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
	private ArrayList<AID> al=new ArrayList<AID> ();
	 
	public StorageDecision(Agent myAgent, double Pd, double Pchp1, double Pchp2, double Ecth){
		this.myAgent=myAgent;
		this.Pd=Pd;
		this.Pchp1=Pchp1;
		this.Pchp2=Pchp2;	
		this.Ecth=Ecth;
	}

	@Override
	public void action() {
			
		if((Pchp1+Pchp2)>=Pd) {
			Pch=Pchp1+Pchp2-Pd;    //charging the storage
		}
				
		else {
			if(Ecth<Storage.capacity*Storage.min) {
				Pch=0;                //do nothing
			}
			else {
				addDecision();   //discharging the storage
			}
		}
		
		
		System.out.println("storage decision is       "+Pch);
		
		al.add(new AID("boiler", AID.ISLOCALNAME));
		al.add(new AID("Supervisor", AID.ISLOCALNAME));
		al.add(new AID("TCP", AID.ISLOCALNAME));
    	String content1=String.valueOf(Pch);
		msg.setContent(content1);
		myAgent.addBehaviour(new SendMessage(msg, myAgent, al));

	}

	private void addDecision() {

		if(Ecth/Storage.sampt>(Pd-Pchp1-Pchp2)) {

			Pch=Pchp1+Pchp2-Pd;         //if provided  > demand
		}
		else {
			
			Pch=-Ecth/Storage.sampt*Storage.max;  
		}
	}
}
}










