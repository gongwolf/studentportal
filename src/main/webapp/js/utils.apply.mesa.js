

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

/**
 * Academic Info 
 */
$(function() {
	 
	$("#academic_grad_date").datepicker({
		defaultDate : "+1w",
		changeMonth : true,
		changeYear : true,
		numberOfMonths : 1,
		dateFormat : 'mm/dd/yy',
	});
	 
	$("#transfer_date").datepicker({
		defaultDate : "+1w",
		changeMonth : true,
		changeYear : true,
		numberOfMonths : 1,
		dateFormat : 'mm/dd/yy',
	});
	
	$("#signature_date").datepicker({
		defaultDate : "+1w",
		changeMonth : false,
		changeYear : false,
		numberOfMonths : 1,
		dateFormat : 'mm/dd/yy',
	});
	
});

/**
 * Program Involvement 
 */

$(document).ready(function() {
	$("#list_amp_school").css("display", "none");
	$(".choose_amp_scholarship").click(function() {
		if ($('input[name=ampScholarship]:checked').val() == "1") {
			$("#list_amp_school").slideDown("fast");
		} else {
			$("#list_amp_school").slideUp("fast");
		}
	});
 
	$("#list_other_scholarship").css("display", "none");
	$(".choose_other_scholarship").click(function() {
		if ($('input[name=otherScholarship]:checked').val() == "1") {
			$("#list_other_scholarship").slideDown("fast");
		} else {
			$('#listOtherScholarship').val("");
			$("#list_other_scholarship").slideUp("fast");
		}
	});
	 
});
	
/**
 * Short essay 
 */
function update_counter(counter, text_elem, max_len){
	var counterElem = document.getElementById(counter);
	var textElem = document.getElementById(text_elem);
	var words = textElem.value.match(/\S+/g).length;
     if (words > max_len) {
			while(words > max_len){
				var str = textElem.value;
				str = str.substring(0, str.length - 1);
				textElem.value=str;
				 words = textElem.value.match(/\S+/g).length;
			}
			var msg = "You have reached your maximum limit of words allowed";
			 alert(msg);
     }
     else {
         counterElem.innerHTML = max_len - words;
     }
}	
 