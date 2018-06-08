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

moment.tz.add("America/Denver|MST MDT MWT MPT|70 60 60 60|01010101023010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010|-261r0 1nX0 11B0 1nX0 11B0 1qL0 WN0 mn0 Ord0 8x20 ix0 LCN0 1fz0 1a10 1fz0 1cN0 1cL0 1cN0 1cL0 1cN0 1cL0 1cN0 1cL0 1cN0 1fz0 1cN0 1cL0 1cN0 1cL0 s10 1Vz0 LB0 1BX0 1cN0 1fz0 1a10 1fz0 1cN0 1cL0 1cN0 1cL0 1cN0 1cL0 1cN0 1cL0 1cN0 1fz0 1a10 1fz0 1cN0 1cL0 1cN0 1cL0 1cN0 1cL0 14p0 1lb0 14p0 1nX0 11B0 1nX0 11B0 1nX0 14p0 1lb0 14p0 1lb0 14p0 1nX0 11B0 1nX0 11B0 1nX0 14p0 1lb0 14p0 1lb0 14p0 1lb0 14p0 1nX0 11B0 1nX0 11B0 1nX0 14p0 1lb0 14p0 1lb0 14p0 1nX0 11B0 1nX0 11B0 1nX0 Rd0 1zb0 Op0 1zb0 Op0 1zb0 Rd0 1zb0 Op0 1zb0 Op0 1zb0 Op0 1zb0 Op0 1zb0 Op0 1zb0 Rd0 1zb0 Op0 1zb0 Op0 1zb0 Op0 1zb0 Op0 1zb0 Rd0 1zb0 Op0 1zb0 Op0 1zb0 Op0 1zb0 Op0 1zb0 Op0 1zb0 Rd0 1zb0 Op0 1zb0 Op0 1zb0 Op0 1zb0 Op0 1zb0 Rd0 1zb0 Op0 1zb0 Op0 1zb0 Op0 1zb0 Op0 1zb0 Op0 1zb0|26e5");
 
