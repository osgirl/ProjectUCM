/*     */ package intradoc.soap.custom;
/*     */ 
/*     */ import intradoc.common.PropertiesTreeNode;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.soap.SoapUtils;
/*     */ import intradoc.soap.SoapXmlSerializer;
/*     */ import java.io.IOException;
/*     */ import java.io.Writer;
/*     */ 
/*     */ public class SoapCustomFieldData extends SoapCustomElementData
/*     */ {
/*  30 */   public static int STRING = 0;
/*  31 */   public static int INTEGER = 1;
/*  32 */   public static int FLOAT = 2;
/*  33 */   public static int BOOLEAN = 3;
/*  34 */   public static int DATE = 4;
/*     */   public int m_fieldType;
/*     */ 
/*     */   public SoapCustomFieldData()
/*     */   {
/*  36 */     this.m_fieldType = -1;
/*     */   }
/*     */ 
/*     */   public void init(String name, String type, String subType, String idcName)
/*     */   {
/*  41 */     super.init(name, type, subType, idcName);
/*     */ 
/*  43 */     subType = subType.toLowerCase();
/*     */ 
/*  45 */     if (subType.equals("string"))
/*     */     {
/*  47 */       this.m_fieldType = STRING;
/*  48 */       this.m_wsdlType = "s:string";
/*     */     }
/*  50 */     else if (subType.startsWith("int"))
/*     */     {
/*  52 */       this.m_fieldType = INTEGER;
/*  53 */       this.m_wsdlType = "s:int";
/*     */     }
/*  55 */     else if (subType.equals("float"))
/*     */     {
/*  57 */       this.m_fieldType = FLOAT;
/*  58 */       this.m_wsdlType = "s:float";
/*     */     }
/*  60 */     else if (subType.startsWith("bool"))
/*     */     {
/*  62 */       this.m_fieldType = BOOLEAN;
/*  63 */       this.m_wsdlType = "s:boolean";
/*     */     }
/*  65 */     else if (subType.equals("date"))
/*     */     {
/*  67 */       this.m_fieldType = DATE;
/*  68 */       this.m_wsdlType = "s:string";
/*     */     }
/*     */     else
/*     */     {
/*  72 */       this.m_fieldType = STRING;
/*  73 */       this.m_wsdlType = "s:string";
/*     */     }
/*     */   }
/*     */ 
/*     */   public void parseRequest(DataBinder data, PropertiesTreeNode node)
/*     */   {
/*  80 */     if (this.m_idcName.equalsIgnoreCase("userOnly"))
/*     */     {
/*  82 */       clearBoolValue(data, this.m_idcName, node.m_value);
/*  83 */       return;
/*     */     }
/*     */ 
/*  86 */     data.putLocal(this.m_idcName, node.m_value);
/*     */   }
/*     */ 
/*     */   public void clearBoolValue(DataBinder data, String key, String value)
/*     */   {
/*  91 */     boolean flag = StringUtils.convertToBool(value, false);
/*  92 */     if (!flag)
/*     */     {
/*  94 */       value = "";
/*     */     }
/*     */ 
/*  97 */     data.putLocal(key, value);
/*     */   }
/*     */ 
/*     */   public void sendResponse(DataBinder data, StringBuffer buffer)
/*     */   {
/* 103 */     String value = data.getLocal(this.m_idcName);
/*     */ 
/* 105 */     if ((value == null) || (value.trim().equals("")))
/*     */     {
/* 107 */       if ((this.m_fieldType == INTEGER) || (this.m_fieldType == BOOLEAN) || (this.m_fieldType == FLOAT))
/*     */       {
/* 110 */         value = "0";
/*     */       }
/*     */       else
/*     */       {
/* 114 */         value = "";
/*     */       }
/*     */ 
/*     */     }
/*     */     else {
/* 119 */       value = SoapUtils.encodeXmlValue(value);
/*     */     }
/*     */ 
/* 122 */     buffer.append("<idc:");
/* 123 */     buffer.append(this.m_name);
/* 124 */     buffer.append(">");
/* 125 */     if (SoapXmlSerializer.m_useCDATA)
/*     */     {
/* 127 */       buffer.append("<![CDATA[");
/*     */     }
/* 129 */     buffer.append(value);
/* 130 */     if (SoapXmlSerializer.m_useCDATA)
/*     */     {
/* 132 */       buffer.append("]]>");
/*     */     }
/* 134 */     buffer.append("</idc:");
/* 135 */     buffer.append(this.m_name);
/* 136 */     buffer.append(">\r\n");
/*     */   }
/*     */ 
/*     */   public void writeSchemaElement(Writer writer, int numTabs)
/*     */     throws IOException
/*     */   {
/* 143 */     SoapUtils.writeSchemaElement(this.m_name, this.m_wsdlType, 1, writer, numTabs);
/*     */   }
/*     */ 
/*     */   public void writeSchemaComplexType(Writer writer, int numTabs)
/*     */     throws IOException
/*     */   {
/*     */   }
/*     */ 
/*     */   public String getDataTypes()
/*     */   {
/* 156 */     return "field:string, field:int, field:date, field:boolean";
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 161 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.soap.custom.SoapCustomFieldData
 * JD-Core Version:    0.5.4
 */