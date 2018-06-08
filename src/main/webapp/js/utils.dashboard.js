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

function chart_summary(config) {
	// summary chart 
	var series = config.series, 
	categories = config.categories,
	dom = config.dom,
	mapAccept = config.mapAccept,
	mapSemester = config.mapSemester; 
	
	var totalAccept = 0, totalDeny = 0,
	totalSpring = 0, totalSummer = 0, totalFall = 0; 
	
	Object.entries(mapAccept).forEach(
		([key, value]) => {
			Object.entries(value).forEach(
					([k, v]) => {
						if(k.split("-")[1] === '1'){
							totalAccept += v; 
						}else{
							totalDeny += v; 
						}
					}
				);
		}
	);
	
	Object.entries(mapSemester).forEach(
			([key, value]) => {
				Object.entries(value).forEach(
						([k, v]) => {
							if(k.split("-")[1] === 'Spring'){
								totalSpring += v; 
							}else if(k.split("-")[1] === 'Summer'){
								totalSummer += v; 
							}else{
								totalFall += v; 
							}
						}
					);
			}
		);
	
	var menuItems = [{
        textKey: 'downloadPNG',
        onclick: function () {
            this.exportChart();
        }
    }, {
        textKey: 'downloadJPEG',
        onclick: function () {
            this.exportChart({
                type: 'image/jpeg'
            });
        }
    }, {
    	textKey: 'downloadCSV',
        onclick: function () { this.downloadCSV(); }
    }]; 
	
	var options = {
		    chart: {
		        renderTo: dom,
		        defaultSeriesType: 'column'
		    },
		    title: {
	    		text: ''
	    },
	    xAxis: {
	    		categories: categories
	    },
	    yAxis: {
	        min: 0,
	        title: {
	            text: ''
	        },
	        stackLabels: {
	            enabled: false,
	            style: {
	                fontWeight: 'bold',
	                color: (Highcharts.theme && Highcharts.theme.textColor) || 'gray'
	            }
	        }
	    },
	    legend: {
	        align: 'right',
	       x: -40,
	        verticalAlign: 'top',
	       y: 0,
	        floating: true,
	        backgroundColor: (Highcharts.theme && Highcharts.theme.background2) || 'white',
	        borderColor: '#CCC',
	        borderWidth: 0,
	        shadow: false
	    },
	    tooltip: {
	        headerFormat: '<b>{point.x}</b><br/>',
	        pointFormat: '{series.name}: {point.y}<br/>Total: {point.stackTotal}'
	    },
	    plotOptions: {
	        column: {
	            stacking: 'normal',
	            dataLabels: {
	                enabled: false,
	                color: (Highcharts.theme && Highcharts.theme.dataLabelsColor) || 'white'
	            } 
	        },
	        series: {
                cursor: 'pointer',
                point: {
                    events: {
                        click: function () {
                        		$('#title-total').text('Program: ' + this.series.name +', Year: '+this.category+ ', Total: ' +this.y);

                        		chartAccept.series[0].setData([
                        	        ['Accept', mapAccept[this.series.name][this.category+'-1']],
                        	        ['Deny', mapAccept[this.series.name][this.category+'-0']] 
                        	    ], true);
                        		
                        		chartSemester.series[0].setData([
                        	        ['Spring', mapSemester[this.series.name][this.category+'-Spring']],
                        	        ['Summer', mapSemester[this.series.name][this.category+'-Summer']] ,
                        	        ['Fall', mapSemester[this.series.name][this.category+'-Fall']] 
                        	    ], true);
                        }
                    }
                }
            }
	    },
	    credits: {
	        enabled: false
	    }, 
	    series: series,
	    exporting: {
            buttons: {
                contextButton: {
                		menuItems: menuItems
                }
            }
        }
		};
	var chart = new Highcharts.Chart(options);
	
	// sub-chart 
	var chartAccept = new Highcharts.Chart({
        chart: {
            renderTo: 'chart-accept',
            plotBackgroundColor: null,
            plotBorderWidth: null,
            plotShadow: false
        },
        title: {
            text: ''
        },
        tooltip: {
            formatter: function() {
                return '<b>'+ this.point.name +'</b>: '+ this.point.y;
            }
        },
        plotOptions: {
            pie: {
                allowPointSelect: true,
                cursor: 'pointer',
                dataLabels: {
                    enabled: true,
                    color: '#000000',
                    connectorColor: '#000000',
                    formatter: function() {
                        return '<b>'+ this.point.name +'</b>';
                    }
                }
            }
        },
        credits: {
	        enabled: false
	    },
        series: [{
            type: 'pie',
            name: 'Browser share',
            data: [
                ['Deny',   totalDeny],
                {
                    name: 'Accept',
                    y: totalAccept,
                    sliced: true,
                    selected: true
                }
            ]
        }],
        exporting: {
            buttons: {
                contextButton: {
                		menuItems: menuItems
                }
            }
        }
    });
	      
	
	var chartSemester = new Highcharts.Chart({
        chart: {
            renderTo: 'chart-semester',
            plotBackgroundColor: null,
            plotBorderWidth: null,
            plotShadow: false
        },
        title: {
            text: ''
        },
        tooltip: {
            formatter: function() {
                return '<b>'+ this.point.name +'</b>: '+ this.point.y;
            }
        },
        plotOptions: {
            pie: {
                allowPointSelect: true,
                cursor: 'pointer',
                dataLabels: {
                    enabled: true,
                    color: '#000000',
                    connectorColor: '#000000',
                    formatter: function() {
                        return '<b>'+ this.point.name +'</b>';
                    }
                }
            }
        },
        credits: {
	        enabled: false
	    },
        series: [{
            type: 'pie',
            name: 'Browser share',
            data: [
                ['Fall',   totalFall],
                ['Summer', totalSummer],
                {
                    name: 'Spring',
                    y: totalSpring,
                    sliced: true,
                    selected: true
                }
            ]
        }],
        exporting: {
            buttons: {
                contextButton: {
                		menuItems: menuItems
                }
            }
        }
    });

}
 