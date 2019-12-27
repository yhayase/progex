/*** In The Name of Allah ***/
package ghaffarian.progex.java;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

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
		CommonTokenStream[] tokenStreams = new CommonTokenStream[javaFiles.length];
		int numErrorFiles = 0;
		for (int i = 0; i < javaFiles.length; ++i) {
			InputStream inFile = new FileInputStream(javaFiles[i]);
			ANTLRInputStream input = new ANTLRInputStream(inFile);
			JavaLexer lexer = new JavaLexer(input);
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			tokenStreams[i] = tokens;
			JavaParser parser = new JavaParser(tokens);
			if (parser.getNumberOfSyntaxErrors()==0) {
				parseTrees[i] = parser.compilationUnit();
			} else {
				numErrorFiles+=1;
			}
		}
		if (numErrorFiles>0) {
			File[] newJavaFiles = new File[javaFiles.length - numErrorFiles];
			ParseTree[] newParseTrees = new ParseTree[javaFiles.length - numErrorFiles];
			int nullCount = 0;
			for (int i = 0; i < javaFiles.length; ++i) {
				if (parseTrees[i] == null) {
					nullCount += 1;
				} else {
					newJavaFiles[i-nullCount] = javaFiles[i];
					newParseTrees[i-nullCount] = parseTrees[i];
				}
			}
			assert(numErrorFiles == nullCount);

			javaFiles = newJavaFiles;
			parseTrees = newParseTrees;
		}
		Logger.info("Done.");

		ControlDependenceGraph[] ctrlSubgraphs;
		ctrlSubgraphs = new ControlDependenceGraph[javaFiles.length];
		for (int i = 0; i < javaFiles.length; ++i) {
			try {
				ctrlSubgraphs[i] = JavaCDGBuilder.build(parseTrees[i], javaFiles[i], tokenStreams[i]);
			} catch(NullPointerException e) {
				// ctrlSubgraphs[i] remains null.
			}
		}
        //
		DataDependenceGraph[] dataSubgraphs;
		dataSubgraphs = JavaDDGBuilder.buildForAll(parseTrees, javaFiles, tokenStreams);
        //
		// Join the subgraphs into PDGs
		ProgramDependeceGraph[] pdgArray = new ProgramDependeceGraph[javaFiles.length];
		for (int i = 0; i < javaFiles.length; ++i) {
			if (ctrlSubgraphs[i] != null && dataSubgraphs[i]!=null) {
				pdgArray[i] = new ProgramDependeceGraph(javaFiles[i].getName(),
						ctrlSubgraphs[i], dataSubgraphs[i]);
			}
		}

		return Arrays.stream(pdgArray).filter(e-> e!=null).toArray(ProgramDependeceGraph[]::new);
	}

}

