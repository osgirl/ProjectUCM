/*     */ package intradoc.apps.useradmin;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomText;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.FixedSizeList;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.RoleDefinitions;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.UserAttribInfo;
/*     */ import intradoc.shared.UserData;
/*     */ import intradoc.shared.gui.SecurityEditHelper;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class EditUserRolePanel extends EditUserBasePanel
/*     */   implements ActionListener
/*     */ {
/*     */   protected SecurityPermissionsHelper m_permissionsHelper;
/*     */ 
/*     */   protected void initUI(boolean isEdit)
/*     */   {
/*  54 */     this.m_permissionsHelper = new SecurityPermissionsHelper(this.m_systemInterface, this.m_helper, false);
/*     */ 
/*  56 */     FixedSizeList roleList = new FixedSizeList(15, 250, true);
/*  57 */     roleList.setMultipleMode(true);
/*  58 */     determineRoleList(roleList, false);
/*     */ 
/*  60 */     this.m_helper.makePanelGridBag(this, 1);
/*  61 */     this.m_helper.m_gridHelper.m_gc.anchor = 18;
/*  62 */     this.m_helper.addLastComponentInRow(this, new CustomText(LocaleResources.getString("apPermissionsAssignedAreUnion", this.m_ctx), 120));
/*     */ 
/*  64 */     this.m_permissionsHelper.init(this, this);
/*  65 */     refreshRoleDisplay();
/*     */   }
/*     */ 
/*     */   protected boolean determineRoleList(FixedSizeList roleList, boolean isAll)
/*     */   {
/*  71 */     Vector roles = null;
/*  72 */     if (this.m_isAdmin)
/*     */     {
/*  74 */       roles = SharedObjects.getOptList("roles");
/*     */     }
/*     */     else
/*     */     {
/*  78 */       roles = this.m_loggedInUserData.getAttributes("role");
/*     */     }
/*     */ 
/*  81 */     if ((roles != null) && (roles.size() > 0))
/*     */     {
/*  83 */       boolean useDisplayNames = SharedObjects.getEnvValueAsBoolean("UseRoleAndAliasDisplayNames", false);
/*     */ 
/*  85 */       int size = roles.size();
/*  86 */       for (int i = 0; i < size; ++i)
/*     */       {
/*  88 */         String str = null;
/*  89 */         Object obj = roles.elementAt(i);
/*  90 */         if (this.m_isAdmin)
/*     */         {
/*  92 */           str = (String)obj;
/*     */         }
/*     */         else
/*     */         {
/*  96 */           UserAttribInfo info = (UserAttribInfo)obj;
/*  97 */           str = info.m_attribName;
/*     */         }
/*  99 */         if (!isAll)
/*     */         {
/* 102 */           String[] alreadyPresent = StringUtils.convertListToArray(this.m_permissionsHelper.m_visibleRoles);
/* 103 */           if (StringUtils.findStringIndex(alreadyPresent, str) >= 0) {
/*     */             continue;
/*     */           }
/*     */         }
/*     */ 
/* 108 */         if (useDisplayNames)
/*     */         {
/* 111 */           int index = this.m_permissionsHelper.m_roleDefs.getFieldInfoIndex("dRoleName");
/* 112 */           this.m_permissionsHelper.m_roleDefs.findRow(index, str);
/* 113 */           String displayName = this.m_permissionsHelper.m_roleDefs.getStringValueByName("dRoleDisplayName");
/* 114 */           if ((displayName != null) && (displayName.length() > 0))
/*     */           {
/* 116 */             str = displayName + " (" + str + ")";
/*     */           }
/*     */         }
/* 119 */         roleList.add(str);
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 124 */       reportError(IdcMessageFactory.lc("apNoRolesInSystem", new Object[0]));
/* 125 */       return false;
/*     */     }
/* 127 */     return true;
/*     */   }
/*     */ 
/*     */   protected void refreshRoleDisplay()
/*     */   {
/* 132 */     Vector roles = new IdcVector();
/*     */ 
/* 135 */     Vector v = this.m_editHelper.m_userData.getAttributes("role");
/* 136 */     if (v != null)
/*     */     {
/* 138 */       int nroles = v.size();
/* 139 */       for (int i = 0; i < nroles; ++i)
/*     */       {
/* 141 */         UserAttribInfo uai = (UserAttribInfo)v.elementAt(i);
/* 142 */         roles.addElement(uai.m_attribName);
/*     */       }
/*     */     }
/* 145 */     this.m_permissionsHelper.m_visibleRoles = roles;
/* 146 */     this.m_permissionsHelper.refreshLists();
/*     */   }
/*     */ 
/*     */   public void editRoles(ActionEvent evt)
/*     */   {
/* 154 */     if ((!this.m_isAdmin) && (this.m_isEditSelf))
/*     */     {
/* 156 */       reportError(IdcMessageFactory.lc("apInsufficientPrivilegeToEditOwnRole", new Object[0]));
/* 157 */       return;
/*     */     }
/*     */ 
/* 160 */     String cmd = evt.getActionCommand();
/* 161 */     if (cmd.equals("addRole"))
/*     */     {
/* 163 */       DialogHelper addRoleDlg = new DialogHelper(this.m_systemInterface, LocaleResources.getString("apLabelAddRole", this.m_ctx), true);
/*     */ 
/* 165 */       DialogCallback okCallback = new DialogCallback()
/*     */       {
/*     */         public boolean handleDialogEvent(ActionEvent e)
/*     */         {
/*     */           try
/*     */           {
/* 172 */             if (this.m_dlgHelper.m_result == 1)
/*     */             {
/* 174 */               FixedSizeList list = (FixedSizeList)EditUserRolePanel.this.m_ctx.getCachedObject("addRoleList");
/* 175 */               String[] roles = list.getSelectedItems();
/* 176 */               if ((roles == null) || (roles.length == 0))
/*     */               {
/* 178 */                 this.m_errorMessage = IdcMessageFactory.lc("apSelectRole", new Object[0]);
/* 179 */                 return false;
/*     */               }
/*     */ 
/* 182 */               for (int i = 0; i < roles.length; ++i)
/*     */               {
/* 185 */                 int begin = roles[i].indexOf("(");
/* 186 */                 if (begin >= 0)
/*     */                 {
/* 188 */                   int end = roles[i].indexOf(")");
/* 189 */                   roles[i] = roles[i].substring(begin + 1, end);
/*     */                 }
/*     */ 
/* 192 */                 EditUserRolePanel.this.m_editHelper.m_userData.addAttribute("role", roles[i], "15");
/*     */               }
/*     */ 
/* 195 */               EditUserRolePanel.this.refreshRoleDisplay();
/*     */             }
/*     */ 
/* 198 */             EditUserRolePanel.this.m_ctx.setCachedObject("addRoleList", null);
/*     */           }
/*     */           catch (Exception exp)
/*     */           {
/* 202 */             this.m_errorMessage = IdcMessageFactory.lc(exp);
/* 203 */             return false;
/*     */           }
/* 205 */           return true;
/*     */         }
/*     */       };
/* 209 */       FixedSizeList roleList = new FixedSizeList(15, 250, true);
/* 210 */       roleList.setMultipleMode(true);
/* 211 */       if (!determineRoleList(roleList, false))
/*     */       {
/* 213 */         return;
/*     */       }
/*     */ 
/* 216 */       if (roleList.getItemCount() == 0)
/*     */       {
/* 218 */         reportError(IdcMessageFactory.lc("apAllRolesAlreadyAssigned", new Object[0]));
/* 219 */         return;
/*     */       }
/*     */ 
/* 222 */       this.m_ctx.setCachedObject("addRoleList", roleList);
/*     */ 
/* 224 */       JPanel mainPanel = addRoleDlg.initStandard(null, okCallback, 2, true, DialogHelpTable.getHelpPage("AddRole"));
/*     */ 
/* 226 */       addRoleDlg.m_cancelCallback = okCallback;
/* 227 */       CustomLabel label = new CustomLabel(LocaleResources.getString("apLabelRoleName", this.m_ctx));
/* 228 */       mainPanel.setLayout(new BorderLayout());
/* 229 */       mainPanel.add(label, "North");
/* 230 */       mainPanel.add(roleList, "Center");
/*     */ 
/* 232 */       addRoleDlg.prompt();
/*     */     } else {
/* 234 */       if (!cmd.equals("deleteRole"))
/*     */         return;
/* 236 */       String roleName = this.m_permissionsHelper.m_roleList.getSelectedItem();
/* 237 */       if ((roleName == null) || (roleName.length() == 0))
/*     */       {
/* 239 */         reportError(IdcMessageFactory.lc("apSelectRole", new Object[0]));
/* 240 */         return;
/*     */       }
/*     */ 
/* 243 */       if ((roleName.equals("admin")) && (this.m_isEditSelf))
/*     */       {
/* 245 */         reportError(IdcMessageFactory.lc("apCannotRemoveAdminPrivilegesFromSelf", new Object[0]));
/* 246 */         return;
/*     */       }
/*     */ 
/* 250 */       int begin = roleName.indexOf("(");
/* 251 */       if (begin >= 0)
/*     */       {
/* 253 */         int end = roleName.indexOf(")");
/* 254 */         roleName = roleName.substring(begin + 1, end);
/*     */       }
/*     */ 
/* 257 */       this.m_editHelper.m_userData.removeAttribute("role", roleName);
/* 258 */       refreshRoleDisplay();
/*     */     }
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 267 */     editRoles(e);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 272 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 83339 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.useradmin.EditUserRolePanel
 * JD-Core Version:    0.5.4
 */