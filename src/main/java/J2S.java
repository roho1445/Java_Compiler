import java.util.ArrayList;
import java.util.HashMap;

import IR.token.FunctionName;
import IR.token.Identifier;
import IR.token.Label;
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
import sparrow.Add;
import sparrow.Alloc;
import sparrow.Block;
import sparrow.Call;
import sparrow.ErrorMessage;
import sparrow.FunctionDecl;
import sparrow.Goto;
import sparrow.IfGoto;
import sparrow.Instruction;
import sparrow.LabelInstr;
import sparrow.LessThan;
import sparrow.Load;
import sparrow.Move_Id_FuncName;
import sparrow.Move_Id_Id;
import sparrow.Move_Id_Integer;
import sparrow.Multiply;
import sparrow.Print;
import sparrow.Program;
import sparrow.Store;
import sparrow.Subtract;


public class J2S{
    public static void main(String [] args) {
        Goal root;
        try {
            new MiniJavaParser(System.in);
            root = MiniJavaParser.Goal();
            Program program = new Program(new ArrayList<FunctionDecl>());
            root.accept(new GoalVisitor(), program);
            System.out.println(program.toString());
        }
        catch(minijava.ParseException e)
        {
            System.out.println("Type error");
            System.exit(0);
        }
    }
}

class ClassObjectDataStructure {
    public ArrayList<String> methods = new ArrayList<String>();
    public ArrayList<String> classVars = new ArrayList<String>();
    public ArrayList<String> methodCallNames = new ArrayList<String>();
}

class classTables {
    public static HashMap<String, ClassObjectDataStructure> tables = new HashMap<String, ClassObjectDataStructure>();
}

class classTableResolver extends GJNoArguDepthFirst <String> {
    public String visit(TypeDeclaration x) {
        return x.f0.accept(this);
    }
    public String visit(ClassDeclaration x) {
        String className = x.f1.f0.toString();
        ClassObjectDataStructure classTable = new ClassObjectDataStructure();

        //Accept variables into table
        for (Integer i = 0; i < x.f3.size(); i++) {
            classTable.classVars.add(x.f3.elementAt(i).accept(this));
        }

        //Accept Methods
        for (Integer i = 0; i < x.f4.size(); i++) {
            String methodName = x.f4.elementAt(i).accept(this);
            classTable.methods.add(methodName);
            classTable.methodCallNames.add(className + methodName);
        }

        ResolvedClasses.resolvedClasses.add(className);
        classTables.tables.put(className, classTable);
        return null;
    }

    public String visit(ClassExtendsDeclaration x) {
        String className = x.f1.f0.toString();
        ClassObjectDataStructure classTable = new ClassObjectDataStructure();
        
        //Accept variables into table
        for (Integer i = 0; i < x.f5.size(); i++) {
            classTable.classVars.add(x.f5.elementAt(i).accept(this));
        }

        //Accept Methods
        for (Integer i = 0; i < x.f6.size(); i++) {
            String methodName = x.f6.elementAt(i).accept(this);
            classTable.methods.add(methodName);
            classTable.methodCallNames.add(className + methodName);
        }

        classTables.tables.put(className, classTable);
        return null;
    }

    public String visit(MethodDeclaration x) {
        return x.f2.f0.toString();
    }

    public String visit(VarDeclaration x) {
        return x.f1.f0.toString();
    }
}

class NameHandler {
    private static int varNum = 0;
    private static int labelNum = 0;

    public static String generateVarName()
    {
        String retString = "v" + varNum;
        varNum++;
        return retString;
    }
    public static String generateLabelName()
    {
        String retString = "l" + labelNum;
        labelNum++;
        return retString;
    }
}

class NoneClassVars
{
    public static ArrayList<String> scopeVars;
}

class MethodVisitor extends GJDepthFirst < String, FunctionDecl > {

    public String visit(MethodDeclaration x, FunctionDecl f) {
        //Add to var-class mapping
        for (Integer i = 0; i < x.f7.size(); i++) {
            x.f7.elementAt(i).accept(new VarDecResolver());
        }

        //Initialize formal parameter list and add to it
        f.formalParameters = new ArrayList<IR.token.Identifier>();
        f.formalParameters.add(new IR.token.Identifier("this"));
        x.f4.accept(this, f); // add parameter names to list

        NoneClassVars.scopeVars = new ArrayList<>();
        for(int i = 1; i < f.formalParameters.size(); i++)
        {
            NoneClassVars.scopeVars.add(f.formalParameters.get(i).toString());
        }

        //add method vars to scope
        for (Integer i = 0; i < x.f7.size(); i++) {
            NoneClassVars.scopeVars.add(x.f7.elementAt(i).accept(this, f));
        }


        //Translate lines into instructions and add to block
        f.block = new sparrow.Block();
        f.block.parent = f;
        f.block.instructions = new ArrayList<Instruction>();
        for (Integer i = 0; i < x.f8.size(); i++) {
            x.f8.elementAt(i).accept(new InstructionVisitor(), f.block);
        }

        //Add return id
        f.block.return_id = x.f10.accept(new InstructionVisitor(), f.block);

        //Update name of FunctionDecl by adding method name to class name
        String methodName = x.f2.f0.toString();
        f.functionName.name += methodName;

        return "OK";
    }

    public String visit(FormalParameterList x, FunctionDecl f)
    {
        f.formalParameters.add(new IR.token.Identifier(x.f0.accept(this, f)));
        for (Integer i = 0; i < x.f1.size(); i++) 
        {
            f.formalParameters.add(new IR.token.Identifier(x.f1.elementAt(i).accept(this, f)));
        }
        return "OK";
    }

    public String visit(FormalParameter x, FunctionDecl f)
    {
        x.accept(new VarDecResolver());
        return x.f1.f0.toString();
    }

    public String visit(FormalParameterRest x, FunctionDecl f)
    {
        return x.f1.accept(this, f);
    }

    public String visit(VarDeclaration x, FunctionDecl f)
    {
        return x.f1.f0.toString();
    }
}

class classTableExtender extends GJNoArguDepthFirst <String> {
    public String visit(TypeDeclaration x) {

        return x.f0.accept(this);
    }
    public String visit(ClassDeclaration x) {
        return null;
    }

    public String visit(ClassExtendsDeclaration x) {
        String className = x.f1.f0.toString();
        String parentClassName = x.f3.f0.toString();

        if(!ResolvedClasses.resolvedClasses.contains(parentClassName) || ResolvedClasses.resolvedClasses.contains(className))
        {
            return null;
        }

        ClassObjectDataStructure classTable = classTables.tables.get(className);
        ClassObjectDataStructure parentTable = classTables.tables.get(parentClassName);
        
        //Update vars in table
        ArrayList<String> parentVars = parentTable.classVars;
        for (Integer i = parentVars.size() - 1; i >= 0 ; i--) {
            /*
            String varToAdd = parentVars.get(i);
            if(classTable.classVars.contains(varToAdd))
            {
                classTable.classVars.remove(classTable.classVars.indexOf(varToAdd));
            }
            classTable.classVars.add(0, varToAdd);
            */


            String varToAdd = parentVars.get(i);
            if(classTable.classVars.contains(varToAdd))
            {
                int intToRemove = classTable.classVars.indexOf(varToAdd);
                classTable.classVars.remove(intToRemove);
                classTable.classVars.add(0, varToAdd);
            }
            else
            {
                classTable.classVars.add(0, varToAdd);
            }
        }

        //Update methods in table
        ArrayList<String> parentMethods = parentTable.methods;
        for (Integer i = parentMethods.size() - 1; i >= 0; i--) {
            String methodToAdd = parentMethods.get(i);
            if(classTable.methods.contains(methodToAdd))
            {
                int intToRemove = classTable.methods.indexOf(methodToAdd);
                classTable.methodCallNames.remove(intToRemove);
                classTable.methods.remove(intToRemove);

                classTable.methods.add(0, methodToAdd);
                classTable.methodCallNames.add(0, className + methodToAdd);
            }
            else
            {
                classTable.methods.add(0, methodToAdd);
                classTable.methodCallNames.add(0, parentTable.methodCallNames.get(i));
            }
        }

        
        classTables.tables.put(className, classTable);
        ResolvedClasses.resolvedClasses.add(className);
        return null;
    }

