/*** In The Name of Allah ***/
package ghaffarian.progex.java;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import ghaffarian.nanologger.Logger;
import ghaffarian.progex.graphs.pdg.ControlDependenceGraph;
import ghaffarian.progex.graphs.pdg.DataDependenceGraph;
import ghaffarian.progex.graphs.pdg.ProgramDependeceGraph;
import ghaffarian.progex.java.parser.JavaLexer;
import ghaffarian.progex.java.parser.JavaParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * Program Dependence Graph (PDG) builder for Java programs.
 * A Java parser generated via ANTLRv4 is used for this purpose.
 * This implementation is based on ANTLRv4's Visitor pattern.
 * 
 * @author Seyed Mohammad Ghaffarian
 */
public class JavaPDGBuilder {
	
	/**
	 * Builds and returns Program Dependence Graphs (PDG) for each given Java file.
	 */
	public static ProgramDependeceGraph[] buildForAll(String[] javaFilePaths) throws IOException {
		File[] javaFiles = new File[javaFilePaths.length];
		for (int i = 0; i < javaFiles.length; ++i)
			javaFiles[i] = new File(javaFilePaths[i]);
		return buildForAll(javaFiles);
	}
	
	/**
	 * Builds and returns Program Dependence Graphs (PDG) for each given Java file.
	 */
	public static ProgramDependeceGraph[] buildForAll(File[] javaFiles) throws IOException {
		Logger.info("Parsing all source files ... ");
		ParseTree[] parseTrees = new ParseTree[javaFiles.length];
		for (int i = 0; i < javaFiles.length; ++i) {
			InputStream inFile = new FileInputStream(javaFiles[i]);
			ANTLRInputStream input = new ANTLRInputStream(inFile);
			JavaLexer lexer = new JavaLexer(input);
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			JavaParser parser = new JavaParser(tokens);
			parseTrees[i] = parser.compilationUnit();
		}
		Logger.info("Done.");

		ControlDependenceGraph[] ctrlSubgraphs;
		ctrlSubgraphs = new ControlDependenceGraph[javaFiles.length];
		for (int i = 0; i < javaFiles.length; ++i)
			ctrlSubgraphs[i] = JavaCDGBuilder.build(parseTrees[i], javaFiles[i]);
        //
		DataDependenceGraph[] dataSubgraphs;
		dataSubgraphs = JavaDDGBuilder.buildForAll(parseTrees, javaFiles);
        //
		// Join the subgraphs into PDGs
		ProgramDependeceGraph[] pdgArray = new ProgramDependeceGraph[javaFiles.length];
		for (int i = 0; i < javaFiles.length; ++i) {
			pdgArray[i] = new ProgramDependeceGraph(javaFiles[i].getName(), 
					ctrlSubgraphs[i], dataSubgraphs[i]);
		}
		
		return pdgArray;
	}

}

