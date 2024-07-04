import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import IR.ParseException;
import IR.SparrowParser;
import IR.registers.Registers;
import IR.syntaxtree.Node;
import IR.token.Identifier;
import IR.visitor.SparrowVConstructor;
import sparrowv.Add;
import sparrowv.Alloc;
import sparrowv.Block;
import sparrowv.Call;
import sparrowv.ErrorMessage;
import sparrowv.FunctionDecl;
import sparrowv.Goto;
import sparrowv.IfGoto;
import sparrowv.Instruction;
import sparrowv.LabelInstr;
import sparrowv.LessThan;
import sparrowv.Load;
import sparrowv.Move_Id_Reg;
import sparrowv.Move_Reg_FuncName;
import sparrowv.Move_Reg_Id;
import sparrowv.Move_Reg_Integer;
import sparrowv.Move_Reg_Reg;
import sparrowv.Multiply;
import sparrowv.Print;
import sparrowv.Program;
import sparrowv.Store;
import sparrowv.Subtract;
import sparrowv.visitor.DepthFirst;
import sparrowv.visitor.RetVisitor;

public class SV2V {
  public static void main(String[] args) throws ParseException, IOException {
    Registers.SetRiscVregs();
    InputStream in = System.in;
    new SparrowParser(in);
    Node root = SparrowParser.Program();
    SparrowVConstructor constructor = new SparrowVConstructor();
    root.accept(constructor);
    Program sparrowv_program = constructor.getProgram();
    sparrowv_program.accept(new SV2V_Translator());
    /* 
    ArrayList<String> riscv_program = translator.get_riscv_program();
    for(String line : riscv_program)
    {
      System.out.println(line);
    }
      */

  }
}

class SV2V_Translator extends DepthFirst
{
    //private ArrayList<String> p;
    private ArrayList<String> stack_vars;
    private ArrayList<String> param_vars;
    private int labelNum;

    public SV2V_Translator()
    {
      //p = new ArrayList<String>();
      stack_vars = new ArrayList<String>();
      param_vars = new ArrayList<String>();
      labelNum = 0;
    }
    //public ArrayList<String> get_riscv_program(){return p;}

