package weixin.cms.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.jeecgframework.core.common.controller.BaseController;
import org.jeecgframework.core.common.exception.BusinessException;
import org.jeecgframework.core.common.hibernate.qbc.CriteriaQuery;
import org.jeecgframework.core.common.model.common.UploadFile;
import org.jeecgframework.core.common.model.json.AjaxJson;
import org.jeecgframework.core.common.model.json.DataGrid;
import org.jeecgframework.core.constant.Globals;
import org.jeecgframework.core.util.BrowserUtils;
import org.jeecgframework.core.util.ExceptionUtil;
import org.jeecgframework.core.util.MyBeanUtils;
import org.jeecgframework.core.util.MyClassLoader;
import org.jeecgframework.core.util.ResourceUtil;
import org.jeecgframework.core.util.StringUtil;
import org.jeecgframework.core.util.oConvertUtils;
import org.jeecgframework.poi.excel.ExcelExportUtil;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.entity.ExcelTitle;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.tag.core.easyui.TagUtil;
import org.jeecgframework.web.system.pojo.base.TSDocument;
import org.jeecgframework.web.system.pojo.base.TSType;
import org.jeecgframework.web.system.pojo.base.TSTypegroup;
import org.jeecgframework.web.system.service.SystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import weixin.cms.entity.WeixinCmsSiteEntity;
import weixin.cms.service.WeixinCmsSiteServiceI;
import weixin.util.DateUtils;

/**
 * @Title: Controller
 * @Description: ???????????????
 * @author onlineGenerator
 * @date 2014-07-15 21:04:08
 * @version V1.0
 *
 */
@Scope("prototype")
@Controller
@RequestMapping("/weixinCmsSiteController")
public class WeixinCmsSiteController extends BaseController {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger
			.getLogger(WeixinCmsSiteController.class);

	@Autowired
	private WeixinCmsSiteServiceI weixinCmsSiteService;
	@Autowired
	private SystemService systemService;
	private String message;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * ????????????????????? ????????????
	 * 
	 * @return
	 */
	@RequestMapping(params = "weixinCmsSite")
	public ModelAndView weixinCmsSite(HttpServletRequest request) {
		return new ModelAndView("weixin/cms/site/weixinCmsSiteList");
	}

	/**
	 * easyui AJAX????????????
	 * 
	 * @param request
	 * @param response
	 * @param dataGrid
	 * @param user
	 */

	@RequestMapping(params = "datagrid")
	public void datagrid(WeixinCmsSiteEntity weixinCmsSite,
			HttpServletRequest request, HttpServletResponse response,
			DataGrid dataGrid) {
		CriteriaQuery cq = new CriteriaQuery(WeixinCmsSiteEntity.class,
				dataGrid);
		// ?????????????????????
		org.jeecgframework.core.extend.hqlsearch.HqlGenerateUtil.installHql(cq,
				weixinCmsSite, request.getParameterMap());
		cq.eq(ACCOUNTID, ResourceUtil.getWeiXinAccountId());
		try {
			// ???????????????????????????
		} catch (Exception e) {
			throw new BusinessException(e.getMessage());
		}
		cq.add();
		this.weixinCmsSiteService.getDataGridReturn(cq, true);
		TagUtil.datagrid(response, dataGrid);
	}

	/**
	 * ?????????????????????
	 * 
	 * @return
	 */
	@RequestMapping(params = "doDel")
	@ResponseBody
	public AjaxJson doDel(WeixinCmsSiteEntity weixinCmsSite,
			HttpServletRequest request) {
		AjaxJson j = new AjaxJson();
		weixinCmsSite = systemService.getEntity(WeixinCmsSiteEntity.class,
				weixinCmsSite.getId());
		message = "???????????????????????????";
		try {
			weixinCmsSiteService.delete(weixinCmsSite);
			systemService.addLog(message, Globals.Log_Type_DEL,
					Globals.Log_Leavel_INFO);
		} catch (Exception e) {
			e.printStackTrace();
			message = "???????????????????????????";
			throw new BusinessException(e.getMessage());
		}
		j.setMsg(message);
		return j;
	}

