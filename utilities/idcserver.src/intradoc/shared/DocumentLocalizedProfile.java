/*     */ package intradoc.shared;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class DocumentLocalizedProfile
/*     */ {
/*     */   public UserData m_userData;
/*     */   public int m_privilege;
/*     */   public ExecutionContext m_cxt;
/*     */ 
/*     */   public DocumentLocalizedProfile(UserData userData, int priv, ExecutionContext cxt)
/*     */   {
/*  66 */     this.m_userData = userData;
/*  67 */     this.m_privilege = priv;
/*  68 */     this.m_cxt = cxt;
/*     */   }
/*     */ 
/*     */   public Vector getOptionList(String optKey, boolean strictListOnly)
/*     */   {
/*  77 */     if (this.m_userData == null)
/*     */     {
/*  79 */       return null;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/*  84 */       if (optKey.equals("securityGroups"))
/*     */       {
/*  86 */         return SecurityUtils.getUserGroupsWithPrivilege(this.m_userData, this.m_privilege);
/*     */       }
/*  88 */       if (optKey.equals("docAccounts"))
/*     */       {
/*  90 */         Vector acctList = null;
/*  91 */         Vector clbraList = null;
/*  92 */         if (SecurityUtils.m_useCollaboration)
/*     */         {
/*     */           try
/*     */           {
/*  97 */             clbraList = Collaborations.computeUserCollaborationLists(this.m_userData, this.m_cxt, this.m_privilege);
/*     */           }
/*     */           catch (DataException e)
/*     */           {
/* 102 */             Report.trace(null, null, e);
/*     */           }
/*     */         }
/*     */ 
/* 106 */         if (SecurityUtils.m_useAccounts)
/*     */         {
/* 108 */           acctList = SecurityUtils.getAccessibleAccounts(this.m_userData, false, this.m_privilege, null);
/*     */         }
/*     */ 
/* 112 */         if ((clbraList != null) && (clbraList.size() > 0))
/*     */         {
/* 114 */           int startIndex = 0;
/* 115 */           int cSize = clbraList.size();
/* 116 */           if ((acctList != null) && (acctList.size() > 0))
/*     */           {
/* 119 */             int size = acctList.size();
/* 120 */             String firstClbra = "prj/" + (String)clbraList.elementAt(0);
/*     */ 
/* 122 */             for (int i = 0; i < size; ++i)
/*     */             {
/* 124 */               String acct = (String)acctList.elementAt(i);
/* 125 */               int r = acct.compareTo(firstClbra);
/* 126 */               if (r <= 0)
/*     */                 continue;
/* 128 */               startIndex = i;
/* 129 */               break;
/*     */             }
/*     */ 
/*     */           }
/*     */           else
/*     */           {
/* 135 */             acctList = new IdcVector();
/*     */           }
/*     */ 
/* 138 */           for (int i = 0; i < cSize; ++i)
/*     */           {
/* 140 */             acctList.insertElementAt("prj/" + clbraList.elementAt(i), startIndex + i);
/*     */           }
/*     */         }
/* 143 */         return acctList;
/*     */       }
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 148 */       Report.trace(null, "Unable to retrieve localized option list '" + optKey + "'.", e);
/*     */     }
/*     */ 
/* 151 */     return null;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 158 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.DocumentLocalizedProfile
 * JD-Core Version:    0.5.4
 */