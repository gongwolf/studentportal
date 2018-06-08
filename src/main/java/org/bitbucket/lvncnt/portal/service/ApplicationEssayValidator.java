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

import org.bitbucket.lvncnt.portal.model.EssayBean;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Service("applicationEssayValidator")
public class ApplicationEssayValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return EssayBean.class.isAssignableFrom(clazz);
	}
	
	@Override
	public void validate(Object target, Errors errors) {
	 
		String program = ((EssayBean) target).getProgramNameAbbr();
		
		switch (program) {
		case ProgramCode.CCCONF:
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "essayCriticalEvent", 
					"NotBlank.essayBean.essayCriticalEvent", "Required");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "essayEducationalGoal", 
					"NotBlank.essayBean.essayEducationalGoal", "Required");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "essayProfesionalGoal", 
					"NotBlank.essayBean.essayProfesionalGoal", "Required");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "essayAmpGain", 
					"NotBlank.essayBean.essayAmpGain", "Required");
			break;
			
		case ProgramCode.MESA:
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "essayProfesionalGoal", 
					"NotBlank.essayBean.essayProfesionalGoal", "Required");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "essayCriticalEvent", 
					"NotBlank.essayBean.essayCriticalEvent", "Required");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "essayAcademicPathway", 
					"NotBlank.essayBean.essayAcademicPathway", "Required");
			break; 
			
		case ProgramCode.SCCORE:
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "sccoreSchoolAttendPref", 
					"NotBlank.essayBean.sccoreSchoolAttendPref", "Required");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "sccoreSchoolAttendAltn", 
					"NotBlank.essayBean.sccoreSchoolAttendAltn", "Required");
			
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "essayEducationalGoal", 
					"NotBlank.essayBean.essayEducationalGoal", "Required");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "essayFieldOfStudy", 
					"NotBlank.essayBean.essayFieldOfStudy", "Required");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "essayPreferredResearch", 
					"NotBlank.essayBean.essayPreferredResearch", "Required");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "essayStrengthBring", 
					"NotBlank.essayBean.essayStrengthBring", "Required");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "essayAmpGain", 
					"NotBlank.essayBean.essayAmpGain", "Required");
			break; 
			
		case ProgramCode.TRANS:
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "essayProfesionalGoal", 
					"NotBlank.essayBean.essayProfesionalGoal", "Required");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "essayCriticalEvent", 
					"NotBlank.essayBean.essayCriticalEvent", "Required");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "essayAmpGain", 
					"NotBlank.essayBean.essayAmpGain", "Required");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "essayFieldOfInterest", 
					"NotBlank.essayBean.essayFieldOfInterest", "Required");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "essayMentor", 
					"NotBlank.essayBean.essayMentor", "Required");
			break; 
			
		case ProgramCode.URS:
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "essayEducationalGoal", 
					"NotBlank.essayBean.essayEducationalGoal", "Required");
			break; 
			
		default:
			break;
		}
 
	}
}
