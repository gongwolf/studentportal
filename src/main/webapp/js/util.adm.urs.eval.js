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

$(function() {
	 $(".evalDeadline").datepicker({
			defaultDate : "+1w",
			changeMonth : true,
			changeYear : true,
			numberOfMonths : 1,
			dateFormat : 'mm/dd/yy',
	});
	 $(".evalDeadlineStu").datepicker({
			defaultDate : "+1w",
			changeMonth : true,
			changeYear : true,
			numberOfMonths : 1,
			dateFormat : 'mm/dd/yy',
	});
}); 

$(document).ready(function(){
	moment.tz.add("America/Denver|MST MDT MWT MPT|70 60 60 60|01010101023010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010|-261r0 1nX0 11B0 1nX0 11B0 1qL0 WN0 mn0 Ord0 8x20 ix0 LCN0 1fz0 1a10 1fz0 1cN0 1cL0 1cN0 1cL0 1cN0 1cL0 1cN0 1cL0 1cN0 1fz0 1cN0 1cL0 1cN0 1cL0 s10 1Vz0 LB0 1BX0 1cN0 1fz0 1a10 1fz0 1cN0 1cL0 1cN0 1cL0 1cN0 1cL0 1cN0 1cL0 1cN0 1fz0 1a10 1fz0 1cN0 1cL0 1cN0 1cL0 1cN0 1cL0 14p0 1lb0 14p0 1nX0 11B0 1nX0 11B0 1nX0 14p0 1lb0 14p0 1lb0 14p0 1nX0 11B0 1nX0 11B0 1nX0 14p0 1lb0 14p0 1lb0 14p0 1lb0 14p0 1nX0 11B0 1nX0 11B0 1nX0 14p0 1lb0 14p0 1lb0 14p0 1nX0 11B0 1nX0 11B0 1nX0 Rd0 1zb0 Op0 1zb0 Op0 1zb0 Rd0 1zb0 Op0 1zb0 Op0 1zb0 Op0 1zb0 Op0 1zb0 Op0 1zb0 Rd0 1zb0 Op0 1zb0 Op0 1zb0 Op0 1zb0 Op0 1zb0 Rd0 1zb0 Op0 1zb0 Op0 1zb0 Op0 1zb0 Op0 1zb0 Op0 1zb0 Rd0 1zb0 Op0 1zb0 Op0 1zb0 Op0 1zb0 Op0 1zb0 Rd0 1zb0 Op0 1zb0 Op0 1zb0 Op0 1zb0 Op0 1zb0 Op0 1zb0|26e5");

	var table = $('#evaluationTable').DataTable( {
   	"aaData": evaluationList, 
   	'columnDefs': [{
       'targets': 0,
       'searchable':false,
       'orderable':false,
       'className': 'dt-body-center',
       'render': function (data, type, full, meta){
    	   return '<input class = "checkbox_input" type="checkbox" name="id[]" value="'+full+'">';
       }
     },
     {
       'targets': 1,
       'render': function (data, type, full, meta){
       	return '<span>'+full.evalTerm+' '+full.evalYear+'</span>';
       }
     },
     {
       'targets': 3,
       'render': function (data, type, full, meta){
       	return moment.tz(full.evalDeadline, "America/Denver").format('YYYY-MM-DD');
       }
      },
      {
        'targets': 4,
        'render': function (data, type, full, meta){
        	if(full.notifiedDate){
        		return moment.tz(full.notifiedDate, "America/Denver").format('YYYY-MM-DD');
        	}else{
        		return ""; 
        	}
        }
      },
      {
         'targets': 5,
         'render': function (data, type, full, meta){
         	if(full.submitDate){
         		return moment.tz(full.submitDate, "America/Denver").format('YYYY-MM-DD');
         	}else{
         		return ""; 
         	}
         }
      }
      ],
         "aoColumns": [
           {"mData":""},
           {"mData":""}, 
           {"mData": "evalPoint"},
           {"mData": ""},
           {"mData": ""},
           {"mData": ""} 
         ],
         "paging":true,
         "pageLength":10,
         "ordering":true,
         "order":[1,"asc"]
    });
	
	$('#evaluationTable tbody').on('click', 'input[type="checkbox"]',
		function(e) {
			var $row = $(this).closest('tr');
			var data = table.row($row).data();
			var index = $.inArray(data, rows_selected);
			 
			if (this.checked && index === -1) {
				rows_selected.push(data);
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
			$('#evaluationTable tbody input[type="checkbox"]:not(:checked)').trigger('click');
		} else {
			$('#evaluationTable tbody input[type="checkbox"]:checked').trigger('click');
		}
		e.stopPropagation();
	});
	
	table.on('draw', function() {
	updateDataTableSelectAllCtrl(table);
	});
	
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
}); 
					      
