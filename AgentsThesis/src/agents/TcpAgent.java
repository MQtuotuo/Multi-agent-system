package agents;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ThreadedBehaviourFactory;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class TcpAgent extends Agent {

	/**
	 * The TCP agent works as an interface to exchange values 
	 * between JAVA and MATLAB
	 */
	private static final long serialVersionUID = 1L;
	
	// Class variables
	ServerSocket srvr = null;	
	Socket skt = null;
	PrintStream out;
	BufferedReader buf;
	
	// Constructor
	public TcpAgent() 
	{
		super();
	}

	
	protected void setup() {
		System.out.println("Agent started");
		// Create the TCP connection
		
		/** Registration with the DF */
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("TCPAgent");
        sd.setName(getName());       
        dfd.setName(getAID());
        dfd.addServices(sd);
        
				try 
				{
					DFService.register(this,dfd);
					
					// Create server and socket
					srvr = new ServerSocket(1234);
					skt = srvr.accept();
					System.out.println("Server connection initiated");

					// Create writer and reader to send and receive data
				     out = new PrintStream(skt.getOutputStream());
					 buf = new BufferedReader(new InputStreamReader(skt.getInputStream()));
					
						//initialized the models in Matlab
						out.println(1);//chp1
						out.println(1);//chp2
						out.println(1);//boiler operation
						out.println(0);//boiler
						out.println(0);//storage charging or discharging
					 						
						
					ThreadedBehaviourFactory thb=new ThreadedBehaviourFactory();					
					fromMatlabToAgents fma=new fromMatlabToAgents(this);
					fromAgentsToMatlab fam=new fromAgentsToMatlab(this);
					addBehaviour(thb.wrap(fma));
					addBehaviour(thb.wrap(fam));

				} 
				catch (IOException e) 
				{
					e.printStackTrace();
				} catch (FIPAException e) {
					e.printStackTrace();
				}
}
	
	
class fromMatlabToAgents extends CyclicBehaviour{

	/**
	 * This behavior is used for
	 * receiving consumption and storage capacity from MATLAB
	 * and sending to household/storage agent
	 */
	private static final long serialVersionUID = 1L;
	private ArrayList<String> FromMatlab = new ArrayList<String> ();
	ACLMessage msgHouse=new ACLMessage(ACLMessage.INFORM);
	ACLMessage msgStorage=new ACLMessage(ACLMessage.INFORM);
	
	String SenderName;
    ArrayList<Double> al=new ArrayList<Double>();
	double chp1switch, chp2switch, Pboiler, Pstorage; 
	boolean chp1state, chp2state;
	

	public fromMatlabToAgents(Agent myAgent) {
		super(myAgent);
	}

	@Override
	public void action() {
		
		boolean flag=true;
		try {
		while(flag) {
			
			//receive data from client side		
			String str;			
			str = buf.readLine();
						
			if(str == null || "".equals(str)) {
				flag=false;
			}
			else {			
				FromMatlab.add(str);				
			}					
		}
		}catch (IOException e) {
			e.printStackTrace();
		}
			
		String Pcom=FromMatlab.get(0);
		String Peth=FromMatlab.get(1);
		System.out.println(FromMatlab);
		//System.out.println("The consumption data is   "+FromMatlab.get(0));    //the consumption data
		//System.out.println("The capacity data is  "+FromMatlab.get(1));    //the storage capacity data
		
		//send separately to household and storage agent
		msgHouse.setContent(Pcom);
		msgHouse.addReceiver(new AID("Household", AID.ISLOCALNAME));
		myAgent.send(msgHouse);
		msgHouse.clearAllReceiver();
		
		msgStorage.setContent(Peth);
		msgStorage.addReceiver(new AID("storage", AID.ISLOCALNAME));
		myAgent.send(msgStorage);
		msgStorage.clearAllReceiver();
		FromMatlab.clear();			
		
	}
	
}
	
	
	

class fromAgentsToMatlab extends CyclicBehaviour{
	/**
	 * This behavior is used for
	 * receiving operations from different agents
	 * sending operations to MATLAB
	 * */
	 
	private static final long serialVersionUID = 1L;

	private String SenderName;
	private ArrayList<Double> al=new ArrayList<Double>();
	double chp1switch, chp2switch, Pboiler, Pstorage; 
	boolean chp1state, chp2state;
	public fromAgentsToMatlab(Agent myAgent) {
		// TODO Auto-generated constructor stub
		this.myAgent=myAgent;
	}
	@Override
	public void action() {
	
    ACLMessage msg=myAgent.receive();
		
		if (msg==null) {
			block();
			return;
		}
		else {
            SenderName=msg.getSender().getLocalName();
			
			switch(SenderName) {
						
			case "chp1":
				chp1switch=Double.parseDouble(msg.getContent());
				chp1state=true;
				break;
				
			case "chp2":
				chp2switch=Double.parseDouble(msg.getContent());
				chp2state=true;	
				break;
				
			case "boiler": 
				Pboiler=Double.parseDouble(msg.getContent());
				break;
				
			case "storage":
				Pstorage=Double.parseDouble(msg.getContent());
				break;	  	
	    }
			
			//After receiving the last value from the boiler, sending the values to Matlab
			if(SenderName.equals("boiler")) {
				
				al.add(chp1switch);
				al.add(chp2switch);
				al.add(Double.parseDouble("1"));
				al.add(Pboiler);
				al.add(Pstorage);
				System.out.println("The data are  "+al);
				
				//send to MATLAB
				for(int i=0;i<al.size();i++){
					out.println(al.get(i));					
				}
			
				Pboiler=0;Pstorage=0;
				chp1state=false;
				chp2state=false;	
				al.clear();
				
				}
		}		
	}
}

}














