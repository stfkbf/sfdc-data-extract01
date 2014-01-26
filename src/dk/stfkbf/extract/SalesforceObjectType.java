package dk.stfkbf.extract;

import java.util.ArrayList;
import java.util.HashMap;

import com.sforce.soap.partner.ChildRelationship;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.FieldType;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;

public class SalesforceObjectType {

	public static HashMap<String, SalesforceObjectType> objectsByName = new HashMap<String, SalesforceObjectType>();
	public static HashMap<String, String> objectSystemFields = new HashMap<String, String>();
	//TODO: Move into properties
	public static String REFERENCE_DATA = "BusinessArea__c|ControlledFunctionType__c|CustomerType__c|InvestmentType__c|PermissionType__c|PermissionFunctionAssociation__c";
	public static String SYSTEM_FIELDS = "IsEmailBounced|OwnerId|CreatedById|CreatedDate|LastModifiedById|LastModifiedDate|SystemModstamp|LastViewedDate|LastReferencedDate|IsDeleted";
	
	static {
		objectSystemFields.put("Contact","Name");
		objectSystemFields.put("Case","IsClosed|CaseNumber");
		objectSystemFields.put("Task","AccountId|IsArchived|IsClosed");
	}
	
	private PartnerConnection connection;

	private String name;

	private boolean isLeaf = false;
	private boolean isRoot = true;
	private boolean isProcessed = false;

	private ArrayList<SalesforceFieldType> fields;
	private ArrayList<SalesforceObjectType> children;

	public SalesforceObjectType(PartnerConnection connection, String name) {
		this.connection = connection;
		this.name = name;
		this.fields = new ArrayList<SalesforceFieldType>();
		this.children = new ArrayList<SalesforceObjectType>();

		objectsByName.put(name, this);

		String objectToDescribe = name;

		try {
			DescribeSObjectResult[] dsrArray = this.connection
					.describeSObjects(new String[] { objectToDescribe });

			DescribeSObjectResult dsr = dsrArray[0];

			System.out.println("\nObject Name: " + dsr.getName());

			for (int i = 0; i < dsr.getFields().length; i++) {
				Field field = dsr.getFields()[i];
				//field.getType();
				if (!field.getName().matches(SYSTEM_FIELDS) && !(objectSystemFields.containsKey(dsr.getName()) && field.getName().matches(objectSystemFields.get(dsr.getName()))) ){
					if (field.getType().equals(FieldType.reference)) {
						//Loop over the possible links
						for (int j = 0; j < field.getReferenceTo().length; j++){
							if (field.getReferenceTo()[j].matches(".*__c|Account|Case|Contact|Task")) {
								if (!objectsByName.containsKey(field.getReferenceTo()[j])) {
									objectsByName.put(field.getReferenceTo()[j], new SalesforceObjectType(connection, field.getReferenceTo()[j]));
								}	
								if (!isDuplicateField(field.getName())){
									this.fields.add(new SalesforceFieldType(field.getName(), field.getType().toString(), objectsByName.get(field.getReferenceTo()[j])));
									this.isRoot = false;
								}
							}
						}
					} else {					
						this.fields.add(new SalesforceFieldType(field.getName(), field.getType().toString()));
					}
				}

			}

			if (dsr.getChildRelationships().length == 0){
				this.isLeaf = true;
			}
			
			for (int i = 0; i < dsr.getChildRelationships().length; i++) {
				ChildRelationship child = dsr.getChildRelationships()[i];
				
				boolean isChildReferenceData = child.getChildSObject().matches(REFERENCE_DATA);
				boolean isParentReferenceData = this.name.matches(REFERENCE_DATA);
				
				if (!(isParentReferenceData && !isChildReferenceData) && child.getChildSObject().matches(".*__c|Account|Case|Contact|Task")) {
					if (!objectsByName.containsKey(child.getChildSObject())) {
						objectsByName.put(child.getChildSObject(), new SalesforceObjectType(connection, child.getChildSObject()));
					}
					this.children.add(objectsByName.get(child.getChildSObject()));
				}
			}

		} catch (ConnectionException ce) {
			ce.printStackTrace();
		}

	}
	
	private boolean isDuplicateField(String name){
		boolean result = false;
		
		for (SalesforceFieldType field : this.fields){
			if (field.getName().equals(name)){
				result = true;
				break;
			}					
		}
		
		return result;
	}
	
	public boolean canProcess(){
		return this.canProcess(false);
	}
	
	public boolean canProcess(boolean verbose){
		boolean ready = true;
		
		if (!(this.isProcessed && this.isRoot)){
			for (SalesforceFieldType field : this.fields){
				if (field.isReference() && !field.getReferenceObject().equals(this) && !field.getReferenceObject().isProcessed){
					if (verbose) System.out.println(field.getName());
					ready = false;
					break;
				}
			}
		}
		
		return ready;
	}
	
	public void process(){
		System.out.println(this.getQuery());
		this.isProcessed = true;
	}
	
	public String getQuery(){
		String query = "SELECT";
		
		int i = 0;
		for (i = 0; i < (this.fields.size() - 1); i++){
			query = query + " " + this.fields.get(i).getName() + ",";
		}
		query = query + " " + this.fields.get(i).getName();
		
		query = query + " FROM " + this.name;
		
		return query;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isLeaf() {
		return isLeaf;
	}

	public void setLeaf(boolean isLeaf) {
		this.isLeaf = isLeaf;
	}

	public boolean isProcessed() {
		return isProcessed;
	}

	public void setProcessed(boolean isProcessed) {
		this.isProcessed = isProcessed;
	}

	public ArrayList<SalesforceFieldType> getFields() {
		return fields;
	}

	public void setFields(ArrayList<SalesforceFieldType> fields) {
		this.fields = fields;
	}

	public ArrayList<SalesforceObjectType> getChildren() {
		return children;
	}

	public void setChildren(ArrayList<SalesforceObjectType> children) {
		this.children = children;
	}

	public boolean isRoot() {
		return isRoot;
	}

	public void setRoot(boolean isRoot) {
		this.isRoot = isRoot;
	}
	
	
	
}
