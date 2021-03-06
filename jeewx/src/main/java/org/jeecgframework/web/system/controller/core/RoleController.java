package org.jeecgframework.web.system.controller.core;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.jeecgframework.core.common.controller.BaseController;
import org.jeecgframework.core.common.hibernate.qbc.CriteriaQuery;
import org.jeecgframework.core.common.model.json.AjaxJson;
import org.jeecgframework.core.common.model.json.ComboTree;
import org.jeecgframework.core.common.model.json.DataGrid;
import org.jeecgframework.core.common.model.json.TreeGrid;
import org.jeecgframework.core.common.model.json.ValidForm;
import org.jeecgframework.core.constant.Globals;
import org.jeecgframework.core.util.ExceptionUtil;
import org.jeecgframework.core.util.NumberComparator;
import org.jeecgframework.core.util.SetListSort;
import org.jeecgframework.core.util.StringUtil;
import org.jeecgframework.core.util.oConvertUtils;
import org.jeecgframework.tag.core.easyui.TagUtil;
import org.jeecgframework.tag.vo.easyui.ComboTreeModel;
import org.jeecgframework.tag.vo.easyui.TreeGridModel;
import org.jeecgframework.web.system.pojo.base.TSFunction;
import org.jeecgframework.web.system.pojo.base.TSOperation;
import org.jeecgframework.web.system.pojo.base.TSRole;
import org.jeecgframework.web.system.pojo.base.TSRoleFunction;
import org.jeecgframework.web.system.pojo.base.TSRoleUser;
import org.jeecgframework.web.system.service.SystemService;
import org.jeecgframework.web.system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;


/**
 * ???????????????
 * 
 * @author ?????????
 * 
 */
@Scope("prototype")
@Controller
@RequestMapping("/roleController")
public class RoleController extends BaseController {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(RoleController.class);
	private UserService userService;
	private SystemService systemService;
	private String message = null;

	@Autowired
	public void setSystemService(SystemService systemService) {
		this.systemService = systemService;
	}

	public UserService getUserService() {
		return userService;
	}

	@Autowired
	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	/**
	 * ????????????????????????
	 * 
	 * @return
	 */
	@RequestMapping(params = "role")
	public ModelAndView role() {
		return new ModelAndView("system/role/roleList");
	}

	/**
	 * easyuiAJAX????????????
	 * 
	 * @param request
	 * @param response
	 * @param dataGrid
	 * @param user
	 */

	@RequestMapping(params = "roleGrid")
	public void roleGrid( TSRole role,HttpServletRequest request,
			HttpServletResponse response, DataGrid dataGrid) {
		CriteriaQuery cq = new CriteriaQuery(TSRole.class, dataGrid);
		org.jeecgframework.core.extend.hqlsearch.HqlGenerateUtil.installHql(cq, role);
		cq.add();
		this.systemService.getDataGridReturn(cq, true);
		TagUtil.datagrid(response, dataGrid);;
	}

