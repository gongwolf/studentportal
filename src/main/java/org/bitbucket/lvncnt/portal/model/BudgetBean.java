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
package org.bitbucket.lvncnt.portal.model;

import java.math.BigDecimal;

public class BudgetBean {

	private BigDecimal totalDomesticTravel; 
	private BigDecimal totalRoundTrip;
	private BigDecimal totalVisa;
	private BigDecimal totalPassport;
	private BigDecimal totalImmunization;
	private BigDecimal totalHousing;
	private BigDecimal totalCommunication;
	private BigDecimal totalMeal;
	private BigDecimal totalMiscellaneous;
	
	private BigDecimal currentDomesticTravel; 
	private BigDecimal currentRoundTrip;
	private BigDecimal currentVisa;
	private BigDecimal currentPassport;
	private BigDecimal currentImmunization;
	private BigDecimal currentHousing;
	private BigDecimal currentCommunication;
	private BigDecimal currentMeal;
	private BigDecimal currentMiscellaneous;
	 
	 
	public BigDecimal getTotalDomesticTravel() {
		return totalDomesticTravel;
	}
	public void setTotalDomesticTravel(BigDecimal totalDomesticTravel) {
		this.totalDomesticTravel = totalDomesticTravel;
	}
	public BigDecimal getTotalRoundTrip() {
		return totalRoundTrip;
	}
	public void setTotalRoundTrip(BigDecimal totalRoundTrip) {
		this.totalRoundTrip = totalRoundTrip; 
	}
 
	 
	private String miscellaneousDescribe;
	private String fundingSource;
	public String getMiscellaneousDescribe() {
		return miscellaneousDescribe;
	}
	public void setMiscellaneousDescribe(String miscellaneousDescribe) {
		this.miscellaneousDescribe = miscellaneousDescribe;
	}
	public String getFundingSource() {
		return fundingSource;
	}
	public void setFundingSource(String fundingSource) {
		this.fundingSource = fundingSource;
	}
	public BigDecimal getTotalVisa() {
		return totalVisa;
	}
	public void setTotalVisa(BigDecimal totalVisa) {
		this.totalVisa = totalVisa;
	}
	public BigDecimal getTotalPassport() {
		return totalPassport;
	}
	public void setTotalPassport(BigDecimal totalPassport) {
		this.totalPassport = totalPassport;
	}
	public BigDecimal getTotalImmunization() {
		return totalImmunization;
	}
	public void setTotalImmunization(BigDecimal totalImmunization) {
		this.totalImmunization = totalImmunization;
	}
	public BigDecimal getTotalHousing() {
		return totalHousing;
	}
	public void setTotalHousing(BigDecimal totalHousing) {
		this.totalHousing = totalHousing;
	}
	public BigDecimal getTotalCommunication() {
		return totalCommunication;
	}
	public void setTotalCommunication(BigDecimal totalCommunication) {
		this.totalCommunication = totalCommunication;
	}
	public BigDecimal getTotalMeal() {
		return totalMeal;
	}
	public void setTotalMeal(BigDecimal totalMeal) {
		this.totalMeal = totalMeal;
	}
	public BigDecimal getTotalMiscellaneous() {
		return totalMiscellaneous;
	}
	public void setTotalMiscellaneous(BigDecimal totalMiscellaneous) {
		this.totalMiscellaneous = totalMiscellaneous;
	}
	public BigDecimal getCurrentDomesticTravel() {
		return currentDomesticTravel;
	}
	public void setCurrentDomesticTravel(BigDecimal currentDomesticTravel) {
		this.currentDomesticTravel = currentDomesticTravel;
	}
	public BigDecimal getCurrentRoundTrip() {
		return currentRoundTrip;
	}
	public void setCurrentRoundTrip(BigDecimal currentRoundTrip) {
		this.currentRoundTrip = currentRoundTrip;
	}
	public BigDecimal getCurrentVisa() {
		return currentVisa;
	}
	public void setCurrentVisa(BigDecimal currentVisa) {
		this.currentVisa = currentVisa;
	}
	public BigDecimal getCurrentPassport() {
		return currentPassport;
	}
	public void setCurrentPassport(BigDecimal currentPassport) {
		this.currentPassport = currentPassport;
	}
	public BigDecimal getCurrentImmunization() {
		return currentImmunization;
	}
	public void setCurrentImmunization(BigDecimal currentImmunization) {
		this.currentImmunization = currentImmunization;
	}
	public BigDecimal getCurrentHousing() {
		return currentHousing;
	}
	public void setCurrentHousing(BigDecimal currentHousing) {
		this.currentHousing = currentHousing;
	}
	public BigDecimal getCurrentCommunication() {
		return currentCommunication;
	}
	public void setCurrentCommunication(BigDecimal currentCommunication) {
		this.currentCommunication = currentCommunication;
	}
	public BigDecimal getCurrentMeal() {
		return currentMeal;
	}
	public void setCurrentMeal(BigDecimal currentMeal) {
		this.currentMeal = currentMeal;
	}
	public BigDecimal getCurrentMiscellaneous() {
		return currentMiscellaneous;
	}
	public void setCurrentMiscellaneous(BigDecimal currentMiscellaneous) {
		this.currentMiscellaneous = currentMiscellaneous;
	}
	

}
