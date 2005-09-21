/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ws.commons.schema;


/**
 * Defines an annotation. Represents the World Wide Web Consortium (W3C)
 * annotation element.
 *
 * @author mukund
 */

// October 15th - momo  - initial implementation
// Feb 15th 2002 - Joni - items initialized when instantiated.

public class XmlSchemaAnnotation extends XmlSchemaObject {
    XmlSchemaObjectCollection items;

    /**
     * Creates new XmlSchemaAnnotation
     */
    public XmlSchemaAnnotation() {
        items = new XmlSchemaObjectCollection();
    }

    public XmlSchemaObjectCollection getItems() {
        return items;
    }
}