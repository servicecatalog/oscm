/* 
 *  Copyright FUJITSU LIMITED 2018
 */ 
function BssParameterConfigurator(tDiv) {
    this._targetDiv = tDiv;
    this._locale = null;
    this._parameters = null;
    this._table = null;
}

BssParameterConfigurator.prototype = {
    constructor : BssParameterConfigurator,

    render : function(configRequest) {
        this._locale = configRequest.getLocale();
        this._parameters = configRequest.getParameters();
        
        if (this._table == null) {
            this.generateParameterTable();
            this.generateButtons();
        } else {
            this.updateParameters();
        }
    },
    
    generateParameterTable : function() {
   	    this._table = document.createElement("table");
	    var tableBody = document.createElement("tbody");
	
        this._table.setAttribute("border", "1"); 
        this._table.setAttribute("bgcolor", "gold");

	    for (var i=0; i < this._parameters.length; i++) {
		    var row=document.createElement("tr");
		    <!-- create text node for parameter description and append it -->
		    var idCell = document.createElement("td");
	        var idTextNode=document.createTextNode(this._parameters[i].getDescription());
	        idCell.appendChild(idTextNode);

	        <!-- create text node for parameter value and append it -->
	        var valueCell = document.createElement("td");       

	        if (this._parameters[i].getValueType() === Parameter.Type.ENUMERATION) {
	            this.generateOptions(this._parameters[i], valueCell);
	        } else {
	            var valueInputField = document.createElement("input");
	            if (this._parameters[i].getValueType() === Parameter.Type.BOOLEAN) {
	                valueInputField.type ="checkbox";	
	                if (this._parameters[i].getValue()== "true")	{
                		valueInputField.checked= "checked";
                	}
	            } else  if(this._parameters[i].getId().indexOf("_PWD") > -1){
	            	valueInputField.type ="password";
	            }
	            else if ((this._parameters[i].getValueType() === Parameter.Type.INTEGER) 
	                    || (this._parameters[i].getValueType() === Parameter.Type.STRING) 
	                    || (this._parameters[i].getValueType() === Parameter.Type.LONG)) {      
	                valueInputField.type ="text";	
	            }
	        
	            if (this._parameters[i].getValue() != null) {
	                valueInputField.value= this._parameters[i].getValue();
	            }
	        
	            valueInputField.id = this._parameters[i].getId() + ":value";
	            valueInputField.disabled=this._parameters[i].isReadonly();
	            valueCell.appendChild(valueInputField);
	            this.generateRangeText(this._parameters[i], valueCell);
	            this.generatePriceDiv(this._parameters[i], valueCell);
	        }

	        this.generateErrorDiv(this._parameters[i], valueCell);
	        row.appendChild(idCell);
	        row.appendChild(valueCell);
	        tableBody.appendChild(row);
        }

            var row=document.createElement("tr");

            var idCell = document.createElement("td");
	    var idTextNode=document.createTextNode("Total price:");
	    idCell.appendChild(idTextNode);
	    row.appendChild(idCell);


	    var valueCell = document.createElement("td");
            valueCell.id = ":totalPrice";
	    var valueTextNode=document.createTextNode("1");
	    valueCell.appendChild(valueTextNode);
	    row.appendChild(valueCell);

	    tableBody.appendChild(row);
	
	    this._table.appendChild(tableBody);
        this._targetDiv.appendChild(this._table);

        this.updateTotalPrice();
    },

    updateTotalPrice : function() {
        var price = document.getElementById(":totalPrice");
        var totalPrice = 0;

	for (var i=0; i < this._parameters.length; i++) {
	    if (this._parameters[i].getValueType() === Parameter.Type.ENUMERATION) {
                var optionIds = this._parameters[i].getOptionIds();
	        for (var op=0; op < optionIds.length; op++) {
                    var currentOption = document.getElementById(this._parameters[i].getId() + ":option:" + optionIds[op]);
                    if(currentOption.checked) {
                        totalPrice += (this._parameters[i].getOptions()[op].pricePerSubscription)*1000000;
                    }
                }
	        } else {
                totalPrice += (this._parameters[i].getPricePerSubscription())*1000000;
	        }
        }
        price.innerHTML = totalPrice/1000000;
    },

    generateButtons : function() {
        var parConfigurator = this;
        
        var configureButton = document.createElement("input");
        configureButton.type ="button";
        configureButton.value = "Configure";
        configureButton.onclick = function () {
	        parConfigurator.configure();   	
 	    };
	
        this._targetDiv.appendChild(configureButton);
 
        var cancelButton = document.createElement("input");
	    cancelButton.type ="button";
        cancelButton.value = "Cancel";
        cancelButton.onclick = function () {
            parConfigurator.cancel();
 	    };

        this._targetDiv.appendChild(cancelButton);
    },

    generateOptions : function(parameter, valueCell) {
        if (parameter.getOptionIds && parameter.getOptionDescriptions) {
            var options = parameter.getOptions();
            var optionIds = parameter.getOptionIds();
            var optionDescriptions = parameter.getOptionDescriptions();
            
            var hiddenOptionValue = document.createElement("input");
            hiddenOptionValue.type ="hidden";
            hiddenOptionValue.id = parameter.getId() + ":value";
            hiddenOptionValue.value = parameter.getValue();
            
            for (var op=0; op < optionIds.length; op++) {
                var optionColumn = document.createElement("td");
                var optionRadio = document.createElement("input");
                optionRadio.id = parameter.getId() + ":option:" + optionIds[op]; 
                optionRadio.type ="radio";  
                optionRadio.name= parameter.getId();                
                optionRadio.value=optionIds[op];
                optionRadio.disabled=parameter.isReadonly();
                optionRadio.onclick = function() {
                    hiddenOptionValue.value = this.value;
                };
                var self = this;
                optionRadio.onchange = function() {
                    self.updateTotalPrice();
                };
            
                <!-- Set initially selected value -->        
                if (parameter.getValue() == optionRadio.value) {
                    optionRadio.checked=true;
                }
        
                optionColumn.appendChild(optionRadio);
                valueCell.appendChild(optionColumn);
           
                var optionSpan = document.createElement("span"); 
                var localizedOptionTextNode=document.createTextNode(optionDescriptions[op]);
                optionSpan.appendChild(localizedOptionTextNode);            
                valueCell.appendChild(optionSpan);
	        this.generateOptionPriceDiv(options[op], valueCell);
            }
            valueCell.appendChild(hiddenOptionValue);
        }    
    },

    generateRangeText : function(parameter, valueCell) {  
        if ((parameter.getValueType() === Parameter.Type.INTEGER) || (parameter.getValueType() === Parameter.Type.LONG)) {
            var rangeText ="";
        
            var min = parameter.getMinimumValue();
            var max = parameter.getMaximumValue();
        
            if (min != "" && max != "") {
                rangeText =  min + "-" + max;
            } else if (min != "") {
                rangeText = ">= " + min;
            } else if (max != "") {
                rangeText = "<= " + max;
            }
     
            var rangeTextNode = document.createTextNode(rangeText); 
            valueCell.appendChild(rangeTextNode);
         }   
    },

    generateErrorDiv : function(parameter, valueCell) {    
        var errorDiv = document.createElement("div");
        errorDiv.id = parameter.getId() + ":error";
        errorDiv.style.color="red";
    
        var errorText ="";
        errorDiv.innerHTML = errorText;
        valueCell.appendChild(errorDiv); 
    },

    generateOptionPriceDiv : function(parameter, valueCell) {
        var priceDiv = document.createElement("div");
        priceDiv.id = parameter.id + ":price";

        priceDiv.innerHTML = "Price: ";
        var price = parameter.pricePerSubscription;
        if(price === 0.0){
             priceDiv.innerHTML += "free";
        }else{
             priceDiv.innerHTML += price;
        }
        valueCell.appendChild(priceDiv);
    },

    generatePriceDiv : function(parameter, valueCell) {
        var priceDiv = document.createElement("div");
        priceDiv.id = parameter.getId() + ":price";

        priceDiv.innerHTML = "Price: ";
        var price = parameter.getPricePerSubscription();
        if(price === 0.0){
             priceDiv.innerHTML += "free";
        }else{
             priceDiv.innerHTML += price;
        }
        valueCell.appendChild(priceDiv);
    },

    isInvalidNumeric : function(fieldValue, parameter) {    
        if (isNaN(fieldValue)) {
            return true;
        } else {
             if (fieldValue % 1 == 0) { 
                var minValue = new Number(parameter.getMinimumValue());
                var maxValue = new Number(parameter.getMaximumValue());
                var value = new Number(fieldValue);
            
                return this.isNotInRange(value, minValue, maxValue);
            } else {            
                return true;
            }     
        }    
    },

    isNotInRange : function(value, min, max) {
        if ((min != "" && value < min) || (max != "" && value > max)) {
            return true;
        }
        return false;
    },

    isInvalidDuration : function(fieldValue, parameter) {
        return false;
    },

    isInvalid : function(fieldValue, parameter) {
        if ((fieldValue == null || fieldValue.length === 0) && (parameter.isMandatory())) {
            return true;                
        }
    
        var maxLength = 11;
        if (fieldValue.length > maxLength) {
            return true;
        }
    
        if ((parameter.getValueType() ===  Parameter.Type.INTEGER) || (parameter.getValueType() ===  Parameter.Type.LONG)) {
            return this.isInvalidNumeric(fieldValue,parameter);
        }
    
        if (parameter.getValueType() === Parameter.Type.DURATION) {
           return this.isInvalidDuration(fieldValue, parameter);
        }
    
        return false;
    },


    updateErrorDiv : function(parameter, errorText) {      
        var errorDiv = document.getElementById(parameter.getId() + ":error");
        errorDiv.innerHTML = errorText;
    },

    validateParameters : function() {
        var validationErrorOccurred = false;
    
        for (var i=0; i < this._parameters.length; i++) {
            var parameterField = document.getElementById(this._parameters[i].getId() + ":value");
            if (this.isInvalid(parameterField.value, this._parameters[i])) {
                validationErrorOccurred = true;
                this.updateErrorDiv(this._parameters[i], "The parameter value is invalid.");         
            } else {
                this.updateErrorDiv(this._parameters[i], "");
            }  
        }
        return validationErrorOccurred;    
    },

    updateParameters : function() {
        for (var i=0; i < this._parameters.length; i++) {
        
            var valueInputField = document.getElementById(this._parameters[i].getId() + ":value");
        
            if (this._parameters[i].getValueType() === Parameter.Type.ENUMERATION) {
                var optionIds = this._parameters[i].getOptionIds();
                valueInputField.value = this._parameters[i].getValue();
            
                for (var op=0; op < optionIds.length; op++) {
                    var optionRadio = document.getElementById(this._parameters[i].getId() + ":option:" + optionIds[op]);
                    optionRadio.disabled=this._parameters[i].isReadonly();

                    if (this._parameters[i].getValue() == optionRadio.value) {
                        optionRadio.checked = true;
                    } else {
                        optionRadio.checked = false;
                    }
                }
            } else if (this._parameters[i].getValueType() === Parameter.Type.BOOLEAN) {
                valueInputField.disabled=this._parameters[i].isReadonly();
                
                if (this._parameters[i].getValue()== "true")    {
                    valueInputField.checked= "checked";
                } else {
                    valueInputField.checked= "";
                }
            } else {
                valueInputField.disabled=this._parameters[i].isReadonly();
                
                if (this._parameters[i].getValue() != null) {
                    valueInputField.value = this._parameters[i].getValue();
                } else {
                    valueInputField.value = "";
                }
            
                if (this._parameters[i].hasValueError()) {
                    this.updateErrorDiv(this._parameters[i], "The parameter value is invalid.");
                } else {
                    this.updateErrorDiv(this._parameters[i], "");
                }
            }
        }
    },

    configure : function() {
        // validationErrorOccurred = this.validateParameters();
        // if (!validationErrorOccurred) {
        this.sendParameters();
        // }
    },

    sendParameters : function() {   
        for (var i=0; i < this._parameters.length; i++) {
            var parameterField = document.getElementById(this._parameters[i].getId() + ":value");
         
            if (this._parameters[i].getValueType() === Parameter.Type.BOOLEAN) {
                if (parameterField.checked) {
                    this._parameters[i].setValue("true");    
                } else {
                    this._parameters[i].setValue("false");
                }
            } else { 
                this._parameters[i].setValue(parameterField.value);
            }
        }
    
        var configResponse = new ConfigResponse(ConfigResponse.Code.CONFIGURATION_FINISHED, this._parameters);
        send(configResponse.toJSON());
    },

    cancel : function() {
        var configResponse = new ConfigResponse(ConfigResponse.Code.CONFIGURATION_CANCELLED, null);
        send(configResponse.toJSON());
    }
}
