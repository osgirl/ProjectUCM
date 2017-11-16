/*     */ package intradoc.tools.common;
/*     */ 
/*     */ import java.io.DataInputStream;
/*     */ import java.io.DataOutputStream;
/*     */ import java.io.IOException;
/*     */ 
/*     */ public class ClassFileV5 extends ClassFileV2
/*     */ {
/*     */   protected void checkVersion()
/*     */     throws IOException
/*     */   {
/*  73 */     int major = this.major_version;
/*  74 */     if ((major >= 45) && (major <= 49))
/*     */       return;
/*  76 */     String msg = "classfile version out of range [45.0, 49.0]: " + major + (this.minor_version & 0xFFFF);
/*  77 */     throw new IOException(msg);
/*     */   }
/*     */ 
/*     */   protected ClassFile.attribute_info loadAttribute(DataInputStream dis, short attrNameIndex, String attrName, int attrLength)
/*     */     throws IOException
/*     */   {
/*  84 */     if (attrName.equals("EnclosingMethod"))
/*     */     {
/*  86 */       if (attrLength != 4)
/*     */       {
/*  88 */         throw new IOException("bad attribute length for EnclosingMethod: " + attrLength);
/*     */       }
/*  90 */       EnclosingMethod_attribute attr = new EnclosingMethod_attribute();
/*  91 */       attr.attribute_name_index = attrNameIndex;
/*  92 */       attr.class_index = dis.readShort();
/*  93 */       attr.method_index = dis.readShort();
/*  94 */       return attr;
/*     */     }
/*  96 */     if (attrName.equals("Signature"))
/*     */     {
/*  98 */       if (attrLength != 2)
/*     */       {
/* 100 */         throw new IOException("bad attribute length for Signature: " + attrLength);
/*     */       }
/* 102 */       Signature_attribute attr = new Signature_attribute();
/* 103 */       attr.attribute_name_index = attrNameIndex;
/* 104 */       attr.signature_index = dis.readShort();
/* 105 */       return attr;
/*     */     }
/* 107 */     if (attrName.equals("SourceDebugExtension"))
/*     */     {
/* 109 */       byte[] bytes = new byte[attrLength];
/* 110 */       SourceDebugExtension_attribute attr = new SourceDebugExtension_attribute(bytes);
/* 111 */       attr.attribute_name_index = attrNameIndex;
/* 112 */       return attr;
/*     */     }
/* 114 */     if (attrName.equals("LocalVariableTypeTable"))
/*     */     {
/* 116 */       LocalVariableTypeTable_attribute attr = new LocalVariableTypeTable_attribute();
/* 117 */       attr.attribute_name_index = attrNameIndex;
/* 118 */       int numTypes = dis.readShort() & 0xFFFF;
/* 119 */       if (attrLength != 2 + 10 * numTypes)
/*     */       {
/* 121 */         String msg = "bad attribute length for LocalVariableTypeTable: " + attrLength + " (expected " + (2 + 10 * numTypes) + ")";
/*     */ 
/* 124 */         throw new IOException(msg);
/*     */       }
/* 126 */       ClassFileV5.LocalVariableTypeTable_attribute.Type[] types = attr.local_variable_type_table = new ClassFileV5.LocalVariableTypeTable_attribute.Type[numTypes];
/*     */ 
/* 128 */       for (int t = 0; t < numTypes; ++t)
/*     */       {
/*     */         LocalVariableTypeTable_attribute tmp318_316 = attr; tmp318_316.getClass(); ClassFileV5.LocalVariableTypeTable_attribute.Type type = types[t] =  = new ClassFileV5.LocalVariableTypeTable_attribute.Type(tmp318_316);
/* 131 */         type.start_pc = dis.readShort();
/* 132 */         type.length = dis.readShort();
/* 133 */         type.name_index = dis.readShort();
/* 134 */         type.signature_index = dis.readShort();
/* 135 */         type.index = dis.readShort();
/*     */       }
/* 137 */       return attr;
/*     */     }
/*     */ 
/* 140 */     return super.loadAttribute(dis, attrNameIndex, attrName, attrLength);
/*     */   }
/*     */ 
/*     */   protected void saveAttribute(DataOutputStream dos, ClassFile.attribute_info attribute) throws IOException
/*     */   {
/* 145 */     if (attribute instanceof EnclosingMethod_attribute)
/*     */     {
/* 147 */       EnclosingMethod_attribute attr = (EnclosingMethod_attribute)attribute;
/* 148 */       dos.writeInt(4);
/* 149 */       dos.writeShort(attr.class_index);
/* 150 */       dos.writeShort(attr.method_index);
/*     */     }
/* 152 */     else if (attribute instanceof Signature_attribute)
/*     */     {
/* 154 */       Signature_attribute attr = (Signature_attribute)attribute;
/* 155 */       dos.writeInt(2);
/* 156 */       dos.writeShort(attr.signature_index);
/*     */     }
/* 158 */     else if (attribute instanceof SourceDebugExtension_attribute)
/*     */     {
/* 160 */       SourceDebugExtension_attribute attr = (SourceDebugExtension_attribute)attribute;
/* 161 */       byte[] bytes = attr.debug_extension;
/* 162 */       dos.writeInt(bytes.length);
/* 163 */       dos.write(bytes);
/*     */     }
/* 165 */     else if (attribute instanceof LocalVariableTypeTable_attribute)
/*     */     {
/* 167 */       LocalVariableTypeTable_attribute attr = (LocalVariableTypeTable_attribute)attribute;
/* 168 */       ClassFileV5.LocalVariableTypeTable_attribute.Type[] types = attr.local_variable_type_table;
/* 169 */       int numTypes = types.length;
/* 170 */       if (numTypes > 65535)
/*     */       {
/* 172 */         throw new IOException("too many local variables");
/*     */       }
/* 174 */       dos.writeInt(2 + 10 * numTypes);
/* 175 */       dos.writeShort(numTypes);
/* 176 */       for (int t = 0; t < numTypes; ++t)
/*     */       {
/* 178 */         ClassFileV5.LocalVariableTypeTable_attribute.Type type = types[t];
/* 179 */         dos.writeShort(type.start_pc);
/* 180 */         dos.writeShort(type.length);
/* 181 */         dos.writeShort(type.name_index);
/* 182 */         dos.writeShort(type.signature_index);
/* 183 */         dos.writeShort(type.index);
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 188 */       super.saveAttribute(dos, attribute);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected int getAttributeLength(ClassFile.attribute_info attribute)
/*     */   {
/* 194 */     if (attribute instanceof EnclosingMethod_attribute)
/*     */     {
/* 196 */       return 4;
/*     */     }
/* 198 */     if (attribute instanceof Signature_attribute)
/*     */     {
/* 200 */       return 2;
/*     */     }
/* 202 */     if (attribute instanceof SourceDebugExtension_attribute)
/*     */     {
/* 204 */       SourceDebugExtension_attribute attr = (SourceDebugExtension_attribute)attribute;
/* 205 */       return attr.debug_extension.length;
/*     */     }
/* 207 */     if (attribute instanceof LocalVariableTypeTable_attribute)
/*     */     {
/* 209 */       LocalVariableTypeTable_attribute attr = (LocalVariableTypeTable_attribute)attribute;
/* 210 */       return 2 + 10 * attr.local_variable_type_table.length;
/*     */     }
/*     */ 
/* 213 */     return super.getAttributeLength(attribute);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 219 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99523 $";
/*     */   }
/*     */ 
/*     */   public class LocalVariableTypeTable_attribute extends ClassFile.attribute_info
/*     */   {
/*     */     public Type[] local_variable_type_table;
/*     */ 
/*     */     public LocalVariableTypeTable_attribute()
/*     */     {
/*  55 */       super(ClassFileV5.this);
/*     */     }
/*     */ 
/*     */     public class Type
/*     */     {
/*     */       public short start_pc;
/*     */       public short length;
/*     */ 
/*     */       @ClassFile.ConstantPoolIndex
/*     */       public short name_index;
/*     */ 
/*     */       @ClassFile.ConstantPoolIndex
/*     */       public short signature_index;
/*     */       public short index;
/*     */ 
/*     */       public Type()
/*     */       {
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public class SourceDebugExtension_attribute extends ClassFile.attribute_info
/*     */   {
/*     */     public final byte[] debug_extension;
/*     */     public final String m_debugExtension;
/*     */ 
/*     */     public SourceDebugExtension_attribute(byte[] asBytes)
/*     */     {
/*  45 */       super(ClassFileV5.this);
/*  46 */       this.debug_extension = asBytes;
/*  47 */       this.m_debugExtension = new String(asBytes, ClassFile.s_UTF8Charset);
/*     */     }
/*     */     public SourceDebugExtension_attribute(String asString) {
/*  50 */       super(ClassFileV5.this);
/*  51 */       this.m_debugExtension = asString;
/*  52 */       this.debug_extension = asString.getBytes(ClassFile.s_UTF8Charset);
/*     */     }
/*     */   }
/*     */ 
/*     */   public class Signature_attribute extends ClassFile.attribute_info
/*     */   {
/*     */ 
/*     */     @ClassFile.ConstantPoolIndex
/*     */     public short signature_index;
/*     */ 
/*     */     public Signature_attribute()
/*     */     {
/*  34 */       super(ClassFileV5.this);
/*     */     }
/*     */   }
/*     */ 
/*     */   public class EnclosingMethod_attribute extends ClassFile.attribute_info
/*     */   {
/*     */ 
/*     */     @ClassFile.ConstantPoolIndex
/*     */     public short class_index;
/*     */ 
/*     */     @ClassFile.ConstantPoolIndex
/*     */     public short method_index;
/*     */ 
/*     */     public EnclosingMethod_attribute()
/*     */     {
/*  29 */       super(ClassFileV5.this);
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.tools.common.ClassFileV5
 * JD-Core Version:    0.5.4
 */