
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

/* zip mask */
$('.zipcode', '#academic_info').keydown(
		function(e) {
			var key = e.charCode || e.keyCode || 0;
			$zip = $(this);

			if (key !== 8 && key !== 9) {
				if ($zip.val().length === 5) {
					$zip.val($zip.val() + '-');
				}
			}
			// Allow numeric (and tab, backspace, delete) keys only
			return (key == 8 || key == 9 || key == 46
					|| (key >= 48 && key <= 57) || (key >= 96 && key <= 105));
})

/*
 * Select a date from a popup or inline calendar
 */
$(function() {
	var reportStartYear = parseInt(semester.split(" ")[1]);
	var reportEndYear = reportStartYear + 1;   
	var year_range = "'" + reportStartYear + ":" + reportEndYear + "'"; 
 	$('#conference_date').datepicker({
		changeMonth: true,
	    changeYear: true,
	    minDate: new Date(reportStartYear,7,1),
	 	maxDate: new Date(reportEndYear,6,31),
		dateFormat : 'mm/dd/yy'
	});
	
    $( "#job_startdate" ).datepicker({
      defaultDate: "+1w",
      changeMonth: true,
      changeYear: true,
      	minDate: new Date(reportStartYear,7,1),
	 	maxDate: new Date(reportEndYear,6,31),
      dateFormat : 'mm/dd/yy',
      onClose: function( selectedDate ) {
        $( "#job_enddate" ).datepicker( "option", "minDate", selectedDate );
      }
    });
    $( "#job_enddate" ).datepicker({
      defaultDate: "+1w",
      changeMonth: true,
      changeYear: true, 
      minDate: new Date(reportStartYear,7,1),
	  maxDate: new Date(reportEndYear,6,31),
      dateFormat : 'mm/dd/yy',
      onClose: function( selectedDate ) {
        $( "#job_startdate" ).datepicker( "option", "maxDate", selectedDate );
      }
    });
    $( "#travel_startdate" ).datepicker({
	      defaultDate: "+1w",
	      changeMonth: true,
	      changeYear: true,
	     minDate: new Date(reportStartYear,7,1),
	    maxDate: new Date(reportEndYear,6,31),
	      numberOfMonths: 1,
	      dateFormat : 'mm/dd/yy',
	      onClose: function( selectedDate ) {
	        $( "#travel_enddate" ).datepicker( "option", "minDate", selectedDate );
	      }
	    });
	$( "#travel_enddate" ).datepicker({
	      defaultDate: "+1w",
	      changeMonth: true,
	      changeYear: true, 
	      minDate: new Date(reportStartYear,7,1),
		  maxDate: new Date(reportEndYear,6,31),
	      dateFormat : 'mm/dd/yy',
	      onClose: function( selectedDate ) {
	        $( "#travel_startdate" ).datepicker( "option", "maxDate", selectedDate );
	      }
	});
  });

