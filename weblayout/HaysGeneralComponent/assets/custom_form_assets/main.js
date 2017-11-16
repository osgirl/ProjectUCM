/**
 * Copyright (c) 2010 Ewen Elder
 *
 * Licensed under the MIT and GPL licenses:
 * http://www.opensource.org/licenses/mit-license.php
 * http://www.gnu.org/licenses/gpl.html
 *
 * You can use any code from this file.
 * I would appreciate it if you would add my contact details in the file that this code is used in, it's not necessary of course.
 * Also it would be cool if you would let me know where my stuff is being used.
 * Thanks :)
 *
 * @author: Ewen Elder <glomainn at yahoo dot co dot uk> <ewen at jainaewen dot com>
**/

var jae = {
	init : function ()
	{
		// Set stored text size if any and add accessibility features.
		this.accessibilityTextSize();
		
		// Carousel.
		this.carousel();
		
		// Head login form and Foot contact form.
		this.loginContactInputs();
		
		// Various IE thingies.
		this.ieThingies();
		
		// Event listeners.
		this.eventListeners();
	},
	
	/**
	 * Add resize text links.
	**/
	accessibilityPanel : function ()
	{
		var selector = 'ul.accessibility';
		
		$(selector).append('<li><small><a accesskey="-" title="Decrement font size by two pixels">A-</a></small></li>');
		$(selector).append('<li><strong><a accesskey="0" title="Reset the font size back to default">A</a></strong></li>');
		$(selector).append('<li><big><a accesskey="+" title="Increment font size by two pixels">A+</a></big></li>');
		$(selector).animate
		({
			top : 0
		});
	},
	
	/**
	 * Resize text etc.
	**/
	accessibilityTextSize : function (element)
	{
		var end, font, selectors, start;
		
		if (!$('div#main').length)
		{
			return false;
		}
		
		font = $('div#main').css('fontSize').replace('px', '');
		selectors = 'div#main div.sub,div#main div.main';
				
		// Set the stored font size;
		if (typeof element === 'undefined')
		{
			this.accessibilityPanel();
			
			if (document.cookie.indexOf('cfs=') !== -1)
			{
				start = document.cookie.indexOf('cfs=') + 4;
				end = document.cookie.indexOf(';', start);
				
				if (end === -1)
				{
					end = document.cookie.length;
				}
				
				font = document.cookie.substr(start, (end - start));
				
				$(selectors).css
				({
					fontSize : font + 'px'
				});
			}
			
			if (document.cookie.indexOf('dfs') === -1)
			{
				document.cookie = 'dfs=' + font;
			}
			
			return false;
		}
		
		
		switch ($(element).parent().attr('tagName').toLowerCase())
		{
			case 'small' :
				if (+font < 10)
				{
					return false;
				}
				
				font = +font - 2;
				document.cookie = 'cfs=' + font;
				
				$(selectors).css
				({
					fontSize : font + 'px'
				});
			break;
			
			case 'big' :
				font = +font + 2;
				document.cookie = 'cfs=' + font;
				
				$(selectors).css
				({
					fontSize : font + 'px'
				});
			break;
			
			case 'strong' :
				start = document.cookie.indexOf('dfs=') + 4;
				end = document.cookie.indexOf(';', start);
				
				if (end === -1)
				{
					end = document.cookie.length;
				}
				
				font = document.cookie.substr(start, (end - start));
				document.cookie = 'cfs=' + font;
				$(selectors).css
				({
					fontSize : font + 'px'
				});
			break;
		}
		
		return false;
	},
	
	/**
	 * Image Carousel.
	 * Add pagination links.
	 * Because this carousel requires the gallery element to be
	 * mixed in with tag soup, we will add another div wrapper
	 * to the gallery.
	**/
	carousel : function ()
	{
		// Make sure the gallery exists.
		if ($('#head ul.gallery').length === 0)
		{
			return false;
		}
		
		var gallery = $('#head ul.gallery');
		$('#head ul.gallery').remove();
		$('#head').append('<div class="gallery"><div class="container"><div></div></div></div>');
		$('#head div div').append($(gallery));
		$('#head ul.gallery').removeClass('gallery');
		$('div#head div.gallery').append('<a class="prev">&laquo;</a><a class="next">&raquo;</a>');
		
		$('div#head div.gallery div.container div').jCarouselLite({
			btnNext : '.next',
			btnPrev : '.prev',
			visible : 7,
			speed : 1000,
			auto : 1500
		});
	},
	
	
	/**
	 * Set the default values for the head login form and
	 * for the footer contact link form, clear the default
	 * values on focus, replace default values on blur if
	 * input elements are empty.
	**/
	loginContactInputs : function ()
	{
		// Head container's username and password inputs.
		if ($('div#head input#username').length > 0 && $('div#head input#password').length > 0)
		{
			var defUserVal, defPassVal;
			defUserVal = $('div#head label[for=username]').html().replace(':', '');
			defPassVal = $('div#head label[for=password]').html().replace(':', '');
			
			// Put Username and Password label text as
			// username and password input values.
			$('div#head input#username').val(defUserVal);
			$('div#head input#password').val(defPassVal);
			
			// On username focus, remove the default value.
			$('div#head input#username').focus
			(
				function ()
				{
					if (this.value === defUserVal)
					{
						this.value = '';
					}
				}
			);
			
			// On username blur, if the value is empty
			// then reset the default value.
			$('div#head input#username').blur
			(
				function ()
				{
					if (this.value === '')
					{
						this.value = defUserVal;
					}
				}
			);
			
			// On password focus, remove the default value.
			$('div#head input#password').focus
			(
				function ()
				{
					if (this.value === defPassVal)
					{
						this.value = '';
					}
				}
			);
			
			// On password blur, if the value is empty
			// then reset the default value.
			$('div#head input#password').blur
			(
				function ()
				{
					if (this.value === '')
					{
						this.value = defPassVal;
					}
				}
			);
		}
		
		
		// Foot containers contact email input.
		if ($('div#foot input#contact-hook-email').length > 0)
		{
			var defEmailVal = $('div#foot label[for=email]').html().replace(':', '');
			
			// Put Email label text as email input values.
			$('div#foot input#contact-hook-email').val(defEmailVal);
			
			// On email focus, remove the default value.
			$('div#foot input#contact-hook-email').focus
			(
				function ()
				{
					if (this.value === defEmailVal)
					{
						this.value = '';
					}
				}
			);
			
			// On email blur, if the value is empty then 
			// reset the default value.
			$('div#foot input#contact-hook-email').blur
			(
				function ()
				{
					if (this.value === '')
					{
						this.value = defEmailVal;
					}
				}
			);
		}
	},
	
	/**
	 * Various Internet Explorer thingies;
	 *	 pngfix for IE6
	**/
	ieThingies : function ()
	{
		var featuredDetails, i = 1;
		
		// If the browser isn't ie, return false.
		if (!document.all)
		{
			return false;
		}
		
		
		// Add corners for #main section.
		$('div#main').append('<span class="b"></span>');
		
		
		// IE 6 png fix
		if (jQuery.ifixpng)
		{
			// Add classNames to represent unsuported CSS selectors.
			$('ul.accessibility li:first-child').addClass('first-child');
			$('div.index-page div.news ul li:first-child').addClass('first-child');
			
			featuredDetails = $('div.index-page dl.details').children();
			$.each
			(
				featuredDetails,
				function ()
				{
					this.className = 'nth-child-' + i;
					i++;
				}
			);
			
			
			$('div#main').append('<span class="main-bg"></span>');
			$('div#main span.main-bg').css
			({
				height : ($('div#main').innerHeight() - 1) + 'px'
			});
			
			
			selectors = 'div#main span.main-bg,';
			selectors += 'div#main div.sub,';
			selectors += 'div#main div.main,';
			selectors += 'div#main span.bl,';
			selectors += 'div#main span.br,';
			selectors += 'div#main span.b,';
			selectors += 'div#foot,';
			selectors += 'div#foot div.info';
			
			$(selectors).ifixpng();
			
			
		}
	},
	
	/**
	 * Attatch event listeners.
	**/
	eventListeners : function ()
	{
		var This = this;
		
		// Accessibility text resize.
		$('ul.accessibility li:nth-child(2) a, ul.accessibility li:nth-child(3) a, ul.accessibility li:nth-child(4) a').bind
		(
			'click',
			function ()
			{
				This.accessibilityTextSize(this);
			}
		);
	}
};


