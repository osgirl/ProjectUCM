package hays.co.uk.search;

import intradoc.common.SystemUtils;

public abstract class AbstractHaysQueryBuilder {
	protected StringBuffer queryPart = new StringBuffer();
	public StringBuffer getQueryPart() {
		return queryPart;
	}
	private boolean isFuzzy;
	protected boolean isOnlyJobTitle;
	protected String alertprofileid;
	public String thes_name;
	
	public boolean isOnlyJobTitle() {
		return isOnlyJobTitle;
	}
	private String inputString;
	
	
	public String getInputString() {
		return inputString;
	}

	public void setInputString(String inputString) {
		this.inputString = inputString;
	}

	public AbstractHaysQueryBuilder(boolean isFuzzy, boolean isOnlyJobTitle,String thes_name,String alertprofileid) {
		super();
		this.isFuzzy = isFuzzy;
		this.isOnlyJobTitle = isOnlyJobTitle;
		this.thes_name=thes_name;
		this.alertprofileid=alertprofileid;
		
	}
	
	public void buildQueryPart(){
		
        if( inputString != null && !"".equals(inputString)) {
        	SystemUtils.trace("hays_search", "First time keywords=" + this.getInputString() );
        	inputString = QueryUtils.decodeHaysSpecialKeywords(this.inputString);
        	inputString = inputString.trim();
        	inputString = QueryUtils.formatStringForReservedKeyWords(inputString);
        	buildQuery();
        }
	}
	public abstract void buildQuery();
}