	/**
	 * ????????????
	 * 
	 * @param ids
	 * @return
	 */
	@RequestMapping(params = "delRole")
	@ResponseBody
	public AjaxJson delRole(TSRole role,HttpServletRequest request) {
		AjaxJson j = new AjaxJson();
		int count = userService.getUsersOfThisRole(role.getId());
		if(count == 0){
			//?????????????????????????????????????????????
			delRoleFunction(role);
			role = systemService.getEntity(TSRole.class, role.getId());
			userService.delete(role);
			message = "??????: " + role.getRoleName() + "???????????????";
			systemService.addLog(message, Globals.Log_Type_DEL,
					Globals.Log_Leavel_INFO);
		}else{
			message = "??????: ?????????????????????????????????????????????";
		}
		j.setMsg(message);
		return j;
	}
	/**
	 * ????????????
	 * 
	 * @param ids
	 * @return
	 */
	@RequestMapping(params = "checkRole")
	@ResponseBody
	public ValidForm checkRole(TSRole role,HttpServletRequest request,HttpServletResponse response) {
		ValidForm v = new ValidForm();
		String roleCode=oConvertUtils.getString(request.getParameter("param"));
		String code=oConvertUtils.getString(request.getParameter("code"));
		List<TSRole> roles=systemService.findByProperty(TSRole.class,"roleCode",roleCode);
		if(roles.size()>0&&!code.equals(roleCode))
		{
			v.setInfo("?????????????????????");
			v.setStatus("n");
		}
		return v;
	}
	/**
	 * ??????????????????
	 * @param role
	 */
	protected void delRoleFunction(TSRole role){
		List<TSRoleFunction> roleFunctions=systemService.findByProperty(TSRoleFunction.class,"TSRole.id",role.getId());	
		if (roleFunctions.size()>0) {
			for (TSRoleFunction tsRoleFunction : roleFunctions) {
			systemService.delete(tsRoleFunction);	
			}
		}
		List<TSRoleUser> roleUsers=systemService.findByProperty(TSRoleUser.class,"TSRole.id",role.getId());
	    for (TSRoleUser tsRoleUser : roleUsers) {
	    	systemService.delete(tsRoleUser);
		}	
	}
	/**
	 * ????????????
	 * 
	 * @param ids
	 * @return
	 */
	@RequestMapping(params = "saveRole")
	@ResponseBody
	public AjaxJson saveRole(TSRole role, HttpServletRequest request) {
		AjaxJson j = new AjaxJson();
		if (StringUtil.isNotEmpty(role.getId())) {
			message = "??????: " + role.getRoleName() + "???????????????";
			userService.saveOrUpdate(role);
			systemService.addLog(message, Globals.Log_Type_UPDATE,
					Globals.Log_Leavel_INFO);
		} else {
			message = "??????: " + role.getRoleName() + "???????????????";
			userService.save(role);
			systemService.addLog(message, Globals.Log_Type_INSERT,
					Globals.Log_Leavel_INFO);
		}
		
		return j;
	}

	/**
	 * ????????????????????????
	 * 
	 * @return
	 */
	@RequestMapping(params = "fun")
	public ModelAndView fun(HttpServletRequest request) {
		String roleId = request.getParameter("roleId");
		request.setAttribute("roleId", roleId);
		return new ModelAndView("system/role/roleSet");
	}

	/**
	 * ????????????
	 * 
	 * @param role
	 * @param request
	 * @param response
	 * @param dataGrid
	 * @param user
	 * @return
	 */
	@RequestMapping(params = "setAuthority")
	@ResponseBody
	public List<ComboTree> setAuthority(TSRole role, HttpServletRequest request,
			ComboTree comboTree) {
		CriteriaQuery cq = new CriteriaQuery(TSFunction.class);
		if (comboTree.getId() != null) {
			cq.eq("TSFunction.id",
					comboTree.getId());
		}
		if (comboTree.getId() == null) {
			cq.isNull("TSFunction");
		}
		cq.notEq("functionLevel",Short.parseShort("-1"));
		cq.add();
		List<TSFunction> functionList = systemService.getListByCriteriaQuery(cq,false);
		Collections.sort(functionList, new NumberComparator());
		List<ComboTree> comboTrees = new ArrayList<ComboTree>();
		String roleId = request.getParameter("roleId");
		List<TSFunction> loginActionlist = new ArrayList<TSFunction>();// ??????????????????
		role = this.systemService.get(TSRole.class, roleId);
		if (role != null) {
			List<TSRoleFunction> roleFunctionList=systemService.findByProperty(TSRoleFunction.class, "TSRole.id", role.getId());
			if (roleFunctionList.size() > 0) {
				for (TSRoleFunction roleFunction : roleFunctionList) {
					TSFunction function = (TSFunction) roleFunction
							.getTSFunction();
					loginActionlist.add(function);
				}
			}
		}
		ComboTreeModel comboTreeModel = new ComboTreeModel("id", "functionName", "TSFunctions");
		comboTrees = systemService.ComboTree(functionList,comboTreeModel,loginActionlist);
		return comboTrees;
	}