	/**
	 * ???????????????????????????
	 * 
	 * @return
	 */
	@RequestMapping(params = "doBatchDel")
	@ResponseBody
	public AjaxJson doBatchDel(String ids, HttpServletRequest request) {
		AjaxJson j = new AjaxJson();
		message = "???????????????????????????";
		try {
			for (String id : ids.split(",")) {
				WeixinCmsSiteEntity weixinCmsSite = systemService.getEntity(
						WeixinCmsSiteEntity.class, id);
				weixinCmsSiteService.delete(weixinCmsSite);
				systemService.addLog(message, Globals.Log_Type_DEL,
						Globals.Log_Leavel_INFO);
			}
		} catch (Exception e) {
			e.printStackTrace();
			message = "???????????????????????????";
			throw new BusinessException(e.getMessage());
		}
		j.setMsg(message);
		return j;
	}

	/**
	 * ?????????????????????
	 * 
	 * @param ids
	 * @return
	 */
	@RequestMapping(params = "doAdd")
	@ResponseBody
	public AjaxJson doAdd(WeixinCmsSiteEntity weixinCmsSite,
			HttpServletRequest request) {
		AjaxJson j = new AjaxJson();
		List lst = weixinCmsSiteService.findByProperty(
				WeixinCmsSiteEntity.class, "accountid",
				ResourceUtil.getWeiXinAccountId());
		if(lst.size()!=0){
			message="??????????????????????????????????????????";
			j.setSuccess(false);
		}else{
			message = "???????????????????????????";
			try {
				weixinCmsSiteService.save(weixinCmsSite);
				systemService.addLog(message, Globals.Log_Type_INSERT,
						Globals.Log_Leavel_INFO);
			} catch (Exception e) {
				e.printStackTrace();
				message = "???????????????????????????";
				throw new BusinessException(e.getMessage());
			}
		}
		j.setMsg(message);
		return j;
	}

	/**
	 * ?????????????????????
	 * 
	 * @param ids
	 * @return
	 */
	@RequestMapping(params = "doUpdate")
	@ResponseBody
	public AjaxJson doUpdate(WeixinCmsSiteEntity weixinCmsSite,
			HttpServletRequest request) {
		AjaxJson j = new AjaxJson();
		message = "???????????????????????????";
		WeixinCmsSiteEntity t = weixinCmsSiteService.get(
				WeixinCmsSiteEntity.class, weixinCmsSite.getId());
		try {
			MyBeanUtils.copyBeanNotNull2Bean(weixinCmsSite, t);
			weixinCmsSiteService.saveOrUpdate(t);
			systemService.addLog(message, Globals.Log_Type_UPDATE,
					Globals.Log_Leavel_INFO);
		} catch (Exception e) {
			e.printStackTrace();
			message = "???????????????????????????";
			throw new BusinessException(e.getMessage());
		}
		j.setMsg(message);
		return j;
	}

	/**
	 * ?????????????????????????????????
	 * 
	 * @return
	 */
	@RequestMapping(params = "goAdd")
	public ModelAndView goAdd(WeixinCmsSiteEntity weixinCmsSite,
			HttpServletRequest req) {
		if (StringUtil.isNotEmpty(weixinCmsSite.getId())) {
			weixinCmsSite = weixinCmsSiteService.getEntity(
					WeixinCmsSiteEntity.class, weixinCmsSite.getId());
			req.setAttribute("weixinCmsSitePage", weixinCmsSite);
		}
		return new ModelAndView("weixin/cms/site/weixinCmsSite-add");
	}

	/**
	 * ?????????????????????????????????
	 * 
	 * @return
	 */
	@RequestMapping(params = "goUpdate")
	public ModelAndView goUpdate(WeixinCmsSiteEntity weixinCmsSite,
			HttpServletRequest req) {
		if (StringUtil.isNotEmpty(weixinCmsSite.getId())) {
			weixinCmsSite = weixinCmsSiteService.getEntity(
					WeixinCmsSiteEntity.class, weixinCmsSite.getId());
			req.setAttribute("weixinCmsSitePage", weixinCmsSite);
		}
		return new ModelAndView("weixin/cms/site/weixinCmsSite-update");
	}

	/**
	 * ??????????????????
	 * 
	 * @return
	 */
	@RequestMapping(params = "upload")
	public ModelAndView upload(HttpServletRequest req) {
		return new ModelAndView("weixin/cms/site/weixinCmsSiteUpload");
	}

