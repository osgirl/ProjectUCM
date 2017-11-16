WebUpload_Integration Component

1.	HAYS_GET_OFFICE_LOCATIONS   Service
	Input Parameters - Town or PostCode or Both
	OutParameters - Resultset OFFICE_LOCATIONS 
	Resultset Columns - OfficeId,OfficeName,BrandId,BrandName,Building,Street,Town,County,PostCode,Telephone,Fax,EmailAddress,Latitude,Longitude,miles.
	
	This service will be used for the office locator functionality for hays.
	
	Dependencies -
	1. This service depends on the HAYS_GET_GEOCODES service which is defined in GoogleIntegration component.
	2. UCM must be configured with a database provider with the following details
		Provider Name - SqlServer 
		Provider Description - SqlServer
		Provider Class - intradoc.jdbc.JdbcWorkspace
		Connection Class - intradoc.jdbc.JdbcConnection
		Configuration Class - intradoc.server.DbProviderConfig
		Test Query - select * from AdaptUser
		Database Type - JDBC (Selected)
		Database Directory - none
		Database Name - HaysGlobalLive
		JDBC Driver - net.sourceforge.jtds.jdbc.Driver
		JDBC Connection String - jdbc:jtds:sqlserver://10.175.151.218:1433/HaysGlobalLive;instance=ISOLATED
		JDBC User - WebUser
		Password - w3bus3r
		Number of Connections - 10
		
		Trouble shoot -
		In case of error 
		
		This is a preserve case database, but it is missing the ColumnTranslation table for it to work properly. Please check your resource tables.
		
		Add the following to the provider.hda of the new provider:

		@ResultSet ColumnMap
		2
		column
		alias
		@end

		The provider.hda can be found in <install-dir>/data/providers/<providername>/provider.hda