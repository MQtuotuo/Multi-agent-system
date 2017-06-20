package behaviours;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;

import java.io.FileWriter;
import java.io.IOException;

public class writeCSV extends OneShotBehaviour{

	/**
	 * receiving record object
	 * write into csv file
	 */
	private static final long serialVersionUID = 1L;
	private Records record;
	public writeCSV(Agent myAgent, Records record) {
		this.myAgent=myAgent;
		this.record=record;
	}
	
	//Delimiter used in CSV file
	private static final String COMMA_DELIMITER = ",";
	private static final String NEW_LINE_SEPARATOR = "\n";
	
	//CSV file header
    //private static final String FILE_HEADER = "id,Pd,Pchp1,Pchp2,Pboiler,Pstorage";
	
	FileWriter fileWriter=null;

	@Override
	public void action() {
		
		try {
			
		    fileWriter = new FileWriter("records.csv",true);
			
			//Write the CSV file header
			//fileWriter.append(FILE_HEADER.toString());
			
			//Add a new line separator after the header
			//fileWriter.append(NEW_LINE_SEPARATOR);

			//Write a new student object list to the CSV file
		
				fileWriter.append(String.valueOf(record.getId()));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(String.valueOf(record.getPd()));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(String.valueOf(record.getChp1()));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(String.valueOf(record.getChp2()));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(String.valueOf(record.getBoiler()));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(String.valueOf(record.getStorage()));
				fileWriter.append(NEW_LINE_SEPARATOR);
						
			System.out.println("CSV file was created successfully !!!");
			System.out.println("같같같같같같같같같같같같같같같같");
			
		} catch (Exception e) {
			System.out.println("Error in CsvFileWriter !!!");
			e.printStackTrace();
		} finally {
			
			try {
				fileWriter.flush();
				fileWriter.close();
			} catch (IOException e) {
				System.out.println("Error while flushing/closing fileWriter !!!");
                e.printStackTrace();
			}
			
		}
	
	}
	
}
