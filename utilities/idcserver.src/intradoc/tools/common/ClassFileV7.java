/*     */ package intradoc.tools.common;
/*     */ 
/*     */ import java.io.DataInputStream;
/*     */ import java.io.DataOutputStream;
/*     */ import java.io.IOException;
/*     */ 
/*     */ public class ClassFileV7 extends ClassFileV6
/*     */ {
/*     */   protected void checkVersion()
/*     */     throws IOException
/*     */   {
/* 118 */     int major = this.major_version;
/* 119 */     if ((major >= 45) && (major <= 51))
/*     */       return;
/* 121 */     String msg = "classfile version out of range [45.0, 51.0]: " + major + (this.minor_version & 0xFFFF);
/* 122 */     throw new IOException(msg);
/*     */   }
/*     */ 
/*     */   protected ClassFile.cp_info loadConstantPoolItem(DataInputStream dis, byte tag)
/*     */     throws IOException
/*     */   {
/* 130 */     switch (tag) {
/*     */     case 15:
/* 133 */       byte referenceKind = dis.readByte();
/* 134 */       short referenceIndex = dis.readShort();
/* 135 */       return new CONSTANT_MethodHandle_info(referenceKind, referenceIndex);
/*     */     case 16:
/* 137 */       short descriptorIndex = dis.readShort();
/* 138 */       return new CONSTANT_MethodType_info(descriptorIndex);
/*     */     case 18:
/* 140 */       short methodAttrIndex = dis.readShort();
/* 141 */       short nameAndTypeIndex = dis.readShort();
/* 142 */       return new CONSTANT_InvokeDynamic_info(methodAttrIndex, nameAndTypeIndex);
/*     */     case 17:
/*     */     }
/* 144 */     return super.loadConstantPoolItem(dis, tag);
/*     */   }
/*     */ 
/*     */   protected ClassFile.attribute_info loadAttribute(DataInputStream dis, short attrNameIndex, String attrName, int attrLength)
/*     */     throws IOException
/*     */   {
/* 150 */     if (attrName.equals("BootstrapMethods"))
/*     */     {
/* 152 */       if (attrLength < 2)
/*     */       {
/* 154 */         throw new IOException("bad attribute length for Bootstrapmethods: " + attrLength);
/*     */       }
/* 156 */       BootstrapMethods_attribute attr = new BootstrapMethods_attribute();
/* 157 */       attr.attribute_name_index = attrNameIndex;
/* 158 */       int numMethods = dis.readShort() & 0xFFFF;
/* 159 */       ClassFileV7.BootstrapMethods_attribute.Method[] bsMethods = attr.bootstrap_methods = new ClassFileV7.BootstrapMethods_attribute.Method[numMethods];
/*     */ 
/* 161 */       for (int m = 0; m < numMethods; ++m)
/*     */       {
/*     */         BootstrapMethods_attribute tmp101_99 = attr; tmp101_99.getClass(); ClassFileV7.BootstrapMethods_attribute.Method method = bsMethods[m] =  = new ClassFileV7.BootstrapMethods_attribute.Method(tmp101_99);
/* 164 */         method.bootstrap_method_ref = dis.readShort();
/* 165 */         int numArgs = dis.readShort() & 0xFFFF;
/* 166 */         short[] args = method.bootstrap_arguments = new short[numArgs];
/* 167 */         for (int a = 0; a < numArgs; ++a)
/*     */         {
/* 169 */           args[a] = dis.readShort();
/*     */         }
/*     */       }
/* 172 */       int actualLength = getAttributeLength(attr);
/* 173 */       if (actualLength != attrLength)
/*     */       {
/* 175 */         throw new IOException("attribute length mismatch: " + actualLength + " (expected " + attrLength + ")");
/*     */       }
/* 177 */       return attr;
/*     */     }
/*     */ 
/* 180 */     return super.loadAttribute(dis, attrNameIndex, attrName, attrLength);
/*     */   }
/*     */ 
/*     */   protected void saveConstantPoolItem(DataOutputStream dos, ClassFile.cp_info item)
/*     */     throws IOException
/*     */   {
/* 186 */     byte tag = item.tag;
/* 187 */     switch (tag) {
/*     */     case 15:
/* 190 */       CONSTANT_MethodHandle_info methodHandleInfo = (CONSTANT_MethodHandle_info)item;
/* 191 */       dos.writeByte(methodHandleInfo.reference_kind);
/* 192 */       dos.writeShort(methodHandleInfo.reference_index);
/* 193 */       return;
/*     */     case 16:
/* 195 */       CONSTANT_MethodType_info methodTypeInfo = (CONSTANT_MethodType_info)item;
/* 196 */       dos.writeShort(methodTypeInfo.descriptor_index);
/* 197 */       return;
/*     */     case 18:
/* 199 */       CONSTANT_InvokeDynamic_info invokeDynamicInfo = (CONSTANT_InvokeDynamic_info)item;
/* 200 */       dos.writeShort(invokeDynamicInfo.bootstrap_method_attr_index);
/* 201 */       dos.writeShort(invokeDynamicInfo.name_and_type_index);
/* 202 */       return;
/*     */     case 17:
/*     */     }
/* 204 */     super.saveConstantPoolItem(dos, item);
/*     */   }
/*     */ 
/*     */   protected void saveAttribute(DataOutputStream dos, ClassFile.attribute_info attribute) throws IOException
/*     */   {
/* 209 */     if (attribute instanceof BootstrapMethods_attribute)
/*     */     {
/* 211 */       int length = getAttributeLength(attribute);
/* 212 */       dos.writeInt(length);
/* 213 */       BootstrapMethods_attribute attr = (BootstrapMethods_attribute)attribute;
/* 214 */       ClassFileV7.BootstrapMethods_attribute.Method[] bsMethods = attr.bootstrap_methods;
/* 215 */       int numMethods = bsMethods.length;
/* 216 */       if (numMethods > 65535)
/*     */       {
/* 218 */         throw new IOException("too many methods");
/*     */       }
/* 220 */       dos.writeShort(numMethods);
/* 221 */       for (int m = 0; m < numMethods; ++m)
/*     */       {
/* 223 */         ClassFileV7.BootstrapMethods_attribute.Method method = bsMethods[m];
/* 224 */         dos.writeShort(method.bootstrap_method_ref);
/* 225 */         short[] args = method.bootstrap_arguments;
/* 226 */         int numArgs = args.length;
/* 227 */         if (numArgs > 65535)
/*     */         {
/* 229 */           throw new IOException("too many arguments");
/*     */         }
/* 231 */         dos.writeShort(numArgs);
/* 232 */         for (int a = 0; a < numArgs; ++a)
/*     */         {
/* 234 */           dos.writeShort(args[a]);
/*     */         }
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 240 */       super.saveAttribute(dos, attribute);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected int getAttributeLength(ClassFile.attribute_info attribute)
/*     */   {
/* 246 */     if (attribute instanceof BootstrapMethods_attribute)
/*     */     {
/* 248 */       BootstrapMethods_attribute attr = (BootstrapMethods_attribute)attribute;
/* 249 */       ClassFileV7.BootstrapMethods_attribute.Method[] bsMethods = attr.bootstrap_methods;
/* 250 */       int numMethods = bsMethods.length;
/* 251 */       int length = 2 + 4 * numMethods;
/* 252 */       for (int m = 0; m < numMethods; ++m)
/*     */       {
/* 254 */         ClassFileV7.BootstrapMethods_attribute.Method method = bsMethods[m];
/* 255 */         length += 2 * method.bootstrap_arguments.length;
/*     */       }
/* 257 */       return length;
/*     */     }
/*     */ 
/* 260 */     return super.getAttributeLength(attribute);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 266 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99523 $";
/*     */   }
/*     */ 
/*     */   public class BootstrapMethods_attribute extends ClassFile.attribute_info
/*     */   {
/*     */     public Method[] bootstrap_methods;
/*     */ 
/*     */     public BootstrapMethods_attribute()
/*     */     {
/* 100 */       super(ClassFileV7.this);
/*     */     }
/*     */ 
/*     */     public class Method
/*     */     {
/*     */ 
/*     */       @ClassFile.ConstantPoolIndex
/*     */       public short bootstrap_method_ref;
/*     */ 
/*     */       @ClassFile.ConstantPoolIndex
/*     */       public short[] bootstrap_arguments;
/*     */ 
/*     */       public Method()
/*     */       {
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public class CONSTANT_InvokeDynamic_info extends ClassFile.cp_info
/*     */   {
/*     */ 
/*     */     @ClassFile.ConstantPoolIndex
/*     */     public short bootstrap_method_attr_index;
/*     */ 
/*     */     @ClassFile.ConstantPoolIndex
/*     */     public short name_and_type_index;
/*     */ 
/*     */     public CONSTANT_InvokeDynamic_info(short bootstrapMethodAttrIndex, short nameAndTypeIndex)
/*     */     {
/*  86 */       super(ClassFileV7.this, 18);
/*  87 */       this.bootstrap_method_attr_index = bootstrapMethodAttrIndex;
/*  88 */       this.name_and_type_index = nameAndTypeIndex;
/*     */     }
/*     */ 
/*     */     protected void appendTo(StringBuilder sb)
/*     */     {
/*  93 */       sb.append("bootstrap ");
/*  94 */       ClassFileV7.this.appendConstantPoolIndexTo(sb, this.bootstrap_method_attr_index);
/*  95 */       sb.append(" for ");
/*  96 */       ClassFileV7.this.appendConstantPoolIndexTo(sb, this.name_and_type_index);
/*     */     }
/*     */   }
/*     */ 
/*     */   public class CONSTANT_MethodType_info extends ClassFile.cp_info
/*     */   {
/*     */ 
/*     */     @ClassFile.ConstantPoolIndex
/*     */     public short descriptor_index;
/*     */ 
/*     */     public CONSTANT_MethodType_info(short descriptorIndex)
/*     */     {
/*  69 */       super(ClassFileV7.this, 16);
/*  70 */       this.descriptor_index = descriptorIndex;
/*     */     }
/*     */ 
/*     */     protected void appendTo(StringBuilder sb)
/*     */     {
/*  75 */       sb.append("method type ");
/*  76 */       ClassFileV7.this.appendConstantPoolIndexTo(sb, this.descriptor_index);
/*     */     }
/*     */   }
/*     */ 
/*     */   public class CONSTANT_MethodHandle_info extends ClassFile.cp_info
/*     */   {
/*     */     public byte reference_kind;
/*     */ 
/*     */     @ClassFile.ConstantPoolIndex
/*     */     public short reference_index;
/*     */ 
/*     */     public CONSTANT_MethodHandle_info(byte referenceKind, short referenceIndex)
/*     */     {
/*  37 */       super(ClassFileV7.this, 15);
/*  38 */       this.reference_kind = referenceKind;
/*  39 */       this.reference_index = referenceIndex;
/*     */     }
/*     */ 
/*     */     protected void appendTo(StringBuilder sb)
/*     */     {
/*  44 */       sb.append("method handle ");
/*  45 */       switch (this.reference_kind)
/*     */       {
/*     */       case 1:
/*  47 */         sb.append("getField"); break;
/*     */       case 2:
/*  48 */         sb.append("getStatic"); break;
/*     */       case 3:
/*  49 */         sb.append("putField"); break;
/*     */       case 4:
/*  50 */         sb.append("putStatic"); break;
/*     */       case 5:
/*  51 */         sb.append("invokeVirtual"); break;
/*     */       case 6:
/*  52 */         sb.append("invokeStatic"); break;
/*     */       case 7:
/*  53 */         sb.append("invokeSpecial"); break;
/*     */       case 8:
/*  54 */         sb.append("newInvokeSpecial"); break;
/*     */       case 9:
/*  55 */         sb.append("invokeInterface"); break;
/*     */       default:
/*  56 */         sb.append('('); sb.append(this.reference_kind); sb.append(')');
/*     */       }
/*  58 */       sb.append(" ref ");
/*  59 */       ClassFileV7.this.appendConstantPoolIndexTo(sb, this.reference_index);
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.tools.common.ClassFileV7
 * JD-Core Version:    0.5.4
 */