/* Internship: manage entry */ 
$(function () {
	$.each(JSON.parse(internJson), function (index, data) {
		$('#tableIntern > tbody:last-child').append("<tr>"+
				  "<td><a href='javascript:void(0);' id='removeIntern' Title='Remove Entry'><span><i class='fa fa-times' aria-hidden='true'></i></span></a></td>"+
				  "<td><a href='javascript:void(0);' id='editIntern' Title='Edit Entry'><span><i class='fa fa-pencil' aria-hidden='true'></i></span></a></td>"+
				  "<td>"+data['jobCompany']+"</td>" + 
				  "<td>"+data['jobCity']+"</td>" + 
				  "<td>"+data['jobState']+"</td>" + 
				  "<td>"+data['jobStartDate']+"</td>" + 
				  "<td>"+data['jobEndDate']+"</td>" +
				  "<td>"+data['jobType']+"</td>" +
				  "<td>"+data['jobDuty']+"</td></tr>");
	}); 
	
    $(document).on('click', 'td #removeIntern', function() {
	    	if (confirm('Remove the entry?')) {
	    		$(this).closest("tr").remove();
	    	} 
    });
	
    $('#internship_info').submit(function() {
        var rows = [];
        $('table#tableIntern tbody tr:has(td)').each(function(i, n){
            var $row = $(n);
            rows.push({
            		jobCompany: $.trim($row.find("td:nth-child(3)").text()),
            		jobCity:   $.trim($row.find("td:nth-child(4)").text()),
            		jobState:    $.trim($row.find('td:nth-child(5)').text()),
            		jobStartDate:       $.trim($row.find('td:nth-child(6)').text()),
            		jobEndDate:         $.trim($row.find('td:nth-child(7)').text()),
            		jobType:        $.trim($row.find('td:nth-child(8)').text()),
            		jobDuty:          $.trim($row.find('td:nth-child(9)').text())
            });
        });
         
        if(rows.length > 10){
        		return false; 
        }else{ 
        		$("#internJson").val(JSON.stringify(rows)); 
        		return true; 
        } 
    });
 
    var dialogIntern = function (type, row) {
        var dlg = $("#modalIntern").clone();
        var form = dlg.find(("#formIntern")); 
        type = type || 'Create';
        var config = {
            autoOpen: true,
            draggable: true,
            height: "auto",
            width: "auto",
            resizable: true,
            modal: true,
            show : "blind", hide : "blind", 
            open : function(event, ui){
            		$(".ui-widget-overlay").css({background: "#000000", opacity: 0.5});
            },
            buttons: {
            		"Cancel": function () {
                    dlg.dialog("close");
                }, 
                "Done": save_data
            },
            close: function () {
                dlg.remove();
            }
        };
        if (type === 'Edit') {
            config.title = "Edit";
            get_data();
            delete(config.buttons['Done']);
            config.buttons['Done'] = function () {
                row.remove();
                save_data();
            };
        }
        dlg.dialog(config);

        function get_data() { 
	        	dlg.find(("#jobCompany")).val(row.find("td:nth-child(3)").text());
	        	dlg.find(("#jobCity")).val(row.find("td:nth-child(4)").text());
	        	dlg.find(("#job_state")).val(row.find("td:nth-child(5)").text()).prop('selected', true);
	        	dlg.find(("#job_startdate")).val(row.find("td:nth-child(6)").text());
	        	dlg.find(("#job_enddate")).val(row.find("td:nth-child(7)").text());
	        	dlg.find(("#job_type")).val(row.find("td:nth-child(8)").text()).prop('selected', true);
	        	dlg.find(("#job_duty")).val(row.find("td:nth-child(9)").text()); 
        }

        function save_data() {
	        	var data = form.serializeArray().reduce(function(obj, item) {
	    		    obj[item.name] = item.value;
	    		    return obj;
	    		}, {});
	        	
	        	var valid = 1; 
	        	Object.keys(data).forEach(function(key) {
	        		if($.trim(data[key]).length === 0) {
	        			valid = 0;  
	        		}
	        	}); 
     		 if(valid === 1){
     			$('#tableIntern > tbody:last-child').append("<tr>"+
  	      			  "<td><a href='javascript:void(0);' id='removeIntern' Title='Remove Entry'><span><i class='fa fa-times' aria-hidden='true'></i></span></a></td>"+
  	      			  "<td><a href='javascript:void(0);' id='editIntern' Title='Edit Entry'><span><i class='fa fa-pencil' aria-hidden='true'></i></span></a></td>"+
  	      			  "<td>"+data['jobCompany']+"</td>" + 
  	      			  "<td>"+data['jobCity']+"</td>" + 
  	      			  "<td>"+data['jobState']+"</td>" + 
  	      			  "<td>"+data['job_startdate']+"</td>" + 
  	      			  "<td>"+data['job_enddate']+"</td>" +
  	      			  "<td>"+data['job_type']+"</td>" +
  	      			  "<td>"+data['job_duty']+"</td></tr>");
              dlg.dialog("close");
     		 }
	        	
	        
        }
    };
     
	$(document).on('click', '#editIntern', function() {
		dialogIntern('Edit', $(this).parents('tr')); 
    });
     
    $("#addIntern").button().click(dialogIntern);
  
});