function notifyMentor(){
 	if(rows_selected.length != 1){
		alert("Please select one project!"); 
	}else{
		if (confirm('Continue?\nClick OK to send an email to mentor.')){
			 var requestData = {
	    	'evalTerm': rows_selected[0].evalTerm, 
	    	'evalYear': rows_selected[0].evalYear,
	    	'evalPoint': rows_selected[0].evalPoint
			};
		   requestData[_csrf_param_name] = _csrf_token; 
		  
		   $.ajax({
			  type: 'POST',
			  url: "/portal/admin/urs-project/detail/email-evaluation/"+schoolYear+"/"+schoolSemester+"/"+rows_selected[0].applicationID,
			  data: requestData, 
			  dataType: 'json',
			  success : function(response) {
				  alert(response.status); 
				location.reload(); 
			},
			error: function(xhr, status, error) {
				console.log(xhr.responseText + "."+error);
			}
			}); 
		 }
	}
}

function updateDeadline(){
 	if(rows_selected.length != 1){
		alert("Please select one project!"); 
	}else{
		$("#updateDeadlineModal #modalEvalSemester").text(rows_selected[0].evalTerm+' '+rows_selected[0].evalYear);
		$("#updateDeadlineModal #modalEvalPoint").text(rows_selected[0].evalPoint);
		
		$("#updateDeadlineModal #modalEvalDeadline")
		.val(moment.tz(rows_selected[0].evalDeadline, "America/Denver").format('MM/DD/YYYY'));
		$("#updateDeadlineModal #footerEvalSemester").val(rows_selected[0].evalTerm); 
		$("#updateDeadlineModal #footerEvalYear").val(rows_selected[0].evalYear);
		$("#updateDeadlineModal #footerEvalPoint").val(rows_selected[0].evalPoint);
		
		$('#updateDeadlineModal').modal('show');
	}
}
 
function exportEvaluationPdf(){
	var matches = [];
 	if(rows_selected.length != 1){
		alert("Please select one application!"); 
	}else{
		matches.push(rows_selected[0].applicationID); 
		var data = 'evalSemester='+rows_selected[0].evalTerm+'&evalYear='+rows_selected[0].evalYear+'&applicationID='+rows_selected[0].applicationID; 
		window.location.href = "/portal/admin/urs-project/export-evaluation-pdf/"+rows_selected[0].evalPoint+"?"+data; 
	}
}

