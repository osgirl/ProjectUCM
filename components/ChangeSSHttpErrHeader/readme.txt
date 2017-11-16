DetectHTTPFilter Component

How the component worK
============================
In the error page, we check for user agent that is calling the page
Usually error page returns a 200 ok but in our case we want to change
the header to 404 based on the crawler agent.
We check for crawler agent, if the user agent matched we set value to 
change the header to true.

How to setup DetectHTTPFilter Component work in Site Studio Error page
=======================================================================
In the error page for the site in Site Studio 
Add the following code in the <head> </head>

<!--$var = HTTP_USER_AGENT-->
<!--$if regexMatches(var,".*MSIE.*")-->
<!--$setValue("#local","ssChangeHTTPHeader","true")-->
<!--$endif-->

Replace the MSIE with the User-Agent that crawls the website
(in the example above it looking for Microsoft IE browser agent)


Component has a java class file that changes the HTTP header from 200 to 404



Note:
This is merely an example of how to change HTTP Header from a 200 to 404 
on an Error page and is not supported by Oracle/Stellent. 
 
