GoogleIntegration Component
This component contains all functionality that integrates with google, e.g Map integration, geocode finding etc

1.	For obtaining latitude and longitude information giving a PostCode or Town information use the following service
	Service - HAYS_GET_GEOCODES
	Input parameters - PostCode or Town or both
	Output Parameters - Latitude and Longitude

	For the above service to work set the following variable in the config.cfg file
	GoogleGeocodeRequestUrl=http://maps.google.com/maps/api/geocode/xml
	
2.	Map Integration functionality
	For map functionality add the following variable in the config.cfg file
	GoogleMapApiKey=ABQIAAAABn1n9RsOyMbZfmwdfXZ-WRQLa5ZeHyWDDboWRIhsX_1pU5I3jBRMTJ_htsuLZCleZP4xeuDL4kDA8A
	
	The value of the key needs to be updated for every environment, the value for different environment can be generated 
	from google map website http://www.google.com/apis/maps/signup.html
	
	To add the map on a template add the following two lines 
	
	<$include mapIntegrationjavaScriptCode$>
	<div id='haysMap' style="position:relative; width:760px; height:470px;"></div>
	
	The id of the div tag must be haysMap.