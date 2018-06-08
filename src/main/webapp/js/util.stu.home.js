
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

$( document ).ready(function() {
    $(".pstatement-container").css('height', '450px');
	$('#pstatement').html(pstatement.replace(/\n|\r\n|\r/g, '<br/>'));
    $(".pstatement-container").css('overflow', 'hidden');

});

function previewPersonalStatement(){   
	$('textarea[name="pstatementarea"]').html(pstatement);
	$("#modalPersonalStatement").dialog({width: 600,height:600});   
}
  
$('#formPersonalStatement').submit(function(e){
	e.preventDefault();
	
	var text = $('textarea[name="pstatementarea"]').val();  
	if(text.trim().length === 0){
		return; 
	} 
    var requestData = {
    		'pstatement': text
	};
	requestData[_csrf_param_name] = _csrf_token; 

	$.ajax({
       type: 'POST',
       url: "/portal/profile/personal-statement",
       dataType: 'json',
       data: requestData, 
       success: function(response){
    	   		if(response.status=='ok'){ 
    	   			$('#modalPersonalStatement').dialog('close')
    	   			window.location.href = '/portal/home';
    	   		} 
       },
       error: function(ts){
      	 console.log(ts.responseText);
      	}
    });  
 
});

function revealHiddenOverflow(t) {
    d = $('.pstatement-container');
    icon = $(t).find("i");
    if( d.css('overflow') === "hidden" ) {
        d.css('overflow', 'visible');
        d.css('height', 'auto');
        icon.toggleClass("fa-angle-double-down fa-angle-double-up");
    } else {
        d.css('overflow', 'hidden');
        d.css('height', '450px');
        icon.toggleClass("fa-angle-double-up fa-angle-double-down");
        $("html, body").animate({ scrollTop: 0 }, 200);
    }
}