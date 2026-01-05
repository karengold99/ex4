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

			/**************************/
			/* [6] Semant the AST ... */
			/**************************/
			ast.semantMe();

			/**********************/
			/* [7] IR the AST ... */
			/**********************/
			ast.irMe();

			/*************************************/
			/* [8] Build Control Flow Graph    */
			/*************************************/
			List<IrCommand> irList = Ir.getInstance().getAllCommands();
			ControlFlowGraph cfg = ControlFlowGraph.build(irList);
			
			/*************************************/
			/* [9] Data Flow Analysis          */
			/*************************************/
			dfa.UninitializedAnalyzer analyzer = new dfa.UninitializedAnalyzer(cfg);
			java.util.Set<String> uninitialized = analyzer.analyze();

			if (uninitialized.isEmpty()) {
				fileWriter.print("!OK");
			} else {
				fileWriter.print(String.join("\n", uninitialized));
			}

			/**************************/
			/* [10] Close output file */
			/**************************/
			fileWriter.close();

			/*************************************/
			/* [11] Finalize AST GRAPHIZ DOT file */
			/*************************************/
			AstGraphviz.getInstance().finalizeFile();
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}


