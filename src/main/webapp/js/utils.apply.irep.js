

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
 * Academic Info & Project Abstract  
 */
$(function() {
	 
	$("#academic_grad_date").datepicker({
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
	
	$("#research_date").datepicker({
		defaultDate : "+1w",
		changeMonth : true,
		changeYear : true,
		numberOfMonths : 1,
		dateFormat : 'mm/dd/yy',
	});
	
	$("#leave_date").datepicker({
		defaultDate : "+1w",
		changeMonth : true,
		changeYear : true,
		numberOfMonths : 1,
		dateFormat : 'mm/dd/yy',
	});
	
	$("#return_date").datepicker({
		defaultDate : "+1w",
		changeMonth : true,
		changeYear : true,
		numberOfMonths : 1,
		dateFormat : 'mm/dd/yy',
	});
	
});

/**
 * Budget Table 
 */
$(document).ready(function(){
	$('#alert').hide();
	var budgetTable = $('#budgetTable'), 
	footer = budgetTable.find('tfoot tr'), 
	dataRows = budgetTable.find('tbody tr'); 
	
	// init 
	dataRows.each(function () {
		var row = $(this); 
 
		var c1 = parseFloat(row.children('td:nth-child(2)').find("input").val()),
		c2 = parseFloat(row.children('td:nth-child(3)').find("input").val());
		
		if((!isNaN(c1)) && (!isNaN(c2))){
			row.children('td:last-child').find("input").val("$"+(c1-c2).toFixed(2));
		} 
	});
	
	for (var column = 1; column <= 2; column ++) {
		var total = 0; 
		
		dataRows.each(function () {
			var row = $(this), 
			val = parseFloat(row.children().eq(column).find("input").val());
			if(!(!isNaN(val) && isFinite(val))){
				val = 0; 
			} 
			total += val;
		});
		footer.children().eq(column).find("input").val("$"+total.toFixed(2));
		var c1 = parseFloat(footer.children().eq(1).find("input").val().replace(/\$/g, '')),
		c2 = parseFloat(footer.children().eq(2).find("input").val().replace(/\$/g, ''));
		footer.children().eq(3).find("input").val("$"+(c1-c2).toFixed(2));
	}
 
	$(".number").on('change',function() {
		var temp = parseFloat(this.value.replace(/,/g, ""));
		
		if(!(!isNaN(temp) && temp > 0 && isFinite(temp))){
			temp = 0; 
		} 
		
		this.value = temp.toFixed(2).toString(); 
		
		var cell = $(this).parent('td'),
		column = cell.index(),
		total = 0, show=false;
		
		dataRows.each(function () {
			var row = $(this), 
			val = parseFloat(row.children().eq(column).find("input").val());
			if(!(!isNaN(val) && isFinite(val))){
				val = 0; 
			} 
			total += val;
			
			var c1 = parseFloat(row.children('td:nth-child(2)').find("input").val()),
			c2 = parseFloat(row.children('td:nth-child(3)').find("input").val());
			if(!(!isNaN(c1) && isFinite(c1))){
				c1 = 0; 
			} 
			if(!(!isNaN(c2) && isFinite(c2))){
				c2 = 0; 
			}
			if(c1 < c2){
				show = true; 
			}else{
				if(!show)show =false; 
				row.children('td:last-child').find("input").val("$"+(c1-c2).toFixed(2));
			} 
		});
 
		if (total > 1000000) {
			show = true; 
		} else {
			if(!show)show = false; 
			footer.children().eq(column).find("input").val("$"+total.toFixed(2));
			
			var c1 = parseFloat(footer.children().eq(1).find("input").val().replace(/\$/g, '')),
			c2 = parseFloat(footer.children().eq(2).find("input").val().replace(/\$/g, ''));
			if(!(!isNaN(c1) && isFinite(c1))){
				c1 = 0; 
			} 
			if(!(!isNaN(c2) && isFinite(c2))){
				c2 = 0; 
			}
		 
			if(c1 < c2){
				show = true; 
			}else{
				if(!show)show =false; 
				footer.children().eq(3).find("input").val("$"+(c1-c2).toFixed(2));
			} 
			
		}
		 if(show){
			 $('#alert').show();
		 }else{
			 $('#alert').hide();
		 }
		
	 });
	
}); 
 
/**
 * Project Abstract - Program Involvement 
 */
$(document).ready(function() {
	$("#list_program").css("display", "none");
	$(".ever_fund_amp").click(function() {
		if ($('input[name=everFundAmp]:checked').val() == "1") {
			$("#list_program").slideDown("fast");
		} else {
			$("#list_program").slideUp("fast");
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
 