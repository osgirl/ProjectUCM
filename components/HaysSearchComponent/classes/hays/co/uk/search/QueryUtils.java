package hays.co.uk.search;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryUtils {
	
	
	
	public static String formatStringForReservedKeyWords(String stringToFormat){
		try{
			
			StringTokenizer st = new StringTokenizer(stringToFormat, ", ",true);
			StringBuffer formattedString = new StringBuffer();
			while(st.hasMoreTokens()) {
				String token = st.nextToken();
				if(!token.equals(",") && IHaysSearchConstants.reservedKeyWordsForOracleTextSearch.indexOf(token.toUpperCase()) > 0){
					token="{"+token+"}";
				}
				formattedString.append(token);
			}
			return formattedString.toString();
		}
		catch(Exception e){
			
			return stringToFormat;
		}
	}
	
	public static String encodeForHaysSpecialKeywords(String stringToEncode){
        Pattern regex = null;        
        Matcher myMatcher = null;
        
        for( int i = 0; i < IHaysSearchConstants.specialKeyWordsForHaysSearchArr.length; i++) {    
            regex = Pattern.compile(IHaysSearchConstants.specialKeyWordsForHaysSearchArr[i], Pattern.CASE_INSENSITIVE);
            myMatcher = regex.matcher(stringToEncode);
            stringToEncode = myMatcher.replaceAll( IHaysSearchConstants.specialKeyWordsForHaysSearchArr[i+1]);
            i=i+1;
        }
 	
    	return stringToEncode;
	}
	
	public static String decodeHaysSpecialKeywords(String stringToDecode){
		
        for( int i = 0; i < IHaysSearchConstants.specialKeyWordsForHaysSearchArr.length; i++) {    
        	stringToDecode = stringToDecode.replaceAll(IHaysSearchConstants.specialKeyWordsForHaysSearchArr[i+1], IHaysSearchConstants.specialKeyWordsForHaysSearchArr[i]);
            i=i+1;
        }
        return stringToDecode;
	}
}
