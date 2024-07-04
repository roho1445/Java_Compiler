import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Parse
{
  public static void main(String[] args)
  {
    //List<String> tokenList = MyScanner.scan();
    MyScanner.parser();
  }
}



class MyScanner {
  static int token_number = 0;
  static List<String> tokens = scan();

  public static void parser()
  {
    S();
    if(token_number == tokens.size())
    {
      System.out.println("Program parsed successfully");
    }
    else
    {
      System.out.println("Parse error");
      System.exit(0);
    }

  }


  static void consume()
  {
    token_number++;
  }

  static void match(String expectedVal)
  {
    if (tokens.get(token_number).equals(expectedVal))
    {
      consume();
    }
    else
    {
      System.out.println("Parse error");
      System.exit(0);
    } 
  }

  static void S()
  {
    switch(tokens.get(token_number))
    {
      case "{":
        consume();
        L();
        match("}");
        break;
      case "System.out.println":
        consume();
        match("(");
        E();
        match(")");
        match(";");
        break;
      case "if":
        consume();
        match("(");
        E();
        match(")");
        S();
        match("else");
        S();
        break;
      case "while":
        consume();
        match("(");
        E();
        match(")");
        S();
        break;
      default:
        System.out.println("Parse error");
        System.exit(0);
    }
  }

  static void L()
  {
    switch(tokens.get(token_number))
    {
      case "{":
      case "System.out.println":
      case "if":
      case "while":
        S();
        L();
        break;
      case "}":
        break; //Empty Char
      default:
        System.out.println("Parse error");
        System.exit(0);
    }
  }

  static void E()
  {
    switch(tokens.get(token_number))
    {
      case "true":
        consume();
        break;
      case "false":
        consume();
        break;
      case "!":
        consume();
        E();
        break;
      default:
        System.out.println("Parse error");
        System.exit(0);
    }
  }

