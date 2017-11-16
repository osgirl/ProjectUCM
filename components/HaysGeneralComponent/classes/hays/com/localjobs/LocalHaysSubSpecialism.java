package hays.com.localjobs;

public class LocalHaysSubSpecialism {
     String subspecialismId;
     String url;
     String isSubSpecialism;
     
     String description; //to temporarily hold description, to avoid calling ontology services again

     
     public LocalHaysSubSpecialism(String subspecialismId, String url, String isSubSpecialism){
    	 this.subspecialismId = subspecialismId;
    	 this.url = url;
    	 this.isSubSpecialism = isSubSpecialism;
     }

     public String getSubspecialismId() {
		return subspecialismId;
	}
	public void setSubspecialismId(String subspecialismId) {
		this.subspecialismId = subspecialismId;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
    public String getDescription() {
 		return description;
 	}

 	public void setDescription(String description) {
 		this.description = description;
 	}
 	public String getIsSubSpecialism()
 	{
 		return isSubSpecialism;
 	}
 	public void setIsSubSpecialism()
 	{
 		this.isSubSpecialism = isSubSpecialism;
 	}

}
