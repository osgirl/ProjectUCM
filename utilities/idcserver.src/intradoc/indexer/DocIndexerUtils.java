/*     */ package intradoc.indexer;
/*     */ 
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.shared.ActiveIndexState;
/*     */ import intradoc.shared.SecurityAccessListUtils;
/*     */ import java.io.UnsupportedEncodingException;
/*     */ import java.util.Iterator;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class DocIndexerUtils
/*     */ {
/*     */   public static void addZoneFields(Map props, Map zoneFields)
/*     */   {
/*  44 */     for (Iterator en = zoneFields.keySet().iterator(); en.hasNext(); )
/*     */     {
/*  46 */       String name = (String)en.next();
/*  47 */       String val = null;
/*  48 */       if ((val = (String)props.get(name)) == null) {
/*     */         continue;
/*     */       }
/*     */ 
/*  52 */       boolean useUpperCaseZoneField = StringUtils.convertToBool(ActiveIndexState.getActiveProperty("UseUpperCaseZonedSecurityField"), false);
/*     */ 
/*  60 */       Vector entityList = StringUtils.parseArray(val, ',', '^');
/*  61 */       int size = entityList.size();
/*  62 */       IdcStringBuilder buff = new IdcStringBuilder();
/*  63 */       for (int i = 0; i < size; ++i)
/*     */       {
/*  65 */         String attribStr = (String)entityList.elementAt(i);
/*  66 */         boolean isPrivilegeStringPresent = false;
/*  67 */         String[] attribs = null;
/*     */ 
/*  69 */         if (attribStr.indexOf(40) >= 0)
/*     */         {
/*  71 */           attribs = SecurityAccessListUtils.parseSecurityFlags(attribStr, "");
/*     */ 
/*  73 */           int priv = NumberUtils.parseInteger(attribs[1], 0);
/*  74 */           if ((priv & 0x1) > 0)
/*     */           {
/*  76 */             isPrivilegeStringPresent = true;
/*     */           }
/*     */           else
/*     */           {
/*  80 */             attribStr = null;
/*     */           }
/*     */         }
/*     */ 
/*  84 */         if ((attribStr == null) || (attribStr.equalsIgnoreCase("idcnull")))
/*     */           continue;
/*  86 */         if (buff.length() > 0)
/*     */         {
/*  88 */           buff.append(" ");
/*     */         }
/*  90 */         if (useUpperCaseZoneField)
/*     */         {
/*  92 */           attribStr = attribStr.toUpperCase();
/*     */         }
/*     */ 
/*  95 */         buff.append("z");
/*     */         try
/*     */         {
/*  98 */           StringUtils.appendAsHex(buff, attribStr);
/*     */ 
/* 100 */           if ((isPrivilegeStringPresent == true) && (attribs != null))
/*     */           {
/* 102 */             buff.append(" z");
/* 103 */             String nameStr = attribs[0];
/* 104 */             if (useUpperCaseZoneField)
/*     */             {
/* 106 */               nameStr = nameStr.toUpperCase();
/*     */             }
/* 108 */             StringUtils.appendAsHex(buff, nameStr);
/*     */           }
/*     */         }
/*     */         catch (UnsupportedEncodingException ignore)
/*     */         {
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 117 */       String str = buff.toString();
/* 118 */       if (str.length() == 0)
/*     */       {
/* 120 */         str = "idcnull";
/*     */       }
/* 122 */       props.put("z" + name, str);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg) {
/* 127 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99260 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.indexer.DocIndexerUtils
 * JD-Core Version:    0.5.4
 */