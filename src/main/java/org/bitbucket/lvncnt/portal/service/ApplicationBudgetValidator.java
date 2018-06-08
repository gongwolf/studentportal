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

import org.bitbucket.lvncnt.portal.model.BudgetBean;

@Service("applicationBudgetValidator")
public class ApplicationBudgetValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return BudgetBean.class.isAssignableFrom(clazz);
	}
	
	@Override
	public void validate(Object target, Errors errors) {
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "totalDomesticTravel", 
				"NotBlank.budgetBean.totalDomesticTravel", "Required");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "totalRoundTrip", 
				"NotBlank.budgetBean.totalRoundTrip", "Required");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "totalVisa", 
				"NotBlank.budgetBean.totalVisa", "Required");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "totalPassport", 
				"NotBlank.budgetBean.totalPassport", "Required");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "totalImmunization", 
				"NotBlank.budgetBean.totalImmunization", "Required");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "totalHousing", 
				"NotBlank.budgetBean.totalHousing", "Required");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "totalCommunication", 
				"NotBlank.budgetBean.totalCommunication", "Required");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "totalMeal", 
				"NotBlank.budgetBean.totalMeal", "Required");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "totalMiscellaneous", 
				"NotBlank.budgetBean.totalMiscellaneous", "Required");
		 
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "currentDomesticTravel", 
				"NotBlank.budgetBean.currentDomesticTravel", "Required");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "currentRoundTrip", 
				"NotBlank.budgetBean.currentRoundTrip", "Required");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "currentVisa", 
				"NotBlank.budgetBean.currentVisa", "Required");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "currentPassport", 
				"NotBlank.budgetBean.currentPassport", "Required");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "currentImmunization", 
				"NotBlank.budgetBean.currentImmunization", "Required");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "currentHousing", 
				"NotBlank.budgetBean.currentHousing", "Required");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "currentCommunication", 
				"NotBlank.budgetBean.currentCommunication", "Required");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "currentMeal", 
				"NotBlank.budgetBean.currentMeal", "Required");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "currentMiscellaneous", 
				"NotBlank.budgetBean.currentMiscellaneous", "Required");
 
		BudgetBean budgetBean = (BudgetBean) target; 
		if(budgetBean.getTotalDomesticTravel() != null
			&& budgetBean.getCurrentDomesticTravel() != null){
			if(budgetBean.getTotalDomesticTravel()
					.compareTo(budgetBean.getCurrentDomesticTravel()) == -1){
				errors.rejectValue("totalDomesticTravel","Size.budgetBean.totalDomesticTravel");
				errors.rejectValue("currentDomesticTravel","Size.budgetBean.totalDomesticTravel");
			}
		}
		if(budgetBean.getTotalRoundTrip() != null
				&& budgetBean.getCurrentRoundTrip() != null){
				if(budgetBean.getTotalRoundTrip()
						.compareTo(budgetBean.getCurrentRoundTrip()) == -1){
					errors.rejectValue("totalRoundTrip","Size.budgetBean.totalRoundTrip");
					errors.rejectValue("currentRoundTrip","Size.budgetBean.totalRoundTrip");
				}
		}
		if(budgetBean.getTotalVisa() != null
				&& budgetBean.getCurrentVisa() != null){
				if(budgetBean.getTotalVisa()
						.compareTo(budgetBean.getCurrentVisa()) == -1){
					errors.rejectValue("totalVisa","Size.budgetBean.totalVisa");
					errors.rejectValue("currentVisa","Size.budgetBean.totalVisa");
				}
		}
		if(budgetBean.getTotalPassport() != null
				&& budgetBean.getCurrentPassport() != null){
				if(budgetBean.getTotalPassport()
						.compareTo(budgetBean.getCurrentPassport()) == -1){
					errors.rejectValue("totalPassport","Size.budgetBean.totalPassport");
					errors.rejectValue("currentPassport","Size.budgetBean.totalPassport");
				}
		}
		if(budgetBean.getTotalImmunization() != null
				&& budgetBean.getCurrentImmunization() != null){
				if(budgetBean.getTotalImmunization()
						.compareTo(budgetBean.getCurrentImmunization()) == -1){
					errors.rejectValue("totalImmunization","Size.budgetBean.totalImmunization");
					errors.rejectValue("currentImmunization","Size.budgetBean.totalImmunization");
				}
		}
		if(budgetBean.getTotalHousing() != null
				&& budgetBean.getCurrentHousing() != null){
				if(budgetBean.getTotalHousing()
						.compareTo(budgetBean.getCurrentHousing()) == -1){
					errors.rejectValue("totalHousing","Size.budgetBean.totalHousing");
					errors.rejectValue("currentHousing","Size.budgetBean.totalHousing");
				}
		}
		if(budgetBean.getTotalCommunication() != null
				&& budgetBean.getCurrentCommunication() != null){
				if(budgetBean.getTotalCommunication()
						.compareTo(budgetBean.getCurrentCommunication()) == -1){
					errors.rejectValue("totalCommunication","Size.budgetBean.totalCommunication");
					errors.rejectValue("currentCommunication","Size.budgetBean.totalCommunication");
				}
		}
		if(budgetBean.getTotalMeal() != null
				&& budgetBean.getCurrentMeal() != null){
				if(budgetBean.getTotalMeal()
						.compareTo(budgetBean.getCurrentMeal()) == -1){
					errors.rejectValue("totalMeal","Size.budgetBean.totalMeal");
					errors.rejectValue("currentMeal","Size.budgetBean.totalMeal");
				}
		}
		if(budgetBean.getTotalMiscellaneous() != null
				&& budgetBean.getCurrentMiscellaneous() != null){
				if(budgetBean.getTotalMiscellaneous()
						.compareTo(budgetBean.getCurrentMiscellaneous()) == -1){
					errors.rejectValue("totalMiscellaneous","Size.budgetBean.totalMiscellaneous");
					errors.rejectValue("currentMiscellaneous","Size.budgetBean.totalMiscellaneous");
				}
		}

	}
	 
}
