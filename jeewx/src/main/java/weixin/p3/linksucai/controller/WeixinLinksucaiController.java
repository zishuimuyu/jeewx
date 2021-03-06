package weixin.p3.linksucai.controller;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.jeecgframework.core.common.controller.BaseController;
import org.jeecgframework.core.common.exception.BusinessException;
import org.jeecgframework.core.common.hibernate.qbc.CriteriaQuery;
import org.jeecgframework.core.common.model.json.AjaxJson;
import org.jeecgframework.core.common.model.json.DataGrid;
import org.jeecgframework.core.constant.Globals;
import org.jeecgframework.core.util.BrowserUtils;
import org.jeecgframework.core.util.ExceptionUtil;
import org.jeecgframework.core.util.MyBeanUtils;
import org.jeecgframework.core.util.ResourceUtil;
import org.jeecgframework.core.util.StringUtil;
import org.jeecgframework.core.util.oConvertUtils;
import org.jeecgframework.poi.excel.ExcelExportUtil;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.entity.ExcelTitle;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.tag.core.easyui.TagUtil;
import org.jeecgframework.web.system.service.SystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import weixin.guanjia.account.entity.WeixinAccountEntity;
import weixin.guanjia.account.service.WeixinAccountServiceI;
import weixin.guanjia.core.util.WeixinUtil;
import weixin.p3.linksucai.entity.WeixinLinksucaiEntity;
import weixin.p3.linksucai.service.WeixinLinksucaiServiceI;
import weixin.p3.oauth2.rule.RemoteWeixinMethod;
import weixin.p3.oauth2.util.SignatureUtil;



/**   
 * @Title: Controller
 * @Description: ????????????
 * @author onlineGenerator
 * @date 2015-01-22 21:39:44
 * @version V1.0   
 *
 */
@Controller
@RequestMapping("/weixinLinksucaiController")
public class WeixinLinksucaiController extends BaseController {
	/**
	 * ????????????key
	 */
	private static final String SIGN_KEY = "4B6CAED6F7B19126F72780372E839CC47B1912B6CAED753F";
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(WeixinLinksucaiController.class);

