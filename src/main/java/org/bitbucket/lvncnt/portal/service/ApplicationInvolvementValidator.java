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

import org.bitbucket.lvncnt.portal.model.InvolvementBean;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Service("applicationInvolvementValidator")
public class ApplicationInvolvementValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return InvolvementBean.class.isAssignableFrom(clazz);
	}
	
	@Override
	public void validate(Object target, Errors errors) {
		
		InvolvementBean bean = (InvolvementBean) target; 
		String program = bean.getProgramNameAbbr();
		
		switch (program) {
		case ProgramCode.CCCONF:
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "ampScholarship", 
					"NotBlank.involvementBean.ampScholarship", "Required");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "otherScholarship", 
					"NotBlank.involvementBean.otherScholarship", "Required");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "isCurrentEmploy", 
					"NotBlank.involvementBean.isCurrentEmploy", "Required");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "everInResearch", 
					"NotBlank.involvementBean.everInResearch", "Required");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "everAttendConference", 
					"NotBlank.involvementBean.everAttendConference", "Required");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "programEverIn", 
					"NotBlank.involvementBean.programEverIn", "Required");
			break;
			
		case ProgramCode.MESA:
		case ProgramCode.TRANS:
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "ampScholarship", 
					"NotBlank.involvementBean.ampScholarship", "Required");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "otherScholarship", 
					"NotBlank.involvementBean.otherScholarship", "Required");
			break; 
			
		case ProgramCode.SCCORE:
		case ProgramCode.URS:
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "programEverIn", 
					"NotBlank.involvementBean.programEverIn", "Required");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "ampScholarship", 
					"NotBlank.involvementBean.ampScholarship", "Required");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "otherScholarship", 
					"NotBlank.involvementBean.otherScholarship", "Required");
			break; 

		default:
			break;
		}
		 
	}
}