/* Conference */ 
$(function () {
	$.each(JSON.parse(conferenceJson), function (index, data) {
		$('#tableConference > tbody:last-child').append("<tr>"+
				  "<td><a href='javascript:void(0);' id='removeConference' Title='Remove Entry'><span><i class='fa fa-times' aria-hidden='true'></i></span></a></td>"+
				  "<td><a href='javascript:void(0);' id='editConference' Title='Edit Entry'><span><i class='fa fa-pencil' aria-hidden='true'></i></span></a></td>"+
				  "<td>"+data['conferenceName']+"</td>" + 
      			  "<td>"+data['conferencePresentationTitle']+"</td>" + 
      			  "<td>"+data['conferenceDate']+"</td>" + 
      			  "<td>"+data['conferencePresentationType']+"</td></tr>"); 
	});  
 
    $(document).on('click', 'td #removeConference', function() {
	    	if (confirm('Remove the entry?')) {
	    		$(this).closest("tr").remove();
	    	} 
    });
 
    var dialogConference = function (type, row) {
        var dlg = $("#modalConference").clone();
        var form = dlg.find(("#formConference")); 
        type = type || 'Create';
        var config = {
            autoOpen: true,
            draggable: true,
            height: "auto",
            width: "auto",
            resizable: true,
            modal: true,
            show : "blind", hide : "blind", 
            open : function(event, ui){
            		$(".ui-widget-overlay").css({background: "#000000", opacity: 0.5});
            },
            buttons: {
            		"Cancel": function () {
                    dlg.dialog("close");
                }, 
                "Done": save_data
            },
            close: function () {
                dlg.remove();
            }
        };
        if (type === 'Edit') {
            config.title = "Edit";
            get_data();
            delete(config.buttons['Done']);
            config.buttons['Done'] = function () {
                row.remove();
                save_data();
            };
        }
        dlg.dialog(config);

        function get_data() { 
	        	dlg.find(("#conferenceName")).val(row.find("td:nth-child(3)").text());
	        	dlg.find(("#conferencePresentationTitle")).val(row.find("td:nth-child(4)").text());
	        dlg.find(("#conference_date")).val(row.find("td:nth-child(5)").text());
	        dlg.find(("#conferencePresentationType")).val(row.find("td:nth-child(6)").text()).prop('selected', true);
        }

        function save_data() {
	        	var data = form.serializeArray().reduce(function(obj, item) {
	    		    obj[item.name] = item.value;
	    		    return obj;
	    		}, {});
	        	
	        	var valid = 1; 
	        	Object.keys(data).forEach(function(key) {
	        		if($.trim(data[key]).length === 0) {
	        			valid = 0;  
	        		}
	        	}); 
     		 if(valid === 1){ 
     			$('#tableConference > tbody:last-child').append("<tr>"+
  	      			  "<td><a href='javascript:void(0);' id='removeConference' Title='Remove Entry'><span><i class='fa fa-times' aria-hidden='true'></i></span></a></td>"+
  	      			  "<td><a href='javascript:void(0);' id='editConference' Title='Edit Entry'><span><i class='fa fa-pencil' aria-hidden='true'></i></span></a></td>"+
  	      			  "<td>"+data['conferenceName']+"</td>" + 
  	      			  "<td>"+data['conferencePresentationTitle']+"</td>" + 
  	      			  "<td>"+data['conferenceDate']+"</td>" + 
  	      			  "<td>"+data['conferencePresentationType']+"</td></tr>"); 
              dlg.dialog("close");
     		 }
        }
    };
   
	$(document).on('click', '#editConference', function() {
		dialogConference('Edit', $(this).parents('tr')); 
    });
     
    $("#addMoreConference").button().click(dialogConference);
    
    $('#conference_info').submit(function() {
        var rows = [];
        $('table#tableConference tbody tr:has(td)').each(function(i, n){
            var $row = $(n);
            rows.push({
            	conferenceName: $.trim($row.find("td:nth-child(3)").text()),
            	conferenceDate:   $.trim($row.find("td:nth-child(4)").text()),
            	conferencePresentationTitle:    $.trim($row.find('td:nth-child(5)').text()),
            	conferencePresentationType:       $.trim($row.find('td:nth-child(6)').text()),
            });
        });
          
        if(rows.length > 10){
        		return false; 
        }else{ 
        		$("#conferenceJson").val(JSON.stringify(rows)); 
        		return true; 
        } 
    });
  
});

