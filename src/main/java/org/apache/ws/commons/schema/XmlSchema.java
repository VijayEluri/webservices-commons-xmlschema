/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.ws.commons.schema;

import org.apache.ws.commons.schema.constants.Constants;
import org.apache.ws.commons.schema.utils.NamespaceContextOwner;
import org.apache.ws.commons.schema.utils.NamespacePrefixList;
import org.w3c.dom.Document;

import javax.xml.namespace.QName;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;


/**
 * Contains the definition of a schema. All XML Schema definition language (XSD)
 * elements are children of the schema element. Represents the World Wide Web
 * Consortium (W3C) schema element
 */

// Oct 15th - momo - initial impl
// Oct 17th - vidyanand - add SimpleType + element
// Oct 18th - momo - add ComplexType
// Oct 19th - vidyanand - handle external
// Dec 6th - Vidyanand - changed RuntimeExceptions thrown to XmlSchemaExceptions
// Jan 15th - Vidyanand - made changes to SchemaBuilder.handleElement to look for an element ref.
// Feb 20th - Joni - Change the getXmlSchemaFromLocation schema
//            variable to name s.
// Feb 21th - Joni - Port to XMLDomUtil and Tranformation.

public class XmlSchema extends XmlSchemaAnnotated implements NamespaceContextOwner {
    static final String SCHEMA_NS = "http://www.w3.org/2001/XMLSchema";
    XmlSchemaForm attributeFormDefault, elementFormDefault;

    XmlSchemaObjectTable attributeGroups,
            attributes, elements, groups,
            notations, schemaTypes;
    XmlSchemaDerivationMethod blockDefault, finalDefault;
    XmlSchemaObjectCollection includes, items;
    boolean isCompiled;
    String syntacticalTargetNamespace, logicalTargetNamespace, version;
    String schema_ns_prefix = "";
    XmlSchemaCollection parent;

    private NamespacePrefixList namespaceContext;
    //keep the encoding of the input
    private String inputEncoding;

    public void setInputEncoding(String encoding){
        this.inputEncoding = encoding;
    }
    
    /**
     * Create a new XmlSchema within an XmlSchemaCollection
     * 
     * @param parent the parent XmlSchemaCollection
     */
    public XmlSchema(XmlSchemaCollection parent) {
        this.parent = parent;
        attributeFormDefault = new XmlSchemaForm(XmlSchemaForm.UNQUALIFIED);
        elementFormDefault = new XmlSchemaForm(XmlSchemaForm.UNQUALIFIED);
        blockDefault = new XmlSchemaDerivationMethod(Constants.BlockConstants.NONE);
        finalDefault = new XmlSchemaDerivationMethod(Constants.BlockConstants.NONE);
        items = new XmlSchemaObjectCollection();
        includes = new XmlSchemaObjectCollection();
        elements = new XmlSchemaObjectTable();
        attributeGroups = new XmlSchemaObjectTable();
        attributes = new XmlSchemaObjectTable();
        groups = new XmlSchemaObjectTable();
        notations = new XmlSchemaObjectTable();
        schemaTypes = new XmlSchemaObjectTable();
    }

    public XmlSchema(String namespace, XmlSchemaCollection parent) {
        this(parent);
        syntacticalTargetNamespace = logicalTargetNamespace = namespace;
    }

    public XmlSchemaForm getAttributeFormDefault() {
        return attributeFormDefault;
    }

    public void setAttributeFormDefault(XmlSchemaForm value) {
        attributeFormDefault = value;
    }

    public XmlSchemaObjectTable getAttributeGroups() {
        return attributeGroups;
    }

    public XmlSchemaObjectTable getAttributes() {
        return attributes;
    }

    public XmlSchemaDerivationMethod getBlockDefault() {
        return blockDefault;
    }

    public void setBlockDefault(XmlSchemaDerivationMethod blockDefault) {
        this.blockDefault = blockDefault;
    }

    public XmlSchemaForm getElementFormDefault() {
        return elementFormDefault;
    }

    public void setElementFormDefault(XmlSchemaForm elementFormDefault) {
        this.elementFormDefault = elementFormDefault;
    }

    public XmlSchemaObjectTable getElements() {
        return elements;
    }

