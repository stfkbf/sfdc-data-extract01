package dk.stfkbf.extract;

public class SalesforceFieldType {
	private String name;
	private boolean reference = false;
	private SalesforceObjectType referenceObject;

	public SalesforceFieldType(String name) {	
		this.name = name;
	}
	
	public SalesforceFieldType(String name, SalesforceObjectType referenceObject) {
		this.name = name;
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

}
