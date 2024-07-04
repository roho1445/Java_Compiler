import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import IR.ParseException;
import IR.SparrowParser;
import IR.syntaxtree.Node;
import IR.token.Identifier;
import IR.token.Register;
import IR.visitor.SparrowConstructor;
import sparrow.visitor.ArgRetVisitor;
import sparrow.visitor.ArgVisitor;
import sparrow.visitor.DepthFirst;
import sparrow.visitor.RetVisitor;
import sparrowv.Move_Id_Reg;
import sparrowv.Move_Reg_Id;
import sparrowv.Move_Reg_Reg;

public class S2SV {
    public static void main(String[] args) throws ParseException {
        new SparrowParser(System.in);
        Node root = SparrowParser.Program();
        SparrowConstructor constructor = new SparrowConstructor();
        root.accept(constructor);
        sparrow.Program sparrow_program = constructor.getProgram();
        sparrowv.Program sparrowv_program = new sparrowv.Program(new ArrayList<sparrowv.FunctionDecl>());
        sparrow_program.accept(new SparrowProgramTranslator(), sparrowv_program);
        System.out.println(sparrowv_program.toString());
    }
}

class GlobalMappings
{
    public static HashMap<String, String> permVarToRegMap = new HashMap<String, String>();
    public static HashMap<String, String> tempVarToRegMap = new HashMap<String, String>();
    public static ArrayList<VarInterval> varTimeline = new ArrayList<VarInterval> ();
    public static ArrayList<VarInterval> a_varTimeline = new ArrayList<VarInterval> ();
    //public static Identifier t0 = null;
    //public static Identifier t1 = null;
}

class VarInterval{
    public String var;
    public int startTime;
    public int endTime;
    public VarInterval(String name, int start, int end)
    {
        var = name;
        startTime = start;
        endTime = end;
    }
    public int getStartTime()
    {
        return startTime;
    }
}

class LHS_Retriever implements RetVisitor <IR.token.Identifier> {
    //Add, Alloc, Call, ErrorMessage, Goto, IfGoto,
    //LabelInstr, LessThan, Load, Move_Id_FuncName, Move_Id_Id, Move_Id_Integer, Multiply, Print, Store, Subtract
    public IR.token.Identifier visit(sparrow.Program x)
    {
        return null;
    }
    public IR.token.Identifier visit(sparrow.Block x)
    {
        return null;
    }
    public IR.token.Identifier visit(sparrow.FunctionDecl x)
    {
        return null;
    }
    public IR.token.Identifier visit(sparrow.Print x)
    {
        return null;
    }
    public IR.token.Identifier visit(sparrow.Store x)
    {
        return null;
    }
    public IR.token.Identifier visit(sparrow.ErrorMessage x)
    {
        return null;
    }
    public IR.token.Identifier visit(sparrow.Goto x)
    {
        return null;
    }
    public IR.token.Identifier visit(sparrow.IfGoto x)
    {
        return null;
    }
    public IR.token.Identifier visit(sparrow.LabelInstr x)
    {
        return null;
    }
    //Instructions with a left hand side
    public IR.token.Identifier visit(sparrow.Add x)
    {
        return x.lhs;
    }
    public IR.token.Identifier visit(sparrow.Alloc x)
    {
        return x.lhs;
    }
    public IR.token.Identifier visit(sparrow.Call x)
    {
        return x.lhs;
    }
    public IR.token.Identifier visit(sparrow.LessThan x)
    {
        return x.lhs;
    }
    public IR.token.Identifier visit(sparrow.Load x)
    {
        return x.lhs;
    }
    public IR.token.Identifier visit(sparrow.Move_Id_FuncName x)
    {
        return x.lhs;
    }
    public IR.token.Identifier visit(sparrow.Move_Id_Id x)
    {
        return x.lhs;
    }
    public IR.token.Identifier visit(sparrow.Move_Id_Integer x)
    {
        return x.lhs;
    }
    public IR.token.Identifier visit(sparrow.Multiply x)
    {
        return x.lhs;
    }
    public IR.token.Identifier visit(sparrow.Subtract x)
    {
        return x.lhs;
    }
}

