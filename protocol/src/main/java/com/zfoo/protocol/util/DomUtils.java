/*
 * Copyright (C) 2020 The zfoo Authors
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *
 */

package com.zfoo.protocol.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.zfoo.protocol.collection.CollectionUtils;
import com.zfoo.protocol.exception.RunException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.stream.XMLInputFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * DOM(Document Object Model)文档对象模型
 * <p>
 * Convenience methods for working with the DOM API,in particular for working with DOM Nodes and DOM Elements.
 * </p>
 *
 * @author godotg
 */
public abstract class DomUtils {

    private static final XmlMapper MAPPER = XmlMapper.builder()
            .defaultUseWrapper(false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)  // 当反序列化有未知属性则抛异常，true打开这个设置
            .build();

    public static <T> T string2Object(String xml, Class<T> clazz) {
        try {
            return MAPPER.readValue(xml, clazz);
        } catch (IOException e) {
            throw new RunException("将xml字符串[xml:{}]转换为对象[{}]异常", xml, clazz, e);
        }
    }

    public static <T> T file2Object(File xmlFile, Class<T> clazz) {
        try {
            var f = XMLInputFactory.newFactory();
            var sr = f.createXMLStreamReader(new FileInputStream(xmlFile));
            return MAPPER.readValue(sr, clazz);
        } catch (Exception e) {
            throw new RunException("将xml文件[xml:{}]转换为对象[{}]异常", xmlFile, clazz, e);
        }
    }

    public static <T> T inputStream2Object(InputStream xmlInputStream, Class<T> clazz) {
        try {
            return MAPPER.readValue(xmlInputStream, clazz);
        } catch (Exception e) {
            throw new RunException("将xmlInputStream转换为对象[{}]异常", clazz, e);
        }
    }


    /**
     * 获取包含有指定属性的Element
     */
    public static List<Element> getElementsByAttribute(Element element, String attribute, String attributeValue) {
        var elements = new ArrayList<Element>();

        var queue = new LinkedList<Element>();
        queue.add(element);
        while (!queue.isEmpty()) {
            var ele = queue.poll();

            var eleClassName = ele.getAttribute(attribute);
            if (StringUtils.isNotEmpty(eleClassName) && eleClassName.equalsIgnoreCase(attributeValue)) {
                elements.add(ele);
                continue;
            }

            var eles = DomUtils.getChildElements(ele);
            if (CollectionUtils.isNotEmpty(eles)) {
                for (var e : eles) {
                    queue.offer(e);
                }
            }
        }

        return elements;
    }

    public static List<Element> getElementsByTag(Element element, String tag) {
        var elements = new ArrayList<Element>();

        var queue = new LinkedList<Element>();
        queue.add(element);
        while (!queue.isEmpty()) {
            var ele = queue.poll();

            var tagName = ele.getTagName();
            if (StringUtils.isNotEmpty(tagName) && tagName.equalsIgnoreCase(tag)) {
                elements.add(ele);
                continue;
            }

            var eles = DomUtils.getChildElements(ele);
            if (CollectionUtils.isNotEmpty(eles)) {
                for (var e : eles) {
                    queue.offer(e);
                }
            }
        }

        return elements;
    }

    /**
     * 只返回第一层的孩子节点，不返回第一层孩子节点的孩子节点
     * <p>
     * Retrieves all child elements of the given DOM element
     * </p>
     *
     * @param element the DOM element to analyze
     * @return a List of child {@code org.w3c.dom.Element} instances
     */
    public static List<Element> getChildElements(Element element) {
        AssertionUtils.notNull(element, "Element must not be null");
        var nodeList = element.getChildNodes();
        var childEles = new ArrayList<Element>();
        for (var i = 0; i < nodeList.getLength(); i++) {
            var node = nodeList.item(i);
            if (node instanceof Element) {
                childEles.add((Element) node);
            }
        }
        return childEles;
    }

    /**
     * Retrieves all child elements of the given DOM element that match any of the given element names.
     * Only looks at the direct child level of the given element; do not go into further depth
     * (in contrast to the DOM API's {@code getElementsByTagName} method).
     *
     * @param element           the DOM element to analyze
     * @param childElementNames the child element names to look for
     * @return a List of child {@code org.w3c.dom.Element} instances
     * @see org.w3c.dom.Element
     * @see org.w3c.dom.Element#getElementsByTagName
     */
    public static List<Element> getChildElementsByTagName(Element element, String... childElementNames) {
        AssertionUtils.notNull(element, "Element must not be null");
        AssertionUtils.notNull(childElementNames, "Element names collection must not be null");
        var childEleNameList = Arrays.asList(childElementNames);
        var childNodes = element.getChildNodes();
        var elements = new ArrayList<Element>();
        for (var i = 0; i < childNodes.getLength(); i++) {
            var node = childNodes.item(i);
            if (node instanceof Element && nodeNameMatch(node, childEleNameList)) {
                elements.add((Element) node);
            }
        }
        return elements;
    }

    public static List<Element> getChildElementsByTagName(Element ele, String childElementName) {
        return getChildElementsByTagName(ele, new String[]{childElementName});
    }

    /**
     * Utility method that returns the first child element identified by its name.
     *
     * @param ele          the DOM element to analyze
     * @param childEleName the child element name to look for
     * @return the {@code org.w3c.dom.Element} instance, or {@code null} if none found
     */
    public static Element getFirstChildElementByTagName(Element ele, String childEleName) {
        AssertionUtils.notNull(ele, "Element must not be null");
        AssertionUtils.notNull(childEleName, "Element name must not be null");
        var nodeList = ele.getChildNodes();
        for (var i = 0; i < nodeList.getLength(); i++) {
            var node = nodeList.item(i);
            if (node instanceof Element && nodeNameMatch(node, childEleName)) {
                return (Element) node;
            }
        }
        return null;
    }

    /*
     Namespace-aware equals comparison.
     */
    public static boolean nodeNameEquals(Node node, String desiredName) {
        AssertionUtils.notNull(node, "Node must not be null");
        AssertionUtils.notNull(desiredName, "Desired name must not be null");
        return nodeNameMatch(node, desiredName);
    }

    /*
     Matches the given node's name and local name against the given desired names.
     */
    private static boolean nodeNameMatch(Node node, Collection<?> desiredNames) {
        return (desiredNames.contains(node.getNodeName()) || desiredNames.contains(node.getLocalName()));
    }

    /*
     Matches the given node's name and local name against the given desired name.
     */
    private static boolean nodeNameMatch(Node node, String desiredName) {
        return (desiredName.equals(node.getNodeName()) || desiredName.equals(node.getLocalName()));
    }

}