  static List<String> scan()
  {
    List<String> tokens = new ArrayList<>();

    // Read input line by line until Ctrl+D (or Ctrl+Z in Windows) is pressed
    String input = "";
    BufferedReader reader = null;

    try
    {
      reader = new BufferedReader(new InputStreamReader(System.in));

      String line;
      int q = 0;
      while ((line = reader.readLine()) != null)
      {
        input += line; // Output each line read from the file
        
        if(q > 0)
        {
          input += '\n';
        }
        q++;
      }
    }
    catch (IOException e)
    {
      System.err.println("Error reading file: " + e.getMessage());
    }
    finally
    {
      try
      {
        if (reader != null)
        {
          reader.close(); // Close the BufferedReader
        }
      }
      catch (IOException e)
      {
        System.err.println("Error closing reader: " + e.getMessage());
      }
    }

    int current_state = 0; // initial state
    char nextChar;

    for (int i = 0; i < input.length() + 1; i++)
    {
      // Read the next token
      
      if (i < input.length())
      {
        nextChar = input.charAt(i);
      }
      else
      {
        nextChar = '\u0000';
      }

      switch (current_state)
      {
        case 0:
          switch(nextChar){
            case '{':
              current_state = 1;
              break;
            case '}':
              current_state = 2;
              break;
            case ';':
              current_state = 3;
              break;
            case '(':
              current_state = 4;
              break;
            case ')':
              current_state = 5;
              break;
            case '!':
              current_state = 6;
              break;
            case 'i':
              current_state = 7;
              break;
            case 't':
              current_state = 9;
              break;
            case 'e':
              current_state = 13;
              break;
            case 'w':
              current_state = 17;
              break;
            case 'f':
              current_state = 22;
              break;
            case 'S':
              current_state = 27;
              break;
            case '\n':
            case ' ':
            case '\t':
            case '\r':
            case '\u0000':
              break;
            default:
              System.out.println("Parse error");
              System.exit(0);
          }
          break;
        case 1:
          //Create and add token for '{'
          switch(nextChar){
            case '{':
              current_state = 1;
              tokens.add("{");
              break;
            case '}':
              current_state = 2;
              tokens.add("{");
              break;
            case ';':
              current_state = 3;
              tokens.add("{");
              break;
            case '(':
              current_state = 4;
              tokens.add("{");
              break;
            case ')':
              current_state = 5;
              tokens.add("{");
              break;
            case '!':
              current_state = 6;
              tokens.add("{");
              break;
            case 'i':
              current_state = 7;
              tokens.add("{");
              break;
            case 't':
              current_state = 9;
              tokens.add("{");
              break;
            case 'e':
              current_state = 13;
              tokens.add("{");
              break;
            case 'w':
              current_state = 17;
              tokens.add("{");
              break;
            case 'f':
              current_state = 22;
              tokens.add("{");
              break;
            case 'S':
              current_state = 27;
              tokens.add("{");
              break;
            case '\n':
            case ' ':
            case '\t':
            case '\r':
              break;
            case '\u0000':
              tokens.add("{");
              break;
            default:
              System.out.println("Parse error");
              System.exit(0);
          }
          break;
        case 2:
          //Create and add token for '}'
          switch(nextChar){
            case '{':
              current_state = 1;
              tokens.add("}");
              break;
            case '}':
              current_state = 2;
              tokens.add("}");
              break;
            case ';':
              current_state = 3;
              tokens.add("}");
              break;
            case '(':
              current_state = 4;
              tokens.add("}");
              break;
            case ')':
              current_state = 5;
              tokens.add("}");
              break;
            case '!':
              current_state = 6;
              tokens.add("}");
              break;
            case 'i':
              current_state = 7;
              tokens.add("}");
              break;
            case 't':
              current_state = 9;
              tokens.add("}");
              break;
            case 'e':
              current_state = 13;
              tokens.add("}");
              break;
            case 'w':
              current_state = 17;
              tokens.add("}");
              break;
            case 'f':
              current_state = 22;
              tokens.add("}");
              break;
            case 'S':
              current_state = 27;
              tokens.add("}");
              break;
            case '\n':
            case ' ':
            case '\t':
            case '\r':
              break;
            case '\u0000':
              tokens.add("}");
              break;
            default:
              System.out.println("Parse error");
              System.exit(0);
          }
          break;
        case 3:
          //Create and add token for ';'
          switch(nextChar){
            case '{':
              current_state = 1;
              tokens.add(";");
              break;
            case '}':
              current_state = 2;
              tokens.add(";");
              break;
            case ';':
              current_state = 3;
              tokens.add(";");
              break;
            case '(':
              current_state = 4;
              tokens.add(";");
              break;
            case ')':
              current_state = 5;
              tokens.add(";");
              break;
            case '!':
              current_state = 6;
              tokens.add(";");
              break;
            case 'i':
              current_state = 7;
              tokens.add(";");
              break;
            case 't':
              current_state = 9;
              tokens.add(";");
              break;
            case 'e':
              current_state = 13;
              tokens.add(";");
              break;
            case 'w':
              current_state = 17;
              tokens.add(";");
              break;
            case 'f':
              current_state = 22;
              tokens.add(";");
              break;
            case 'S':
              current_state = 27;
              tokens.add(";");
              break;
            case '\n':
            case ' ':
            case '\t':
            case '\r':
              break;
            case '\u0000':
              tokens.add(";");
              break;
            default:
              System.out.println("Parse error");
              System.exit(0);
          }
          break;
        case 4:
          //Create and add token for '('
          switch(nextChar){
            case '{':
              current_state = 1;
              tokens.add("(");
              break;
            case '}':
              current_state = 2;
              tokens.add("(");
              break;
            case ';':
              current_state = 3;
              tokens.add("(");
              break;
            case '(':
              current_state = 4;
              tokens.add("(");
              break;
            case ')':
              current_state = 5;
              tokens.add("(");
              break;
            case '!':
              current_state = 6;
              tokens.add("(");
              break;
            case 'i':
              current_state = 7;
              tokens.add("(");
              break;
            case 't':
              current_state = 9;
              tokens.add("(");
              break;
            case 'e':
              current_state = 13;
              tokens.add("(");
              break;
            case 'w':
              current_state = 17;
              tokens.add("(");
              break;
            case 'f':
              current_state = 22;
              tokens.add("(");
              break;
            case 'S':
              current_state = 27;
              tokens.add("(");
              break;
            case '\n':
            case ' ':
            case '\t':
            case '\r':
              break;
            case '\u0000':
              tokens.add("(");
              break;
            default:
              System.out.println("Parse error");
              System.exit(0);
          }
          break;
        case 5:
          //Create and add token for ')'
          switch(nextChar){
            case '{':
              current_state = 1;
              tokens.add(")");
              break;
            case '}':
              current_state = 2;
              tokens.add(")");
              break;
            case ';':
              current_state = 3;
              tokens.add(")");
              break;
            case '(':
              current_state = 4;
              tokens.add(")");
              break;
            case ')':
              current_state = 5;
              tokens.add(")");
              break;
            case '!':
              current_state = 6;
              tokens.add(")");
              break;
            case 'i':
              current_state = 7;
              tokens.add(")");
              break;
            case 't':
              current_state = 9;
              tokens.add(")");
              break;
            case 'e':
              current_state = 13;
              tokens.add(")");
              break;
            case 'w':
              current_state = 17;
              tokens.add(")");
              break;
            case 'f':
              current_state = 22;
              tokens.add(")");
              break;
            case 'S':
              current_state = 27;
              tokens.add(")");
              break;
            case '\n':
            case ' ':
            case '\t':
            case '\r':
              break;
            case '\u0000':
              tokens.add(")");
              break;
            default:
              System.out.println("Parse error");
              System.exit(0);
          }
          break;
        case 6:
          //Create and add token for '!'
          switch(nextChar){
            case '{':
              current_state = 1;
              tokens.add("!");
              break;
            case '}':
              current_state = 2;
              tokens.add("!");
              break;
            case ';':
              current_state = 3;
              tokens.add("!");
              break;
            case '(':
              current_state = 4;
              tokens.add("!");
              break;
            case ')':
              current_state = 5;
              tokens.add("!");
              break;
            case '!':
              current_state = 6;
              tokens.add("!");
              break;
            case 'i':
              current_state = 7;
              tokens.add("!");
              break;
            case 't':
              current_state = 9;
              tokens.add("!");
              break;
            case 'e':
              current_state = 13;
              tokens.add("!");
              break;
            case 'w':
              current_state = 17;
              tokens.add("!");
              break;
            case 'f':
              current_state = 22;
              tokens.add("!");
              break;
            case 'S':
              current_state = 27;
              tokens.add("!");
              break;
            case '\n':
            case ' ':
            case '\t':
            case '\r':
              break;
            case '\u0000':
              tokens.add("!");
              break;
            default:
              System.out.println("Parse error");
              System.exit(0);
          }
          break;
        case 7:
          switch(nextChar){
            case 'f':
              current_state = 8;
              break;
            default:
              System.out.println("Parse error");
              System.exit(0);
          }
          break;
        case 8:
          //Create and add token for 'if'
          switch(nextChar){
            case '{':
              current_state = 1;
              tokens.add("if");
              break;
            case '}':
              current_state = 2;
              tokens.add("if");
              break;
            case ';':
              current_state = 3;
              tokens.add("if");
              break;
            case '(':
              current_state = 4;
              tokens.add("if");
              break;
            case ')':
              current_state = 5;
              tokens.add("if");
              break;
            case '!':
              current_state = 6;
              tokens.add("if");
              break;
            case 'i':
              current_state = 7;
              tokens.add("if");
              break;
            case 't':
              current_state = 9;
              tokens.add("if");
              break;
            case 'e':
              current_state = 13;
              tokens.add("if");
              break;
            case 'w':
              current_state = 17;
              tokens.add("if");
              break;
            case 'f':
              current_state = 22;
              tokens.add("if");
              break;
            case 'S':
              current_state = 27;
              tokens.add("if");
              break;
            case '\n':
            case ' ':
            case '\t':
            case '\r':
              break;
            case '\u0000':
              tokens.add("if");
              break;
            default:
              System.out.println("Parse error");
              System.exit(0);
          }
          break;
        case 9:
          switch(nextChar){
            case 'r':
              current_state = 10;
              break;
            default:
              System.out.println("Parse error");
              System.exit(0);
          }
          break;
        case 10:
          switch(nextChar){
            case 'u':
              current_state = 11;
              break;
            default:
              System.out.println("Parse error");
              System.exit(0);
          }
          break;
        case 11:
          switch(nextChar){
            case 'e':
              current_state = 12;
              break;
            default:
              System.out.println("Parse error");
              System.exit(0);
          }
          break;
        case 12:
          //Create and add token for 'true'
          switch(nextChar){
            case '{':
              current_state = 1;
              tokens.add("true");
              break;
            case '}':
              current_state = 2;
              tokens.add("true");
              break;
            case ';':
              current_state = 3;
              tokens.add("true");
              break;
            case '(':
              current_state = 4;
              tokens.add("true");
              break;
            case ')':
              current_state = 5;
              tokens.add("true");
              break;
            case '!':
              current_state = 6;
              tokens.add("true");
              break;
            case 'i':
              current_state = 7;
              tokens.add("true");
              break;
            case 't':
              current_state = 9;
              tokens.add("true");
              break;
            case 'e':
              current_state = 13;
              tokens.add("true");
              break;
            case 'w':
              current_state = 17;
              tokens.add("true");
              break;
            case 'f':
              current_state = 22;
              tokens.add("true");
              break;
            case 'S':
              current_state = 27;
              tokens.add("true");
              break;
            case '\n':
            case ' ':
            case '\t':
            case '\r':
              break;
            case '\u0000':
              tokens.add("true");
              break;
            default:
              System.out.println("Parse error");
              System.exit(0);
          }
          break;
        case 13:
          switch(nextChar){
            case 'l':
              current_state = 14;
              break;
            default:
              System.out.println("Parse error");
              System.exit(0);
          }
          break;
        case 14:
          switch(nextChar){
            case 's':
              current_state = 15;
              break;
            default:
              System.out.println("Parse error");
              System.exit(0);
          }
          break;
        case 15:
          switch(nextChar){
            case 'e':
              current_state = 16;
              break;
            default:
              System.out.println("Parse error");
              System.exit(0);
          }
          break;
        case 16:
          //Create and add token for 'else'
          switch(nextChar){
            case '{':
              current_state = 1;
              tokens.add("else");
              break;
            case '}':
              current_state = 2;
              tokens.add("else");
              break;
            case ';':
              current_state = 3;
              tokens.add("else");
              break;
            case '(':
              current_state = 4;
              tokens.add("else");
              break;
            case ')':
              current_state = 5;
              tokens.add("else");
              break;
            case '!':
              current_state = 6;
              tokens.add("else");
              break;
            case 'i':
              current_state = 7;
              tokens.add("else");
              break;
            case 't':
              current_state = 9;
              tokens.add("else");
              break;
            case 'e':
              current_state = 13;
              tokens.add("else");
              break;
            case 'w':
              current_state = 17;
              tokens.add("else");
              break;
            case 'f':
              current_state = 22;
              tokens.add("else");
              break;
            case 'S':
              current_state = 27;
              tokens.add("else");
              break;
            case '\n':
            case ' ':
            case '\t':
            case '\r':
              break;
            case '\u0000':
              tokens.add("else");
              break;
            default:
              System.out.println("Parse error");
              System.exit(0);
          }
          break;
        case 17:
          switch(nextChar){
            case 'h':
              current_state = 18;
              break;
            default:
              System.out.println("Parse error");
              System.exit(0);
          }
          break;
        case 18:
          switch(nextChar){
            case 'i':
              current_state = 19;
              break;
            default:
              System.out.println("Parse error");
              System.exit(0);
          }
          break;
        case 19:
          switch(nextChar){
            case 'l':
              current_state = 20;
              break;
            default:
              System.out.println("Parse error");
              System.exit(0);
          }
          break;
        case 20:
          switch(nextChar){
            case 'e':
              current_state = 21;
              break;
            default:
              System.out.println("Parse error");
              System.exit(0);
          }
          break;
        case 21:
          //Create and add token for 'while'
          switch(nextChar){
            case '{':
              tokens.add("while");
              current_state = 1;
              break;
            case '}':
              tokens.add("while");
              current_state = 2;
              break;
            case ';':
              tokens.add("while");
              current_state = 3;
              break;
            case '(':
              current_state = 4;
              tokens.add("while");
              break;
            case ')':
              current_state = 5;
              tokens.add("while");
              break;
            case '!':
              current_state = 6;
              tokens.add("while");
              break;
            case 'i':
              current_state = 7;
              tokens.add("while");
              break;
            case 't':
              current_state = 9;
              tokens.add("while");
              break;
            case 'e':
              current_state = 13;
              tokens.add("while");
              break;
            case 'w':
              current_state = 17;
              tokens.add("while");
              break;
            case 'f':
              current_state = 22;
              tokens.add("while");
              break;
            case 'S':
              current_state = 27;
              tokens.add("while");
              break;
            case '\n':
            case ' ':
            case '\t':
            case '\r':
              break;
            case '\u0000':
              tokens.add("while");
              break;
            default:
              System.out.println("Parse error");
              System.exit(0);
          }
          break;
        case 22:
          switch(nextChar){
            case 'a':
              current_state = 23;
              break;
            default:
              System.out.println("Parse error");
              System.exit(0);
          }
          break;
        case 23:
          switch(nextChar){
            case 'l':
              current_state = 24;
              break;
            default:
              System.out.println("Parse error");
              System.exit(0);
          }
          break;
        case 24:
          switch(nextChar){
            case 's':
              current_state = 25;
              break;
            default:
              System.out.println("Parse error");
              System.exit(0);
          }
          break;
        case 25:
          switch(nextChar){
            case 'e':
              current_state = 26;
              break;
            default:
              System.out.println("Parse error");
              System.exit(0);
          }
          break;
        case 26:
          //Create and add token for 'false'
          switch(nextChar){
            case '{':
              tokens.add("false");
              current_state = 1;
              break;
            case '}':
              tokens.add("false");
              current_state = 2;
              break;
            case ';':
              tokens.add("false");
              current_state = 3;
              break;
            case '(':
              tokens.add("false");
              current_state = 4;
              break;
            case ')':
              tokens.add("false");
              current_state = 5;
              break;
            case '!':
              tokens.add("false");
              current_state = 6;
              break;
            case 'i':
              tokens.add("false");
              current_state = 7;
              break;
            case 't':
              tokens.add("false");
              current_state = 9;
              break;
            case 'e':
              tokens.add("false");
              current_state = 13;
              break;
            case 'w':
              tokens.add("false");
              current_state = 17;
              break;
            case 'f':
              tokens.add("false");
              current_state = 22;
              break;
            case 'S':
              tokens.add("false");
              current_state = 27;
              break;
            case '\n':
            case ' ':
            case '\t':
            case '\r':
              break;
            case '\u0000':
              tokens.add("false");
              break;
            default:
              System.out.println("Parse error");
              System.exit(0);
          }
          break;
        case 27:
          switch(nextChar){
            case 'y':
              current_state = 28;
              break;
            default:
              System.out.println("Parse error");
              System.exit(0);
          }
          break;
        case 28:
          switch(nextChar){
            case 's':
              current_state = 29;
              break;
            default:
              System.out.println("Parse error");
              System.exit(0);
          }
          break;
        case 29:
          switch(nextChar){
            case 't':
              current_state = 30;
              break;
            default:
              System.out.println("Parse error");
              System.exit(0);
          }
          break;
        case 30:
          switch(nextChar){
            case 'e':
              current_state = 31;
              break;
            default:
              System.out.println("Parse error");
              System.exit(0);
          }
          break;
        case 31:
          switch(nextChar){
            case 'm':
              current_state = 32;
              break;
            default:
              System.out.println("Parse error");
              System.exit(0);
          }
          break;
        case 32:
          switch(nextChar){
            case '.':
              current_state = 33;
              break;
            default:
              System.out.println("Parse error");
              System.exit(0);
          }
          break;
        case 33:
          switch(nextChar){
            case 'o':
              current_state = 34;
              break;
            default:
              System.out.println("Parse error");
              System.exit(0);
          }
          break;
        case 34:
          switch(nextChar){
            case 'u':
              current_state = 35;
              break;
            default:
              System.out.println("Parse error");
              System.exit(0);
          }
          break;
        case 35:
          switch(nextChar){
            case 't':
              current_state = 36;
              break;
            default:
              System.out.println("Parse error");
              System.exit(0);
          }
          break;
        case 36:
          switch(nextChar){
            case '.':
              current_state = 37;
              break;
            default:
              System.out.println("Parse error");
              System.exit(0);
          }
          break;
        case 37:
          switch(nextChar){
            case 'p':
              current_state = 38;
              break;
            default:
              System.out.println("Parse error");
              System.exit(0);
          }
          break;
        case 38:
          switch(nextChar){
            case 'r':
              current_state = 39;
              break;
            default:
              System.out.println("Parse error");
              System.exit(0);
          }
          break;
        case 39:
          switch(nextChar){
            case 'i':
              current_state = 40;
              break;
            default:
              System.out.println("Parse error");
              System.exit(0);
          }
          break;
        case 40:
          switch(nextChar){
            case 'n':
              current_state = 41;
              break;
            default:
              System.out.println("Parse error");
              System.exit(0);
          }
          break;
        case 41:
          switch(nextChar){
            case 't':
              current_state = 42;
              break;
            default:
              System.out.println("Parse error");
              System.exit(0);
          }
          break;
        case 42:
          switch(nextChar){
            case 'l':
              current_state = 43;
              break;
            default:
              System.out.println("Parse error");
              System.exit(0);
          }
          break;
        case 43:
          switch(nextChar){
            case 'n':
              current_state = 44;
              break;
            default:
              System.out.println("Parse error");
              System.exit(0);
          }
          break;
        case 44:
          //Create and add token for 'System.out.println'
          switch(nextChar){
            case '{':
              tokens.add("System.out.println");
              current_state = 1;
              break;
            case '}':
              tokens.add("System.out.println");
              current_state = 2;
              break;
            case ';':
              tokens.add("System.out.println");
              current_state = 3;
              break;
            case '(':
              tokens.add("System.out.println");
              current_state = 4;
              break;
            case ')':
              tokens.add("System.out.println");
              current_state = 5;
              break;
            case '!':
              tokens.add("System.out.println");
              current_state = 6;
              break;
            case 'i':
              tokens.add("System.out.println");
              current_state = 7;
              break;
            case 't':
              tokens.add("System.out.println");
              current_state = 9;
              break;
            case 'e':
              tokens.add("System.out.println");
              current_state = 13;
              break;
            case 'w':
              tokens.add("System.out.println");
              current_state = 17;
              break;
            case 'f':
              tokens.add("System.out.println");
              current_state = 22;
              break;
            case 'S':
              tokens.add("System.out.println");
              current_state = 27;
              break;
            case '\n':
            case ' ':
            case '\t':
            case '\r':
              break;
            case '\u0000':
              tokens.add("System.out.println");
              break;
            default:
              System.out.println("Parse error");
              System.exit(0);
          }
          break;
        default:
          System.out.println("Parse error");
          System.exit(0);

      } 

      
      
    } //End of State switch

    /* 
    for (String element : tokens)
    {
      System.out.println(element);
    }
    */

    //System.out.println(tokens.size());
    return tokens;

  }// End of Method

}//End of Class



