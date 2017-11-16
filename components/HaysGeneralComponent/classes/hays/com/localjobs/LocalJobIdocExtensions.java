package hays.com.localjobs;

import hays.custom.multilingual.HaysWebSite;
import infomentum.ontology.Converter;
import infomentum.ontology.loader.OntologyFacade;
import intradoc.common.*;
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
public class LocalJobIdocExtensions extends ScriptExtensionsAdaptor
{
	public final String[] SPECIALISM_PROPERTIES = {"specialismId", "sectionId", "url", "description" , "isSubSpecialism"};
	public final String[] LOCATION_PROPERTIES = {"locationId", "url", "description"};
	public final String[] OTHER_LOCATIONS_PROPERTIES = {"locationId", "url", "description", "parentLocationId","parentUrl","parentDescription"};
	
	public LocalJobIdocExtensions()
	{
		
		// this is a list of the functions that can be called with the custom code
		m_functionTable = new String[] {"parseLocalJobUrl","getOtherLocations","localJobsInYourArea", "localJobsInYourAreaSubSpec"};

		// Configuration data for functions.  This list must align with the "m_functionTable"
		// list.  In order the values are "id number", "Number of arguments", "First argument type",
		// "Second argument type", "Return Type".  Return type has the following possible
		// values: 0 generic object (such as strings) 1 boolean 2 integer 3 double.
		// The value "-1" means the value is unspecified.
		m_functionDefinitionTable = new int[][]
		{
			{0, 4, GrammarElement.STRING_VAL, GrammarElement.STRING_VAL,  GrammarElement.STRING_VAL, GrammarElement.STRING_VAL, 0}, // parseLocalJobUrl
			{1, 3, GrammarElement.STRING_VAL, GrammarElement.STRING_VAL, GrammarElement.STRING_VAL, 0}, // getOtherLocations
			{2, 3, GrammarElement.STRING_VAL, GrammarElement.STRING_VAL, GrammarElement.STRING_VAL, 0}, // localJobsInYourArea
			{3, 4, GrammarElement.STRING_VAL, GrammarElement.STRING_VAL,  GrammarElement.STRING_VAL, GrammarElement.STRING_VAL, 0} // localJobsInYourAreaSubSpec
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
		
		case 0:		// parseLocalJobUrl
			SystemUtils.trace("LocalData", " inside parseLocalJobUrl");
			String url = sArg1;
			String domainId = sArg2;
			String language = ScriptUtils.getDisplayString(args[2], context);
			String locationColumn = ScriptUtils.getDisplayString(args[3], context);
			String urlParts[] = url.split("/");
			String pageType = null, specialismId, locationId;
			Object specialismObj = SharedObjects.getObject("LocalData", "DomainSpecialismMap");
			Object locationObj = SharedObjects.getObject("LocalData", "DomainLocationsMap");
			HashMap<String, LocalDomainSpecialism> specialismMap = (HashMap<String, LocalDomainSpecialism>)specialismObj;
			HashMap<String, LocalDomainLocations> locationMap = (HashMap<String, LocalDomainLocations>)locationObj;
			
			LocalHaysSpecialism lhs = null;
			LocalHaysRegionLocation lhrs = null;
			LocalHaysSubSpecialism lhss = null;
			LocalHaysTownLocation lhtl = null;
			try{
				SystemUtils.trace("LocalData", " urlparts length "+urlParts.length);
			if(urlParts.length == 0 || urlParts.length == 1){
				//localjobs landing page
				pageType = "1";
				setSpecialismRS(specialismMap.get(domainId).getAllSpecialisms(), binder, language);
				setRegionLocationRS(locationMap.get(domainId).getAllLocations(), binder, locationColumn);
			}else if(urlParts.length == 2){
				//regional specialism landing page
				SystemUtils.trace("LocalData", " inside urlparts length "+urlParts.length+ " page type is 2");
				pageType = "2";
				lhs = getSpecialism(specialismMap, domainId, urlParts[0], language);
				lhrs = getRegionLocation(locationMap, domainId, urlParts[1], locationColumn);
				setSpecialismRS(new ArrayList<LocalHaysSpecialism> (Arrays.asList((new LocalHaysSpecialism[]{lhs}))), binder, language);
				setRegionLocationRS(new ArrayList<LocalHaysRegionLocation> (Arrays.asList((new LocalHaysRegionLocation[]{lhrs}))), binder, locationColumn);
				setTownLocationRS(lhrs.getAllTownLocations(), binder, locationColumn);
			}else if(urlParts.length == 3){
				//regional sub-specialism landing page or local specialism landing page
				SystemUtils.trace("LocalData", " inside urlparts length "+urlParts.length+ " page type is 3");
				pageType = "3";
				lhs = getSpecialism(specialismMap, domainId, urlParts[0], language);
				lhss = getSubSpecialism(lhs, urlParts[1], language);
				if(lhss == null){
					SystemUtils.trace("LocalData", urlParts[1]+" is not sub-specialism, looking up location now");
					pageType = "4";
					SystemUtils.trace("LocalData", " inside urlparts length "+urlParts.length+ " page type is 4");
					lhrs = getRegionLocation(locationMap, domainId, urlParts[1], locationColumn);
					lhtl = getTownLocation(lhrs, urlParts[2], locationColumn);
					setSpecialismRS(new ArrayList<LocalHaysSpecialism> (Arrays.asList((new LocalHaysSpecialism[]{lhs}))), binder, language);
					setSubSpecialismRS(lhs.getAllSubSpecialisms(), binder, language);
					setRegionLocationRS(new ArrayList<LocalHaysRegionLocation> (Arrays.asList((new LocalHaysRegionLocation[]{lhrs}))), binder, locationColumn);
					setTownLocationRS(new ArrayList<LocalHaysTownLocation> (Arrays.asList((new LocalHaysTownLocation[]{lhtl}))), binder, locationColumn);
				}else{
					SystemUtils.trace("LocalData", urlParts[1]+" is sub-specialism, looking up location now for "+urlParts[2]);
					lhrs = getRegionLocation(locationMap, domainId, urlParts[2], locationColumn);
					setSpecialismRS(new ArrayList<LocalHaysSpecialism> (Arrays.asList((new LocalHaysSpecialism[]{lhs}))), binder, language);
					setSubSpecialismRS(new ArrayList<LocalHaysSubSpecialism> (Arrays.asList((new LocalHaysSubSpecialism[]{lhss}))), binder, language);
					setRegionLocationRS(new ArrayList<LocalHaysRegionLocation> (Arrays.asList((new LocalHaysRegionLocation[]{lhrs}))), binder, locationColumn);
					setTownLocationRS(lhrs.getAllTownLocations(), binder, locationColumn);
				}
				
			}else if(urlParts.length == 4){
				//local sub-specialism landing page
				SystemUtils.trace("LocalData", " inside urlparts length "+urlParts.length+ " page type is 5");
				pageType = "5";
				lhs = getSpecialism(specialismMap, domainId, urlParts[0], language);
				lhss = getSubSpecialism(lhs, urlParts[1], language);
				lhrs = getRegionLocation(locationMap, domainId, urlParts[2], locationColumn);
				lhtl = getTownLocation(lhrs, urlParts[3], locationColumn);
				setSpecialismRS(new ArrayList<LocalHaysSpecialism> (Arrays.asList((new LocalHaysSpecialism[]{lhs}))), binder, language);
				setSubSpecialismRS(new ArrayList<LocalHaysSubSpecialism> (Arrays.asList((new LocalHaysSubSpecialism[]{lhss}))), binder, language);
				setRegionLocationRS(new ArrayList<LocalHaysRegionLocation> (Arrays.asList((new LocalHaysRegionLocation[]{lhrs}))), binder, locationColumn);
				setTownLocationRS(new ArrayList<LocalHaysTownLocation> (Arrays.asList((new LocalHaysTownLocation[]{lhtl}))), binder, locationColumn);
			}
			binder.putLocal("localePageType", pageType);
			if(lhs !=null ){
				binder.putLocal("localSpecialismId", lhs.getSpecialismId());
			}
			if(lhss !=null ){
				binder.putLocal("localSubSpecialismId", lhss.getSubspecialismId());
			}
			if(lhrs !=null ){
				binder.putLocal("localRegionLocationId", lhrs.getLocationId());
			}
			if(lhtl !=null ){
				binder.putLocal("localTownLocationId", lhtl.getLocationId());
			}
			}catch(Exception e){
				e.printStackTrace();
			}
			oResult = sArg1+" "+sArg2;
			break;
		case 1:		//getOtherLocations
			String locationID = sArg1;
			String locationCol = sArg2;
			String domainID = ScriptUtils.getDisplayString(args[2], context);
			Object locationsOtherObj = SharedObjects.getObject("LocalData", "DomainOtherLocationsMap");
			HashMap<String, List<String>> otherLocations = (HashMap<String, List<String>>)locationsOtherObj;
			List<String> otherLocationsList = otherLocations.get(locationID);
			DataResultSet drs = new DataResultSet(OTHER_LOCATIONS_PROPERTIES);
			if(otherLocationsList != null){
				String otherLocationId = null;
				Vector <String> row = new Vector<String>();
				for (Iterator<String> iterator = otherLocationsList.iterator(); iterator.hasNext();) 
				{
					otherLocationId = iterator.next();
					row = getOtherLocationDetails(otherLocationId, domainID, locationCol);
					drs.addRow(row);
				}
			}
			binder.addResultSet("rsLocalOtherLocations", drs);
			break;
		case 2:		//localJobsInYourArea
			SystemUtils.trace("LocalData", " inside localJobsInYourArea");
		
			String domainid = sArg1;
			String languagecode = sArg2;
			String locationColumnname = ScriptUtils.getDisplayString(args[2], context);
			
			Object specialismObject = SharedObjects.getObject("LocalData", "DomainSpecialismMap");
			Object locationObject = SharedObjects.getObject("LocalData", "DomainLocationsMap");
			HashMap<String, LocalDomainSpecialism> specialismMap1 = (HashMap<String, LocalDomainSpecialism>)specialismObject;
			HashMap<String, LocalDomainLocations> locationMap1 = (HashMap<String, LocalDomainLocations>)locationObject;
			
			LocalHaysSpecialism lhspec = null;
			LocalHaysRegionLocation lhregion = null;
			LocalHaysSubSpecialism lhsubspec = null;
			
			
			try{
			setSpecialismRS(specialismMap1.get(domainid).getAllSpecialisms(), binder, languagecode);
			setRegionLocationRS(locationMap1.get(domainid).getAllLocations(), binder, locationColumnname);
			
			//lhspec = getSpecialism(specialismMap1, domainid, abc, languagecode);
			//lhsubspec = getSubSpecialism(lhspec, abc, languagecode);
			//setSubSpecialismRS(lhspec.getAllSubSpecialisms(), binder, languagecode);
			
			if(lhspec !=null ){
				binder.putLocal("localSpecialismId", lhspec.getSpecialismId());
			}
			if(lhsubspec !=null ){
				binder.putLocal("localSubSpecialismId", lhsubspec.getSubspecialismId());
			}
			if(lhregion !=null ){
				binder.putLocal("localRegionLocationId", lhregion.getLocationId());
			}
			
			}
			catch(Exception e){
				e.printStackTrace();
			}
			oResult = sArg1+" "+sArg2; 
			break;
			
		case 3:		//localJobsInYourAreaSubSpec
			SystemUtils.trace("LocalData", " inside localJobsInYourAreaSubspec");
		
			String domain_id = sArg1;
			String language_code = sArg2;
			String locationColumn_name = ScriptUtils.getDisplayString(args[2], context);
			String specialism = ScriptUtils.getDisplayString(args[3], context);
			
			Object specialismObject1 = SharedObjects.getObject("LocalData", "DomainSpecialismMap");
			
			HashMap<String, LocalDomainSpecialism> specialismMap2 = (HashMap<String, LocalDomainSpecialism>)specialismObject1;
			
			
			LocalHaysSpecialism lhSpec = null;
			LocalHaysSubSpecialism lhSubspec = null;
			
			
			try{
			
			lhSpec = getSpecialism(specialismMap2, domain_id, specialism, language_code);
			lhSubspec = getSubSpecialism(lhSpec, specialism, language_code);
			setSubSpecialismRS(lhSpec.getAllSubSpecialisms(), binder, language_code);
			
			
			if(lhSubspec !=null ){
				binder.putLocal("localSubSpecialismId", lhSubspec.getSubspecialismId());
			}
			
			
			}
			catch(Exception e){
				e.printStackTrace();
			}
			oResult = sArg1+" "+sArg2; 
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
	
	private Vector<String> getOtherLocationDetails(String locationId, String domainId, String locationColumn){
		Vector <String> row = null;
		Object locationObj = SharedObjects.getObject("LocalData", "DomainLocationsMap");
		HashMap<String, LocalDomainLocations> locationMap = (HashMap<String, LocalDomainLocations>)locationObj;
		List<LocalHaysRegionLocation> specialismList = locationMap.get(domainId).getAllLocations();
		LocalHaysRegionLocation lhs = null;
		
		for (Iterator<LocalHaysRegionLocation> iterator = specialismList.iterator(); iterator.hasNext();) 
		{
			lhs = iterator.next();
			List<LocalHaysTownLocation> townList = lhs.getAllTownLocations();
			LocalHaysTownLocation lhtl = null;
			for (Iterator<LocalHaysTownLocation> iteratorTown = townList.iterator(); iteratorTown.hasNext();) 
			{
				lhtl = iteratorTown.next();
				if(locationId.equals(lhtl.getLocationId())){
					row = new Vector<String>();
					row.add(lhtl.getLocationId());
					String specialismText = getTownLocationText(lhtl, locationColumn);
					SystemUtils.trace("LocalData", "town description: " + specialismText);
					if(lhtl.getUrl() == null || "".equals(lhtl.getUrl())){
						row.add(specialismText.toLowerCase().replace(" ", "-"));
					}else{
						row.add(lhtl.getUrl());
					}
					row.add(specialismText);
					
					row.add(lhs.getLocationId());
					String TownText = getLocationText(lhs, locationColumn);
					SystemUtils.trace("LocalData", "town description: " + TownText);
					if(lhs.getUrl() == null || "".equals(lhs.getUrl())){
						row.add(TownText.toLowerCase().replace(" ", "-"));
					}else{
						row.add(lhs.getUrl());
					}
					row.add(TownText);
					break;
				}
			}
			if(row !=null){
				break;
			}
			
		}
		return row;
	}
	
	private void setSpecialismRS(List<LocalHaysSpecialism> specialismList, DataBinder m_binder, String language) 
																				throws ServiceException, DataException{
		DataResultSet drs = new DataResultSet(SPECIALISM_PROPERTIES);
		
		Vector <String> row = null;
		LocalHaysSpecialism lhs = null;
		for (Iterator<LocalHaysSpecialism> iterator = specialismList.iterator(); iterator.hasNext();) 
		{
			lhs = iterator.next();
			row = new Vector<String>();
			
			row.add(lhs.getSpecialismId());
			row.add(lhs.getSectionId());
			
			String specialismText = Converter.getLabel(lhs.getSpecialismId(), OntologyFacade.getOntology("xCategory"), language);
			if(lhs.getUrl() == null || "".equals(lhs.getUrl())){
				row.add(specialismText.toLowerCase().replace(" ", "-"));
			}else{
				row.add(lhs.getUrl());
			}
			row.add(specialismText);
			row.add("");
			drs.addRow(row);
		}
 
		m_binder.addResultSet("rsLocalSpecialismProperties", drs);
	}
	
	private void setSubSpecialismRS(List<LocalHaysSubSpecialism> specialismList, DataBinder m_binder, String language) 
									throws ServiceException, DataException{
		DataResultSet drs = new DataResultSet(SPECIALISM_PROPERTIES);
		
		Vector <String> row = null;
		LocalHaysSubSpecialism lhs = null;
		String specialismText=null;
		for (Iterator<LocalHaysSubSpecialism> iterator = specialismList.iterator(); iterator.hasNext();) 
		{
			lhs = iterator.next();
			row = new Vector<String>();
			
			row.add(lhs.getSubspecialismId());
			row.add("");
			if("N".equalsIgnoreCase(lhs.getIsSubSpecialism()))
			{
				specialismText = lhs.getSubspecialismId();
			}else{
			specialismText = Converter.getLabel(lhs.getSubspecialismId(), OntologyFacade.getOntology("xCategory"), language);
			}
			if(lhs.getUrl() == null || "".equals(lhs.getUrl())){
				
				row.add(specialismText.toLowerCase().replace(" ", "-"));
			}else{
				row.add(lhs.getUrl());
			}
			row.add(specialismText);
			row.add(lhs.getIsSubSpecialism());
			drs.addRow(row);
		}
		
		m_binder.addResultSet("rsLocalSubSpecialismProperties", drs);
		}
	
	private void setRegionLocationRS(List<LocalHaysRegionLocation> specialismList, DataBinder m_binder, String locationColumn)
											throws ServiceException, DataException{
		DataResultSet drs = new DataResultSet(LOCATION_PROPERTIES);
		
		Vector <String> row = new Vector<String>();
		LocalHaysRegionLocation lhs = null;
		for (Iterator<LocalHaysRegionLocation> iterator = specialismList.iterator(); iterator.hasNext();) 
		{
			lhs = iterator.next();
			row = new Vector<String>();
			
			row.add(lhs.getLocationId());
			String specialismText = getLocationText(lhs, locationColumn);
			if(lhs.getUrl() == null || "".equals(lhs.getUrl())){
				row.add(specialismText.toLowerCase().replace(" ", "-"));
			}else{
				row.add(lhs.getUrl());
			}
			row.add(specialismText);
			drs.addRow(row);
		}
		m_binder.addResultSet("rsLocalRegionLocationProperties", drs);
	}
	
	private void setTownLocationRS(List<LocalHaysTownLocation> specialismList, DataBinder m_binder, String locationColumn)
			throws ServiceException, DataException{
		DataResultSet drs = new DataResultSet(LOCATION_PROPERTIES);
		
		Vector <String> row = null;
		LocalHaysTownLocation lhs = null;
		for (Iterator<LocalHaysTownLocation> iterator = specialismList.iterator(); iterator.hasNext();) 
		{
			lhs = iterator.next();
			row = new Vector<String>();
			
			row.add(lhs.getLocationId());
			String specialismText = getTownLocationText(lhs, locationColumn);
			SystemUtils.trace("LocalData", "town description: " + specialismText);
			if(lhs.getUrl() == null || "".equals(lhs.getUrl())){
				row.add(specialismText.toLowerCase().replace(" ", "-"));
			}else{
				row.add(lhs.getUrl());
			}
			row.add(specialismText);
			drs.addRow(row);
		}
		m_binder.addResultSet("rsLocalTownLocationProperties", drs);
	}
	
	private LocalHaysSpecialism getSpecialism(HashMap<String, LocalDomainSpecialism> specialismMap, String domainId, 
			String urlPart, String language) throws ServiceException, DataException {
		LocalHaysSpecialism lhs = null;
		if(specialismMap != null){
			LocalDomainSpecialism lds  = specialismMap.get(domainId);
			lhs = lds.getSpecialismForUrl(urlPart);
			if(lhs == null){
				//value may not be in urlMap yet, so iterating to find out
				List<LocalHaysSpecialism> list = lds.getAllSpecialisms();
				for (Iterator iterator = list.iterator(); iterator.hasNext();) {
					LocalHaysSpecialism localHaysSpecialism = (LocalHaysSpecialism) iterator.next();
					String id = localHaysSpecialism.getSpecialismId();
					String specialismText = Converter.getLabel(id, OntologyFacade.getOntology("xCategory"), language);
					String urlMatch = specialismText.toLowerCase().replace(" ", "-");
					SystemUtils.trace("LocalData", "specialism urlMatch: " + urlMatch);
					if(urlPart.equals(urlMatch)){
						lhs = localHaysSpecialism;
						SystemUtils.trace("LocalData", "url pattern found, specialism Id: " + localHaysSpecialism.getSpecialismId());
						//cache the URL for future use
						lds.getUrlMap().put(urlMatch, lhs);
						SystemUtils.trace("LocalData", "put specialism url in cache: " + lds.getUrlMap());
						break;
					}
				}
			}
			if(lhs == null){
				//not found
				SystemUtils.trace("LocalData", "specialism url pattern not found in set up: " + urlPart);
				return null;
			}
			
		}
		return lhs;
	}
	
	private LocalHaysSubSpecialism getSubSpecialism(LocalHaysSpecialism localHaysSpecialism, String urlPart, String language) throws ServiceException, DataException{
		LocalHaysSubSpecialism lhs = null;
		String subSpecialismText , urlMatch;
		if(localHaysSpecialism != null){
			lhs  = localHaysSpecialism.getSubSpecialismForUrl(urlPart);
			if(lhs == null){
				//value may not be in urlMap yet, so iterating to find out
				List<LocalHaysSubSpecialism> list = localHaysSpecialism.getAllSubSpecialisms();
				for (Iterator iterator = list.iterator(); iterator.hasNext();) {
					LocalHaysSubSpecialism localHaysSubSpecialism = (LocalHaysSubSpecialism) iterator.next();
					String id = localHaysSubSpecialism.getSubspecialismId();
					SystemUtils.trace("LocalData", "specialism id title: " + id);
					String isSubSpecialism = localHaysSubSpecialism.getIsSubSpecialism();
					if("N".equalsIgnoreCase(isSubSpecialism))
					{
						urlMatch = id.toLowerCase().replace(" ", "-");
					}
					else{
						 subSpecialismText = Converter.getLabel(id, OntologyFacade.getOntology("xCategory"), language);
						 urlMatch = subSpecialismText.toLowerCase().replace(" ", "-");
					}
					
					SystemUtils.trace("LocalData", "specialism title urlMatch: " + urlMatch);
					if(urlPart.equals(urlMatch)){
						lhs = localHaysSubSpecialism;
						SystemUtils.trace("LocalData", "url pattern found, sub-specialism Id: " + localHaysSubSpecialism.getSubspecialismId());
						//cache the URL for future use
						localHaysSpecialism.getUrlMap().put(urlMatch, lhs);
						SystemUtils.trace("LocalData", "put specialism url in cache: " + localHaysSpecialism.getUrlMap());
						break;
					}
				}
			}
			if(lhs == null){
				//not found
				SystemUtils.trace("LocalData", "sub-specialism url pattern not found in set up: " + urlPart);
				return null;
			}
			
		}
		return lhs;
	}
	
	private LocalHaysRegionLocation getRegionLocation(HashMap<String, LocalDomainLocations> locationMap, String domainId, String urlPart, String locationColumn){
		LocalHaysRegionLocation lhs = null;
		if(locationMap != null){
			LocalDomainLocations lds  = locationMap.get(domainId);
			lhs = lds.getRegionLocationForUrl(urlPart);
			if(lhs == null){
				//value may not be in urlMap yet, so iterating to find out
				List<LocalHaysRegionLocation> list = lds.getAllLocations();
				for (Iterator iterator = list.iterator(); iterator.hasNext();) {
					LocalHaysRegionLocation localHaysLocation = (LocalHaysRegionLocation) iterator.next();
					String id = localHaysLocation.getLocationId();
					String locationText = getLocationText(localHaysLocation, locationColumn);
					String urlMatch = locationText.toLowerCase().replace(" ", "-");
					SystemUtils.trace("LocalData", "Region Location urlMatch: " + urlMatch);
					if(urlPart.equals(urlMatch)){
						lhs = localHaysLocation;
						SystemUtils.trace("LocalData", "url pattern found, location Id: " + localHaysLocation.getLocationId());
						//cache the URL for future use
						lds.getUrlMap().put(urlMatch, lhs);
						SystemUtils.trace("LocalData", "put location url in cache: " + lds.getUrlMap());
						break;
					}
				}
			}
			if(lhs == null){
				//not found
				SystemUtils.trace("LocalData", "location url pattern not found in set up: " + urlPart);
				return null;
			}
			
		}
		return lhs;
	}
	
	private LocalHaysTownLocation getTownLocation(LocalHaysRegionLocation lhrl, String urlPart, String locationColumn){
		LocalHaysTownLocation lhtl = null;
		if(lhrl != null){
			lhtl = lhrl.getTownLocationForUrl(urlPart);
			if(lhtl == null){
				//value may not be in urlMap yet, so iterating to find out
				List<LocalHaysTownLocation> list = lhrl.getAllTownLocations();
				for (Iterator iterator = list.iterator(); iterator.hasNext();) {
					LocalHaysTownLocation localHaysLocation = (LocalHaysTownLocation) iterator.next();
					String id = localHaysLocation.getLocationId();
					SystemUtils.trace("LocalData", "getTownLocationText" + localHaysLocation.getLocationId()+ " "+localHaysLocation.getDefaultDescription()+ " "+locationColumn);
					String locationText = getTownLocationText(localHaysLocation, locationColumn);
					String urlMatch = locationText.toLowerCase().replace(" ", "-");
					SystemUtils.trace("LocalData", "Town Location urlMatch: " + urlMatch);
					if(urlPart.equals(urlMatch)){
						lhtl = localHaysLocation;
						SystemUtils.trace("LocalData", "url pattern found, location Id: " + localHaysLocation.getLocationId());
						//cache the URL for future use
						lhrl.getUrlMap().put(urlMatch, lhtl);
						SystemUtils.trace("LocalData", "put location url in cache: " + lhrl.getUrlMap());
						break;
					}
				}
			}
			if(lhtl == null){
				//not found
				SystemUtils.trace("LocalData", "Town location url pattern not found in set up: " + urlPart);
				return null;
			}
		}
		return lhtl;
	}
	
	private String getLocationText(LocalHaysRegionLocation localHaysLocation, String locationColumn){
		if("default_description".equals(locationColumn)){
			return localHaysLocation.getDefaultDescription();
		}else if("default_description_1".equals(locationColumn)){
			return localHaysLocation.getDefaultDescription1();
		}else{
			return localHaysLocation.getDefaultDescription2();
		}
			
	}
	
	private String getTownLocationText(LocalHaysTownLocation localHaysLocation, String locationColumn){
		if("default_description".equals(locationColumn)){
			return localHaysLocation.getDefaultDescription();
		}else if("default_description_1".equals(locationColumn)){
			return localHaysLocation.getDefaultDescription1();
		}else{
			return localHaysLocation.getDefaultDescription2();
		}
			
	}
}
