/*
 * Name:		main.js
 * Description:	Includes all other JavaScript files
 */

// create a global namespace
var HY = {};

/*
| -------------------------------------------------------------------
|  WEB_ROOT
| -------------------------------------------------------------------
| 
| Location of the application root folder
| Used mainly to locate assets when HTML page is called from outside
| of the root directory
|
*/
var WEB_ROOT = '';

// define scripts array for include
var load_scripts = new Array(
	'/haysassets/HaysGeneralComponent/assets/js/jquery/jquery-1.4.1.min.js',
	'/haysassets/HaysGeneralComponent/assets/js/jquery/jquery-ui-1.8rc1.custom.min.js',
	'/haysassets/HaysGeneralComponent/assets/js/jquery/jquery.simplemodal-1.3.4.js',
	'/haysassets/HaysGeneralComponent/assets/js/jquery/ui.dropdownchecklist.js',
	'/haysassets/HaysGeneralComponent/assets/js/swfobject.js',
	'/haysassets/HaysGeneralComponent/assets/js/controller.js'
);

// print script tags
for(var i=0, len=load_scripts.length; i<len; i++){
	try{
		// inserting via DOM fails in Safari 2.0, so brute force approach
		document.write('<script type="text/javascript" src="'+WEB_ROOT+load_scripts[i]+'"><\/script>');
	}
	catch(e){
		var script = document.createElement('script');
		script.type = 'text/javascript';
		script.src = load_scripts[i];
		document.getElementByTagName('head')[0].appendChild(script);
	}
}