	/**
	 * ??????excel
	 * 
	 * @param request
	 * @param response
	 */
	@RequestMapping(params = "exportXls")
	public void exportXls(WeixinCmsSiteEntity weixinCmsSite,
			HttpServletRequest request, HttpServletResponse response,
			DataGrid dataGrid) {
		response.setContentType("application/vnd.ms-excel");
		String codedFileName = null;
		OutputStream fOut = null;
		try {
			codedFileName = "???????????????";
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
			CriteriaQuery cq = new CriteriaQuery(WeixinCmsSiteEntity.class,
					dataGrid);
			org.jeecgframework.core.extend.hqlsearch.HqlGenerateUtil
					.installHql(cq, weixinCmsSite, request.getParameterMap());

			List<WeixinCmsSiteEntity> weixinCmsSites = this.weixinCmsSiteService
					.getListByCriteriaQuery(cq, false);
			workbook = ExcelExportUtil.exportExcel(new ExcelTitle("?????????????????????",
					"?????????:" + ResourceUtil.getSessionUserName().getRealName(),
					"????????????"), WeixinCmsSiteEntity.class, weixinCmsSites);
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
	public void exportXlsByT(WeixinCmsSiteEntity weixinCmsSite,
			HttpServletRequest request, HttpServletResponse response,
			DataGrid dataGrid) {
		response.setContentType("application/vnd.ms-excel");
		String codedFileName = null;
		OutputStream fOut = null;
		try {
			codedFileName = "???????????????";
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
			workbook = ExcelExportUtil.exportExcel(new ExcelTitle("?????????????????????",
					"?????????:" + ResourceUtil.getSessionUserName().getRealName(),
					"????????????"), WeixinCmsSiteEntity.class, null);
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
	public AjaxJson importExcel(HttpServletRequest request,
			HttpServletResponse response) {
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
				List<WeixinCmsSiteEntity> listWeixinCmsSiteEntitys = (List<WeixinCmsSiteEntity>) ExcelImportUtil
						.importExcelByIs(file.getInputStream(),
								WeixinCmsSiteEntity.class, params);
				for (WeixinCmsSiteEntity weixinCmsSite : listWeixinCmsSiteEntitys) {
					weixinCmsSiteService.save(weixinCmsSite);
				}
				j.setMsg("?????????????????????");
			} catch (Exception e) {
				j.setMsg("?????????????????????");
				logger.error(ExceptionUtil.getExceptionMessage(e));
			} finally {
				try {
					file.getInputStream().close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return j;
	}

	//update-begin-author:taoYan date:20180313 for:???????????????????????????????????????upload??????????????????--
	@RequestMapping(params = "uploadFile", method = RequestMethod.POST)
	@ResponseBody
	public AjaxJson uploadFile(MultipartHttpServletRequest request,
			HttpServletResponse response) {
	//update-end-author:taoYan date:20180313 for:???????????????????????????????????????upload??????????????????--
		AjaxJson j = new AjaxJson();
		Map<String, Object> attributes = new HashMap<String, Object>();
		TSTypegroup tsTypegroup = systemService
				.getTypeGroup("fieltype", "????????????");
		TSType tsType = systemService.getType("files", "??????", tsTypegroup);
		String fileKey = oConvertUtils.getString(request
				.getParameter("fileKey"));// ??????ID
		String documentTitle = oConvertUtils.getString(request
				.getParameter("documentTitle"));// ????????????
		TSDocument document = new TSDocument();
		if (StringUtil.isNotEmpty(fileKey)) {
			document.setId(fileKey);
			document = systemService.getEntity(TSDocument.class, fileKey);
			document.setDocumentTitle(documentTitle);

		}
		document.setSubclassname(MyClassLoader.getPackPath(document));
		document.setCreatedate(DateUtils.gettimestamp());
		document.setTSType(tsType);
		UploadFile uploadFile = new UploadFile(request, document);
		uploadFile.setCusPath("files");
		uploadFile.setSwfpath("swfpath");
		document = systemService.uploadFile(uploadFile);
		attributes.put("url", document.getRealpath());
		attributes.put("fileKey", document.getId());
		attributes.put("name", document.getAttachmenttitle());
		attributes.put("viewhref", "commonController.do?openViewFile&fileid="
				+ document.getId());
		attributes.put("delurl", "commonController.do?delObjFile&fileKey="
				+ document.getId());
		j.setMsg("??????????????????");
		j.setAttributes(attributes);

		return j;
	}
}