class Var_Checker implements ArgRetVisitor <String, Boolean> {
    //Null (does not apply to class)
    public Boolean visit(sparrow.Block x, String y)
    {
        return false;
    }
    public Boolean visit(sparrow.FunctionDecl x, String y)
    {
        return false;
    }
    public Boolean visit(sparrow.Program x, String y)
    {
        return false;
    }
    public Boolean visit(sparrow.ErrorMessage x, String y)
    {
        return false;
    }
    public Boolean visit(sparrow.Goto x, String y)
    {
        return false;
    }
    public Boolean visit(sparrow.LabelInstr x, String y)
    {
        return false;
    }
    //Add, Alloc, Call, ErrorMessage, Goto, IfGoto, 
    //LabelInstr, LessThan, Load, Move_Id_FuncName, Move_Id_Id, Move_Id_Integer, Multiply, Print, Store, Subtract
    public Boolean visit(sparrow.Add x, String y)
    {
        return x.arg1.toString().equals(y) || x.arg2.toString().equals(y) || x.lhs.toString().equals(y);
    }
    public Boolean visit(sparrow.Alloc x, String y)
    {
        return x.lhs.toString().equals(y) || x.size.toString().equals(y);
    }
    public Boolean visit(sparrow.Call x, String y)
    {
        if(x.callee.toString().equals(y) || x.lhs.toString().equals(y))
            return true;
        
        for(int i = 0; i < x.args.size(); i++)
        {
            if(x.args.get(i).toString().equals(y))
                return true;
        }

        return false;
    }
    public Boolean visit(sparrow.IfGoto x, String y)
    {
        return x.condition.toString().equals(y);
    }
    public Boolean visit(sparrow.LessThan x, String y)
    {
        return x.arg1.toString().equals(y) || x.arg2.toString().equals(y) || x.lhs.toString().equals(y);
    }
    public Boolean visit(sparrow.Load x, String y)
    {
        return x.base.toString().equals(y) || x.lhs.toString().equals(y);
    }
    public Boolean visit(sparrow.Move_Id_FuncName x, String y)
    {
        return x.lhs.toString().equals(y);
    }
    public Boolean visit(sparrow.Move_Id_Id x, String y)
    {
        return x.rhs.toString().equals(y) || x.lhs.toString().equals(y);
    }
    public Boolean visit(sparrow.Move_Id_Integer x, String y)
    {
        return x.lhs.toString().equals(y);
    }
    public Boolean visit(sparrow.Multiply x, String y)
    {
        return x.arg1.toString().equals(y) || x.arg2.toString().equals(y) || x.lhs.toString().equals(y);
    }
    public Boolean visit(sparrow.Print x, String y)
    {
        return x.content.toString().equals(y);
    }
    public Boolean visit(sparrow.Store x, String y)
    {
        return x.base.toString().equals(y) || x.rhs.toString().equals(y);
    }
    public Boolean visit(sparrow.Subtract x, String y)
    {
        return x.arg1.toString().equals(y) || x.arg2.toString().equals(y) || x.lhs.toString().equals(y);
    }
}