    public void visit(Add x)
    {
      System.out.println("add " + x.lhs.toString() + ", " + x.arg1.toString() + ", " + x.arg2.toString());
    }
    public void visit(Alloc x)
    {
      /*
        mv a0, t1 				# Move requested size to a0
        jal alloc 				# Call alloc subroutine to request heap memory
        mv s1, a0 				# Move the returned pointer to s1
      */
      System.out.println("mv a0, " + x.size.toString());
      System.out.println("jal alloc");
      System.out.println("mv " + x.lhs.toString() + ", a0");
    }
    public void visit(Block x)
    {
      /*
      sw fp, -8(sp)
      mv fp, sp
      li t6, 32
      sub sp, sp, t6
      sw ra, -4(fp)
      sw s1, -12(fp) -- optional (based on how many registers you have)
      */

      System.out.println("sw fp, -8(sp)"); // store old frame pointer
      System.out.println("mv fp, sp"); // move frame pointer
      int num_local_vars = x.accept(new LocalVarCount(param_vars));
      int sp_offset = 8 + (num_local_vars*4);
      System.out.println("li t6, " + sp_offset); //move stack pointer to top (get proper number of vars needed here)
      System.out.println("sub sp, sp, t6");
      System.out.println("sw ra, -4(fp)"); //store return address

      //Accept all instructions
      for(Instruction i : x.instructions)
      {
        i.accept(this);
      }

      //Store return val in a0
      String ret_var = x.return_id.toString();
      int ret_var_fp_offset;
      if(stack_vars.contains(ret_var))
        ret_var_fp_offset = -12 - (stack_vars.indexOf(ret_var) *4);
      else //return variable is a parameter if not on stack
        ret_var_fp_offset = param_vars.indexOf(ret_var)*4;
      System.out.println("lw a0, " + ret_var_fp_offset + "(fp)");

      // store return address in ra
      System.out.println("lw ra, -4(fp)");
      
      // restore old fp
      System.out.println("lw fp, -8(fp)");

      // restore stack pointer
      System.out.println("addi sp, sp, " + sp_offset);
      System.out.println("addi sp, sp, " + (param_vars.size()*4));

      //return
      System.out.println("jr ra");
    }
    public void visit(Call x)
    {
      // move stack pointer
      //li t6, numargs*4
      //sub sp, sp, t6
      int arg_stack_offset = 4*x.args.size();
      System.out.println("li t6, " + arg_stack_offset);
      System.out.println("sub sp, sp, t6");
    
      // store arguments on stack
      //lw t6, -16(fp)
      //sw t6, 0(sp)
      //lw t6, -12(fp)
      //sw t6, 4(sp)
      for(int i = 0; i < x.args.size(); i++)
      {
        //First get fp offset
        int arg_ind;
        int fp_offset;
        String arg_name = x.args.get(i).toString();
        if(stack_vars.contains(arg_name))
        {
          arg_ind = stack_vars.indexOf(x.args.get(i).toString());
          fp_offset = -12 - (4*arg_ind);
        }
        else //stack var is a current parameter
        {
          arg_ind = param_vars.indexOf(x.args.get(i).toString());
          fp_offset = (4*arg_ind);
        }

        System.out.println("lw t6, " + fp_offset + "(fp)");
        System.out.println("sw t6, " + (i*4) + "(sp)");
      }

      //jalr s2
      //mv s3, a0
      System.out.println("jalr " + x.callee.toString());
      System.out.println("mv " + x.lhs.toString() + ", a0");
    }
    public void visit(ErrorMessage x)
    {
      /* 
      la a0, msg_0 				# Load the address of the error message to a0
      j error
      */
      if(x.msg.contains("null pointer"))
        System.out.println("la a0, msg_0");
      else
        System.out.println("la a0, msg_1");
      System.out.println("jal error");
    }
    public void visit(FunctionDecl x)
    {
      //.globl Main
      //Main:
      //accept parameters?
      //accept block

      System.out.println(".globl " + x.functionName.toString());
      System.out.println(x.functionName.toString() + ":");
      
      ArrayList<String> old_stack_vars = new ArrayList<>(stack_vars);
      ArrayList<String> old_param_vars = new ArrayList<>(param_vars);
      stack_vars.clear();
      param_vars.clear();
      for(Identifier i : x.formalParameters)
      {
        param_vars.add(i.toString());
      }

      x.block.accept(this);

      param_vars = old_param_vars;
      stack_vars = old_stack_vars;
    }
    public void visit(Goto x)
    {
      //j main_end
      System.out.println("j " + x.parent.parent.functionName.toString() + x.label.toString());
    }
    public void visit(IfGoto x)
    {
      //beqz t0, null1
      /*
      beqz rs1, l1
      jal l2
      l1:
      jal block
      l2:
      */

      String label1 = "TeeshEndLabel" + labelNum;
      labelNum++;
      String label2 = "TeeshEndLabel" + labelNum;
      labelNum++;

      System.out.println("beqz " + x.condition.toString() + ", " + label1);
      System.out.println("j " + label2);
      System.out.println(label1 + ":");
      System.out.println("j " + x.parent.parent.functionName.toString() + x.label.toString());
      System.out.println(label2 + ":");
    }
    public void visit(Instruction x)
    {
      x.accept(this);
    }
    public void visit(LabelInstr x)
    {
      //null1:
      System.out.println(x.parent.parent.functionName.toString() + x.toString());
    }
    public void visit(LessThan x)
    {
      System.out.println("slt " + x.lhs.toString() + ", " + x.arg1.toString() + ", " + x.arg2.toString());
    }
    public void visit(Load x)
    {
      //lhs = [base + offset]
      //lw s3, 0(t1)
      System.out.println("lw " + x.lhs.toString() + ", " + x.offset + "(" + x.base.toString() + ")");
    }
    public void visit(Move_Id_Reg x)
    {
      //sw s1, -12(fp)
      String lhs_var = x.lhs.toString();
      int var_fp_offset;
      if(stack_vars.contains(lhs_var))
      {
        var_fp_offset = -12 - (stack_vars.indexOf(lhs_var)*4);
      }
      else if(param_vars.contains(lhs_var))
      {
        var_fp_offset = param_vars.indexOf(lhs_var)*4;
      }
      else //declaring a new stack variable
      {
        stack_vars.add(lhs_var);
        var_fp_offset = -12 - (stack_vars.indexOf(lhs_var)*4);
      }
      System.out.println("sw " + x.rhs.toString() + ", " + var_fp_offset + "(fp)");
    }
    public void visit(Move_Reg_FuncName x)
    {
      //la t0, FacComputeFac
      System.out.println("la " + x.lhs.toString() + ", " + x.rhs.toString());
    }
    public void visit(Move_Reg_Id x)
    {
      //lw t1, 4(fp)
      String rhs_var = x.rhs.toString();
      int var_fp_offset;
      if(stack_vars.contains(rhs_var))
      {
        var_fp_offset = -12 - (stack_vars.indexOf(rhs_var) *4);
      }
      else //RHS variable is a parameter if not on stack
      {
        var_fp_offset = param_vars.indexOf(rhs_var)*4;
      }

      System.out.println("lw " + x.lhs.toString() + ", " + var_fp_offset + "(fp)");
    }
    public void visit(Move_Reg_Integer x)
    {
      //li t0, 6
      System.out.println("li " + x.lhs.toString() + ", " + x.rhs);
    }
    public void visit(Move_Reg_Reg x)
    {
      //lw rd, rs1
      System.out.println("mv " + x.lhs.toString() + ", " + x.rhs.toString());
    }
    public void visit(Multiply x)
    {
      System.out.println("mul " + x.lhs.toString() + ", " + x.arg1.toString() + ", " + x.arg2.toString());
    }
    public void visit(Print x)
    {
      /*
      mv a1, s3 				# Mov the content to be printed to a1
      li a0, @print_int 				# Load the code for print_int to a0
      ecall 				# Print the number
      */

      System.out.println("mv a1, " + x.content.toString());
      System.out.println("li a0, @print_int");
      System.out.println("ecall");

      //Add newline
      System.out.println("li a1, 10");
      System.out.println("li a0, 11");
      System.out.println("ecall");
    }
    public void visit(Program x)
    {
      System.out.println(".equiv @sbrk, 9");
      System.out.println(".equiv @print_string, 4");
      System.out.println(".equiv @print_char, 11");
      System.out.println(".equiv @print_int, 1");
      System.out.println(".equiv @exit 10");
      System.out.println(".equiv @exit2, 17");

      System.out.println(".text");

      System.out.println("jal Main");
      System.out.println("li a0, @exit");
      System.out.println("ecall");

      for(FunctionDecl function : x.funDecls)
      {
        function.accept(this);
      }

      //Adding Alloc Function
      System.out.println(".globl alloc");
      System.out.println("alloc:");
      System.out.println("mv a1, a0");
      System.out.println("li a0, @sbrk");
      System.out.println("ecall");
      System.out.println("jr ra");

      //Adding Error Function
      System.out.println(".globl error");
      System.out.println("error:");
      System.out.println("mv a1, a0");
      System.out.println("li a0, @print_string");
      System.out.println("ecall");
      System.out.println("li a1, 10");
      System.out.println("li a0, @print_char");
      System.out.println("ecall");
      System.out.println("li a0, @exit");
      System.out.println("ecall");
      System.out.println("abort_17:");
      System.out.println("j abort_17");

      //Adding global error labels
      System.out.println(".data");

      System.out.println(".globl msg_0");
      System.out.println("msg_0:");
      System.out.println(".asciiz \"null pointer\"");
      System.out.println(".align 2");

      System.out.println(".globl msg_1");
      System.out.println("msg_1:");
      System.out.println(".asciiz \"array index out of bounds\"");
      System.out.println(".align 2");

    }
    public void visit(Store x)
    {
      //[base + offset] = rhs
      //sw t1, 0(s1)
      System.out.println("sw " + x.rhs.toString() + ", " + x.offset + "(" + x.base.toString() + ")");
    }
    public void visit(Subtract x)
    {
      System.out.println("sub " + x.lhs.toString() + ", " + x.arg1.toString() + ", " + x.arg2.toString());
    }
}

