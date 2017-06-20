package behaviours;

import java.util.ArrayList;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;


public class SendMessage extends OneShotBehaviour {
        
	/**
	 * This class is used for sending or broadcasting messages
	 */
	private static final long serialVersionUID = 1L;

		private ACLMessage msg;        
        private Agent myAgent;
        private ArrayList<AID> al=new ArrayList<AID> ();
        boolean flag=true;
        
        public SendMessage(ACLMessage msg,Agent myAgent, ArrayList<AID> al){
            super();
            this.msg = msg;
            this.myAgent = myAgent;
            this.al=al;
        }
        
        @Override
        public void action() {
         
        	//Add the receivers to the arraylist
            for(int i=0; i<al.size();i++) {
            	msg.addReceiver(al.get(i));
            }

           while(flag) {
            myAgent.send(msg);
            flag=false;
            al.clear(); //after sending the messages, the receiver list will clear
           }
            
        }
    }

