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
	var table = $('#example').DataTable( {
   	"aaData": data, 
   	'columnDefs': [{
           'targets': 0,
           'searchable':false,
           'orderable':false,
           'className': 'dt-body-center',
           'render': function (data, type, full, meta){
           	return '<input type="checkbox" name="id[]" value="'+full.applicationID+'">';
           }
        },
        {
            'targets': 2,
            'render': function (data, type, full, meta){
            	if(full.projectBean && full.projectBean.projectTitle){
            		return '<span>'+full.projectBean.projectTitle+'</span>';
            	}else{
            		return '<span>N/A</span>';
            	}
            }
      },
        {
            'targets': 3,
            'render': function (data, type, full, meta){
            	if(full.mentorBean && full.mentorBean.mentorFirstName){
            		return '<span>'+full.mentorBean.mentorFirstName+' '+full.mentorBean.mentorLastName+'</span>';
            	}else{
            		return '<span>N/A</span>';
            	}
            }
      },
      {
            'targets': 4,
            'render': function (data, type, full, meta){
            	if(full.firstName && full.lastName){
                    return '<span>'+full.firstName+' '+full.lastName+'</span>';
            	}else{
            		return '<span>N/A</span>';
            	}
            }
      },
       
      {
            'targets': 5,
            'render': function (data, type, full, meta){
            	return '<span>'+full.schoolSemester+' '+full.schoolYear+'</span>';
            }
      },
      {
            'targets': 6,
            'searchable':false,
            'orderable':false,
            'className': 'dt-body-center',
            'render': function (data, type, full, meta){
                return '<a href="detail/'+full.schoolSemester+'/'+full.schoolYear+'/'+full.applicationID+'/'+schoolTarget+'"><button class="btn btn-primary">Edit</button></a>';
            }
         }
     
        ],
         "aoColumns": [
           {"mData":""},
           {"mData":"applicationID"}, 
           { "mData": ""},
           { "mData": ""},
           { "mData": ""},
           { "mData": ""},
           { "mData": ""}
         ],
         "paging":true,
         "pageLength":10,
         "ordering":true,
         "order":[1,"asc"]
       });
								    
	    $('#example-select-all').on('click', function(){
			var rows = table.rows({ page: 'current' }).nodes();
	       $('input[type="checkbox"]', rows).prop('checked', this.checked);
	    });
	    
	    $('#example tbody').on('change', 'input[type="checkbox"]', function(){
	       if(!this.checked){
	          var el = $('#example-select-all').get(0);
	          if(el && el.checked && ('indeterminate' in el)){
	             el.indeterminate = true;
	          }
	       }
	    });
	}); 
					      
function getEvaluations(evalPoint){
	var table = $('#example').dataTable();
	var matches = [];
 	var checkedcollection = table.$("input:checkbox:checked", { "page": "all" });
 	if(checkedcollection.length < 1){
		alert("Please select an application!"); 
	}else{
		checkedcollection.each(function (index, elem) {matches.push($(elem).val()); });
	    var checkedJsonString = JSON.stringify(matches);
		var data = 'appIDList='+checkedJsonString; 
		window.location.href = "export-evaluation-excel/"+evalPoint+"?"+data; 
	}
}
 
function getEvaluationsStu(evalPoint){
	var table = $('#example').dataTable();
	var matches = [];
 	var checkedcollection = table.$("input:checkbox:checked", { "page": "all" });
 	if(checkedcollection.length < 1){
		alert("Please select an application!"); 
	}else{
		checkedcollection.each(function (index, elem) {matches.push($(elem).val()); });
	    var checkedJsonString = JSON.stringify(matches);
		var data = 'appIDList='+checkedJsonString; 
		window.location.href = "export-evaluation-excel-stu/"+evalPoint+"?"+data; 
	}
}