package org.jeecgframework.web.demo.controller.test;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.jeecgframework.poi.excel.entity.vo.POIConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.jeecgframework.core.common.controller.BaseController;
import org.jeecgframework.core.common.hibernate.qbc.CriteriaQuery;
import org.jeecgframework.core.common.model.json.AjaxJson;
import org.jeecgframework.core.common.model.json.DataGrid;
import org.jeecgframework.core.constant.Globals;
import org.jeecgframework.core.util.BrowserUtils;
import org.jeecgframework.core.util.ExceptionUtil;
import org.jeecgframework.core.util.StringUtil;
import org.jeecgframework.poi.excel.ExcelExportUtil;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.ExcelUtil;
import org.jeecgframework.poi.excel.entity.ExcelTitle;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.entity.TemplateExportParams;
import org.jeecgframework.tag.core.easyui.TagUtil;
import org.jeecgframework.web.system.pojo.base.TSDepart;
import org.jeecgframework.web.system.service.SystemService;
import org.jeecgframework.core.util.MyBeanUtils;
import org.jeecgframework.web.demo.entity.test.CourseEntity;
import org.jeecgframework.web.demo.entity.test.JpPersonEntity;
import org.jeecgframework.web.demo.service.test.CourseServiceI;

/**
 * @Title: Controller
 * @Description: ??????
 * @author jueyue
 * @date 2013-08-31 22:53:07
 * @version V1.0
 *
 */
@Scope("prototype")
@Controller
@RequestMapping("/courseController")
public class CourseController extends BaseController {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(CourseController.class);

	@Autowired
	private CourseServiceI courseService;
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
	 * ???????????? ????????????
	 *
	 * @return
	 */
	@RequestMapping(params = "course")
	public ModelAndView course(HttpServletRequest request) {
		return new ModelAndView("jeecg/demo/test/courseList");
	}

	/**
	 * easyui AJAX????????????
	 *
	 * @param request
	 * @param response
	 * @param dataGrid
	 */

	@RequestMapping(params = "datagrid")
	public void datagrid(CourseEntity course,HttpServletRequest request, HttpServletResponse response, DataGrid dataGrid) {
		CriteriaQuery cq = new CriteriaQuery(CourseEntity.class, dataGrid);
		//?????????????????????
		org.jeecgframework.core.extend.hqlsearch.HqlGenerateUtil.installHql(cq, course, request.getParameterMap());
		this.courseService.getDataGridReturn(cq, true);
		TagUtil.datagrid(response, dataGrid);
	}

	/**
	 * ????????????
	 *
	 * @return
	 */
	@RequestMapping(params = "del")
	@ResponseBody
	public AjaxJson del(CourseEntity course, HttpServletRequest request) {
		AjaxJson j = new AjaxJson();
		course = systemService.getEntity(CourseEntity.class, course.getId());
		message = "??????????????????";
		courseService.delete(course);
		systemService.addLog(message, Globals.Log_Type_DEL, Globals.Log_Leavel_INFO);

		j.setMsg(message);
		return j;
	}


	/**
	 * ????????????
	 *
	 * @param course
	 * @return
	 */
	@RequestMapping(params = "save")
	@ResponseBody
	public AjaxJson save(CourseEntity course, HttpServletRequest request) {
		AjaxJson j = new AjaxJson();
		if (StringUtil.isNotEmpty(course.getId())) {
			message = "??????????????????";
			try {
				courseService.updateCourse(course);
				systemService.addLog(message, Globals.Log_Type_UPDATE, Globals.Log_Leavel_INFO);
			} catch (Exception e) {
				e.printStackTrace();
				message = "??????????????????";
			}
		} else {
			message = "??????????????????";
			courseService.saveCourse(course);
			systemService.addLog(message, Globals.Log_Type_INSERT, Globals.Log_Leavel_INFO);
		}
		j.setMsg(message);
		return j;
	}

	/**
	 * ????????????????????????
	 *
	 * @return
	 */
	@RequestMapping(params = "addorupdate")
	public ModelAndView addorupdate(CourseEntity course, HttpServletRequest req) {
		if (StringUtil.isNotEmpty(course.getId())) {
			course = courseService.getEntity(CourseEntity.class, course.getId());
			req.setAttribute("coursePage", course);
		}
		return new ModelAndView("jeecg/demo/test/course");
	}
	/**
	 * ????????????
	 *
	 * @return
	 */
	@RequestMapping(params = "studentsList")
	public ModelAndView studentsList(CourseEntity course, HttpServletRequest req) {
		if (StringUtil.isNotEmpty(course.getId())) {
			course = courseService.getEntity(CourseEntity.class, course.getId());
			req.setAttribute("studentsList", course.getStudents());
		}
		return new ModelAndView("jeecg/demo/test/CourseStudentList");
	}
	/**
	 * ????????????
	 *
	 * @return
	 */
	@RequestMapping(params = "upload")
	public ModelAndView upload(HttpServletRequest req) {
		return new ModelAndView("jeecg/demo/test/courseUpload");
	}

