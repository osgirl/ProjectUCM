
import intradoc.common.*;

import java.lang.*;
import java.net.*;

import intradoc.data.*;
import intradoc.shared.*;
import intradoc.server.*;
import intradoc.server.script.*;

import java.util.*;



/**
 * This class demonstrates how to create custom IdocScript functions. These
 * include variable names that should be evaluated, variables that are either 
 * true or false, as well as new kinds of functions.
 */
public class MyIdocExtensions extends ScriptExtensionsAdaptor
{
	public MyIdocExtensions()
	{
		
		// this is a list of the functions that can be called with the custom code
		m_functionTable = new String[] {"GetDomainName","sanitizeHtml"};

		// Configuration data for functions.  This list must align with the "m_functionTable"
		// list.  In order the values are "id number", "Number of arguments", "First argument type",
		// "Second argument type", "Return Type".  Return type has the following possible
		// values: 0 generic object (such as strings) 1 boolean 2 integer 3 double.
		// The value "-1" means the value is unspecified.
		m_functionDefinitionTable = new int[][]
		{
			{0, 1, GrammarElement.STRING_VAL,-1, 0}, // GetDomainName
			{1, 1, GrammarElement.STRING_VAL,-1, 0}, // Sanitize the html
			
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
		
		case 0:		// GetDomainName

			// this will take the URL and returns the Domain name
			URL url = null;
			
				
			try {
				url = new URL(sArg1);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				SystemUtils.trace("hays_idoc", "Caught Malformed exception");
				oResult = null;
				break;
				
			}
			
			
			
			 oResult = url.getHost();
			
	
			break;
			
		case 1:		// Sanitize the html

			String finalString = null;	
				SystemUtils.trace("hays_idoc", "Sanitizing the html");
				StringBuilder out = new StringBuilder(Math.max(16, sArg1.length()));
			    for (int i = 0; i < sArg1.length(); i++) {
			        char c = sArg1.charAt(i);
			        SystemUtils.trace("hays_idoc", "Sanitizing the html : " + c);
			        if (c == '<' || c == '>' || c == '&' || c == '"' || c=='\'') {
			            out.append("&#");
			            out.append((int) c);
			            out.append(';');
			        } else {
			            out.append(c);
			        }
			    }
			    finalString = out.toString();
			    SystemUtils.trace("hays_idoc", "Sanitizing the html : " + finalString);
			 oResult = finalString;
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


	
	
	

	
	
}
