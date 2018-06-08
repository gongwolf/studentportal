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
	$("#tabs").tabs({
		activate : function(event, ui) {
			var active = $('#tabs').tabs('option', 'active');
			if (active == 2) {
				
				$.ajax({
					   type: 'GET',
					   url: '/portal/admin/manage-ampconf/view-data',
					   dataType: 'json',
					   success : function(response) {
						   console.log(response.value); 
					   	  if(response.status=='ok'){ 
							$('#json').val(response.value); 
							var maxrows = 80;
							var textarea = document.getElementById("json");
							var txt = textarea.value;
							var cols = textarea.cols;
		
							var arraytxt = txt.split('\n');
							var rows = arraytxt.length;
		
							for (i = 0; i < arraytxt.length; i++) {
								rows += parseInt(arraytxt[i].length / cols);
							}
		
							if (rows > maxrows) {
								textarea.rows = maxrows;
							} else {
								textarea.rows = rows;
							}
					   	  } 
						},
					   error: function(ts){
						   console.log(ts.responseText); 
					   }
				});  
			}
		}
	}

	);
});

$(function() {
	$("#start_date").datepicker({
		defaultDate : "+1w",
		changeMonth : true,
		changeYear : true,
		numberOfMonths : 1,
		dateFormat : 'yy-mm-dd',
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
				dateFormat : 'yy-mm-dd',
				onClose : function(selectedDate) {
					$("#start_date").datepicker("option", "maxDate",
							selectedDate);
				}
			});
});
