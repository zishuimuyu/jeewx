package org.jeecgframework.web.cgform.controller.build;

import java.beans.PropertyDescriptor;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jeecgframework.web.cgform.dao.config.CgFormVersionDao;
import org.jeecgframework.web.cgform.entity.config.CgFormHeadEntity;
import org.jeecgframework.web.cgform.pojo.config.CgFormHeadPojo;
import org.jeecgframework.web.cgform.service.migrate.MigrateForm;

import org.apache.log4j.Logger;
import org.jeecgframework.core.common.controller.BaseController;
import org.jeecgframework.core.common.model.common.DBTable;
import org.jeecgframework.core.common.model.common.UploadFile;
import org.jeecgframework.core.common.model.json.AjaxJson;
import org.jeecgframework.core.util.LogUtil;
import org.jeecgframework.core.util.ReflectHelper;
import org.jeecgframework.core.util.ResourceUtil;
import org.jeecgframework.core.util.StringUtil;
import org.jeecgframework.core.util.oConvertUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.thoughtworks.xstream.XStream;

/**
 * @Title: Controller
 * @Description: sql????????????
 * @author duanqilu
 * @date 2013-09-04
 * @version V1.0
 * 
 */
@Scope("prototype")
@Controller
@RequestMapping("/cgformSqlController")
public class CgformSqlController extends BaseController {
	/**
	 * Logger for this class
	 */
	@SuppressWarnings("unused")
	private static final Logger logger = Logger
			.getLogger(CgformSqlController.class);
	@Autowired
	private CgFormVersionDao cgFormVersionDao;

	@Autowired
	@Qualifier("jdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	@Autowired
	@Qualifier("namedParameterJdbcTemplate")
	private NamedParameterJdbcTemplate namedJdbcTemplate;

	private String message;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * ?????? Form(??????SQL??????)
	 * 
	 * @return
	 * @throws SQLException
	 */
	@RequestMapping(params = "doMigrateOut")
	public void doMigrateOut(HttpServletRequest request,HttpServletResponse response){
		String ids = request.getParameter("ids"); // ??????????????????ID
		//List<String> listSQL = new ArrayList<String>();
		try {
			//listSQL = MigrateForm.createSQL(ids, jdbcTemplate);// ??????????????????
			//MigrateForm.executeSQL(listSQL, jdbcTemplate);// ??????sql?????????
			List<DBTable> dbTables = MigrateForm.buildExportDbTableList(ids, jdbcTemplate);  //???????????????????????????
			//MigrateForm.generateXmlDataOutFlieContent(ids, jdbcDao);
			//update by duanqilu 2013-12-05 ???????????????????????????
			String ls_id="";
			if(ids.indexOf(",")>0){
				ls_id=ids.substring(0, ids.indexOf(","));
			}
			else{
				ls_id = ids;
			}
			CgFormHeadEntity cgFormHeadEntity = cgFormVersionDao.getCgFormById(ls_id);
			//update by duanqilu 2013-12-05 ???????????????????????????
			//MigrateForm.createFile(request,cgFormHeadEntity.getTableName())
			String ls_filename = cgFormHeadEntity.getTableName();// ????????????
			String destFileDir = ResourceUtil.getSystempPath()+"/"+ls_filename;
			MigrateForm.generateXmlDataOutFlieContent(dbTables, destFileDir);
			ls_filename = MigrateForm.zip(null, "", destFileDir); // ????????????
			// ????????????
			File file = new File(ls_filename);
			String filename = file.getName();
			InputStream fis = new BufferedInputStream(new FileInputStream(ls_filename));
			// ???????????????zip??????
			// ??????response
			response.reset();
			// ??????response???Header
			response.setContentType("text/html;charset=utf-8");
			request.setCharacterEncoding("UTF-8");
			response.addHeader("Content-Length", "" + file.length());
			OutputStream toClient = new BufferedOutputStream(response.getOutputStream());
			response.setContentType("application/octet-stream");
			response.setHeader("Content-disposition", "attachment;filename="
					+ new String(filename.getBytes("utf-8"), "ISO8859-1"));
			int bytesRead = 0;
			byte[] buffer = new byte[8192];
			while ((bytesRead = fis.read(buffer, 0, 8192)) != -1) {
				toClient.write(buffer, 0, bytesRead);
			}
			toClient.write(buffer);
			toClient.flush();
			toClient.close();
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * ??????sql????????????
	 * 
	 * @param icon
	 * @param req
	 * @return
	 */
	@RequestMapping(params = "toCgformMigrate")
	public ModelAndView toCgformMigrate(HttpServletRequest req) {
		return new ModelAndView("jeecg/cgform/config/cgformMigrateSqlInclude");
	}

	/**
	 * ??????Form(??????SQL??????)
	 * 
	 * @param ids
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(params = "doMigrateIn", method = RequestMethod.POST)
	@ResponseBody
	public AjaxJson doMigrateIn(HttpServletRequest request,
			HttpServletResponse response) {
		AjaxJson j = new AjaxJson();
		String ls_file = "";
		UploadFile uploadFile = new UploadFile(request, ls_file);
		uploadFile.setCusPath("");
		uploadFile.setSwfpath("");
		String uploadbasepath = uploadFile.getBasePath();// ?????????????????????
		if (uploadbasepath == null) {
			uploadbasepath = ResourceUtil.getConfigByName("uploadpath");
		}
		String path = uploadbasepath + "\\";// ????????????????????????????????????
		String realPath = uploadFile.getMultipartRequest().getSession()
				.getServletContext().getRealPath("\\")
				+ path;// ???????????????????????????
		message = null;
		try {
			File file = new File(realPath);
			if (!file.exists()) {
				file.mkdir();// ????????????????????????????????????
			}
			uploadFile.getMultipartRequest().setCharacterEncoding("UTF-8");
			MultipartHttpServletRequest multipartRequest = uploadFile
					.getMultipartRequest();
			Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
			String fileName = "";
			for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
				MultipartFile mf = entity.getValue();// ????????????????????????
				fileName = mf.getOriginalFilename();// ???????????????
				String savePath = realPath + fileName;
				File savefile = new File(savePath);
				String ls_tmp = savefile.getName();
				/*String sqlfilename = realPath
						+ ls_tmp.substring(0, ls_tmp.lastIndexOf(".")) + "\\"
						+ ls_tmp.substring(0, ls_tmp.lastIndexOf(".")) + ".sql";*/
				FileCopyUtils.copy(mf.getBytes(), savefile);
				MigrateForm.unzip(savePath, "");
				String sqlFileDir = realPath + ls_tmp.substring(0, ls_tmp.lastIndexOf("."));
				File sqlDirFile = new File(sqlFileDir);
				String sqlfilename = sqlDirFile.getPath() + "/";
				if(sqlDirFile.isDirectory()){
					sqlfilename += sqlDirFile.list()[0];
				}
				/*OfficeHtmlUtil officeHtml = new OfficeHtmlUtil();
				String sqlStr = officeHtml.getInfo(sqlfilename);
				String[] sqlStrs = sqlStr.split(";");
				for (String exesql : sqlStrs) {
					if (!StringUtil.isEmpty(exesql) && !"\n".equals(exesql)) {
						jdbcTemplate.execute(exesql);
					}
				}*/
				XStream xStream = new XStream();
				xStream.processAnnotations(DBTable.class);
				@SuppressWarnings("unchecked")
				List<DBTable> dbTables = (List<DBTable>) xStream.fromXML(new File(sqlfilename));
				if(!dbTables.isEmpty() && dbTables.size()>0){
					for (DBTable dbTable : dbTables) {
						mergeMigrateInComponent(dbTable);
					}
				}
			}
		} catch (Exception e1) {
			e1.printStackTrace();
			LogUtil.error(e1.toString());
			message = e1.toString();
		}
		if (StringUtil.isNotEmpty(message))
			j.setMsg("SQL??????????????????," + message);
		else
			j.setMsg("SQL??????????????????");

		return j;
	}

