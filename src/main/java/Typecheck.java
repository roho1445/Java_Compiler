import java.util.ArrayList;
import java.util.HashMap;

import minijava.MiniJavaParser;
import minijava.syntaxtree.AllocationExpression;
import minijava.syntaxtree.AndExpression;
import minijava.syntaxtree.ArrayAllocationExpression;
import minijava.syntaxtree.ArrayAssignmentStatement;
import minijava.syntaxtree.ArrayLength;
import minijava.syntaxtree.ArrayLookup;
import minijava.syntaxtree.ArrayType;
import minijava.syntaxtree.AssignmentStatement;
import minijava.syntaxtree.BooleanType;
import minijava.syntaxtree.BracketExpression;
import minijava.syntaxtree.ClassDeclaration;
import minijava.syntaxtree.ClassExtendsDeclaration;
import minijava.syntaxtree.CompareExpression;
import minijava.syntaxtree.Expression;
import minijava.syntaxtree.ExpressionList;
import minijava.syntaxtree.ExpressionRest;
import minijava.syntaxtree.FalseLiteral;
import minijava.syntaxtree.FormalParameter;
import minijava.syntaxtree.FormalParameterList;
import minijava.syntaxtree.FormalParameterRest;
import minijava.syntaxtree.Goal;
import minijava.syntaxtree.IfStatement;
import minijava.syntaxtree.IntegerLiteral;
import minijava.syntaxtree.IntegerType;
import minijava.syntaxtree.MainClass;
import minijava.syntaxtree.MessageSend;
import minijava.syntaxtree.MethodDeclaration;
import minijava.syntaxtree.MinusExpression;
import minijava.syntaxtree.NotExpression;
import minijava.syntaxtree.PlusExpression;
import minijava.syntaxtree.PrimaryExpression;
import minijava.syntaxtree.PrintStatement;
import minijava.syntaxtree.Statement;
import minijava.syntaxtree.ThisExpression;
import minijava.syntaxtree.TimesExpression;
import minijava.syntaxtree.TrueLiteral;
import minijava.syntaxtree.Type;
import minijava.syntaxtree.TypeDeclaration;
import minijava.syntaxtree.VarDeclaration;
import minijava.syntaxtree.WhileStatement;
import minijava.visitor.GJDepthFirst;
import minijava.visitor.GJNoArguDepthFirst;

public class Typecheck {
      public static void main(String [] args) {
        Goal root;
        try {
            new MiniJavaParser(System.in);
            root = MiniJavaParser.Goal();
            SymbolTableVisitor stv = new SymbolTableVisitor("root");
            CheckVisitor cv = new CheckVisitor();
            root.accept(cv, stv);
            System.out.println("Program type checked successfully");
        }
        catch(minijava.ParseException e)
        {
            System.out.println("Type error");
            System.exit(0);
        }
     }
}


class GlobalVars { // Shared variable declaration
    private static HashMap < String, SymbolTableVisitor > classTable = new HashMap < String, SymbolTableVisitor > ();
    public static SymbolTableVisitor getSymbolTable(String className) {
        if (!classTable.containsKey(className)) {
            System.out.println("Type error");
            System.exit(0);
        }
        return classTable.get(className);
    }
    public static void addClass(String className, SymbolTableVisitor stv) {
        if (classTable.containsKey(className)) {
            System.out.println("Type error");
            System.exit(0);
        }
        classTable.put(className, stv);
    }
}

class Tuple<T, U> {
    private ArrayList<T> arrayList;
    private U string;

    public Tuple(ArrayList<T> arrayList, U string) {
        this.arrayList = arrayList;
        this.string = string;
    }

    public ArrayList<T> getParam() {
        return arrayList;
    }

    public U getType() {
        return string;
    }
    
    // You can also add setters if needed
}
class SymbolTableVisitor extends GJNoArguDepthFirst < String > {
    private HashMap < String, String > varTable;
    private HashMap < String, Tuple<String, String>> methodTable;
    private SymbolTableVisitor parentSymbolTable;
    private String scopeName;

    public SymbolTableVisitor() {
        scopeName = null;
        varTable = new HashMap < String, String > ();
        methodTable = new HashMap < String, Tuple<String, String>> ();
        parentSymbolTable = null;
    }

