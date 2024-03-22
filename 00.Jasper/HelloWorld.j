.source                  HelloWorld.java
.class                   public HelloWorld
.super                   java/lang/Object


.method                  public <init>()V
   .limit stack          1
   .limit locals         1
   .line                 1
   aload_0               
   invokespecial         java/lang/Object/<init>()V
   return                
.end method              

.method                  public static main([Ljava/lang/String;)V
   .limit stack          2
   .limit locals         1
   .line                 3
   getstatic             java/lang/System/out Ljava/io/PrintStream;
   ldc                   "Hello, World!"
   invokevirtual         java/io/PrintStream/println(Ljava/lang/String;)V
   .line                 4
   return                
.end method              

