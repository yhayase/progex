/*** In The Name of Allah ***/
package ghaffarian.progex.graphs.pdg;

import ghaffarian.graphs.Edge;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.Map;

import ghaffarian.progex.utils.StringUtils;
import ghaffarian.nanologger.Logger;
import ghaffarian.progex.graphs.AbstractProgramGraph;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;
import java.util.Map.Entry;

/**
 * Control Dependence Graph.
 * 
 * @author Seyed Mohammad Ghaffarian
 */
public class ControlDependenceGraph extends AbstractProgramGraph<PDNode, CDEdge> {
	
	private final File srcFile;
	public final CommonTokenStream tokens;
	
	public ControlDependenceGraph(File srcFile, CommonTokenStream tokens) {
		super();
		this.srcFile = srcFile;
		this.tokens = tokens;
        properties.put("label", "CDG of " + getFileName());
        properties.put("type", "Control Dependence Graph (CDG)");
	}
	
    @Override
	public void exportDOT(String outDir) throws FileNotFoundException {
        if (!outDir.endsWith(File.separator))
            outDir += File.separator;
        File outDirFile = new File(outDir);
        outDirFile.mkdirs();
		String filename = getFileName().substring(0, getFileName().indexOf('.'));
		String filepath = outDir + filename + "-PDG-CTRL.dot";
		try (PrintWriter dot = new PrintWriter(filepath, "UTF-8")) {
			dot.println("digraph " + filename + "_PDG_CTRL {");
            dot.println("  // graph-vertices");
			Map<PDNode, String> nodeNames = new LinkedHashMap<>();
			int nodeCounter = 1;
			for (PDNode node: allVertices) {
				String name = "v" + nodeCounter++;
				nodeNames.put(node, name);
				StringBuilder label = new StringBuilder("  [label=\"");
				if (node.getLineOfCode() > 0)
					label.append(node.getLineOfCode()).append(":  ");
				label.append(StringUtils.escape(node.getCodeStr())).append("\"];");
				dot.println("  " + name + label.toString());
			}
			dot.println("  // graph-edges");
			for (Edge<PDNode, CDEdge> edge: allEdges) {
				String src = nodeNames.get(edge.source);
				String trg = nodeNames.get(edge.target);
				if (edge.label.type.equals(CDEdge.Type.EPSILON))
					dot.println("  " + src + " -> " + trg + ";");
				else
					dot.println("  " + src + " -> " + trg + "  [label=\"" + edge.label.type + "\"];");
			}
			dot.println("  // end-of-graph\n}");
		} catch (UnsupportedEncodingException ex) {
			Logger.error(ex);
		}
		Logger.info("CDS of PDG exported to: " + filepath);
	}

    @Override
    public void exportGML(String outDir) throws IOException {
        if (!outDir.endsWith(File.separator))
            outDir += File.separator;
        File outDirFile = new File(outDir);
        outDirFile.mkdirs();
		String filename = getFileName().substring(0, getFileName().indexOf('.'));
		String filepath = outDir + filename + "-PDG-CTRL.gml";
		try (PrintWriter gml = new PrintWriter(filepath, "UTF-8")) {
			gml.println("graph [");
			gml.println("  directed 1");
			for (Entry<String, String> property: properties.entrySet()) {
                switch (property.getKey()) {
                    case "directed":
                        continue;
                    default:
                        gml.println("  " + property.getKey() + " \"" + property.getValue() + "\"");
                }
            }
            gml.println("  file \"" + this.getFileName() + "\"\n");
            //
			Map<PDNode, Integer> nodeIDs = new LinkedHashMap<>();
			int nodeCounter = 0;
			for (PDNode node: allVertices) {
				gml.println("  node [");
				gml.println("    id " + nodeCounter);
				gml.println("    line " + node.getLineOfCode());
				gml.println("    label \"" + StringUtils.escape(node.getCodeStr()) + "\"");
				gml.println("  ]");
				nodeIDs.put(node, nodeCounter);
				++nodeCounter;
			}
            gml.println();
            //
			int edgeCounter = 0;
			for (Edge<PDNode, CDEdge> edge: allEdges) {
				gml.println("  edge [");
				gml.println("    id " + edgeCounter);
				gml.println("    source " + nodeIDs.get(edge.source));
				gml.println("    target " + nodeIDs.get(edge.target));
				gml.println("    label \"" + edge.label.type + "\"");
				gml.println("  ]");
				++edgeCounter;
			}
			gml.println("]");
		} catch (UnsupportedEncodingException ex) {
			Logger.error(ex);
		}
		Logger.info("CDS of PDG exported to: " + filepath);
    }
	