	private <T> void mergeMigrateInComponent(DBTable<T> dbTable) {
		Class<T> clazz = dbTable.getClass1();
		if(null != clazz){
			List<T> dataList = dbTable.getTableData();
			if(null == dataList || dataList.size() < 1) return;
			Map<String, String> idMap = new HashMap<String, String>();
			String id = "";
			String countSql = "";
			SqlParameterSource sqlParameterSource;
			List<Map<String, Object>> idList = new ArrayList<Map<String,Object>>();
			for (T t : dataList) {
				ReflectHelper reflectHelper = new ReflectHelper(t);
				List<String> ignores = new ArrayList<String>();
				ignores.add("class");
				PropertyDescriptor[] pds = BeanUtils.getPropertyDescriptors(clazz);
				for (PropertyDescriptor pd : pds) {
					if(null == reflectHelper.getMethodValue(pd.getName())){
						ignores.add(pd.getName());
					}
				}
				if(t instanceof CgFormHeadPojo){
					reflectHelper.setMethodValue("isDbsynch", "N");
				}
				id = (String) reflectHelper.getMethodValue("id");
				countSql = "select id from "+ dbTable.getTableName() + " where id=?";
				if(t instanceof CgFormHeadPojo){
					countSql = "select id from "+ dbTable.getTableName() + " where table_name=?";
					id = oConvertUtils.getString(reflectHelper.getMethodValue("tableName"));
				}
				idList = jdbcTemplate.queryForList(countSql, id);
				sqlParameterSource = MigrateForm.generateParameterMap(t, ignores);
				if(idList.size() > 0 && t instanceof CgFormHeadPojo){
					idMap.put("id", oConvertUtils.getString(idList.get(0).get("id")));
					namedJdbcTemplate.update("delete from cgform_field where table_id=:id", idMap);
					namedJdbcTemplate.update("delete from cgform_head where id=:id", idMap);
					namedJdbcTemplate.update(MigrateForm.generateInsertSql(dbTable.getTableName(), clazz, ignores), sqlParameterSource);
				} else if(idList.size() > 0){
					namedJdbcTemplate.update(MigrateForm.generateUpdateSql(dbTable.getTableName(), clazz, ignores), sqlParameterSource);
				} else {
					namedJdbcTemplate.update(MigrateForm.generateInsertSql(dbTable.getTableName(), clazz, ignores), sqlParameterSource);
				}
			}
		}
	}
}
