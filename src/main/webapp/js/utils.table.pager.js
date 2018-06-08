/*
 * Copyright (c) 2018 Feng Liu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*!
 * custom pager controls (beta) for Tablesorter - updated 9/1/2016 (v2.27.6)
  initialize custom pager script BEFORE initializing tablesorter/tablesorter pager
  custom pager looks like this:
  1 | 2 … 5 | 6 | 7 … 99 | 100
    _       _   _        _     adjacentSpacer
        _           _          distanceSpacer
  _____               ________ ends (2 default)
          _________            aroundCurrent (1 default)

 */
/*jshint browser:true, jquery:true, unused:false, loopfunc:true */
/*global jQuery: false */

;(function($) {
"use strict";

$.tablesorter = $.tablesorter || {};

$.tablesorter.customPagerControls = function(settings) {
	var defaults = {
		table          : 'table',
		pager          : '.pager',
		pageSize       : '.left a',
		currentPage    : '.right a',
		ends           : 2,                        // number of pages to show of either end
		aroundCurrent  : 1,                        // number of pages surrounding the current page
		link           : '<a href="#">{page}</a>', // page element; use {page} to include the page number
		currentClass   : 'current',                // current page class name
		adjacentSpacer : '<span> | </span>',       // spacer for page numbers next to each other
		distanceSpacer : '<span> &#133; <span>',   // spacer for page numbers away from each other (ellipsis)
		addKeyboard    : true,                     // use left,right,up,down,pageUp,pageDown,home, or end to change current page
		pageKeyStep    : 10                        // page step to use for pageUp and pageDown
	},
	options = $.extend({}, defaults, settings),
	$table = $(options.table),
	$pager = $(options.pager);

	$table
		.on('pagerInitialized pagerComplete', function (e, c) {
			var indx,
				p = c.pager ? c.pager : c, // using widget
				pages = $('<div/>'),
				cur = p.page + 1,
				pageArray = [],
				max = p.filteredPages,
				around = options.aroundCurrent;
			for (indx = -around; indx <= around; indx++) {
				if (cur + indx >= 1 && cur + indx <= max) {
					pageArray.push(cur + indx);
				}
			}
			if (pageArray.length) {
				// include first and last pages (ends) in the pagination
				for (indx = 0; indx < options.ends; indx++) {
					if ((indx + 1 <= max) && $.inArray(indx + 1, pageArray) === -1) {
						pageArray.push(indx + 1);
					}
					if ((max - indx > 0) && $.inArray(max - indx, pageArray) === -1) {
						pageArray.push(max - indx);
					}
				}
				// sort the list
				pageArray = pageArray.sort(function(a, b) { return a - b; });
				// only include unique pages
				pageArray = $.grep(pageArray, function(value, key) {
					return $.inArray(value, pageArray) === key;
				});
				// make links and spacers
				if (pageArray.length) {
					max = pageArray.length - 1;
					$.each(pageArray, function(indx, value) {
						pages
							.append(
								$(options.link.replace(/\{page\}/g, value))
									.toggleClass(options.currentClass, value === cur)
									.attr('data-page', value)
							)
							.append((indx < max && (pageArray[ indx + 1 ] - 1 !== value) ?
								options.distanceSpacer :
								(indx >= max ? '' : options.adjacentSpacer)
							));
					});
				}
			}
			$pager
				.find('.pagecount')
				.html(pages.html())
				.find('.' + options.currentClass)
				.focus();
		});

	// set up pager controls
	$pager
		.find(options.pageSize)
		.on('click', function () {
			$(this)
			.addClass(options.currentClass)
			.siblings()
			.removeClass(options.currentClass);
			$table.trigger('pageSize', $(this).html());
			return false;
		})
		.end()
		.on('click', options.currentPage, function() {
			var $el = $(this);
			$el
				.addClass(options.currentClass)
				.siblings()
				.removeClass(options.currentClass);
			$table.trigger('pageSet', $el.attr('data-page'));
			return false;
		});

	// make right/left arrow keys work
	if (options.addKeyboard) {
		$(document).on('keydown', function(events) {
			// ignore arrows inside form elements
			if (/input|select|textarea/i.test(events.target.nodeName) ||
				!(events.which > 32 && events.which < 41)) {
				return;
			}
			// only allow keyboard use if element inside of pager is focused
			if ($(document.activeElement).closest(options.pager).is($pager)) {
				events.preventDefault();
				var key = events.which,
					max = $table[0].config.totalRows,
					$el = $pager.find(options.currentPage).filter('.' + options.currentClass),
					page = $el.length ? parseInt($el.attr('data-page'), 10) : null;
				if (page) {
					if (key === 33) { page -= options.pageKeyStep; } // pageUp
					if (key === 34) { page += options.pageKeyStep; } // pageDown
					if (key === 35) { page = max; } // end
					if (key === 36) { page = 1; }   // home
					if (key === 37 || key === 38) { page -= 1; } // left/up
					if (key === 39 || key === 40) { page += 1; } // right/down
					$table.trigger('pageSet', page);
				}
			}
		});
	}
};
})(jQuery);

/**************************************************/

$(function(){

	// initialize custom pager script BEFORE initializing tablesorter/tablesorter pager
	// custom pager looks like this:
	// 1 | 2 … 5 | 6 | 7 … 99 | 100
	//   _       _   _        _     adjacentSpacer
	//       _           _          distanceSpacer
	// _____               ________ ends (2 default)
	//         _________            aroundCurrent (1 default)

	var $table = $('table'),
		$pager = $('.pager');

	$.tablesorter.customPagerControls({
		table          : $table,                   // point at correct table (string or jQuery object)
		pager          : $pager,                   // pager wrapper (string or jQuery object)
		pageSize       : '.left a',                // container for page sizes
		currentPage    : '.right a',               // container for page selectors
		ends           : 2,                        // number of pages to show of either end
		aroundCurrent  : 1,                        // number of pages surrounding the current page
		link           : '<a href="#">{page}</a>', // page element; use {page} to include the page number
		currentClass   : 'current',                // current page class name
		adjacentSpacer : '<span> | </span>',       // spacer for page numbers next to each other
		distanceSpacer : '<span> &#133; <span>',   // spacer for page numbers away from each other (ellipsis = &amp;#133;)
		addKeyboard    : true,                     // use left,right,up,down,pageUp,pageDown,home, or end to change current page
		pageKeyStep    : 10                        // page step to use for pageUp and pageDown
	});

	// initialize tablesorter & pager
	$table
		.tablesorter({
			theme: 'dropbox',
			widgets: ['zebra', 'columns', 'filter'],

		ignoreCase: true,

		// forces the user to have this/these column(s) sorted first
		sortForce: null,
		// initial sort order of the columns, example sortList: [[0,0],[1,0]],
		// [[columnIndex, sortDirection], ... ]
		sortList: [ [0,0],[1,0]],
		// default sort that is added to the end of the users sort
		// selection.
		sortAppend: null,


		// fix the column widths
		widthFixed: false,

		// Show an indeterminate timer icon in the header when the table
		// is sorted or filtered
		showProcessing: false,

		// header layout template (HTML ok); {content} = innerHTML,
		// {icon} = <i/> (class from cssIcon)
		headerTemplate: '{content}{icon}',

		// return the modified template string
		onRenderTemplate: null, 
		})
		.tablesorterPager({
			// target the pager markup - see the HTML block below
			container: $pager,
			size: 12,
			 
			output: 'Showing: {startRow} to {endRow} ({filteredRows})'
		});

});