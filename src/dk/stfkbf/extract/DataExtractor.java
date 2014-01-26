package dk.stfkbf.extract;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.SaveResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

public class DataExtractor {

	private Properties properties;
	
	private String sourceUsername;
	private String sourcePassword;
	private String targetUsername;
	private String targetPassword;
	private String endpoint;
	
	public static void main(String[] args) throws Exception {
		DataExtractor extractor;
		try {
			extractor = new DataExtractor();

			extractor.runExtract();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public DataExtractor() throws IOException, ConnectionException {
		this.properties = new Properties();
		this.properties.load(new FileInputStream("config.properties"));

		this.sourceUsername = this.properties.getProperty("sourceusername");
		this.sourcePassword = this.properties.getProperty("sourcepassword");
		this.targetUsername = this.properties.getProperty("targetusername");
		this.targetPassword = this.properties.getProperty("targetpassword");

		this.endpoint = this.properties.getProperty("endpoint");
	}	
	
	public void runExtract() throws Exception {
		PartnerConnection sourceConnection;
		PartnerConnection targetConnection;

		sourceConnection = login(this.sourceUsername, this.sourcePassword);
		targetConnection = login(this.targetUsername, this.targetPassword);

		//String objectName = "TestAccount__c";
		//String objectId = "a0IE000000D8ZQ1MAN";
		String objectName = "Account";
		String objectId = "001b000000McHqmAAF";
		
		SalesforceObjectType objectType = new SalesforceObjectType(sourceConnection, objectName);

		SalesforceObject object = new SalesforceObject(sourceConnection, objectId, objectType);
		
		SalesforceObject[] objects = new SalesforceObject[SalesforceObject.objectsById.values().size()];
		objects = SalesforceObject.objectsById.values().toArray(objects);
		
		for (int i = 0; i < objects.length; i++) {
			for (int j = 0; j < objects.length; j++) {
				if (!objects[j].isProcessed() && objects[j].canProcess()) {				
					try {
						SaveResult[] result = targetConnection.create(new SObject[] { objects[j].getSObject() });

						if (result.length == 1 && result[0].isSuccess()) {
							objects[j].setTargetId(result[0].getId());
							objects[j].setProcessed(true);
						} else {
							System.out.println("ERROR: Failed to write " + objects[j].getObjectType().getName() + " with Id: " + objects[j].getId());
							System.out.println(result[0].toString());
						}
					} catch (ConnectionException e) {
						System.out.println("ERROR: Failed to write " + objects[j].getObjectType().getName() + " with Id: " + objects[j].getId());
						e.printStackTrace();
					}
				}
			}
		}
		
		System.out.println("Populated " + objectType.getName() + " - " + object.getId());
	}

	private PartnerConnection login(String username, String password) throws ConnectionException {
		ConnectorConfig connector = new ConnectorConfig();
		connector.setUsername(username);
		connector.setPassword(password);

		System.out.println("AuthEndPoint: " + this.endpoint);
		connector.setAuthEndpoint(this.endpoint);
		PartnerConnection connection = new PartnerConnection(connector);

		return connection;
	}

}