/* Travel */ 
$(function () {
	$.each(JSON.parse(travelJson), function (index, data) { 
		$('#tableTravel > tbody:last-child').append("<tr>"+
				  "<td><a href='javascript:void(0);' id='removeTravel' Title='Remove Entry'><span><i class='fa fa-times' aria-hidden='true'></i></span></a></td>"+
				  "<td><a href='javascript:void(0);' id='editTravel' Title='Edit Entry'><span><i class='fa fa-pencil' aria-hidden='true'></i></span></a></td>"+
				  "<td>"+data['travelCity']+"</td>" + 
  	      			  "<td>"+data['travelState']+"</td>" + 
  	      			  "<td>"+data['travelStartDate']+"</td>" + 
  	      			"<td>"+data['travelEndDate']+"</td>" + 
  	      			  "<td>"+data['travelPurpose']+"</td></tr>"); 
	});  
 
    $(document).on('click', 'td #removeTravel', function() {
	    	if (confirm('Remove the entry?')) {
	    		$(this).closest("tr").remove();
	    	} 
    });
 
    var dialogTravel = function (type, row) {
        var dlg = $("#modalTravel").clone();
        var form = dlg.find(("#formTravel")); 
        type = type || 'Create';
        var config = {
            autoOpen: true,
            draggable: true,
            height: "auto",
            width: "auto",
            resizable: true,
            modal: true,
            show : "blind", hide : "blind", 
            open : function(event, ui){
            		$(".ui-widget-overlay").css({background: "#000000", opacity: 0.5});
            },
            buttons: {
            		"Cancel": function () {
                    dlg.dialog("close");
                }, 
                "Done": save_data
            },
            close: function () {
                dlg.remove();
            }
        };
        if (type === 'Edit') {
            config.title = "Edit";
            get_data();
            delete(config.buttons['Done']);
            config.buttons['Done'] = function () {
                row.remove();
                save_data();
            };
        }
        dlg.dialog(config);

        function get_data() { 
	        	dlg.find(("#travelCity")).val(row.find("td:nth-child(3)").text());
		    dlg.find(("#travelState")).val(row.find("td:nth-child(4)").text()).prop('selected', true);
	        	dlg.find(("#travel_startdate")).val(row.find("td:nth-child(5)").text());
	        dlg.find(("#travel_enddate")).val(row.find("td:nth-child(6)").text());
	        dlg.find(("#travelPurpose")).val(row.find("td:nth-child(7)").text()).prop('selected', true);
        }

        function save_data() {
	        	var data = form.serializeArray().reduce(function(obj, item) {
	    		    obj[item.name] = item.value;
	    		    return obj;
	    		}, {});
	        	
	        	var valid = 1; 
	        	Object.keys(data).forEach(function(key) {
	        		if($.trim(data[key]).length === 0) {
	        			valid = 0;  
	        		}
	        	}); 
     		 if(valid === 1){ 
     			$('#tableTravel > tbody:last-child').append("<tr>"+
  	      			  "<td><a href='javascript:void(0);' id='removeTravel' Title='Remove Entry'><span><i class='fa fa-times' aria-hidden='true'></i></span></a></td>"+
  	      			  "<td><a href='javascript:void(0);' id='editTravel' Title='Edit Entry'><span><i class='fa fa-pencil' aria-hidden='true'></i></span></a></td>"+
  	      			  "<td>"+data['travelCity']+"</td>" + 
  	      			  "<td>"+data['travelState']+"</td>" + 
  	      			  "<td>"+data['travelStartDate']+"</td>" + 
  	      			"<td>"+data['travelEndDate']+"</td>" + 
  	      			  "<td>"+data['travelPurpose']+"</td></tr>"); 
              dlg.dialog("close");
     		 }
        }
    };
   
	$(document).on('click', '#editTravel', function() {
		dialogTravel('Edit', $(this).parents('tr')); 
    });
     
    $("#addMoreTravel").button().click(dialogTravel);
    
    $('#travel_info').submit(function() {
        var rows = [];
        $('table#tableTravel tbody tr:has(td)').each(function(i, n){
            var $row = $(n);
            rows.push({
            	travelCity: $.trim($row.find("td:nth-child(3)").text()),
            	travelState:   $.trim($row.find("td:nth-child(4)").text()),
            	travelStartDate:    $.trim($row.find('td:nth-child(5)').text()),
            	travelEndDate:       $.trim($row.find('td:nth-child(6)').text()),
            	travelPurpose:       $.trim($row.find('td:nth-child(7)').text())
            });
        });
          
        if(rows.length > 10){
        		return false; 
        }else{ 
        		$("#travelJson").val(JSON.stringify(rows)); 
        		return true; 
        } 
    });
  
});

