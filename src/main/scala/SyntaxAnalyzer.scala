

/**
  * Created by kostkinaoksana on 5/19/17.
  */

object SyntaxAnalyzer {
  def apply(lexemes: Seq[Lexeme]): SyntaxAnalyzer = new SyntaxAnalyzer(lexemes)
}

class SyntaxAnalyzer(lexemes: Seq[Lexeme]) {

  def analyze: Either[ErrorSyntax, Program] = {

    def findProgram(input: Seq[Lexeme]): Either[ErrorSyntax, Program] = {
      input match {
        case Keyword(_, "PROGRAM") +: (i@Identifier(_, _)) +: Delimiter(_, ";") +: tail =>
          findBlock(tail).map(x => Program(ProcedureIdentifier(i), x))

        case _ =>
          Left(ErrorSyntaxException("Syntax error at program declaration"))

      }
    }

    def findBlock(input: Seq[Lexeme]): Either[ErrorSyntax, Block] = {
      input.splitAt(input.indexOf(Keyword(403, "BEGIN"))) match {
        case (declaration, Keyword(403, "BEGIN") +: statements :+ Keyword(_, "END")) =>
          findStatementsList(statements) match {
            case Right(st) =>
              findDeclarations(declaration).map(x => Block(x, st))
            case Left(e) => Left(e)
          }
        case _ =>
          Left(ErrorSyntaxException("Unexpected position of BEGIN and END"))
      }
    }
  
    def findStatementsList(input: Seq[Lexeme]): Either[ErrorSyntax, StatementsList] = {
      input match {
        case Nil =>
          Right(StatementsList(Left(EmptySyntaxStructure)))
      
        case _ =>
          findStatement(input) match {
            case (Left(e), _) => Left(e)
            case (Right(f), tail) =>
              findStatementsList(tail).map(fl => StatementsList(Right((f, fl))))
          }
      }
    
    
    }
  
    def findStatement(input: Seq[Lexeme]): (Either[ErrorSyntax, Statement], Seq[Lexeme]) = {
      input match {
        case (i1: Identifier) +: Delimiter(_, "=") +: (i2: Identifier) +: Delimiter(_, "+") +: (i3: Identifier) +: Delimiter(59, ";") +: tail =>
          (Right(Statement(i1, i2, i3)), tail)
        case _ =>
          (Left(ErrorSyntaxException("Syntax error at statement declaration")), input)
      }
    
    }
    
    
    
    def findDeclarations(input: Seq[Lexeme]): Either[ErrorSyntax, Declarations] = {
      findMathFunctionDeclaration(input).map(x => Declarations(x))
    }

    def findMathFunctionDeclaration(input: Seq[Lexeme]): Either[ErrorSyntax, MathFunctionDeclaration] = {
      input match {
        case Keyword(_, "DEFFUNC") +: tail =>
          findFunctionList(tail).map(x => MathFunctionDeclaration(Right(x)))

        case Nil =>
          Right(MathFunctionDeclaration(Left(EmptySyntaxStructure)))

        case _ =>
          Left(ErrorSyntaxException("Syntax error at function list declaration"))
      }
    }

    def findFunctionList(input: Seq[Lexeme]): Either[ErrorSyntax, FunctionList] = {
      input match {
        case Nil =>
          Right(FunctionList(Left(EmptySyntaxStructure)))

        case _ =>
          findFunction(input) match {
            case (Left(e), _) => Left(e)
            case (Right(f), tail) =>
              findFunctionList(tail).map(fl => FunctionList(Right((f, fl))))
          }
      }


    }

    def findFunction(input: Seq[Lexeme]): (Either[ErrorSyntax, Function], Seq[Lexeme]) = {
      input.splitAt(input.indexOf(Delimiter(59, ";")) + 1) match {
        case ((i: Identifier) +: Delimiter(_, "=") +: (ui: UnsignedInteger) +: funcChar :+ Delimiter(59, ";"), tail) =>
          (findFunctionChar(funcChar).map(fc => Function(FunctionIdentifier(i), Constant(ui), fc)), tail)
        case (_, tail) =>
          (Left(ErrorSyntaxException("Syntax error at function declaration")), tail)
      }

    }



    def findFunctionChar(input: Seq[Lexeme]): Either[ErrorSyntax, FunctionCharacteristic] = {
      input match {
        case Delimiter(_, "\\") +: (ui1: UnsignedInteger) +: Delimiter(_, ",") +: (ui2: UnsignedInteger) +: Nil =>
          Right(FunctionCharacteristic(ui1, ui2))

        case _ =>
          Left(ErrorSyntaxException("Syntax error at function characteristic declaration"))
      }
    }



    findProgram(lexemes)

  }

}
