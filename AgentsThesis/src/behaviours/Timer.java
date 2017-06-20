package behaviours;

import java.util.ArrayList;

/**
 * This class is used for switch times counting
 * If time exceeds 24h, the time count will initialize again
 * @author Ming
 *
 */

public class Timer {

	private int switchoo;
	public static int chp1switchtimes, chp2switchtimes;
	private static ArrayList<Integer> operationschp1=new ArrayList<Integer> ();
	private static ArrayList<Integer> operationschp2=new ArrayList<Integer> ();
	
	public Timer( int switchoo){
		this.switchoo=switchoo;
	}
	
    public void GetCHP1Times(){
    	 	
    	operationschp1.add(switchoo);
    	
    	int operationSize=operationschp1.size();

    	if(operationSize>1 && operationSize<24) {	
    		
    		if(operationschp1.get((operationSize-2))!=operationschp1.get(operationSize-1)) {
    			chp1switchtimes++;				
    			}
    	
    	}
    	else if(operationSize==1){
    		chp1switchtimes=0;
    	}
    	else {
    		chp1switchtimes=0;
    		operationschp1.clear();
    	}
    }  	
    
    public void GetCHP2Times(){
    	
    	operationschp2.add(switchoo);
    	
    	int operationSize=operationschp2.size();
    	
    	if(operationSize>1 && operationSize<24) {	
    		
    		if(operationschp2.get((operationSize-2))!=operationschp2.get(operationSize-1)) {
    			chp2switchtimes++;				
    			}
    		}
    	
    	else if(operationSize==1){
    		chp2switchtimes=0;
    	}
    	else {
    		chp2switchtimes=0;
    		operationschp2.clear();
    		}
    	}	
}
