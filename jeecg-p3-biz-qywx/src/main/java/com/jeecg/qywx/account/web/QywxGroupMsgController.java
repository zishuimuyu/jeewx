package com.jeecg.qywx.account.web;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.VelocityContext;
import org.jeecgframework.p3.core.common.utils.AjaxJson;
import org.jeecgframework.p3.core.logger.Logger;
import org.jeecgframework.p3.core.logger.LoggerFactory;
import org.jeecgframework.p3.core.util.plugin.ViewVelocity;
import org.jeecgframework.p3.core.web.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.jeecg.qywx.account.dao.QywxAccountDao;
import com.jeecg.qywx.account.dao.QywxAgentDao;
import com.jeecg.qywx.account.entity.QywxAgent;
import com.jeecg.qywx.account.service.AccountService;
import com.jeecg.qywx.api.core.common.AccessToken;
import com.jeecg.qywx.api.message.JwMessageAPI;
import com.jeecg.qywx.api.message.vo.News;
import com.jeecg.qywx.api.message.vo.NewsArticle;
import com.jeecg.qywx.api.message.vo.NewsEntity;
import com.jeecg.qywx.api.message.vo.Text;
import com.jeecg.qywx.api.message.vo.TextEntity;
import com.jeecg.qywx.base.dao.QywxGroupDao;
import com.jeecg.qywx.base.dao.QywxGzuserinfoDao;
import com.jeecg.qywx.base.dao.QywxMessagelogDao;
import com.jeecg.qywx.base.entity.QywxGroup;
import com.jeecg.qywx.base.entity.QywxGzuserinfo;
import com.jeecg.qywx.base.entity.QywxMessagelog;
import com.jeecg.qywx.sucai.dao.QywxNewsitemDao;
import com.jeecg.qywx.sucai.dao.QywxNewstemplateDao;
import com.jeecg.qywx.sucai.entity.QywxNewsitem;
import com.jeecg.qywx.sucai.entity.QywxNewstemplate;
import com.jeecg.qywx.util.ConfigUtil;


 /**
 * ???????????????????????????
 * @author p3.jeecg
 * @since???2016???03???28??? 13???37???49??? ????????? 
 * @version:1.0
 */
@Controller
@RequestMapping("/qywx/groupMsg")
public class QywxGroupMsgController extends BaseController{
  private static final Logger logger = LoggerFactory.getLogger(QywxGroupMsgController.class);
  @Autowired
  private QywxAgentDao qywxAgentDao;
  @Autowired
  private QywxNewstemplateDao qywxNewstemplateDao;
  @Autowired
  private QywxAccountDao qywxAccountDao;
  @Autowired 
   private AccountService accountService;
  @Autowired
  private QywxNewsitemDao qywxNewsitemDao;
  @Autowired
  private QywxGroupDao qywxGroupDao;
  @Autowired
  private QywxMessagelogDao qywxMessagelogDao;
  @Autowired
  private QywxGzuserinfoDao qywxGzuserinfoDao;
	/**
	 * ?????????????????????
	 * @return
	 */
	@RequestMapping(params="toGroupMsgSend",method ={RequestMethod.GET, RequestMethod.POST})
	public void toGroupMsgSend(@ModelAttribute QywxGroup group , HttpServletResponse response, HttpServletRequest request) throws Exception{
			 VelocityContext velocityContext = new VelocityContext();
			 List<QywxAgent> agentList= qywxAgentDao.getAllQywxAgents();
			 velocityContext.put("agentList", agentList);
			 //????????????
			 List<QywxGroup> list =qywxGroupDao.getAllQywxpid();
			 
			//???????????????????????????
			 String yuming=ConfigUtil.getProperty("domain");
			 velocityContext.put("yuming", yuming);
			 velocityContext.put("list", list);
			 String viewName = "qywx/msg/groupMsgSend.vm"; 
			 ViewVelocity.view(request,response,viewName,velocityContext);
	}
	