    public String visit(MethodDeclaration x) {
        return x.f2.f0.toString();
    }

    public String visit(VarDeclaration x) {
        return x.f1.f0.toString();
    }
}

class ResolvedClasses{
    public static ArrayList<String> resolvedClasses = new ArrayList<String>();
}


class GoalVisitor extends GJDepthFirst <IR.token.Identifier, Program> {
    public IR.token.Identifier visit(Goal x, Program p) {
        for (Integer i = 0; i < x.f1.size(); i++) //Create class symbol tables
        {
            x.f1.elementAt(i).accept(new SymbolTableVisitor());
        }
        //First build classTables before translating
        for (Integer i = 0; i < x.f1.size(); i++) 
        {
            x.f1.elementAt(i).accept(new classTableResolver());
        }
        //Handle extensions
        while(ResolvedClasses.resolvedClasses.size() != x.f1.size())
        {
            for (Integer i = 0; i < x.f1.size(); i++) 
            {
                x.f1.elementAt(i).accept(new classTableExtender());
            }
        }
        x.f0.accept(this, p); //Accept MainClass and translate main function
        for (Integer i = 0; i < x.f1.size(); i++) //translate class functions
        {
            x.f1.elementAt(i).accept(this, p);
        }

        return null;
    }
    public IR.token.Identifier visit(MainClass x, Program p) {
        for (Integer i = 0; i < x.f14.size(); i++) {
            x.f14.elementAt(i).accept(new VarDecResolver());
        }
        FunctionDecl func = new FunctionDecl();
        func.functionName = new FunctionName("Main");
        func.parent = p;
        func.formalParameters = new ArrayList<IR.token.Identifier>();
        func.block = new sparrow.Block();
        func.block.parent = func;
        func.block.return_id = new IR.token.Identifier(NameHandler.generateVarName());
        func.block.instructions = new ArrayList<Instruction>();

        // Accept Main method statements into block
        for (Integer i = 0; i < x.f15.size(); i++) {
            x.f15.elementAt(i).accept(new InstructionVisitor(), func.block);
        }

        p.funDecls.add(func);
        return null;
    }

    public IR.token.Identifier visit(TypeDeclaration x, Program p) {
        return x.f0.accept(this, p);
    }


    public IR.token.Identifier visit(ClassDeclaration x, Program p) {
        //First map vars to className
        for (Integer i = 0; i < x.f3.size(); i++) {
            x.f3.elementAt(i).accept(new VarDecResolver());
        }
        String className = x.f1.f0.toString();
        //Accept Method Statements
        for (Integer i = 0; i < x.f4.size(); i++) {
            FunctionDecl func = new FunctionDecl();
            func.parent = p;
            func.functionName = new FunctionName(className);
            x.f4.elementAt(i).accept(new MethodVisitor(), func);
            p.funDecls.add(func);
        }
        return null;
    }

    public IR.token.Identifier visit(ClassExtendsDeclaration x, Program p) {
        for (Integer i = 0; i < x.f5.size(); i++) {
            x.f5.elementAt(i).accept(new VarDecResolver());
        }
        String className = x.f1.f0.toString();
        //Accept Method Statements
        for (Integer i = 0; i < x.f6.size(); i++) {
            FunctionDecl func = new FunctionDecl();
            func.parent = p;
            func.functionName = new FunctionName(className);
            x.f6.elementAt(i).accept(new MethodVisitor(), func);
            p.funDecls.add(func);
        }
        return null;
    }
}

class InstructionVisitor extends GJDepthFirst <IR.token.Identifier, Block> {
    public IR.token.Identifier visit(Statement e, Block b) {
        return e.f0.accept(this, b);
    }