    @Override
	public void exportJSON(String outDir) throws FileNotFoundException {
        if (!outDir.endsWith(File.separator))
            outDir += File.separator;
        File outDirFile = new File(outDir);
        outDirFile.mkdirs();
		String filename = getFileName().substring(0, getFileName().indexOf('.'));
		String filepath;
		int i=0;
		do {
			i++;
		  	filepath = outDir + filename + "-" + i + "-PDG-CTRL.json";
		}  while (new File(filepath).exists());
		try (PrintWriter json = new PrintWriter(filepath, "UTF-8")) {
			json.println("{");
			json.println("  \"directed\": true,");
			for (Entry<String, String> property: properties.entrySet()) {
                switch (property.getKey()) {
                    case "directed":
                        continue;
                    default:
                        json.println("  \"" + property.getKey() + "\": \"" + property.getValue() + "\",");
                }
            }
			json.println("  \"file\": " + StringUtils.toJsonString(getFileName()) + ",");
			json.println("  \"path\": " + StringUtils.toJsonString(srcFile.getPath()) + ",");
            //
			json.println("  \"nodes\": [");
			Map<PDNode, Integer> nodeIDs = new LinkedHashMap<>();
			int nodeCounter = 0;
			for (PDNode node: allVertices) {
				json.println("    {");
				json.println("      \"id\": " + nodeCounter + ",");
				json.println("      \"line\": " + node.getLineOfCode() + ",");
				//json.println("      \"astId\": " + node.getASTNodeList().stream().map(astNode -> "\""+astNode.hashCode()+"\"" ).collect(Collectors.joining(",")) + "],");
				if (node.getASTNodeList().size()>0) {
					json.println("      \"astId\": " + node.getASTNodeList().hashCode() + ",");
					json.print("      \"tokens\":");
					json.print(node.formatTokensToJsonArray(tokens));
					json.println(",");
				}
				var isEntryPoint = (Boolean)node.getProperty("entryPoint");
				if (isEntryPoint!=null) {
					json.println("      \"entryPoint\": " + isEntryPoint + ",");
				}
				var name = (String)node.getProperty("name");
				if (isEntryPoint!=null) {
					json.println("      \"name\": " + StringUtils.toJsonString(name) + ",");
				}
				nodeIDs.put(node, nodeCounter);
				json.println("      \"label\": " + StringUtils.toJsonString(node.getCodeStr()));
				++nodeCounter;
                if (nodeCounter == allVertices.size())
                    json.println("    }");
                else
                    json.println("    },");
			}
            //
			json.println("  ],");
			json.println();
			json.println("  \"edges\": [");
			int edgeCounter = 0;
			for (Edge<PDNode, CDEdge> edge: allEdges) {
				json.println("    {");
				json.println("      \"id\": " + edgeCounter + ",");
				json.println("      \"source\": " + nodeIDs.get(edge.source) + ",");
				json.println("      \"target\": " + nodeIDs.get(edge.target) + ",");
				json.println("      \"label\": " + StringUtils.toJsonString(edge.label.type.toString()));
				++edgeCounter;
                if (edgeCounter == allEdges.size())
                    json.println("    }");
                else
                    json.println("    },");
			}
			json.println("  ]");
			json.println("}");
		} catch (UnsupportedEncodingException ex) {
			Logger.error(ex);
		}
		Logger.info("CDS of PDG exported to: " + filepath);
	}

	private String getFileName() {
		return srcFile.getName();
	}
}
