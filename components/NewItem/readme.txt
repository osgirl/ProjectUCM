NewItem Component
=================

This component implements a service that creates a new content item
based on a current content item, cloning the primary file of the 
original item, cloning the required and custom metadata (if this 
metadata is not already in local data), and checking the new 
content item in using the cloned primary file.

Support for alternate files is not implemented yet.

The new service is exposed on the docinfo page action menu as
Checkin New Item

This component also comes with the scriptable service 
"CHECKIN_NEW_ITEM_BY_NAME", which allows the user to make a copy of a
content item by name with IdocScript:

	<$originalName = "test123"$>
	<$dDocName = originalName$>
	<$newDocName = "copy1_" & originalName$>
	<$dDocTitle = "copy1 of item: " & originalName$>
	<$dDocAuthor = UserName$>
	<$executeService("CHECKIN_NEW_ITEM_BY_NAME")$>
	<$if StatusCode < 1$>
		Error: <$StatusMessage$>
	<$else$>
		Success: <$StatusMessage$>
	<$endif$>

This will create a new content item based on the item with the Content Name
"test123". The new item has a different Content Name, Content Title, and Author.
Other metadata fields can also be overidden, including custom fields.


Changelog
---------

Rev 6 - Bex
- removed requirement for 'newDocName' to be specified in the request.
  If IsAutoNumber is specified that parameter should be optional.
- placed appropriate transaction flags in the 'doNewItemCheckin' action
- added the 'postNewItemCheckin' filter

Rev 5 - 2006/03/07, Bex
- Fixed security hole which allowed a user to create a NewItem of an
  item he doesn't have rights to read. 
- Added "MaxNewCheckinsPerRequest" flag to prevent bad IdocScript from
  checking in unlimited documents. Some protection against denial-of-
  service attacks. Defaults to 5.
- Cleaned up source code, and repackaged it into a 'Component Class' folder.

Rev 4 - 2006/02/09, Ed Bryant
- Added the ability to change security group, doc account, author, and doctype 
  in addition to docname. This is ONLY for the service CHECKIN_NEW_ITEM_BY_NAME.
  Usage:
  * set newDocName idoc variable to the document name of the new content 
    (the "to" document).
  * set newSecurityGroup, newDocAccount, newAuthor, newDocType idoc variables
    to the metadata values for your new content (the "to" document). This step
    is not required if you wish these values copied from the source content.
  * set dDocName to the document name of the content item you wish to copy 
    (the "from" document).
  * execute the service CHECKIN_NEW_ITEM_BY_NAME


Rev 3 - 2005/02/28, Todd Price
- Added new scriptable service that allows Check In New Item by dDocName 
  instead of dID.

Rev 2 - 2005/02/23, Vijay Ramanathan
- The previous rev used the dOriginalName as name of the new file.  This 
  causes a problem with Folders since it doesn't allow 2 files with the same
  name in the same folder. This rev creates a new file name based on the 
  dOriginalName and a time stamp to reduce the chance of duplicate file names.
- Changed the string in action menu from "Checkin New Item" to "Check In 
  New Item" to make it more consistent with regular content server strings
- support for alternate files is not implemented yet

Rev 1 - 2004/11/04, Frank Radichel
- First revision