    public IR.token.Identifier visit(AssignmentStatement s, Block b) {
        IR.token.Identifier lhs = s.f0.accept(this, b);
        IR.token.Identifier rhs = s.f2.accept(this, b);
        //if lhs is classvar
        //  [classname + offset] = rhs
        //  return null
        String className = b.parent.functionName.name;
        if(!className.equals("Main") && !NoneClassVars.scopeVars.contains(lhs.toString()))
        {
            ClassObjectDataStructure classTable = classTables.tables.get(className);
            if(classTable.classVars.contains(lhs.toString()))
            {
                int ind = classTable.classVars.indexOf(lhs.toString());
                Store storeVarInstr = new Store(new Identifier("this"), 4 + (ind*4), rhs);
                b.instructions.add(storeVarInstr);
                return null;
            }
        }

        
        Move_Id_Id assignmentInstr = new Move_Id_Id(lhs, rhs);
        b.instructions.add(assignmentInstr);

        //s.accept(new IntMapVisitor(), new TupleLHSRHS(lhs, rhs));
        return lhs;
    }
    public IR.token.Identifier visit(PrintStatement p, Block b) {
        IR.token.Identifier t = p.f2.accept(this, b);
        Print printInstr = new Print(t);
        b.instructions.add(printInstr);
        return null;
    }
    public IR.token.Identifier visit(WhileStatement w, Block b) {
        //loop1:
        //t0 = translate(e)
        //if0 t0 goto end
        //translate(s)
        //goto loop1
        //end:
        Label loopLabel = new Label(NameHandler.generateLabelName()); //check on label name
        LabelInstr loopLabelInstr = new LabelInstr(loopLabel);
        b.instructions.add(loopLabelInstr);

        IR.token.Identifier condition = w.f2.accept(this, b);
        Label endLabel = new Label(NameHandler.generateLabelName());//check on label name
        IfGoto whileLoopIfInstr = new IfGoto(condition, endLabel);
        b.instructions.add(whileLoopIfInstr);

        w.f4.accept(this, b);//translate statement instructions
        Goto loopAroundInstr = new Goto(loopLabel);
        b.instructions.add(loopAroundInstr);

        LabelInstr endLoopInstr = new LabelInstr(endLabel);
        b.instructions.add(endLoopInstr);

        return null;
    }
    public IR.token.Identifier visit(IfStatement I, Block b) {
        //if0 expr goto l
            //s1
            //goto end
        //l:
            //s2
        //end:

        IR.token.Identifier condition = I.f2.accept(this, b);
        Label s2Label = new Label(NameHandler.generateLabelName()); //generate label properly
        IfGoto ifInstr = new IfGoto(condition, s2Label);
        b.instructions.add(ifInstr);

        I.f4.accept(this, b);//accept s1
        Label endLabel = new Label(NameHandler.generateLabelName()); //generate label properly
        Goto gotoEndInstr = new Goto(endLabel);
        b.instructions.add(gotoEndInstr);

        LabelInstr s1LabelInstr = new LabelInstr(s2Label);
        b.instructions.add(s1LabelInstr);

        I.f6.accept(this, b); //accept s2
        LabelInstr endLabelInstr = new LabelInstr(endLabel);
        b.instructions.add(endLabelInstr);

        return null;
    }
    public IR.token.Identifier visit(minijava.syntaxtree.Block b, Block q) {
        for (Integer i = 0; i < b.f1.size(); i++) {
            b.f1.elementAt(i).accept(this, q);
        }
        return null;
    }
    public IR.token.Identifier visit(ArrayAssignmentStatement a, Block b) {
        IR.token.Identifier arrayName = a.f0.accept(this, b);//x[a] = q
        IR.token.Identifier index = a.f2.accept(this, b);
        IR.token.Identifier rhs = a.f5.accept(this, b);
        //if array is a class variable
        //get arrayIdentifier index
        //array = [this + (4 + 4*arrayIdentifierIndex)]
        //arraylen = [array + 0]
        //minus1 = 1
        //len = len - minus1
        //comp = len < index
        //if0 comp goto l
        //error("index out")
        //l:
        //[array + 4 + 4*index] = rhs
        //[this + (4 + 4*arrayIdentifierIndex)] = arrayName
        ClassObjectDataStructure classTable = classTables.tables.get(b.parent.functionName.name);
        if(!b.parent.functionName.name.equals("Main") && !NoneClassVars.scopeVars.contains(arrayName.toString()) && classTable.classVars.contains(arrayName.toString()))
        {
            //arr = [this + calculate class table offset]
            //arrlen = [arr + 0]
            //one = 1
            //compLen = arrLen - 1
            //comp = compLen < index
            //if0 comp then l2:
                //error out of bounds
            //l2:
            //four 
            //[a+ offset] = rh

            Label nullLabel = new Label(NameHandler.generateLabelName());
            IfGoto checkNullInstr = new IfGoto(new Identifier("this"), nullLabel);
            b.instructions.add(checkNullInstr);

            Identifier four = new Identifier(NameHandler.generateVarName());
            Move_Id_Integer storeFourInstr = new Move_Id_Integer(four, 4);
            b.instructions.add(storeFourInstr);

            Integer ind = classTable.classVars.indexOf(arrayName.toString());
            Identifier heapAccessOffset = new Identifier(NameHandler.generateVarName());
            Move_Id_Integer storeFourOnHeapInstr = new Move_Id_Integer(heapAccessOffset, ind);
            b.instructions.add(storeFourOnHeapInstr);

            Multiply multiplyHeapAccessOffsetInstr = new Multiply(heapAccessOffset, four, heapAccessOffset);
            b.instructions.add(multiplyHeapAccessOffsetInstr);

            Add addHeapAccessInstr = new Add(heapAccessOffset, four, heapAccessOffset);
            b.instructions.add(addHeapAccessInstr);
            
            Identifier fakeArr = new Identifier(NameHandler.generateVarName());
            Add setArrInstr = new Add(fakeArr, new Identifier("this"), heapAccessOffset);
            b.instructions.add(setArrInstr);

            checkNullInstr = new IfGoto(fakeArr, nullLabel);
            b.instructions.add(checkNullInstr);

            Identifier arr = new Identifier(NameHandler.generateVarName());
            Load arrDereferenceInstr = new Load(arr, fakeArr, 0);
            b.instructions.add(arrDereferenceInstr);

            Identifier arrLen = new Identifier(NameHandler.generateVarName());
            Load storeArrayLenInstr = new Load(arrLen, arrayName, 0);
            b.instructions.add(storeArrayLenInstr);

            /* 
            Identifier neg1 = new Identifier(NameHandler.generateVarName());
            Move_Id_Integer neg1Instr = new Move_Id_Integer(neg1, -1);
            b.instructions.add(neg1Instr);
            */

            Identifier zero = new Identifier(NameHandler.generateVarName());
            Move_Id_Integer zeroInstr = new Move_Id_Integer(zero, 0);
            b.instructions.add(zeroInstr);

            IR.token.Identifier minus1 = new IR.token.Identifier(NameHandler.generateVarName());
            Move_Id_Integer loadMinus1Instr = new Move_Id_Integer(minus1, 1);
            b.instructions.add(loadMinus1Instr);

            Identifier negIndex = new Identifier(NameHandler.generateVarName());
            Subtract negIndexInstr = new Subtract(negIndex, zero, index);
            b.instructions.add(negIndexInstr);

            Subtract negIndexInstr2 = new Subtract(negIndex, negIndex, minus1);
            b.instructions.add(negIndexInstr2);

            Identifier negIndComp = new Identifier(NameHandler.generateVarName());
            LessThan negIndCompInstr = new LessThan(negIndComp, negIndex, zero);
            b.instructions.add(negIndCompInstr);

            Label negLabel = new Label(NameHandler.generateLabelName());
            IfGoto if0arrayName = new IfGoto(negIndComp, negLabel);
            b.instructions.add(if0arrayName);

            Subtract updateLenInstr = new Subtract(arrLen, arrLen, minus1);
            b.instructions.add(updateLenInstr);

            IR.token.Identifier comp = new IR.token.Identifier(NameHandler.generateVarName());
            LessThan lengthCompInstr = new LessThan(comp, arrLen, index);
            b.instructions.add(lengthCompInstr);

            Label continueLabel = new Label(NameHandler.generateLabelName());//check on generating more specific name for labels
            IfGoto errorCatchInstr = new IfGoto(comp, continueLabel);
            b.instructions.add(errorCatchInstr);

            ErrorMessage indexErrorInstr = new ErrorMessage("\"array index out of bounds\"");
            b.instructions.add(indexErrorInstr);

            LabelInstr continueInstr = new LabelInstr(continueLabel);
            b.instructions.add(continueInstr);

            Identifier offset = new Identifier(NameHandler.generateVarName());
            Multiply storeOffset1Instr = new Multiply(offset, four, index);
            b.instructions.add(storeOffset1Instr);

            Add storeOffset2Instr = new Add(offset, four, offset);
            b.instructions.add(storeOffset2Instr);

            Identifier spot = new Identifier(NameHandler.generateVarName());
            Add storeSpotInstr = new Add(spot, arr, offset);
            b.instructions.add(storeSpotInstr);

            Store storeRHSInstr = new Store(spot, 0, rhs);
            b.instructions.add(storeRHSInstr);

            Label endLabel = new Label(NameHandler.generateLabelName()); //skip over null block
            Goto gotoEndInstr = new Goto(endLabel);
            b.instructions.add(gotoEndInstr);

            //null:....
            LabelInstr continueNullInstr = new LabelInstr(nullLabel);
            b.instructions.add(continueNullInstr);

            ErrorMessage nullErrorInstr = new ErrorMessage("\"null pointer\"");
            b.instructions.add(nullErrorInstr);

            LabelInstr negLabelCont = new LabelInstr(negLabel);
            b.instructions.add(negLabelCont);

            ErrorMessage negIndError = new ErrorMessage("\"array index out of bounds\"");
            b.instructions.add(negIndError);

            //end
            LabelInstr carryOnInstr = new LabelInstr(endLabel);
            b.instructions.add(carryOnInstr);
        }
        else{
            //len = [varname + 0]
            //minus1 = 1
            //len = len - minus1
            //comp = len < index
            //if0 comp goto l
                //error("index out")
            //l:
            //[arrayName + 4*index + 4] = rhs

            Label nullLabel = new Label(NameHandler.generateLabelName());
            IfGoto checkNullInstr = new IfGoto(arrayName, nullLabel);
            b.instructions.add(checkNullInstr);

            IR.token.Identifier arrLen = new IR.token.Identifier(NameHandler.generateVarName());
            Load setArrLenInstr = new Load(arrLen, arrayName, 0);
            b.instructions.add(setArrLenInstr);

            /*
            Identifier neg1 = new Identifier(NameHandler.generateVarName());
            Move_Id_Integer neg1Instr = new Move_Id_Integer(neg1, -1);
            b.instructions.add(neg1Instr);
            */

            Identifier zero = new Identifier(NameHandler.generateVarName());
            Move_Id_Integer zeroInstr = new Move_Id_Integer(zero, 0);
            b.instructions.add(zeroInstr);

            Identifier minus1 = new Identifier(NameHandler.generateVarName());
            Move_Id_Integer minus1Instr = new Move_Id_Integer(minus1, 1);
            b.instructions.add(minus1Instr);

            Identifier negIndex = new Identifier(NameHandler.generateVarName());
            Subtract negIndexInstr = new Subtract(negIndex, zero, index);
            b.instructions.add(negIndexInstr);

            Subtract negIndexInstr2 = new Subtract(negIndex, negIndex, minus1);
            b.instructions.add(negIndexInstr2);

            Identifier negIndComp = new Identifier(NameHandler.generateVarName());
            LessThan negIndCompInstr = new LessThan(negIndComp, negIndex, zero);
            b.instructions.add(negIndCompInstr);

            Label negLabel = new Label(NameHandler.generateLabelName());
            IfGoto if0arrayName = new IfGoto(negIndComp, negLabel);
            b.instructions.add(if0arrayName);

            Subtract updateLenInstr = new Subtract(arrLen, arrLen, minus1);
            b.instructions.add(updateLenInstr);

            IR.token.Identifier comp = new IR.token.Identifier(NameHandler.generateVarName());
            LessThan lengthCompInstr = new LessThan(comp, arrLen, index);
            b.instructions.add(lengthCompInstr);

            Label continueLabel = new Label(NameHandler.generateLabelName());//check on generating more specific name for labels
            IfGoto errorCatchInstr = new IfGoto(comp, continueLabel);
            b.instructions.add(errorCatchInstr);

            ErrorMessage indexErrorInstr = new ErrorMessage("\"array index out of bounds\"");
            b.instructions.add(indexErrorInstr);

            LabelInstr continueInstr = new LabelInstr(continueLabel);
            b.instructions.add(continueInstr);

            Identifier four = new Identifier(NameHandler.generateVarName());
            Move_Id_Integer storeFourInstr = new Move_Id_Integer(four, 4);
            b.instructions.add(storeFourInstr);

            Identifier offset = new Identifier(NameHandler.generateVarName());
            Multiply storeOffset1Instr = new Multiply(offset, four, index);
            b.instructions.add(storeOffset1Instr);

            Add storeOffset2Instr = new Add(offset, four, offset);
            b.instructions.add(storeOffset2Instr);

            Identifier spot = new Identifier(NameHandler.generateVarName());
            Add storeSpotInstr = new Add(spot, arrayName, offset);
            b.instructions.add(storeSpotInstr);

            Store storeRHSInstr = new Store(spot, 0, rhs);
            b.instructions.add(storeRHSInstr);

            Label skip = new Label(NameHandler.generateLabelName());
            Goto skipInstr = new Goto(skip);

            LabelInstr catchNullArrayInstr = new LabelInstr(nullLabel);
            b.instructions.add(catchNullArrayInstr);

            ErrorMessage nullArrayError = new ErrorMessage("\"null pointer\"");
            b.instructions.add(nullArrayError);

            LabelInstr negLabelCont = new LabelInstr(negLabel);
            b.instructions.add(negLabelCont);

            ErrorMessage negIndError = new ErrorMessage("\"array index out of bounds\"");
            b.instructions.add(negIndError);

            continueInstr = new LabelInstr(skip);
            b.instructions.add(continueInstr);
        }

        return null;
    }
    public IR.token.Identifier visit(Expression e, Block b) {
        return e.f0.accept(this, b);
    }
    public IR.token.Identifier  visit(PrimaryExpression e, Block b) {
        return e.f0.accept(this, b);
    }
    public IR.token.Identifier visit(IntegerLiteral e, Block b) {
        int number = Integer.parseInt(e.f0.toString());
        IR.token.Identifier lhs = new IR.token.Identifier(NameHandler.generateVarName());
        //IntegerStoreMap.intMap.put(lhs, number);

        Move_Id_Integer intInstr = new Move_Id_Integer(lhs, number);
        b.instructions.add(intInstr);
        return lhs;
    }