class IntervalSorter extends DepthFirst  
{
    public void visit(sparrow.Block x)
    {
        //Do interval sorting here

        //ADD IN PARAMETER VALS
        for(int i = 0; i < x.parent.formalParameters.size(); i++)
        {
            VarInterval newInterval = new VarInterval(x.parent.formalParameters.get(i).toString(), -1, -1);
            GlobalMappings.a_varTimeline.add(newInterval);
        }

        //Add start times for other variables
        for(int i = 0; i < x.instructions.size(); i++)
        {
            IR.token.Identifier lhs = x.instructions.get(i).accept(new LHS_Retriever());

            if(lhs != null)
            {
                String lhs_name = lhs.toString();
                //check if lhs has already been assigned a start point
                    //if not add to list with start point at line i
                boolean assigned = false;
                for(int j = 0; j < GlobalMappings.varTimeline.size(); j++)
                {
                    if(GlobalMappings.varTimeline.get(j).var.equals(lhs_name))
                    {
                        assigned = true;
                        break;
                    }
                }
                for(int j = 0; j < GlobalMappings.a_varTimeline.size(); j++)
                {
                    if(GlobalMappings.a_varTimeline.get(j).var.equals(lhs_name))
                    {
                        assigned = true;
                        break;
                    }
                }

                if(!assigned)
                {
                    VarInterval newInterval = new VarInterval(lhs_name, i, i);
                    GlobalMappings.varTimeline.add(newInterval);
                } 
            }
        }



        //TAKING CARE OF LOOPS
        ArrayList<int[]> loopIntervals = new ArrayList<int[]>();
        int z = x.instructions.size() - 1;
        while (z >= 0)
        {
            if(x.instructions.get(z) instanceof sparrow.Goto)//goto statement
            {
                //get goto label
                String gotoString = x.instructions.get(z).toString();
                int lastSpaceIndex = gotoString.lastIndexOf(' ');
                String gotoLabel = gotoString.substring(lastSpaceIndex + 1);
                int j = z - 1;
                while(j >= 0)
                {
                    if(x.instructions.get(j) instanceof sparrow.LabelInstr)
                    {
                        String labelInstrString = x.instructions.get(j).toString();
                        //System.err.println(labelInstrString);
                        int colonIndex = labelInstrString.indexOf(':');
                        String label = labelInstrString.substring(0, colonIndex);
                        if(label.equals(gotoLabel))
                        {
                            loopIntervals.add(new int[]{j, z});
                            z = j;
                            break;
                        }
                    }
                    j--;
                }
            }

            z--;
        }

        for(int[] loopInterval: loopIntervals)
        {
            //System.err.println("LOOPS");
            //System.err.println(Arrays.toString(loopInterval));
        }

        //ADDING END TIMES
        for(VarInterval varInterval : GlobalMappings.varTimeline)
        {
            if(x.return_id.toString().equals(varInterval.var)) //If ending on return statement add it
            {
                varInterval.endTime = x.instructions.size();
            }
            else
            {
                //need to take care of loop
                for(int i = x.instructions.size() - 1; i > varInterval.startTime; i--)
                {
                    //need to check if var is in instruction  //if so, put i as end index
                    if(x.instructions.get(i).accept(new Var_Checker(), varInterval.var))
                    {
                        //System.err.println("Ending " + varInterval.var + " " + i);
                        varInterval.endTime = i;
                        //check if end time is in loop
                        for(int[] loopInterval: loopIntervals)
                        {
                            if(loopInterval[0] < i && i < loopInterval[1])
                            {
                                varInterval.endTime = loopInterval[1];
                                break;
                            }
                        }
                        break;
                    }
                }
            }
        }

        for(VarInterval varInterval : GlobalMappings.a_varTimeline)
        {
            if(x.return_id.toString().equals(varInterval.var)) //If ending on return statement add it
            {
                varInterval.endTime = x.instructions.size();
            }
            else
            {
                //need to take care of loop
                for(int i = x.instructions.size() - 1; i > varInterval.startTime; i--)
                {
                    //need to check if var is in instruction  //if so, put i as end index
                    if(x.instructions.get(i).accept(new Var_Checker(), varInterval.var))
                    {
                        //System.err.println("Ending " + varInterval.var + " " + i);
                        varInterval.endTime = i;
                        //check if end time is in loop
                        for(int[] loopInterval: loopIntervals)
                        {
                            if(loopInterval[0] < i && i < loopInterval[1])
                            {
                                varInterval.endTime = loopInterval[1];
                                break;
                            }
                        }
                        break;
                    }
                }
            }
        }

        //Sort intervals by start time
        GlobalMappings.varTimeline.sort(Comparator.comparing(VarInterval::getStartTime));
        for(VarInterval l : GlobalMappings.varTimeline)
        {
            //System.err.println(l.var + " -- Start: " + l.startTime + " -- End: " + l.endTime);
        }
        //System.err.println();

        //Allocate variables to registers or memory (s1-s11, t2-t5)
        ArrayList <String> availableRegisterNames = new ArrayList<>(Arrays.asList("s1", "s2", "s3", "s4", "s5", "s6", "s7", "s8", "s9", "s10", "s11", "t2", "t3", "t4", "t5"));

        for(int i = 0; i < GlobalMappings.varTimeline.size(); i++)
        {
            //First check if any register assignments can be made permanent
            int curr_startTime = GlobalMappings.varTimeline.get(i).startTime;
            for(int j = 0; j < i; j++)
            {
                if(GlobalMappings.tempVarToRegMap.containsKey(GlobalMappings.varTimeline.get(j).var))//check if intervalvar is active
                {
                    int endTime = GlobalMappings.varTimeline.get(j).endTime;
                    if(endTime <= curr_startTime)
                    {
                        String reg = GlobalMappings.tempVarToRegMap.get(GlobalMappings.varTimeline.get(j).var);
                        if(reg != null)
                        {
                            GlobalMappings.permVarToRegMap.put(GlobalMappings.varTimeline.get(j).var, reg);
                            GlobalMappings.tempVarToRegMap.remove(GlobalMappings.varTimeline.get(j).var);
                            availableRegisterNames.add(0, reg.toString());
                        }
    
                    }
                }
            }

            if(availableRegisterNames.size() == 0)
            {//Do Spill
                //Get varInterval with latest end time - make sure it is a varInterval that is active
                VarInterval latestInterval = null;
                for(int j = 0; j < i; j++)
                {
                    if(GlobalMappings.tempVarToRegMap.containsKey(GlobalMappings.varTimeline.get(j).var))
                    {
                        if(latestInterval == null)
                        {
                            latestInterval = GlobalMappings.varTimeline.get(j);
                            continue;
                        }
                        int endTime = GlobalMappings.varTimeline.get(j).endTime;
                        if(endTime > latestInterval.endTime)
                        {
                            latestInterval = GlobalMappings.varTimeline.get(j);
                        }
                    }
                }

                //Need to g
                String reg = GlobalMappings.tempVarToRegMap.get(latestInterval.var);
                GlobalMappings.permVarToRegMap.put(latestInterval.var, null);
                GlobalMappings.tempVarToRegMap.remove(latestInterval.var);
                availableRegisterNames.add(0, reg.toString());
            }

            GlobalMappings.tempVarToRegMap.put(GlobalMappings.varTimeline.get(i).var, availableRegisterNames.get(0));
            availableRegisterNames.remove(0);
            
        }
        //Put remaining temp table in perm
        for (String key : GlobalMappings.tempVarToRegMap.keySet()) {
            String value = GlobalMappings.tempVarToRegMap.get(key);
            GlobalMappings.permVarToRegMap.put(key, value);
        }
    }
}