    public XmlSchemaElement getElementByName(QName name) {
        XmlSchemaElement element = (XmlSchemaElement) elements.getItem(name);
        if (element == null) {
            //search the imports
            for (Iterator includedItems = includes.getIterator(); includedItems.hasNext();) {
                Object includeOrImport = includedItems.next();
                XmlSchema schema;
                if (includeOrImport instanceof XmlSchemaImport) {
                    schema = ((XmlSchemaImport) includeOrImport).getSchema();
                } else if (includeOrImport instanceof XmlSchemaInclude) {
                    schema = ((XmlSchemaInclude) includeOrImport).getSchema();
                } else {
                    //skip ?
                    continue;
                }
                if (schema.getElementByName(name) != null) {
                    return schema.getElementByName(name);
                }
            }
        } else {
            return element;
        }

        return null;
    }

    public XmlSchemaType getTypeByName(QName name) {
        XmlSchemaType type = (XmlSchemaType) schemaTypes.getItem(name);
        if (type == null) {
            //search the imports
            for (Iterator includedItems = includes.getIterator(); includedItems.hasNext();) {
                Object includeOrImport = includedItems.next();
                XmlSchema schema;
                if (includeOrImport instanceof XmlSchemaImport) {
                    schema = ((XmlSchemaImport) includeOrImport).getSchema();
                } else if (includeOrImport instanceof XmlSchemaInclude) {
                    schema = ((XmlSchemaInclude) includeOrImport).getSchema();
                } else {
                    //skip ?
                    continue;
                }

                if (schema.getTypeByName(name) != null) {
                    return schema.getTypeByName(name);
                }
            }
        } else {
            return type;
        }

        return null;
    }

    public XmlSchemaDerivationMethod getFinalDefault() {
        return finalDefault;
    }

    public void setFinalDefault(XmlSchemaDerivationMethod finalDefault) {
        this.finalDefault = finalDefault;
    }

    public XmlSchemaObjectTable getGroups() {
        return groups;
    }

    public XmlSchemaObjectCollection getIncludes() {
        return includes;
    }

    public boolean isCompiled() {
        return isCompiled;
    }

    public XmlSchemaObjectCollection getItems() {
        return items;
    }

    public XmlSchemaObjectTable getNotations() {
        return notations;
    }

    public XmlSchemaObjectTable getSchemaTypes() {
        return schemaTypes;
    }

    public String getTargetNamespace() {
        return syntacticalTargetNamespace;
    }

    public void setTargetNamespace(String targetNamespace) {
        if (!targetNamespace.equals("")) {
            syntacticalTargetNamespace = logicalTargetNamespace = targetNamespace;
        }
    }

    public String getVersion() {
        return version;
    }

    /**
     * Serialize the schema into the given output stream
     * @param out - the output stream to write to
     */
    public void write(OutputStream out) {
        if (this.inputEncoding!= null &&
                !"".equals(this.inputEncoding)){
            try {
                write(new OutputStreamWriter(out, this.inputEncoding));
            } catch (UnsupportedEncodingException e) {
                //log the error and just write it without the encoding

                write(new OutputStreamWriter(out));
            }
        }else{
            write(new OutputStreamWriter(out));
        }

    }

    /**
     * Serialize the schema into the given output stream
     * @param out - the output stream to write to
     * @param options -  a map of options
     */
    public void write(OutputStream out, Map options) {
        if (this.inputEncoding!= null &&
                !"".equals(this.inputEncoding)){
            try {
                write(new OutputStreamWriter(out, this.inputEncoding), options);
            } catch (UnsupportedEncodingException e) {
                //log the error and just write it without the encoding
                write(new OutputStreamWriter(out));
            }
        }else{
            write(new OutputStreamWriter(out),options);
        }

    }

    /**
     * Serialize the schema
     *
     * @param writer a Writer to serialize to
     * @param options a way to pass arbitrary options to the internal serializer
     */
    public void write(Writer writer, Map options) {
        serialize_internal(this, writer, options);
    }

    /**
     * Serialize the schema
     *
     * @param writer a Writer to serialize to
     */
    public void write(Writer writer) {
        serialize_internal(this, writer, null);
    }

    public Document[] getAllSchemas() {
        try {

            XmlSchemaSerializer xser = new XmlSchemaSerializer();
            xser.setExtReg(this.parent.getExtReg());
            return xser.serializeSchema(this, true);

        } catch (XmlSchemaSerializer.XmlSchemaSerializerException e) {
            throw new XmlSchemaException(e.getMessage());
        }
    }