	/**
	 * ????????????
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(params = "updateAuthority")
	@ResponseBody
	public AjaxJson updateAuthority(HttpServletRequest request) {
		AjaxJson j = new AjaxJson();
		try{
			String roleId =request.getParameter("roleId");
			String rolefunction = request.getParameter("rolefunctions");
			TSRole role = this.systemService.get(TSRole.class, roleId);
			List<TSRoleFunction> roleFunctionList=systemService.findByProperty(TSRoleFunction.class, "TSRole.id", role.getId());
			Map<String,TSRoleFunction> map = new HashMap<String,TSRoleFunction>();
			for (TSRoleFunction functionOfRole : roleFunctionList){
				map.put(functionOfRole.getTSFunction().getId(),functionOfRole);
			}
			String[] roleFunctions = rolefunction.split(",");
			Set<String> set = new HashSet<String>();
			for (String s : roleFunctions) {
				set.add(s);
			}
			updateCompare(set,role,map);
			j.setMsg("??????????????????");			
		}catch (Exception e){
            logger.error(ExceptionUtil.getExceptionMessage(e));    
			j.setMsg("??????????????????");			
		}
		return j;
	}
	/**
	 * ????????????
	 * @param set ?????????????????????
	 * @param role ????????????
	 * @param map ??????????????????
	 * @param entitys ???????????????????????????
	 */
	private void updateCompare(Set<String> set,TSRole role,
			Map<String, TSRoleFunction> map) {
		List<TSRoleFunction> entitys = new ArrayList<TSRoleFunction>();
		List<TSRoleFunction> deleteEntitys = new ArrayList<TSRoleFunction>();
		for (String s : set) {
			if(map.containsKey(s)){
				map.remove(s);
			}else{
				TSRoleFunction rf = new TSRoleFunction();
				TSFunction f = this.systemService.get(TSFunction.class,s);
				rf.setTSFunction(f);
				rf.setTSRole(role);
				entitys.add(rf);
			}
		}
		Collection<TSRoleFunction> collection = map.values();
		Iterator<TSRoleFunction> it = collection.iterator();
		for (; it.hasNext();) {
			deleteEntitys.add(it.next());
	    }
		systemService.batchSave(entitys);
		systemService.deleteAllEntitie(deleteEntitys);
		
	}

	/**
	 * ??????????????????
	 * 
	 * @param role
	 * @param req
	 * @return
	 */
	@RequestMapping(params = "addorupdate")
	public ModelAndView addorupdate(TSRole role, HttpServletRequest req) {
		if (role.getId() != null) {
			role = systemService.getEntity(TSRole.class, role.getId());
			req.setAttribute("role", role);
		}
		return new ModelAndView("system/role/role");
	}

	/**
	 * ??????????????????
	 * 
	 * @param role
	 * @param request
	 * @param response
	 * @param dataGrid
	 * @param user
	 * @return
	 */
	@RequestMapping(params = "setOperate")
	@ResponseBody
	public List<TreeGrid> setOperate(HttpServletRequest request,
			TreeGrid treegrid) {
		String roleId = request.getParameter("roleId");
		CriteriaQuery cq = new CriteriaQuery(TSFunction.class);
		if (treegrid.getId() != null) {
			cq.eq("TSFunction.id",
					treegrid.getId());
		}
		if (treegrid.getId() == null) {
			cq.isNull("TSFunction");
		}
		cq.add();
		List<TSFunction> functionList = systemService.getListByCriteriaQuery(cq,false);
		List<TreeGrid> treeGrids = new ArrayList<TreeGrid>();
		Collections.sort(functionList, new SetListSort());
		TreeGridModel treeGridModel=new TreeGridModel();
		treeGridModel.setRoleid(roleId);
		treeGrids = systemService.treegrid(functionList, treeGridModel);
		return treeGrids;

	}