class SparrowProgramTranslator implements ArgVisitor <sparrowv.Program>
{
    public void visit(sparrow.Add x, sparrowv.Program y) {}
    public void visit(sparrow.Alloc x, sparrowv.Program y) {}
    public void visit(sparrow.Block x, sparrowv.Program y) {}
    public void visit(sparrow.Call x, sparrowv.Program y) {}
    public void visit(sparrow.ErrorMessage x, sparrowv.Program y) {}
    public void visit(sparrow.FunctionDecl x, sparrowv.Program y) {}
    public void visit(sparrow.Goto x, sparrowv.Program y) {}
    public void visit(sparrow.IfGoto x, sparrowv.Program y) {}
    public void visit(sparrow.Instruction x, sparrowv.Program y) {}
    public void visit(sparrow.LabelInstr x, sparrowv.Program y) {}
    public void visit(sparrow.LessThan x, sparrowv.Program y) {}
    public void visit(sparrow.Load x, sparrowv.Program y) {}
    public void visit(sparrow.Move_Id_FuncName x, sparrowv.Program y) {}
    public void visit(sparrow.Move_Id_Id x, sparrowv.Program y) {}
    public void visit(sparrow.Move_Id_Integer x, sparrowv.Program y) {}
    public void visit(sparrow.Multiply x, sparrowv.Program y) {}
    public void visit(sparrow.Print x, sparrowv.Program y) {}
    public void visit(sparrow.Store x, sparrowv.Program y) {}
    public void visit(sparrow.Subtract x, sparrowv.Program y) {}


    public void visit(sparrow.Program x, sparrowv.Program y) {
        for(int i = 0; i < x.funDecls.size(); i++)
        {
            sparrowv.FunctionDecl translatedFunc = new sparrowv.FunctionDecl();
            translatedFunc.parent = y;
            translatedFunc.functionName = x.funDecls.get(i).functionName;
            translatedFunc.formalParameters = new ArrayList<Identifier>();
            translatedFunc.block = new sparrowv.Block();
            x.funDecls.get(i).accept(new SparrowFunctionTranslator(), translatedFunc);
            y.funDecls.add(translatedFunc);
        }
    }
}

class SparrowFunctionTranslator implements ArgVisitor <sparrowv.FunctionDecl>
{
    public void visit(sparrow.Add x, sparrowv.FunctionDecl y) {}
    public void visit(sparrow.Alloc x, sparrowv.FunctionDecl y) {}
    public void visit(sparrow.Block x, sparrowv.FunctionDecl y) {}
    public void visit(sparrow.Call x, sparrowv.FunctionDecl y) {}
    public void visit(sparrow.ErrorMessage x, sparrowv.FunctionDecl y) {}
    public void visit(sparrow.Goto x, sparrowv.FunctionDecl y) {}
    public void visit(sparrow.IfGoto x, sparrowv.FunctionDecl y) {}
    public void visit(sparrow.Instruction x, sparrowv.FunctionDecl y) {}
    public void visit(sparrow.LabelInstr x, sparrowv.FunctionDecl y) {}
    public void visit(sparrow.LessThan x, sparrowv.FunctionDecl y) {}
    public void visit(sparrow.Load x, sparrowv.FunctionDecl y) {}
    public void visit(sparrow.Move_Id_FuncName x, sparrowv.FunctionDecl y) {}
    public void visit(sparrow.Move_Id_Id x, sparrowv.FunctionDecl y) {}
    public void visit(sparrow.Move_Id_Integer x, sparrowv.FunctionDecl y) {}
    public void visit(sparrow.Multiply x, sparrowv.FunctionDecl y) {}
    public void visit(sparrow.Print x, sparrowv.FunctionDecl y) {}
    public void visit(sparrow.Program x, sparrowv.FunctionDecl y) {}
    public void visit(sparrow.Store x, sparrowv.FunctionDecl y) {}
    public void visit(sparrow.Subtract x, sparrowv.FunctionDecl y) {}


    public void visit(sparrow.FunctionDecl x, sparrowv.FunctionDecl y) {
        //First clear hashmap assignments
        //assign parameters to differnet registers in hashmap (a2-a7)
        GlobalMappings.permVarToRegMap.clear();
        GlobalMappings.tempVarToRegMap.clear();
        GlobalMappings.varTimeline.clear();
        GlobalMappings.a_varTimeline.clear();

        //Assign params to register here
        int i = 0;
        while(i < 6 && i < x.formalParameters.size())
        {
            int regNum = i + 2;
            String registerName = "a" + regNum;
            String paramName = x.formalParameters.get(i).toString();
            GlobalMappings.permVarToRegMap.put //map formal param to register
                (paramName, registerName);
            //System.err.println(paramName + " " + registerName);
            i++;
        }

        //Add remaining params to stack
        while (i < x.formalParameters.size())
        {
            y.formalParameters.add(x.formalParameters.get(i));
            i++;
        }

        //Do interval sorting here
        x.block.accept(new IntervalSorter());

        //GlobalMappings.t0 = null;
        //GlobalMappings.t1 = null;
        
        y.block.parent = y;
        y.block.instructions = new ArrayList<sparrowv.Instruction>();

        //Save all registers (s1-s11) - if not Main - Save only needed registers
        ArrayList<String> savedRegs = new ArrayList<String>();
        if(!y.functionName.toString().equals("Main"))
        {
            for (String reg : GlobalMappings.permVarToRegMap.values()) {
                if (reg != null && reg.charAt(0) == 's' && !savedRegs.contains(reg))
                {
                    //System.err.println("ERROR 488 " + reg);
                    savedRegs.add(reg);
                    String saveName = "save_" + reg;
                    Identifier saveReg = new Identifier(saveName);
                    Move_Id_Reg saveRegInstr = new Move_Id_Reg(saveReg, new Register(reg));
                    y.block.instructions.add(saveRegInstr);
                }
            }
        }

        //translate all instructions
        x.block.accept(new SparrowInstrTranslator(), y.block);
        
        //Put back vals in s1-s11 - if not Main
        for (String reg : savedRegs) {
            //System.err.println("ERROR 502 " + reg.toString());
            String saveName = "save_" + reg.toString();
            Identifier saveReg = new Identifier(saveName);
            Move_Reg_Id restoreRegInstr = new Move_Reg_Id(new Register(reg), saveReg);
            y.block.instructions.add(restoreRegInstr);
        }
    }
}