    public IR.token.Identifier visit(TrueLiteral e, Block b) {
        IR.token.Identifier lhs =  new IR.token.Identifier(NameHandler.generateVarName());
        Move_Id_Integer intInstr = new Move_Id_Integer(lhs, 1);
        b.instructions.add(intInstr);
        return lhs;
    }

    public IR.token.Identifier visit(FalseLiteral e, Block b) {
        IR.token.Identifier lhs = new IR.token.Identifier(NameHandler.generateVarName());
        Move_Id_Integer intInstr = new Move_Id_Integer(lhs, 0);
        b.instructions.add(intInstr);
        return lhs;
    }

    public IR.token.Identifier visit(ThisExpression e, Block b) {
        return new IR.token.Identifier("this");
    }

    public IR.token.Identifier visit(ArrayAllocationExpression e, Block b) {
        //new id[int]
        //heapSize = int*4 + 4
        //array = alloc(size)
        //actualSize = int*4
        //[array + 0] = actualSize
        //return array
        Identifier arrLen = e.f3.accept(this, b);
        Identifier arrHeapSizeNeeded = new Identifier(NameHandler.generateVarName());
        Identifier fourHolder = new Identifier(NameHandler.generateVarName());

        Move_Id_Integer loadFourHolderInstr = new Move_Id_Integer(fourHolder, 4);
        b.instructions.add(loadFourHolderInstr);

        Multiply multiplyHeapSizeInstr = new Multiply(arrHeapSizeNeeded, arrLen, fourHolder);
        b.instructions.add(multiplyHeapSizeInstr);

        Add addHeapSizeInstr = new Add(arrHeapSizeNeeded, arrHeapSizeNeeded, fourHolder);
        b.instructions.add(addHeapSizeInstr);

        Identifier array = new Identifier(NameHandler.generateVarName());
        Alloc allocArrayInstr = new Alloc(array, arrHeapSizeNeeded);
        b.instructions.add(allocArrayInstr);

        Store loadArrLenInstr = new Store(array, 0, arrLen);
        b.instructions.add(loadArrLenInstr);






/* 
        int byteSize = 4 + (4*arrLen);

        IR.token.Identifier allocSize = new IR.token.Identifier(NameHandler.generateVarName());
        Move_Id_Integer storeSizeInstr = new Move_Id_Integer(allocSize, byteSize);
        b.instructions.add(storeSizeInstr);

        IR.token.Identifier arrayHeap = new IR.token.Identifier(NameHandler.generateVarName());
        Alloc allocInstr = new Alloc(arrayHeap, allocSize);
        b.instructions.add(allocInstr);

        IR.token.Identifier actualSize = new IR.token.Identifier(NameHandler.generateVarName());
        Move_Id_Integer storeActualSizeInstr = new Move_Id_Integer(actualSize, arrLen);
        b.instructions.add(storeActualSizeInstr);

        Store storeInstr = new Store(arrayHeap, 0, actualSize);
        b.instructions.add(storeInstr);
        */

        return array;
    }

