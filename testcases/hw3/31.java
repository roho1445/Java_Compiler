class Main {
    public static void main(String[] a){
           C c;
           c = new C();
           System.out.println(c.init());
           System.out.println(c.runC());
           System.out.println(c.runB());
           System.out.println((c.getShadow())[3]);
           System.out.println(c.runC());
           System.out.println(c.test(false, 66, 33));
           System.out.println(c.test(true, 66, 33));
           System.out.println(c.run());
           System.out.println(c.afoo());
    }
   }
   
   class C extends B{
       boolean shadow;
       int var5;
       int var6;
       public int run(){
           int var5;
           int ret;
           var5 = 99;
           if(shadow){
               ret = var5;
           }
           else{
               ret = 101;
           }
           return ret;
       }
       public int test(boolean var3, int var4, int var2){
           int _ret;
           if(var3){
               _ret = var4;
           }
           else{
               _ret = var2;
           }
           return _ret;
       }
       public int runC(){
           int alpha;
           var4 = 6;
           var5 = 5;
           var2 = 18; 
           shadow = true;
           if(shadow){
               alpha = 3;
           }
           else{
               alpha = 0;
           }
           return alpha;
       }
       
   }
   
   class B extends A{
       int[] shadow;
       int var3;
       int var4;
       public int runB(){
           shadow = new int[4];
           shadow[3] = 55;
           return shadow[3];
       }
       public int[] getShadow(){
           return shadow;
       }
   }
   
   class A{
       int var1;
       int var2;
       Foo shadow;
       public int run(){
           return 1;
       }
       public int init(){
           shadow = new Foo();
           return 0;
       }
       public int afoo(){
           return shadow.bar(5);
       }
   }
   
   class Foo{
       int x;
       public int bar(int y){
           x = x + y;
           return x;
       }
   }