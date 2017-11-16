/*     */ package intradoc.tools.common;
/*     */ 
/*     */ import java.io.DataInputStream;
/*     */ import java.io.DataOutputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.OutputStream;
/*     */ import java.lang.annotation.Annotation;
/*     */ import java.lang.annotation.Retention;
/*     */ import java.lang.annotation.RetentionPolicy;
/*     */ import java.lang.annotation.Target;
/*     */ import java.lang.reflect.Modifier;
/*     */ import java.nio.charset.Charset;
/*     */ 
/*     */ public abstract class ClassFile
/*     */   implements ClassFileConstants
/*     */ {
/*     */   protected static Charset s_UTF8Charset;
/*     */   public short minor_version;
/*     */   public short major_version;
/*     */   public cp_info[] constant_pool;
/*     */   public short access_flags;
/*     */   public short this_class;
/*     */   public short super_class;
/*     */   public short[] interfaces;
/*     */   public field_info[] fields;
/*     */   public method_info[] methods;
/*     */   public attribute_info[] attributes;
/*     */ 
/*     */   public ClassFile()
/*     */   {
/* 164 */     if (s_UTF8Charset != null)
/*     */       return;
/* 166 */     s_UTF8Charset = Charset.forName("UTF-8");
/*     */   }
/*     */ 
/*     */   protected void appendConstantPoolIndexTo(StringBuilder sb, short index)
/*     */   {
/* 173 */     cp_info[] pool = this.constant_pool;
/* 174 */     int numConstants = (pool != null) ? pool.length : 0;
/* 175 */     int indexUnsigned = index & 0xFFFF;
/* 176 */     if ((indexUnsigned > 0) && (indexUnsigned < numConstants))
/*     */     {
/* 178 */       pool[index].appendTo(sb);
/*     */     }
/*     */     else
/*     */     {
/* 182 */       sb.append('?');
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void appendTo(StringBuilder sb) {
/* 187 */     int accessFlags = this.access_flags & 0x7611;
/* 188 */     String modifiers = Modifier.toString(accessFlags);
/* 189 */     sb.append(modifiers);
/* 190 */     if ((accessFlags & 0x4000) != 0)
/*     */     {
/* 192 */       sb.append(" enum");
/*     */     }
/* 194 */     else if ((accessFlags & 0x200) == 0)
/*     */     {
/* 196 */       sb.append(" class");
/*     */     }
/* 198 */     sb.append(' ');
/* 199 */     appendConstantPoolIndexTo(sb, this.this_class);
/* 200 */     short superClass = this.super_class;
/* 201 */     if (superClass != 0)
/*     */     {
/* 203 */       sb.append(" extends ");
/* 204 */       appendConstantPoolIndexTo(sb, superClass);
/*     */     }
/* 206 */     short[] ifs = this.interfaces;
/* 207 */     int numInterfaces = (ifs != null) ? ifs.length : 0;
/* 208 */     if (numInterfaces <= 0)
/*     */       return;
/* 210 */     sb.append("\nimplements");
/* 211 */     for (int i = 0; i < numInterfaces; ++i)
/*     */     {
/* 213 */       sb.append((i > 0) ? ", " : " ");
/* 214 */       appendConstantPoolIndexTo(sb, ifs[i]);
/*     */     }
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 222 */     StringBuilder sb = new StringBuilder();
/* 223 */     appendTo(sb);
/* 224 */     return sb.toString();
/*     */   }
/*     */ 
/*     */   public void loadFromStream(InputStream is)
/*     */     throws IOException
/*     */   {
/* 238 */     DataInputStream dis = (is instanceof DataInputStream) ? (DataInputStream)is : new DataInputStream(is);
/* 239 */     int magic = dis.readInt();
/* 240 */     if (magic != -889275714)
/*     */     {
/* 242 */       throw new IOException(new StringBuilder().append("Bad classfile magic: 0x").append(Integer.toString(magic, 16)).toString());
/*     */     }
/* 244 */     this.minor_version = dis.readShort();
/* 245 */     this.major_version = dis.readShort();
/* 246 */     checkVersion();
/* 247 */     this.constant_pool = loadConstantPool(dis);
/* 248 */     this.access_flags = dis.readShort();
/* 249 */     this.this_class = dis.readShort();
/* 250 */     this.super_class = dis.readShort();
/*     */ 
/* 252 */     int numInterfaces = dis.readShort() & 0xFFFF;
/* 253 */     short[] ifs = this.interfaces = new short[numInterfaces];
/* 254 */     for (int i = 0; i < numInterfaces; ++i)
/*     */     {
/* 256 */       ifs[i] = dis.readShort();
/*     */     }
/* 258 */     int numFields = dis.readShort() & 0xFFFF;
/* 259 */     field_info[] fis = this.fields = new field_info[numFields];
/* 260 */     for (int f = 0; f < numFields; ++f)
/*     */     {
/* 262 */       fis[f] = loadField(dis);
/*     */     }
/* 264 */     int numMethods = dis.readShort() & 0xFFFF;
/* 265 */     method_info[] mis = this.methods = new method_info[numMethods];
/* 266 */     for (int m = 0; m < numMethods; ++m)
/*     */     {
/* 268 */       mis[m] = loadMethod(dis);
/*     */     }
/* 270 */     this.attributes = loadAttributes(dis);
/*     */   }
/*     */   protected abstract void checkVersion() throws IOException;
/*     */ 
/*     */   protected cp_info[] loadConstantPool(DataInputStream dis) throws IOException {
/* 275 */     int numConstants = dis.readShort() & 0xFFFF;
/* 276 */     if (numConstants < 1)
/*     */     {
/* 278 */       throw new IOException(new StringBuilder().append("constant pool count out of range: ").append(numConstants).toString());
/*     */     }
/* 280 */     cp_info[] pool = new cp_info[numConstants];
/* 281 */     for (int c = 1; c < numConstants; ++c)
/*     */     {
/* 283 */       byte tag = dis.readByte();
/* 284 */       pool[c] = loadConstantPoolItem(dis, tag);
/* 285 */       if ((tag != 5) && (tag != 6))
/*     */         continue;
/* 287 */       ++c;
/*     */     }
/*     */ 
/* 290 */     return pool;
/*     */   }
/*     */   protected abstract cp_info loadConstantPoolItem(DataInputStream paramDataInputStream, byte paramByte) throws IOException;
/*     */ 
/*     */   protected field_info loadField(DataInputStream dis) throws IOException {
/* 295 */     field_info field = new field_info();
/* 296 */     field.access_flags = dis.readShort();
/* 297 */     field.name_index = dis.readShort();
/* 298 */     field.descriptor_index = dis.readShort();
/* 299 */     field.attributes = loadAttributes(dis);
/* 300 */     return field;
/*     */   }
/*     */ 
/*     */   protected method_info loadMethod(DataInputStream dis) throws IOException {
/* 304 */     method_info method = new method_info();
/* 305 */     method.access_flags = dis.readShort();
/* 306 */     method.name_index = dis.readShort();
/* 307 */     method.descriptor_index = dis.readShort();
/* 308 */     method.attributes = loadAttributes(dis);
/* 309 */     return method;
/*     */   }
/*     */ 
/*     */   protected attribute_info[] loadAttributes(DataInputStream dis) throws IOException {
/* 313 */     int numAttributes = dis.readShort() & 0xFFFF;
/* 314 */     attribute_info[] ais = new attribute_info[numAttributes];
/* 315 */     for (int a = 0; a < numAttributes; ++a)
/*     */     {
/* 317 */       short attrNameIndex = dis.readShort();
/* 318 */       ais[a] = loadAttribute(dis, attrNameIndex);
/*     */     }
/* 320 */     return ais;
/*     */   }
/*     */ 
/*     */   protected abstract attribute_info loadAttribute(DataInputStream paramDataInputStream, short paramShort)
/*     */     throws IOException;
/*     */ 
/*     */   public void saveToStream(OutputStream os)
/*     */     throws IOException
/*     */   {
/* 335 */     DataOutputStream dos = (os instanceof DataOutputStream) ? (DataOutputStream)os : new DataOutputStream(os);
/*     */ 
/* 337 */     dos.writeInt(-889275714);
/* 338 */     dos.writeShort(this.minor_version);
/* 339 */     dos.writeShort(this.major_version);
/* 340 */     saveConstantPool(dos);
/* 341 */     dos.writeShort(this.access_flags);
/* 342 */     dos.writeShort(this.this_class);
/* 343 */     dos.writeShort(this.super_class);
/*     */ 
/* 345 */     short[] ifs = this.interfaces;
/* 346 */     int numInterfaces = ifs.length;
/* 347 */     if (numInterfaces > 65535)
/*     */     {
/* 349 */       throw new IOException("too many interfaces");
/*     */     }
/* 351 */     dos.writeShort(numInterfaces);
/* 352 */     for (int i = 0; i < numInterfaces; ++i)
/*     */     {
/* 354 */       dos.writeShort(ifs[i]);
/*     */     }
/*     */ 
/* 357 */     field_info[] fis = this.fields;
/* 358 */     int numFields = fis.length;
/* 359 */     if (numFields > 65535)
/*     */     {
/* 361 */       throw new IOException("too many fields");
/*     */     }
/* 363 */     dos.writeShort(numFields);
/* 364 */     for (int f = 0; f < numFields; ++f)
/*     */     {
/* 366 */       saveField(dos, fis[f]);
/*     */     }
/*     */ 
/* 369 */     method_info[] mis = this.methods;
/* 370 */     int numMethods = mis.length;
/* 371 */     if (numMethods > 65535)
/*     */     {
/* 373 */       throw new IOException("too many methods");
/*     */     }
/* 375 */     dos.writeShort(numMethods);
/* 376 */     for (int m = 0; m < numMethods; ++m)
/*     */     {
/* 378 */       saveMethod(dos, mis[m]);
/*     */     }
/*     */ 
/* 381 */     saveAttributes(dos, this.attributes);
/*     */   }
/*     */ 
/*     */   protected void saveConstantPool(DataOutputStream dos) throws IOException {
/* 385 */     cp_info[] pool = this.constant_pool;
/* 386 */     int numConstants = pool.length;
/* 387 */     if (numConstants > 65535)
/*     */     {
/* 389 */       throw new IOException("too many constants");
/*     */     }
/* 391 */     if (numConstants < 1)
/*     */     {
/* 393 */       throw new IOException("no constants");
/*     */     }
/* 395 */     dos.writeShort(numConstants);
/* 396 */     for (int c = 1; c < numConstants; ++c)
/*     */     {
/* 398 */       cp_info item = pool[c];
/* 399 */       byte tag = item.tag;
/* 400 */       dos.writeByte(tag);
/* 401 */       saveConstantPoolItem(dos, item);
/* 402 */       if ((tag != 5) && (tag != 6))
/*     */         continue;
/* 404 */       ++c;
/*     */     }
/*     */   }
/*     */ 
/*     */   protected abstract void saveConstantPoolItem(DataOutputStream paramDataOutputStream, cp_info paramcp_info) throws IOException;
/*     */ 
/*     */   protected void saveField(DataOutputStream dos, field_info field) throws IOException {
/* 411 */     dos.writeShort(field.access_flags);
/* 412 */     dos.writeShort(field.name_index);
/* 413 */     dos.writeShort(field.descriptor_index);
/* 414 */     saveAttributes(dos, field.attributes);
/*     */   }
/*     */ 
/*     */   protected void saveMethod(DataOutputStream dos, method_info method) throws IOException {
/* 418 */     dos.writeShort(method.access_flags);
/* 419 */     dos.writeShort(method.name_index);
/* 420 */     dos.writeShort(method.descriptor_index);
/* 421 */     saveAttributes(dos, method.attributes);
/*     */   }
/*     */ 
/*     */   protected void saveAttributes(DataOutputStream dos, attribute_info[] attrs) throws IOException {
/* 425 */     int numAttributes = attrs.length;
/* 426 */     if (numAttributes > 65535)
/*     */     {
/* 428 */       throw new IOException("too many attributes");
/*     */     }
/* 430 */     dos.writeShort(numAttributes);
/* 431 */     for (int a = 0; a < numAttributes; ++a)
/*     */     {
/* 433 */       attribute_info attr = attrs[a];
/* 434 */       dos.writeShort(attr.attribute_name_index);
/* 435 */       saveAttribute(dos, attr);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected abstract void saveAttribute(DataOutputStream paramDataOutputStream, attribute_info paramattribute_info) throws IOException;
/*     */ 
/*     */   protected int getAttributeLength(attribute_info attribute) {
/* 441 */     return attribute.info.length;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 447 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99523 $";
/*     */   }
/*     */ 
/*     */   public class attribute_info extends ClassFile.BaseInnerClass
/*     */   {
/*     */ 
/*     */     @ClassFile.ConstantPoolIndex
/*     */     public short attribute_name_index;
/*     */     public byte[] info;
/*     */ 
/*     */     public attribute_info()
/*     */     {
/* 142 */       super(ClassFile.this);
/*     */     }
/*     */ 
/*     */     protected void appendTo(StringBuilder sb)
/*     */     {
/* 151 */       ClassFile.this.appendConstantPoolIndexTo(sb, this.attribute_name_index);
/*     */     }
/*     */ 
/*     */     public String toString()
/*     */     {
/* 156 */       StringBuilder sb = new StringBuilder();
/* 157 */       appendTo(sb);
/* 158 */       return sb.toString();
/*     */     }
/*     */   }
/*     */ 
/*     */   public class method_info extends ClassFile.FieldOrMethodInfo
/*     */   {
/*     */     public method_info()
/*     */     {
/* 127 */       super(ClassFile.this);
/*     */     }
/*     */ 
/*     */     protected void appendTo(StringBuilder sb)
/*     */     {
/* 132 */       int accessFlags = this.access_flags & 0x1D3F;
/* 133 */       String modifiers = Modifier.toString(accessFlags);
/* 134 */       sb.append(modifiers);
/* 135 */       sb.append(' ');
/* 136 */       ClassFile.this.appendConstantPoolIndexTo(sb, this.name_index);
/* 137 */       sb.append(' ');
/* 138 */       ClassFile.this.appendConstantPoolIndexTo(sb, this.descriptor_index);
/*     */     }
/*     */   }
/*     */ 
/*     */   public class field_info extends ClassFile.FieldOrMethodInfo
/*     */   {
/*     */     public field_info()
/*     */     {
/* 113 */       super(ClassFile.this);
/*     */     }
/*     */ 
/*     */     protected void appendTo(StringBuilder sb)
/*     */     {
/* 118 */       int accessFlags = this.access_flags & 0x50DF;
/* 119 */       String modifiers = Modifier.toString(accessFlags);
/* 120 */       sb.append(modifiers);
/* 121 */       sb.append(' ');
/* 122 */       ClassFile.this.appendConstantPoolIndexTo(sb, this.descriptor_index);
/* 123 */       sb.append(' ');
/* 124 */       ClassFile.this.appendConstantPoolIndexTo(sb, this.name_index);
/*     */     }
/*     */   }
/*     */ 
/*     */   public abstract class FieldOrMethodInfo extends ClassFile.BaseInnerClass
/*     */   {
/*     */     public short access_flags;
/*     */ 
/*     */     @ClassFile.ConstantPoolIndex
/*     */     public short name_index;
/*     */ 
/*     */     @ClassFile.ConstantPoolIndex
/*     */     public short descriptor_index;
/*     */     public ClassFile.attribute_info[] attributes;
/*     */ 
/*     */     public FieldOrMethodInfo()
/*     */     {
/*  94 */       super(ClassFile.this);
/*     */     }
/*     */ 
/*     */     protected abstract void appendTo(StringBuilder paramStringBuilder);
/*     */ 
/*     */     public String toString()
/*     */     {
/* 108 */       StringBuilder sb = new StringBuilder();
/* 109 */       appendTo(sb);
/* 110 */       return sb.toString();
/*     */     }
/*     */   }
/*     */ 
/*     */   public abstract class cp_info extends ClassFile.BaseInnerClass
/*     */   {
/*     */     public byte tag;
/*     */ 
/*     */     protected cp_info(byte cpTag)
/*     */     {
/*  81 */       super(ClassFile.this);
/*  82 */       this.tag = cpTag;
/*     */     }
/*     */ 
/*     */     protected abstract void appendTo(StringBuilder paramStringBuilder);
/*     */ 
/*     */     public String toString() {
/*  88 */       StringBuilder sb = new StringBuilder();
/*  89 */       appendTo(sb);
/*  90 */       return sb.toString();
/*     */     }
/*     */   }
/*     */ 
/*     */   public abstract class BaseInnerClass
/*     */   {
/*     */     public BaseInnerClass()
/*     */     {
/*     */     }
/*     */ 
/*     */     public ClassFile getClassFile()
/*     */     {
/*  72 */       return ClassFile.this;
/*     */     }
/*     */   }
/*     */ 
/*     */   @Retention(RetentionPolicy.RUNTIME)
/*     */   @Target({java.lang.annotation.ElementType.FIELD})
/*     */   protected static @interface ConstantPoolIndex
/*     */   {
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.tools.common.ClassFile
 * JD-Core Version:    0.5.4
 */