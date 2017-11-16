/*     */ package intradoc.tools.common;
/*     */ 
/*     */ import intradoc.tools.build.BuildEnvironment;
/*     */ import java.text.DateFormat;
/*     */ import java.util.Map;
/*     */ import java.util.regex.Matcher;
/*     */ import java.util.regex.Pattern;
/*     */ 
/*     */ public class VersionInfoClassFileEditor extends ClassFileEditor
/*     */ {
/*     */   public BuildEnvironment m_env;
/*     */   protected Pattern m_pattern;
/*     */   protected String m_replacement;
/*     */   protected String m_svnTipRev;
/*     */ 
/*     */   public void init(BuildEnvironment env, Long dateInMillis)
/*     */   {
/*  48 */     this.m_env = env;
/*  49 */     this.m_pattern = Pattern.compile("releaseInfo=");
/*  50 */     StringBuilder sb = new StringBuilder();
/*  51 */     sb.append("releaseInfo=");
/*  52 */     sb.append(env.m_productVersionNumber);
/*  53 */     if (dateInMillis != null)
/*     */     {
/*  55 */       sb.append(",relengDate=");
/*  56 */       String dateString = BuildEnvironment.s_iso8601.format(dateInMillis);
/*  57 */       sb.append(dateString);
/*     */     }
/*  59 */     this.m_replacement = sb.toString();
/*     */ 
/*  61 */     Map wcProps = env.m_wcProps;
/*  62 */     if (wcProps == null)
/*     */       return;
/*  64 */     String svnRevision = (String)wcProps.get("svn:entry:committed-rev");
/*  65 */     if (svnRevision == null)
/*     */       return;
/*  67 */     this.m_svnTipRev = svnRevision;
/*     */   }
/*     */ 
/*     */   public boolean alter(ClassFile classfile)
/*     */   {
/*  75 */     ClassFileV2 classfile2 = (ClassFileV2)classfile;
/*  76 */     String className = getClassName(classfile2);
/*  77 */     if ("intradoc/common/VersionInfo".equals(className))
/*     */     {
/*  79 */       return alterVersionInfoClass(classfile2);
/*     */     }
/*  81 */     return alterVersionInfo(classfile2);
/*     */   }
/*     */ 
/*     */   public boolean alterVersionInfo(ClassFileV2 classfile)
/*     */   {
/*  86 */     boolean hasChanged = false;
/*  87 */     Pattern pattern = this.m_pattern;
/*  88 */     String replacement = this.m_replacement;
/*     */ 
/*  90 */     if ((pattern == null) || (replacement == null))
/*     */     {
/*  92 */       return false;
/*     */     }
/*  94 */     ClassFile.cp_info[] constants = classfile.constant_pool;
/*  95 */     for (int c = constants.length - 1; c >= 1; --c)
/*     */     {
/*  97 */       ClassFile.cp_info constant = constants[c];
/*  98 */       if (!constant instanceof ClassFileV2.CONSTANT_String_info)
/*     */         continue;
/* 100 */       ClassFileV2.CONSTANT_String_info stringConstant = (ClassFileV2.CONSTANT_String_info)constant;
/* 101 */       int utf8Index = stringConstant.string_index & 0xFFFF;
/* 102 */       String string = getUTF8StringByIndex(classfile, utf8Index);
/* 103 */       if (string == null)
/*     */         continue;
/* 105 */       Matcher matcher = pattern.matcher(string);
/* 106 */       if (!matcher.find()) {
/*     */         continue;
/*     */       }
/* 109 */       boolean isUseTipRev = true;
/* 110 */       String rev = null;
/* 111 */       String revKeyword = "$Rev:";
/* 112 */       int indexOfRev = string.indexOf(revKeyword);
/* 113 */       if (indexOfRev > 0)
/*     */       {
/* 115 */         int endIndex = string.indexOf("$", indexOfRev + 1);
/* 116 */         if (endIndex > 0)
/*     */         {
/* 118 */           rev = string.substring(indexOfRev + revKeyword.length(), endIndex).trim();
/* 119 */           if (rev.length() > 0)
/*     */           {
/* 121 */             isUseTipRev = false;
/*     */           }
/*     */         }
/*     */       }
/*     */ 
/* 126 */       StringBuilder sb = new StringBuilder(replacement);
/* 127 */       if (isUseTipRev)
/*     */       {
/* 129 */         sb.append(",releaseRevision=$Rev: ");
/* 130 */         sb.append(this.m_svnTipRev);
/* 131 */         sb.append(" $");
/*     */       }
/*     */       else
/*     */       {
/* 135 */         sb.append(",releaseRevision=$Rev: ");
/* 136 */         sb.append(rev);
/* 137 */         sb.append(" $");
/*     */       }
/*     */ 
/* 140 */       replacement = sb.toString();
/*     */       ClassFileV2 tmp263_262 = classfile; tmp263_262.getClass(); constants[utf8Index] = new ClassFileV2.CONSTANT_Utf8_info(tmp263_262, replacement);
/* 142 */       hasChanged = true;
/* 143 */       break;
/*     */     }
/*     */ 
/* 148 */     return hasChanged;
/*     */   }
/*     */ 
/*     */   public boolean alterVersionInfoClass(ClassFileV2 classfile)
/*     */   {
/* 153 */     BuildEnvironment env = this.m_env;
/* 154 */     boolean hasChanged = alterVersionInfo(classfile);
/*     */ 
/* 156 */     String newProductVersionName = env.m_productVersionName;
/* 157 */     if (newProductVersionName != null)
/*     */     {
/* 159 */       hasChanged |= alterStringFieldConstantValue(classfile, "m_productVersion", newProductVersionName);
/*     */     }
/* 161 */     String newProductVersionNumber = env.m_productVersionNumber;
/* 162 */     if (newProductVersionNumber != null)
/*     */     {
/* 164 */       hasChanged |= alterStringFieldConstantValue(classfile, "m_productVersionInfo", newProductVersionNumber);
/*     */     }
/* 166 */     String newProductBuild = env.m_productBuild;
/* 167 */     if (newProductBuild != null)
/*     */     {
/* 169 */       hasChanged |= alterStringFieldConstantValue(classfile, "m_productBuildInfo", newProductBuild);
/*     */     }
/*     */ 
/* 172 */     return hasChanged;
/*     */   }
/*     */ 
/*     */   public boolean alterStringFieldConstantValue(ClassFileV2 classfile, String fieldName, String newValue)
/*     */   {
/* 177 */     ClassFile.field_info field = getFieldByName(classfile, fieldName);
/* 178 */     if (field != null)
/*     */     {
/* 180 */       ClassFile.attribute_info attribute = getAttributeByName(field.attributes, "ConstantValue");
/* 181 */       if (attribute != null)
/*     */       {
/* 183 */         ClassFileV2.ConstantValue_attribute constantValue = (ClassFileV2.ConstantValue_attribute)attribute;
/* 184 */         ClassFile.cp_info[] constants = classfile.constant_pool;
/* 185 */         int valueIndex = constantValue.constantvalue_index & 0xFFFF;
/* 186 */         ClassFile.cp_info constant = constants[valueIndex];
/* 187 */         if (constant instanceof ClassFileV2.CONSTANT_String_info)
/*     */         {
/* 189 */           ClassFileV2.CONSTANT_String_info stringConstant = (ClassFileV2.CONSTANT_String_info)constant;
/* 190 */           int utf8Index = stringConstant.string_index & 0xFFFF;
/* 191 */           String oldValue = getUTF8StringByIndex(classfile, utf8Index);
/* 192 */           if (!newValue.equals(oldValue))
/*     */           {
/*     */             ClassFileV2 tmp113_112 = classfile; tmp113_112.getClass(); constants[utf8Index] = new ClassFileV2.CONSTANT_Utf8_info(tmp113_112, newValue);
/* 195 */             return true;
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/* 200 */     return false;
/*     */   }
/*     */ 
/*     */   public ClassFile.attribute_info getAttributeByName(ClassFile.attribute_info[] attributes, String attributeName)
/*     */   {
/* 206 */     if (attributes != null)
/*     */     {
/* 208 */       for (int a = attributes.length - 1; a >= 0; --a)
/*     */       {
/* 210 */         ClassFile.attribute_info attribute = attributes[a];
/* 211 */         ClassFileV2 classfile = (ClassFileV2)attribute.getClassFile();
/* 212 */         String thisAttributeName = getUTF8StringByIndex(classfile, attribute.attribute_name_index);
/* 213 */         if (attributeName.equals(thisAttributeName))
/*     */         {
/* 215 */           return attribute;
/*     */         }
/*     */       }
/*     */     }
/* 219 */     return null;
/*     */   }
/*     */ 
/*     */   public String getClassName(ClassFileV2 classfile)
/*     */   {
/* 224 */     ClassFile.cp_info[] constants = classfile.constant_pool;
/* 225 */     int thisClassIndex = classfile.this_class & 0xFFFF;
/* 226 */     ClassFile.cp_info thisClass = constants[thisClassIndex];
/* 227 */     if (thisClass instanceof ClassFileV2.CONSTANT_Class_info)
/*     */     {
/* 229 */       ClassFileV2.CONSTANT_Class_info thisClassInfo = (ClassFileV2.CONSTANT_Class_info)thisClass;
/* 230 */       return getUTF8StringByIndex(classfile, thisClassInfo.name_index);
/*     */     }
/* 232 */     return null;
/*     */   }
/*     */ 
/*     */   public ClassFile.field_info getFieldByName(ClassFileV2 classfile, String fieldName)
/*     */   {
/* 237 */     ClassFile.field_info[] fields = classfile.fields;
/* 238 */     for (int f = fields.length - 1; f >= 0; --f)
/*     */     {
/* 240 */       ClassFile.field_info field = fields[f];
/* 241 */       String thisFieldName = getUTF8StringByIndex(classfile, field.name_index);
/* 242 */       if (fieldName.equals(thisFieldName))
/*     */       {
/* 244 */         return field;
/*     */       }
/*     */     }
/* 247 */     return null;
/*     */   }
/*     */ 
/*     */   public String getUTF8StringByIndex(ClassFileV2 classfile, int index)
/*     */   {
/* 252 */     ClassFile.cp_info[] constants = classfile.constant_pool;
/* 253 */     int utf8Index = index & 0xFFFF;
/* 254 */     ClassFile.cp_info utf8Constant = constants[utf8Index];
/* 255 */     if (utf8Constant instanceof ClassFileV2.CONSTANT_Utf8_info)
/*     */     {
/* 257 */       ClassFileV2.CONSTANT_Utf8_info utf8Info = (ClassFileV2.CONSTANT_Utf8_info)utf8Constant;
/* 258 */       return utf8Info.m_string;
/*     */     }
/* 260 */     return null;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 266 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 104057 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.tools.common.VersionInfoClassFileEditor
 * JD-Core Version:    0.5.4
 */