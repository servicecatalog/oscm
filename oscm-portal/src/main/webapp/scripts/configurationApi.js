/* 
 *  Copyright FUJITSU LIMITED 2017
 */ 
function extend(base, sub) {
    if (Object.create) {
        sub.prototype = Object.create(base.prototype);
    } else {
        // for IE8, which doesn't support ECMAScript 5...
        function dummyConstructor() {};
        dummyConstructor.prototype = base.prototype;
        sub.prototype = new dummyConstructor();
    }

    sub.prototype.constructor = sub;
}

function Enum(name, ordinal) {
    this._name = name;
    this._ordinal = ordinal;
}

Enum.prototype = {
    constructor : Enum,

    getName : function() {
        return this._name;
    },

    getOrdinal : function() {
        return this._ordinal;
    }
}

//*******************************************
//*** OSCM Service Parameter definitions ***
//*******************************************

function Parameter(id, valueType, modificationType, value, valueError, mandatory, readonly, description, pricePerUser, pricePerSubscription) {
    this._id = null;
    this._valueType = null;
    this._modificationType = null;
    this._value = "";
    this._valueError = null;
    this._mandatory = null;
    this._readonly = null;
    this._description = null;
    this._pricePerUser = null;
    this._pricePerSubscription = null;

    this.setId(id);
    this.setValueType(valueType);
    this.setModificationType(modificationType);
    this.setValue(value);
    this.setValueError(valueError);
    this.setMandatory(mandatory);
    this.setReadonly(readonly);
    this.setDescription(description);
    this.setPricePerUser(pricePerUser);
    this.setPricePerSubscription(pricePerSubscription);
}

Parameter.Type = {
    BOOLEAN : new Enum("BOOLEAN", 0),
    INTEGER : new Enum("INTEGER", 1),
    LONG : new Enum("LONG", 2),
    STRING : new Enum("STRING", 3),
    ENUMERATION : new Enum("ENUMERATION", 4),
    DURATION : new Enum("DURATION", 5),
    
    isValid : function(t) {
        return (t === this.BOOLEAN || t === this.INTEGER || t === this.LONG
                || t === this.STRING || t === this.ENUMERATION || t === this.DURATION);
    },
    
    valueOf : function(name) {
        switch (name) {
        case "BOOLEAN":
            return this.BOOLEAN;
        case "INTEGER":
            return this.INTEGER;
        case "LONG":
            return this.LONG;
        case "STRING":
            return this.STRING;
        case "ENUMERATION":
            return this.ENUMERATION;
        case "DURATION":
            return this.DURATION;
        default:
            return null;
        }
    }
}

Parameter.ModificationType = {
    STANDARD : new Enum("STANDARD", 0),
    ONE_TIME : new Enum("ONE_TIME", 1),
    isValid : function(mt) {
        return (mt === this.STANDARD || mt === this.ONE_TIME);
    },
    valueOf : function(name) {
        switch (name) {
        case "STANDARD":
            return this.STANDARD;
        case "ONE_TIME":
            return this.ONE_TIME;
        default:
            return null;
        }
    }
}

