
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

$('#surveyform').submit(function(e){
	e.preventDefault();
	 
	if(!validate()){
		alert("Missing required fields!"); 
		return; 
	}
    
	if (confirm('Are you sure you want to submit the evaluation?\n')){
		var surveyform = $('#surveyform').serializeArray();
		var surveyformmData = {};
	    $.each(surveyform, function(i, v) { surveyformmData[v.name] = v.value; }); 
	 
	    var requestData = {
			'json': JSON.stringify(surveyformmData),   
			'applicationID': applicationID,  
		    'evalSemester': evalSemester,  
		    'evalYear': evalYear
		};
		requestData[_csrf_param_name] = _csrf_token; 
    
		$.ajax({
	       type: 'POST',
	       url: "/portal/mentor/evaluation/"+evalPoint,
	       dataType: 'json',
	       data: requestData, 
	       success: function(data){
	    	   $('#resultModal').modal('show');
	       },
	       error: function(ts){
	      	 console.log(ts.responseText);
	      	}
	    });    
	}
 
}); 

function validate(){
	var isChecked = false; 
	var radioNames = ["rating-lab-1","rating-lab-2","rating-lab-3","rating-organization-1","rating-organization-2","rating-organization-3","rating-organization-4","rating-independent-1","rating-independent-2","rating-writing-1","rating-writing-2"]; 
	for(var i in radioNames){
		if(!validateRadioBox(radioNames[i])){
			return false; 
		}
	}
	 
	var textareaNames = ["quest-techniques","quest-publications","quest-conferences","quest-assessment"]; 
	for(var i in textareaNames){
		var val = $('textarea[name="'+textareaNames[i]+'"]').val(); 
		if(val.trim().length === 0){
			return false; 
		}
	}
	 
    return true; 
}

function validateRadioBox(name){
	var radio = document.getElementsByName(name);
    var len = radio.length;
    var isChecked = false;
    for(i=0;i<len;i++) {
    	if(radio[i].checked) {
        	isChecked = true;
         	break;    
        } 
    }
    return isChecked; 
}

$("button#evaluationhome").click(function(event){
	event.preventDefault();
	window.location.href = '/portal/mentor/evaluation';
}); 