    public IR.token.Identifier visit(AllocationExpression e, Block b) {
        String className = e.f1.f0.toString();
        ClassObjectDataStructure classTable = classTables.tables.get(className);
        int numMethods = classTable.methods.size();
        int numVars = classTable.classVars.size();
        int spotsNeededForMainTable = numVars + 1;
        int bytesNeededForMainTable = spotsNeededForMainTable*4;

        IR.token.Identifier mainTableBytes = new IR.token.Identifier(NameHandler.generateVarName());
        Move_Id_Integer mainTableBytesInstr = new Move_Id_Integer(mainTableBytes, bytesNeededForMainTable);
        b.instructions.add(mainTableBytesInstr);

        IR.token.Identifier mainTable = new IR.token.Identifier(NameHandler.generateVarName());
        Alloc mainTableAllocInstr = new Alloc(mainTable, mainTableBytes);
        b.instructions.add(mainTableAllocInstr);

        int bytesNeededForVMT = numMethods*4;
        IR.token.Identifier VMTBytes = new IR.token.Identifier(NameHandler.generateVarName());
        Move_Id_Integer VMTBytesInstr = new Move_Id_Integer(VMTBytes, bytesNeededForVMT);
        b.instructions.add(VMTBytesInstr);

        IR.token.Identifier VMT = new IR.token.Identifier(NameHandler.generateVarName());
        Alloc VMTAllocInstr = new Alloc(VMT, VMTBytes);
        b.instructions.add(VMTAllocInstr);

        for(Integer i = 0; i < numMethods; i++)
        {
            //Func assignment instr
            IR.token.Identifier funcNameTempStore = new IR.token.Identifier(NameHandler.generateVarName());
            //Move_Id_FuncName moveInFuncInstr = new Move_Id_FuncName(funcNameTempStore, new FunctionName(className + classTable.methods.get(i)));
            String actualName = classTable.methodCallNames.get(i);
            Move_Id_FuncName moveInFuncInstr = new Move_Id_FuncName(funcNameTempStore, new FunctionName(actualName));
            //System.err.println(actualName);
            b.instructions.add(moveInFuncInstr);

            Store storeMethodInstr = new Store(VMT, (4*i), funcNameTempStore);
            b.instructions.add(storeMethodInstr);
        }

        Store storeVMTInstr = new Store(mainTable, 0, VMT);
        b.instructions.add(storeVMTInstr);

        return mainTable;
    }

    public IR.token.Identifier visit(NotExpression e, Block b) {
        IR.token.Identifier expr = e.f1.accept(this, b);
        IR.token.Identifier oneVar = new IR.token.Identifier(NameHandler.generateVarName());
        Move_Id_Integer intInstr = new Move_Id_Integer(oneVar, 1);
        b.instructions.add(intInstr);

        IR.token.Identifier lhs = new IR.token.Identifier(NameHandler.generateVarName());
        Subtract subInstr = new Subtract(lhs, oneVar, expr);
        b.instructions.add(subInstr);

        return lhs;
    }

    public IR.token.Identifier visit(BracketExpression e, Block b) {
        return e.f1.accept(this, b);
    }

    public IR.token.Identifier visit(AndExpression a, Block b) {
        IR.token.Identifier arg1 = a.f0.accept(this, b);
        IR.token.Identifier arg2 = a.f2.accept(this, b);
        IR.token.Identifier lhs = new IR.token.Identifier(NameHandler.generateVarName());
        Multiply multInstr = new Multiply(lhs, arg1, arg2);
        b.instructions.add(multInstr);
        return lhs;
    }

    public IR.token.Identifier visit(PlusExpression e, Block b) {
        IR.token.Identifier arg1 = e.f0.accept(this, b);
        IR.token.Identifier arg2 = e.f2.accept(this, b);
        IR.token.Identifier lhs = new IR.token.Identifier(NameHandler.generateVarName());
        Add addInstr = new Add(lhs, arg1, arg2);
        b.instructions.add(addInstr);
        return lhs;
    }

    public IR.token.Identifier visit(MinusExpression e, Block b) {
        IR.token.Identifier arg1 = e.f0.accept(this, b);
        IR.token.Identifier arg2 = e.f2.accept(this, b);
        IR.token.Identifier lhs = new IR.token.Identifier(NameHandler.generateVarName());
        Subtract subInstr = new Subtract(lhs, arg1, arg2);
        b.instructions.add(subInstr);
        return lhs;
    }

    public IR.token.Identifier visit(CompareExpression e, Block b) {
        IR.token.Identifier arg1 = e.f0.accept(this, b);
        IR.token.Identifier arg2 = e.f2.accept(this, b);
        IR.token.Identifier lhs = new IR.token.Identifier(NameHandler.generateVarName());
        LessThan compInstr = new LessThan(lhs, arg1, arg2);
        b.instructions.add(compInstr);
        return lhs;
    }

    public IR.token.Identifier visit(TimesExpression e, Block b) {
        IR.token.Identifier arg1 = e.f0.accept(this, b);
        IR.token.Identifier arg2 = e.f2.accept(this, b);
        IR.token.Identifier lhs = new IR.token.Identifier(NameHandler.generateVarName());
        Multiply multInstr = new Multiply(lhs, arg1, arg2);
        b.instructions.add(multInstr);
        return lhs;
    }

