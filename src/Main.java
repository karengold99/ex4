import java.io.*;
import java.io.PrintWriter;
import java_cup.runtime.Symbol;
import ast.*;
import cfg.*;
import ir.*;
import java.util.List;

public class Main
{
	static public void main(String argv[])
	{
		Lexer l;
		Parser p;
		Symbol s;
		AstDecList ast;
		FileReader fileReader;
		PrintWriter fileWriter;
		String inputFileName = argv[0];
		String outputFileName = argv[1];

		try
		{
			/********************************/
			/* [1] Initialize a file reader */
			/********************************/
			fileReader = new FileReader(inputFileName);

			/********************************/
			/* [2] Initialize a file writer */
			/********************************/
			fileWriter = new PrintWriter(outputFileName);

			/******************************/
			/* [3] Initialize a new lexer */
			/******************************/
			l = new Lexer(fileReader);

			/*******************************/
			/* [4] Initialize a new parser */
			/*******************************/
			p = new Parser(l, fileWriter);

			/***********************************/
			/* [5] 3 ... 2 ... 1 ... Parse !!! */
			/***********************************/
			ast = (AstDecList) p.parse().value;

			/*************************/
			/* [6] Print the AST ... */
			/*************************/
			// ast.printMe();

			/**************************/
			/* [7] Semant the AST ... */
			/**************************/
			ast.semantMe();

			/**********************/
			/* [8] IR the AST ... */
			/**********************/
			ast.irMe();

			/*************************************/
			/* [8.5] Optional: Print IR for debug */
			/*************************************/
			if (System.getenv("DEBUG_IR") != null) {
				try {
					PrintWriter irWriter = new PrintWriter("ir_debug.txt");
					Ir.getInstance().printIR(irWriter);
					irWriter.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			/*************************************/
			/* [8.6] Build Control Flow Graph    */
			/*************************************/
			List<IrCommand> irList = Ir.getInstance().getAllCommands();
			ControlFlowGraph cfg = ControlFlowGraph.build(irList);
			
			/*************************************/
			/* [8.7] Optional: Print CFG for debug */
			/*************************************/
			if (System.getenv("DEBUG_CFG") != null) {
				cfg.printCFG();
				
				// Also save DOT format
				try {
					PrintWriter dotWriter = new PrintWriter("cfg_debug.dot");
					dotWriter.print(cfg.toDot());
					dotWriter.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			/*************************************/
			/* [8.8] Data Flow Analysis          */
			/*************************************/
			dfa.UninitializedAnalyzer analyzer = new dfa.UninitializedAnalyzer(cfg);
			java.util.Set<String> uninitialized = analyzer.analyze();

			if (uninitialized.isEmpty()) {
				fileWriter.println("!OK");
			} else {
				for (String var : uninitialized) {
					fileWriter.println(var);
				}
			}

			/**************************/
			/* [9] Close output file */
			/**************************/
			fileWriter.close();

			/*************************************/
			/* [10] Finalize AST GRAPHIZ DOT file */
			/*************************************/
			AstGraphviz.getInstance().finalizeFile();
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}


