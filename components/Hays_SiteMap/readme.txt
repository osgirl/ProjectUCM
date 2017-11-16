Hays_SiteMap Component:

Create a new Page Template in SS and set its friendly URL to sitemap.xml. Copy the folloing code into this template:


<!--$exec executeService("GET_GOOGLE_SITEMAP")-->
<!--$siteMapXml--><!--$setContentType("text/xml")-->

If SS Designer adds extra lines of code change the template directly.

Enable friendly url feature by adding SSUrlFieldName=<metadata name> to conf file (see SS Technical Reference guide). 
Set <metadata name. value for the Google Site Map page template to 'sitemap.xml'