$(document).ready(function(){
	var rows_selected = [];
	var table = $('#applications').DataTable( {
		"aaData": applicationList, 
    	'columnDefs': [{
            'targets': 1,
            'searchable':false,
            'orderable':false,
            'className': 'dt-body-center',
            'render': function (data, type, full, meta){
            	return '<input class = "checkbox_input" type="checkbox" name="id[]" value="'+full.applicationID+'">';
            }
         },
         {
             'targets': 0,
             'searchable':false,
             'orderable':false,
             'className': 'dt-body-center',
             'render': function (data, type, full, meta){
            	 	return meta.row+1; 
            	 }
          },
          {
              'targets': 8,
              'searchable':false,
              'orderable':false,
              'className': 'dt-body-center',
              'render': function (data, type, full, meta){
            	  return '<button class="btn" id="details_info" type="submit">Details</button>';
             	 }
           }], 
	
          "aoColumns": [
			{"mData":""},
			{"mData": ""},
			{"mData":"applicationID"}, 
			{"mData":"firstName"},
			{ "mData": "lastName"},
			{ "mData": "email"},
			{ "mData": "schoolTarget"},
			{ "mData": "birthDate",
				"mRender":function(data, type, full){
					return moment.tz(full.birthDate, "America/Denver").format('MM/DD/YYYY');
				 }
			},
			{"mData": ""},
			{ "mData": "completeDate",
				"mRender":function(data, type, full){
					if(full.completeDate){
		            	return '<button class="btn" id="viewprogress" type="submit">complete</button>';
	            	}else{
		            	return '<button class="btn" id="viewprogress" type="submit">in progress</button>';
	            	}
				 }
			},
			{ "mData": "decision"},
			{ "mData": "notifiedDate",
				"mRender":function(data, type, full){
					if(full.notifiedDate == null)return '';
					return moment.tz(full.notifiedDate, "America/Denver").format('MM/DD/YYYY');
				 }
			}
          ],
          "paging":true,
          "pageLength":10,
          "ordering":true,
          "order":[2,"asc"]
        }); 
			
	table.on( 'order.dt search.dt', function () {
        table.column(0, {search:'applied', order:'applied'}).nodes().each( function (cell, i) {
            cell.innerHTML = i+1;
        } );
    } ).draw();
	
// Handle click on checkbox
$('#applications tbody').on('click', 'input[type="checkbox"]',
	function(e) {
		var $row = $(this).closest('tr');
		var data = table.row($row).data();
		var rowId = data[0];
		var index = $.inArray(rowId, rows_selected);

		if (this.checked && index === -1) {
			rows_selected.push(rowId);
		} else if (!this.checked && index !== -1) {
			rows_selected.splice(index, 1);
		}

		if (this.checked) {
			$row.addClass('selected');
		} else {
			$row.removeClass('selected');
		}

		updateDataTableSelectAllCtrl(table);
		e.stopPropagation();
	});
	$('thead input[name="select_all"]', table.table().container())
			.on('click', function(e) {
				if (this.checked) {
					$('#applications tbody input[type="checkbox"]:not(:checked)').trigger('click');
				} else {
					$('#applications tbody input[type="checkbox"]:checked').trigger('click');
				}

				e.stopPropagation();
	});
	
	table.on('draw', function() {
		updateDataTableSelectAllCtrl(table);
	});
	
	//added by qixu
	//show details information of one application
	$(function() {
		$('#applications tbody ').on('click','button#details_info',function () {
			var full = table.row($(this).parents('tr')).data();
//			alert(full.applicationID);
			window.open('manage-application-result/application-preview?applicationID='+full.applicationID+'&program='+program, 'name'); 
			})
		})
						    
	$(function() {
		$('#applications tbody ').on('click','button#viewprogress',function () {
			 var full = table.row($(this).parents('tr')).data();
			 $("#progressModal #progressModalName").html(full.firstName+" "+full.lastName); 
			 var htmlData = ''; 
  		   htmlData += '<table class="table table-striped">'; 
  		   htmlData += '<thead><tr><th>Item</th><th>Date Submitted</th></tr></thead>';
  		   htmlData += '<tbody>'; 
	  	   
  		 	var startDate = full.startDate;
			if (startDate != null) {
				startDate = moment.tz(full.startDate, "America/Denver").format('MM/DD/YYYY');
			}else{
				startDate = ''; 
			}
			
			var transcriptDate = full.transcriptDate;
			if (transcriptDate != null) {
				transcriptDate = moment.tz(full.transcriptDate, "America/Denver").format('MM/DD/YYYY');
			}else{
				transcriptDate = ''; 
			}
			
			var applicantSubmitDate = full.applicantSubmitDate;
			if (applicantSubmitDate != null) {
				applicantSubmitDate = moment.tz(full.applicantSubmitDate, "America/Denver").format('MM/DD/YYYY')
			}else{
				applicantSubmitDate = ''; 
			}
			 
			var recommenderSubmitDate = full.recommenderSubmitDate;
			if (recommenderSubmitDate != null) {
				recommenderSubmitDate = moment.tz(full.recommenderSubmitDate, "America/Denver").format('MM/DD/YYYY')
			}else{
				recommenderSubmitDate = ''; 
			}
		 
			var completeDate = full.completeDate;
			if (completeDate != null) {
				completeDate = moment.tz(full.completeDate, "America/Denver").format('MM/DD/YYYY')
			}else{
				completeDate = ''; 
			}
			
			htmlData += '<tr><td>Start Date</td><td>'+startDate+'</td></tr>'; 
	  		htmlData += '<tr><td>Complete Date</td><td>'+completeDate+'</td></tr>';
			switch(full.programNameAbbr) {
			case 'CCCONF':
				htmlData += '<tr><td>Application and Short Essay</td><td>'+applicantSubmitDate+'</td></tr>'; 
	    		htmlData += '<tr><td>Unofficial transcript</td><td>'+transcriptDate+'</td></tr>';
	    		break; 
	  	    case 'TRANS':
	  	    	htmlData += '<tr><td>Application with Essay Questions</td><td>'+applicantSubmitDate+'</td></tr>'; 
	    		htmlData += '<tr><td>Community College Transcripts</td><td>'+transcriptDate+'</td></tr>';
	    		htmlData += '<tr><td>Letter of Recommendation</td><td>'+recommenderSubmitDate+'</td></tr>';
	  	        break;
	  	    case 'MESA':
	  	    	htmlData += '<tr><td>Application with Essay Questions</td><td>'+applicantSubmitDate+'</td></tr>'; 
	    		htmlData += '<tr><td>Unofficial transcript</td><td>'+transcriptDate+'</td></tr>';
	    		htmlData += '<tr><td>Letter of Reference</td><td>'+recommenderSubmitDate+'</td></tr>';
	  	    	break; 
	  	    case 'SCCORE':
	  	    	var medicalSubmitDate = full.medicalSubmitDate;
				if (medicalSubmitDate != null) {
					medicalSubmitDate = moment.tz(full.medicalSubmitDate, "America/Denver").format('MM/DD/YYYY')
				}else{
					medicalSubmitDate = ''; 
				}
	  	    	htmlData += '<tr><td>Application with Questionnaire</td><td>'+applicantSubmitDate+'</td></tr>'; 
	    		htmlData += '<tr><td>Unofficial transcript</td><td>'+transcriptDate+'</td></tr>';
	    		htmlData += '<tr><td>Medical Authorization Form</td><td>'+medicalSubmitDate+'</td></tr>';
	    		break; 
	  	    case 'URS':
				var mentorInfoDate = full.mentorInfoDate;
				if (mentorInfoDate != null) {
					mentorInfoDate = moment.tz(full.mentorInfoDate, "America/Denver").format('MM/DD/YYYY')
				}else{
					mentorInfoDate = ''; 
				}
				var mentorSubmitDate = full.mentorSubmitDate;
				if (mentorSubmitDate != null) {
					mentorSubmitDate = moment.tz(full.mentorSubmitDate, "America/Denver").format('MM/DD/YYYY')
				}else{
					mentorSubmitDate = ''; 
				}
	  	    	htmlData += '<tr><td>Application with Essay Questions</td><td>'+applicantSubmitDate+'</td></tr>'; 
	    		htmlData += '<tr><td>Unofficial transcript</td><td>'+transcriptDate+'</td></tr>';
	    		htmlData += '<tr><td>Faculty Mentor Information</td><td>'+mentorInfoDate+'</td></tr>';
	    		htmlData += '<tr><td>Project Proposal</td><td>'+mentorSubmitDate+'</td></tr>';
	    		break; 
	  	  	case 'IREP':
				var mentorSubmitDate = full.mentorSubmitDate;
				if (mentorSubmitDate != null) {
					mentorSubmitDate = moment.tz(full.mentorSubmitDate, "America/Denver").format('MM/DD/YYYY')
				}else{
					mentorSubmitDate = ''; 
				}
	  	    	htmlData += '<tr><td>Student Application</td><td>'+applicantSubmitDate+'</td></tr>'; 
	    		htmlData += '<tr><td>Unofficial transcript</td><td>'+transcriptDate+'</td></tr>';
	    		htmlData += '<tr><td>Project Abstract</td><td>'+mentorSubmitDate+'</td></tr>';
	    		break; 
	  	   	} 
		 
			htmlData += '</tbody></table>';
  		   $("#progresstable").html(htmlData);
			 $('#progressModal').modal('show');
        
         });
     });
 
}); 

