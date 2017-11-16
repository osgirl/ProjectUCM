package intradoc.tools.common;

public abstract interface ClassFileConstants
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99326 $";
  public static final short ACC_PUBLIC = 1;
  public static final short ACC_PRIVATE = 2;
  public static final short ACC_PROTECTED = 4;
  public static final short ACC_STATIC = 8;
  public static final short ACC_FINAL = 16;
  public static final short ACC_SUPER = 32;
  public static final short ACC_SYNCHRONIZED = 32;
  public static final short ACC_VOLATILE = 64;
  public static final short ACC_BRIDGE = 64;
  public static final short ACC_TRANSIENT = 128;
  public static final short ACC_VARARGS = 128;
  public static final short ACC_NATIVE = 256;
  public static final short ACC_INTERFACE = 512;
  public static final short ACC_ABSTRACT = 1024;
  public static final short ACC_STRICT = 2048;
  public static final short ACC_SYNTHETIC = 4096;
  public static final short ACC_ANNOTATION = 8192;
  public static final short ACC_ENUM = 16384;
  public static final short CLASS_ACCESS_FLAGS = 30257;
  public static final short FIELD_ACCESS_FLAGS = 20703;
  public static final short METHOD_ACCESS_FLAGS = 7679;
  public static final int CLASSFILE_MAGIC = -889275714;
  public static final byte CONSTANT_Utf8 = 1;
  public static final byte CONSTANT_Integer = 3;
  public static final byte CONSTANT_Float = 4;
  public static final byte CONSTANT_Long = 5;
  public static final byte CONSTANT_Double = 6;
  public static final byte CONSTANT_Class = 7;
  public static final byte CONSTANT_String = 8;
  public static final byte CONSTANT_Fieldref = 9;
  public static final byte CONSTANT_Methodref = 10;
  public static final byte CONSTANT_InterfaceMethodref = 11;
  public static final byte CONSTANT_NameAndType = 12;
  public static final byte CONSTANT_MethodHandle = 15;
  public static final byte CONSTANT_MethodType = 16;
  public static final byte CONSTANT_InvokeDynamic = 18;
  public static final byte REF_getField = 1;
  public static final byte REF_getStatic = 2;
  public static final byte REF_putField = 3;
  public static final byte REF_putStatic = 4;
  public static final byte REF_invokeVirtual = 5;
  public static final byte REF_invokeStatic = 6;
  public static final byte REF_invokeSpecial = 7;
  public static final byte REF_newInvokeSpecial = 8;
  public static final byte REF_invokeInterface = 9;
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.tools.common.ClassFileConstants
 * JD-Core Version:    0.5.4
 */