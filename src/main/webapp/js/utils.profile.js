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

/* phone num, zip mask */
$('.phone_num', '#contact_info').keydown(
		function(e) {
			var key = e.charCode || e.keyCode || 0;
			$phone = $(this);

			if (key !== 8 && key !== 9) {
				if ($phone.val().length === 4) {
					$phone.val($phone.val() + ')');
				}
				if ($phone.val().length === 5) {
					$phone.val($phone.val() + ' ');
				}
				if ($phone.val().length === 9) {
					$phone.val($phone.val() + '-');
				}
			}

			// Allow numeric (and tab, backspace, delete) keys only
			return (key == 8 || key == 9 || key == 46
					|| (key >= 48 && key <= 57) || (key >= 96 && key <= 105));
		})

.bind('focus click', function() {
	$phone = $(this);

	if ($phone.val().length === 0) {
		$phone.val('(');
	} else {
		var val = $phone.val();
		$phone.val('').val(val);
	}
})

.blur(function() {
	$phone = $(this);

	if ($phone.val() === '(') {
		$phone.val('');
	}
});

$('.zipcode', '#contact_info').keydown(
		function(e) {
			var key = e.charCode || e.keyCode || 0;
			$zip = $(this);

			if (key !== 8 && key !== 9) {
				if ($zip.val().length === 5) {
					$zip.val($zip.val() + '-');
				}
			}
			// Allow numeric (and tab, backspace, delete) keys only
			return (key == 8 || key == 9 || key == 46
					|| (key >= 48 && key <= 57) || (key >= 96 && key <= 105));
		})

// auto-complete current address based on checkbox
function fillCurrentAddress() {
	document.getElementById('current_address_line1').value = document
			.getElementById('permanent_address_line1').value;
	document.getElementById('current_address_line2').value = document
			.getElementById('permanent_address_line2').value;
	document.getElementById('current_address_city').value = document
			.getElementById('permanent_address_city').value;

	document.getElementById("current_address_state").selectedIndex = document
			.getElementById("permanent_address_state").selectedIndex;

	document.getElementById('current_address_zip').value = document
			.getElementById('permanent_address_zip').value;
	document.getElementById('current_address_county').value = document
			.getElementById('permanent_address_county').value;
}
function clearCurrentAddress() {
	document.getElementById('current_address_line1').value = "";
	document.getElementById('current_address_line2').value = "";
	document.getElementById('current_address_city').value = "";
	document.getElementById("current_address_state").selectedIndex = 0;
	document.getElementById('current_address_zip').value = "";
	document.getElementById('current_address_county').value = "";
}
 
/* show password rules */ 
$(document).ready(function() {
	$('input[type=password]').keyup(function() {
	}).focus(function() {
	    $('#pswd_rules').show();
	}).blur(function() {
	    $('#pswd_rules').hide();
	});
}); 
