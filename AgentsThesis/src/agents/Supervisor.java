package agents;

import behaviours.Records;
import behaviours.writeCSV;
import jade.core.AID;
import jade.core.Agent;
import jade.core.ServiceException;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.messaging.TopicManagementHelper;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;


public class Supervisor extends Agent{

	/**
	 * The supervisor agent is responsible for recording each operation 
	 * and have the right to 
	 */
	private static final long serialVersionUID = 1L;
	
	protected void setup() {
		/** Registration with the DF */
		DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("SupervisorAgent");
        sd.setName(getName());      
        dfd.setName(getAID());
        dfd.addServices(sd);
		try {
      
	        DFService.register(this,dfd);
	            	
        	//register the topic -- 'Prediction'
	        TopicManagementHelper topicHelper = (TopicManagementHelper) getHelper(TopicManagementHelper.SERVICE_NAME);
	        final AID topic = topicHelper.createTopic("Prediction");
	        topicHelper.register(topic);  
	        
	        //add the internal record behaviour
	         addBehaviour(new recordBehaviour(this));	
	         
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

class recordBehaviour extends CyclicBehaviour {

	/**
	 * This behavior is used for 
	 * recording operations during each sampling time
	 */
	private static final long serialVersionUID = 1L;
	
	int m=8760;
	String SenderName;
	private double Pd, Pboiler, Pstorage, Pchp1, Pchp2;
	private int chp1switch, chp2switch;
	boolean chp1state, chp2state;
	
	int k=1;
	public recordBehaviour(Agent myAgent){
		super(myAgent);
		this.myAgent=myAgent;
	}
	
	@Override
	public void action() {
		
		ACLMessage msg=myAgent.receive();
			
		if(msg==null) {
			block();
			return;
		}
		
		try {	
										
			SenderName=msg.getSender().getLocalName();
			
			switch(SenderName) {
		
			case "Prediction":
				Pd=Double.parseDouble(msg.getContent());
				break;
				
			case "chp1":
				
				String MsgCHP1=msg.getContent();
				//Supervisor agent has the right to restart the chp1 agent
				if(MsgCHP1.equals("Error1")){		    
				    myAgent.getContainerController().createNewAgent("chp1", AgentCHP1.class.getName(), new Object[0]).start();			    
				    }
				
				else{					
					chp1switch=Integer.parseInt(MsgCHP1);
					chp1state=true;
					if(chp1switch==1){
						Pchp1=AgentCHP1.Pchp1;
						}
					else {
						Pchp1=0;
						}
					}
				break;
				
			case "chp2":
				
				String MsgCHP2=msg.getContent();
				//Supervisor agent has the right to restart the chp2 agent
				if(MsgCHP2.equals("Error2")){
				    myAgent.getContainerController().createNewAgent("chp2", AgentCHP2.class.getName(), new Object[0]).start();
				}
				else{
				chp2switch=Integer.parseInt(MsgCHP2);
				chp2state=true;
				if(chp2switch==1){
					Pchp2=AgentCHP2.Pchp2;
				}
				else {
					Pchp2=0;
				}
				}
		
				break;
				
			case "boiler": 
				Pboiler=Double.parseDouble(msg.getContent());
				break;
				
			case "storage":
				Pstorage=Double.parseDouble(msg.getContent());
				break;	
				
			}
			
			if(SenderName.equals("boiler")) {
				
				Records record=new Records(k, Pd, Pchp1, Pchp2, Pboiler, Pstorage);
				System.out.println(record);
				
				//write into csv file
				//add the write csv behaviour
				myAgent.addBehaviour(new writeCSV(myAgent,record));								
				k++;
				Pd=0;Pboiler=0;Pstorage=0;
				chp1state=false;
				chp2state=false;			
			}
						
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}		
		
	}
	
}







