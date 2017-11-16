/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.PropParameters;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.shared.CustomSecurityRightsData;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.UserData;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class CustomSecurityRightsServiceHandler extends ServiceHandler
/*     */ {
/*     */   @IdcServiceAction
/*     */   public void hasCustomRights()
/*     */     throws DataException, ServiceException
/*     */   {
/*  34 */     if (this.m_service.isConditionVarTrue("DoNotCheckCustomRights"))
/*     */     {
/*  36 */       return;
/*     */     }
/*     */ 
/*  40 */     boolean isForceLogin = StringUtils.convertToBool(this.m_binder.getLocal("isForceLogin"), false);
/*     */ 
/*  42 */     if (isForceLogin)
/*     */     {
/*  44 */       this.m_service.checkForceLogin();
/*     */     }
/*     */ 
/*  47 */     UserData userData = this.m_service.getUserData();
/*  48 */     Vector v = this.m_currentAction.m_params;
/*  49 */     if (v == null)
/*     */     {
/*  51 */       throw new ServiceException("!csActionParamMissing");
/*     */     }
/*     */ 
/*  56 */     int size = v.size();
/*  57 */     boolean hasRight = false;
/*  58 */     for (int i = 0; i < size; ++i)
/*     */     {
/*  60 */       String right = (String)v.elementAt(i);
/*  61 */       if (!CustomSecurityRightsData.hasCustomRights(userData, right))
/*     */         continue;
/*  63 */       hasRight = true;
/*  64 */       break;
/*     */     }
/*     */ 
/*  68 */     if (hasRight)
/*     */       return;
/*  70 */     String rights = "";
/*  71 */     for (int i = 0; i < size; ++i)
/*     */     {
/*  73 */       String curRight = (String)v.elementAt(i);
/*  74 */       if ((curRight == null) || (curRight.length() <= 0))
/*     */         continue;
/*  76 */       if (rights.length() > 0)
/*     */       {
/*  78 */         rights = rights + ", ";
/*     */       }
/*     */ 
/*  81 */       rights = rights + curRight;
/*     */     }
/*     */ 
/*  84 */     String errorMsg = LocaleUtils.encodeMessage("csCustomMustHaveRight", null, rights);
/*  85 */     throw new ServiceException(errorMsg);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void filterCustomSecurityTable()
/*     */     throws DataException
/*     */   {
/*  92 */     String rsetName = this.m_currentAction.getParamAt(0);
/*  93 */     DataResultSet drset = SharedObjects.getTable(rsetName);
/*  94 */     if (drset == null)
/*     */     {
/*  96 */       return;
/*     */     }
/*     */ 
/*  99 */     boolean isRightsTable = false;
/* 100 */     if (rsetName.equals("CustomSecurityRights"))
/*     */     {
/* 102 */       isRightsTable = true;
/*     */     }
/* 104 */     DataResultSet filteredSet = filterCustomSecurityTableEx(drset, isRightsTable);
/*     */ 
/* 106 */     this.m_binder.addResultSet(rsetName, filteredSet);
/*     */   }
/*     */ 
/*     */   public DataResultSet filterCustomSecurityTableEx(DataResultSet drset, boolean isRightsTable)
/*     */     throws DataException
/*     */   {
/* 112 */     DataResultSet filteredSet = new DataResultSet();
/* 113 */     filteredSet.copyFieldInfo(drset);
/*     */ 
/* 116 */     if (!isRightsTable)
/*     */     {
/* 118 */       FieldInfo fi = new FieldInfo();
/* 119 */       fi.m_name = "IsHidden";
/*     */ 
/* 121 */       Vector fieldList = new IdcVector();
/* 122 */       fieldList.addElement(fi);
/* 123 */       filteredSet.mergeFieldsWithFlags(fieldList, 0);
/*     */     }
/*     */ 
/* 126 */     PageMerger pageMerger = new PageMerger(this.m_binder, null);
/*     */ 
/* 128 */     int disableIndex = -1;
/* 129 */     if (isRightsTable)
/*     */     {
/* 131 */       disableIndex = ResultSetUtils.getIndexMustExist(drset, "disableCondition");
/*     */     }
/*     */ 
/* 134 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*     */     {
/* 136 */       boolean isHide = false;
/*     */ 
/* 138 */       String hideConditionStr = ResultSetUtils.getValue(drset, "hideCondition");
/*     */       try
/*     */       {
/* 141 */         String isHideStr = pageMerger.evaluateScript(hideConditionStr);
/* 142 */         isHide = StringUtils.convertToBool(isHideStr, false);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 146 */         Report.error(null, e, "csUnableEvaluateCondition", new Object[0]);
/* 147 */         isHide = true;
/*     */       }
/*     */ 
/* 150 */       Properties rowProps = drset.getCurrentRowProps();
/* 151 */       PropParameters params = new PropParameters(rowProps);
/*     */ 
/* 154 */       if (!isRightsTable)
/*     */       {
/* 156 */         if (isHide)
/*     */         {
/* 158 */           rowProps.put("IsHidden", "1");
/*     */         }
/*     */         else
/*     */         {
/* 162 */           rowProps.put("IsHidden", "");
/*     */         }
/*     */       }
/*     */ 
/* 166 */       if ((isHide) && (isRightsTable))
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 172 */       if (isRightsTable)
/*     */       {
/*     */         try
/*     */         {
/* 176 */           String disableConditionStr = drset.getStringValue(disableIndex);
/* 177 */           String isDisabledStr = pageMerger.evaluateScript(disableConditionStr);
/* 178 */           boolean isDisabled = StringUtils.convertToBool(isDisabledStr, false);
/*     */ 
/* 180 */           if (isDisabled)
/*     */           {
/* 182 */             rowProps.put("disableCondition", "1");
/*     */           }
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/* 187 */           Report.error(null, e, "csUnableEvaluateCondition", new Object[0]);
/* 188 */           isHide = true;
/*     */         }
/*     */       }
/*     */ 
/* 192 */       Vector row = filteredSet.createRow(params);
/* 193 */       filteredSet.addRow(row);
/*     */     }
/*     */ 
/* 196 */     return filteredSet;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 201 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 70705 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.CustomSecurityRightsServiceHandler
 * JD-Core Version:    0.5.4
 */