Parameter.prototype = {
    constructor : Parameter,

    getId : function() {
        return this._id;
    },

    setId : function(id) {
        if (typeof id == "string") {
            this._id = id;
        }
    },

    getValueType : function() {
        return this._valueType;
    },

    setValueType : function(valueType) {
        if (Parameter.Type.isValid(valueType)) {
            this._valueType = valueType;
        }
    },

    getModificationType : function() {
        return this._modificationType;
    },

    setModificationType : function(modificationType) {
        if (Parameter.ModificationType.isValid(modificationType)) {
            this._modificationType = modificationType;
        }
    },

    getValue : function() {
        return this._value;
    },

    setValue : function(value) {
        if (typeof value == "string") {
            this._value = value;
        }
    },

    hasValueError : function() {
        return this._valueError;
    },

    setValueError : function(valueError) {
        if (typeof valueError == "boolean") {
            this._valueError = valueError;
        }
    },

    isMandatory : function() {
        return this._mandatory;
    },

    setMandatory : function(mandatory) {
        if (typeof mandatory == "boolean") {
            this._mandatory = mandatory;
        }
    },

    isReadonly : function() {
        return this._readonly;
    },

    setReadonly : function(readonly) {
        if (typeof readonly == "boolean") {
            this._readonly = readonly;
        }
    },

    getDescription : function() {
        return this._description;
    },

    setDescription : function(description) {
        if (typeof description == "string") {
            this._description = description;
        }
    },

    getPricePerUser : function() {
        return this._pricePerUser;
    },

    setPricePerUser : function(pricePerUser) {
        this._pricePerUser = pricePerUser;
    },

    getPricePerSubscription : function() {
        return this._pricePerSubscription;
    },

    setPricePerSubscription : function(pricePerSubscription) {
        this._pricePerSubscription = pricePerSubscription;
    },
    
    toJsonObject : function() {
        var jsonObject = new Object();
        
        jsonObject.id = this.getId();
        jsonObject.valueType = this.getValueType().getName();
        jsonObject.modificationType = this.getModificationType().getName();
        jsonObject.value = this.getValue();
        jsonObject.valueError = this.hasValueError();
        jsonObject.mandatory = this.isMandatory();
        jsonObject.readonly = this.isReadonly();
        jsonObject.description = this.getDescription();
        jsonObject.pricePerUser = this.getPricePerUser();
        jsonObject.pricePerSubscription = this.getPricePerSubscription();
        
        if (typeof this._toJsonObject == 'function') {
            this._toJsonObject(jsonObject);
        }

        return jsonObject;
    }    
}


function NumericParameter(id, valueType, modificationType, value, valueError, mandatory, 
        readonly, description, minValue, maxValue, pricePerUser, pricePerSubscription) {
    Parameter.call(this, id, valueType, modificationType, value, valueError, mandatory, 
        readonly, description, pricePerUser, pricePerSubscription);

    this._minValue = "";
    this._maxValue = "";

    this.setMinimumValue(minValue);
    this.setMaximumValue(maxValue);
}

extend(Parameter, NumericParameter);

NumericParameter.prototype.getMinimumValue = function() {
    return this._minValue;
}

NumericParameter.prototype.setMinimumValue = function(minValue) {
    if (typeof minValue == "string") {
        this._minValue = minValue;
    }
}

NumericParameter.prototype.getMaximumValue = function() {
    return this._maxValue;
}

NumericParameter.prototype.setMaximumValue = function(maxValue) {
    if (typeof maxValue == "string") {
        this._maxValue = maxValue;
    }
}

NumericParameter.prototype._toJsonObject = function(jsonObject) {
    jsonObject.minValue = this.getMinimumValue();
    jsonObject.maxValue = this.getMaximumValue();
}


function OptionParameter(id, valueType, modificationType, value, valueError, mandatory, 
        readonly, description, options, optionIds, optionDescriptions, pricePerUser,
                     pricePerSubscription) {
    Parameter.call(this, id, valueType, modificationType, value, valueError, mandatory, 
        readonly, description, pricePerUser, pricePerSubscription);

    this._options = null;
    this._optionIds = null;
    this._optionDescriptions = null;

    this.setOptions(options, optionIds, optionDescriptions);
}

extend(Parameter, OptionParameter);

OptionParameter.prototype.getOptions = function() {
    return this._options;
}

OptionParameter.prototype.getOptionIds = function() {
    return this._optionIds;
}

OptionParameter.prototype.getOptionDescriptions = function() {
    return this._optionDescriptions;
}

OptionParameter.prototype.setOptions = function(options, optionIds, optionDescriptions) {
    if (typeof optionIds == "object" && optionIds instanceof Array
            && typeof optionDescriptions == "object"
            && optionDescriptions instanceof Array
            && optionIds.length > 0 && optionDescriptions.length > 0
            && optionIds.length == optionDescriptions.length) {
        for (var i=0; i < optionIds.length; i++) {
            if (typeof optionIds[i] != "string") {
                return;
            }
        }
        
        for (var i=0; i < optionDescriptions.length; i++) {
            if (typeof optionDescriptions[i] != "string") {
                return;
            }
        }
        
        this._options = options;
        this._optionIds = optionIds;
        this._optionDescriptions = optionDescriptions;
    }
}