/* Awards */ 
$(function(){
	$.each(JSON.parse(awardsJson), function (index, data) {
		var row = $("#tableAwards tr:eq(1)").clone().appendTo("#tableAwards");
		row.attr("style", ""); 
		row.find("input").removeAttr("disabled");
		row.find("select").removeAttr("disabled");
		row.find("input[type='text']").val(data['awardsName']);
		row.find("select[name='awardsSemester']").val(data['awardsSemester']);
		row.find("select[name='awardsYear']").val(data['awardsYear']);
	}); 
	
    $('#addMoreAwards').on('click', function() {
      var data = $("#tableAwards tr:eq(1)").clone().appendTo("#tableAwards > tbody:last-child");
      data.attr("style", ""); 
      data.find("input").val('');
      data.find("select").val('');
      data.find("input").removeAttr("disabled");
      data.find("select").removeAttr("disabled"); 
     }); 
    
     $(document).on('click', 'td .removeAwards', function() {
         var trIndex = $(this).closest("tr").index();
         if (confirm('Remove the entry?')) {
        		$(this).closest("tr").remove();
        	} 
      });
 	
     $('#awards_info').submit(function() {
         var rows = [];
         $('table#tableAwards tbody tr:has(td)').each(function(i, n){
             var $row = $(n); 
             var name = $.trim($row.find("td:eq(1) input[type='text']").val()),
             semester = $.trim($row.find("td:eq(2) select").val()),
             year =  $.trim($row.find("td:eq(3) select").val()) ;
             if(name.length > 0 && semester.length > 0 & year.length > 0){
            	 	rows.push({
                	 awardsName: name,
                	 awardsSemester: semester,
                 awardsYear: year  
                 });
             }
         }); 
         
         if( rows.length > 10){
         		return false; 
         }else{
         		$("#awardsJson").val(JSON.stringify(rows)); 
         		return true; 
         } 
     });
}); 

/* Publication */ 
$(function(){
	$.each(JSON.parse(publicationJson), function (index, data) {
		var row = $("#tablePublication tr:eq(1)").clone().appendTo("#tablePublication");
		row.attr("style", ""); 
		row.find("textarea").removeAttr("disabled");
		//row.find("input[type='text']").val(data['awardsName']);
		row.find("textarea[name='publication']").val(data['publication']);
	}); 
	
    $('#addMorePublication').on('click', function() {
      var data = $("#tablePublication tr:eq(1)").clone().appendTo("#tablePublication > tbody:last-child");
      data.attr("style", ""); 
      data.find("textarea[name='publication']").val('');
     }); 
    
     $(document).on('click', 'td .removePublication', function() {
         var trIndex = $(this).closest("tr").index();
         if (confirm('Remove the entry?')) {
        		$(this).closest("tr").remove();
        	} 
      });
 	
     $('#publication_info').submit(function() {
         var rows = [];
         $('table#tablePublication tbody tr:has(td)').each(function(i, n){
             var $row = $(n); 
             var publication = $.trim($row.find("td:eq(1) textarea[name='publication']").val());
             if(publication.length > 0){
            	 	rows.push({
            	 		publication: publication 
                 });
             }
         }); 
         
         if(rows.length > 10){
      		return false; 
	      }else{
	      		$("#publicationJson").val(JSON.stringify(rows)); 
	      		return true; 
	      }  
     });
}); 