/**
 * Various functions.
**/
var jaeFunctions = {
	
	/**
	 * Check if path can be found in the current URI.
	**/
	isPage : function (path)
	{
		if (window.location.href.indexOf(path) > -1)
		{
			return true;
		}
		
		return false;
	},
	
	
	randomNumber : function (strLength)
	{
		strLength = strLength > 0 ? strLength : 8;
		return String((new Date()).getTime()).replace(/\D/gi, '').substr(strLength)
	},
	
	
	/**
	 * Set and display the message box,
	 * (the message to set, the class name to use, append to existing messages)
	**/
	setMessage : function (message, className, append, animate, close)
	{
		var selector, This, timeout;
		selector = 'div#message-box';
		This = this;
		
		clearTimeout(timeout);
		
		// Set the new className and whether to keep or replace existing messages.
		className = !className.length ? 'message' : className;
		append = append ? true : false;
		animate = !animate ? false : true;
		
		// If the message box isn't in the DOM, add it.
		if (!$(selector).length)
		{
			$('body').prepend('<div id="message-box"><a class="close">Close</a><div class="messages"></div></div>');
			$(selector).css
			({
				display : 'none'
			});
			
			// Attach the close link listener.
			$(selector + ' a.close').bind
			(
				'click',
				function ()
				{
					This.setMessage('', '', '', '', true);
				}
			);
		}
		
		
		// If the message box is visible, animate the changes.
		if ($(selector).css('display') === 'block')
		{
			// Animate changes??
			if ($(selector).attr('className') !== className)
			{
				if (animate)
				{
					$(selector).fadeOut
					(
						150,
						function ()
						{
							// Change the message, then fade back in.
							This.setMessage(message, className, append, false, close);
							$(this).fadeIn(150);
						}
					);
					
					return false;
				}
				
				else
				{
					// Change the class name.
					$(selector).removeClass().addClass(className);
				}
			}
			
			
			// Close the box??
			if (close === true)
			{
				$(selector).fadeOut(400);
				return false;
			}
			
			// Close the box after a timeout??
			else if (typeof close === 'number')
			{
				timeout = setTimeout('$(\'div#message-box\').fadeOut(400)', close);
			}
			
		}
		
		// If the message box is not visible, animate it.
		else
		{
			// Change the class name.
			$(selector).removeClass().addClass(className);
			$(selector).fadeIn(150);
		}
		
		
		// Add the new message.
		if (!append)
		{
			$(selector + ' div.messages').children().remove();
		}
		
		$(selector + ' div.messages').append(message);
		
		
		// Figure out new position.
		$(selector).css
		({
			margin : '0 -' + ($(selector).outerWidth() / 2) + 'px'
		});
	}
};


