/*     */ package intradoc.server.utils;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import java.io.InputStream;
/*     */ import java.io.OutputStream;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class LegacyListEditor
/*     */ {
/*     */   public static final String m_activeCompFileName = "components.hda";
/*     */   public static final String m_editCompFileName = "edit_components.hda";
/*     */   public static final String m_editTableName = "EditComponents";
/*     */   protected DataBinder m_activeCompBinder;
/*     */   protected DataBinder m_editCompBinder;
/*     */   protected DataResultSet m_activeComponents;
/*     */   protected DataResultSet m_editComponents;
/*     */   protected InputStream m_activeInput;
/*     */   protected InputStream m_editInput;
/*     */   protected OutputStream m_activeOutput;
/*     */   protected OutputStream m_editOutput;
/*     */   protected String m_editEncoding;
/*     */   protected String m_activeEncoding;
/*     */   protected boolean m_editExists;
/*     */ 
/*     */   public void setComponentData(DataBinder binder, String encoding)
/*     */   {
/*  63 */     this.m_activeCompBinder = binder;
/*  64 */     this.m_activeEncoding = encoding;
/*  65 */     this.m_activeComponents = ((DataResultSet)this.m_activeCompBinder.getResultSet("Components"));
/*     */   }
/*     */ 
/*     */   public void setEditComponentData(DataBinder binder, String encoding)
/*     */   {
/*  70 */     this.m_editCompBinder = binder;
/*  71 */     this.m_editEncoding = encoding;
/*  72 */     if (this.m_editCompBinder == null)
/*     */       return;
/*  74 */     this.m_editComponents = ((DataResultSet)this.m_editCompBinder.getResultSet("EditComponents"));
/*     */   }
/*     */ 
/*     */   public void setInputStreams(InputStream activeIn, InputStream editIn)
/*     */   {
/*  80 */     this.m_activeInput = activeIn;
/*  81 */     this.m_editInput = editIn;
/*     */   }
/*     */ 
/*     */   public void setOutputStreams(OutputStream activeOut, OutputStream editOut)
/*     */   {
/*  86 */     this.m_activeOutput = activeOut;
/*  87 */     this.m_editOutput = editOut;
/*     */   }
/*     */ 
/*     */   protected void loadLegacy()
/*     */     throws DataException, ServiceException
/*     */   {
/*  95 */     loadEditComponents();
/*  96 */     mergeActiveComponents();
/*     */ 
/*  99 */     if (this.m_editCompBinder == null)
/*     */     {
/* 101 */       this.m_editCompBinder = new DataBinder();
/*     */     }
/* 103 */     this.m_editCompBinder.addResultSet("EditComponents", this.m_editComponents);
/*     */   }
/*     */ 
/*     */   protected void loadEditComponents()
/*     */     throws DataException, ServiceException
/*     */   {
/* 113 */     if (this.m_editComponents != null)
/*     */     {
/* 115 */       this.m_editExists = true;
/*     */       try
/*     */       {
/* 118 */         ComponentListUtils.updateComponentListColumns(this.m_editComponents);
/*     */ 
/* 120 */         int statusIndex = ResultSetUtils.getIndexMustExist(this.m_editComponents, "status");
/* 121 */         for (; this.m_editComponents.isRowPresent(); this.m_editComponents.next())
/*     */         {
/* 123 */           Vector v = this.m_editComponents.getCurrentRowValues();
/* 124 */           v.setElementAt("Disabled", statusIndex);
/*     */         }
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/* 129 */         throw new ServiceException(e, "csComponentUnableToSetDefaultStatus", new Object[0]);
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 134 */       this.m_editComponents = ComponentListUtils.createDefaultComponentsResultSet();
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void mergeActiveComponents() throws ServiceException
/*     */   {
/* 140 */     if (this.m_activeComponents == null)
/*     */     {
/* 142 */       Report.trace("componentloader", "not merging legacy active components because ResultSet is null.", null);
/*     */ 
/* 144 */       return;
/*     */     }FieldInfo[] fi;
/*     */     int nameIndex;
/*     */     int locationIndex;
/*     */     int statusIndex;
/*     */     try { fi = ResultSetUtils.createInfoList(this.m_activeComponents, new String[] { "name", "location" }, true);
/*     */ 
/* 152 */       nameIndex = ResultSetUtils.getIndexMustExist(this.m_editComponents, "name");
/* 153 */       locationIndex = ResultSetUtils.getIndexMustExist(this.m_editComponents, "location");
/* 154 */       statusIndex = ResultSetUtils.getIndexMustExist(this.m_editComponents, "status");
/*     */ 
/* 157 */       for (this.m_activeComponents.first(); this.m_activeComponents.isRowPresent(); )
/*     */       {
/* 160 */         String name = this.m_activeComponents.getStringValue(fi[0].m_index);
/* 161 */         String location = this.m_activeComponents.getStringValue(fi[1].m_index);
/* 162 */         Vector v = this.m_editComponents.findRow(0, name);
/* 163 */         boolean addRow = v == null;
/*     */ 
/* 165 */         if (addRow)
/*     */         {
/* 167 */           v = this.m_editComponents.createEmptyRow();
/* 168 */           this.m_editComponents.addRow(v);
/* 169 */           this.m_editComponents.last();
/* 170 */           v.setElementAt(name, nameIndex);
/*     */         }
/*     */ 
/* 173 */         v.setElementAt(location, locationIndex);
/* 174 */         v.setElementAt("Enabled", statusIndex);
/*     */ 
/* 158 */         this.m_activeComponents.next();
/*     */       }
/*     */  }
/*     */     catch (DataException e)
/*     */     {
/* 179 */       throw new ServiceException("!csUnableToMergeComponents", e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void updateComponents() throws DataException, ServiceException
/*     */   {
/* 185 */     int activeNameIndex = ResultSetUtils.getIndexMustExist(this.m_activeComponents, "name");
/* 186 */     int activeLocationIndex = ResultSetUtils.getIndexMustExist(this.m_activeComponents, "location");
/* 187 */     int editNameIndex = ResultSetUtils.getIndexMustExist(this.m_editComponents, "name");
/* 188 */     int editLocationIndex = ResultSetUtils.getIndexMustExist(this.m_editComponents, "location");
/* 189 */     int statusIndex = ResultSetUtils.getIndexMustExist(this.m_editComponents, "status");
/*     */ 
/* 191 */     for (this.m_editComponents.first(); this.m_editComponents.isRowPresent(); this.m_editComponents.next())
/*     */     {
/* 193 */       String name = this.m_editComponents.getStringValue(editNameIndex);
/* 194 */       String status = this.m_editComponents.getStringValue(statusIndex);
/*     */ 
/* 196 */       Vector row = this.m_activeComponents.findRow(activeNameIndex, name);
/* 197 */       if (status.equalsIgnoreCase("disabled"))
/*     */       {
/* 199 */         if (row == null)
/*     */           continue;
/* 201 */         this.m_activeComponents.deleteCurrentRow();
/*     */       }
/*     */       else
/*     */       {
/* 206 */         if (row != null)
/*     */           continue;
/* 208 */         String location = this.m_editComponents.getStringValue(editLocationIndex);
/*     */ 
/* 210 */         row = this.m_activeComponents.createEmptyRow();
/* 211 */         row.setElementAt(name, activeNameIndex);
/* 212 */         row.setElementAt(location, activeLocationIndex);
/* 213 */         this.m_activeComponents.addRow(row);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 218 */     this.m_activeCompBinder.addResultSet("Components", this.m_activeComponents);
/* 219 */     this.m_editCompBinder.addResultSet("EditComponents", this.m_editComponents);
/*     */   }
/*     */ 
/*     */   public void saveListingFiles(String configDir, String compDir)
/*     */     throws ServiceException
/*     */   {
/* 225 */     configDir = FileUtils.directorySlashes(configDir);
/* 226 */     ComponentListUtils.saveListingFile(configDir + "components.hda", this.m_activeCompBinder, 4, this.m_activeOutput, this.m_activeEncoding);
/*     */ 
/* 229 */     ComponentListUtils.saveListingFile(compDir + "edit_components.hda", this.m_editCompBinder, (this.m_editExists) ? 4 : 0, this.m_editOutput, this.m_editEncoding);
/*     */   }
/*     */ 
/*     */   public void closeAllStreams()
/*     */   {
/* 235 */     FileUtils.closeFiles(this.m_editOutput, this.m_editInput);
/* 236 */     FileUtils.closeFiles(this.m_activeOutput, this.m_activeInput);
/* 237 */     this.m_editOutput = null;
/* 238 */     this.m_activeOutput = null;
/* 239 */     this.m_editInput = null;
/* 240 */     this.m_activeInput = null;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 245 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 92740 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.utils.LegacyListEditor
 * JD-Core Version:    0.5.4
 */