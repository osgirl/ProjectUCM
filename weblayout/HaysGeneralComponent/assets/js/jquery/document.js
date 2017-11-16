var DDDEV = new Object();
var HAYS = new Object();

/*DDDEV Equal height*/
DDDEV.equalHeight = function(e, start, finish) {
	var tallest = 0;
	start--;
	$(e).slice(start,finish).each(function() {
		if ($(this).height() > tallest) {
			tallest = $(this).height();
		}
	});
	$(e).slice(start,finish).css('min-height',tallest+'px','height','auto!important','height',tallest+'px');
}

$(document).ready(function(){
	/* Booommmmmmmmmm */
	$('body').addClass('jon');
	
	/*Multiselect*/ 
	DDDEV.hide_multiselect = function(e) { //To be added to a more generic/better performing DDDEV funtion later
		if(!$(e.target).is('.multiselect *')) {
			$('.multiselect > div').trigger('click');
			$(document).unbind('click');
		}
	}	
	$('.multiselect > div').click ( function() {
		$(this).toggleClass('ms_hover');
		$(document).bind('click', DDDEV.hide_multiselect);
	});
	$('.multiselect > div').keypress (function(e) {
		if (e.keyCode != 9) {
			$(this).trigger('click');
			$('.multiselect > ol').focus();
		}
	});
	$('.multiselect input').click (function() {
		var ms_selected = '';
		$('.multiselect :checked').each (function(i) {
			ms_selected = ms_selected + $(this).attr('name') + ', ';
		});	
		$('#ms_selected').text(ms_selected.substring(0, ms_selected.length - 2));
	});
	$('.multiselect input:last').blur (function() {
		$('.multiselect > div').trigger('click');
	});	
	
	/* grid_33 Equal heights */
	$('.grid_33').each (function() { // For each grid_33 on-screen
		HAYS.grid_33 = $('.promo_box',this);
		if ($(this).parent().attr('id') !== 'Expertise') { //Expertise overriden
			for (i=1;i<HAYS.grid_33.length;i=i+3) {
				DDDEV.equalHeight(HAYS.grid_33,i,i+2);
			}
		}
		else {
			for (i=1;i<HAYS.grid_33.length;i=i+2) {
				DDDEV.equalHeight(HAYS.grid_33,i,i+1);
			}
		}
		HAYS.nav_33 = $('> li > a',this);
		for (i=1;i<HAYS.nav_33.length;i=i+3) {
			DDDEV.equalHeight(HAYS.nav_33,i,i+2);
		}		
	});
	
	/* grid_50 Equal heights*/
	$('.grid_50').each (function() {
		HAYS.grid_50 = $('.promo_box',this);
		for (i=1;i<HAYS.grid_50.length;i=i+2) {
			DDDEV.equalHeight(HAYS.grid_50,i,i+1);
		}
		HAYS.nav_50 = $('> li > a',this);
		for (i=1;i<HAYS.nav_50.length;i=i+2) {
			DDDEV.equalHeight(HAYS.nav_50,i,i+1);
		}
	});
	
	/* grid_25 Equal heights*/
	$('.grid_25').each (function() {
		HAYS.grid_25 = $('.promo_box',this);
		for (i=1;i<HAYS.grid_25.length;i=i+3) {
			DDDEV.equalHeight(HAYS.grid_25,i,i+3);
		}
	});
	
	/* Toggle*/
	$('.toggle').each (function(t) { // Need to DDDEV function later
		$('.thead',this).each (function(i) {
			$(this).append('<a href="#toggle'+t+'_pane'+i+'"><span class="access">'+$(this).text()+'</span></a>');
			$(this).next().attr('id',('toggle'+t+'_pane'+i));
			$(this).click (function() {
				$(this).toggleClass('show');
				return false;
			});
		});
	});

 });

/*Suckerfish*/
sfHover = function() {
	var sfEls = document.getElementById("suckerfish").getElementsByTagName("LI");
	for (var i=0; i<sfEls.length; i++) {
		sfEls[i].onmouseover=function() {
			this.className+=" sfhover";
		}
		sfEls[i].onmouseout=function() {
			this.className=this.className.replace(new RegExp(" sfhover\\b"), "");
		}
	}
}
if (window.attachEvent) window.attachEvent("onload", sfHover);

jQuery.validator.addMethod("require_from_group", function(value, element, options) {
    numberRequired = options[0];
    selector = options[1];
    //Look for our selector within the parent form
	
    var validOrNot = $(selector, element.form).filter(function() {
         // Each field is kept if it has a value
		 return $(this).not('.watermark').val();
         // Set to true if there are enough, else to false
    }).length >= numberRequired;

    //The elegent part - this element needs to check the others that match the
    //selector, but we don't want to set off a feedback loop where all the
    //elements check all the others which check all the others which
    //check all the others...
    //So instead we
    //  1) Flag all matching elements as 'currently being validated'
    //  using jQuery's .data()
    //  2) Re-run validation on each of them. Since the others are now
    //     flagged as being in the process, they will skip this section,
    //     and therefore won't turn around and validate everything else
    //  3) Once that's done, we remove the 'currently being validated' flag
    //     from all the elements
    /*
	if(!$(element).data('being_validated')) {
    var fields = $(selector, element.form);
    //.valid() means "validate using all applicable rules" (which 
    //includes this one)
    fields.data('being_validated', true).valid();
    fields.data('being_validated', false);
    }
	*/
	
    return validOrNot;
    // {0} below is the 0th item in the options field
    }, jQuery.format("Please fill out at least {0} of these fields.")
);

function nowatermark(value, element){
	return $(element).hasClass(':not(.watermark)');
}

$.validator.addMethod("nowatermark", nowatermark, "This field is required.");