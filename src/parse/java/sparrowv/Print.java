package sparrowv;

import IR.token.Register;
import sparrowv.visitor.ArgRetVisitor;
import sparrowv.visitor.ArgVisitor;
import sparrowv.visitor.RetVisitor;
import sparrowv.visitor.Visitor;

public class Print extends Instruction {
  public Register content;

  public Print(Register content) {
    this.content = content;
  }

  public void accept(Visitor v) {
    v.visit(this);
  }

  public <A> void accept(ArgVisitor<A> v, A arg) {
    v.visit(this, arg);
  }

  public <A,R> R accept(ArgRetVisitor<A,R> v, A arg) {
    return v.visit(this, arg);
  }

  public <R> R accept(RetVisitor<R> v){
    return v.visit(this);
  }

  public String toString() {
    return "print(" + content + ")";
  }
}
