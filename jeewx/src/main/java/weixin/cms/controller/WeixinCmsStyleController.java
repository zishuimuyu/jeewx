package weixin.cms.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
import org.jeecgframework.core.util.ZipUtil;
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

import weixin.cms.entity.WeixinCmsStyleEntity;
import weixin.cms.service.WeixinCmsStyleServiceI;
import weixin.util.DateUtils;

/**
 * @Title: Controller
 * @Description: ???????????????
 * @author onlineGenerator
 * @date 2014-07-15 22:20:46
 * @version V1.0
 *
 */
@Scope("prototype")
@Controller
@RequestMapping("/weixinCmsStyleController")
public class WeixinCmsStyleController extends BaseController {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger
			.getLogger(WeixinCmsStyleController.class);

	@Autowired
	private WeixinCmsStyleServiceI weixinCmsStyleService;
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
	@RequestMapping(params = "weixinCmsStyle")
	public ModelAndView weixinCmsStyle(HttpServletRequest request) {
		return new ModelAndView("weixin/cms/style/weixinCmsStyleList");
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
	public void datagrid(WeixinCmsStyleEntity weixinCmsStyle,
			HttpServletRequest request, HttpServletResponse response,
			DataGrid dataGrid) {
		CriteriaQuery cq = new CriteriaQuery(WeixinCmsStyleEntity.class,
				dataGrid);
		// ?????????????????????
		org.jeecgframework.core.extend.hqlsearch.HqlGenerateUtil.installHql(cq,
				weixinCmsStyle, request.getParameterMap());
		cq.eq(ACCOUNTID, ResourceUtil.getWeiXinAccountId());
		try {
			// ???????????????????????????
		} catch (Exception e) {
			throw new BusinessException(e.getMessage());
		}
		cq.add();
		this.weixinCmsStyleService.getDataGridReturn(cq, true);
		TagUtil.datagrid(response, dataGrid);
	}

	/**
	 * ?????????????????????
	 * 
	 * @return
	 */
	@RequestMapping(params = "doDel")
	@ResponseBody
	public AjaxJson doDel(WeixinCmsStyleEntity weixinCmsStyle,
			HttpServletRequest request) {
		AjaxJson j = new AjaxJson();
		weixinCmsStyle = systemService.getEntity(WeixinCmsStyleEntity.class,
				weixinCmsStyle.getId());
		message = "???????????????????????????";
		try {
			weixinCmsStyleService.delete(weixinCmsStyle);
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
				WeixinCmsStyleEntity weixinCmsStyle = systemService.getEntity(
						WeixinCmsStyleEntity.class, id);
				weixinCmsStyleService.delete(weixinCmsStyle);
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
	public AjaxJson doAdd(WeixinCmsStyleEntity weixinCmsStyle,
			HttpServletRequest request) {
		AjaxJson j = new AjaxJson();
		message = "???????????????????????????";
		try {
			weixinCmsStyleService.save(weixinCmsStyle);
			systemService.addLog(message, Globals.Log_Type_INSERT,
					Globals.Log_Leavel_INFO);
		} catch (Exception e) {
			e.printStackTrace();
			message = "???????????????????????????";
			throw new BusinessException(e.getMessage());
		}
		j.setMsg(message);
		j.setObj(weixinCmsStyle);
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
	public AjaxJson doUpdate(WeixinCmsStyleEntity weixinCmsStyle,
			HttpServletRequest request) {
		AjaxJson j = new AjaxJson();
		message = "???????????????????????????";
		WeixinCmsStyleEntity t = weixinCmsStyleService.get(
				WeixinCmsStyleEntity.class, weixinCmsStyle.getId());
		try {
			MyBeanUtils.copyBeanNotNull2Bean(weixinCmsStyle, t);
			weixinCmsStyleService.saveOrUpdate(t);
			systemService.addLog(message, Globals.Log_Type_UPDATE,
					Globals.Log_Leavel_INFO);
		} catch (Exception e) {
			e.printStackTrace();
			message = "???????????????????????????";
			throw new BusinessException(e.getMessage());
		}
		j.setMsg(message);
		j.setObj(weixinCmsStyle);
		return j;
	}

	/**
	 * ?????????????????????????????????
	 * 
	 * @return
	 */
	@RequestMapping(params = "goAdd")
	public ModelAndView goAdd(WeixinCmsStyleEntity weixinCmsStyle,
			HttpServletRequest req) {
		if (StringUtil.isNotEmpty(weixinCmsStyle.getId())) {
			weixinCmsStyle = weixinCmsStyleService.getEntity(
					WeixinCmsStyleEntity.class, weixinCmsStyle.getId());
			req.setAttribute("weixinCmsStylePage", weixinCmsStyle);
		}
		return new ModelAndView("weixin/cms/style/weixinCmsStyle-add");
	}

	/**
	 * ?????????????????????????????????
	 * 
	 * @return
	 */
	@RequestMapping(params = "goUpdate")
	public ModelAndView goUpdate(WeixinCmsStyleEntity weixinCmsStyle,
			HttpServletRequest req) {
		if (StringUtil.isNotEmpty(weixinCmsStyle.getId())) {
			weixinCmsStyle = weixinCmsStyleService.getEntity(
					WeixinCmsStyleEntity.class, weixinCmsStyle.getId());
			req.setAttribute("weixinCmsStylePage", weixinCmsStyle);
		}
		return new ModelAndView("weixin/cms/style/weixinCmsStyle-update");
	}

	/**
	 * ??????????????????
	 * 
	 * @return
	 */
	@RequestMapping(params = "doupload")
	public ModelAndView doupload(HttpServletRequest req) {
		return new ModelAndView("weixin/cms/style/weixinCmsStyleUpload");
	}

	/**
	 * ??????excel
	 * 
	 * @param request
	 * @param response
	 */
	@RequestMapping(params = "exportXls")
	public void exportXls(WeixinCmsStyleEntity weixinCmsStyle,
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
			CriteriaQuery cq = new CriteriaQuery(WeixinCmsStyleEntity.class,
					dataGrid);
			org.jeecgframework.core.extend.hqlsearch.HqlGenerateUtil
					.installHql(cq, weixinCmsStyle, request.getParameterMap());

			List<WeixinCmsStyleEntity> weixinCmsStyles = this.weixinCmsStyleService
					.getListByCriteriaQuery(cq, false);
			workbook = ExcelExportUtil.exportExcel(new ExcelTitle("?????????????????????",
					"?????????:" + ResourceUtil.getSessionUserName().getRealName(),
					"????????????"), WeixinCmsStyleEntity.class, weixinCmsStyles);
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
	public void exportXlsByT(WeixinCmsStyleEntity weixinCmsStyle,
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
					"????????????"), WeixinCmsStyleEntity.class, null);
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
				List<WeixinCmsStyleEntity> listWeixinCmsStyleEntitys = (List<WeixinCmsStyleEntity>) ExcelImportUtil
						.importExcelByIs(file.getInputStream(),
								WeixinCmsStyleEntity.class, params);
				for (WeixinCmsStyleEntity weixinCmsStyle : listWeixinCmsStyleEntitys) {
					weixinCmsStyleService.save(weixinCmsStyle);
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

	@RequestMapping(params = "upload", method = RequestMethod.POST)
	@ResponseBody
	public AjaxJson upload(MultipartHttpServletRequest request,
			HttpServletResponse response) {
		AjaxJson j = new AjaxJson();
		// ????????????ID
		String id = request.getParameter("id");
		Map<String, Object> attributes = new HashMap<String, Object>();
		TSTypegroup tsTypegroup = systemService.getTypeGroup("fieltype", "????????????");
		TSType tsType = systemService.getType("files", "??????", tsTypegroup);
		String fileKey = oConvertUtils.getString(request.getParameter("fileKey"));// ??????ID
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
		// ??????????????????
		WeixinCmsStyleEntity weixinCmsStyle = weixinCmsStyleService.getEntity(
				WeixinCmsStyleEntity.class, id);
		// ?????????????????????????????????template/cms/{userid}/????????????
		String requestPath = request.getSession().getServletContext().getRealPath("/template/cms");
		// ????????????????????????????????????
		String realpath = requestPath + "/"+ weixinCmsStyle.getAccountid() + "/"+ weixinCmsStyle.getTemplateUrl()+"/";
		File tempfolder = new File(realpath);
		if (!tempfolder.exists()) {
			// ???????????????????????????
			tempfolder.mkdirs();
		}
		weixinCmsStyleService.updateEntitie(weixinCmsStyle);
		try {
			String path = request.getSession().getServletContext().getRealPath("/")+ document.getRealpath();
			ZipUtil.unZip(path,tempfolder.getAbsolutePath()+"/");
		} catch (Exception e) {
			e.printStackTrace();
		}
		attributes.put("url", document.getRealpath());
		attributes.put("fileKey", document.getId());
		attributes.put("name", document.getAttachmenttitle());
		attributes.put("viewhref", "commonController.do?openViewFile&fileid="+ document.getId());
		attributes.put("delurl", "commonController.do?delObjFile&fileKey="+ document.getId());
		j.setMsg("??????????????????");
		j.setAttributes(attributes);
		return j;
	}
	
	/**
	 * ?????????????????????
	 * @param request
	 * @param response
	 * @param id
	 */
	@RequestMapping(params = "downloadTemplate")
	public void downloadTemplate(HttpServletRequest request,HttpServletResponse response,String id){
		String url = "default";
		WeixinCmsStyleEntity style = weixinCmsStyleService.get(WeixinCmsStyleEntity.class, id);
		if(style!=null){
			url = style.getTemplateUrl();
		}
		String path = "template/cms/"+ResourceUtil.getWeiXinAccountId()+"/"+url;
		
		String sourceSrc = request.getSession().getServletContext().getRealPath(path);
		String downloadSrc = sourceSrc + ".zip";
		String fileName = style.getTemplateName()+".zip";
		try {
			// ?????????????????????
			ZipUtil.compress(sourceSrc, downloadSrc);
			response.reset();
			// ??????response???Header
			response.setContentType("APPLICATION/OCTET-STREAM");
			OutputStream out = new BufferedOutputStream(response.getOutputStream());
			// ???????????????
			response.setHeader("Content-disposition", "attachment;filename="+ new String(fileName.getBytes("utf-8"), "ISO8859-1"));
			InputStream inStream = new FileInputStream(downloadSrc);
			int bytesRead = 0;
			byte[] buffer = new byte[8192];
			while ((bytesRead = inStream.read(buffer, 0, 8192)) != -1) {
				out.write(buffer, 0, bytesRead);
			}
			out.write(buffer);
			out.flush();
			out.close();
			inStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * ??????????????????
	 * @param request
	 * @param response
	 */
	@RequestMapping(params = "downloadDefaultTemplate")
	public void downloadDefaultTemplate(HttpServletRequest request,HttpServletResponse response) {
		String path = "template/cms/default/";
		String sourceSrc = request.getSession().getServletContext().getRealPath(path);
		String downloadSrc = sourceSrc + ".zip";
		try {
			// ?????????????????????
			ZipUtil.compress(sourceSrc, downloadSrc);
			String fileName = "jeewx_default.zip";
			response.reset();
			// ??????response???Header
			response.setContentType("APPLICATION/OCTET-STREAM");
			OutputStream out = new BufferedOutputStream(response.getOutputStream());
			// ???????????????
			response.setHeader("Content-disposition", "attachment;filename="+ new String(fileName.getBytes("utf-8"), "ISO8859-1"));
			InputStream inStream = new FileInputStream(downloadSrc);
			int bytesRead = 0;
			byte[] buffer = new byte[8192];
			while ((bytesRead = inStream.read(buffer, 0, 8192)) != -1) {
				out.write(buffer, 0, bytesRead);
			}
			out.write(buffer);
			out.flush();
			out.close();
			inStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