    public IR.token.Identifier visit(ArrayLookup a, Block b) {
        //What if x is a classvar?
        IR.token.Identifier arrayName = a.f0.accept(this, b);//x[a] = q
        IR.token.Identifier index = a.f2.accept(this, b);
        ClassObjectDataStructure classTable = classTables.tables.get(b.parent.functionName.name);
        if(!b.parent.functionName.name.equals("Main") && !NoneClassVars.scopeVars.contains(arrayName.toString()) && classTable.classVars.contains(arrayName.toString()))
        {
            //v0 = [this + indOffset]
            //arrayName = v0
            Label nullLabel = new Label(NameHandler.generateLabelName());
            IfGoto checkNullInstr = new IfGoto(new Identifier("this"), nullLabel); ///check if array is null
            b.instructions.add(checkNullInstr);

            Identifier four = new Identifier(NameHandler.generateVarName());
            Move_Id_Integer storeFourInstr = new Move_Id_Integer(four, 4);
            b.instructions.add(storeFourInstr);

            Integer ind = classTable.classVars.indexOf(arrayName.toString());
            Identifier heapAccessOffset = new Identifier(NameHandler.generateVarName());
            Move_Id_Integer storeFourOnHeapInstr = new Move_Id_Integer(heapAccessOffset, ind);
            b.instructions.add(storeFourOnHeapInstr);

            Multiply multiplyHeapAccessOffsetInstr = new Multiply(heapAccessOffset, four, heapAccessOffset);
            b.instructions.add(multiplyHeapAccessOffsetInstr);

            Add addHeapAccessInstr = new Add(heapAccessOffset, four, heapAccessOffset);
            b.instructions.add(addHeapAccessInstr);
            
            Identifier fakeArr = new Identifier(NameHandler.generateVarName());
            Add setArrInstr = new Add(fakeArr, new Identifier("this"), heapAccessOffset);
            b.instructions.add(setArrInstr);

            checkNullInstr = new IfGoto(fakeArr, nullLabel);
            b.instructions.add(checkNullInstr);

            Identifier arr = new Identifier(NameHandler.generateVarName());
            Load arrDereferenceInstr = new Load(arr, fakeArr, 0);
            b.instructions.add(arrDereferenceInstr);

            IR.token.Identifier arrLen = new IR.token.Identifier(NameHandler.generateVarName());
            Load setArrLenInstr = new Load(arrLen, arr, 0);
            b.instructions.add(setArrLenInstr);
    
            /*
            Identifier neg1 = new Identifier(NameHandler.generateVarName());
            Move_Id_Integer neg1Instr = new Move_Id_Integer(neg1, -1);
            b.instructions.add(neg1Instr);
            */

            Identifier zero = new Identifier(NameHandler.generateVarName());
            Move_Id_Integer zeroInstr = new Move_Id_Integer(zero, 0);
            b.instructions.add(zeroInstr);

            IR.token.Identifier minus1 = new IR.token.Identifier(NameHandler.generateVarName());
            Move_Id_Integer minus1Instr = new Move_Id_Integer(minus1, 1);
            b.instructions.add(minus1Instr);

            Identifier negIndex = new Identifier(NameHandler.generateVarName());
            Subtract negIndexInstr = new Subtract(negIndex, zero, index);
            b.instructions.add(negIndexInstr);

            Subtract negIndexInstr2 = new Subtract(negIndex, negIndex, minus1);
            b.instructions.add(negIndexInstr2);

            Identifier negIndComp = new Identifier(NameHandler.generateVarName());
            LessThan negIndCompInstr = new LessThan(negIndComp, negIndex, zero);
            b.instructions.add(negIndCompInstr);

            Label negLabel = new Label(NameHandler.generateLabelName());
            IfGoto if0arrayName = new IfGoto(negIndComp, negLabel);
            b.instructions.add(if0arrayName);
    
            Subtract updateLenInstr = new Subtract(arrLen, arrLen, minus1);
            b.instructions.add(updateLenInstr);
    
            IR.token.Identifier comp = new IR.token.Identifier(NameHandler.generateVarName());
            LessThan lengthCompInstr = new LessThan(comp, arrLen, index);
            b.instructions.add(lengthCompInstr);
    
            Label continueLabel = new Label(NameHandler.generateLabelName());//check on generating more specific name for labels
            IfGoto errorCatchInstr = new IfGoto(comp, continueLabel);
            b.instructions.add(errorCatchInstr);
    
            ErrorMessage indexErrorInstr = new ErrorMessage("\"array index out of bounds\"");
            b.instructions.add(indexErrorInstr);
    
            LabelInstr continueInstr = new LabelInstr(continueLabel);
            b.instructions.add(continueInstr);

            Identifier offset = new Identifier(NameHandler.generateVarName());
            Multiply offsetMultInstr = new Multiply(offset, four, index);
            b.instructions.add(offsetMultInstr);

            Add offsetAddInstr = new Add(offset, offset, four);
            b.instructions.add(offsetAddInstr);

            Identifier spot = new Identifier(NameHandler.generateVarName());
            Add spotAddInstr = new Add(spot, arr, offset);
            b.instructions.add(spotAddInstr);
    
            IR.token.Identifier lookupVal = new IR.token.Identifier(NameHandler.generateVarName());
            Load retrieveValInstr = new Load(lookupVal, spot, 0);
            b.instructions.add(retrieveValInstr);

            Label endLabel = new Label(NameHandler.generateLabelName()); //skip over null block
            Goto gotoEndInstr = new Goto(endLabel);
            b.instructions.add(gotoEndInstr);

            //null:....
            LabelInstr continueNullInstr = new LabelInstr(nullLabel);
            b.instructions.add(continueNullInstr);

            ErrorMessage nullErrorInstr = new ErrorMessage("\"null pointer\"");
            b.instructions.add(nullErrorInstr);

            //neg index
            LabelInstr negLabelCont = new LabelInstr(negLabel);
            b.instructions.add(negLabelCont);

            ErrorMessage negIndError = new ErrorMessage("\"array index out of bounds\"");
            b.instructions.add(negIndError);


            //end
            LabelInstr carryOnInstr = new LabelInstr(endLabel);
            b.instructions.add(carryOnInstr);
            
            return lookupVal;
        }

        //len = [arrayname + 0]
        //minus1 = 1
        //len = len - minus1
        //comp = len < index
        //if0 comp goto l
            //error("index out")
        //l:
            //vx = [arrayname + indexOffset]
            //return vx
        
        Label nullLabel = new Label(NameHandler.generateLabelName());
        IfGoto checkNullInstr = new IfGoto(arrayName, nullLabel); ///check if array is null
        b.instructions.add(checkNullInstr);

        IR.token.Identifier arrLen = new IR.token.Identifier(NameHandler.generateVarName());
        Load setArrLenInstr = new Load(arrLen, arrayName, 0);
        b.instructions.add(setArrLenInstr);

            /*
            Identifier neg1 = new Identifier(NameHandler.generateVarName());
            Move_Id_Integer neg1Instr = new Move_Id_Integer(neg1, -1);
            b.instructions.add(neg1Instr);
            */

        Identifier zero = new Identifier(NameHandler.generateVarName());
        Move_Id_Integer zeroInstr = new Move_Id_Integer(zero, 0);
        b.instructions.add(zeroInstr);

        IR.token.Identifier minus1 = new IR.token.Identifier(NameHandler.generateVarName());
        Move_Id_Integer minus1Instr = new Move_Id_Integer(minus1, 1);
        b.instructions.add(minus1Instr);

        Identifier negIndex = new Identifier(NameHandler.generateVarName());
        Subtract negIndexInstr = new Subtract(negIndex, zero, index);
        b.instructions.add(negIndexInstr);

        Subtract negIndexInstr2 = new Subtract(negIndex, negIndex, minus1);
        b.instructions.add(negIndexInstr2);

        Identifier negIndComp = new Identifier(NameHandler.generateVarName());
        LessThan negIndCompInstr = new LessThan(negIndComp, negIndex, zero);
        b.instructions.add(negIndCompInstr);

        Label negLabel = new Label(NameHandler.generateLabelName());
        IfGoto if0arrayName = new IfGoto(negIndComp, negLabel);
        b.instructions.add(if0arrayName);

        Subtract updateLenInstr = new Subtract(arrLen, arrLen, minus1);
        b.instructions.add(updateLenInstr);

        IR.token.Identifier comp = new IR.token.Identifier(NameHandler.generateVarName());
        LessThan lengthCompInstr = new LessThan(comp, arrLen, index);
        b.instructions.add(lengthCompInstr);

        Label continueLabel = new Label(NameHandler.generateLabelName());//check on generating more specific name for labels
        IfGoto errorCatchInstr = new IfGoto(comp, continueLabel);
        b.instructions.add(errorCatchInstr);

        ErrorMessage indexErrorInstr = new ErrorMessage("\"array index out of bounds\"");
        b.instructions.add(indexErrorInstr);

        LabelInstr continueInstr = new LabelInstr(continueLabel);
        b.instructions.add(continueInstr);

        Identifier four = new Identifier(NameHandler.generateVarName());
        Move_Id_Integer storeFourInstr = new Move_Id_Integer(four, 4);
        b.instructions.add(storeFourInstr);
        
        Identifier offset = new Identifier(NameHandler.generateVarName());
        Multiply offsetMultInstr = new Multiply(offset, four, index);
        b.instructions.add(offsetMultInstr);

        Add offsetAddInstr = new Add(offset, offset, four);
        b.instructions.add(offsetAddInstr);

        Identifier spot = new Identifier(NameHandler.generateVarName());
        Add spotAddInstr = new Add(spot, arrayName, offset);
        b.instructions.add(spotAddInstr);

        IR.token.Identifier lookupVal = new IR.token.Identifier(NameHandler.generateVarName());
        Load retrieveValInstr = new Load(lookupVal, spot, 0);
        b.instructions.add(retrieveValInstr);

        Label skip = new Label(NameHandler.generateLabelName());
        Goto skipInstr = new Goto(skip);
        b.instructions.add(skipInstr);

        //neg ind
        LabelInstr negLabelCont = new LabelInstr(negLabel);
        b.instructions.add(negLabelCont);

        ErrorMessage negIndError = new ErrorMessage("\"array index out of bounds\"");
        b.instructions.add(negIndError);

        //null:....
        LabelInstr continueNullInstr = new LabelInstr(nullLabel);
        b.instructions.add(continueNullInstr);

        ErrorMessage nullErrorInstr = new ErrorMessage("\"null pointer\"");
        b.instructions.add(nullErrorInstr);

        continueInstr = new LabelInstr(skip);
        b.instructions.add(continueInstr);


        return lookupVal;
    }