class LocalVarCount implements RetVisitor <Integer>
{
  public Integer visit(Add x) { return 0;}
  public Integer visit(Alloc x) { return 0;}
  public Integer visit(Call x) { return 0;}
  public Integer visit(ErrorMessage x) { return 0;}
  public Integer visit(FunctionDecl x){ return 0;}
  public Integer visit(Goto x) { return 0;}
  public Integer visit(IfGoto x) { return 0;}
  public Integer visit(LabelInstr x) { return 0;}
  public Integer visit(LessThan x) { return 0;}
  public Integer visit(Load x) { return 0;}
  public Integer visit(Move_Reg_FuncName x) { return 0;}
  public Integer visit(Move_Reg_Id x) { return 0;}
  public Integer visit(Move_Reg_Integer x) { return 0;}
  public Integer visit(Move_Reg_Reg x) { return 0;}
  public Integer visit(Multiply x) { return 0;}
  public Integer visit(Print x) { return 0;}
  public Integer visit(Program x) { return 0;}
  public Integer visit(Subtract x) { return 0;}
  public Integer visit(Store x) { return 0;}

  private ArrayList<String> recorded_vars;
  public LocalVarCount (ArrayList<String> params)
  {
    recorded_vars = new ArrayList<>(params);
  }

  public Integer visit(Move_Id_Reg x)
  { 
    if(recorded_vars.contains(x.lhs.toString()))
      return 0;
    recorded_vars.add(x.lhs.toString());
    return 1;
  }

  public Integer visit(Block x) 
  { 
    int numVars = 0;
    for(Instruction i : x.instructions)
    {
      numVars += i.accept(this);
    }
    return numVars;
  }

  public Integer visit(Instruction x)
  { 
    return x.accept(this);
  }
}