//Updates "Select all" control in a data table
function updateDataTableSelectAllCtrl(table){
   var $table             = table.table().node();
   var $chkbox_all        = $('tbody input[type="checkbox"]', $table);
   var $chkbox_checked    = $('tbody input[type="checkbox"]:checked', $table);
   var chkbox_select_all  = $('thead input[name="select_all"]', $table).get(0);

   if($chkbox_checked.length === 0){
      chkbox_select_all.checked = false;
      if('indeterminate' in chkbox_select_all){
         chkbox_select_all.indeterminate = false;
      }

   } else if ($chkbox_checked.length === $chkbox_all.length){
      chkbox_select_all.checked = true;
      if('indeterminate' in chkbox_select_all){
         chkbox_select_all.indeterminate = false;
      }

   } else {
      chkbox_select_all.checked = true;
      if('indeterminate' in chkbox_select_all){
         chkbox_select_all.indeterminate = true;
      }
   }
}

function exportApplicationExcel(){
	var table = $('#applications').dataTable();
	var matches = [];
 	var checkedcollection = table.$(".checkbox_input:checked", { "page": "all" });
 	if(checkedcollection.length < 1){
		alert("Please select application!"); 
	}else{
		checkedcollection.each(function (index, elem) {matches.push($(elem).val()); });
	    var checkedJsonString = JSON.stringify(matches);
		var data = 'year='+schoolYear+'&semester='+schoolSemester+'&program='+program+'&appIDList='+checkedJsonString;
		window.location.href = "manage-application-result/export-application-excel?"+data; 
	}
}

