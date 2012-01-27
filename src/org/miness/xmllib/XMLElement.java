package org.miness.xmllib;

import java.util.LinkedHashMap;
import java.util.Map;

public class XMLElement {
	
	private Map<String, Object> map = new LinkedHashMap<String, Object>();
	
	public XMLElement add(String key, Object value) {
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
	
	public Map<String, Object> getMap() {
		return map;
	}
	
}