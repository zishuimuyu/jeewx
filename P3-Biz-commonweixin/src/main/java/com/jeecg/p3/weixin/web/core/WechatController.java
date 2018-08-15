package com.jeecg.p3.weixin.web.core;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.jeecg.p3.commonweixin.entity.MyJwWebJwid;
import com.jeecg.p3.system.service.MyJwWebJwidService;
import com.jeecg.p3.weixin.service.WechatService;
import com.jeecg.p3.weixin.util.SignUtil;

/**
 * 微信客户端，请求处理核心类
 * @author zhangdaihao
 *
 */
@Controller
@RequestMapping("/wechatController")
public class WechatController {
	@Autowired
	private WechatService wechatService;
	@Autowired
	private MyJwWebJwidService webJwidService;

	@RequestMapping(params="wechat", method = RequestMethod.GET)
	public void wechatGet(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value = "signature") String signature,
			@RequestParam(value = "timestamp") String timestamp,
			@RequestParam(value = "nonce") String nonce,
			@RequestParam(value = "echostr") String echostr) throws IOException {

		List<MyJwWebJwid> myJwWebJwids = webJwidService.queryAll();
		if(myJwWebJwids != null && myJwWebJwids.size() > 0) {
			for (MyJwWebJwid myJwWebJwid : myJwWebJwids) {
				if(SignUtil.checkSignature(myJwWebJwid.getToken(), signature, timestamp, nonce)) {
					try {
						response.getWriter().print(echostr);
						break;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	@RequestMapping(params = "wechat", method = RequestMethod.POST)
	public void wechatPost(HttpServletResponse response,
			HttpServletRequest request) throws IOException {
		String respMessage = wechatService.coreService(request);
		PrintWriter out = response.getWriter();
		out.print(respMessage);
		out.close();
	}

}
