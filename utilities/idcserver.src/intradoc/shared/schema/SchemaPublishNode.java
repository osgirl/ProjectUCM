/*     */ package intradoc.shared.schema;
/*     */ 
/*     */ import intradoc.common.IdcAppendable;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.shared.ProgressState;
/*     */ import intradoc.shared.ProgressStateUtils;
/*     */ import intradoc.util.IdcAppendableBase;
/*     */ import java.util.ArrayList;
/*     */ 
/*     */ public class SchemaPublishNode
/*     */ {
/*     */   public SchemaViewData m_view;
/*     */   public SchemaRelationData m_relationship;
/*     */   public SchemaPublishNode m_parent;
/*  36 */   public ArrayList m_childList = new ArrayList();
/*  37 */   public boolean m_findShortestOnly = false;
/*  38 */   public int m_depth = 0;
/*     */   public SchemaPublishNode m_recursivePointer;
/*     */   public SchemaLoader m_loader;
/*     */ 
/*     */   public SchemaPublishNode()
/*     */   {
/*     */   }
/*     */ 
/*     */   public SchemaPublishNode(SchemaViewData view, SchemaRelationData relationship)
/*     */   {
/*  52 */     this.m_view = view;
/*  53 */     this.m_relationship = relationship;
/*     */   }
/*     */ 
/*     */   public SchemaPublishNode(SchemaViewData view, SchemaRelationData relationship, SchemaPublishNode parent, SchemaLoader loader)
/*     */   {
/*  61 */     this.m_view = view;
/*  62 */     this.m_relationship = relationship;
/*  63 */     this.m_parent = parent;
/*  64 */     if (this.m_parent != null)
/*     */     {
/*  66 */       this.m_depth = (this.m_parent.m_depth + 1);
/*     */     }
/*  68 */     this.m_loader = loader;
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/*  74 */     IdcStringBuilder builder = new IdcStringBuilder();
/*  75 */     builder.append("[node ").append(this.m_view.m_name).append("->").append((this.m_relationship != null) ? this.m_relationship.m_name : "<no-relationship>").append("->(");
/*     */ 
/*  80 */     for (int i = 0; i < this.m_childList.size(); ++i)
/*     */     {
/*  82 */       if (i > 0)
/*     */       {
/*  84 */         builder.append(',');
/*     */       }
/*  86 */       SchemaPublishNode child = (SchemaPublishNode)this.m_childList.get(i);
/*  87 */       builder.append(child.m_view.m_name);
/*     */     }
/*  89 */     builder.append(")]");
/*  90 */     return builder.toString();
/*     */   }
/*     */ 
/*     */   public void trace(String section, ProgressState progress)
/*     */   {
/*  95 */     if (!SystemUtils.isActiveTrace(section))
/*     */     {
/*  97 */       return;
/*     */     }
/*  99 */     ArrayList branches = new ArrayList();
/* 100 */     ArrayList branch = new ArrayList();
/* 101 */     buildTrace(branches, branch);
/*     */ 
/* 103 */     IdcStringBuilder builder = new IdcStringBuilder();
/* 104 */     for (int i = 0; i < branches.size(); ++i)
/*     */     {
/* 106 */       builder.setLength(0);
/* 107 */       branch = (ArrayList)branches.get(i);
/* 108 */       copyBranchToBuilder(branch, builder);
/*     */ 
/* 112 */       ProgressStateUtils.traceProgress(progress, section, builder.toStringNoRelease(), null);
/*     */     }
/*     */ 
/* 117 */     builder.releaseBuffers();
/*     */   }
/*     */ 
/*     */   public void buildTrace(ArrayList branches, ArrayList branch)
/*     */   {
/* 122 */     branch.add(this.m_view);
/* 123 */     if (this.m_childList.size() == 0)
/*     */     {
/* 125 */       branches.add(branch);
/* 126 */       return;
/*     */     }
/* 128 */     branch.add(this.m_relationship);
/* 129 */     for (int i = 0; i < this.m_childList.size(); ++i)
/*     */     {
/* 131 */       SchemaPublishNode child = (SchemaPublishNode)this.m_childList.get(i);
/* 132 */       child.buildTrace(branches, (ArrayList)branch.clone());
/*     */     }
/*     */   }
/*     */ 
/*     */   public void copyBranchToBuilder(ArrayList branch, IdcStringBuilder builder)
/*     */   {
/* 138 */     for (int i = 0; i < branch.size(); ++i)
/*     */     {
/* 140 */       SchemaData data = (SchemaData)branch.get(i);
/* 141 */       if (i > 0)
/*     */       {
/* 143 */         builder.append("->");
/*     */       }
/* 145 */       builder.append(data.m_name);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 151 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 74220 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.schema.SchemaPublishNode
 * JD-Core Version:    0.5.4
 */