	@Autowired
	private WeixinLinksucaiServiceI weixinLinksucaiService;
	@Autowired
	private SystemService systemService;
	@Autowired
	private RemoteWeixinMethod remoteWeixinMethod;
	@Autowired
	private WeixinAccountServiceI weixinAccountService;
	private String message;
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}


	/**
     * ??????????????????
     * @return
     */
	@RequestMapping(params = "privateList")
	public ModelAndView privateList() {
		return new ModelAndView("weixin/guanjia/linksucai/privateWeixinLinksucaiList");
	}
	
    @RequestMapping(params = "privateDatagrid")
	@ResponseBody
	/**
	 * ??????????????????
	 * @param newsTemplate
	 * @param request
	 * @param response
	 * @param dataGrid
	 */
	public void privateDatagrid(WeixinLinksucaiEntity weixinLinksucai,HttpServletRequest request, HttpServletResponse response, DataGrid dataGrid) {
    	CriteriaQuery cq = new CriteriaQuery(WeixinLinksucaiEntity.class, dataGrid);
		cq.eq("accountid", ResourceUtil.getShangJiaAccountId());
		cq.add();
		org.jeecgframework.core.extend.hqlsearch.HqlGenerateUtil.installHql(cq,weixinLinksucai);
		this.weixinLinksucaiService.getDataGridReturn(cq, true);
		String baseurl = ResourceUtil.getConfigByName("domain");
		
		for(int i=0;i<dataGrid.getResults().size();i++)
		{
			WeixinLinksucaiEntity  t=(WeixinLinksucaiEntity) dataGrid.getResults().get(i);
			
			String inner_link = baseurl+"/weixinLinksucaiController.do?link&id="+t.getId();
			t.setInnerLink(inner_link);
		}
		//update-begin--Author:macaholin  Date:20150404 for??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
		List<WeixinAccountEntity> accountList = weixinAccountService.loadAll(WeixinAccountEntity.class);
		//update-end--Author:macaholin  Date:20150404 for??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
		TagUtil.datagrid(response, dataGrid);
	}
    
	/**
	 * ????????????
	 */
	@RequestMapping(params = "link")
	public void link(WeixinLinksucaiEntity weixinLinksucai,HttpServletRequest request, HttpServletResponse response) {
		//??????????????????
		String backUrl = this.getRequestUrlWithParams(request);
		//URL??????ID
		String id = request.getParameter("id");
		//URL????????????
		weixinLinksucai = systemService.getEntity(WeixinLinksucaiEntity.class, id);
		//??????????????????ID
		String accountid = weixinLinksucai.getAccountid();
		
		//update-begin-------author:scott-----------date:20151012--------for:?????????????????????jwid????????????jwid??????ID?????????????????????------------
		//??????????????????jwid???????????????????????????????????????
		String jwid = request.getParameter("jwid");
		if(oConvertUtils.isNotEmpty(jwid)){
			WeixinAccountEntity weixinAccountEntity = weixinAccountService.getWeixinAccountByWeixinOldId(jwid);
			if(weixinAccountEntity!=null){
				accountid = weixinAccountEntity.getId();
			}
		}
		//update-end-------author:scott-----------date:20151012--------for:?????????????????????jwid????????????jwid??????ID?????????????????????--------------
		
		
		//???????????? Openid
		String openid = ResourceUtil.getUserOpenId();
		//???????????????
		String outer_link_deal = null;
		
		//update-start--Author:scott  Date:20150809 for???????????????????????????????????????????????????????????????????????????----------------------
	    String requestQueryString = (request.getRequestURL() + "?" + request.getQueryString()).replace(weixinLinksucai.getInnerLink(), "");
	    String outUrl = weixinLinksucai.getOuterLink();
	    if(oConvertUtils.isNotEmpty(requestQueryString)){
	    	outUrl = outUrl + requestQueryString;
	    }
	    //update-start--Author:scott  Date:20150809 for???????????????????????????????????????????????????????????????????????????----------------------
	    
		//-------------------------------------------------------------------------------------------------------------
		//?????????????????????author2.0??????
		if(oConvertUtils.isEmpty(openid)){
			 outer_link_deal = remoteWeixinMethod.callWeixinAuthor2ReturnUrl(request, accountid, backUrl);
		}
		if(oConvertUtils.isEmpty(outer_link_deal)){
		    openid = ResourceUtil.getUserOpenId();
		    System.out.println("------------------begin----------begin1-------------------");
			outer_link_deal = weixinLinksucaiService.installOuterLinkWithSysParams(outUrl, openid, accountid,null);
			System.out.println("------------------begin----------begin2-------------------");
		}
		//-------------------------------------------------------------------------------------------------------------
		
		try {
			//---update-begin--author:scott-----date:20151127-----for:???????????????----------------------------------
			if(outer_link_deal.indexOf("https://open.weixin.qq.com")!=-1){
				//???????????????auth2.0??????????????????
				response.sendRedirect(outer_link_deal);
			}else{
				//???????????????????????????????????????
				String sign = SignatureUtil.sign(SignatureUtil.getSignMap(outer_link_deal), SIGN_KEY);
				response.sendRedirect(outer_link_deal+"&sign="+sign);
			}
			//---update-end--author:scott-----date:20151127-----for:???????????????----------------------------------
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * ??????????????????
	 * 
	 * @return
	 */
	@RequestMapping(params = "doDel")
	@ResponseBody
	public AjaxJson doDel(WeixinLinksucaiEntity weixinLinksucai, HttpServletRequest request) {
		AjaxJson j = new AjaxJson();
		weixinLinksucai = systemService.getEntity(WeixinLinksucaiEntity.class, weixinLinksucai.getId());
		message = "????????????????????????";
		
		if(!weixinLinksucai.getAccountid().equals(ResourceUtil.getShangJiaAccountId())){
			message="????????????????????????????????????????????????";
		}else{
			try{
				weixinLinksucaiService.delete(weixinLinksucai);
				systemService.addLog(message, Globals.Log_Type_DEL, Globals.Log_Leavel_INFO);
			}catch(Exception e){
				e.printStackTrace();
				message = "????????????????????????";
				throw new BusinessException(e.getMessage());
			}
		}
		j.setMsg(message);
		return j;
	}
	
	/**
	 * ????????????????????????
	 * 
	 * @return
	 */
	@RequestMapping(params = "doBatchDel")
	@ResponseBody
	public AjaxJson doBatchDel(String ids,HttpServletRequest request){
		AjaxJson j = new AjaxJson();
		message = "????????????????????????";
		try{
			for(String id:ids.split(",")){
				WeixinLinksucaiEntity weixinLinksucai = systemService.getEntity(WeixinLinksucaiEntity.class, 
				id
				);
				if(!weixinLinksucai.getAccountid().equals(ResourceUtil.getShangJiaAccountId())){
					continue;
				}
				weixinLinksucaiService.delete(weixinLinksucai);
				systemService.addLog(message, Globals.Log_Type_DEL, Globals.Log_Leavel_INFO);
			}
		}catch(Exception e){
			e.printStackTrace();
			message = "????????????????????????";
			throw new BusinessException(e.getMessage());
		}
		j.setMsg(message);
		return j;
	}


	/**
	 * ??????????????????
	 * 
	 * @param ids
	 * @return
	 */
	@RequestMapping(params = "doAdd")
	@ResponseBody
	public AjaxJson doAdd(WeixinLinksucaiEntity weixinLinksucai, HttpServletRequest request) {
		AjaxJson j = new AjaxJson();
		message = "????????????????????????";
		try{
			weixinLinksucaiService.save(weixinLinksucai);
			systemService.addLog(message, Globals.Log_Type_INSERT, Globals.Log_Leavel_INFO);
		}catch(Exception e){
			e.printStackTrace();
			message = "????????????????????????";
			throw new BusinessException(e.getMessage());
		}
		j.setMsg(message);
		return j;
	}
	
	/**
	 * ??????????????????
	 * 
	 * @param ids
	 * @return
	 */
	@RequestMapping(params = "doUpdate")
	@ResponseBody
	public AjaxJson doUpdate(WeixinLinksucaiEntity weixinLinksucai, HttpServletRequest request) {
		AjaxJson j = new AjaxJson();
		message = "????????????????????????";
		WeixinLinksucaiEntity t = weixinLinksucaiService.get(WeixinLinksucaiEntity.class, weixinLinksucai.getId());
		try {
			MyBeanUtils.copyBeanNotNull2Bean(weixinLinksucai, t);
			weixinLinksucaiService.saveOrUpdate(t);
			systemService.addLog(message, Globals.Log_Type_UPDATE, Globals.Log_Leavel_INFO);
		} catch (Exception e) {
			e.printStackTrace();
			message = "????????????????????????";
			throw new BusinessException(e.getMessage());
		}
		j.setMsg(message);
		return j;
	}
	

	/**
	 * ??????????????????????????????
	 * 
	 * @return
	 */
	@RequestMapping(params = "goAdd")
	public ModelAndView goAdd(WeixinLinksucaiEntity weixinLinksucai, HttpServletRequest req) {
		if (StringUtil.isNotEmpty(weixinLinksucai.getId())) {
			weixinLinksucai = weixinLinksucaiService.getEntity(WeixinLinksucaiEntity.class, weixinLinksucai.getId());
			req.setAttribute("weixinLinksucaiPage", weixinLinksucai);
			
		}
		req.setAttribute("accountid", ResourceUtil.getShangJiaAccountId());
		return new ModelAndView("weixin/guanjia/linksucai/weixinLinksucai-add");
	}
	/**
	 * ??????????????????????????????
	 * 
	 * @return
	 */
	@RequestMapping(params = "goUpdate")
	public ModelAndView goUpdate(WeixinLinksucaiEntity weixinLinksucai, HttpServletRequest req) {
		if (StringUtil.isNotEmpty(weixinLinksucai.getId())) {
			weixinLinksucai = weixinLinksucaiService.getEntity(WeixinLinksucaiEntity.class, weixinLinksucai.getId());
			req.setAttribute("weixinLinksucaiPage", weixinLinksucai);
			
		}
		req.setAttribute("accountid", ResourceUtil.getShangJiaAccountId());
		return new ModelAndView("weixin/guanjia/linksucai/weixinLinksucai-update");
	}
	
	/**
	 * ??????????????????
	 * 
	 * @return
	 */
	@RequestMapping(params = "upload")
	public ModelAndView upload(HttpServletRequest req) {
		return new ModelAndView("weixin/guanjia/linksucai/weixinLinksucaiUpload");
	}
	
	/**
	 * ??????excel
	 * 
	 * @param request
	 * @param response
	 */
	@RequestMapping(params = "exportXls")
	public void exportXls(WeixinLinksucaiEntity weixinLinksucai,HttpServletRequest request,HttpServletResponse response
			, DataGrid dataGrid) {
		response.setContentType("application/vnd.ms-excel");
		String codedFileName = null;
		OutputStream fOut = null;
		try {
			codedFileName = "????????????";
			// ?????????????????????????????????????????????????????????
			if (BrowserUtils.isIE(request)) {
				response.setHeader(
						"content-disposition",
						"attachment;filename="
								+ java.net.URLEncoder.encode(codedFileName,
										"UTF-8") + ".xls");
			} else {
				String newtitle = new String(codedFileName.getBytes("UTF-8"),
						"ISO8859-1");
				response.setHeader("content-disposition",
						"attachment;filename=" + newtitle + ".xls");
			}
			// ?????????????????????
			HSSFWorkbook workbook = null;
			CriteriaQuery cq = new CriteriaQuery(WeixinLinksucaiEntity.class, dataGrid);
			org.jeecgframework.core.extend.hqlsearch.HqlGenerateUtil.installHql(cq, weixinLinksucai, request.getParameterMap());
			
			List<WeixinLinksucaiEntity> weixinLinksucais = this.weixinLinksucaiService.getListByCriteriaQuery(cq,false);
			workbook = ExcelExportUtil.exportExcel(new ExcelTitle("??????????????????", "?????????:"+ResourceUtil.getSessionUserName().getRealName(),
					"????????????"), WeixinLinksucaiEntity.class, weixinLinksucais);
			fOut = response.getOutputStream();
			workbook.write(fOut);
		} catch (Exception e) {
		} finally {
			try {
				fOut.flush();
				fOut.close();
			} catch (IOException e) {

			}
		}
	}
	/**
	 * ??????excel ?????????
	 * 
	 * @param request
	 * @param response
	 */
	@RequestMapping(params = "exportXlsByT")
	public void exportXlsByT(WeixinLinksucaiEntity weixinLinksucai,HttpServletRequest request,HttpServletResponse response
			, DataGrid dataGrid) {
		response.setContentType("application/vnd.ms-excel");
		String codedFileName = null;
		OutputStream fOut = null;
		try {
			codedFileName = "????????????";
			// ?????????????????????????????????????????????????????????
			if (BrowserUtils.isIE(request)) {
				response.setHeader(
						"content-disposition",
						"attachment;filename="
								+ java.net.URLEncoder.encode(codedFileName,
										"UTF-8") + ".xls");
			} else {
				String newtitle = new String(codedFileName.getBytes("UTF-8"),
						"ISO8859-1");
				response.setHeader("content-disposition",
						"attachment;filename=" + newtitle + ".xls");
			}
			// ?????????????????????
			HSSFWorkbook workbook = null;
			workbook = ExcelExportUtil.exportExcel(new ExcelTitle("??????????????????", "?????????:"+ResourceUtil.getSessionUserName().getRealName(),
					"????????????"), WeixinLinksucaiEntity.class, null);
			fOut = response.getOutputStream();
			workbook.write(fOut);
		} catch (Exception e) {
		} finally {
			try {
				fOut.flush();
				fOut.close();
			} catch (IOException e) {

			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(params = "importExcel", method = RequestMethod.POST)
	@ResponseBody
	public AjaxJson importExcel(HttpServletRequest request, HttpServletResponse response) {
		AjaxJson j = new AjaxJson();
		
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
		for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
			MultipartFile file = entity.getValue();// ????????????????????????
			ImportParams params = new ImportParams();
			params.setTitleRows(2);
			params.setSecondTitleRows(1);
			params.setNeedSave(true);
			try {
				List<WeixinLinksucaiEntity> listWeixinLinksucaiEntitys = 
					(List<WeixinLinksucaiEntity>)ExcelImportUtil.importExcelByIs(file.getInputStream(),WeixinLinksucaiEntity.class,params);
				for (WeixinLinksucaiEntity weixinLinksucai : listWeixinLinksucaiEntitys) {
					weixinLinksucaiService.save(weixinLinksucai);
				}
				j.setMsg("?????????????????????");
			} catch (Exception e) {
				j.setMsg("?????????????????????");
				logger.error(ExceptionUtil.getExceptionMessage(e));
			}finally{
				try {
					file.getInputStream().close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return j;
	}
	
	@RequestMapping(params = "poplink")
	public ModelAndView poplink(ModelMap modelMap,
                                    @RequestParam String id) 
	{
		//WeixinLinksucaiEntity weixinLinksucai = weixinLinksucaiService.getEntity(WeixinLinksucaiEntity.class, id);
		
		ResourceBundle bundler = ResourceBundle.getBundle("sysConfig");
		String absolutePathUrl =  bundler.getString("domain")  + "/weixinLinksucaiController.do?link&id=" + id;
        modelMap.put("url",absolutePathUrl);
		return new ModelAndView("weixin/guanjia/linksucai/poplinksucai");
	}
	
	
	
	/**
     * ??????Request????????????????????? ?????????
     * @param request
     * @return
     */
    private static String getRequestUrlWithParams(HttpServletRequest request){
  	  String backurl = request.getScheme()+"://"+request.getServerName()+request.getRequestURI()+"?"+request.getQueryString();
  	  return backurl;
    }
}
