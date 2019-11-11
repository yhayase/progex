/*** In The Name of Allah ***/
package ghaffarian.progex.graphs.pdg;

import org.antlr.v4.runtime.ParserRuleContext;

import java.util.*;

/**
 * Class type of PDG nodes.
 * 
 * @author Seyed Mohammad Ghaffarian
 */
public class PDNode {

	private Map<String, Object> properties;
	private Set<String> DEFs, USEs, selfFlows;
	private List<ParserRuleContext> astNodeList = Collections.emptyList();

	public PDNode() {
		DEFs = new HashSet<>();
		USEs = new HashSet<>();
		selfFlows = new HashSet<>();
		properties = new HashMap<>();
	}
	
	public void setLineOfCode(int line) {
		properties.put("line", line);
	}
	
	public int getLineOfCode() {
		return (Integer) properties.get("line");
	}
	
	public void setCodeStr(String code) {
		properties.put("code", code);
	}
	
	public String getCodeStr() {
		return (String) properties.get("code");
	}

	public void setASTNodeList(List<ParserRuleContext> astNodeList) {
		this.astNodeList = astNodeList;
	}

	public void setASTNodeList(ParserRuleContext ... astNodeArray) {
		setASTNodeList(List.of(astNodeArray));
	}

	public List<ParserRuleContext> getASTNodeList() {
		return this.astNodeList;
	}

	public boolean addDEF(String var) {
		return DEFs.add(var);
	}
	
	public boolean hasDEF(String var) {
		return DEFs.contains(var);
	}

	public String[] getAllDEFs() {
		return DEFs.toArray(new String[DEFs.size()]);
	}
	
	public boolean addUSE(String var) {
		return USEs.add(var);
	}
	
	public boolean hasUSE(String var) {
		return USEs.contains(var);
	}
	
	public String[] getAllUSEs() {
		return USEs.toArray(new String[USEs.size()]);
	}
	
	public boolean addSelfFlow(String var) {
		return selfFlows.add(var);
	}
	
	public String[] getAllSelfFlows() {
		return selfFlows.toArray(new String[selfFlows.size()]);
	}
	
	public void setProperty(String key, Object value) {
		properties.put(key.toLowerCase(), value);
	}
	
	public Object getProperty(String key) {
		return properties.get(key.toLowerCase());
	}
	
	public Set<String> getAllProperties() {
		return properties.keySet();
	}
	
	@Override
	public String toString() {
		int line = (Integer) properties.get("line");
		String code = (String) properties.get("code");
		return (line + ": " + code);
	}
}