function exportApplicationPdf(){
	var table = $('#applications').dataTable();
	var matches = [];
 	var checkedcollection = table.$(".checkbox_input:checked", { "page": "all" });
 	if(checkedcollection.length < 1){
		alert("Please select application!"); 
	}else{
		checkedcollection.each(function (index, elem) {matches.push($(elem).val()); });
	    var checkedJsonString = JSON.stringify(matches); 
		var data = 'year='+schoolYear+'&semester='+schoolSemester+'&program='+program+'&appIDList='+checkedJsonString;
		window.location.href = "manage-application-result/export-application-zip?"+data; 
	}
}

function downloadApplicationTranscript(){
	var table = $('#applications').dataTable();
	var matches = [];
 	var checkedcollection = table.$(".checkbox_input:checked", { "page": "all" });
 	if(checkedcollection.length != 1){
		alert("Please select one application!"); 
	}else{
		checkedcollection.each(function (index, elem) {matches.push($(elem).val()); });
		var data = 'program='+program+'&appID='+matches[0]; 
	    window.location.href = "manage-application-result/download-application-transcript?"+data; 
	}
}

function downloadApplicationReference(){
	var table = $('#applications').dataTable();
	var matches = [];
 	var checkedcollection = table.$(".checkbox_input:checked", { "page": "all" });
 	if(checkedcollection.length != 1){
		alert("Please select one application!"); 
	}else{
		checkedcollection.each(function (index, elem) {matches.push($(elem).val()); });
		var data = 'program='+program+'&appID='+matches[0]; 
	    window.location.href = "manage-application-result/download-application-reference?"+data; 
	}
}

function downloadApplicationMedicalForm(){
	var table = $('#applications').dataTable();
	var matches = [];
 	var checkedcollection = table.$(".checkbox_input:checked", { "page": "all" });
 	if(checkedcollection.length != 1){
		alert("Please select one application!"); 
	}else{
		checkedcollection.each(function (index, elem) {matches.push($(elem).val()); });
		var data = 'program='+program+'&appID='+matches[0]; 
	    window.location.href = "manage-application-result/download-application-medical-form?"+data; 
	}
}


function deleteApplication(){
	var table = $('#applications').dataTable();
	var matches = [];
	var checkedcollection = table.$(".checkbox_input:checked", { "page": "all" });
	if(checkedcollection.length != 1){
		alert("Select one application only!"); 
	}else{
		var applicationID = $(checkedcollection[0]).val(); 
		if (confirm('Are you sure you want to delete this application?\n'+applicationID)) {
			var requestData = {
		    	'year': schoolYear,
		    	'semester': schoolSemester,
		    	'program': program,
		    	'applicationID': applicationID,
			};
			requestData[_csrf_param_name] = _csrf_token; 
			$.ajax({
				type: 'POST',
				url: "manage-application-result/delete-application", 
				data: requestData, 
				dataType: 'json',
				success: function(response){
					if(response.status=='ok'){
						alert("Application deleted:\n"+applicationID); 
						location.reload(); 
					}else{
						alert("Error occurrred during deletion:\n"+applicationID); 
					}
				},
				error: function(e){
					console.log("error"); 
				}
			}); 
		}
	}
}