OptionParameter.prototype._toJsonObject = function(jsonObject) {
    var optionArray = [];
    var optionIds = this.getOptionIds();
    var optionDescriptions = this.getOptionDescriptions();
    
    for (var k=0; k < optionIds.length; k++) {
        var option = new Object();
        option.id = optionIds[k];
        option.description = optionDescriptions[k];
        optionArray.push(option);
    }
    
    jsonObject.options = optionArray;
}

Parameter.parse = function(parsedPar) {
    var parType = parsedPar.valueType;
            
    switch (parType) {
            
    case "INTEGER": 
    case "DURATION": 
    case "LONG": 
        return new NumericParameter(parsedPar.id, Parameter.Type.valueOf(parType), 
                     Parameter.ModificationType.valueOf(parsedPar.modificationType), 
                     parsedPar.value, parsedPar.valueError, parsedPar.mandatory, 
                     parsedPar.readonly, parsedPar.description, 
                     parsedPar.minValue, parsedPar.maxValue, parsedPar.pricePerUser,
                     parsedPar.pricePerSubscription);
        break;
    case "ENUMERATION":
        var parsedOptions = parsedPar.options;
        var optionIds = [];
        var optionDescriptions = [];
                
        for (var k=0; k < parsedOptions.length; k++) {
            optionIds.push(parsedOptions[k].id);
            optionDescriptions.push(parsedOptions[k].description);
        }
                
        return new OptionParameter(parsedPar.id, Parameter.Type.valueOf(parType), 
                     Parameter.ModificationType.valueOf(parsedPar.modificationType), 
                     parsedPar.value, parsedPar.valueError, parsedPar.mandatory, 
                     parsedPar.readonly, parsedPar.description, 
                     parsedOptions, optionIds, optionDescriptions, parsedPar.pricePerUser,
                     parsedPar.pricePerSubscription);
        break;
    default:
        return new Parameter(parsedPar.id, Parameter.Type.valueOf(parType), 
                     Parameter.ModificationType.valueOf(parsedPar.modificationType),
                     parsedPar.value, parsedPar.valueError, parsedPar.mandatory, 
                     parsedPar.readonly, parsedPar.description, parsedPar.pricePerUser,
                     parsedPar.pricePerSubscription);
    }
}


//***************************
//*** Message definitions ***
//***************************

Message.Type = {
        INIT_MESSAGE : "INIT_MESSAGE",
        CONFIG_REQUEST : "CONFIG_REQUEST",
        CONFIG_RESPONSE : "CONFIG_RESPONSE"
}

function Message(messageType) {
    this._messageType = null;
    this.setMessageType(messageType);
}

Message.prototype = {
    constructor : Message,

    getMessageType : function() {
        return this._messageType;
    },

    setMessageType : function(messageType) {
        if (typeof messageType == "string") {
            this._messageType = messageType;
        }
    }
}

function InitMessage(screenWidth, screenHeight) {
    Message.call(this, Message.Type.INIT_MESSAGE);
    
    this._screenWidth = null;
    this._screenHeight = null;

    this.setScreenWidth(screenWidth);
    this.setScreenHeight(screenHeight);
}

extend(Message, InitMessage);

InitMessage.prototype.getScreenWidth = function() {
    return this._screenWidth;
}

InitMessage.prototype.setScreenWidth = function(screenWidth) {
    if (typeof screenWidth == "number") {
        this._screenWidth = screenWidth;
    }
}

InitMessage.prototype.getScreenHeight = function() {
    return this._screenHeight;
}

InitMessage.prototype.setScreenHeight = function(screenHeight) {
    if (typeof screenHeight == "number") {
        this._screenHeight = screenHeight;
    }
}

InitMessage.prototype.toJSON = function() {
    var jsonObject = new Object();
    jsonObject.messageType = this._messageType;
    jsonObject.screenWidth = this._screenWidth;
    jsonObject.screenHeight = this._screenHeight;
    return JSON.stringify(jsonObject);       
}


function ConfigRequest(locale, parameters) {
    Message.call(this, Message.Type.CONFIG_REQUEST);
    
    this._locale = null;
    this._parameters = null;
    
    this.setLocale(locale);
    this.setParameters(parameters);
}

extend(Message, ConfigRequest);

