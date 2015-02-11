/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.event.notifier.core.internal.type.json;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.wso2.carbon.event.notifier.core.config.EventNotifierConstants;
import org.wso2.carbon.event.notifier.core.config.OutputMapping;
import org.wso2.carbon.event.notifier.core.exception.EventNotifierConfigurationException;

import javax.xml.namespace.QName;
import java.util.Iterator;


/**
 * This class is used to read the values of the event builder configuration defined in XML configuration files
 */

public class JSONMapperConfigurationBuilder {

    private JSONMapperConfigurationBuilder() {

    }

    public static OutputMapping fromOM(
            OMElement mappingElement)
            throws EventNotifierConfigurationException {


        JSONOutputMapping jsonOutputMapping = new JSONOutputMapping();

        String customMappingEnabled = mappingElement.getAttributeValue(new QName(EventNotifierConstants.EF_ATTR_CUSTOM_MAPPING));
        if (customMappingEnabled == null || (customMappingEnabled.equals(EventNotifierConstants.TM_VALUE_ENABLE))) {
            jsonOutputMapping.setCustomMappingEnabled(true);
            if (!validateJSONMapping(mappingElement)) {
                throw new EventNotifierConfigurationException("JSON Mapping is not valid, check the output mapping");
            }

            OMElement innerMappingElement = mappingElement.getFirstChildWithName(
                    new QName(EventNotifierConstants.EF_CONF_NS, EventNotifierConstants.EF_ELE_MAPPING_INLINE));
            if (innerMappingElement != null) {
                jsonOutputMapping.setRegistryResource(false);
            } else {
                innerMappingElement = mappingElement.getFirstChildWithName(
                        new QName(EventNotifierConstants.EF_CONF_NS, EventNotifierConstants.EF_ELE_MAPPING_REGISTRY));
                if (innerMappingElement != null) {
                    jsonOutputMapping.setRegistryResource(true);
                } else {
                    throw new EventNotifierConfigurationException("XML Mapping is not valid, Mapping should be inline or from registry");
                }
            }

            if (innerMappingElement.getText() == null || innerMappingElement.getText().trim().isEmpty()) {
                throw new EventNotifierConfigurationException("There is no any mapping content available");

            } else {
                jsonOutputMapping.setMappingText(innerMappingElement.getText());
            }

        } else {
            jsonOutputMapping.setCustomMappingEnabled(false);
        }

        return jsonOutputMapping;
    }


    private static boolean validateJSONMapping(OMElement omElement) {

        int count = 0;
        Iterator<OMElement> mappingIterator = omElement.getChildElements();
        while (mappingIterator.hasNext()) {
            count++;
            mappingIterator.next();
        }

        return count != 0;

    }


    public static OMElement outputMappingToOM(
            OutputMapping outputMapping, OMFactory factory) {

        JSONOutputMapping jsonOutputMapping = (JSONOutputMapping) outputMapping;

        OMElement mappingOMElement = factory.createOMElement(new QName(
                EventNotifierConstants.EF_ELE_MAPPING_PROPERTY));
        mappingOMElement.declareDefaultNamespace(EventNotifierConstants.EF_CONF_NS);

        mappingOMElement.addAttribute(EventNotifierConstants.EF_ATTR_TYPE, EventNotifierConstants.EF_JSON_MAPPING_TYPE, null);

        if (jsonOutputMapping.isCustomMappingEnabled()) {
            mappingOMElement.addAttribute(EventNotifierConstants.EF_ATTR_CUSTOM_MAPPING, EventNotifierConstants.TM_VALUE_ENABLE, null);


            OMElement innerMappingElement;
            if (jsonOutputMapping.isRegistryResource()) {
                innerMappingElement = factory.createOMElement(new QName(
                        EventNotifierConstants.EF_ELE_MAPPING_REGISTRY));
                innerMappingElement.declareDefaultNamespace(EventNotifierConstants.EF_CONF_NS);
            } else {
                innerMappingElement = factory.createOMElement(new QName(
                        EventNotifierConstants.EF_ELE_MAPPING_INLINE));
                innerMappingElement.declareDefaultNamespace(EventNotifierConstants.EF_CONF_NS);
            }
            mappingOMElement.addChild(innerMappingElement);
            innerMappingElement.setText(jsonOutputMapping.getMappingText());
        } else {
            mappingOMElement.addAttribute(EventNotifierConstants.EF_ATTR_CUSTOM_MAPPING, EventNotifierConstants.TM_VALUE_DISABLE, null);
        }

        return mappingOMElement;
    }


}



