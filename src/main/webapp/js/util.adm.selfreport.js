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
		$("#start_date").datepicker({
			defaultDate : "+1w",
			changeMonth : true,
			changeYear : true,
			numberOfMonths : 1,
			dateFormat : 'mm/dd/yy',
			onClose : function(selectedDate) {
				$("#end_date").datepicker("option", "minDate", selectedDate);
			}
		});
		$("#end_date").datepicker(
				{
					defaultDate : "+1w",
					changeMonth : true,
					changeYear : true,
					numberOfMonths : 1,
					dateFormat : 'mm/dd/yy',
					onClose : function(selectedDate) {
						$("#start_date").datepicker("option", "maxDate",
								selectedDate);
					}
				});
	});

$(document).ready(function(){
	moment.tz.add("America/Denver|MST MDT MWT MPT|70 60 60 60|01010101023010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010|-261r0 1nX0 11B0 1nX0 11B0 1qL0 WN0 mn0 Ord0 8x20 ix0 LCN0 1fz0 1a10 1fz0 1cN0 1cL0 1cN0 1cL0 1cN0 1cL0 1cN0 1cL0 1cN0 1fz0 1cN0 1cL0 1cN0 1cL0 s10 1Vz0 LB0 1BX0 1cN0 1fz0 1a10 1fz0 1cN0 1cL0 1cN0 1cL0 1cN0 1cL0 1cN0 1cL0 1cN0 1fz0 1a10 1fz0 1cN0 1cL0 1cN0 1cL0 1cN0 1cL0 14p0 1lb0 14p0 1nX0 11B0 1nX0 11B0 1nX0 14p0 1lb0 14p0 1lb0 14p0 1nX0 11B0 1nX0 11B0 1nX0 14p0 1lb0 14p0 1lb0 14p0 1lb0 14p0 1nX0 11B0 1nX0 11B0 1nX0 14p0 1lb0 14p0 1lb0 14p0 1nX0 11B0 1nX0 11B0 1nX0 Rd0 1zb0 Op0 1zb0 Op0 1zb0 Rd0 1zb0 Op0 1zb0 Op0 1zb0 Op0 1zb0 Op0 1zb0 Op0 1zb0 Rd0 1zb0 Op0 1zb0 Op0 1zb0 Op0 1zb0 Op0 1zb0 Rd0 1zb0 Op0 1zb0 Op0 1zb0 Op0 1zb0 Op0 1zb0 Op0 1zb0 Rd0 1zb0 Op0 1zb0 Op0 1zb0 Op0 1zb0 Op0 1zb0 Rd0 1zb0 Op0 1zb0 Op0 1zb0 Op0 1zb0 Op0 1zb0 Op0 1zb0|26e5");
	 
	var table = $('#windows').DataTable( {
   	"aaData": data, 
   	'columnDefs': [{
       'targets': 0,
       'searchable':false,
       'orderable':false,
       'className': 'dt-body-center',
       'render': function (data, type, full, meta){
    	   return '<input type="checkbox" name="id[]" value="'+full.windowID+'">';
       }
     },
     {
       'targets': 1,
       'render': function (data, type, full, meta){
       	return '<span>'+full.semester+'</span>';
       }
     },
     {
       'targets': 2,
       'render': function (data, type, full, meta){
       	return moment.tz(full.startDate, "America/Denver").format('YYYY-MM-DD');
       }
      },
      {
        'targets': 3,
        'render': function (data, type, full, meta){
        	return moment.tz(full.endDate, "America/Denver").format('YYYY-MM-DD');
        }
      },
      {
         'targets': 4,
         'render': function (data, type, full, meta){
			return '<a href="manage-selfreport/edit?windowid='+full.windowID+'"><button class="btn btn-primary">Edit</button></a>';
         }
      }
      ],
         "aoColumns": [
           {"mData":""},
           {"mData":""}, 
           {"mData":""},
           {"mData":""},
           {"mData":""}
         ],
         "paging":true,
         "pageLength":10,
         "ordering":true,
         "order":[1,"asc"]
    });
}); 