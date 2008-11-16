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

package org.apache.ws.commons.schema.utils;

import org.apache.ws.commons.schema.constants.Constants;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.xml.namespace.NamespaceContext;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Implementation of {@link NamespaceContext}, which is based on a DOM node.
 */
public class NodeNamespaceContext implements NamespacePrefixList {
    private static final String NODE_NAMSPACE_CONTEXT = NamespacePrefixList.class.getName();
    private static final Collection XML_NS_PREFIX_COLLECTION = Collections.singletonList(Constants.XML_NS_PREFIX);
    private static final Collection XMLNS_ATTRIBUTE_COLLECTION = Collections.singletonList(Constants.XMLNS_ATTRIBUTE);
    
    static Method getUserData;
    static Method setUserData;
    static {
        try {
            Class cls = Class.forName("org.w3c.dom.UserDataHandler", false, Node.class.getClassLoader());
            getUserData = Node.class.getMethod("getUserData", new Class[]{String.class});
            setUserData = Node.class.getMethod("setUserData", new Class[]{String.class, Object.class, cls});
        } catch (Throwable e) {
            getUserData = null;
            setUserData = null;
        }
    }
    
    
    
    private final Map declarations;
    private String[] prefixes;

    /**
     * Creates a new instance with the given nodes context.
     */
    private NodeNamespaceContext(Map decls) {
        declarations = decls;
    }
    
    public static NodeNamespaceContext getNamespaceContext(Node pNode) {
        if (getUserData != null) {
            try {
                NodeNamespaceContext ctx = (NodeNamespaceContext)getUserData.invoke(pNode, new Object[] {NODE_NAMSPACE_CONTEXT});
                if (ctx == null) {
                    Map declarations = new HashMap();
        
                    Node parentNode = pNode.getParentNode();
                    if (parentNode != null) {
                        NodeNamespaceContext parent = 
                            (NodeNamespaceContext)getUserData.invoke(parentNode, new Object[] {NODE_NAMSPACE_CONTEXT});
                        if (parent == null) {
                            parent = getNamespaceContext(parentNode);
                        }
                        declarations.putAll(parent.declarations);
                    }
                    
                    NamedNodeMap map = pNode.getAttributes();
                    if (map != null) {
                        for (int i = 0; i < map.getLength(); i++) {
                            Node attr = map.item(i);
                            final String uri = attr.getNamespaceURI();
                            if (Constants.XMLNS_ATTRIBUTE_NS_URI.equals(uri)) {
                                String localName = attr.getLocalName();
                                String prefix = Constants.XMLNS_ATTRIBUTE.equals(localName) ? Constants.DEFAULT_NS_PREFIX : localName;
                                declarations.put(prefix, attr.getNodeValue());
                            }
                        }
                    }
                    ctx = new NodeNamespaceContext(declarations);
                    setUserData.invoke(pNode, new Object[] {NODE_NAMSPACE_CONTEXT, ctx, null});
                }
                return ctx;
            } catch (Throwable t) {
                //ignore.  DOM level 2 implementation would not have the getUserData stuff.   
                //Thus, fall back to the old, slower method.
            }
        }
        
        final Map declarations = new HashMap();
        new PrefixCollector(){
            protected void declare(String pPrefix, String pNamespaceURI) {
                declarations.put(pPrefix, pNamespaceURI);
            }
        }.searchAllPrefixDeclarations(pNode);
        return new NodeNamespaceContext(declarations);
    }

    public String getNamespaceURI(String pPrefix) {
        if (pPrefix == null) {
            throw new IllegalArgumentException("The prefix must not be null.");
        }
        if (Constants.XML_NS_PREFIX.equals(pPrefix)) {
            return Constants.XML_NS_URI;
        }
        if (Constants.XMLNS_ATTRIBUTE.equals(pPrefix)) {
            return Constants.XMLNS_ATTRIBUTE_NS_URI;
        }
        final String uri = (String) declarations.get(pPrefix);
        return uri == null ? Constants.NULL_NS_URI : uri;
    }

    public String getPrefix(String pNamespaceURI) {
        if (pNamespaceURI == null) {
            throw new IllegalArgumentException("The namespace URI must not be null.");
        }
        if (Constants.XML_NS_URI.equals(pNamespaceURI)) {
            return Constants.XML_NS_PREFIX;
        }
        if (Constants.XMLNS_ATTRIBUTE_NS_URI.equals(pNamespaceURI)) {
            return Constants.XMLNS_ATTRIBUTE;
        }
        for (Iterator iter = declarations.entrySet().iterator();  iter.hasNext();  ) {
            Map.Entry entry = (Map.Entry) iter.next();
            if (pNamespaceURI.equals(entry.getValue())) {
                return (String) entry.getKey();
            }
        }
        return null;
    }

    public Iterator getPrefixes(String pNamespaceURI) {
        if (pNamespaceURI == null) {
            throw new IllegalArgumentException("The namespace URI must not be null.");
        }
        if (Constants.XML_NS_URI.equals(pNamespaceURI)) {
            return XML_NS_PREFIX_COLLECTION.iterator();
        }
        if (Constants.XMLNS_ATTRIBUTE_NS_URI.equals(pNamespaceURI)) {
            return XMLNS_ATTRIBUTE_COLLECTION.iterator();
        }
        final List list = new ArrayList();
        for (Iterator iter = declarations.entrySet().iterator();  iter.hasNext();  ) {
            Map.Entry entry = (Map.Entry) iter.next();
            if (pNamespaceURI.equals(entry.getValue())) {
                list.add(entry.getKey());
            }
        }
        return list.iterator();
    }

    public String[] getDeclaredPrefixes() {
        if (prefixes == null) {
            Collection keys = declarations.keySet();
            prefixes = (String[]) keys.toArray(new String[keys.size()]);
        }
        return prefixes;
    }
}