function updateDecision(decision){
   var table = $('#applications').dataTable();
   var matches = [];
   var checkedcollection = table.$(".checkbox_input:checked", { "page": "all" });
   if(schoolTarget === 'ALL'){
	   alert("Please choose the Institution Applying to \nat the Manage Applications page!"); 
   }else if(checkedcollection.length < 1){
	   alert("Please select application!");
   }else{
	   checkedcollection.each(function (index, elem) {matches.push($(elem).val()); });
	   var checkedJsonString = JSON.stringify(matches);
	   if (confirm(decision +' the following applications?\n(student will not be notified at this time)\n'+ JSON.parse(checkedJsonString))) {
		   var requestData = {
	    	'year': schoolYear,
	    	'semester': schoolSemester,
	    	'program': program,
	    	'schoolTarget': schoolTarget, 
	    	'appIDList': checkedJsonString,
			};
		   requestData[_csrf_param_name] = _csrf_token; 
		   $.ajax({
			  type: 'POST',
			  url: "manage-application-result/update-decision/" + decision,
			  data: requestData, 
			  dataType: 'json',
			  success : function(response) {
				if(response.status=='ok'){
					alert("Decision updated:\n"+JSON.parse(checkedJsonString)); 
				}
				location.reload(); 
				},
			error: function(xhr, status, error) {
				console.log(xhr.responseText + "."+error);
			}
			});
	   }
   }
}
	 			 
function emailDecision(){
	var table = $('#applications').dataTable();
   var matches = [];
   var checkedcollection = table.$(".checkbox_input:checked", { "page": "all" });
   if(checkedcollection.length < 1){
		alert("Please select application!"); 
	}else{
		checkedcollection.each(function (index, elem) {matches.push($(elem).val()); });
		var checkedJsonString = JSON.stringify(matches);
		var requestData = {
    	'year': schoolYear,
    	'semester': schoolSemester,
    	'program': program,
    	'appIDList': checkedJsonString,
		};   
		
		 $.ajax({
	      type: 'GET',
	      url: "manage-application-result/email-decision-resume",
	      data: requestData,
	      dataType: 'json',
	       success : function(response) {
	    	   if(response.status=='ok'){
	    		   $("#emailDecisionModal #decisionIDList").val(response.ids); 
	    		   var htmlData = ''; 
	    		   htmlData += '<table class="table table-condensed table-hover">'; 
	    		   htmlData += '<thead><tr><th>Decision</th><th>Recipient List</th></tr></thead>';
	    		   htmlData += '<tbody>'; 
	    		   htmlData += '<tr><td>Admit</td><td>'+escapeHTML(response.admits)+'</td></tr>'; 
	    		   htmlData += '<tr><td>Deny</td><td>'+escapeHTML(response.denys)+'</td></tr>';
	    		   htmlData += '<tr><td>N/A</td><td>'+escapeHTML(response.nas)+'</td></tr>';
					htmlData += '</tbody></table>';
	    		   $("#decisiontable").html(htmlData);
	    		   $('#emailDecisionModal').modal('show');
	    	  }else{
	    		 console.log(response.status); 
	    	  }
			},
	       error: function(e){console.log(e);}
		});
	}
}					    
 		      
function sendApplications(){
	var table = $('#applications').dataTable();
   var matches = [];
   var checkedcollection = table.$(".checkbox_input:checked", { "page": "all" });
   if(checkedcollection.length < 1){
		alert("Please select application!"); 
	}else{
		checkedcollection.each(function (index, elem) {matches.push($(elem).val()); });
		var checkedJsonString = JSON.stringify(matches);
		$('#userForm').find('[name="applicationlist"]').val(JSON.parse(checkedJsonString));
       url = "manage-application-result/download-review-zip?" ; 
  		 var data = $("#userForm").serialize();
  		 url += data
       document.getElementById("downloadzip").href=url; 
       $('#myModalHorizontal').modal('show'); 
	}
}
					
function escapeHTML(s) { 
    return s.replace(/&/g, '&amp;')
            .replace(/\"/g, '&quot;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;');
}