    /**
     * serialize the schema - this is the method that does the work
     * @param schema XmlSchema to serialize
     * @param out the Writer we'll write to
     * @param options options to customize the serialization
     */
    private void serialize_internal(XmlSchema schema, Writer out, Map options) {

        try {
            XmlSchemaSerializer xser = new XmlSchemaSerializer();
            xser.setExtReg(this.parent.getExtReg());
            Document[] serializedSchemas = xser.serializeSchema(schema, false);
            TransformerFactory trFac = TransformerFactory.newInstance();

            try {
                trFac.setAttribute("indent-number", "4");
            } catch (IllegalArgumentException e) {
                //do nothing - we'll just silently let this pass if it
                //was not compatible
            }

            Source source = new DOMSource(serializedSchemas[0]);
            Result result = new StreamResult(out);
            javax.xml.transform.Transformer tr = trFac.newTransformer();

            //use the input encoding if there is one
            if (schema.inputEncoding!= null && !"".equals(schema.inputEncoding)) {
                tr.setOutputProperty(OutputKeys.ENCODING, schema.inputEncoding);
            }

            // If options were passed, we'll use them to figure out encoding, etc.
            // If not, we load the default ones.

            if (options == null) {
                options = new HashMap();
                loadDefaultOptions(options);
            }

            Iterator keys = options.keySet().iterator();
            while (keys.hasNext()) {
                Object key = keys.next();
                tr.setOutputProperty((String)key, (String)options.get(key));
            }

            tr.transform(source, result);
            out.flush();
        } catch (TransformerConfigurationException e) {
            throw new XmlSchemaException(e.getMessage());
        } catch (TransformerException e) {
            throw new XmlSchemaException(e.getMessage());
        } catch (XmlSchemaSerializer.XmlSchemaSerializerException e) {
            throw new XmlSchemaException(e.getMessage());
        } catch (IOException e) {
            throw new XmlSchemaException(e.getMessage());
        }
    }

    /**
     * Load the default options
     * @param options  - the map of
     */
    private void loadDefaultOptions(Map options) {
        options.put(OutputKeys.OMIT_XML_DECLARATION, "yes");
        options.put(OutputKeys.INDENT, "yes");
    }

    public void addType(XmlSchemaType type) {
        QName qname = type.getQName();
        if (schemaTypes.contains(qname)) {
            throw new RuntimeException("Schema for namespace '" +
                    syntacticalTargetNamespace + "' already contains type '" +
                    qname.getLocalPart());
        }
        schemaTypes.add(qname, type);
    }

    public NamespacePrefixList getNamespaceContext() {
        return namespaceContext;
    }

    /**
     * Sets the schema element's namespace context. This may be used for schema
     * serialization, until a better mechanism is found.
     *
     * @param namespaceContext representation of the currently defined namespace prefixes
     */
    public void setNamespaceContext(NamespacePrefixList namespaceContext) {
        this.namespaceContext = namespaceContext;
    }

    /**
     * Override the equals(Object) method with equivalence checking
     * that is specific to this class.
     */
    public boolean equals(Object what) {

        //Note: this method may no longer be required when line number/position are used correctly in XmlSchemaObject.
        //Currently they are simply initialized to zero, but they are used in XmlSchemaObject.equals 
        //which can result in a false positive (e.g. if a WSDL contains 2 inlined schemas).

        if (what == this) {
            return true;
        }

        //If the inherited behaviour determines that the objects are NOT equal, return false. 
        //Otherwise, do some further equivalence checking.

        if(!super.equals(what)) {
            return false;
        }

        if (!(what instanceof XmlSchema)) {
            return false;
        }

        XmlSchema xs = (XmlSchema) what;

        if (this.id != null) {
            if (!this.id.equals(xs.id)) {
                return false;
            }
        } else {
            if (xs.id != null) {
                return false;
            }
        }

        if (this.syntacticalTargetNamespace != null) {
            if (!this.syntacticalTargetNamespace.equals(xs.syntacticalTargetNamespace)) {
                return false;
            }
        } else {
            if (xs.syntacticalTargetNamespace != null) {
                return false;
            }
        }

        //TODO decide if further schema content should be checked for equivalence.

        return true;
    }
    public String getInputEncoding() {
        return inputEncoding;
    }
}