class SparrowInstrTranslator implements ArgVisitor <sparrowv.Block>
{
    public void visit(sparrow.Program x, sparrowv.Block b){}
    public void visit(sparrow.FunctionDecl x, sparrowv.Block b){}
    
    //END OF DUMMY FUNCTIONS
    private ArrayList<String> activeARegisters = new ArrayList<String>();
    private ArrayList<String> activeTRegisters = new ArrayList<String>();

    public void visit(sparrow.Block x, sparrowv.Block b)
    {
        for(int i = 0; i < x.instructions.size(); i++)
        {
            //clear any vars that are not live and add vars that are live
            activeARegisters.clear();
            activeTRegisters.clear();
            for(VarInterval interval : GlobalMappings.varTimeline)
            {
                String regName = GlobalMappings.permVarToRegMap.get(interval.var);
                if(regName != null)
                {
                    if(regName.charAt(0) == 's')
                    {
                        continue;
                    }
                    int startTime = interval.startTime;
                    int endTime = interval.endTime;
    
                    if(startTime <= i && i <= endTime)
                    {
                        activeTRegisters.add(regName);
                    }
                }

            }
            for(VarInterval interval : GlobalMappings.a_varTimeline)
            {
                String regName = GlobalMappings.permVarToRegMap.get(interval.var);
                if(regName != null)
                {
                    int startTime = interval.startTime;
                    int endTime = interval.endTime;
                    if(startTime <= i && i <= endTime)
                    {
                        activeARegisters.add(regName);
                    }
                }
            }
            //System.err.println(activeARegisters);
            x.instructions.get(i).accept(this, b);
        }

        //Return statement translation
        Identifier sparrow_return = x.return_id;
        String r_sparrow_return = GlobalMappings.permVarToRegMap.get(sparrow_return.toString());
        if(r_sparrow_return == null)
        {
            b.return_id = sparrow_return;
        }
        else
        {
            //if register
            //temp_var = t*
            //return temp_var
            b.instructions.add(new Move_Id_Reg(sparrow_return, new Register(r_sparrow_return)));
            b.return_id = sparrow_return;
        }
    }
    public void visit(sparrow.Add x, sparrowv.Block b)
    {
        Identifier lhs = x.lhs;
        Identifier arg1 = x.arg1;
        Identifier arg2 = x.arg2;

        String r_lhs = GlobalMappings.permVarToRegMap.get(lhs.toString());
        String r_arg1 = GlobalMappings.permVarToRegMap.get(arg1.toString());
        String r_arg2 = GlobalMappings.permVarToRegMap.get(arg2.toString());

        boolean lhs_in_mem = false;

        if(r_arg1 == null)
        {
            //t0 = arg1
            r_arg1 = "t0";
            b.instructions.add(new Move_Reg_Id(new Register(r_arg1), arg1));
        }

        if(r_arg2 == null)
        {
            r_arg2 = "t1";
            b.instructions.add(new Move_Reg_Id(new Register(r_arg2), arg2));
        }

        if(r_lhs == null)
        {
            r_lhs = "t0";
            lhs_in_mem = true;
        }
        
        b.instructions.add(new sparrowv.Add(new Register(r_lhs), new Register(r_arg1), new Register(r_arg2)));

        if(lhs_in_mem)
        {
            //lhs = t0
            b.instructions.add(new sparrowv.Move_Id_Reg(lhs, new Register(r_lhs)));
        }
    }

