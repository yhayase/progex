/*** In The Name of Allah ***/
package progex.graphs.cfg;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Class type of Control Flow (CF) nodes.
 * 
 * @author Seyed Mohammad Ghaffarian
 */
public class CFNode {
	
	private Map<String, Object> properties;
	
	public CFNode() {
		properties = new LinkedHashMap<>();
	}
	
	public void setLineOfCode(int line) {
		properties.put("line", line);
	}
	
	public int getLineOfCode() {
		return (Integer) properties.get("line");
	}
	
	public void setCode(String code) {
		properties.put("code", code);
	}
	
	public String getCode() {
		return (String) properties.get("code");
	}
	
	public void setProperty(String key, Object value) {
		properties.put(key.toLowerCase(), value);
	}
	
	public Object getProperty(String key) {
		return properties.get(key.toLowerCase());
	}
	
	public Set<String> getAllKeys() {
		return properties.keySet();
	}
	
	@Override
	public String toString() {
		return (Integer) properties.get("line") + ": " + 
				(String) properties.get("code");
	}
}