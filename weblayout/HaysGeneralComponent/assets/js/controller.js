
// run scripts on load
$(function(){
	
	// add .JS class
	$('html').addClass('JS');
	
	
	// FOR DEV - box style switcher
	$('.js-box-switcher a').click(function(){
		var style = $(this).html();
		$('div.box').attr('class', 'box ' + style);
		return false;
	});
	
	
	// adds striping to lists and tables
	$('ul.box-group').each(function(){ $(this).find('> li:odd').addClass('odd'); })
	$('table.item-list').each(function(){ $(this).find('tbody tr:odd').addClass('odd'); })
	$('#office-results').each(function(){ $(this).find('tbody tr:odd').addClass('odd'); })
	
	
	// homepage hero/promo box rollovers
	$('div.hero-box, div.promo-box').hover(
		function(){ $(this).addClass('over'); },
		function(){ $(this).removeClass('over'); }
	);
	
	
	// tabbed boxes
	$('.tabbed').tabs();
	
	
	// collapsable boxes
	HY.BoxSwitcher.init();
	
	
	// category nav flyout
	if($('#expertise-nav')){
		HY.ExpertiseNav.init();
	}
	
	
	// latest news filter
	if($('#latest-news')){
		HY.NewsFilter.init();
	}
	
	
	// job results - toggle single line display
	$('#rv-oneline').click(function(){
		if($(this).is(':checked')){
			$('.job-results .jr-summary').hide();
		}
		else {
			$('.job-results .jr-summary').show();
		}
	});
	
	
	// overlays
	$('a.js-overlay').click(function(){
		var options = {	};
		var contentId = $(this).attr('href');
		$(contentId).modal(options);
		return false;
	});
	
	
	// form fields with hint information
	$('input.hint').each(function(){
		if(this.value != this.defaultValue){
			$(this).removeClass('hint');
		} 
	});
	$('input.hint').focus(function(){
		$(this).removeClass('hint');
		if(this.value == this.defaultValue){  
			this.value = '';  
		}  
		if(this.value != this.defaultValue){  
			this.select();  
		}
	}).blur(function(){
		if(this.value == ''){
			this.value = (this.defaultValue ? this.defaultValue : '');
			$(this).addClass('hint');
		} 
	});
	
	
	
	
	// drop down boxes
	$('select').dropdownchecklist();
	
	/*
	$('select').each(function(){
		
		
		var selectWidth = $(this).css('width').replace('px', '');
		
		$(this).dropdownchecklist({
			width: selectWidth,
			maxDropHeight: 150,
			emptyText: ''
		});
	});
	*/
	
	
	
});




HY.BoxSwitcher = {
	
	
	init: function(){
		$('.js-collapsable').each(function(){
										   
			// add switcher
			var box = $(this);
			var boxHeader = box.find('.box-header:first .box-content');
			var button = $('<a class="js-switch js-switch-closed" href="">Show</a>').click(function(event){
				HY.BoxSwitcher.toggle(event); return false;
			});
			boxHeader.prepend(button);
			
			// initialise box state
			if(box.hasClass('js-open')){
				HY.BoxSwitcher.showBox(box);
			}
			else{
				HY.BoxSwitcher.hideBox(box);
			}
		});
	},
	
	toggle: function(event){
		
		// get containing box and run appropriate script
		var box = $(event.target).parents('.js-collapsable:first');
		if(box.hasClass('js-open')){
			this.hideBox(box);
		}
		else{
			this.showBox(box);
		}
	},
	
	hideBox: function(box){
		
		// update box settings
		box.removeClass('js-open');
		box.find('.box-body:first').hide();
		box.find('.box-header:first .js-switch')
			.removeClass('js-switch-open')
			.addClass('js-switch-closed')
			.text('Show');
	},
	
	showBox: function(box){
		
		// update box settings
		box.addClass('js-open');
		box.find('.box-body:first').show();
		box.find('.box-header:first .js-switch')
			.removeClass('js-switch-closed')
			.addClass('js-switch-open')
			.text('Hide');
	}
	
}



