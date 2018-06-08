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
package org.bitbucket.lvncnt.portal.service;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import org.bitbucket.lvncnt.portal.model.ContactBean;

@Service("profileContactValidator")
public class ProfileContactValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return ContactBean.class.isAssignableFrom(clazz);
	}
	
	@Override
	public void validate(Object target, Errors errors) {
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "phoneNum1", 
				"NotBlank.contactBean.phoneNum1", "Please provide phone number.");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "phoneType1", 
				"NotBlank.contactBean.phoneType1", "Please select phone type.");
		
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "permanentAddressLine1", 
				"NotBlank.contactBean.permanentAddressLine1", "Please provide address.");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "permanentAddressCity", 
				"NotBlank.contactBean.permanentAddressCity", "Please provide city.");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "permanentAddressCounty", 
				"NotBlank.contactBean.permanentAddressCounty", "Please provide county.");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "permanentAddressState", 
				"NotBlank.contactBean.permanentAddressState", "Please select state.");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "permanentAddressZip", 
				"NotBlank.contactBean.permanentAddressZip", "Please provide zip.");
 
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "currentAddressLine1", 
				"NotBlank.contactBean.currentAddressLine1", "Please provide address.");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "currentAddressCity", 
				"NotBlank.contactBean.currentAddressCity", "Please provide city.");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "currentAddressCounty", 
				"NotBlank.contactBean.currentAddressCounty", "Please provide county.");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "currentAddressState", 
				"NotBlank.contactBean.currentAddressState", "Please select state.");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "currentAddressZip", 
				"NotBlank.contactBean.currentAddressZip", "Please provide zip.");

		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "ecFirstName", 
				"NotBlank.contactBean.ecFirstName", "Please provide first name.");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "ecLastName", 
				"NotBlank.contactBean.ecLastName", "Please provide last name.");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "ecRelationship", 
				"NotBlank.contactBean.ecRelationship", "Please provide relationship.");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "ecPhoneNum1", 
				"NotBlank.contactBean.ecPhoneNum1", "Please provide phone number.");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "ecPhoneType1", 
				"NotBlank.contactBean.ecPhoneType1", "Please select phone type.");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "ecAddressLine1", 
				"NotBlank.contactBean.ecAddressLine1", "Please provide address.");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "ecAddressCity", 
				"NotBlank.contactBean.ecAddressCity", "Please provide city.");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "ecAddressCounty", 
				"NotBlank.contactBean.ecAddressCounty", "Please provide county.");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "ecAddressState", 
				"NotBlank.contactBean.ecAddressState", "Please select state.");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "ecAddressZip", 
				"NotBlank.contactBean.ecAddressZip", "Please provide zip.");
		
	}
}