    public SymbolTableVisitor(SymbolTableVisitor parentScope) {
        varTable = new HashMap < String, String > ();
        methodTable = new HashMap < String, Tuple<String, String>> ();
        parentSymbolTable = parentScope;
        scopeName = null;
    }
    public SymbolTableVisitor(String name) {
        scopeName = name;
        varTable = new HashMap < String, String > ();
        methodTable = new HashMap < String, Tuple<String, String>> ();
        parentSymbolTable = null;
    }
    public SymbolTableVisitor(String name, SymbolTableVisitor parentScope) {
        scopeName = name;
        varTable = new HashMap < String, String > ();
        methodTable = new HashMap < String, Tuple<String, String>> ();
        parentSymbolTable = parentScope;
    }
    public String getScopeName() {
        if(scopeName == null)
        {
            return parentSymbolTable.getScopeName();
        }
        return scopeName;
    }
    public HashMap < String, String > getVarTable() {
        return varTable;
    }
    public HashMap < String, Tuple<String, String>> getMethodTable() {
        return methodTable;
    }

    public void setParentTable(String parent){
        if(GlobalVars.getSymbolTable(parent).isChildOf(scopeName)) //take care of cycles
        {
            System.out.println("Type error");
            System.exit(0);
        }
        parentSymbolTable = GlobalVars.getSymbolTable(parent);
    }

    public Tuple<String, String> getMethod(String method){
        if(methodTable.containsKey(method))
        {
            return methodTable.get(method);
        }
        if(parentSymbolTable == null)
        {
            return null;
        }
        return parentSymbolTable.getMethod(method);
    }

    public String getVarType(String varName) {
        String classVarType = varTable.get(varName);
        if (classVarType != null) {
            return classVarType;
        }
        if (parentSymbolTable == null) {
            System.out.println("Type error");
            System.exit(0);
        }
        return parentSymbolTable.getVarType(varName);
    }
    public boolean isChildOf(String parentName) {
        if(parentName.equals("int") || parentName.equals("bool") || parentName.equals("int[]"))
        {
            return false;
        }
        if (scopeName.equals(parentName)) {
            return true;
        }
        if (parentSymbolTable == null) {
            return false;
        }
        return parentSymbolTable.isChildOf(parentName);
    }

    public String visit(MainClass x) {
        String className = x.f1.f0.toString();
        this.scopeName = className;
        // add class global variables to symbol table
        for (Integer i = 0; i < x.f14.size(); i++) {
            x.f14.elementAt(i).accept(this);
        }
        GlobalVars.addClass(className, this);
        return "OK";
    }
    

    public String visit(ClassDeclaration x) {
        String className = x.f1.f0.toString();
        this.scopeName = className;
        // add class global variables to symbol table
        for (Integer i = 0; i < x.f3.size(); i++) {
            x.f3.elementAt(i).accept(this);
        }
        GlobalVars.addClass(className, this);

        //Accept Method Declarations
        for (Integer i = 0; i < x.f4.size(); i++) {
            x.f4.elementAt(i).accept(this);
        }
        return "OK";
    }

    public String visit(ClassExtendsDeclaration x) {
        String className = x.f1.f0.toString();
        this.scopeName = className;
        // add class global variables to symbol table
        for (Integer i = 0; i < x.f5.size(); i++) {
            x.f5.elementAt(i).accept(this);
        }
        GlobalVars.addClass(className, this); //Store class along with symbol table
        for (Integer i = 0; i < x.f6.size(); i++) //Accept methods declarations
        {
            x.f6.elementAt(i).accept(this);
        }
        return "OK";
    }

    public String visit(MethodDeclaration n) {
        String type = n.f1.accept(this);
        String methodName = n.f2.accept(this);
        ArrayList<String> methodParams = n.f4.accept(new ParamArrayResolver(), this);
        if (methodTable.containsKey(methodName)) {
            System.out.println("Type error");
            System.exit(0);
        }
        else {
            Tuple <String,String> parentMethod = this.getMethod(methodName);
            if(parentMethod != null)
            {
                String parentReturnType = parentMethod.getType();
                ArrayList<String> parentParams = parentMethod.getParam();
                if(parentReturnType != null && (!parentReturnType.equals(type) || !parentParams.equals(methodParams)))
                {
                    System.out.println("Type error");
                    System.exit(0);
                }
            }
            methodTable.put(methodName, new Tuple<String, String>(methodParams, type));
        }
        return "OK";
    }

    public String visit(FormalParameter x) {
        return x.f0.accept(this);
    }
    public String visit(FormalParameterRest x) {
        return x.f1.accept(this);
    }
    public String visit(VarDeclaration n) {
        String type = n.f0.accept(this);
        String id = n.f1.accept(this);
        if (varTable.containsKey(id)) {
            System.out.println("Type error");
            System.exit(0);
        }
        varTable.put(id, type);
        return "OK";
    }
    public String visit(Type a) {
        return a.f0.accept(this);
    }
    public String visit(ArrayType a) {
        return "int[]";
    }
    public String visit(BooleanType a) {
        return "bool";
    }
    public String visit(IntegerType a) {
        return "int";
    }
    public String visit(minijava.syntaxtree.Identifier a) {
        return a.f0.toString();
    }
}