/**
 * Contact page.
**/
var jaeContactPage = {
	
	init : function ()
	{
		if (!jaeFunctions.isPage('/contact.html'))
		{
			return false;
		}
		
		
		this.eventListeners();
	},
	
	submitContactForm : function (element)
	{
		jaeFunctions.setMessage('<div class="message">Verifying...</div>', 'message', false, true);
		
		$.post
		(
			element.action,
			$(element).serialize(),
			function (response)
			{
				if (response.indexOf('class="error') > -1)
				{
					className = 'error';
				}
				else
				{
					className = 'success';
				}
				
				jaeFunctions.setMessage(response, className, false, true);
			}
		);
		
		return false;
	},
	
	
	captcha : function ()
	{
		var selector = 'fieldset#contact-submit dd li img';
		
		$(selector).css
		({
			//display : 'none'
		});
		
		//$(selector).after('<span>Loading...</span>');
		$(selector).attr('src', '/files/php/captcha/image.php?' + jaeFunctions.randomNumber());
		
		return false;
	},
	
	
	eventListeners : function ()
	{
		var This;
		This = this;
		
		$('#contact').bind
		(
			'submit',
			function ()
			{
				return This.submitContactForm(this);
			}
		);
		
		$('fieldset#contact-submit a').bind
		(
			'click',
			function ()
			{
				return This.captcha();
			}
		);
	}
};



