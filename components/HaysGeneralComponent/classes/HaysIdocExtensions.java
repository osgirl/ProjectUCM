
import intradoc.common.*;
import intradoc.conversion.*;
import intradoc.data.*;
import intradoc.shared.*;
import intradoc.server.*;
import intradoc.server.script.*;

import java.util.*;
import java.text.SimpleDateFormat;

import org.owasp.esapi.ESAPI;


/**
 * This class demonstrates how to create custom IdocScript functions. These
 * include variable names that should be evaluated, variables that are either 
 * true or false, as well as new kinds of functions. 
 */
public class HaysIdocExtensions extends ScriptExtensionsAdaptor
{
	public HaysIdocExtensions()
	{
		
		// this is a list of the functions that can be called with the custom code
		m_functionTable = new String[] {"strConcat","strReverse","strSubStringHtmlTags", "filterHtmlTags","encodeForHTML","encodeForHTMLAttribute","encodeForJavaScript"};

		// Configuration data for functions.  This list must align with the "m_functionTable"
		// list.  In order the values are "id number", "Number of arguments", "First argument type",
		// "Second argument type", "Return Type".  Return type has the following possible
		// values: 0 generic object (such as strings) 1 boolean 2 integer 3 double.
		// The value "-1" means the value is unspecified.
		m_functionDefinitionTable = new int[][]
		{
			{0, 2, GrammarElement.STRING_VAL, GrammarElement.STRING_VAL, 0}, // strConcat
			{1,1, GrammarElement.STRING_VAL, -1, 0}, // strReverse
			{2,2, GrammarElement.STRING_VAL, GrammarElement.INTEGER_VAL, 0},// strSubStringHtmlTags
			{3,1, GrammarElement.STRING_VAL, -1, 0}, //filterHtmlTags
			{4,1, GrammarElement.STRING_VAL, -1, 0}, //encodeForHTML
			{5,1, GrammarElement.STRING_VAL, -1, 0}, //encodeForHTMLAttribute
			{6,1, GrammarElement.STRING_VAL, -1, 0}, //encodeForJavaScript
		};
	}


	/**
	 * This is where the custom IdocScript function is evaluated.
	 */
	public boolean evaluateFunction(ScriptInfo info, Object[] args, ExecutionContext context)
		throws ServiceException
	{
		/**
		 * This code below is optimized for speed, not clarity.  Do not modify
		 * the code below when making new IdocScript functions.  It is needed to
		 * prepare the necessary variables for the evaluation and return of the
		 * custom IdocScript functions.  Only customize the switch statement below.
		 */
		int config[] = (int[])info.m_entry;
		String function = info.m_key;
		
		int nargs = args.length - 1;
		int allowedParams = config[1];
		if (allowedParams >= 0 && allowedParams != nargs)
		{
			String msg = LocaleUtils.encodeMessage("csScriptEvalNotEnoughArgs", 
				null, function, ""+allowedParams);
			throw new IllegalArgumentException(msg);
		}
		
		String msg = LocaleUtils.encodeMessage("csScriptMustBeInService", 
			null, function, "Service");
		Service service = ScriptExtensionUtils.getService(context, msg);
		DataBinder binder = service.getBinder();
		
		UserData userData = (UserData)context.getCachedObject("UserData");
		if (userData == null)
		{
			msg = LocaleUtils.encodeMessage("csUserDataNotAvailable", null, function);
			throw new ServiceException(msg);
		}
		
		// Do some initial conversion of arguments.  Choices of what initial conversions to make
		// are based on frequency of usage.  If a function uses nontypical parameters it will
		// have to do its own conversion.
		String sArg1 = null;
		String sArg2 = null;
		long lArg1 = 0;
		long lArg2 = 0;
		if (nargs > 0)
		{
			if (config[2] == GrammarElement.STRING_VAL)
			{
				sArg1 = ScriptUtils.getDisplayString(args[0], context);
			}
			else if (config[2] == GrammarElement.INTEGER_VAL)
			{
				lArg1 = ScriptUtils.getLongVal(args[0], context);
								
			}
				
		}
		if (nargs > 1)
		{
			if (config[3] == GrammarElement.STRING_VAL)
			{
				sArg2 = ScriptUtils.getDisplayString(args[1], context);
			}
			else if (config[3] == GrammarElement.INTEGER_VAL)
			{
				lArg2 = ScriptUtils.getLongVal(args[1], context);
			}
		}



		/**
		 * Here is where the custom code should go. The case values coincide
		 * with the "id values" in m_functionDefinitionTable. Perform the
		 * calculations here, and place the result into ONE of the result
		 * variables declared below.  Use 'sArg1' and 'sArg2' for the first and
		 * second String arguments for the function (if they exist).  Likewise use
		 * 'lArg1' and 'lArg2' for the first and second long integer arguments.
		 */
		boolean bResult = false;  // Used for functions that return a boolean.
		int iResult = 0; // Used for functions that return an integer.
		double dResult = 0.0;  // Used for functions that return a double.
		Object oResult = null; // Used for functions that return an object (string).
		switch (config[0])
		{
		
		case 0:		// strConcat

			// this will look at the 2 strings passed, and return Concatenated string
			
			 oResult = sArg1+" "+sArg2;
	
			break;
			
		case 1:		// strReverse

			// this will reverse a string
			
			int i, len = sArg1.length();
		    StringBuffer reStr = new StringBuffer(len);

		    for (i = (len - 1); i >= 0; i--)
		    	reStr.append(sArg1.charAt(i));
		    oResult= reStr.toString();
		 
	
			break;
			
		case 2:		// strReverse
			// this will reverse a string
		    oResult= closeTags(sArg1,lArg2);
			break;

		case 3: //filterHtmlTags
			// this will remove html tags from the string
			 oResult =sArg1.replaceAll("<(.|\n)*?>", "");
			break;
		
		case 4: //encodeForHTML
			// this will remove html tags from the string
			 oResult = ESAPI.encoder().encodeForHTML(sArg1);
			break;
		
		case 5: //encodeForHTMLAttribute
			// this will remove html tags from the string
			 oResult = ESAPI.encoder().encodeForHTMLAttribute(sArg1); 
			break;
			
		case 6: //encodeForJavaScript
			// this will remove html tags from the string
			 oResult = ESAPI.encoder().encodeForJavaScript(sArg1);
			break;	
			
			

		default:
			return false;
		}


		/**
		 * Do not alter code below here
		 */
		args[nargs] = ScriptExtensionUtils.computeReturnObject(config[4],
			bResult, iResult, dResult, oResult);

		// Handled function.
		return true;
	}


	
	