class ClassTableExtender extends GJNoArguDepthFirst <String >
{
    public String visit(ClassExtendsDeclaration x)
    {
        String className = x.f1.f0.toString();
        String parentName = x.f3.f0.toString();
        GlobalVars.getSymbolTable(className).setParentTable(parentName);
        return "OK";
    }
    public String visit(ClassDeclaration x)
    {
        return "OK";
    }
}

class ParamArrayResolver extends GJDepthFirst <ArrayList<String>, SymbolTableVisitor>
{
    public ArrayList<String> visit(FormalParameterList x, SymbolTableVisitor stv) {
        ArrayList<String> methodParams = new ArrayList<String>();
        methodParams.add(x.f0.accept(stv));
        for (Integer i = 0; i < x.f1.size(); i++) //Accept methods
        {
            methodParams.add(x.f1.elementAt(i).accept(stv));
        }
        return methodParams;
    }

    public ArrayList<String> visit(ExpressionList e, SymbolTableVisitor table) {
        ArrayList<String> callParams = new ArrayList<String>();
        CheckVisitor cv = new CheckVisitor();
        callParams.add(e.f0.accept(cv, table));
        for (Integer i = 0; i < e.f1.size(); i++) {
            callParams.add(e.f1.elementAt(i).accept(cv, table));
        }
        return callParams;
    }
}

class MethodParamVarAdder extends GJDepthFirst <String, SymbolTableVisitor>
{
    public String visit(FormalParameterList x, SymbolTableVisitor table) {
        x.f0.accept(this, table);
        for (Integer i = 0; i < x.f1.size(); i++) //Accept methods
        {
            x.f1.elementAt(i).accept(this, table);
        }
        return "OK";
    }
    public String visit(FormalParameter x, SymbolTableVisitor table) {
        String id = x.f1.accept(table);
        String type = x.f0.accept(table);
        if(table.getVarTable().containsKey(id))
        {
            System.out.println("Type error");
            System.exit(0);
        }
        table.getVarTable().put(id,type);
        return "OK";
    }
    public String visit(FormalParameterRest x, SymbolTableVisitor table) {
        return x.f1.accept(this, table);
    }
}



