package org.miness.xmllib;

import java.io.*;
import java.util.*;

import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

@SuppressWarnings({"unchecked"})
public class XMLDocument {
    
    private Map<String, Object> map;
    private String value;
    private String root;
    private int layer;

    public XMLDocument() {};
    
    private XMLDocument(Map<String, Object> map, String root) {
        int dotPos = root.indexOf(".") == -1 ? root.length() : root.indexOf(".");
    	this.root = root.substring(0, dotPos);
    	this.map = map;
    }
    
    public XMLDocument parse(File file, String xpathString) {
        try {
        	Document doc = new SAXBuilder().build(file);
        	Element rootElement = (xpathString == null) ? doc.getRootElement() : (Element) XPath.selectSingleNode(doc.getRootElement(), xpathString);
        	map = putValues(rootElement);
        	root = rootElement.getName();
        }
        catch (JDOMException e) {
            System.out.println("Error while parsing XML!");
        }
        catch (IOException e) {
            System.out.println("Error while reading file!");
        }
        return this;
    }
    
    public XMLDocument parse(File file) {
        return parse(file, null);
    }
    
    public XMLDocument parse(Map<String, Object> map) {
    	for (String key : map.keySet()) {
    		root = key;
    	}
    	this.map = (Map<String, Object>) map.get(root);
    	return this;
    }
    
    public XMLDocument parse(XMLElement element) {
        for (String key : element.getMap().keySet()) {
            root = key;
        }
    	this.map = (Map<String, Object>) element.getMap().get(root);
    	return this;
    }
    
    public XMLDocument get(String value) {
        XMLDocument doc = this;
    	String[] values = value.split("/");
    	if (!value.equals("")) {
    	    for (String val : values) {
                Object mapValue = doc.getRawMap().get(val);
                if (mapValue instanceof Map) {
                    doc = new XMLDocument((Map<String, Object>)mapValue, val);
                }
                else if (mapValue instanceof String) {
                    doc.value = (String) mapValue;
                }
                else {
                    System.out.println("Error while reading element '" + val + "'. Jumping to next element if available.");
                }
            }
            return doc;
    	}
    	else {
    	    return this;
    	}
    }
    
    public XMLDocument get(String value, int index) {
        StringBuilder dots = new StringBuilder();
        while (index > 0) {
            dots.append(".");
            index--;
        }
        return get(value + dots);
    }
    
    public XMLDocument add(String key, Object value) {
    	while (map.containsKey(key)) {
        	key += ".";
        }
		if (value instanceof String) {
			map.put(key, value);
		}
		else if (value instanceof XMLElement){
			map.put(key, ((XMLElement) value).getMap());
		}
		return this;
    }
    
    public int count() {
        return map.keySet().size();
    }
    
    public int count(String value) {
        int count = 0;
        while (map.containsKey(value)) {
            count++;
            value += ".";
        }
        return count;
    }
    
    public String getXML() {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
        xml.append("<" + root + ">\n");
        layer++;
    	writeXML(map, xml);
    	xml.append("</" + root + ">\n");
    	layer--;
    	return xml.toString();
    }
    
    public Map<String, Object> getMap() {
        Map<String, Object> tempMap = new LinkedHashMap<String, Object>();
        tempMap.put(root, map);
    	return tempMap;
    }
    
    public String getRootName() {
        return root;
    }
    
    public String getString() {
        if (value != null && !value.equals("")) {
            return value;
        }
        else {
            System.out.println("Calling getString() on an element which has no value. Returning null.");
            return null;
        }
    }
    
    public Integer getInteger() {
        Integer i = null;
        if (value != null && !value.equals("")) {
            try {
                i = Integer.parseInt(value);
            }
            catch (NumberFormatException e) {
                System.out.println("Value '" + value + "' is not an integer. Returning null.");
            }
        }
        else {
            System.out.println("Calling getInteger() on an element which has no value. Returning null.");
        }
        return i;
    }

    public Boolean getBoolean() {
        Boolean b = null;
        if (value != null && !value.equals("")) {
            try {
                b = Boolean.parseBoolean(value);
            }
            catch (NumberFormatException e) {
                System.out.println("Value '" + value + "' is not a boolean. Returning null.");
            }
        }
        else {
            System.out.println("Calling getBoolean() on an element which has no value. Returning null.");
        }
        return b;
    }
    
    private static Map<String, Object> putValues(Element element) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        List<Element> list = element.getChildren();
        for (Element e : list) {
            if (e.getChildren().size() == 0) {
                map.put(appendFlag(e.getName(), map), e.getText());
            }
            else {
                map.put(appendFlag(e.getName(), map), putValues(e));
            }
        }
        return map;
    }
    
    private void writeXML(Map<String, Object> map, StringBuilder xml) {
    	for (Map.Entry<String, Object> entry : map.entrySet()) {
    		int dotPos = entry.getKey().indexOf(".") == -1 ? entry.getKey().length() : entry.getKey().indexOf(".");
    		if (entry.getValue() instanceof Map) {
    			appendSpaces(layer, xml);
    			xml.append("<" + entry.getKey().substring(0, dotPos) + ">\n");
    			layer++;
    			writeXML((Map<String, Object>) map.get(entry.getKey()), xml);
    			appendSpaces(layer - 1, xml);
    			xml.append("</" + entry.getKey().substring(0, dotPos) + ">\n");
    			layer--;
    		}
    		else {
    			appendSpaces(layer, xml);
    			xml.append("<" + entry.getKey().substring(0, dotPos) + ">" + entry.getValue() + "</" + entry.getKey().substring(0, dotPos) + ">\n");
    		}
    	}
    }
    
    private static String appendFlag(String value, Map<String, Object> map) {
    	while (map.containsKey(value)) {
        	value += ".";
        }
        return value;
    }
    
    private void appendSpaces(int layer, StringBuilder xml) {
    	while (layer > 0) {
			xml.append("    ");
			layer--;
		}
    }
    
    private Map<String, Object> getRawMap() {
        return map;
    }

}