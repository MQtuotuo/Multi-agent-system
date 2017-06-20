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

public class Boiler extends Agent {

	/**
	 * This is the boiler agent which owns the fourth priority
	 * @Ming
	 */
	private static final long serialVersionUID = 1145383232096899641L;
	
	protected void setup() {
		/** Registration with the DF */
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("BoilerAgent");
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
            addBehaviour(new receiveMessageboiler(this));
            
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

class receiveMessageboiler extends CyclicBehaviour {

	/**
	 * Boiler receives messages and classifies the senders
	 */
	private static final long serialVersionUID = 1L;
    boolean chp1state,chp2state;
    private String SenderName;
    private Agent myAgent;
    private double Pd, Pchp1, Pchp2, Pch;
    
    public receiveMessageboiler(Agent myAgent) {
    	
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
	        	
	        	
	        case "storage":
	        	Pch=Double.parseDouble(msg.getContent());
	        		        	
	        	break;
	        }
	        	        
            //when receiving storage operation, then make storage operation
	        if (SenderName.equals("storage")) {
	          	
	        	myAgent.addBehaviour(new BoilerDecision(myAgent, Pd, Pchp1, Pchp2, Pch)); 
	            Pd=0; Pchp1=0;Pchp2=0;Pch=0;
	            chp1state=false;
	            chp2state=false;
	        }
	                	    	
	    }
	    catch(Exception ex) {
	    	ex.printStackTrace();
	    }	
	}
		
}

class BoilerDecision extends OneShotBehaviour {

	/**
	 * This is the internal decision behaviour which only belongs to the boiler agent
	 * The decision algorithm is based on the control strategy
	 */
	
	 private static final long serialVersionUID = 1L;
	 private Agent myAgent;
	 private double Pchp1;
	 private double Pchp2;
	 private double Pd;
	 private double Pch;
	 private double Pre;	 
	 private ArrayList<AID> al=new ArrayList<AID> ();
	
	public BoilerDecision(Agent myAgent, double Pd, double Pchp1, double Pchp2, double Pch) {
		this.myAgent=myAgent;
		this.Pchp1=Pchp1;
		this.Pchp2=Pchp2;
		this.Pd=Pd;
		this.Pch=Pch;
	}
	

	@Override
	public void action() {
		
		ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
		if(Pch>0) {    //the storage is charging
			Pre=0;
		}
		else {         //the storage is discharging Pch<0
			if((Pchp1+Pchp2-Pch)>=Pd){
				Pre=0;
			}
			else{
				Pre=Pd-(Pchp1+Pchp2-Pch);
			}
		}
	
		System.out.println("boiler will make the dicision: "+Pre);	
		al.add(new AID("Supervisor", AID.ISLOCALNAME));
		al.add(new AID("TCP", AID.ISLOCALNAME));
		String content=String.valueOf(Pre);
		msg.setContent(content);
		myAgent.addBehaviour(new SendMessage(msg, myAgent, al));
				
	}
	
}