/**
 * Home page class.
**/
var jaeHomePage = {
	init : function ()
	{
		/*
		if (!jaeFunctions.isPage('/index.'))
		{
			return false;
		}
		*/
		
		this.featuredTabs();
		this.eventListeners();
		this.ieThingies();
	},
	
	/**
	 * Homepage Featured Image.
	 * Add the html for the top corners.
	**/
	featuredTabs : function ()
	{
		// Make sure the featured area exists.
		if ($('div.index-page div#main div.featured').length === 0)
		{
			return false;
		}
		
		// Add the html for the corners that cover the featured image.
		$('div#main div.main').append('<span class="l"></span><span class="r"></span>');
		
		// Show the image and remove the background image.
		$('div.featured img').fadeIn
		(
			250,
			function ()
			{
				// Remove the loader image.
				$(this).parent().css('background', 'none');
			}
		);
		
		// Add the featured tabs
		$('div#main').append('<ul class="tabs"><li><a href="#" class="tab-a">+ details</a></li><li><a href="#" class="tab-b">+ comments</a></li><li><a href="#" class="tab-c">+ fullview</a></li></ul>');
		
		// Animate the tabs
		$('ul.tabs li:first-child').animate
		(
			{
				right : '-25px'
			},
			500
		);
		
		$('ul.tabs li:nth-child(2)').animate
		(
			{
				right : '-25px'
			},
			600
		);
		
		$('ul.tabs li:last-child').animate
		(
			{
				right : '-25px'
			},
			700
		);
	},
	
	/**
	 * Animate the featured panel switching.
	**/
	animateFeaturedPanels : function (event)
	{
		var tabClassName, selectors = Array;
		
		tabClassName = event.target.className;
		selectors[0] = 'div.featured ul.comments';
		selectors[1] = 'div.featured dl.details';
		selectors[2] = 'div.featured ul.photograph img';
		selectors[3] = selectors[1] + ' dt:nth-child(11), ' + selectors[1] + ' dd:nth-child(12), ' + selectors[1] + ' dt:nth-child(13), ' + selectors[1] + ' dd:nth-child(14)';
		selectors[4] = 'div.featured ul.photograph li:first-child';
		
		// If tab a or b, which are details and comments, are clicked,
		// animate the image and fade in the comments/details
		if (tabClassName === 'tab-a' || tabClassName === 'tab-b')
		{
			// If tab a, fade in the details, also fade in the tags and description
			// incase they have already been shown and set to display:none;
			if (tabClassName === 'tab-a')
			{
				$(selectors[0]).fadeOut(400);
				$(selectors[1] + ', ' + selectors[3]).fadeIn(400);
			}
			
			// If tab b, fade in comments and details, but hide the detail tags and
			// description.
			else if (tabClassName === 'tab-b')
			{
				$(selectors[0] + ',' + selectors[1]).fadeIn(400);
				$(selectors[3]).fadeOut(400);
			}
			
			// Animate the main image.
			$(selectors[2]).css('display', 'inline').animate
			(
				{
					width : '220px',
					height : '133px',
					padding : '10px'
				},
				500
			);
		}
		
		// If tab c is clicked, then hide the details and comments
		// and animate the main image back to it's original size
		// and position.
		else if (tabClassName === 'tab-c')
		{
			$(selectors[0] + ',' + selectors[1]).fadeOut(400);
			
			$(selectors[2]).animate
			(
				 {
					width : '551px',
					height : '332px',
					padding : '0px'
				},
				500
			);
		}
	},
	
	/** 
	 * Internet Explorer thingies.
	**/
	ieThingies : function ()
	{
		if (jQuery.ifixpng)
		{
			selectors = '.index-page div#main div.featured span.l,';
			selectors += '.index-page div#main div.featured span.r,';
			
			$(selectors).ifixpng();
		}
	},
	
	/**
	 * Attatch event listeners.
	**/
	eventListeners : function ()
	{
		var This = this;
		
		// Homepage featured tabs.
		$('ul.tabs a.tab-a, ul.tabs a.tab-b, ul.tabs a.tab-c').bind
		(
			'click',
			function (event)
			{
				This.animateFeaturedPanels(event);
				return false;
			}
		);
	}
};



/**
 * PHP Highlight page class.
**/
var jaeHighlightPage = {
	init : function ()
	{
		if (!jaeFunctions.isPage('/code/highlighter.'))
		{
			return false;
		}
		
		this.resizable();
	},
	
	/**
	 * Resizable code highlight.
	**/
	resizable : function ()
	{
		var selector, storeHighlight;
		
		selector = 'div#main div.highlight';
		storeHighlight = $(selector);
		
		$(selector).after('<div id="highlight-container"></div>');
		$(selector).remove();
		
		selector = 'div#main div#highlight-container';
		$(selector).append(storeHighlight);
		$(selector).resizable();
	}
};

window.onload = function ()
{
	jae.init();
	jaeContactPage.init();
	jaeHomePage.init();
	jaeHighlightPage.init();
};