    public IR.token.Identifier visit(ArrayLength e, Block b) {
        //whatf arrname is classvar
        //vx = [arrName + 0]
        //return vx
        IR.token.Identifier arrayName = e.f0.accept(this, b);
        ClassObjectDataStructure classTable = classTables.tables.get(b.parent.functionName.name);
        if(!b.parent.functionName.name.equals("Main") && !NoneClassVars.scopeVars.contains(arrayName.toString()) && classTable.classVars.contains(arrayName.toString()))
        {
            //v0 = [this + indOffset]
            //arrayName = v0
            Label nullLabel = new Label(NameHandler.generateLabelName());
            IfGoto checkNullInstr = new IfGoto(new Identifier("this"), nullLabel);
            b.instructions.add(checkNullInstr);

            Identifier four = new Identifier(NameHandler.generateVarName());
            Move_Id_Integer storeFourInstr = new Move_Id_Integer(four, 4);
            b.instructions.add(storeFourInstr);

            Integer ind = classTable.classVars.indexOf(arrayName.toString());
            Identifier heapAccessOffset = new Identifier(NameHandler.generateVarName());
            Move_Id_Integer storeFourOnHeapInstr = new Move_Id_Integer(heapAccessOffset, ind);
            b.instructions.add(storeFourOnHeapInstr);

            Multiply multiplyHeapAccessOffsetInstr = new Multiply(heapAccessOffset, four, heapAccessOffset);
            b.instructions.add(multiplyHeapAccessOffsetInstr);

            Add addHeapAccessInstr = new Add(heapAccessOffset, four, heapAccessOffset);
            b.instructions.add(addHeapAccessInstr);
            
            Identifier fakeArr = new Identifier(NameHandler.generateVarName());
            Add setArrInstr = new Add(fakeArr, new Identifier("this"), heapAccessOffset);
            b.instructions.add(setArrInstr);

            checkNullInstr = new IfGoto(fakeArr, nullLabel);
            b.instructions.add(checkNullInstr);

            Identifier arr = new Identifier(NameHandler.generateVarName());
            Load arrDereferenceInstr = new Load(arr, fakeArr, 0);
            b.instructions.add(arrDereferenceInstr);

            IR.token.Identifier length = new IR.token.Identifier(NameHandler.generateVarName());
            Load arrLengthRetrieveInstr = new Load(length, arr, 0);
            b.instructions.add(arrLengthRetrieveInstr);

            Label endLabel = new Label(NameHandler.generateLabelName()); //skip over null block
            Goto gotoEndInstr = new Goto(endLabel);
            b.instructions.add(gotoEndInstr);

            //null:....
            LabelInstr continueNullInstr = new LabelInstr(nullLabel);
            b.instructions.add(continueNullInstr);

            ErrorMessage nullErrorInstr = new ErrorMessage("\"null pointer\"");
            b.instructions.add(nullErrorInstr);

            //end
            LabelInstr carryOnInstr = new LabelInstr(endLabel);
            b.instructions.add(carryOnInstr);

            return length;
        }
        Label nullLabel = new Label(NameHandler.generateLabelName());
        IfGoto checkNullInstr = new IfGoto(arrayName, nullLabel);
        b.instructions.add(checkNullInstr);

        IR.token.Identifier length = new IR.token.Identifier(NameHandler.generateVarName());
        Load arrLengthRetrieveInstr = new Load(length, arrayName, 0);
        b.instructions.add(arrLengthRetrieveInstr);

        Label endLabel = new Label(NameHandler.generateLabelName()); //skip over null block
        Goto gotoEndInstr = new Goto(endLabel);
        b.instructions.add(gotoEndInstr);

        //null:....
        LabelInstr continueNullInstr = new LabelInstr(nullLabel);
        b.instructions.add(continueNullInstr);

        ErrorMessage nullErrorInstr = new ErrorMessage("\"null pointer\"");
        b.instructions.add(nullErrorInstr);

        //end
        LabelInstr carryOnInstr = new LabelInstr(endLabel);
        b.instructions.add(carryOnInstr);

        return length;
    }

    public IR.token.Identifier visit(MessageSend n, Block b) {
        //Get Name and Data table
        IR.token.Identifier classHeap = n.f0.accept(this, b);
        String methodName = n.f2.f0.toString();
        String className = n.f0.accept(new classNameFinder(), b);

        //Get list of parameters
        ArrayList<IR.token.Identifier> sparrowParams = n.f4.accept(new SparrowParamArrayResolver(), b);
        if(sparrowParams == null)
        {
            sparrowParams = new ArrayList<IR.token.Identifier>();
        }
        sparrowParams.add(0, classHeap);

        
        Label nullLabel = new Label(NameHandler.generateLabelName());
        IfGoto checkNullInstr = new IfGoto(classHeap, nullLabel);
        b.instructions.add(checkNullInstr);

        ClassObjectDataStructure classTable = classTables.tables.get(className);
        int indexOfFunction = classTable.methods.indexOf(methodName);

        IR.token.Identifier VMT = new Identifier(NameHandler.generateVarName());
        Load VMTLoadInstr = new Load(VMT, classHeap, 0);
        b.instructions.add(VMTLoadInstr);

        IR.token.Identifier funcDataLocation = new Identifier(NameHandler.generateVarName());
        Load funcDataLoadInstr = new Load(funcDataLocation, VMT, (4*indexOfFunction));
        b.instructions.add(funcDataLoadInstr);

        IR.token.Identifier callVar = new Identifier(NameHandler.generateVarName());
        Call callInstr = new Call(callVar, funcDataLocation, sparrowParams);
        b.instructions.add(callInstr);

        Label endLabel = new Label(NameHandler.generateLabelName()); //skip over null block
        Goto gotoEndInstr = new Goto(endLabel);
        b.instructions.add(gotoEndInstr);

        //null:....
        LabelInstr continueNullInstr = new LabelInstr(nullLabel);
        b.instructions.add(continueNullInstr);

        ErrorMessage nullErrorInstr = new ErrorMessage("\"null pointer\"");
        b.instructions.add(nullErrorInstr);

        //end
        LabelInstr carryOnInstr = new LabelInstr(endLabel);
        b.instructions.add(carryOnInstr);

        return callVar;
    }

    public IR.token.Identifier visit(ExpressionRest e, Block b) {
        return e.f1.accept(this, b);
    }
 
