package com.jeecg.p3.weixin.web.back;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.jeecgframework.p3.core.common.utils.AjaxJson;
import org.jeecgframework.p3.core.util.WeiXinHttpUtil;
import org.jeecgframework.p3.core.util.oConvertUtils;
import org.jeewx.api.core.exception.WexinReqException;
import org.jeewx.api.core.req.model.menu.WeixinButton;
import org.jeewx.api.wxmenu.JwMenuAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jeecg.p3.weixin.entity.WeixinMenu;
import com.jeecg.p3.weixin.service.WeixinMenuService;

@Controller
@RequestMapping("/menuManagerController")
public class MenuManagerController {
	
	@Autowired
	private WeixinMenuService weixinMenuService;

	@RequestMapping(value="/doSyncMenu",method={RequestMethod.GET, RequestMethod.POST})
	@ResponseBody
	public AjaxJson doSyncMenu(HttpServletRequest request){
		AjaxJson j=new AjaxJson();
		//获取jwid
		String jwid =  request.getSession().getAttribute("jwid").toString();
		//根据jwid获取一级菜单
		WeixinMenu queryFristMenu=new WeixinMenu();
		queryFristMenu.setFatherId("");
		queryFristMenu.setJwid(jwid);
		List<WeixinMenu> firstMenus=weixinMenuService.queryMenusByJwid(queryFristMenu);
		//获取token方法替换
		String accessToken =WeiXinHttpUtil.getRedisWeixinToken(jwid);
		if(oConvertUtils.isEmpty(accessToken)){
			j.setSuccess(false);
			j.setMsg("未获取到公众号accessToken");
		}
		//判断如果菜单为空的话，则调用删除菜单的接口
		if(firstMenus.size()==0){
			try {
				JwMenuAPI.deleteMenu("");
				j.setSuccess(true);
				j.setMsg("同步微信菜单成功！");
				return j;
			} catch (WexinReqException e) {
				e.printStackTrace();
				j.setSuccess(false);
				j.setMsg("同步微信菜单失败！");
			}
		}
		//获取二级菜单
		List<WeixinButton> resultList=new ArrayList<WeixinButton>();
		for(int i=0;i<firstMenus.size();i++){
			WeixinMenu queryChildMenu=new WeixinMenu();
			queryChildMenu.setJwid(jwid);
			queryChildMenu.setFatherId(firstMenus.get(i).getId());
			List<WeixinMenu> childMenus=weixinMenuService.queryMenusByJwid(queryChildMenu);
			if(childMenus.size()==0){
				//组装菜单接口的参数结构体
				resultList.add(combineBtn(firstMenus.get(i)));
			}else{
				//组装一级菜单名称
				WeixinButton wxButton = new WeixinButton();
				wxButton.setName(firstMenus.get(i).getMenuName());
				//组装二级菜单接口的参数结构体
				List<WeixinButton> childlist=new ArrayList<WeixinButton>();
				for(int m=0;m<childMenus.size();m++){
					childlist.add(combineBtn(childMenus.get(m)));
				}
				wxButton.setSub_button(childlist);
				resultList.add(wxButton);
			}
		}
		//TODO 提示改造
		try {
			JwMenuAPI.createMenu(accessToken, resultList);
			j.setMsg("同步微信菜单成功！");
			j.setSuccess(true);
		} catch (WexinReqException e) {
			e.printStackTrace();
		}
		return j;
	}

	/**
	 * @功能：组装菜单接口的参数结构体
	 * @param weixinMenu
	 * @param listBtnSub
	 */
	private WeixinButton combineBtn(WeixinMenu weixinMenu) {
		WeixinButton wxButton=new WeixinButton();
		//网页链接类
		if("view".equals(weixinMenu.getMenuType())){
			wxButton.setName(weixinMenu.getMenuName());
			wxButton.setType(weixinMenu.getMenuType());
			wxButton.setUrl(weixinMenu.getUrl());
		}
		//消息触发类
		if("click".equals(weixinMenu.getMenuType())){
			wxButton.setName(weixinMenu.getMenuName());
			wxButton.setType(weixinMenu.getMenuType());
			wxButton.setKey(weixinMenu.getMenuKey());
		}
		//小程序类
		if("miniprogram".equals(weixinMenu.getMenuType())){
			wxButton.setName(weixinMenu.getMenuName());
			wxButton.setType(weixinMenu.getMenuType());
			wxButton.setUrl(weixinMenu.getUrl());
			wxButton.setAppid(weixinMenu.getMiniprogramAppid());
			wxButton.setPagepath(weixinMenu.getMiniprogramPagepath());
		}
		return wxButton;
	}
	
}
