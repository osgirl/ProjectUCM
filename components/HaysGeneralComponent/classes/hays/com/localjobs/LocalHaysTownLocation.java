package hays.com.localjobs;

public class LocalHaysTownLocation {
     String locationId;
     String url;
     String defaultDescription;
     String defaultDescription1;
     String defaultDescription2;
    
     public LocalHaysTownLocation(String locationId, String url, String defaultDescription, String defaultDescription1, String defaultDescription2){
    	 this.locationId = locationId;
    	 this.url = url;
    	 this.defaultDescription = defaultDescription;
    	 this.defaultDescription1 = defaultDescription1;
    	 this.defaultDescription2 = defaultDescription2;
     }
     
     public String getDefaultDescription() {
		return defaultDescription;
	}

	public void setDefaultDescription(String defaultDescription) {
		this.defaultDescription = defaultDescription;
	}

	public String getDefaultDescription1() {
		return defaultDescription1;
	}

	public void setDefaultDescription1(String defaultDescription1) {
		this.defaultDescription1 = defaultDescription1;
	}

	public String getDefaultDescription2() {
		return defaultDescription2;
	}

	public void setDefaultDescription2(String defaultDescription2) {
		this.defaultDescription2 = defaultDescription2;
	}

	public String getLocationId() {
		return locationId;
	}
	public void setLocationId(String locationId) {
		this.locationId = locationId;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
}
