package weixin.guanjia.core.service.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

import org.jeecgframework.core.util.LogUtil;
import org.jeecgframework.core.util.oConvertUtils;
import org.jeecgframework.web.cgform.engine.FreemarkerHelper;
import org.jeecgframework.web.system.service.SystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import weixin.cms.dao.CmsAdDao;
import weixin.guanjia.account.service.WeixinAccountServiceI;
import weixin.guanjia.base.entity.Subscribe;
import weixin.guanjia.base.entity.WeixinExpandconfigEntity;
import weixin.guanjia.base.service.SubscribeServiceI;
import weixin.guanjia.base.service.WeixinExpandconfigServiceI;
import weixin.guanjia.core.entity.message.resp.Article;
import weixin.guanjia.core.entity.message.resp.NewsMessageResp;
import weixin.guanjia.core.entity.message.resp.TextMessageResp;
import weixin.guanjia.core.util.MessageUtil;
import weixin.guanjia.menu.entity.MenuEntity;
import weixin.guanjia.message.dao.TextTemplateDao;
import weixin.guanjia.message.entity.AutoResponse;
import weixin.guanjia.message.entity.NewsItem;
import weixin.guanjia.message.entity.NewsTemplate;
import weixin.guanjia.message.entity.ReceiveText;
import weixin.guanjia.message.entity.TextTemplate;
import weixin.guanjia.message.service.AutoResponseServiceI;
import weixin.guanjia.message.service.NewsItemServiceI;
import weixin.guanjia.message.service.NewsTemplateServiceI;
import weixin.guanjia.message.service.ReceiveTextServiceI;
import weixin.guanjia.message.service.TextTemplateServiceI;
import weixin.idea.extend.function.KeyServiceI;
import weixin.util.DateUtils;

@Service("wechatService")
public class WechatService {
	@Autowired
	private TextTemplateDao textTemplateDao;
	@Autowired
	private AutoResponseServiceI autoResponseService;
	@Autowired
	private TextTemplateServiceI textTemplateService;
	@Autowired
	private NewsTemplateServiceI newsTemplateService;
	@Autowired
	private ReceiveTextServiceI receiveTextService;
	@Autowired
	private NewsItemServiceI newsItemService;
	@Autowired
	private SystemService systemService;
	@Autowired
	private SubscribeServiceI subscribeService;
	@Autowired
	private WeixinExpandconfigServiceI weixinExpandconfigService;
	@Autowired
	private WeixinAccountServiceI weixinAccountService;