	//??? ??????????????????????????????
	@RequestMapping(params="getAllUploadNewsTemplate",method ={RequestMethod.GET, RequestMethod.POST})
	public void getAllUploadNewsTemplate(HttpServletResponse response,HttpServletRequest request) throws Exception{
		
		 VelocityContext velocityContext = new VelocityContext();
		 String viewName = "qywx/msg/showGroupMessageNews.vm";
		  String symbol = request.getParameter("symbol");
		  //??????????????????
		if("page".equals(symbol)){
		//??????????????????
			List<QywxNewstemplate> templateList = qywxNewstemplateDao.getAllQywxNewstemplate();
			//????????????
			//???????????????????????????????????????
			for(QywxNewstemplate  template : templateList){
				String templateId=template.getId();
				List <QywxNewsitem>	item=qywxNewsitemDao.getALLNews(templateId);
				template.setiNewsitem(item);
				            }
			    velocityContext.put("templateList", templateList);
			    
			           }	
		
		String yuming=ConfigUtil.getProperty("ftp_img_domain");
		velocityContext.put("yuming", yuming);
				try {
					ViewVelocity.view(request,response,viewName,velocityContext);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		
		}

	// ????????????
	@RequestMapping(params = "toGroupTextSend", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public AjaxJson toGroupTextSend(HttpServletResponse response, HttpServletRequest request) {
		AjaxJson j = new AjaxJson();
		try {
			JSONObject receive = null;
			//???????????????????????????
			String touserids="";
			//??????id
			String touserid = request.getParameter("touserId");
			String toallUser=request.getParameter("toModel");
			if("2".equals(toallUser)){
				touserids="@all";
			}else{
				touserids=touserid.replace(",", "|");
			}
			//??????id
			String toparty = request.getParameter("toparty");// ??????Id
			String topartys = toparty.replaceAll(",", "|");
			// ??????????????????????????????
			String toAgent = request.getParameter("toAgent");// ??????id
			Integer agenid = null;
			if (toAgent != null) {
				agenid = Integer.valueOf(toAgent);
			}
			String msgtype = request.getParameter("msgtype");// ??????
			String param = request.getParameter("param");// ??????????????????
			QywxMessagelog allmessage = new QywxMessagelog();
			if ("text".equals(msgtype)) {
				Text text = new Text();
				text.setMsgtype(msgtype);
				text.setAgentid(agenid);// ????????????id??????
				text.setToparty(topartys);// ??????id??????????????????
				text.setTouser(touserids);//??????????????????
				TextEntity textentity = new TextEntity();
				textentity.setContent(param);
				text.setText(textentity);
				//--update---author:scott-----date:20161217------------for:???????????????TOKERN??????????????????--
				//AccessToken accessToken = JwAccessTokenAPI.getAccessToken(JwParamesAPI.corpId, JwParamesAPI.secret);
				AccessToken accessToken = accountService.getAccessToken();
				//--update---author:scott-----date:20161217------------for:???????????????TOKERN??????????????????--
				receive = JwMessageAPI.sendTextMessage(text, accessToken.getAccesstoken());
				logger.info("message+sendTextMessage",receive );
				// update-begin--Author:malimei Date:2016525 for?????????????????????
				// ?????????????????????
				allmessage.setTopartysId(toparty);// ??????id?????????????????????
				allmessage.setWxAgentId(toAgent);// ??????id
				allmessage.setMessageType(msgtype);// ????????????
				allmessage.setMessageContent(param);// ?????????????????????
				allmessage.setReceiveMessage(receive.toJSONString());
				String randomSeed = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
				allmessage.setId(randomSeed);
				allmessage.setCreateDate(new Date());
				qywxMessagelogDao.insert(allmessage);
				// update-end--Author:malimei Date:2016525 for?????????????????????
			}
			// ?????????????????????
			String templateId = request.getParameter("templateId");
			if ("news".equals(msgtype)) {
				// ??????????????????
				List<QywxNewsitem> item = qywxNewsitemDao.getALLNews(templateId);
				News news = new News();
				news.setToparty(topartys);// ??????Id
				news.setTouser(touserids);//????????????id??????
				news.setMsgtype(msgtype);// ????????????
				news.setAgentid(agenid);// ????????????id??????

				List<NewsArticle> ls = new ArrayList<NewsArticle>();
				for (int i = 0; i < item.size(); i++) {
					String picurl = item.get(i).getImagePath();
					String title = item.get(i).getTitle();
					String description = item.get(i).getDescription();
					String url = item.get(i).getUrl();
					NewsArticle newsarticle = new NewsArticle();
					newsarticle.setDescription(description);
					// ??????????????????????????????
					String domain = ConfigUtil.getProperty("domain");
					newsarticle.setPicurl(domain + "/" + picurl);
					newsarticle.setTitle(title);
					newsarticle.setUrl(domain + "/qywx/qywxNewsitem.do?goContent&id=" + item.get(i).getId());
					ls.add(newsarticle);
				}
				NewsEntity newsEntity = new NewsEntity();
				newsEntity.setArticles(ls.toArray(new NewsArticle[ls.size()]));
				news.setNews(newsEntity);
				//--update---author:scott-----date:20161217------------for:???????????????TOKERN??????????????????--
				//AccessToken accessToken = JwAccessTokenAPI.getAccessToken(JwParamesAPI.corpId, JwParamesAPI.secret);// ??????token???
				AccessToken accessToken = accountService.getAccessToken();
				//--update---author:scott-----date:20161217------------for:???????????????TOKERN??????????????????--
				
				receive = JwMessageAPI.sendNewsMessage(news, accessToken.getAccesstoken());
				 logger.info("message+sendTextMessage",receive );
				 // update-begin--Author:malimei Date:2016525 for?????????????????????
				allmessage.setTopartysId(toparty);// ??????id?????????????????????
				allmessage.setWxAgentId(toAgent);// ??????id
				allmessage.setMessageType(msgtype);// ????????????
				allmessage.setContentId(templateId);// ?????????????????????id
				allmessage.setReceiveMessage(receive.toJSONString());
				allmessage.setCreateDate(new Date());
				String randomSeed = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
				allmessage.setId(randomSeed);
				qywxMessagelogDao.insert(allmessage);// ????????????
				// update-begin--Author:malimei Date:2016525 for?????????????????????
			}
			String code = receive.getString("errcode");
			if("0".equals(code)){
				j.setSuccess(true);
				j.setObj("sucess");
				j.setMsg("????????????");
			}
		} catch (Exception e) {
			log.error(e.toString());
			j.setSuccess(false);
			j.setMsg("????????????");
		}
		return j;

	}
	//???????????????????????????????????????
	@RequestMapping(params="toGroupNewsSend",method ={RequestMethod.GET, RequestMethod.POST})
	@ResponseBody
	public AjaxJson toGroupNewsSend(HttpServletResponse response,HttpServletRequest request ){
		AjaxJson j = new AjaxJson();
		try{
			String templateId=request.getParameter("templateId");
			//??????templateId??????item?????????
			List<QywxNewsitem> item=  qywxNewsitemDao.getALLNews(templateId);
			j.setObj(item);
			
	} catch (Exception e) {
	    log.info(e.getMessage());
		j.setSuccess(false);
		j.setMsg("????????????!");
	}
		return j;
	
	}
	
	@RequestMapping(params="getAuthTree",method ={RequestMethod.GET, RequestMethod.POST})
	@ResponseBody
	public AjaxJson getAuthTree(HttpServletResponse response,HttpServletRequest request ){
		AjaxJson j = new AjaxJson();
		try{
			  List<QywxGroup> allAuthList =qywxGroupDao.getAllQywxpid();
			  List<Map> list=new ArrayList<Map>();
			for (QywxGroup  authList:allAuthList) {
				String id=authList.getId();
				String parentid = authList.getParentid();
				String name = authList.getName();
				String open="true";
				Map<String, String> map = new HashMap<String, String>();
				map.put("id", id);
				map.put("pId", parentid);
				map.put("name", name);
				map.put("open",open );
				 list.add(map);
			}
			j.setObj(list);
			
	} catch (Exception e) {
	    log.info(e.getMessage());
		j.setSuccess(false);
		j.setMsg("????????????!");
	}
		return j;
	
	}
	
	//???????????????????????????
	@RequestMapping(params="getUserList",method ={RequestMethod.GET, RequestMethod.POST})
	public void getUserList(HttpServletResponse response,HttpServletRequest request) throws Exception{
		 VelocityContext velocityContext = new VelocityContext();
		 String viewName="qywx/msg/userMessage.vm";
		 ViewVelocity.view(request,response,viewName,velocityContext);
		 
	}
	//????????????????????????getUserResult
	@RequestMapping(params="getUserResult",method ={RequestMethod.GET, RequestMethod.POST})
	@ResponseBody
	public AjaxJson getUserResult(HttpServletResponse response, HttpServletRequest request) {
		AjaxJson j = new AjaxJson();
		String parameter = request.getParameter("pid");
		String[] namelist = parameter.split(",");
		List<List<QywxGzuserinfo>>list=new ArrayList<List<QywxGzuserinfo>>();
		for (int i = 0; i < namelist.length; i++) {
			List<QywxGzuserinfo> byDepartment = qywxGzuserinfoDao.getByDepartment(namelist[i]);
			 if(byDepartment!=null){
			    list.add(byDepartment);
			 }
		}
		j.setObj(list);
		return j;
	}
	
	@RequestMapping(params = "getUserTempletNew", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public AjaxJson getUserTempletNew(HttpServletResponse response, HttpServletRequest request) throws Exception {
		AjaxJson j = new AjaxJson();
	
			LOG.info(request, " back list");
			String parameter = request.getParameter("pid");//????????????id?????????id
 		    String[] namelist = parameter.split(",");
			VelocityContext velocityContext = new VelocityContext();
			QywxGzuserinfo query=new QywxGzuserinfo();
			if( parameter==null||"".equals(parameter)){//????????????????????????????????????????????????????????????????????????,????????????0??????????????????
				List<QywxGzuserinfo> allUser = qywxGzuserinfoDao.getAllUser();
				velocityContext.put("pageInfos", allUser);
			}
			//???????????????
		for (int i = 0; i < namelist.length; i++) {// ??????????????????
			QywxGroup qywxGroup = qywxGroupDao.get(namelist[i]);
			String parentid = "";
			if (qywxGroup != null) {
				parentid = qywxGroup.getParentid();
			}
			if ("0".equals(parentid)) {// ????????????0?????????
				List<QywxGzuserinfo> allUser = qywxGzuserinfoDao.getAllUser();
				velocityContext.put("pageInfos", allUser);
			} else {// ???????????????0?????????
				List<QywxGzuserinfo> byDepartment = qywxGzuserinfoDao.getdepartments(parameter);
				velocityContext.put("pageInfos", byDepartment);
			}
		}
		velocityContext.put("qywxGzuserinfo", query);
		String viewName = "qywx/msg/usertemplet.vm";
		j.setObj(ViewVelocity.getViewContent(request, response, viewName, velocityContext));
		return j;
	}
}