	/**
	 * ??????excel
	 *
	 * @param request
	 * @param response
	 */
	@RequestMapping(params = "exportXls")
	public String exportXls(CourseEntity course,HttpServletRequest request,HttpServletResponse response
			, DataGrid dataGrid,ModelMap map) {

        CriteriaQuery cq = new CriteriaQuery(CourseEntity.class, dataGrid);
        org.jeecgframework.core.extend.hqlsearch.HqlGenerateUtil.installHql(cq, course, request.getParameterMap());
        List<CourseEntity> courses = this.courseService.getListByCriteriaQuery(cq,false);

        map.put(POIConstants.FILE_NAME,"????????????");
        map.put(POIConstants.CLASS,CourseEntity.class);
        map.put(POIConstants.EXCEL_TITLE,new ExcelTitle("????????????", "?????????:Jeecg",
                "????????????"));
        map.put(POIConstants.DATA_LIST,courses);
        return POIConstants.JEECG_EXCEL_VIEW;

	}
	/**
	 * ??????excel ?????????
	 *
	 * @param request
	 * @param response
	 */
	@RequestMapping(params = "exportXlsByTest")
	public void exportXlsByTest(CourseEntity course,HttpServletRequest request,HttpServletResponse response
			, DataGrid dataGrid) {
		response.setContentType("application/vnd.ms-excel");
		String codedFileName = null;
		OutputStream fOut = null;
		try {
			codedFileName = "??????????????????";
			// ?????????????????????????????????????????????????????????
			if (BrowserUtils.isIE(request)) {
				response.setHeader(
						"content-disposition",
						"attachment;filename="
								+ java.net.URLEncoder.encode(codedFileName,
										"UTF-8") + ".xls");//??????????????????????????????????????????????????????
			} else {
				String newtitle = new String(codedFileName.getBytes("UTF-8"),
						"ISO8859-1");
				response.setHeader("content-disposition",
						"attachment;filename=" + newtitle + ".xls");
			}

			// ?????????????????????
			Workbook workbook = null;
			Map<String,Object> map = new HashMap<String, Object>();
			map.put("month", 10);
			Map<String,Object> temp;
			for(int i = 1;i<8;i++){
				temp = new HashMap<String, Object>();
				temp.put("per", i*10);
				temp.put("mon", i*1000);
				temp.put("summon", i*10000);
				map.put("i"+i, temp);
			}
			workbook = ExcelExportUtil.exportExcel(
					new TemplateExportParams("export/template/exportTemp.xls",1),map);
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
	public void exportXlsByT(CourseEntity course,HttpServletRequest request,HttpServletResponse response
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
										"UTF-8") + ".xls");//??????????????????????????????????????????????????????
			} else {
				String newtitle = new String(codedFileName.getBytes("UTF-8"),
						"ISO8859-1");
				response.setHeader("content-disposition",
						"attachment;filename=" + newtitle + ".xls");
			}

			// ?????????????????????
			Workbook workbook = null;
			CriteriaQuery cq = new CriteriaQuery(CourseEntity.class, dataGrid);
			org.jeecgframework.core.extend.hqlsearch.HqlGenerateUtil.installHql(cq, course, request.getParameterMap());
			List<CourseEntity> courses = this.courseService.getListByCriteriaQuery(cq,false);
			Map<String,Object> map = new HashMap<String, Object>();
			map.put("year", "2013");
			map.put("sunCourses", courses.size());
			Map<String,Object> obj = new HashMap<String, Object>();
			map.put("obj", obj);
			obj.put("name", courses.size());
			workbook = ExcelExportUtil.exportExcel(new TemplateExportParams("export/template/exportTemp.xls"),
					CourseEntity.class, courses,map);
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
			params.setSecondTitleRows(2);
			params.setNeedSave(true);
			try {
				List<CourseEntity> listCourses =
					(List<CourseEntity>)ExcelImportUtil.importExcelByIs(file.getInputStream(),CourseEntity.class,params);
				for (CourseEntity course : listCourses) {
					if(course.getName()!=null){
						courseService.saveCourse(course);
					}
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
}