	public String coreService(HttpServletRequest request) {
		String respMessage = null;
		try {
			// ?????????????????????????????????
			String respContent = "???????????????????????????????????????";
			// xml????????????
			Map<String, String> requestMap = MessageUtil.parseXml(request);
			// ??????????????????open_id???
			String fromUserName = requestMap.get("FromUserName");
			// ????????????
			String toUserName = requestMap.get("ToUserName");
			// ????????????
			String msgType = requestMap.get("MsgType");
			String msgId = requestMap.get("MsgId");
			//????????????
			String content = requestMap.get("Content");
			LogUtil.info("------------???????????????????????????---------------------   |   fromUserName:"+fromUserName+"   |   ToUserName:"+toUserName+"   |   msgType:"+msgType+"   |   msgId:"+msgId+"   |   content:"+content);
			//????????????ID,????????????????????????????????????ID
			LogUtil.info("-toUserName--------"+toUserName);
			String sys_accountId = weixinAccountService.findByToUsername(toUserName).getId();
			LogUtil.info("-sys_accountId--------"+sys_accountId);
			ResourceBundle bundler = ResourceBundle.getBundle("sysConfig");
			// ???????????????????????????
			TextMessageResp textMessage = new TextMessageResp();
			textMessage.setToUserName(fromUserName);
			textMessage.setFromUserName(toUserName);
			textMessage.setCreateTime(new Date().getTime());
			textMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_TEXT);
			textMessage.setContent(getMainMenu());
			// ??????????????????????????????xml?????????
			respMessage = MessageUtil.textMessageToXml(textMessage);
			//????????????????????????????????????
			if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_TEXT)) {
				LogUtil.info("------------???????????????????????????------------------????????????????????????????????????---");
				respMessage = doTextResponse(content,toUserName,textMessage,bundler,
						sys_accountId,respMessage,fromUserName,request,msgId,msgType);
			}
			//????????????????????????????????????
			else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_IMAGE)) {
				respContent = "??????????????????????????????";
			}
			//??????????????????????????????????????????
			else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_LOCATION)) {
				respContent = "????????????????????????????????????";
			}
			//????????????????????????????????????
			else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_LINK)) {
				respContent = "??????????????????????????????";
			}
			//????????????????????????????????????
			else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_VOICE)) {
				respContent = "??????????????????????????????";
			}
			//????????????????????????????????????
			else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_EVENT)) {
				LogUtil.info("------------???????????????????????????------------------????????????????????????????????????---");
				// ????????????
				String eventType = requestMap.get("Event");
				// ??????
				if (eventType.equals(MessageUtil.EVENT_TYPE_SUBSCRIBE)) {
					respMessage = doDingYueEventResponse(requestMap, textMessage, bundler, respMessage, toUserName, fromUserName, respContent, sys_accountId);
				}
				// ????????????
				else if (eventType.equals(MessageUtil.EVENT_TYPE_UNSUBSCRIBE)) {
					// TODO ???????????????????????????????????????????????????????????????????????????????????????
				}
				// ???????????????????????????
				else if (eventType.equals(MessageUtil.EVENT_TYPE_CLICK)) {
					respMessage = doMyMenuEvent(requestMap, textMessage, bundler, respMessage, toUserName, fromUserName, respContent, sys_accountId, request);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return respMessage;
	}


	/**
	 * Q??????????????????
	 * 
	 * @return
	 */
	public static String getTranslateUsage() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("??????????????????").append("\n\n");
		buffer.append("???????????????????????????????????????????????????????????????????????????????????????").append("\n");
		buffer.append("    ??? -> ???").append("\n");
		buffer.append("    ??? -> ???").append("\n");
		buffer.append("    ??? -> ???").append("\n\n");
		buffer.append("???????????????").append("\n");
		buffer.append("    ?????????????????????").append("\n");
		buffer.append("    ??????dream").append("\n");
		buffer.append("    ?????????????????????").append("\n\n");
		buffer.append("????????????????????????????");
		return buffer.toString();
	}

	/**
	 * ??????????????????????????????????????????????????????????????????
	 * 
	 * @param content
	 * @return
	 */
	private AutoResponse findKey(String content, String toUsername) {
		LogUtil.info("---------sys_accountId--------"+toUsername+"|");
		//???????????????????????????ID
		String sys_accountId = weixinAccountService.findByToUsername(toUsername).getId();
		LogUtil.info("---------sys_accountId--------"+sys_accountId);
		// ??????????????????????????????????????????????????????
		List<AutoResponse> autoResponses = autoResponseService.findByProperty(AutoResponse.class, "accountId", sys_accountId);
		LogUtil.info("---------sys_accountId----??????????????????????????????----"+autoResponses!=null?autoResponses.size():0);
		for (AutoResponse r : autoResponses) {
			// ?????????????????????
			String kw = r.getKeyWord();
			String[] allkw = kw.split(",");
			for (String k : allkw) {
				if (k.equals(content)) {
					LogUtil.info("---------sys_accountId----????????????----"+r);
					return r;
				}
			}
		}
		return null;
	}

	/**
	 * ??????????????????
	 * @param content
	 * @param toUserName
	 * @param textMessage
	 * @param bundler
	 * @param sys_accountId
	 * @param respMessage
	 * @param fromUserName
	 * @param request
	 * @throws Exception 
	 */
	String doTextResponse(String content,String toUserName,TextMessageResp textMessage,ResourceBundle bundler,
			String sys_accountId,String respMessage,String fromUserName,HttpServletRequest request,String msgId,String msgType) throws Exception{
		//=================================================================================================================
		// ????????????????????????
		ReceiveText receiveText = new ReceiveText();
		receiveText.setContent(content);
		Timestamp temp = Timestamp.valueOf(DateUtils
				.getDate("yyyy-MM-dd HH:mm:ss"));
		receiveText.setCreateTime(temp);
		receiveText.setFromUserName(fromUserName);
		receiveText.setToUserName(toUserName);
		receiveText.setMsgId(msgId);
		receiveText.setMsgType(msgType);
		receiveText.setResponse("0");
		receiveText.setAccountId(toUserName);
		this.receiveTextService.save(receiveText);
		//=================================================================================================================
		//Step.1 ????????????????????????????????????????????????????????????????????????????????????????????????
		LogUtil.info("------------???????????????????????????--------------Step.1 ????????????????????????????????????????????????????????????????????????????????????????????????---");
		AutoResponse autoResponse = findKey(content, toUserName);
		// ????????????????????????????????????????????????????????????
		if (autoResponse != null) {
			String resMsgType = autoResponse.getMsgType();
			if (MessageUtil.REQ_MESSAGE_TYPE_TEXT.equals(resMsgType)) {
				//??????????????????key??????????????????????????????????????????????????????
				TextTemplate textTemplate = textTemplateDao.getTextTemplate(sys_accountId, autoResponse.getTemplateName());
				textMessage.setContent(textTemplate.getContent());
				respMessage = MessageUtil.textMessageToXml(textMessage);
			} else if (MessageUtil.RESP_MESSAGE_TYPE_NEWS.equals(resMsgType)) {
				List<NewsItem> newsList = this.newsItemService.findByProperty(NewsItem.class,"newsTemplate.id", autoResponse.getResContent());
				NewsTemplate newsTemplate = newsTemplateService.getEntity(NewsTemplate.class, autoResponse.getResContent());
				List<Article> articleList = new ArrayList<Article>();
				for (NewsItem news : newsList) {
					Article article = new Article();
					article.setTitle(news.getTitle());
					article.setPicUrl(bundler.getString("domain") + "/"+ news.getImagePath());
					String url = "";
					if (oConvertUtils.isEmpty(news.getUrl())) {
						url = bundler.getString("domain")+ "/newsItemController.do?goContent&id="+ news.getId();
					} else {
						url = news.getUrl();
					}
					article.setUrl(url);
					article.setDescription(news.getDescription());
					articleList.add(article);
				}
				NewsMessageResp newsResp = new NewsMessageResp();
				newsResp.setCreateTime(new Date().getTime());
				newsResp.setFromUserName(toUserName);
				newsResp.setToUserName(fromUserName);
				newsResp.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_NEWS);
				newsResp.setArticleCount(newsList.size());
				newsResp.setArticles(articleList);
				respMessage = MessageUtil.newsMessageToXml(newsResp);
			}
		} else {
			// Step.2  ???????????????????????????????????????????????????????????????????????????
			LogUtil.info("------------???????????????????????????--------------Step.2  ???????????????????????????????????????????????????????????????????????????---");
			List<WeixinExpandconfigEntity> weixinExpandconfigEntityLst = weixinExpandconfigService.findByQueryString("FROM WeixinExpandconfigEntity");
			if (weixinExpandconfigEntityLst.size() != 0) {
				for (WeixinExpandconfigEntity wec : weixinExpandconfigEntityLst) {
					boolean findflag = false;// ???????????????????????????
					// ????????????????????????????????????????????????????????????
					if (findflag) {
						break;// ????????????????????????
					}
					String[] keys = wec.getKeyword().split(",");
					for (String k : keys) {
						if (content.indexOf(k) != -1) {
							String className = wec.getClassname();
							KeyServiceI keyService = (KeyServiceI) Class.forName(className).newInstance();
							respMessage = keyService.excute(content,textMessage, request);
							findflag = true;// ?????????????????????????????????????????????????????????????????????
							break;// ??????????????????????????????????????????????????????
						}
					}
				}
			}

		}
		return respMessage;
	}
	
	/**
	 * ??????????????????
	 * @param requestMap
	 * @param textMessage
	 * @param bundler
	 * @param respMessage
	 * @param toUserName
	 * @param fromUserName
	 */
	String doDingYueEventResponse(Map<String, String> requestMap,TextMessageResp textMessage ,ResourceBundle bundler,String respMessage
			,String toUserName,String fromUserName,String respContent,String sys_accountId){
		respContent = "???????????????????????????\"?\"??????????????????";
		List<Subscribe> lst = subscribeService.findByProperty(Subscribe.class, "accountid", sys_accountId);
		if (lst.size() != 0) {
			Subscribe subscribe = lst.get(0);
			String type = subscribe.getMsgType();
			if (MessageUtil.REQ_MESSAGE_TYPE_TEXT.equals(type)) {
				TextTemplate textTemplate = this.textTemplateService
						.getEntity(TextTemplate.class, subscribe
								.getTemplateId());
				String content = textTemplate.getContent();
				textMessage.setContent(content);
				respMessage = MessageUtil.textMessageToXml(textMessage);
			} else if (MessageUtil.RESP_MESSAGE_TYPE_NEWS.equals(type)) {
				List<NewsItem> newsList = this.newsItemService.findByProperty(NewsItem.class,"newsTemplate.id", subscribe.getTemplateId());
				List<Article> articleList = new ArrayList<Article>();
				NewsTemplate newsTemplate = newsTemplateService.getEntity(NewsTemplate.class, subscribe.getTemplateId());
				for (NewsItem news : newsList) {
					Article article = new Article();
					article.setTitle(news.getTitle());
					article.setPicUrl(bundler.getString("domain")+ "/" + news.getImagePath());
					String url = "";
					if (oConvertUtils.isEmpty(news.getUrl())) {
						url = bundler.getString("domain")+ "/newsItemController.do?goContent&id="+ news.getId();
					} else {
						url = news.getUrl();
					}
					article.setUrl(url);
					article.setDescription(news.getDescription());
					articleList.add(article);
				}
				NewsMessageResp newsResp = new NewsMessageResp();
				newsResp.setCreateTime(new Date().getTime());
				newsResp.setFromUserName(toUserName);
				newsResp.setToUserName(fromUserName);
				newsResp.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_NEWS);
				newsResp.setArticleCount(newsList.size());
				newsResp.setArticles(articleList);
				respMessage = MessageUtil.newsMessageToXml(newsResp);
			}
		}
		return respMessage;
	}
	
	/**
	 * 
	 * @param requestMap
	 * @param textMessage
	 * @param bundler
	 * @param respMessage
	 * @param toUserName
	 * @param fromUserName
	 * @param respContent
	 * @param sys_accountId
	 * @param request
	 * @return
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	String doMyMenuEvent(Map<String, String> requestMap,TextMessageResp textMessage ,ResourceBundle bundler,String respMessage
			,String toUserName,String fromUserName,String respContent,String sys_accountId,HttpServletRequest request) throws Exception{
		String key = requestMap.get("EventKey");
		//???????????????CLICK??????
		MenuEntity menuEntity = this.systemService.findUniqueByProperty(MenuEntity.class, "menuKey",key);
		if (menuEntity != null&& oConvertUtils.isNotEmpty(menuEntity.getTemplateId())) {
			String type = menuEntity.getMsgType();
			if (MessageUtil.REQ_MESSAGE_TYPE_TEXT.equals(type)) {
				TextTemplate textTemplate = this.textTemplateService.getEntity(TextTemplate.class, menuEntity.getTemplateId());
				String content = textTemplate.getContent();
				textMessage.setContent(content);
				respMessage = MessageUtil.textMessageToXml(textMessage);
			} else if (MessageUtil.RESP_MESSAGE_TYPE_NEWS.equals(type)) {
				List<NewsItem> newsList = this.newsItemService.findByProperty(NewsItem.class,"newsTemplate.id", menuEntity.getTemplateId());
				List<Article> articleList = new ArrayList<Article>();
				NewsTemplate newsTemplate = newsTemplateService.getEntity(NewsTemplate.class, menuEntity.getTemplateId());
				for (NewsItem news : newsList) {
					Article article = new Article();
					article.setTitle(news.getTitle());
					article.setPicUrl(bundler.getString("domain")+ "/" + news.getImagePath());
					String url = "";
					if (oConvertUtils.isEmpty(news.getUrl())) {
						url = bundler.getString("domain")+ "/newsItemController.do?goContent&id="+ news.getId();
					} else {
						url = news.getUrl();
					}
					article.setUrl(url);
					article.setDescription(news.getDescription());
					articleList.add(article);
				}
				NewsMessageResp newsResp = new NewsMessageResp();
				newsResp.setCreateTime(new Date().getTime());
				newsResp.setFromUserName(toUserName);
				newsResp.setToUserName(fromUserName);
				newsResp.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_NEWS);
				newsResp.setArticleCount(newsList.size());
				newsResp.setArticles(articleList);
				respMessage = MessageUtil
						.newsMessageToXml(newsResp);
			} else if ("expand".equals(type)) {
				WeixinExpandconfigEntity expandconfigEntity = weixinExpandconfigService.getEntity(WeixinExpandconfigEntity.class,menuEntity.getTemplateId());
				String className = expandconfigEntity.getClassname();
				KeyServiceI keyService = (KeyServiceI) Class.forName(className).newInstance();
				respMessage = keyService.excute("", textMessage,request);

			}
		}
		return respMessage;
	}
	
	/**
	 * ?????????
	 * @return
	 */
	public static String getMainMenu() {
		// ??????????????????????????????????????????????????????
		String html = new FreemarkerHelper().parseTemplate("/weixin/welcome.ftl", null);
		return html;
	}
}
