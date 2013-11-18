package dk.stfkbf.extract;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.sforce.soap.partner.ChildRelationship;
import com.sforce.soap.partner.DescribeGlobalResult;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.FieldType;
import com.sforce.soap.partner.GetUserInfoResult;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.PicklistEntry;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.SaveResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

public class DataExtractor {
	
	private static BufferedReader reader = new BufferedReader(
	         new InputStreamReader(System.in));

	   public PartnerConnection sourceConnection;
	   public PartnerConnection targetConnection;
	   String authEndPoint = "";

	public static void main(String[] args) {
		DataExtractor extractor = new DataExtractor("https://login.salesforce.com/services/Soap/u/29.0");
		
		extractor.runExtract();
	}
	
	public void runExtract(){
		if (this.login()){
			String objectName = "TestAccount__c";
			String objectId = "a0IE000000D8ZQ1MAN";
		
			SalesforceObjectType objectType = new SalesforceObjectType(this.sourceConnection, objectName);
		
			SalesforceObject object = new SalesforceObject(sourceConnection,objectId,objectType);
		
			System.out.println("Populated " + objectType.getName() + " - " + object.getId());
						
			if (this.targetLogin()){
				for (SalesforceObject object1 : SalesforceObject.objectsById.values()){
					for (SalesforceObject object2 : SalesforceObject.objectsById.values()){
						System.out.println("Checking: " + object2.getObjectType().getName() + " " + object2.getId());
						if (!object2.isProcessed() && object2.canProcess()){
							try {
								SaveResult[] result = this.targetConnection.create(new SObject[] { object2.getSObject() });
								
								if (result.length == 1 && result[0].isSuccess()){
									object2.setTargetId(result[0].getId());
									object2.setProcessed(true);
								}
							} catch (ConnectionException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			this.targetLogout();
			}
			
			this.logout();
		}
	}
	
	public DataExtractor(String authEndPoint){
		this.authEndPoint = authEndPoint;
	}

	private boolean login() {
	      boolean success = false;
	      String username = "";
	      String password = "";

	      try {
	         ConnectorConfig config = new ConnectorConfig();
	         config.setUsername(username);
	         config.setPassword(password);

	         System.out.println("AuthEndPoint: " + authEndPoint);
	         config.setAuthEndpoint(authEndPoint);

	         sourceConnection = new PartnerConnection(config);
	         printUserInfo(config);

	         success = true;
	      } catch (ConnectionException ce) {
	         ce.printStackTrace();
	      } 

	      return success;
	   }
	
	private void logout() {
	      try {
	         sourceConnection.logout();
	         System.out.println("Logged out.");
	      } catch (ConnectionException ce) {
	         ce.printStackTrace();
	      }
	   }

	private boolean targetLogin() {
	      boolean success = false;
	      String username = "";
	      String password = "";

	      try {
	         ConnectorConfig config = new ConnectorConfig();
	         config.setUsername(username);
	         config.setPassword(password);

	         System.out.println("AuthEndPoint: " + authEndPoint);
	         config.setAuthEndpoint(authEndPoint);

	         targetConnection = new PartnerConnection(config);
	         printUserInfo(config);

	         success = true;
	      } catch (ConnectionException ce) {
	         ce.printStackTrace();
	      } 

	      return success;
	   }
	
	private void targetLogout() {
	      try {
	         targetConnection.logout();
	         System.out.println("Logged out.");
	      } catch (ConnectionException ce) {
	         ce.printStackTrace();
	      }
	   }

	
	private void printUserInfo(ConnectorConfig config) {
	      try {
	         GetUserInfoResult userInfo = sourceConnection.getUserInfo();

	         System.out.println("\nLogging in ...\n");
	         System.out.println("UserID: " + userInfo.getUserId());
	         System.out.println("User Full Name: " + userInfo.getUserFullName());
	         System.out.println("User Email: " + userInfo.getUserEmail());
	         System.out.println();
	         System.out.println("SessionID: " + config.getSessionId());
	         System.out.println("Auth End Point: " + config.getAuthEndpoint());
	         System.out
	               .println("Service End Point: " + config.getServiceEndpoint());
	         System.out.println();
	      } catch (ConnectionException ce) {
	         ce.printStackTrace();
	      }
	   }
	
	private void describeGlobalSample() {
	      try {
	         // describeGlobal() returns an array of object results that
	         // includes the object names that are available to the logged-in user.
	         DescribeGlobalResult dgr = sourceConnection.describeGlobal();

	         System.out.println("\nDescribe Global Results:\n");
	         // Loop through the array echoing the object names to the console
	         for (int i = 0; i < dgr.getSobjects().length; i++) {
	            System.out.println(dgr.getSobjects()[i].getName());
	         }
	      } catch (ConnectionException ce) {
	         ce.printStackTrace();
	      }
	   }
	
	private void describeSObjectsSample() {
	      String objectToDescribe = "Contact";

	      try {
	         // Call describeSObjects() passing in an array with one object type
	         // name
	         DescribeSObjectResult[] dsrArray = sourceConnection
	               .describeSObjects(new String[] { objectToDescribe });

	         // Since we described only one sObject, we should have only
	         // one element in the DescribeSObjectResult array.
	         DescribeSObjectResult dsr = dsrArray[0];

	         // First, get some object properties
	         System.out.println("\n\nObject Name: " + dsr.getName());

	         if (dsr.getCustom())
	            System.out.println("Custom Object");
	         if (dsr.getLabel() != null)
	            System.out.println("Label: " + dsr.getLabel());

	         // Get the permissions on the object

	         if (dsr.getCreateable())
	            System.out.println("Createable");
	         if (dsr.getDeletable())
	            System.out.println("Deleteable");
	         if (dsr.getQueryable())
	            System.out.println("Queryable");
	         if (dsr.getReplicateable())
	            System.out.println("Replicateable");
	         if (dsr.getRetrieveable())
	            System.out.println("Retrieveable");
	         if (dsr.getSearchable())
	            System.out.println("Searchable");
	         if (dsr.getUndeletable())
	            System.out.println("Undeleteable");
	         if (dsr.getUpdateable())
	            System.out.println("Updateable");

	         System.out.println("Number of fields: " + dsr.getFields().length);

	         // Now, retrieve metadata for each field
	         for (int i = 0; i < dsr.getFields().length; i++) {
	            // Get the field
	            Field field = dsr.getFields()[i];

	            // Write some field properties
	            System.out.println("Field name: " + field.getName());
	            System.out.println("\tField Label: " + field.getLabel());

	            // This next property indicates that this
	            // field is searched when using
	            // the name search group in SOSL
	            if (field.getNameField())
	               System.out.println("\tThis is a name field.");

	            if (field.getRestrictedPicklist())
	               System.out.println("This is a RESTRICTED picklist field.");

	            System.out.println("\tType is: " + field.getType());

	            if (field.getLength() > 0)
	               System.out.println("\tLength: " + field.getLength());

	            if (field.getScale() > 0)
	               System.out.println("\tScale: " + field.getScale());

	            if (field.getPrecision() > 0)
	               System.out.println("\tPrecision: " + field.getPrecision());

	            if (field.getDigits() > 0)
	               System.out.println("\tDigits: " + field.getDigits());

	            if (field.getCustom())
	               System.out.println("\tThis is a custom field.");

	            // Write the permissions of this field
	            if (field.getNillable())
	               System.out.println("\tCan be nulled.");
	            if (field.getCreateable())
	               System.out.println("\tCreateable");
	            if (field.getFilterable())
	               System.out.println("\tFilterable");
	            if (field.getUpdateable())
	               System.out.println("\tUpdateable");

	            // If this is a picklist field, show the picklist values
	            if (field.getType().equals(FieldType.picklist)) {
	               System.out.println("\t\tPicklist values: ");
	               PicklistEntry[] picklistValues = field.getPicklistValues();

	               for (int j = 0; j < field.getPicklistValues().length; j++) {
	                  System.out.println("\t\tValue: "
	                        + picklistValues[j].getValue());
	               }
	            }

	            // If this is a foreign key field (reference),
	            // show the values
	            if (field.getType().equals(FieldType.reference)) {
	               System.out.println("\tCan reference these objects:");
	               for (int j = 0; j < field.getReferenceTo().length; j++) {
	                  System.out.println("\t\t" + field.getReferenceTo()[j]);
	               }
	            }
	            	         	            
	            System.out.println("");
	         }
	         
	         for (int i = 0; i < dsr.getChildRelationships().length; i++) {
	            	ChildRelationship child = dsr.getChildRelationships()[i];
	            	System.out.println("Chield Relationship Name: " + child.getRelationshipName());
	            	System.out.println("Chield Relationship Field: " + child.getField());
	            	System.out.println("Chield Relationship Object: " + child.getChildSObject());
	            	System.out.println("");
	            }
	         
	      } catch (ConnectionException ce) {
	         ce.printStackTrace();
	      }
	   }
	
	public void querySample() {
	      String soqlQuery = "SELECT FirstName, LastName FROM Contact";
	      try {
	         QueryResult qr = sourceConnection.query(soqlQuery);
	         boolean done = false;

	         if (qr.getSize() > 0) {
	            System.out.println("\nLogged-in user can see "
	                  + qr.getRecords().length + " contact records.");

	            while (!done) {
	               System.out.println("");
	               SObject[] records = qr.getRecords();
	               for (int i = 0; i < records.length; ++i) {
	                  String fName = records[i].getField("FirstName").toString();
	                  String lName = records[i].getField("LastName").toString();

	                  if (fName == null) {
	                     System.out.println("Contact " + (i + 1) + ": " + lName);
	                  } else {
	                     System.out.println("Contact " + (i + 1) + ": " + fName
	                           + " " + lName);
	                  }
	               }

	               if (qr.isDone()) {
	                  done = true;
	               } else {
	                  qr = sourceConnection.queryMore(qr.getQueryLocator());
	               }
	            }
	         } else {
	            System.out.println("No records found.");
	         }
	      } catch (ConnectionException ce) {
	         ce.printStackTrace();
	      }
	   }

}
