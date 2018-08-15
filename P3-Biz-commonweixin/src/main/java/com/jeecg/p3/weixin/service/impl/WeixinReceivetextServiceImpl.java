package com.jeecg.p3.weixin.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import org.jeecgframework.p3.core.utils.common.PageList;
import org.jeecgframework.p3.core.utils.common.PageQuery;
import org.jeecgframework.p3.core.utils.common.Pagenation;
import com.jeecg.p3.weixin.service.WeixinReceivetextService;
import com.jeecg.p3.weixin.entity.WeixinReceivetext;
import com.jeecg.p3.weixin.dao.WeixinReceivetextDao;

/**
 * 描述：</b>消息存储<br>
 * @author：LeeShaoQing
 * @since：2018年07月25日 16时02分13秒 星期三 
 * @version:1.0
 */
@Service("weixinReceivetextService")
public class WeixinReceivetextServiceImpl implements WeixinReceivetextService {
	@Resource
	private WeixinReceivetextDao weixinReceivetextDao;

	@Override
	public void doAdd(WeixinReceivetext weixinReceivetext) {
		weixinReceivetextDao.add(weixinReceivetext);
	}

	@Override
	public void doEdit(WeixinReceivetext weixinReceivetext) {
		weixinReceivetextDao.update(weixinReceivetext);
	}

	@Override
	public void doDelete(String id) {
		weixinReceivetextDao.delete(id);
	}

	@Override
	public WeixinReceivetext queryById(String id) {
		WeixinReceivetext weixinReceivetext  = weixinReceivetextDao.get(id);
		return weixinReceivetext;
	}

	@Override
	public PageList<WeixinReceivetext> queryPageList(
		PageQuery<WeixinReceivetext> pageQuery) {
		PageList<WeixinReceivetext> result = new PageList<WeixinReceivetext>();
		Integer itemCount = weixinReceivetextDao.count(pageQuery);
		List<WeixinReceivetext> list = weixinReceivetextDao.queryPageList(pageQuery,itemCount);
		Pagenation pagenation = new Pagenation(pageQuery.getPageNo(), itemCount, pageQuery.getPageSize());
		result.setPagenation(pagenation);
		result.setValues(list);
		return result;
	}
	
}
