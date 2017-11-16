/*     */ package intradoc.shared.schema;
/*     */ 
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Map;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class SchemaViewConfig extends SchemaResultSet
/*     */ {
/*     */   public static final int VIEW_NAME = 0;
/*     */   public static final int CANONICAL_NAME = 1;
/*     */   public static final int VIEW_LAST_LOADED = 2;
/*     */   public static final int VIEW_UPTODATE = 3;
/*     */   public static final int VIEW_DESCRIPTION = 4;
/*     */   public static final int VIEW_TABLE_NAME = 5;
/*     */   public static final int VIEW_INTERNAL_COLUMN = 6;
/*     */   public static final int VIEW_SYSTEM = 7;
/*  40 */   public static String[] VIEW_COLUMNS = { "schViewName", "schCanonicalName", "schViewLastLoaded", "schViewIsUpToDate", "schViewDescription", "schTableName", "schInternalColumn", "schIsSystemObject" };
/*     */ 
/*  52 */   protected Vector m_loaders = new IdcVector();
/*     */ 
/*     */   public SchemaViewConfig()
/*     */   {
/*  56 */     super("SchemaViewData", VIEW_COLUMNS);
/*     */   }
/*     */ 
/*     */   public void addLoader(SchemaLoader loader)
/*     */   {
/*  61 */     this.m_loaders.addElement(loader);
/*     */   }
/*     */ 
/*     */   public Vector getLoaders()
/*     */   {
/*  66 */     return (Vector)this.m_loaders.clone();
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public SchemaLoader findLoader(SchemaViewData data)
/*     */   {
/*  74 */     SystemUtils.reportDeprecatedUsage("Using SchemaViewConfig.findLoader() without a relationship.");
/*     */ 
/*  76 */     return findLoader(data, null, null);
/*     */   }
/*     */ 
/*     */   public SchemaLoader findLoader(SchemaViewData view, SchemaRelationData relationship, Map args)
/*     */   {
/*  82 */     int flags = 0;
/*  83 */     flags = SchemaLoaderUtils.computeSchemaCacheItemFlags(args, flags);
/*  84 */     boolean isBackwardsRelationship = (flags & 0x1) != 0;
/*  85 */     synchronized (this.m_loaders)
/*     */     {
/*  87 */       int size = this.m_loaders.size();
/*  88 */       for (int i = 0; i < size; ++i)
/*     */       {
/*  90 */         SchemaLoader loader = (SchemaLoader)this.m_loaders.elementAt(i);
/*     */ 
/*  92 */         if (SystemUtils.m_verbose)
/*     */         {
/*  94 */           Report.debug("schemaloader", "considering loader " + loader + " for view " + view + " and relationship " + relationship, null);
/*     */         }
/*     */ 
/*  97 */         Map capabilities = null;
/*  98 */         if (relationship != null)
/*     */         {
/* 100 */           capabilities = loader.getLoaderCapabilities(relationship, args);
/* 101 */           boolean hasKeyTypes = SchemaLoaderUtils.hasKeyTypes(capabilities);
/* 102 */           if ((hasKeyTypes) && (view != null))
/*     */           {
/* 104 */             SchemaViewData[] childViews = null;
/*     */             try
/*     */             {
/* 107 */               if (isBackwardsRelationship)
/*     */               {
/* 109 */                 childViews = loader.getParentViews(relationship);
/*     */               }
/*     */               else
/*     */               {
/* 113 */                 childViews = loader.getChildViews(relationship);
/*     */               }
/*     */             }
/*     */             catch (DataException e)
/*     */             {
/* 118 */               Report.trace("schemaloader", null, e);
/* 119 */               break label340:
/*     */             }
/* 121 */             boolean found = false;
/* 122 */             for (int j = 0; j < childViews.length; ++j)
/*     */             {
/* 124 */               if (!view.m_name.equals(childViews[j].m_name))
/*     */                 continue;
/* 126 */               found = true;
/* 127 */               break;
/*     */             }
/*     */ 
/* 130 */             if (!found)
/*     */             {
/* 132 */               if (SystemUtils.m_verbose)
/*     */               {
/* 134 */                 Report.debug("schemaloader", "loader " + loader + " cannot load the parent/child view of this view", null);
/*     */               }
/*     */ 
/* 137 */               capabilities = null;
/*     */             }
/*     */           }
/*     */         }
/* 141 */         else if (view != null)
/*     */         {
/* 143 */           capabilities = loader.getLoaderCapabilities(view, args);
/*     */         }
/*     */ 
/* 146 */         if (capabilities == null) {
/*     */           continue;
/*     */         }
/*     */ 
/* 150 */         if (!SchemaLoaderUtils.hasKeyTypes(capabilities))
/*     */           continue;
/* 152 */         if (view != null)
/*     */         {
/* 156 */           view.m_loadsUnconvertedValues = SchemaLoaderUtils.supportsCapability(capabilities, 1);
/*     */         }
/*     */ 
/* 159 */         label340: return loader;
/*     */       }
/*     */     }
/*     */ 
/* 163 */     return null;
/*     */   }
/*     */ 
/*     */   public DataResultSet shallowClone()
/*     */   {
/* 169 */     SchemaViewConfig rset = new SchemaViewConfig();
/* 170 */     initShallow(rset);
/* 171 */     rset.m_loaders = this.m_loaders;
/* 172 */     return rset;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 177 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 84235 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.schema.SchemaViewConfig
 * JD-Core Version:    0.5.4
 */