package dk.stfkbf.extract;

public class SalesforceFieldType {
	private String name;
	private String fieldType;
	private boolean reference = false;
	private SalesforceObjectType referenceObject;

	public SalesforceFieldType(String name, String fieldType) {	
		this.name = name;
		this.fieldType = fieldType;
	}
	
	public SalesforceFieldType(String name, String fieldType, SalesforceObjectType referenceObject) {
		this.name = name;
		this.fieldType = fieldType;
		this.referenceObject = referenceObject;
		this.reference = true;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isReference() {
		return reference;
	}

	public void setReference(boolean reference) {
		this.reference = reference;
	}

	public SalesforceObjectType getReferenceObject() {
		return referenceObject;
	}

	public void setReferenceObject(SalesforceObjectType referenceObject) {
		this.referenceObject = referenceObject;
	}

	public String getFieldType() {
		return fieldType;
	}

	public void setFieldType(String fieldType) {
		this.fieldType = fieldType;
	}
	
	

}
