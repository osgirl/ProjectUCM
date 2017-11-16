package hays.co.uk.search;

import intradoc.common.SystemUtils;
import intradoc.shared.SharedObjects;

import java.util.StringTokenizer;
import java.util.logging.Level;

public class FuzzyHaysQueryBuilder extends AbstractHaysQueryBuilder{

	String lLevel = null;
	String lRadius = null;
	String lIsAdvanceSearch = null;
	boolean lRelaxFullTextCountryList = false;
	
	public FuzzyHaysQueryBuilder(boolean isFuzzy, boolean isOnlyJobTitle,String thes_name,String alertprofileid,
			String level,String pIsAdvanceSearch,boolean pRelaxFullTextCountry,String pRadius) {
		super(isFuzzy, isOnlyJobTitle,thes_name,alertprofileid); 
		this.lLevel = level;
		this.lIsAdvanceSearch = pIsAdvanceSearch;
		this.lRelaxFullTextCountryList = pRelaxFullTextCountry;
		this.lRadius = pRadius;
	}


	public void buildQuery() {
		// TODO Auto-generated method stub
    	if(( this.getInputString().length() > 0 )) {
    		String inputFuzzyJobTitle = this.getInputString();
    		String inputFuzzyJobTitleQuotes=null;
    		int inputFuzzyJobTitleLenght = inputFuzzyJobTitle.length();
    		if(!isOnlyJobTitle()){
    			
						String keyBufferString = this.getInputString();
						keyBufferString = keyBufferString.replaceAll(IHaysSearchConstants.fuzzySpecialCharacters, " ");
						keyBufferString = keyBufferString.replaceAll("(?i)[&;!>]| AND ", " ");
						SystemUtils.trace("hays_search", "KeyBuffer String Value"+keyBufferString);
						keyBufferString = keyBufferString.replaceAll("(?i)[|]| OR ", ",");
						keyBufferString = keyBufferString.replaceAll("-", "\\\\-");
						keyBufferString = keyBufferString.replaceAll("_", "\\\\_");
						keyBufferString = keyBufferString.replaceAll("\"", "");  // Escaping double quotes
						int keybufflen = keyBufferString.trim().length();
												
						if(keybufflen>0){       //Handling extra comma in the Alert search criteria 
							if(keyBufferString.charAt(0)==',')
								keyBufferString = keyBufferString.substring(1, keybufflen-1);
							if( keyBufferString.charAt(keyBufferString.length()-1)==',' )
							    keyBufferString = keyBufferString.substring(0, keyBufferString.length()-1);
						}
						SystemUtils.trace("hays_search", "KeyBuffer String Value1"+keyBufferString);
																		
			    		/*Sprint 18*/
			    		StringTokenizer commaTokenizer = new StringTokenizer(keyBufferString,",");
			    			
			    			while(commaTokenizer.hasMoreTokens()){
			    				String token = commaTokenizer.nextToken().trim();
		    			        SystemUtils.trace("hays_search", "processSearchParameters: fuzzyJobTitle Inside Token   ='" + token + "'");
		    			        queryPart
		        				.append("fuzzy(")
		        				.append(token.replaceAll("[ ][ ]*","+"))
		        				.append(",,6,N) ACCUM ");
		    			       
		    			    	
			    			}
			    	    /* Sprint 18*/
			    		queryPart.append("(").append(keyBufferString).append(") WITHIN xKeywords ACCUM ");
			    		    		}
    		queryPart.append(" (").append(prepareJobTitleFuzzyQuery(inputFuzzyJobTitle,alertprofileid)).append(")");
    		SystemUtils.trace("hays_search", "Query Part till now111"+queryPart);
        }
	}
	
