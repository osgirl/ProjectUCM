RSSFeeds Component
==================

This component adds XML-based RSS feed support to the Content Server and
Site Studio. It includes several sample RSS feeds for searches, workflows,
error logs, dynamic lists, and static lists.

Its adds an "XML" button on every search results page to allow the user
to subscribe to the results with an RSS feed. As soon as a new Content
Item is checked-in that satisfies the criteria, the RSS feed is also updated.

It also adds a new page, "My Stellent RSS Feeds." From this page you can
access RSS feeds for your checked-out content, your workflow items,
your personal URLs, and your saved searches. An administrator can add
addition RSS feeds to this page as needed.

Finally, it adds new services and IdocScript functions that enable a 
developer to consume RSS feeds from other sites.

The "samples" folder contains samples of how to use RSS feeds with other
Stellent products. This includes sample HCSP, JSP, and HCST templates,
a Site Studio fragment library, and Site Studio templates.

Please read the RSSFeeds-guide.pdf for complete information on what RSS 
is, and how to use it with the Content Server.


Compatibility Information
-------------------------

This is now updated for 10gR3.  Because of changes in the core, it does
not support 7.5 or earlier.

This component superceeds the following components which you may
have installed:

RSS
RSSReader
RSSLogGenerator
RSSFunctions

Disable the above components before installing and enabling this one.
  


Change Log
----------
2008_09_02
  Swapped out the Site Studio fragment with one that imports into the 10gR3 version of Site Studio
  
2008_07_20
  Updated the component to place the text 'RSS' on the search results menu bar instead of the XML image.  
  This allows us to avoid having to modify a core standard resource.
  
2007_09-13
  Changed deleted xui_searchapi_results_action_form include and replaced with heading_menu_extra_right.
  XML icon now display at the right side of Query Actions on every search result page.  The 
  custom_finish_layout_init include was updated to MY_CONTENT instead of MY_STELLENT which was changed
  in 10gR3

2006_08_31
  Changed the RssDateFormat to hard-code the time zone to UTC. You can change
  it to something else by setting 'RssDateFormat' in the config.cfg.

2006_03_21
  Made the RSS log writer always log server restarts, regardless of the
  error log threshold. Fixed some typos in the documentation.

2006_02_23_1
  Fixed bug with the XML icon on some of the pages. Added another default
  feed so users can subscribe to their own content. Added another static 
  list template specific to Ravenna Office Locations page. Changed how
  guid is set to support wider array of RSS readers. Fixed bug with
  RSS log generator not publishing new items. Added more screenshots to
  documentation.
  
2006_02_22_1
  First publicly available build. Added Site Studio samples, finished the
  documentation, added some tutorials. Made minor changes to the component to
  help make Site Studio RSS feeds more flexible, and easier to create.

2006_02_21_1
  Quick build for testing. A few minor changes to make it easier to add 
  RSS feeds to Site Studio pages.

2006_02_20_1
  Added 'guid' tags to RSS Content Server logs so they can be more easily read 
  by alternate RSS readers. Fixed formatting problem in OPML files. Fixed some
  IdocScript errors. Added better error messages for badly formatted remote feeds.
  Needs testing with Site Studio, more samples, and documentation.
  
2006_02_17_1
  Added more caching to Java code. Made the RSS includes easier to customize
  for both public and secured feeds. Fixed some bugs.

2006_02_16_3
  Got the Java code working!
  
2006_02_16_2
  Quick build for testing COAO on another machine
  
2006_02_16_1
  First attempt to consolidate RSS functionality into one component. Integrated
  most of the changes to the SCS UI, and tested the feeds with Thunderbird. 
  Working fine. Need to test with Outlook and Check-out And Open component.










