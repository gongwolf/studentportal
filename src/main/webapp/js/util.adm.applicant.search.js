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

$(document).ready(function(){
	var rows_selected = [];
	var table = $('#results').DataTable({
		"aaData": applicationList,
		'columnDefs': [{
	         'targets': 0,
	         'searchable':false,
	         'orderable':false,
	         'className': 'dt-body-center',
	         'render': function (data, type, full, meta){
	         	return meta.row + 1;
	         }
	     },
	     {
	         'targets': 1,
	         'searchable':false,
	         'orderable':false,
	         'className': 'dt-body-center',
	         'render': function (data, type, full, meta){
	         	return '<input class = "checkbox_input" type="checkbox" name="id[]" value="'+full.userID+'">';
	         }
	     },
	     {
            'targets': 7,
            'searchable':false,
            'orderable':false,
            'className': 'dt-body-center',
            'render': function (data, type, full, meta){
				return '<a href="download/student-agreement-and-disclosure-form?userID='+full.userID+'&name='+full.firstName+'-'+full.lastName+'"><button class="btn btn-primary">Download</button></a>';
            }
       	 }
	     ],
	     "aoColumns":[
			{"mData":""},
			{"mData":""},
			{"mData":"userID"},
			{ "mData": "firstName"},
			{ "mData": "lastName"},
			{ "mData": "email"},
			{ "mData": "birthDate",
				"mRender":function(data, type, full){
					return formatDate(new Date(full.birthDate));
				 }
			},
			{"mData":""}
	     ],
	     "paging":true,
	     "pageLength":10,
	     "ordering":true,
	     "order":[3,"asc"]
	}); 
	
	table.on( 'order.dt search.dt', function () {
        table.column(0, {search:'applied', order:'applied'}).nodes().each( function (cell, i) {
            cell.innerHTML = i+1;
        } );
    } ).draw();
	
	$('#results tbody').on('click', 'input[type="checkbox"]',
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
    
	$('#results').on(
			'click',
			'tbody td, thead th:first-child',
			function(e) {
				$(this).parent().find('input[type="checkbox"]')
						.trigger('click');
			});
	
	$('thead input[name="select_all"]',table.table().container()).on('click',
		function(e) {
			if (this.checked) {
				$('#results tbody input[type="checkbox"]:not(:checked)').trigger('click');
			} else {
				$('#results tbody input[type="checkbox"]:checked').trigger('click');
			}
			e.stopPropagation();
	});
	
	table.on('draw', function() {
		updateDataTableSelectAllCtrl(table);
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
     
function formatDate(date) {
    var d = new Date(date),
        month = '' + (d.getMonth() + 1),
        day = '' + d.getDate(),
        year = d.getFullYear();
    if (month.length < 2) month = '0' + month;
    if (day.length < 2) day = '0' + day;
    return [month, day, year].join('/');
}

function exportProfile(){
var table = $('#results').dataTable();
var matches = [];
var checkedcollection = table.$(".checkbox_input:checked", { "page": "all" });
checkedcollection.each(function (index, elem) {matches.push($(elem).val()); });
var checkedJsonString = JSON.stringify(matches);
var data = 'userIDList='+JSON.parse(checkedJsonString); 
 url = "export-profile"; 
 $.ajax({
   type: 'GET',
   url: url,
   data: data,
   dataType: 'json',
   success : function(response) {
   	  if(response.status=='ok'){
	    window.location.href = "export-profile/download?"+data; 
   	  }else{
   		  alert("No student selected!");
   	  }
	},
   error: function(e){alert(e);}
  }); 
}