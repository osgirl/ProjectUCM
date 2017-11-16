package hays.co.uk.search;

import intradoc.common.SystemUtils;

import java.util.StringTokenizer;

public class IsOnlyJobTitleQueryBuilder extends AbstractHaysQueryBuilder {
	
	public IsOnlyJobTitleQueryBuilder(boolean isFuzzy, boolean isOnlyJobTitle,String thes_name,String alertprofileid) {
		super(isFuzzy, isOnlyJobTitle,thes_name,alertprofileid);
		// TODO Auto-generated constructor stub
	}

	//@Override
	public void buildQuery() {
		// TODO Auto-generated method stub
		if( this.getInputString().length() > 0 ) {
			String synstring = this.getInputString();
			String synstringqoutes=synstring;
			if(synstring.charAt(0) =='\"' && synstring.charAt(synstring.length()-1) =='\"' ){
				synstring=synstring.substring(1, synstring.length()-1);
			}
			StringTokenizer commaTokenizer = new StringTokenizer(synstring,",");
			SystemUtils.trace("hays_search", "Inside IsOnlyJobTitleQueryBuilder");
			String isOnlyjobTitle = null;
			int count=0;
			while(commaTokenizer.hasMoreTokens()){
				String token = commaTokenizer.nextToken().trim();
				 token = token.replaceAll("-", "\\\\-");
			     token = token.replaceAll("_", "\\\\_");
				if(count>0)
				{
				    SystemUtils.trace("hays_search", "Count is greater than 0");
				    queryPart.append("ACCUM");
				  	count=count-1;
				}
				
		        SystemUtils.trace("hays_search", "ISOnlyJobTitle TOken ='" + token + "'");
		       
			
					
			
		
    	isOnlyjobTitle = token.replaceAll("[ ]+", " ");	
    	SystemUtils.trace("hays_search","isOnlyjobTitle Value"+isOnlyjobTitle);
    	//jobTitlePart.append(" (").append(jobTitle).append(") WITHIN dDocTitle ");
    	queryPart.append(" ($(SYN(").append(isOnlyjobTitle).append(", ").append(thes_name).append(") WITHIN dDocTitle ))*4 ACCUM (SYN (").append(isOnlyjobTitle).append(", ").append(thes_name).append(") WITHIN dDocTitle )");
    	// added for stemming
    	if(synstringqoutes.charAt(0) =='\"' && synstringqoutes.charAt(synstring.length()-1) =='\"' ){
    		queryPart.append(" ACCUM (($").append(isOnlyjobTitle).append(") WITHIN dDocTitle )*2 ");
    	}
    	else{
    	isOnlyjobTitle = isOnlyjobTitle.replaceAll(" ", " and \\$ ");
    	queryPart.append(" ACCUM (($").append(isOnlyjobTitle).append(") WITHIN dDocTitle )*2 ");
    	SystemUtils.trace("hays_search","queryPart"+queryPart);
    	}
    	count=count+1;
			}
						    		
			queryPart.append(")");
			
	}

}
}