/* Latest news filter
------------------------------------------------------------------------*/
HY.NewsFilter = {
	
	itemsPerPage: 	5,
	items: 			{},
	numItems: 		0,
	numPages: 		0,
	currentPage: 	0,
	displayFrom: 	0,
	displayTo: 		0,
	
	init: function(){
		
		// add events listeneres
		$('#ln-nav a').click(function(event){ HY.NewsFilter.filter(event.target); return false; });
		$('#ln-prev').click(function(){ HY.NewsFilter.prevPage(); return false; });
		$('#ln-next').click(function(){ HY.NewsFilter.nextPage(); return false; });
		
		// initialise the first tab
		$('#ln-nav li:first').addClass('selected');
		this.filter($('#ln-nav li:first a'));
	},
	
	filter: function(link){
		
		// get selected tab and reset settings
		var filterOn = $(link).attr('id');
		this.resetSettings(filterOn);
		
		// set selected state
		$('#ln-nav li').removeClass('selected');
		$(link).parent().addClass('selected');
		
		// display first page
		this.displayPage(1);
		
	},
	
	resetSettings: function(filterOn){
		
		// update items object and item count
		switch(filterOn){
			case 'ln-news':
				this.items = $('#ln-items li.ln-news-item');
				break;
			case 'ln-articles':
				this.items = $('#ln-items li.ln-article-item');
				break;
			case 'ln-news-articles':
				this.items = $('#ln-items li');
		}
		this.numItems = this.items.length;
		
		// total number of pages
		this.numPages = Math.ceil(this.numItems / this.itemsPerPage);
		
		// current page and position
		this.currentPage = 1;
		this.displayFrom = 1;
		this.displayTo = (this.numItems < this.itemsPerPage) ? this.numItems : this.itemsPerPage;
		this.currentPos = this.displayFrom+'-'+this.displayTo;
		
	},
	
	displayPage: function(pageNum){
		
		// only run if the page number is valid
		if(pageNum > 0 && pageNum <= this.numPages){
			
			// update page and position vars
			this.currentPage = pageNum;
			this.displayFrom = 1 + (this.itemsPerPage * (this.currentPage - 1));
			this.displayTo = this.displayFrom + this.itemsPerPage - 1;
			this.displayTo = (this.numItems < this.displayTo) ? this.numItems : this.itemsPerPage;
			
			// write to page
			$('#ln-current').text(this.displayFrom+'-'+this.displayTo);
			$('#ln-total').text(this.numItems);
			
			// display selected items
			$('#ln-items li').hide();
			for(var i=this.displayFrom-1; i<=this.displayTo-1; i++){
				$(this.items[i]).show();
			}
		}
		
	},
	
	nextPage: function(){
		this.displayPage(this.currentPage+1);
	},
	
	prevPage: function(){
		this.displayPage(this.currentPage-1);
	}
	
}






/* Category nav flyout
------------------------------------------------------------------------*/
HY.ExpertiseNav = {
	
	active: false,
	
	init: function(){
		// inject html
		var cn = $('#expertise-nav');
		cn.before('<div id="en-overlay">'
        	+'<div id="eno-category"></div>'
            +'<div id="eno-links"><ul></ul></div>'
        	+'</div>');
		// add listeners
		$('#expertise-nav li').mouseenter(
			function(event){
				HY.ExpertiseNav.show(event);
			}
		);
		$('#en-overlay').mouseleave(
			function(){
				HY.ExpertiseNav.hide();
			}
		);

	},
	
	show: function(event){
		// get the node
		var li = (event.target.nodeName == 'LI') ? $(event.target) : $(event.target).parents('li');
		// test that there is are sub links
		var links = li.find('ul').html();
		if(links){
			// populate the flyout
			$('#eno-category').html(li.find('.en-title').html());
			$('#eno-links ul').html(li.find('ul').html());
			// position the elements
			var posX = li.position().left;
			var posY = li.position().top;
			$('#en-overlay').stop().css({
				left: posX+'px',
				top: posY+'px'
			})
			.show();
		}
	},
	
	hide: function(){
		if(!this.active){
			$('#en-overlay').hide();
		}
	}
};