ConfigRequest.prototype.getLocale = function() {
    return this._locale;
}

ConfigRequest.prototype.setLocale = function(locale) {
    if (typeof locale == "string") {
        this._locale = locale;
    }
}

ConfigRequest.prototype.getParameters = function() {
    return this._parameters;
}

ConfigRequest.prototype.setParameters = function(parameters) {
    if (typeof parameters == "object" && parameters instanceof Array) {
        this._parameters = parameters;
    }
}

ConfigRequest.prototype.toJSON = function() {
    var jsonObject = new Object();
    jsonObject.messageType = this._messageType;
    jsonObject.locale = this._locale;
     
    if (typeof this._parameters == "object" && this._parameters instanceof Array) {
        var parameterArray = [];
        for (var i=0; i < this._parameters.length; i++) {
            parameterArray.push(this._parameters[i].toJsonObject());
        }
        jsonObject.parameters = parameterArray;
    }
        
    return JSON.stringify(jsonObject);       
}


function ConfigResponse(responseCode, parameters) {
    Message.call(this, Message.Type.CONFIG_RESPONSE);
    
    this._responseCode = null;
    this._parameters = null;
    
    this.setResponseCode(responseCode);
    this.setParameters(parameters);
}

ConfigResponse.Code = {
    CONFIGURATION_FINISHED : "CONFIGURATION_FINISHED",
    CONFIGURATION_CANCELLED : "CONFIGURATION_CANCELLED"
}

extend(Message, ConfigResponse);

ConfigResponse.prototype.getResponseCode = function() {
    return this._responseCode;    
}

ConfigResponse.prototype.setResponseCode = function(code) {
    if (typeof code == "string") {
        this._responseCode = code;
    }
}

ConfigResponse.prototype.getParameters = function() {
    return this._parameters;    
}

ConfigResponse.prototype.setParameters = function(parameters) {
    if (typeof parameters == "object" && parameters instanceof Array) {
        this._parameters = parameters;
    }
}

ConfigResponse.prototype.toJSON = function() {
    var jsonObject = new Object();
    jsonObject.messageType = this._messageType;
    jsonObject.responseCode = this._responseCode;
     
    if (typeof this._parameters == "object" && this._parameters instanceof Array) {
        var parameterArray = [];
        for (var i=0; i < this._parameters.length; i++) {
            parameterArray.push(this._parameters[i].toJsonObject());
        }
        jsonObject.parameters = parameterArray;
    }
        
    return JSON.stringify(jsonObject);       
}
    

InitMessage.parse = function(parsedObject) {
    return new InitMessage(parsedObject.screenWidth, parsedObject.screenHeight);
}

ConfigRequest.parse = function(parsedObject) {
    var locale = parsedObject.locale;
    var parsedPars = parsedObject.parameters;
    var parameterArray = null;

    if (typeof parsedPars == "object" && parsedPars instanceof Array && parsedPars.length > 0) {
        parameterArray = [];
        for (var i=0; i < parsedPars.length; i++) {
            parameterArray.push(Parameter.parse(parsedPars[i]));
        }
    }
    
    return new ConfigRequest(locale, parameterArray);
}

ConfigResponse.parse = function(parsedObject) {
    var responseCode = parsedObject.responseCode;
    var parsedPars = parsedObject.parameters;
    var parameterArray = null;
    
    if (typeof parsedPars == "object" && parsedPars instanceof Array && parsedPars.length > 0) {
        parameterArray = [];
        for (var i=0; i < parsedPars.length; i++) {
            parameterArray.push(Parameter.parse(parsedPars[i]));
        }
    }
    
    return new ConfigResponse(responseCode, parameterArray);
}

Message.parse = function(jsonString) {
    var parsedObject = JSON.parse(jsonString);
    var messageType = parsedObject.messageType;
    
    if (!typeof messageType == "string") {
        return null;
    }
        
    switch (messageType) {
        
    case Message.Type.INIT_MESSAGE:
        return InitMessage.parse(parsedObject);
        
    case Message.Type.CONFIG_REQUEST:
        return ConfigRequest.parse(parsedObject);
        
    case Message.Type.CONFIG_RESPONSE:
        return ConfigResponse.parse(parsedObject);
        
    default:
        return null;    
    }
}