$(document).ready(function(){
	var tableStu = $('#evaluationTableStu').DataTable( {
   	"aaData": evaluationListStu, 
   	'columnDefs': [{
       'targets': 0,
       'searchable':false,
       'orderable':false,
       'className': 'dt-body-center',
       'render': function (data, type, full, meta){
    	   return '<input class = "checkbox_input" type="checkbox" name="id[]" value="'+full+'">';
       }
     },
     {
       'targets': 1,
       'render': function (data, type, full, meta){
       	return '<span>'+full.evalTerm+' '+full.evalYear+'</span>';
       }
     },
     {
       'targets': 3,
       'render': function (data, type, full, meta){
       	return moment.tz(full.evalDeadline, "America/Denver").format('YYYY-MM-DD');
       }
      },
      {
        'targets': 4,
        'render': function (data, type, full, meta){
        	if(full.notifiedDate){
        		return moment.tz(full.notifiedDate, "America/Denver").format('YYYY-MM-DD');
        	}else{
        		return ""; 
        	}
        }
      },
      {
         'targets': 5,
         'render': function (data, type, full, meta){
         	if(full.submitDate){
         		return moment.tz(full.submitDate, "America/Denver").format('YYYY-MM-DD');
         	}else{
         		return ""; 
         	}
         }
      }
      ],
         "aoColumns": [
           {"mData":""},
           {"mData":""}, 
           {"mData": "evalPoint"},
           {"mData": ""},
           {"mData": ""},
           {"mData": ""} 
         ],
         "paging":true,
         "pageLength":10,
         "ordering":true,
         "order":[1,"asc"]
    });
	
	$('#evaluationTableStu tbody').on('click', 'input[type="checkbox"]',
		function(e) {
			var $row = $(this).closest('tr');
			var data = tableStu.row($row).data();
			var index = $.inArray(data, rows_selected_stu);
			 
			if (this.checked && index === -1) {
				rows_selected_stu.push(data);
			} else if (!this.checked && index !== -1) {
				rows_selected_stu.splice(index, 1);
			}
			 
			if (this.checked) {
				$row.addClass('selected');
			} else {
				$row.removeClass('selected');
			}
			updateDataTableSelectAllCtrl(tableStu);
			e.stopPropagation();
	});
	
	$('thead input[name="select_all"]', tableStu.table().container())
	.on('click', function(e) {
		if (this.checked) {
			$('#evaluationTableStu tbody input[type="checkbox"]:not(:checked)').trigger('click');
		} else {
			$('#evaluationTableStu tbody input[type="checkbox"]:checked').trigger('click');
		}
		e.stopPropagation();
	});
	
	tableStu.on('draw', function() {
	updateDataTableSelectAllCtrl(tableStu);
	});
	 
}); 

function updateDeadlineStu(){
 	if(rows_selected_stu.length != 1){
		alert("Please select one project!"); 
	}else{
		$("#updateDeadlineModalStu #modalEvalSemester").text(rows_selected_stu[0].evalTerm+' '+rows_selected_stu[0].evalYear);
		$("#updateDeadlineModalStu #modalEvalPoint").text(rows_selected_stu[0].evalPoint);
		
		$("#updateDeadlineModalStu #modalEvalDeadlineStu")
		.val(moment.tz(rows_selected_stu[0].evalDeadline, "America/Denver").format('MM/DD/YYYY'));
		$("#updateDeadlineModalStu #footerEvalSemester").val(rows_selected_stu[0].evalTerm); 
		$("#updateDeadlineModalStu #footerEvalYear").val(rows_selected_stu[0].evalYear);
		$("#updateDeadlineModalStu #footerEvalPoint").val(rows_selected_stu[0].evalPoint);
		
		$('#updateDeadlineModalStu').modal('show');
	}
}

function notifyStudent(){
 	if(rows_selected_stu.length != 1){
		alert("Please select one project!"); 
	}else{
		if (confirm('Continue?\nClick OK will send an email to student.')){
			 var requestData = {
	    	'evalTerm': rows_selected_stu[0].evalTerm, 
	    	'evalYear': rows_selected_stu[0].evalYear,
	    	'evalPoint': rows_selected_stu[0].evalPoint
			};
		   requestData[_csrf_param_name] = _csrf_token; 
		  
		   $.ajax({
			  type: 'POST',
			  url: "/portal/admin/urs-project/detail/email-evaluation-stu/"+schoolYear+"/"+schoolSemester+"/"+rows_selected_stu[0].applicationID,
			  data: requestData, 
			  dataType: 'json',
			  success : function(response) {
				  alert(response.status); 
				location.reload(); 
			},
			error: function(xhr, status, error) {
				console.log(xhr.responseText + "."+error);
			}
			}); 
		 }
	}
}

function exportEvaluationPdfStu(){
	var matches = [];
 	if(rows_selected_stu.length != 1){
		alert("Please select one application!"); 
	}else{
		matches.push(rows_selected_stu[0].applicationID); 
		var data = 'evalSemester='+rows_selected_stu[0].evalTerm+'&evalYear='+rows_selected_stu[0].evalYear+'&applicationID='+rows_selected_stu[0].applicationID; 
		window.location.href = "/portal/admin/urs-project/export-evaluation-pdf-stu/"+rows_selected_stu[0].evalPoint+"?"+data; 
	}
}