	/**
	 * ????????????
	 * 
	 * @param ids
	 * @return
	 */
	@RequestMapping(params = "saveOperate")
	@ResponseBody
	public AjaxJson saveOperate(HttpServletRequest request) {
		AjaxJson j = new AjaxJson();
		String fop = request.getParameter("fp");
		String roleId = request.getParameter("roleId");
		//?????????????????????????????????????????????
		clearp(roleId);
		String[] fun_op = fop.split(",");
		String aa = "";
		String bb = "";
		//?????????????????????
		if (fun_op.length == 1) {
			bb = fun_op[0].split("_")[1];
			aa = fun_op[0].split("_")[0];
			savep( roleId,bb,aa);
		}else{
			//??????2????????????
			for (int i = 0; i < fun_op.length; i++) {
				String cc = fun_op[i].split("_")[0];  //??????id
				if (i > 0 && bb.equals(fun_op[i].split("_")[1])) {
					aa += "," + cc;
					if (i== (fun_op.length - 1)) {
						savep( roleId,bb,aa);
					}
				} else if (i > 0) {
					    savep(roleId,bb,aa);
					    aa = fun_op[i].split("_")[0];   //??????ID
					    if (i==(fun_op.length-1)){
					    	bb = fun_op[i].split("_")[1]; //??????id
					    	savep(roleId,bb,aa);
						}
					    
				} else {
					aa = fun_op[i].split("_")[0]; //??????ID
				}
				bb = fun_op[i].split("_")[1]; //??????id

			}	
		}
		
		
		return j;
	}
     /**
      *????????????
      * @param roleId
      * @param functionid
      * @param ids
      */
	public void savep(String roleId,String functionid, String ids) {
		//String hql = "from TSRoleFunction t where" + " t.TSRole.id=" + oConvertUtils.getInt(roleId,0)
		//		+ " " + "and t.TSFunction.id=" + oConvertUtils.getInt(functionid,0);		
		CriteriaQuery cq=new CriteriaQuery(TSRoleFunction.class);
		cq.eq("TSRole.id",roleId);
		cq.eq("TSFunction.id",functionid);
		cq.add();
		List<TSRoleFunction> rFunctions =systemService.getListByCriteriaQuery(cq,false);
		if (rFunctions.size() > 0) {
			TSRoleFunction roleFunction = rFunctions.get(0);
			roleFunction.setOperation(ids);
			systemService.saveOrUpdate(roleFunction);
		}
	}
	/**
	 * ????????????
	 * @param roleId
	 */
	public void clearp(String roleId) {	
		List<TSRoleFunction> rFunctions = systemService.findByProperty(TSRoleFunction.class,"TSRole.id",roleId);
		if (rFunctions.size() > 0){
			for (TSRoleFunction tRoleFunction : rFunctions) {
				tRoleFunction.setOperation(null);
				systemService.saveOrUpdate(tRoleFunction);
			}
	 	}
	}
	/**
	 * ??????????????????
	 * @param request
	 * @param functionId
	 * @param roleId
	 * @return
	 */
	@RequestMapping(params = "operationListForFunction")
	public ModelAndView operationListForFunction(HttpServletRequest request,String functionId,String roleId) {	
		CriteriaQuery cq = new CriteriaQuery(TSOperation.class);
		cq.eq("TSFunction.id", functionId);
		cq.add(); 
		List<TSOperation> operationList = this.systemService.getListByCriteriaQuery(cq, false);
		Set<String> operationCodes = systemService.getOperationCodesByRoleIdAndFunctionId(roleId, functionId);
		request.setAttribute("operationList", operationList);
		request.setAttribute("operationcodes", operationCodes);
		request.setAttribute("functionId", functionId);
		return new ModelAndView("system/role/operationListForFunction");
	}
	/**
	 * ??????????????????
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(params = "updateOperation")
	@ResponseBody
	public AjaxJson updateOperation(HttpServletRequest request) {
		AjaxJson j = new AjaxJson();
		String roleId =request.getParameter("roleId");
		String functionId = request.getParameter("functionId");
		String operationcodes = null;
		try {
			operationcodes = URLDecoder.decode(request.getParameter("operationcodes"), "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		CriteriaQuery cq1=new CriteriaQuery(TSRoleFunction.class);
		cq1.eq("TSRole.id",roleId);
		cq1.eq("TSFunction.id",functionId);
		cq1.add();
		List<TSRoleFunction> rFunctions =systemService.getListByCriteriaQuery(cq1,false);
		if(null!=rFunctions && rFunctions.size()>0){
			TSRoleFunction tsRoleFunction =  rFunctions.get(0);
			tsRoleFunction.setOperation(operationcodes);
			systemService.saveOrUpdate(tsRoleFunction);
		}
		j.setMsg("????????????????????????");
		return j;
	}
}
