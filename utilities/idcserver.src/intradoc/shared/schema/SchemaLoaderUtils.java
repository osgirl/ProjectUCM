/*     */ package intradoc.shared.schema;
/*     */ 
/*     */ import intradoc.common.ScriptUtils;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class SchemaLoaderUtils
/*     */ {
/*     */   public static final int F_LOADS_UNCONVERTED = 1;
/*     */   public static final int F_HAS_RELATIONSHIP_TYPES = 2;
/*     */   public static final int F_SUPPORTS_PRIMARY_KEY_SELECTION = 4;
/*     */   public static final int F_SUPPORTS_SORTING = 8;
/*     */   public static final int F_SUPPORTS_FIELD_FILTERING = 16;
/*     */   public static final String S_RELATIONSHIP_IS_BACKWARDS = "isBackwardsRelationship";
/*     */   public static final int F_RELATIONSHIP_IS_BACKWARDS = 1;
/*     */ 
/*     */   public static void addStandardCapabilities(Map capabilities, int flags)
/*     */   {
/*  45 */     Vector keyTypes = new IdcVector();
/*  46 */     if ((flags & 0x2) != 0)
/*     */     {
/*  48 */       keyTypes.addElement(SchemaCacheItem.RELATION_KEY_STRING);
/*     */     }
/*     */ 
/*  51 */     if ((flags & 0x4) != 0)
/*     */     {
/*  53 */       keyTypes.addElement(SchemaCacheItem.PRIMARY_KEY_STRING);
/*     */     }
/*     */ 
/*  56 */     if ((flags & 0x8) != 0)
/*     */     {
/*  58 */       keyTypes.addElement(SchemaCacheItem.SORT_KEY_STRING);
/*     */     }
/*     */ 
/*  61 */     if ((flags & 0x10) != 0)
/*     */     {
/*  63 */       keyTypes.addElement(SchemaCacheItem.FIELD_KEY_STRING);
/*     */     }
/*     */ 
/*  66 */     capabilities.put("keyTypes", keyTypes);
/*  67 */     capabilities.put("flags", new Integer(flags));
/*     */   }
/*     */ 
/*     */   public static boolean supportsCapability(Map capabilities, int flag)
/*     */   {
/*  72 */     Object o = capabilities.get("flags");
/*  73 */     if (o instanceof Integer)
/*     */     {
/*  75 */       Integer flagsInt = (Integer)o;
/*  76 */       return (flag & flagsInt.intValue()) != 0;
/*     */     }
/*  78 */     return false;
/*     */   }
/*     */ 
/*     */   public static boolean supportsLoaderCapability(SchemaViewData viewData, SchemaLoader loader, int flag)
/*     */   {
/*  84 */     Map capabilities = loader.getLoaderCapabilities(viewData, null);
/*  85 */     return supportsCapability(capabilities, flag);
/*     */   }
/*     */ 
/*     */   public static boolean hasKeyTypes(Map capabilities)
/*     */   {
/*  90 */     Object o = capabilities.get("keyTypes");
/*  91 */     return (o != null) && (o instanceof List);
/*     */   }
/*     */ 
/*     */   public static boolean hasKeyType(String keyType, Map capabilities)
/*     */   {
/*  96 */     Object o = capabilities.get("keyTypes");
/*  97 */     if ((o == null) || (!o instanceof List))
/*     */     {
/*  99 */       return false;
/*     */     }
/* 101 */     List keyTypes = (List)o;
/* 102 */     Iterator it = keyTypes.iterator();
/* 103 */     while (it.hasNext())
/*     */     {
/* 105 */       String val = (String)it.next();
/* 106 */       if (keyType.startsWith(val))
/*     */       {
/* 108 */         return true;
/*     */       }
/*     */     }
/*     */ 
/* 112 */     return false;
/*     */   }
/*     */ 
/*     */   public static int computeSchemaCacheItemFlags(Map args, int curFlags)
/*     */   {
/* 117 */     if (args == null)
/*     */     {
/* 119 */       return curFlags;
/*     */     }
/* 121 */     Object backwardsVal = args.get("isBackwardsRelationship");
/* 122 */     if (ScriptUtils.convertObjectToBool(backwardsVal, false))
/*     */     {
/* 124 */       curFlags |= 1;
/*     */     }
/* 126 */     return curFlags;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 131 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.schema.SchemaLoaderUtils
 * JD-Core Version:    0.5.4
 */