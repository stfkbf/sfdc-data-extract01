package dk.stfkbf.extract;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;

public class SalesforceObject {

	private PartnerConnection connection;
	
	private String id;
	private String targetId;
	private SObject object;
	private boolean processed = false;
	
	private ArrayList<SalesforceObject> parents;
	private ArrayList<SalesforceObject> children;
	
	public static HashMap<String, SalesforceObject> objectsById = new HashMap<String, SalesforceObject>();
	
	private SalesforceObjectType objectType;
		
	public SalesforceObject(PartnerConnection connection, String id, SalesforceObjectType objectType){
		this.connection = connection;
		this.id = id;
		this.objectType = objectType;
		this.parents = new ArrayList<SalesforceObject>();
		this.children = new ArrayList<SalesforceObject>();
		
		
		objectsById.put(this.id, this);
		System.out.println(this.id + " " + this.objectType.getName());
		
		String query = objectType.getQuery() + " WHERE ID = '" + id + "'";
		
		try {
			QueryResult qr = connection.query(query);
			
			if (qr.getSize() == 1) {
				this.object = qr.getRecords()[0];
				
				// get all the parents
				for (SalesforceFieldType field : this.objectType.getFields()){
					if (field.isReference() && object.getField(field.getName()) != null){
						String objectId = object.getField(field.getName()).toString();
						
						if (!objectsById.containsKey(objectId)){
							objectsById.put(objectId, new SalesforceObject(connection,objectId,field.getReferenceObject()));
						}
						parents.add(objectsById.get(objectId));
					}
				}
				
				// get all the children				
				for (SalesforceObjectType childObjectType : this.objectType.getChildren()){
					for (SalesforceFieldType field : childObjectType.getFields()){
						if (field.isReference() && field.getReferenceObject().getName().equals(this.objectType.getName())){
							//TODO: Add configurable where clause
							String childQuery = "SELECT Id FROM " + childObjectType.getName() + " WHERE " + field.getName() + " = '" + this.id + "'";
						
							QueryResult childQr = this.connection.query(childQuery);
							
							boolean done = false;

					         if (childQr.getSize() > 0) {
					            while (!done) {
					               SObject[] records = childQr.getRecords();
					               for (int i = 0; i < records.length; ++i) {
					            	   String objectId = records[i].getId();
										
										if (!objectsById.containsKey(objectId)){
											objectsById.put(objectId, new SalesforceObject(connection,objectId,childObjectType));
										}
										children.add(objectsById.get(objectId));
					               }

					               if (qr.isDone()) {
					                  done = true;
					               } else {
					                  qr = connection.queryMore(qr.getQueryLocator());
					               }
					            }
					         }
						}
					}
				}
			}
			
		} catch (ConnectionException e) {
			e.printStackTrace();
		}		
	}

	public SObject getSObject() throws Exception{
		SObject result = new SObject();
		
		result.setType(this.object.getType());		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat dateTimeFormat = new SimpleDateFormat ("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date dateValue;
		for (SalesforceFieldType field : this.objectType.getFields()){
			if (field.isReference() && this.object.getField(field.getName()) != null){
				System.out.println();
				String id = this.object.getField(field.getName()).toString();
				result.setField(field.getName(), SalesforceObject.objectsById.get(id).getTargetId());
			} else {
				if (!field.getName().equals("Id")) {
					Object value = this.object.getField(field.getName());
					if (value != null && field.getFieldType() == "date"){
						dateValue = dateFormat.parse(value.toString());
						result.setField(field.getName(), dateValue);
					} else if (value != null && field.getFieldType() == "datetime") {
						dateValue = dateTimeFormat.parse(value.toString().replaceAll("Z$", "+0000"));
						result.setField(field.getName(), dateValue);
					} else if (value != null && field.getFieldType() == "_boolean"){							
						result.setField(field.getName(), Boolean.parseBoolean(value.toString()));
					} else {
						result.setField(field.getName(), value);
					}
				}
			}
		}
		
		return result;
	}
	
	public boolean canProcess(){
		boolean result = true;
		
		if (!this.isProcessed()){
			for (SalesforceObject parent : this.parents){
				if (!parent.isProcessed()){
					result = false;
					break;
				}
			}
		}
		
		return result;
	}
	
	
	
	public String getTargetId() {
		return targetId;
	}

	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public SObject getObject() {
		return object;
	}

	public void setObject(SObject object) {
		this.object = object;
	}

	public ArrayList<SalesforceObject> getParents() {
		return parents;
	}

	public void setParents(ArrayList<SalesforceObject> parents) {
		this.parents = parents;
	}
	
	public ArrayList<SalesforceObject> getChildren() {
		return children;
	}

	public void setChildren(ArrayList<SalesforceObject> children) {
		this.children = children;
	}

	public SalesforceObjectType getObjectType() {
		return objectType;
	}

	public void setObjectType(SalesforceObjectType objectType) {
		this.objectType = objectType;
	}

	public boolean isProcessed() {
		return processed;
	}

	public void setProcessed(boolean processed) {
		this.processed = processed;
	}
	
	
}