	public final static String closeTags(String str,long count) {

		String[] SINGLE_TAGS = { "br", "hr" };
        Map openTags = new HashMap();
        LinkedList tagList = new LinkedList();
        
        boolean inTag = false;
        boolean inTagName = false;
        boolean inOpenTag = true;
        String tagName = "";
        List singleTags = Arrays.asList(SINGLE_TAGS);
        //str=str.substring(0, (int)count);
        //Added code for removing cutted word
        if(count < str.length()){
        	if(!(Character.isWhitespace(str.charAt(250-1))) && !(Character.isWhitespace(str.charAt(251-1))))
        	{
        		System.out.println("In 250 character IF condition");
        		str=str.substring(0, (int)count);		        	
	        	int lastWSpaceIndex=str.lastIndexOf(" ", str.length());
		        System.out.println("White space at  Index: "+lastWSpaceIndex);
		        str=str.substring(0, lastWSpaceIndex);        		
        	}
        	else
        	{
        		System.out.println("In 250 character ELSE condition");
        		str=str.substring(0, (int)count);
        	}
        }
//        if(count==250)
//        {	        	
//        	if(!(Character.isWhitespace(str.charAt(250-1))) && !(Character.isWhitespace(str.charAt(251-1))))
//        	{
//        		System.out.println("In 250 character IF condition");
//        		str=str.substring(0, (int)count);		        	
//	        	int lastWSpaceIndex=str.lastIndexOf(" ", str.length());
//		        System.out.println("White space at  Index: "+lastWSpaceIndex);
//		        str=str.substring(0, lastWSpaceIndex);        		
//        	}
//        	else
//        	{
//        		System.out.println("In 250 character ELSE condition");
//        		str=str.substring(0, (int)count);
//        	}
//        	
//        }
//        else
//        {	        	
//        	System.out.println("String less than 250 chars in ELSE condition");
//			str=str.substring(0, (int)count);
//        }
        //code ends 
        char[] strA = str.toCharArray();
        for (int i = 0; i < strA.length; i++) 
        {
            char c = strA[i];
            if (!inTag) 
            { // not in a tag  
                if (c == '<')  {// start of a tag
                                // reset all state variables at start of each new tag
                    inTag = true;
                    inTagName = true;
                    inOpenTag = true;
                    tagName = "";
                }
            } 
            else // in a tag
            {

                if ((tagName.length() == 0) &&
                    (c == '/'))
                { // start of a close tag
                    inOpenTag = false;
                }
                else if (inTagName && ((c == ' ') || (c == '>') || (c == '/')))
                { // end of the tagname or tag
                    
                    inTagName = false;
                    if (inOpenTag &&!singleTags.contains(tagName.toLowerCase())) 
                    {

                        // count this tag in the list of open tags

                        if (openTags.get(tagName) == null) 
                        {

                            openTags.put(tagName, new Integer(1));
                            tagList.add(tagName);

                        } 
                        else {

                            int tagCount =
                                ((Integer)openTags.get(tagName)).intValue();

                            openTags.put(tagName, new Integer(tagCount + 1));
                            tagList.add(tagName);
                        }
                    } 
                    else // in close tag

                    {

                        // remove it from closetags

                        if (openTags.get(tagName) != null) 
                        {

                            int tagCount =
                                ((Integer)openTags.get(tagName)).intValue();


                            if (tagCount > 1) 
                            {

                                openTags.put(tagName,new Integer(tagCount - 1));
                                tagList.add(tagName);

                            }
                            else
                            {

                                openTags.remove(tagName);
                                tagList.remove(tagName);

                            }

                        }

                    }


                    if (c == '>') // end of tag

                    {

                        inTag = false;

                    }

                } 
                else if (inTagName) // still in tag name

                {

                    tagName += c;
                    

                } 
                else if (c == '>') // end of tag and there were attributes

                {

                    inTag = false;

                }

            }

        }
		/*Added by Kanika*/
        if (inTag)
        {
        	int i, len = str.length();
        	 int incompleteTagIndex = 0;
        	
        	 for (i = (len - 1); i >= 0; i--)
        		 if (str.charAt(i)=='<')
        		 {
        			 incompleteTagIndex = i;
        			 break;
        		}
        	str=str.substring(0, incompleteTagIndex);
        		 
        }
        
      	/*End*/

        StringBuffer closedString = new StringBuffer(str);
        Iterator it = tagList.descendingIterator();
        while(it.hasNext()){
        closedString.append("</").append(it.next()).append('>');
         }
       

          
        return closedString.toString();


    }

	
	
}