    public void visit(sparrow.Alloc x, sparrowv.Block b)
    {
        Identifier lhs = x.lhs;
        Identifier size = x.size;

        String r_lhs = GlobalMappings.permVarToRegMap.get(lhs.toString());
        String r_size = GlobalMappings.permVarToRegMap.get(size.toString());

        boolean lhs_in_mem = false;

        if(r_lhs == null)
        {
            lhs_in_mem = true;
            r_lhs = "t0";
            //Move_Reg_Id assignT0Instr = new Move_Reg_Id(new Register(r_lhs), lhs);
            //b.instructions.add(assignT0Instr);
        }
        if(r_size == null)
        {
            r_size = "t1";
            Move_Reg_Id assignT1Instr = new Move_Reg_Id(new Register(r_size), size);
            b.instructions.add(assignT1Instr);
        }

        sparrowv.Alloc allocInstr = new sparrowv.Alloc(new Register(r_lhs), new Register(r_size));
        b.instructions.add(allocInstr);

        if(lhs_in_mem)
        {
            //lhs = r_lhs
            b.instructions.add(new Move_Id_Reg(lhs, new Register(r_lhs)));
        }
    }
    public void visit(sparrow.Call x, sparrowv.Block b)
    {
        Identifier lhs = x.lhs;
        String r_lhs = GlobalMappings.permVarToRegMap.get(lhs.toString());

        //SAVE ANY EXCESS NEEDED DATA TO MEMORY
        ArrayList<Identifier> sparrowvCallParams = new ArrayList<Identifier>();
        for(int q = 6; q < x.args.size(); q++)
        {
            sparrowvCallParams.add(x.args.get(q));
        }

        for(Identifier var : sparrowvCallParams)
        {
            String regName = GlobalMappings.permVarToRegMap.get(var.toString());
            if(regName != null)
            {
                //var = reg
                b.instructions.add(new Move_Id_Reg(var, new Register(regName)));
            }
        }


        //load parameters into registers
            //saver registers before if register is still live
        //a2-a7

        //Do saving first
        int numArgsNeeded = x.args.size();
        ArrayList<String> savedRegs = new ArrayList<String>();
        //System.err.println(activeARegisters);
        for(int i = 0; i < numArgsNeeded && i < 6; i++)
        {
            int regNum = 2 + i;
            String regName = "a" + regNum;

            if(activeARegisters.contains(regName))//need to take a live register and save it before hand
            {
                //save_a* = a*
                //a* = t* or a* = varName
                //First Save Register - save_a* = a*

                if(!regName.equals(r_lhs)) //save only if a != lhs
                {
                    String saveVarName = "save_" + regName;
                    b.instructions.add(new Move_Id_Reg(new Identifier(saveVarName), new Register(regName)));
                    savedRegs.add(regName);
                }
            }
        }

        //Now do assignments as needed
        ArrayList<String> alteredARegs = new ArrayList<String>();
        for(int i = 0; i < numArgsNeeded && i < 6; i++)
        {
            int regNum = 2 + i;
            String regName = "a" + regNum;
            //regName = r_arg
            String r_arg = GlobalMappings.permVarToRegMap.get(x.args.get(i).toString());
            
            if(r_arg == null)
            {
                //regName = identifer
                b.instructions.add(new Move_Reg_Id(new Register(regName), x.args.get(i)));
                alteredARegs.add(regName);
            }
            else
            {
                if(alteredARegs.contains(r_arg))
                {
                    //regName = save_r_arg
                    String regSaveName = "save_" + r_arg;
                    b.instructions.add(new Move_Reg_Id(new Register(regName), new Identifier(regSaveName)));
                    alteredARegs.add(regName);
                }
                else
                {
                    //regName = r_arg
                    b.instructions.add(new Move_Reg_Reg(new Register(regName), new Register(r_arg)));
                    alteredARegs.add(regName);
                }
            }
        }



        //Save t parameters that are still live - if t is going to be used as the lhs, do NOT save
        for(int t = 2; t <= 5; t++)
        {
            String regName = "t" + t;
            if(r_lhs != null && regName.equals(r_lhs))
                continue;
            if(activeTRegisters.contains(regName))
            {
                String saveVarName = "save_" + regName;
                b.instructions.add(new Move_Id_Reg(new Identifier(saveVarName), new Register(regName)));
                savedRegs.add(regName);
            }
        }

        //do call with updated param list
        Identifier callee = x.callee;
        String r_callee = GlobalMappings.permVarToRegMap.get(callee.toString());
        boolean lhs_in_mem = false;
        if(r_callee == null)
        {
            //assign t1 to callee - t1 = callee
            r_callee = "t1";
            b.instructions.add(new Move_Reg_Id(new Register("t1"), callee));

        }
        if(r_lhs == null)
        {
            lhs_in_mem = true;
            //assign t0 to lhs after function call
            r_lhs = "t0";
        }
        b.instructions.add(new sparrowv.Call(new Register(r_lhs), new Register(r_callee), sparrowvCallParams)); 

        //Restore lhs to memory if needed
        if(lhs_in_mem)
        {
            //lhs = t0
            b.instructions.add(new Move_Id_Reg(lhs, new Register(r_lhs)));
        }

        //retrive registers (t/a) as needed
        for(String regName : savedRegs)
        {
            //t* = save_t*
            String saveName = "save_" + regName;
            b.instructions.add(new Move_Reg_Id(new Register(regName), new Identifier(saveName)));
        }
        

    }
    public void visit(sparrow.ErrorMessage x, sparrowv.Block b)
    {
        sparrowv.ErrorMessage errorInstr = new sparrowv.ErrorMessage(x.msg);
        b.instructions.add(errorInstr);
    }
    public void visit(sparrow.Goto x, sparrowv.Block b)
    {
        sparrowv.Goto gotoInstr = new sparrowv.Goto(x.label);
        b.instructions.add(gotoInstr);
    }
    public void visit(sparrow.IfGoto x, sparrowv.Block b)
    {
        Identifier condition = x.condition;
        String r_condition = GlobalMappings.permVarToRegMap.get(condition.toString());

        if(r_condition == null)
        {
            r_condition = "t0";
            Move_Reg_Id assignCondInstr = new Move_Reg_Id(new Register(r_condition), condition);
            b.instructions.add(assignCondInstr);
        }
        sparrowv.IfGoto ifGotoInstr = new sparrowv.IfGoto(new Register(r_condition), x.label);
        b.instructions.add(ifGotoInstr);
    }
    public void visit(sparrow.LabelInstr x, sparrowv.Block b)
    {
        sparrowv.LabelInstr labelInstr = new sparrowv.LabelInstr(x.label);
        b.instructions.add(labelInstr);
    }
    public void visit(sparrow.LessThan x, sparrowv.Block b)
    {
        Identifier lhs = x.lhs;
        Identifier arg1 = x.arg1;
        Identifier arg2 = x.arg2;

        String r_lhs = GlobalMappings.permVarToRegMap.get(lhs.toString());
        String r_arg1 = GlobalMappings.permVarToRegMap.get(arg1.toString());
        String r_arg2 = GlobalMappings.permVarToRegMap.get(arg2.toString());

        boolean lhs_in_mem = false;

        if(r_arg1 == null)
        {
            //t0 = arg1
            r_arg1 = "t0";
            b.instructions.add(new Move_Reg_Id(new Register(r_arg1), arg1));
        }

        if(r_arg2 == null)
        {
            r_arg2 = "t1";
            b.instructions.add(new Move_Reg_Id(new Register(r_arg2), arg2));
        }

        if(r_lhs == null)
        {
            r_lhs = "t0";
            lhs_in_mem = true;
        }
        
        b.instructions.add(new sparrowv.LessThan(new Register(r_lhs), new Register(r_arg1), new Register(r_arg2)));

        if(lhs_in_mem)
        {
            //lhs = t0
            b.instructions.add(new sparrowv.Move_Id_Reg(lhs, new Register(r_lhs)));
        }
    }
    public void visit(sparrow.Load x, sparrowv.Block b)
    {
        //lhs = [base + offset]
        Identifier lhs = x.lhs;
        Identifier base = x.base;

        String r_lhs = GlobalMappings.permVarToRegMap.get(lhs.toString());
        String r_base = GlobalMappings.permVarToRegMap.get(base.toString());

        boolean lhs_in_mem = false;

        if(r_base == null)
        {
            //t1 = base
            r_base = "t1";
            b.instructions.add(new Move_Reg_Id(new Register(r_base), base));
        }

        if(r_lhs == null)
        {
            r_lhs = "t0";
            lhs_in_mem = true;
        }

        b.instructions.add(new sparrowv.Load(new Register(r_lhs), new Register(r_base), x.offset));

        if(lhs_in_mem)
        {
            //lhs = t0
            b.instructions.add(new sparrowv.Move_Id_Reg(lhs, new Register(r_lhs)));
        }

    }
    public void visit(sparrow.Move_Id_FuncName x, sparrowv.Block b)
    {
        //t0 = @functionname
        //lhs = t0
        Identifier lhs = x.lhs;
        String r_lhs = GlobalMappings.permVarToRegMap.get(lhs.toString());
        boolean tempVar = false;
        if(r_lhs == null)
        {
            tempVar = true;
            r_lhs = "t0";
        }
        sparrowv.Move_Reg_FuncName regFuncInstr = new sparrowv.Move_Reg_FuncName(new Register(r_lhs), x.rhs);
        b.instructions.add(regFuncInstr);
        if(tempVar)
        {
            Move_Id_Reg storeTempInstr = new Move_Id_Reg(lhs, new Register(r_lhs));
            b.instructions.add(storeTempInstr);
        }
    }
    public void visit(sparrow.Move_Id_Id x, sparrowv.Block b)
    {
        //lhs = rhs
        //t0 = rhs
        //lhs = t0

        Identifier lhs = x.lhs;
        Identifier rhs = x.rhs;
        String r_lhs = GlobalMappings.permVarToRegMap.get(lhs.toString());
        String r_rhs = GlobalMappings.permVarToRegMap.get(rhs.toString());

        if(r_lhs == null)
        {
            if(r_rhs == null)
            {
                //t1 = rhs
                //lhs = t1
                b.instructions.add(new Move_Reg_Id(new Register("t1"), rhs));
                b.instructions.add(new Move_Id_Reg(lhs, new Register("t1")));
            }
            else
            {
                //lhs = r_rhs
                b.instructions.add(new Move_Id_Reg(lhs, new Register(r_rhs)));
            }
        }
        else
        {
            if(r_rhs == null)
            {
                //r_lhs = rhs
                b.instructions.add(new Move_Reg_Id(new Register(r_lhs), rhs));
            }
            else
            {
                //r_lhs = r_rhs
                b.instructions.add(new Move_Reg_Reg(new Register(r_lhs), new Register(r_rhs)));
            }
        }
    }
    public void visit(sparrow.Move_Id_Integer x, sparrowv.Block b)
    {
        //lhs = 111
        //lhs register = 111
        //t0 = 111
        //lhs = t0

        Identifier lhs = x.lhs;
        String r_lhs = GlobalMappings.permVarToRegMap.get(lhs.toString());

        boolean inMem = false;

        if(r_lhs == null)
        {
            inMem = true;
            r_lhs = "t0";
        }

        sparrowv.Move_Reg_Integer storeIntInstr = new sparrowv.Move_Reg_Integer(new Register(r_lhs), x.rhs);
        b.instructions.add(storeIntInstr);

        if(inMem)
        {
            b.instructions.add(new Move_Id_Reg(lhs, new Register(r_lhs)));
        }

    }
    public void visit(sparrow.Multiply x, sparrowv.Block b)
    {
        Identifier lhs = x.lhs;
        Identifier arg1 = x.arg1;
        Identifier arg2 = x.arg2;

        String r_lhs = GlobalMappings.permVarToRegMap.get(lhs.toString());
        String r_arg1 = GlobalMappings.permVarToRegMap.get(arg1.toString());
        String r_arg2 = GlobalMappings.permVarToRegMap.get(arg2.toString());

        boolean lhs_in_mem = false;

        if(r_arg1 == null)
        {
            //t0 = arg1
            r_arg1 = "t0";
            b.instructions.add(new Move_Reg_Id(new Register(r_arg1), arg1));
        }

        if(r_arg2 == null)
        {
            r_arg2 = "t1";
            b.instructions.add(new Move_Reg_Id(new Register(r_arg2), arg2));
        }

        if(r_lhs == null)
        {
            r_lhs = "t0";
            lhs_in_mem = true;
        }
        
        b.instructions.add(new sparrowv.Multiply(new Register(r_lhs), new Register(r_arg1), new Register(r_arg2)));

        if(lhs_in_mem)
        {
            //lhs = t0
            b.instructions.add(new sparrowv.Move_Id_Reg(lhs, new Register(r_lhs)));
        }
    }
    public void visit(sparrow.Print x, sparrowv.Block b)
    {
        //print(id)
        //print(r_id)
        //t0 = id
        //print(t0)
        Identifier content = x.content;
        String r_content = GlobalMappings.permVarToRegMap.get(content.toString());

        if(r_content == null)
        {
            r_content = "t0";
            b.instructions.add(new Move_Reg_Id(new Register(r_content), content));
        }

        b.instructions.add(new sparrowv.Print(new Register(r_content)));
    }
    public void visit(sparrow.Store x, sparrowv.Block b)
    {
        Identifier base = x.base;
        Identifier rhs = x.rhs;

        String r_base = GlobalMappings.permVarToRegMap.get(base.toString());
        String r_rhs = GlobalMappings.permVarToRegMap.get(rhs.toString());

        //[r_base + c] = r_rhs

        if(r_base == null)
        {
            r_rhs = "t0";
            b.instructions.add(new Move_Reg_Id(new Register(r_base), base));
        }

        if(r_rhs == null)
        {
            r_rhs = "t1";
            b.instructions.add(new Move_Reg_Id(new Register(r_rhs), rhs));
        }

        b.instructions.add(new sparrowv.Store(new Register(r_base), x.offset, new Register(r_rhs)));
    }
    public void visit(sparrow.Subtract x, sparrowv.Block b)
    {
        Identifier lhs = x.lhs;
        Identifier arg1 = x.arg1;
        Identifier arg2 = x.arg2;

        String r_lhs = GlobalMappings.permVarToRegMap.get(lhs.toString());
        String r_arg1 = GlobalMappings.permVarToRegMap.get(arg1.toString());
        String r_arg2 = GlobalMappings.permVarToRegMap.get(arg2.toString());

        boolean lhs_in_mem = false;

        if(r_arg1 == null)
        {
            //t0 = arg1
            r_arg1 = "t0";
            b.instructions.add(new Move_Reg_Id(new Register(r_arg1), arg1));
        }

        if(r_arg2 == null)
        {
            r_arg2 = "t1";
            b.instructions.add(new Move_Reg_Id(new Register(r_arg2), arg2));
        }

        if(r_lhs == null)
        {
            r_lhs = "t0";
            lhs_in_mem = true;
        }
        
        b.instructions.add(new sparrowv.Subtract(new Register(r_lhs), new Register(r_arg1), new Register(r_arg2)));

        if(lhs_in_mem)
        {
            //lhs = t0
            b.instructions.add(new sparrowv.Move_Id_Reg(lhs, new Register(r_lhs)));
        }

    }
}