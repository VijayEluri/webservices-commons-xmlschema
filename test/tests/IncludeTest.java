package tests;

import java.io.FileInputStream;
import java.io.InputStream;

import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;
import org.apache.ws.commons.schema.XmlSchemaInclude;

/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * Copyright 2006 International Business Machines Corp.
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
 *
 */
public class IncludeTest extends TestCase {

    /**
     * This method will test the include.
     *
     * @throws Exception Any exception encountered
     */
    public void testInclude() throws Exception {

        /*
        <schema xmlns="http://www.w3.org/2001/XMLSchema"
                xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                xmlns:tns="http://soapinterop.org/types"
                targetNamespace="http://soapinterop.org/types">
  
          <include schemaLocation="include2.xsd"/>
          <include schemaLocation="include3.xsd"/>

        </schema>

        
        <schema xmlns="http://www.w3.org/2001/XMLSchema"
                xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                xmlns:tns="http://soapinterop.org/types"
                targetNamespace="http://soapinterop.org/types">
  
          <element name="test1include" type="string"/>

        </schema>


        <schema xmlns="http://www.w3.org/2001/XMLSchema"
                xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                xmlns:tns="http://soapinterop.org/types"
                targetNamespace="http://soapinterop.org/types">
  
          <element name="test2include" type="integer"/>

        </schema>
        */

        QName ELEMENT_QNAME = new QName("http://soapinterop.org/types",
                                        "test1include");
        InputStream is = new FileInputStream("test-resources/include.xsd");
        XmlSchemaCollection schemaCol = new XmlSchemaCollection();
        XmlSchema schema = schemaCol.read(new StreamSource(is), null);

        XmlSchemaObjectCollection c = schema.getIncludes();
        assertEquals(2, c.getCount());

        Set set = new HashSet();
        set.add("test-resources/include2.xsd");
        set.add("test-resources/include3.xsd");
        for (int i = 0; i < c.getCount(); i++) {
            XmlSchemaInclude include = (XmlSchemaInclude)c.getItem(i);
            assertNotNull(include);
            XmlSchema s = include.getSchema();
            assertNotNull(s);
            String schemaLocation = include.getSchemaLocation();
            if (schemaLocation.equals("test-resources/include2.xsd")) {
                XmlSchemaElement xse =
                    s.getElementByName(new
                        QName("http://soapinterop.org/types", "test1include"));
                assertEquals("test1include", xse.getName());
                assertEquals(new QName("http://www.w3.org/2001/XMLSchema", "string"),
                             xse.getSchemaTypeName());
            } else if (schemaLocation.equals("test-resources/include3.xsd")) {
                XmlSchemaElement xse =
                    s.getElementByName(new 
                        QName("http://soapinterop.org/types", "test2include"));
                assertEquals("test2include", xse.getName());
                assertEquals(new QName("http://www.w3.org/2001/XMLSchema", "integer"),
                             xse.getSchemaTypeName());
            } else {
                fail("The schemaLocation of \"" + schemaLocation + "\" was"
                     + " not expected.");
            }
            set.remove(schemaLocation);
        }

        assertTrue("The set should have been empty, but instead contained: "
                   + set + ".",
                   set.isEmpty());

    }

}