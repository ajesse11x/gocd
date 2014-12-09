/*************************GO-LICENSE-START*********************************
 * Copyright 2014 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *************************GO-LICENSE-END***********************************/

package com.thoughtworks.go.plugin.access.pluggabletask;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.config.Property;
import com.thoughtworks.go.plugin.api.response.execution.ExecutionResult;
import com.thoughtworks.go.plugin.api.response.validation.ValidationError;
import com.thoughtworks.go.plugin.api.response.validation.ValidationResult;
import com.thoughtworks.go.plugin.api.task.TaskConfig;
import com.thoughtworks.go.plugin.api.task.TaskConfigProperty;
import com.thoughtworks.go.plugin.api.task.TaskExecutionContext;
import com.thoughtworks.go.plugin.api.task.TaskView;
import com.thoughtworks.go.util.ListUtil;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class JsonBasedTaskExtensionHandler_V1 implements JsonBasedTaskExtensionHandler {
    public static final String VERSION = "1.0";
    private static final Logger LOGGER = Logger.getLogger(JsonBasedTaskExtensionHandler_V1.class);

    @Override
    public String version() {
        return VERSION;
    }

    @Override
    public String convertTaskConfigToJson(TaskConfig taskConfig) {
        return new Gson().toJson(configPropertiesAsMap(taskConfig));
    }

    @Override
    public TaskConfig convertJsonToTaskConfig(String configJson) {
        TaskConfig taskConfig = new TaskConfig();
        ArrayList<String> exceptions = new ArrayList<String>();
        try {
            Map<String, Object> configMap = (Map) new GsonBuilder().create().fromJson(configJson, Object.class);
            if (configMap.isEmpty()) {
                exceptions.add("The Json for Task Config cannot be empty");
            }
            for (Map.Entry<String, Object> entry : configMap.entrySet()) {
                TaskConfigProperty property = new TaskConfigProperty(entry.getKey(), null);
                Map propertyValue = (Map) entry.getValue();
                if (propertyValue != null) {
                    Object defaultValue = propertyValue.get("default-value");
                    if (propertyValue.containsKey("default-value") && !(defaultValue instanceof String)) {
                        exceptions.add(String.format("Key: '%s' - The Json for Task Config should contain a not-null 'default-value' of type String", entry.getKey()));
                    } else {
                        property.withDefault((String) defaultValue);
                    }
                    Object secure = propertyValue.get("secure");
                    if (propertyValue.containsKey("secure") && !(secure instanceof Boolean)) {
                        exceptions.add(String.format("Key: '%s' - The Json for Task Config should contain a 'secure' field of type Boolean", entry.getKey()));
                    } else {
                        property.with(Property.SECURE, (Boolean) secure);
                    }
                    Object required = propertyValue.get("required");
                    if (propertyValue.containsKey("required") && !(required instanceof Boolean)) {
                        exceptions.add(String.format("Key: '%s' - The Json for Task Config should contain a 'required' field of type Boolean", entry.getKey()));
                    } else {
                        property.with(Property.REQUIRED, (Boolean) required);
                    }
                }
                taskConfig.add(property);
            }
            if (!exceptions.isEmpty()) {
                throw new RuntimeException(ListUtil.join(exceptions));
            }
            return taskConfig;
        } catch (Exception e) {
            LOGGER.error(String.format("Error occurred while converting the Json to Task Config. Error: %s. The Json received was '%s'.", e.getMessage(), configJson));
            throw new RuntimeException(String.format("Error occurred while converting the Json to Task Config. Error: %s.", e.getMessage()));
        }
    }

    @Override
    public ValidationResult toValidationResult(String responseBody) {
        ValidationResult validationResult = new ValidationResult();
        ArrayList<String> exceptions = new ArrayList<String>();
        try {
            Map result = (Map) new GsonBuilder().create().fromJson(responseBody, Object.class);
            final Map<String, Object> errors = (Map<String, Object>) result.get("errors");
            if (errors != null) {
                if (errors.isEmpty()) {
                    exceptions.add("The Json for Validation Result must either be an empty body or it should have errors with the 'errors' key");
                }
                for (Map.Entry<String, Object> entry : errors.entrySet()) {
                    if (!(entry.getValue() instanceof String)) {
                        exceptions.add(String.format("Key: '%s' - The Json for Validation Request must contain a not-null error message of type String", entry.getKey()));
                    } else {
                        validationResult.addError(new ValidationError(entry.getKey(), entry.getValue().toString()));
                    }
                }
            }
            if (!exceptions.isEmpty()) {
                throw new RuntimeException(ListUtil.join(exceptions));
            }
            return validationResult;
        } catch (Exception e) {
            LOGGER.error(String.format("Error occurred while converting the Json to Validation Result. Error: %s. The Json received was '%s'.", e.getMessage(), responseBody));
            throw new RuntimeException(String.format("Error occurred while converting the Json to Validation Result. Error: %s.", e.getMessage()));
        }
    }

    @Override
    public TaskView toTaskView(String responseBody) {
        ArrayList<String> exceptions = new ArrayList<String>();
        try {
            final Map map = (Map) new GsonBuilder().create().fromJson(responseBody, Object.class);
            final Object displayValue = map.get("displayValue");
            final Object template = map.get("template");
            if (map.isEmpty()) {
                exceptions.add("The Json for Task View cannot be empty");
            } else {
                if (!map.containsKey("displayValue")) {
                    exceptions.add("The Json for Task View must contain 'displayValue'");
                } else if (!(displayValue instanceof String)) {
                    exceptions.add("The Json for Task View must contain a not-null 'displayValue' of type String");
                }
                if (!map.containsKey("template")) {
                    exceptions.add("The Json for Task View must contain 'template'");
                } else if (!(template instanceof String)) {
                    exceptions.add("The Json for Task View must contain a not-null 'template' of type String");
                }
            }
            if (!exceptions.isEmpty()) {
                throw new RuntimeException(ListUtil.join(exceptions));
            }
            return new TaskView() {
                @Override
                public String displayValue() {
                    return (String) displayValue;
                }

                @Override
                public String template() {
                    return (String) template;
                }
            };
        } catch (Exception e) {
            LOGGER.error(String.format("Error occurred while converting the Json to Task View. Error: %s. The Json received was '%s'.", e.getMessage(), responseBody));
            throw new RuntimeException(String.format("Error occurred while converting the Json to Task View. Error: %s.", e.getMessage()));
        }
    }

    @Override
    public ExecutionResult toExecutionResult(String responseBody) {
        ExecutionResult executionResult = new ExecutionResult();
        ArrayList<String> exceptions = new ArrayList<String>();
        try {
            Map result = (Map) new GsonBuilder().create().fromJson(responseBody, Object.class);
            if (!result.containsKey("success")) {
                exceptions.add("The execution result must have a success status");
            } else if (!(result.get("success") instanceof Boolean)) {
                exceptions.add("The success status must be a boolean value");
            }
            if (result.containsKey("message") && (!(result.get("message") instanceof String))) {
                exceptions.add("If the 'message' key is present in the Json for Execution Result, it must contain a not-null message of type String");
            }
            if (!exceptions.isEmpty()) {
                throw new RuntimeException(ListUtil.join(exceptions));
            }
            if ((Boolean) result.get("success")) {
                executionResult.withSuccessMessages((String) result.get("message"));
            } else {
                executionResult.withErrorMessages((String) result.get("message"));
            }
            return executionResult;
        } catch (Exception e) {
            LOGGER.error(String.format("Error occurred while converting the Json to Execution Result. Error: %s. The Json received was '%s'.", e.getMessage(), responseBody));
            throw new RuntimeException(String.format("Error occurred while converting the Json to Execution Result. Error: %s.", e.getMessage()));
        }
    }

    @Override
    public String getTaskExecutionBody(TaskConfig config, TaskExecutionContext taskExecutionContext) {
        Map requestBody = new HashMap();
        Map contextMap = new HashMap();
        contextMap.put("environmentVariables", taskExecutionContext.environment().asMap());
        contextMap.put("workingDirectory", taskExecutionContext.workingDir());
        requestBody.put("context", contextMap);
        requestBody.put("config", configPropertiesAsMap(config));
        return new Gson().toJson(requestBody);

    }

    private Map configPropertiesAsMap(TaskConfig taskConfig) {
        HashMap properties = new HashMap();
        for (Property property : taskConfig.list()) {
            final HashMap propertyValue = new HashMap();
            propertyValue.put("value", property.getValue());
            propertyValue.put("secure", property.getOption(Property.SECURE));
            propertyValue.put("required", property.getOption(Property.REQUIRED));
            properties.put(property.getKey(), propertyValue);
        }
        return properties;
    }
}