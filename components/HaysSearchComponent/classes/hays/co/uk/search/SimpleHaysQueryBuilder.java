package hays.co.uk.search;

import java.util.StringTokenizer;

import intradoc.common.SystemUtils;

public class SimpleHaysQueryBuilder extends AbstractHaysQueryBuilder{

	public SimpleHaysQueryBuilder(boolean isFuzzy, boolean isOnlyJobTitle,String thes_name,String alertprofileid) {
		super(isFuzzy, isOnlyJobTitle,thes_name,alertprofileid);
		// TODO Auto-generated constructor stub
	}

	
	public void buildQuery() {
		// TODO Auto-generated method stub
    	if( this.getInputString().length() > 0 ) {
    		/*updated for MR 209*/
    		String synstring = this.getInputString();
    		String synstringqoutes=synstring;
			if(synstring.charAt(0) =='\"' && synstring.charAt(synstring.length()-1) =='\"' )
			{
				synstring=synstring.substring(1, synstring.length()-1);
			}
			StringTokenizer commaTokenizer = new StringTokenizer(synstring,",");
			SystemUtils.trace("hays_search", "Inside SimpleHaysQueryBuilder");
			String simpleJobTitle = null;
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
		        SystemUtils.trace("hays_search", "SimpleHaysQuery TOken ='" + token + "'");
		        simpleJobTitle = token.replaceAll("[ ]+", " ");	
    	SystemUtils.trace("hays_search","simpleJobTitle Value"+simpleJobTitle);
    	queryPart.append(" (").append(simpleJobTitle).append(") WITHIN xKeywords ACCUM ");
    	//jobTitlePart.append(" (").append(jobTitle).append(") WITHIN dDocTitle ");
    	queryPart.append(" ($(SYN(").append(simpleJobTitle).append(", ").append(thes_name).append(") WITHIN dDocTitle ))*4 ACCUM (SYN (").append(simpleJobTitle).append(", ").append(thes_name).append(") WITHIN dDocTitle )");
    	// added for stemming
    	if(synstringqoutes.charAt(0) =='\"' && synstringqoutes.charAt(synstringqoutes.length()-1) =='\"' )
    	{
    		queryPart.append(" ACCUM (($").append(simpleJobTitle).append(") WITHIN dDocTitle )*2 ");	
    	}
    	else{
    	 simpleJobTitle = simpleJobTitle.replaceAll(" ", " and \\$ ");
    	queryPart.append(" ACCUM (($").append(simpleJobTitle).append(") WITHIN dDocTitle )*2 ");
    	SystemUtils.trace("hays_search","queryPart inside while is :"+queryPart);
    	   }
    	count=count+1;
    			}
			queryPart.append("))");//Extra closing bracket to avoid the deletion of bracket
			SystemUtils.trace("hays_search", "QueryPart Value is :"+queryPart);
        }
	}
}