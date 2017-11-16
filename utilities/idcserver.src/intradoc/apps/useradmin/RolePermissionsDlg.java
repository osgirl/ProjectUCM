/*     */ package intradoc.apps.useradmin;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.apps.useradmin.irmintg.IRMSecurityPermissionsHelper;
/*     */ import intradoc.apps.useradmin.irmintg.util.IRMUtils;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.common.Validation;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.FixedSizeList;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.WindowHelper;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.gui.ComponentValidator;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JDialog;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class RolePermissionsDlg extends JDialog
/*     */   implements ComponentBinder
/*     */ {
/*     */   protected SystemInterface m_systemInterface;
/*     */   protected ExecutionContext m_ctx;
/*     */   protected ComponentValidator m_cmpValidator;
/*  63 */   protected DialogHelper m_helper = null;
/*  64 */   protected SecurityPermissionsHelper m_securityHelper = null;
/*     */ 
/*     */   public RolePermissionsDlg(SystemInterface sys, String title)
/*     */   {
/*  68 */     super(sys.getMainWindow(), title, true);
/*     */ 
/*  70 */     this.m_systemInterface = sys;
/*  71 */     this.m_ctx = sys.getExecutionContext();
/*     */ 
/*  73 */     DataResultSet roleSet = SharedObjects.getTable("RoleDefinition");
/*  74 */     this.m_cmpValidator = new ComponentValidator(roleSet);
/*     */ 
/*  76 */     this.m_helper = new DialogHelper();
/*  77 */     this.m_helper.attachToDialog(this, sys, null);
/*     */ 
/*  80 */     if (IRMUtils.isIRMIntgEnabled())
/*     */     {
/*  82 */       this.m_securityHelper = new IRMSecurityPermissionsHelper(sys, this.m_helper, true);
/*     */     }
/*     */     else
/*     */     {
/*  87 */       this.m_securityHelper = new SecurityPermissionsHelper(sys, this.m_helper, true);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void init()
/*     */   {
/*  94 */     ActionListener editListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/*  98 */         RolePermissionsDlg.this.editRoles(e);
/*     */       }
/*     */     };
/* 102 */     this.m_securityHelper.init(this.m_helper.m_mainPanel, editListener);
/*     */ 
/* 104 */     refreshDisplay(null);
/*     */ 
/* 106 */     this.m_helper.show();
/*     */   }
/*     */ 
/*     */   public Insets getInsets()
/*     */   {
/* 115 */     Insets curInsets = super.getInsets();
/* 116 */     curInsets.top += 10;
/* 117 */     curInsets.left += 10;
/* 118 */     curInsets.right += 5;
/* 119 */     return curInsets;
/*     */   }
/*     */ 
/*     */   public void editRoles(ActionEvent evt)
/*     */   {
/* 124 */     String cmd = evt.getActionCommand();
/* 125 */     if (cmd.equals("addRole"))
/*     */     {
/* 127 */       DialogHelper addRoleDlg = new DialogHelper(this.m_systemInterface, LocaleResources.getString("apTitleAddNewRole", this.m_ctx), true);
/*     */ 
/* 129 */       DialogCallback okCallback = new DialogCallback()
/*     */       {
/*     */         public boolean handleDialogEvent(ActionEvent e)
/*     */         {
/*     */           try
/*     */           {
/* 136 */             Properties localData = this.m_dlgHelper.m_props;
/* 137 */             localData.put("dPrivilege", "0");
/* 138 */             DataBinder binder = new DataBinder(true);
/* 139 */             binder.setLocalData(localData);
/* 140 */             AppLauncher.executeService("ADD_ROLE", binder);
/* 141 */             RolePermissionsDlg.this.refreshDisplay(localData.getProperty("dRoleName"));
/* 142 */             return true;
/*     */           }
/*     */           catch (Exception exp)
/*     */           {
/* 146 */             this.m_errorMessage = IdcMessageFactory.lc(exp);
/* 147 */           }return false;
/*     */         }
/*     */       };
/* 152 */       JPanel mainPanel = addRoleDlg.initStandard(this, okCallback, 2, true, DialogHelpTable.getHelpPage("AddNewRole"));
/*     */ 
/* 154 */       addRoleDlg.addLabelFieldPair(mainPanel, LocaleResources.getString("apLabelRoleName", this.m_ctx), new CustomTextField(20), "dRoleName");
/*     */ 
/* 156 */       addRoleDlg.addLabelFieldPair(mainPanel, LocaleResources.getString("apLabelRoleDisplayName", this.m_ctx), new CustomTextField(30), "dRoleDisplayName");
/*     */ 
/* 158 */       addRoleDlg.prompt();
/*     */     }
/* 160 */     else if (cmd.equals("editRole"))
/*     */     {
/* 162 */       String roleName = this.m_securityHelper.m_roleList.getSelectedItem();
/* 163 */       if ((roleName == null) || (roleName.length() == 0))
/*     */       {
/* 165 */         reportError(IdcMessageFactory.lc("apSelectRole", new Object[0]));
/* 166 */         return;
/*     */       }
/*     */ 
/* 170 */       int begin = roleName.indexOf("(");
/* 171 */       if (begin >= 0)
/*     */       {
/* 173 */         int end = roleName.indexOf(")");
/* 174 */         roleName = roleName.substring(begin + 1, end);
/*     */       }
/*     */ 
/* 177 */       String title = LocaleResources.getString("apTitleEditRole", this.m_ctx);
/* 178 */       String helpPageName = "EditRole";
/* 179 */       Properties props = new Properties();
/* 180 */       props.put("dRoleName", roleName);
/* 181 */       DataResultSet drs = this.m_securityHelper.m_roleDefs;
/* 182 */       if (drs != null)
/*     */       {
/* 184 */         int index = drs.getFieldInfoIndex("dRoleName");
/* 185 */         if (index != -1)
/*     */         {
/* 187 */           Vector v = drs.findRow(index, roleName);
/* 188 */           if (v != null)
/*     */           {
/* 190 */             String roleDisplayName = drs.getStringValueByName("dRoleDisplayName");
/* 191 */             if (roleDisplayName != null)
/*     */             {
/* 193 */               props.put("dRoleDisplayName", roleDisplayName);
/*     */             }
/*     */           }
/*     */         }
/*     */       }
/*     */ 
/* 199 */       EditRoleDlg dlg = new EditRoleDlg(this.m_systemInterface, title, drs, DialogHelpTable.getHelpPage(helpPageName));
/*     */ 
/* 201 */       dlg.init(props);
/* 202 */       if (dlg.prompt() == 1)
/*     */       {
/*     */         try
/*     */         {
/* 206 */           refreshDisplay(roleName);
/*     */         }
/*     */         catch (Exception exp)
/*     */         {
/* 210 */           reportError(exp);
/* 211 */           return;
/*     */         }
/*     */       }
/*     */     }
/* 215 */     else if (cmd.equals("deleteRole"))
/*     */     {
/* 217 */       String roleName = this.m_securityHelper.m_roleList.getSelectedItem();
/* 218 */       if ((roleName == null) || (roleName.length() == 0))
/*     */       {
/* 220 */         reportError(IdcMessageFactory.lc("apSelectRole", new Object[0]));
/* 221 */         return;
/*     */       }
/*     */ 
/* 225 */       int begin = roleName.indexOf("(");
/* 226 */       if (begin >= 0)
/*     */       {
/* 228 */         int end = roleName.indexOf(")");
/* 229 */         roleName = roleName.substring(begin + 1, end);
/*     */       }
/*     */ 
/* 232 */       if ((roleName.equalsIgnoreCase("guest")) || (roleName.equalsIgnoreCase("admin")))
/*     */       {
/* 234 */         reportError(IdcMessageFactory.lc("apRoleMayNotBeDeleted", new Object[] { roleName }));
/* 235 */         return;
/*     */       }
/*     */ 
/* 238 */       if (MessageBox.doMessage(this.m_systemInterface, IdcMessageFactory.lc("apVerifyRoleDelete", new Object[] { roleName }), 4) == 2)
/*     */       {
/*     */         try
/*     */         {
/* 244 */           DataBinder binder = new DataBinder();
/* 245 */           Properties localData = binder.getLocalData();
/* 246 */           localData.put("dRoleName", roleName);
/* 247 */           AppLauncher.executeService("DELETE_ROLE", binder);
/* 248 */           refreshDisplay(null);
/*     */         }
/*     */         catch (ServiceException exp)
/*     */         {
/* 252 */           reportError(exp);
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 260 */       refreshDisplay(null);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void refreshDisplay(String role)
/*     */   {
/* 266 */     Vector roleNames = SharedObjects.getOptList("roles");
/* 267 */     if (roleNames == null)
/*     */     {
/* 270 */       reportError(IdcMessageFactory.lc("apNoRolesInSystem", new Object[0]));
/* 271 */       return;
/*     */     }
/* 273 */     this.m_securityHelper.m_visibleRoles = roleNames;
/*     */ 
/* 275 */     this.m_securityHelper.refreshLists(role);
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 284 */     WindowHelper helper = (WindowHelper)exchange.m_currentObject;
/* 285 */     helper.exchangeComponentValue(exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 290 */     String name = exchange.m_compName;
/* 291 */     String val = exchange.m_compValue;
/*     */ 
/* 293 */     int maxLength = this.m_cmpValidator.getMaxLength(name, 30);
/*     */ 
/* 295 */     IdcMessage errMsg = null;
/* 296 */     if (name.equals("dRoleName"))
/*     */     {
/* 298 */       int valResult = Validation.checkFormField(val);
/* 299 */       switch (valResult)
/*     */       {
/*     */       case 0:
/* 302 */         break;
/*     */       case -1:
/* 304 */         errMsg = IdcMessageFactory.lc("apRoleNameCannotBeEmpty", new Object[0]);
/* 305 */         break;
/*     */       case -2:
/* 307 */         errMsg = IdcMessageFactory.lc("apRoleNameCannotHaveSpaces", new Object[0]);
/* 308 */         break;
/*     */       case -3:
/* 310 */         errMsg = IdcMessageFactory.lc("apInvalidCharsInRoleName", new Object[0]);
/*     */       }
/*     */ 
/* 314 */       if ((val != null) && (val.length() > maxLength))
/*     */       {
/* 316 */         errMsg = IdcMessageFactory.lc("apRoleNameExceedsMaxLength", new Object[] { Integer.valueOf(maxLength) });
/*     */       }
/*     */     }
/* 319 */     else if (name.equals("dRoleDisplayName"))
/*     */     {
/* 321 */       if ((val != null) && (val.length() > maxLength))
/*     */       {
/* 323 */         errMsg = IdcMessageFactory.lc("apRoleDisplayNameExceedsMaxLength", new Object[] { Integer.valueOf(maxLength) });
/*     */       }
/*     */     }
/* 326 */     else if ((name.equals("dDescription")) && 
/* 328 */       (val != null) && (val.length() > maxLength))
/*     */     {
/* 330 */       errMsg = IdcMessageFactory.lc("apDescriptionExceedsMaxLength", new Object[] { Integer.valueOf(maxLength) });
/*     */     }
/*     */ 
/* 333 */     if (errMsg != null)
/*     */     {
/* 335 */       exchange.m_errorMessage = errMsg;
/* 336 */       return false;
/*     */     }
/*     */ 
/* 339 */     return true;
/*     */   }
/*     */ 
/*     */   protected void reportError(IdcMessage msg)
/*     */   {
/* 344 */     MessageBox.reportError(this.m_systemInterface, msg);
/*     */   }
/*     */ 
/*     */   protected void reportError(Exception e)
/*     */   {
/* 349 */     MessageBox.reportError(this.m_systemInterface, e);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 354 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 92578 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.useradmin.RolePermissionsDlg
 * JD-Core Version:    0.5.4
 */