    public IR.token.Identifier visit(minijava.syntaxtree.Identifier a, Block b) {
        //check if a is a classvar
        // v0 = [classname + offset]
        // return v0
        String className = b.parent.functionName.name;
        ClassObjectDataStructure classTable = classTables.tables.get(className);
        if(!className.equals("Main") && !NoneClassVars.scopeVars.contains(a.f0.toString()) && classTable.classVars.contains(a.f0.toString()))
        {
            Label nullLabel = new Label(NameHandler.generateLabelName());
            IfGoto checkNullInstr = new IfGoto(new Identifier("this"), nullLabel);
            b.instructions.add(checkNullInstr);

            Identifier four = new Identifier(NameHandler.generateVarName());
            Move_Id_Integer storeFourInstr = new Move_Id_Integer(four, 4);
            b.instructions.add(storeFourInstr);

            Integer ind = classTable.classVars.indexOf(a.f0.toString());
            Identifier heapAccessOffset = new Identifier(NameHandler.generateVarName());
            Move_Id_Integer storeFourOnHeapInstr = new Move_Id_Integer(heapAccessOffset, ind);
            b.instructions.add(storeFourOnHeapInstr);

            Multiply multiplyHeapAccessOffsetInstr = new Multiply(heapAccessOffset, four, heapAccessOffset);
            b.instructions.add(multiplyHeapAccessOffsetInstr);

            Add addHeapAccessInstr = new Add(heapAccessOffset, four, heapAccessOffset);
            b.instructions.add(addHeapAccessInstr);

            Identifier varLoc = new Identifier(NameHandler.generateVarName());
            Add assignVarLocInstr = new Add(varLoc, new Identifier("this"), heapAccessOffset);
            b.instructions.add(assignVarLocInstr);

            /*
            Label nullLabel = new Label(NameHandler.generateLabelName());
            IfGoto checkNullInstr = new IfGoto(varLoc, nullLabel);
            b.instructions.add(checkNullInstr);
            */

            IR.token.Identifier varHeap = new Identifier(a.f0.toString());
            Load VMTLoadInstr = new Load(varHeap, new Identifier("this"), (4+ (4*ind)));

            b.instructions.add(VMTLoadInstr);

            Label endLabel = new Label(NameHandler.generateLabelName()); //skip over null block
            Goto gotoEndInstr = new Goto(endLabel);
            b.instructions.add(gotoEndInstr);

            //null:....
            LabelInstr continueNullInstr = new LabelInstr(nullLabel);
            b.instructions.add(continueNullInstr);

            ErrorMessage nullErrorInstr = new ErrorMessage("\"null pointer\"");
            b.instructions.add(nullErrorInstr);

            //end
            LabelInstr carryOnInstr = new LabelInstr(endLabel);
            b.instructions.add(carryOnInstr);

            return varHeap;
        }
        return (new IR.token.Identifier(a.f0.toString()));
    }
}

class SparrowParamArrayResolver extends GJDepthFirst <ArrayList<IR.token.Identifier>, Block>
{
    public ArrayList<IR.token.Identifier> visit(ExpressionList e, Block b) {
        ArrayList<IR.token.Identifier> callParams = new ArrayList<IR.token.Identifier>();
        callParams.add(e.f0.accept(new InstructionVisitor(), b));
        for (Integer i = 0; i < e.f1.size(); i++) {
            callParams.add(e.f1.elementAt(i).accept(new InstructionVisitor(), b));
        }
        return callParams;
    }
}

class classNameFinder extends GJDepthFirst <String, Block>
{
    public String visit(AllocationExpression e, Block b) {
        return e.f1.f0.toString();
    }

    public String visit(ThisExpression e, Block b) {
        return b.parent.functionName.name;
    }

    public String visit(PrimaryExpression e, Block b) {
        return e.f0.accept(this, b);
    }

    public String visit(BracketExpression e, Block b) {
        return e.f1.accept(this, b);
    }

    public String visit(Expression e, Block b) {
        return e.f0.accept(this, b);
    }

    public String visit(minijava.syntaxtree.Identifier e, Block b) {
        return idToClassMap.map.get(e.f0.toString());
    }

    public String visit(MessageSend n, Block b) {
        String className = n.f0.accept(this, b);
        String methodName = n.f2.f0.toString();
        Tuple<String, String> calledMethod = GlobalVars.getSymbolTable(className).getMethod(methodName);
        String type = calledMethod.getType();;
        return type;
    }
}



class idToClassMap
{
    public static HashMap<String, String> map = new HashMap<String, String>();
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


/* 
class IntegerStoreMap
{
    public static HashMap<Identifier, Integer> intMap = new HashMap<Identifier, Integer>();
    public static HashMap<Identifier, ArrayList<Integer>> arrMap = new HashMap<Identifier, ArrayList<Integer>>();
}

class IntMapVisitor extends GJDepthFirst < Integer, TupleLHSRHS >
{
    public Integer visit(AssignmentStatement e, TupleLHSRHS T) {
        
        if rhs in map keys
            lhs maps to rhs val
            return
            
        if rhs is integer
            lhs maps to integer
        return "OK";
        

        IR.token.Identifier lhs = T.lhs;
        IR.token.Identifier rhs = T.rhs;

        if(IntegerStoreMap.intMap.containsKey(rhs))
        {
            IntegerStoreMap.intMap.put(lhs, IntegerStoreMap.intMap.get(rhs));
            return 1;
        }

        Integer RHS = e.f2.accept(this, T);
        if(RHS != null)
        {
            IntegerStoreMap.intMap.put(lhs, RHS);
            return 1;
        }
        
        return 0;
    }


    public Integer visit(Expression e, TupleLHSRHS T) {
        return e.f0.accept(this, T);
    }

    public Integer visit(PrimaryExpression e, TupleLHSRHS T) {
        return e.f0.accept(this, T);
    }

    public Integer visit(IntegerLiteral e, TupleLHSRHS T) {
        return Integer.parseInt(e.f0.toString());
    }

    public Integer visit(ArrayAllocationExpression e, TupleLHSRHS T) {
        IntegerStoreMap.arrMap.put(T.lhs, new ArrayList<Integer>());
        return null;
    }

}

class TupleLHSRHS{
    public Identifier lhs;
    public Identifier rhs;
    public TupleLHSRHS(Identifier lhs_in, Identifier rhs_in){
        lhs = lhs_in;
        rhs = rhs_in;
    }
}



class MethodToClass
{
    public static HashMap<String, HashMap<String,String>> map = new HashMap<String, HashMap<String,String>>();
}
*/

class VarDecResolver extends GJNoArguDepthFirst < String >
{
    public String visit(VarDeclaration e) {
        String className = e.f0.accept(this);
        String varName = e.f1.f0.toString();
        idToClassMap.map.put(varName, className);
        return "OK";
    }
    public String visit(FormalParameter e) {
        String className = e.f0.accept(this);
        String varName = e.f1.f0.toString();
        idToClassMap.map.put(varName, className);
        return "OK";
    }
    public String visit(Type e) {
        return e.f0.accept(this);
    }
    public String visit(minijava.syntaxtree.Identifier e) {
        return e.f0.toString();
    }
    public String visit(ArrayType e) {
        return "int[]";
    }
    public String visit(BooleanType e) {
        return "bool";
    }
    public String visit(IntegerType e) {
        return "int";
    }
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

class ClassTableExtenderOld extends GJNoArguDepthFirst <String >
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
            x.f1.elementAt(i).accept(new ClassTableExtenderOld());
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
