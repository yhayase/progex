/*** In The Name of Allah ***/
package ghaffarian.progex.java;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.BitSet;

import ghaffarian.nanologger.Logger;
import ghaffarian.progex.graphs.pdg.ControlDependenceGraph;
import ghaffarian.progex.graphs.pdg.DataDependenceGraph;
import ghaffarian.progex.graphs.pdg.ProgramDependeceGraph;
import ghaffarian.progex.java.parser.JavaLexer;
import ghaffarian.progex.java.parser.JavaParser;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
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

	private static class ParserAndLexerListener implements ANTLRErrorListener {
		private int numErrors = 0;
		@Override
		public void syntaxError(Recognizer<?, ?> recognizer, Object o, int i, int i1, String s, RecognitionException e) {
			numErrors += 1;
		}

		@Override
		public void reportAmbiguity(Parser parser, DFA dfa, int i, int i1, boolean b, BitSet bitSet, ATNConfigSet atnConfigSet) {
			// nop
		}

		@Override
		public void reportAttemptingFullContext(Parser parser, DFA dfa, int i, int i1, BitSet bitSet, ATNConfigSet atnConfigSet) {
			// nop
		}

		@Override
		public void reportContextSensitivity(Parser parser, DFA dfa, int i, int i1, int i2, ATNConfigSet atnConfigSet) {
			// nop
		}

		public int getNumErrors() {
			return numErrors;
		}
	};

	/**
	 * Builds and returns Program Dependence Graphs (PDG) for each given Java file.
	 */
	public static ProgramDependeceGraph[] buildForAll(File[] javaFiles) throws IOException {
		Logger.info("Parsing all source files ... ");
		ParseTree[] parseTrees = new ParseTree[javaFiles.length];
		CommonTokenStream[] tokenStreams = new CommonTokenStream[javaFiles.length];
		int numErrorFiles = 0;
		for (int i = 0; i < javaFiles.length; ++i) {
			Logger.info("Parsing " + javaFiles[i].getPath());

			InputStream inFile = new FileInputStream(javaFiles[i]);
			ANTLRInputStream input = new ANTLRInputStream(inFile);
			JavaLexer lexer = new JavaLexer(input);
			var listener = new ParserAndLexerListener();
			lexer.addErrorListener(listener);
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			tokenStreams[i] = tokens;
			JavaParser parser = new JavaParser(tokens);
			parser.addErrorListener(listener);
			var tmpParseTree = parser.compilationUnit();
			// parser.getNumberOfSyntaxErrors() is insufficient for detecting syntax errors,
			// since a tokenizing error causes no parsing errors in some case.
			if (listener.getNumErrors()==0) {
				parseTrees[i] = tmpParseTree;
			} else {
				Logger.error("Error on parsing " + javaFiles[i].getPath());
				numErrorFiles += 1;
			}
		}
		if (numErrorFiles>0) {
			final int newLength = javaFiles.length - numErrorFiles;
			File[] newJavaFiles = new File[newLength];
			ParseTree[] newParseTrees = new ParseTree[newLength];
			CommonTokenStream[] newTokenStreams = new CommonTokenStream[newLength];
			int nullCount = 0;
			for (int i = 0; i < javaFiles.length; ++i) {
				if (parseTrees[i] == null) {
					nullCount += 1;
				} else {
					newJavaFiles[i-nullCount] = javaFiles[i];
					newParseTrees[i-nullCount] = parseTrees[i];
					newTokenStreams[i-nullCount] = tokenStreams[i];
				}
			}
			assert(numErrorFiles == nullCount);

			javaFiles = newJavaFiles;
			parseTrees = newParseTrees;
			tokenStreams = newTokenStreams;
		}
		Logger.info("Done.");

		ControlDependenceGraph[] ctrlSubgraphs;
		ctrlSubgraphs = new ControlDependenceGraph[javaFiles.length];
		for (int i = 0; i < javaFiles.length; ++i) {
			Logger.info("Calculating CDG from " + javaFiles[i].getPath());
			try {
				ctrlSubgraphs[i] = JavaCDGBuilder.build(parseTrees[i], javaFiles[i], tokenStreams[i]);
			} catch(NullPointerException e) {
				Logger.error("Error on caluculating CDG from " + javaFiles[i].getPath());
				Logger.error(e);
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

