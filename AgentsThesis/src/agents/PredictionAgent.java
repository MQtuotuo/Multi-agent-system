package agents;

import java.util.ArrayList;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.messaging.TopicManagementHelper;
import jade.lang.acl.ACLMessage;

public class PredictionAgent extends Agent{

	/**
	 * The Prediction Agent is used for forecasting the heating consumption from the measurements 
	 * Get data from  household's consumption 
	 * After prediction, this agent will send information to each CHP and heating agent and also keep the data in DB
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static long consumption;
	static long P, T;
	public static ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
	
	protected void setup() { 
		try {
			
			// Create the topic -- Prediction
			TopicManagementHelper topicHelper = (TopicManagementHelper) getHelper(TopicManagementHelper.SERVICE_NAME);
			final AID topic = topicHelper.createTopic("Prediction");
			msg.addReceiver(topic);						
			addBehaviour(new receiveHouseData(this));
		}	
		catch (Exception e) {
			System.err.println("Agent "+getLocalName()+": ERROR creating topic \"Prediction\"");
			e.printStackTrace();
		}
	}
}

class receiveHouseData extends CyclicBehaviour {

	/**
	 * Receive data from household and store them into an arraylist
	 * Refresh the historical data
	 * Use a prediction algorithm to forecast the future demand
	 */
	private static final long serialVersionUID = 1L;
    private double test, Pd;
	private double stat=0.546;	 //smoothing constant
	
	private String Message_Content;
	ArrayList <Double> al = new ArrayList<Double> (); //store the heat consumption data
	public static String forevalue;
	
	public receiveHouseData(Agent myAgent){
          this.myAgent = myAgent;
    }
	
	
	//Simple Exponential Smoothing Algorithm
	@Override
	public void action() {
		
		 ACLMessage msg = myAgent.receive();
		 if(msg!=null) {

			 Message_Content=msg.getContent();
			 Double data=Double.parseDouble(Message_Content);
			 al.add(data);		
			 int size=al.size();
		
			 if(size==1) {
				 Pd=al.get(0); // Initialize the algorithm. The first prediction is the first hour heat consumption 
			 }
			 
			 else {
				 Pd=0;
				 for(int i=0; i<size; i++) {	
					 //the parameters in the algorithm
					 //add the parameters for a loop
					 test=stat*Math.pow((1-stat), (size-1-i))*(al.get(i));
				     Pd=Pd+test;
				 }			 
			 }			 

			 String msgPd=String.valueOf(Pd);			 
		     PredictionAgent.msg.setContent(msgPd);   
		     myAgent.send(PredictionAgent.msg);

		 }	
		 else {
			 block();
		 }
	}	
}