	private String prepareJobTitleFuzzyQuery(String inputJobTitle,String alertprofileid){
		StringBuffer fuzzyJobTitleQueryBuffer = new StringBuffer();
		String fuzzyJobTitleQuery = null;
		String inputJobTitleQuotes =inputJobTitle;
		inputJobTitle = inputJobTitle.replaceAll(IHaysSearchConstants.fuzzySpecialCharacters, " ");
        SystemUtils.trace("hays_search", "processSearchParameters: fuzzyJobTitle 2 ='" + inputJobTitle + "'");
        inputJobTitle = inputJobTitle.trim();
        /*updated for MR 209*/
       if(inputJobTitle.charAt(0) =='\"' && inputJobTitle.charAt(inputJobTitle.length()-1) =='\"' )
       {
    	   inputJobTitle = inputJobTitle.substring(1, inputJobTitle.length()-1);
		}
       inputJobTitle = inputJobTitle.replaceAll("(?i)[&;!>]| AND ", " ");
       SystemUtils.trace("hays_search", "processSearchParameters: inputJobTitle  ='" + inputJobTitle + "'");
       inputJobTitle = inputJobTitle.replaceAll("(?i) OR ", ",");
       StringTokenizer commaTokenizer = new StringTokenizer(inputJobTitle,",");
              	int count=0;
				while(commaTokenizer.hasMoreTokens()){
						String token = commaTokenizer.nextToken().trim();
						 if(count>0)
							{
							    SystemUtils.trace("hays_search", "Count is greater than 0");
								fuzzyJobTitleQueryBuffer.append("ACCUM");
							  	count=count-1;
							}
						
				        SystemUtils.trace("hays_search", "processSearchParameters: fuzzyJobTitle Inside Token   ='" + token + "'");
				        token = token.replaceAll("\"", "");
				        token = token.replaceAll("_", "\\\\_");
				        token = token.replaceAll("_", "\\\\_");
				        fuzzyJobTitleQueryBuffer
						.append("(fuzzy(")
						.append(token.replaceAll("[ ][ ]*","+"))
						.append(",,6,N) WITHIN dDocTitle) ACCUM ");
				        SystemUtils.trace("hays_search", "token is ="+token);
				        fuzzyJobTitleQueryBuffer.append("($(SYN(").append(token).append(", ").append(thes_name).append(") WITHIN dDocTitle ))*4 ACCUM (SYN (").append(token).append(", ").append(thes_name).append(") WITHIN dDocTitle )");
				        fuzzyJobTitleQuery = token.replaceAll("[ ]+", " or \\$");	//LOC 87
						SystemUtils.trace("hays_search", "fuzzyJobTitleQuery: '" + fuzzyJobTitleQuery );
				   	/*If the keyword is inside quotes we will append the token as it is to the ddoctitle term and add a content search term if isOnlyJob title is not true in the code below */
						if(inputJobTitleQuotes.charAt(0) =='\"' && inputJobTitleQuotes.charAt(inputJobTitleQuotes.length()-1) =='\"' ){
							
							fuzzyJobTitleQueryBuffer.append("ACCUM (($(").append(token).append(")) WITHIN dDocTitle)*2 ");
							if(!(isOnlyJobTitle()))//If false we add the content search term
							fuzzyJobTitleQueryBuffer.append("ACCUM (").append(token).append(")");
						}
				        else{
						if(!(isOnlyJobTitle()))
						{	
							SystemUtils.trace("hays_search", "IIISSOnlyJObTitle"+isOnlyJobTitle());
						fuzzyJobTitleQueryBuffer.append(" ACCUM(((($").append(fuzzyJobTitleQuery).append(") WITHIN dDocTitle)"); //added for Stemming
						fuzzyJobTitleQuery = fuzzyJobTitleQuery.replaceAll(" or \\$", " or ");	
						SystemUtils.trace("hays_search", "fuzzyJobTitleQuery:: '" + fuzzyJobTitleQuery );
						fuzzyJobTitleQueryBuffer.append(" ACCUM (").append(fuzzyJobTitleQuery).append(") WITHIN dDocTitle)");
						
						
						
						String tempfuzzyJobTitleQuery = null;
						
						String DefaultRadius = SharedObjects.getEnvironmentValue("DefaultRadius");
						SystemUtils.trace("hays_search", "relaxFullTextCountry. " +lRelaxFullTextCountryList);
						SystemUtils.trace("hays_search", "level. " +lLevel);
						SystemUtils.trace("hays_search", "alertprofileid. " +alertprofileid);
						SystemUtils.trace("hays_search", "isAdvanceSearch. " +lIsAdvanceSearch);
						SystemUtils.trace("hays_search", "radius. " +lRadius);
						
						if((lRelaxFullTextCountryList && lLevel != null && "6".equalsIgnoreCase(lLevel.trim())) && 
								((alertprofileid!=null && (lRadius.equals("0") || lRadius.equals(DefaultRadius))) ||
										(lIsAdvanceSearch!=null && lRadius.equals("0")) ||
										(lIsAdvanceSearch==null))
								)
						{
							tempfuzzyJobTitleQuery = fuzzyJobTitleQuery.replaceAll(" or ", " or \\$");	
							fuzzyJobTitleQueryBuffer.append("AND ($").append(tempfuzzyJobTitleQuery).append("))");
							SystemUtils.trace("hays_search", "NOT REPLACING OR FOR LEVEL 6 LOCATIONS");
						}
						else
						{
							fuzzyJobTitleQuery = fuzzyJobTitleQuery.replaceAll(" or ", " and \\$");
							fuzzyJobTitleQueryBuffer.append("AND ($").append(fuzzyJobTitleQuery).append("))");
							SystemUtils.trace("hays_search", "REPLACING OR FOR LEVEL 6 LOCATIONS");
						}
						
						}
						else
						{
							/*Else do not add anything but replace the or \\$ added in LOC 87 so that the LOC 112 works fine */
							fuzzyJobTitleQuery = fuzzyJobTitleQuery.replaceAll(" or \\$", " or ");
						}
						//String level = super.m_binder.getLocal(IHaysSearchConstants.LEVEL);
					    fuzzyJobTitleQuery = fuzzyJobTitleQuery.replaceAll(" or ", " and \\$");//LOC 112
						
						SystemUtils.trace("hays_search", "fuzzyJobTitleQuery::: '" + fuzzyJobTitleQuery );
										
						fuzzyJobTitleQueryBuffer.append("ACCUM (($").append(fuzzyJobTitleQuery).append(") WITHIN dDocTitle)*2 ");
						
						SystemUtils.trace("hays_search", "Inside While");
						}
				        count=count+1;
					}
	             
	            
				  
				 				  
				  
				 SystemUtils.trace("hays_search", "For fuzzyJobTitleQueryBuffer"+fuzzyJobTitleQueryBuffer);
				return fuzzyJobTitleQueryBuffer.toString();
			
                
          		
	}
}