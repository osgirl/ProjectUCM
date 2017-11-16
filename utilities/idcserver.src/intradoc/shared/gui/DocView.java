/*     */ package intradoc.shared.gui;
/*     */ 
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.DisplayStringCallbackAdaptor;
/*     */ import intradoc.gui.iwt.ColumnInfo;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.shared.AdditionalRenditions;
/*     */ import intradoc.shared.DocumentLocalizedProfile;
/*     */ import intradoc.shared.FieldDef;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.TableFields;
/*     */ import intradoc.shared.ViewFieldDef;
/*     */ import intradoc.shared.ViewFields;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class DocView extends BaseView
/*     */ {
/*  54 */   protected String[][] m_renMap = (String[][])null;
/*     */ 
/*     */   public DocView(ContainerHelper helper, RefreshView refresher, DocumentLocalizedProfile docProfile)
/*     */   {
/*  59 */     super(helper, refresher, docProfile);
/*     */ 
/*  61 */     this.m_displayColumns = new String[] { "dReleaseState", "dStatus", "dProcessingState" };
/*  62 */     this.m_columnData.m_columnStr = "dDocName,dRevLabel,dID,dStatus,dReleaseState";
/*     */   }
/*     */ 
/*     */   public void initUI(ViewData viewData, JPanel mainPanel)
/*     */   {
/*  68 */     super.initUI(viewData, mainPanel);
/*     */ 
/*  71 */     ActionListener infoListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/*  75 */         DocView.this.info();
/*     */       }
/*     */     };
/*  78 */     addActionListener(infoListener);
/*     */   }
/*     */ 
/*     */   public void info()
/*     */   {
/*  83 */     int index = this.m_list.getSelectedIndex();
/*  84 */     if (index < 0)
/*     */     {
/*  86 */       return;
/*     */     }
/*     */ 
/*  89 */     Properties data = this.m_list.getDataAt(index);
/*  90 */     String title = LocaleUtils.encodeMessage("apInfoFor", null, data.getProperty("dDocName"));
/*  91 */     title = LocaleResources.localizeMessage(title, this.m_cxt);
/*     */ 
/*  93 */     AddDocumentDlg dlg = new AddDocumentDlg(this.m_systemInterface, title, null);
/*  94 */     dlg.initDisplay(data, this.m_refresher);
/*  95 */     dlg.prompt();
/*     */   }
/*     */ 
/*     */   protected ColumnInfo makeColumnInfo(FieldDef fdef)
/*     */   {
/* 104 */     ColumnInfo cinfo = super.makeColumnInfo(fdef);
/*     */ 
/* 108 */     if ((cinfo.m_fieldId.equals("dRevLabel")) || (cinfo.m_fieldId.equals("dID")))
/*     */     {
/* 110 */       cinfo.m_columnAlignment = 12;
/*     */     }
/*     */ 
/* 113 */     return cinfo;
/*     */   }
/*     */ 
/*     */   public void configureFilter(boolean forceRefresh)
/*     */   {
/* 119 */     if ((this.m_filterFields != null) && (!forceRefresh))
/*     */       return;
/* 121 */     this.m_filterFields = new ViewFields(this.m_cxt);
/* 122 */     this.m_filterFields.addStandardDocFields();
/* 123 */     ViewFieldDef def = this.m_filterFields.addField("Revisions.dID", LocaleResources.getString("apTitleID", this.m_cxt));
/* 124 */     def.m_type = "int";
/* 125 */     this.m_filterFields.addDocFlags(true);
/* 126 */     this.m_filterFields.addDocDateFields(true, true);
/*     */ 
/* 128 */     DataResultSet drset = SharedObjects.getTable("DocMetaDefinition");
/*     */     try
/*     */     {
/* 132 */       this.m_filterFields.addMetaFields(drset);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 136 */       String msg = LocaleUtils.encodeMessage("apUnableToReadMetaFieldInfo", e.getMessage());
/*     */ 
/* 138 */       Report.trace(null, LocaleResources.localizeMessage(msg, this.m_cxt), e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void configureShowColumns(boolean forceRefresh)
/*     */   {
/* 146 */     ViewFields columnFields = this.m_columnData.m_columnFields;
/* 147 */     if ((columnFields != null) && (!forceRefresh))
/*     */       return;
/* 149 */     columnFields = new ViewFields(this.m_cxt);
/* 150 */     columnFields.addStandardDocFields();
/* 151 */     columnFields.addField("dID", LocaleResources.getString("apTitleID", this.m_cxt));
/* 152 */     columnFields.addDocDateFields(true, true);
/* 153 */     columnFields.addDocFlags(false);
/*     */ 
/* 155 */     DataResultSet drset = SharedObjects.getTable("DocMetaDefinition");
/*     */     try
/*     */     {
/* 158 */       columnFields.addMetaFields(drset);
/* 159 */       this.m_columnData.m_columnFields = columnFields;
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 163 */       String msg = LocaleUtils.encodeMessage("apUnableToReadMetaFieldInfo", e.getMessage());
/*     */ 
/* 165 */       Report.trace(null, LocaleResources.localizeMessage(msg, this.m_cxt), e);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void addDisplayMaps()
/*     */   {
/* 176 */     if (this.m_displayCallback == null)
/*     */     {
/* 178 */       String[] coreColumns = { "dReleaseState", "dStatus", "dProcessingState", "dIsCheckedOut", "dPublishState", "dPublishType", "dIndexerState", "dWorkflowState" };
/*     */ 
/* 181 */       int numRenditions = AdditionalRenditions.m_maxNum;
/* 182 */       this.m_displayColumns = new String[coreColumns.length + numRenditions];
/* 183 */       System.arraycopy(coreColumns, 0, this.m_displayColumns, 0, coreColumns.length);
/* 184 */       int index = coreColumns.length;
/* 185 */       for (int i = 0; i < numRenditions; ++i)
/*     */       {
/* 187 */         this.m_displayColumns[(index++)] = ("dRendition" + (i + 1));
/*     */       }
/*     */ 
/* 190 */       this.m_displayCallback = new DisplayStringCallbackAdaptor()
/*     */       {
/*     */         public String createDisplayString(FieldInfo finfo, String name, String value, Vector row)
/*     */         {
/* 196 */           String[][] displayMap = (String[][])null;
/* 197 */           if (name.equals("dReleaseState"))
/*     */           {
/* 199 */             displayMap = TableFields.RELEASESTATE_OPTIONLIST;
/*     */           }
/* 201 */           else if (name.equals("dStatus"))
/*     */           {
/* 203 */             displayMap = TableFields.STATUS_OPTIONLIST;
/*     */           }
/* 205 */           else if (name.equals("dProcessingState"))
/*     */           {
/* 207 */             displayMap = TableFields.PROCESSINGSTATE_OPTIONLIST;
/*     */           }
/* 209 */           else if (name.startsWith("dRendition"))
/*     */           {
/* 211 */             displayMap = DocView.this.m_renMap;
/*     */           }
/* 213 */           else if (name.equals("dIsCheckedOut"))
/*     */           {
/* 215 */             displayMap = TableFields.YESNO_OPTIONLIST;
/*     */           }
/* 217 */           else if (name.equals("dPublishState"))
/*     */           {
/* 219 */             displayMap = TableFields.PUBLISHSTATE_OPTIONLIST;
/*     */           }
/* 221 */           else if (name.equals("dPublishType"))
/*     */           {
/* 223 */             displayMap = TableFields.PUBLISHTYPE_OPTIONLIST;
/*     */           }
/* 225 */           else if (name.equals("dIndexerState"))
/*     */           {
/* 227 */             displayMap = TableFields.INDEXERSTATE_OPTIONLIST;
/*     */           }
/* 229 */           else if (name.equals("dWorkflowState"))
/*     */           {
/* 231 */             displayMap = TableFields.WORKFLOWSTATE_OPTIONLIST;
/*     */           }
/*     */ 
/* 234 */           if (displayMap == null)
/*     */           {
/* 236 */             return value;
/*     */           }
/*     */ 
/* 239 */           String displayStr = StringUtils.getPresentationString(displayMap, value);
/* 240 */           if (displayStr == null)
/*     */           {
/* 242 */             displayStr = "";
/*     */           }
/* 244 */           return displayStr;
/*     */         }
/*     */ 
/*     */         public String createExtendedDisplayString(FieldInfo finfo, String name, String value, Vector row)
/*     */         {
/* 251 */           String[][] displayMap = (String[][])null;
/* 252 */           if (name.equals("dReleaseState"))
/*     */           {
/* 254 */             displayMap = TableFields.RELEASESTATE_OPTIONLIST;
/*     */           }
/* 256 */           else if (name.equals("dStatus"))
/*     */           {
/* 258 */             displayMap = TableFields.STATUS_OPTIONLIST;
/*     */           }
/* 260 */           else if (name.equals("dProcessingState"))
/*     */           {
/* 262 */             displayMap = TableFields.PROCESSINGSTATE_OPTIONLIST;
/*     */           }
/* 264 */           else if (name.startsWith("dRendition"))
/*     */           {
/* 266 */             displayMap = DocView.this.m_renMap;
/*     */           }
/* 268 */           else if (name.equals("dIsCheckedOut"))
/*     */           {
/* 270 */             displayMap = TableFields.YESNO_OPTIONLIST;
/*     */           }
/* 272 */           else if (name.equals("dPublishState"))
/*     */           {
/* 274 */             displayMap = TableFields.PUBLISHSTATE_OPTIONLIST;
/*     */           }
/* 276 */           else if (name.equals("dPublishType"))
/*     */           {
/* 278 */             displayMap = TableFields.PUBLISHTYPE_OPTIONLIST;
/*     */           }
/* 280 */           else if (name.equals("dIndexerState"))
/*     */           {
/* 282 */             displayMap = TableFields.INDEXERSTATE_OPTIONLIST;
/*     */           }
/* 284 */           else if (name.equals("dWorkflowState"))
/*     */           {
/* 286 */             displayMap = TableFields.WORKFLOWSTATE_OPTIONLIST;
/*     */           }
/*     */ 
/* 289 */           if (displayMap == null)
/*     */           {
/* 291 */             return value;
/*     */           }
/*     */ 
/* 294 */           String displayStr = null;
/* 295 */           for (int i = 0; i < displayMap.length; ++i)
/*     */           {
/* 297 */             if ((!displayMap[i][0].equals(value)) || (displayMap[i].length <= 2))
/*     */               continue;
/* 299 */             displayStr = displayMap[i][2];
/*     */           }
/*     */ 
/* 303 */           if (displayStr == null)
/*     */           {
/* 305 */             displayStr = "";
/*     */           }
/* 307 */           return displayStr;
/*     */         }
/*     */       };
/*     */     }
/*     */ 
/* 312 */     super.addDisplayMaps();
/*     */   }
/*     */ 
/*     */   public void prepareForView(DataBinder binder)
/*     */     throws ServiceException
/*     */   {
/* 319 */     AdditionalRenditions renSet = (AdditionalRenditions)SharedObjects.getTable("AdditionalRenditions");
/*     */ 
/* 321 */     if (renSet == null)
/*     */       return;
/* 323 */     this.m_renMap = renSet.createDisplayMap(this.m_cxt);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 363 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78525 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.gui.DocView
 * JD-Core Version:    0.5.4
 */