class CheckVisitor extends GJDepthFirst < String, SymbolTableVisitor > {
    public String visit(Goal x, SymbolTableVisitor table) {
        for (Integer i = 0; i < x.f1.size(); i++) //Create class tables
        {
            x.f1.elementAt(i).accept(new SymbolTableVisitor());
        }
        //Take care of class extensions
        for (Integer i = 0; i < x.f1.size(); i++) 
        {
            x.f1.elementAt(i).accept(new ClassTableExtender());
        }
        
        x.f0.accept(new SymbolTableVisitor()); //Accept MainClass

        x.f0.accept(this, table); //Accept MainClass in Depth
        for (Integer i = 0; i < x.f1.size(); i++) //Accept Type Declarations statements in depth
        {
            x.f1.elementAt(i).accept(this, table);
        }
        return "OK";
    }
    public String visit(MainClass x, SymbolTableVisitor table) {
        String className = x.f1.f0.toString();
        SymbolTableVisitor stv = GlobalVars.getSymbolTable(className);
        // type check methods statements
        for (Integer i = 0; i < x.f15.size(); i++) {
            x.f15.elementAt(i).accept(this, stv);
        }
        return "OK";
    }
    public String visit(TypeDeclaration x, SymbolTableVisitor table) {
        x.f0.accept(this, table);
        return "OK";
    }
    public String visit(ClassDeclaration x, SymbolTableVisitor table) {
        String className = x.f1.f0.toString();
        SymbolTableVisitor stv = GlobalVars.getSymbolTable(className);
        //Accept Method Statements
        for (Integer i = 0; i < x.f4.size(); i++) {
            x.f4.elementAt(i).accept(this, stv);
        }
        return "OK";
    }
    public String visit(ClassExtendsDeclaration x, SymbolTableVisitor table) {
        String className = x.f1.f0.toString();
        SymbolTableVisitor stv = GlobalVars.getSymbolTable(className);
        for (Integer i = 0; i < x.f6.size(); i++) //Accept methods statements
        {
            x.f6.elementAt(i).accept(this, stv);
        }
        return "OK";
    }
    public String visit(MethodDeclaration x, SymbolTableVisitor table) {
        //Create new symbol table for method and combine with over-arching class table
        SymbolTableVisitor stv = new SymbolTableVisitor(table);
        MethodParamVarAdder paramStv = new MethodParamVarAdder();

        x.f4.accept(paramStv, stv); // add parameters to symbol table
        // add local vars to symbol table
        for (Integer i = 0; i < x.f7.size(); i++) {
            x.f7.elementAt(i).accept(stv);
        }
        //check that return val type is subtype of defined return type
        String retValType = x.f10.accept(this, stv);
        String methodType = x.f1.accept(this, stv);
        if (retValType != methodType) {
            if (!GlobalVars.getSymbolTable(retValType).isChildOf(methodType)) {
                System.out.println("Type error");
                System.exit(0);
            }
        }
        // type check method statements
        for (Integer i = 0; i < x.f8.size(); i++) {
            x.f8.elementAt(i).accept(this, stv);
        }
        return "OK";
    }
    /* 
    public String visit(FormalParameterList x, SymbolTableVisitor table) {
        String methodParams = "";
        methodParams += x.f0.accept(this, table);
        for (Integer i = 0; i < x.f1.size(); i++) //Accept methods
        {
            methodParams += x.f1.elementAt(i).accept(this, table);
        }
        return methodParams;
    }
    public String visit(FormalParameter x, SymbolTableVisitor table) {
        return x.f0.accept(this, table);
    }
    public String visit(FormalParameterRest x, SymbolTableVisitor table) {
        return x.f1.accept(this, table);
    }
    */
    public String visit(Statement e, SymbolTableVisitor table) {
        return e.f0.accept(this, table);
    }
    public String visit(AssignmentStatement s, SymbolTableVisitor table) {
        String t1 = s.f0.accept(this, table);
        String t2 = s.f2.accept(this, table);
        if (t1 != t2) {
            if (!GlobalVars.getSymbolTable(t2).isChildOf(t1)) { //check subtype
                System.out.println("Type error");
                System.exit(0);
            }
        }
        return "OK";
    }
    public String visit(PrintStatement p, SymbolTableVisitor table) {
        String t = p.f2.accept(this, table);
        if (!t.equals("int")) {
            System.out.println("Type error");
            System.exit(0);
        }
        return "OK";
    }
    public String visit(WhileStatement w, SymbolTableVisitor table) {
        String t = w.f2.accept(this, table);
        if (!t.equals("bool")) {
            System.out.println("Type error");
            System.exit(0);
        }
        return "OK";
    }
    public String visit(IfStatement I, SymbolTableVisitor table) {
        String t1 = I.f2.accept(this, table);
        if (!t1.equals("bool")) {
            System.out.println("Type error");
            System.exit(0);
        }
        I.f4.accept(this, table);
        I.f6.accept(this, table);
        return "OK";
    }
    public String visit(minijava.syntaxtree.Block b, SymbolTableVisitor table) {
        for (Integer i = 0; i < b.f1.size(); i++) {
            b.f1.elementAt(i).accept(this, table);
        }
        return "OK";
    }
    public String visit(ArrayAssignmentStatement a, SymbolTableVisitor table) {
        String t0 = a.f0.accept(this, table);
        if (!t0.equals("int[]")) {
            System.out.println("Type error");
            System.exit(0);
        }
        String t2 = a.f2.accept(this, table);
        if (!t2.equals("int")) {
            System.out.println("Type error");
            System.exit(0);
        }
        String t5 = a.f5.accept(this, table);
        if (!t5.equals("int")) {
            System.out.println("Type error");
            System.exit(0);
        }
        return "OK";
    }
    public String visit(Expression e, SymbolTableVisitor table) {
        return e.f0.accept(this, table);
    }
    public String visit(PrimaryExpression e, SymbolTableVisitor table) {
        return e.f0.accept(this, table);
    }
    public String visit(IntegerLiteral e, SymbolTableVisitor table) {
        return "int";
    }
    public String visit(TrueLiteral e, SymbolTableVisitor table) {
        return "bool";
    }
    public String visit(FalseLiteral e, SymbolTableVisitor table) {
        return "bool";
    }
    public String visit(ThisExpression e, SymbolTableVisitor table) {
        return table.getScopeName();
    }
    public String visit(ArrayAllocationExpression e, SymbolTableVisitor table) {
        String t3 = e.f3.accept(this, table);
        if (!t3.equals("int")) {
            System.out.println("Type error");
            System.exit(0);
        }
        return "int[]";
    }
    public String visit(AllocationExpression e, SymbolTableVisitor table) {
        return e.f1.f0.toString();
    }
    public String visit(NotExpression e, SymbolTableVisitor table) {
        String t1 = e.f1.accept(this, table);
        if (!t1.equals("bool")) {
            System.out.println("Type error");
            System.exit(0);
        }
        return "bool";
    }
    public String visit(BracketExpression e, SymbolTableVisitor table) {
        return e.f1.accept(this, table);
    }
    public String visit(AndExpression a, SymbolTableVisitor table) {
        String t0 = a.f0.accept(this, table);
        String t2 = a.f2.accept(this, table);
        if (!t0.equals("bool") || !t2.equals("bool")) {
            System.out.println("Type error");
            System.exit(0);
        }
        return "bool";
    }
    public String visit(PlusExpression e, SymbolTableVisitor table) {
        String t1 = e.f0.accept(this, table);
        String t2 = e.f2.accept(this, table);
        if (!t1.equals("int") || !t2.equals("int")) {
            System.out.println("Type error");
            System.exit(0);
        }
        return "int";
    }
    public String visit(MinusExpression e, SymbolTableVisitor table) {
        String t1 = e.f0.accept(this, table);
        String t2 = e.f2.accept(this, table);
        if (!t1.equals("int") || !t2.equals("int")) {
            System.out.println("Type error");
            System.exit(0);
        }
        return "int";
    }
    public String visit(CompareExpression e, SymbolTableVisitor table) {
        String t1 = e.f0.accept(this, table);
        String t2 = e.f2.accept(this, table);
        if (!t1.equals("int") || !t2.equals("int")) {
            System.out.println("Type error");
            System.exit(0);
        }
        return "bool";
    }
    public String visit(TimesExpression e, SymbolTableVisitor table) {
        String t1 = e.f0.accept(this, table);
        String t2 = e.f2.accept(this, table);
        if (!t1.equals("int") || !t2.equals("int")) {
            System.out.println("Type error");
            System.exit(0);
        }
        return "int";
    }
    public String visit(ArrayLookup e, SymbolTableVisitor table) {
        String t1 = e.f0.accept(this, table);
        String t2 = e.f2.accept(this, table);
        if (!t1.equals("int[]") || !t2.equals("int")) {
            System.out.println("Type error");
            System.exit(0);
        }
        return "int";
    }
    public String visit(ArrayLength e, SymbolTableVisitor table) {
        String t1 = e.f0.accept(this, table);
        if (!t1.equals("int[]")) {
            System.out.println("Type error");
            System.exit(0);
        }
        return "int";
    }
    public String visit(MessageSend n, SymbolTableVisitor table) {
        String className = n.f0.accept(this, table);
        String methodName = n.f2.f0.toString();
        ArrayList<String> callParams = n.f4.accept(new ParamArrayResolver(), table);
        Tuple<String, String> calledMethod = GlobalVars.getSymbolTable(className).getMethod(methodName);
        if(calledMethod == null)
        {
            System.out.println("Type error");
            System.exit(0);
        }
        String type = calledMethod.getType();
        if(type == null)
        {
            System.out.println("Type error");
            System.exit(0);
        }
        if((callParams == null && calledMethod.getParam() != null) || (callParams != null && calledMethod.getParam() == null ))
        {
            System.out.println("Type error");
            System.exit(0);
        }

        if(callParams != null && calledMethod.getParam() != null)
        {
            if(callParams.size() != calledMethod.getParam().size())
            {
                System.out.println("Type error");
                System.exit(0);
            }
    
            for (int i = 0; i < callParams.size(); i++)
            {
                String methodParamDef = calledMethod.getParam().get(i);
                String argDef = callParams.get(i);
                if(!methodParamDef.equals(argDef))
                {
                    if(!GlobalVars.getSymbolTable(argDef).isChildOf(methodParamDef))
                    {
                        System.out.println("Type error");
                        System.exit(0);
                    }
                }
            }
        }

        return type;
    }
    /* 
    public String visit(ExpressionList e, SymbolTableVisitor table) {
        String callParams = "";
        callParams += e.f0.accept(this, table);
        for (Integer i = 0; i < e.f1.size(); i++) {
            callParams += e.f1.elementAt(i).accept(this, table);
        }
        return callParams;
    }
       */
    public String visit(ExpressionRest e, SymbolTableVisitor table) {
        return e.f1.accept(this, table);
    }
 
    public String visit(minijava.syntaxtree.Identifier a, SymbolTableVisitor table) {
        // no logic for if not in the table
        return table.getVarType(a.f0.toString());
    }
    public String visit(Type a, SymbolTableVisitor table) {
        return a.f0.accept(table);
    }
}