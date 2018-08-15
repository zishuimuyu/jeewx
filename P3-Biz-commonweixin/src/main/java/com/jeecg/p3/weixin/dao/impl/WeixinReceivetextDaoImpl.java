package com.jeecg.p3.weixin.dao.impl;

import java.util.List;

import org.jeecgframework.p3.core.utils.common.PageQuery;
import org.jeecgframework.p3.core.utils.common.PageQueryWrapper;
import org.jeecgframework.p3.core.utils.persistence.mybatis.GenericDaoDefault;
import org.springframework.stereotype.Repository;
import com.jeecg.p3.weixin.dao.WeixinReceivetextDao;
import com.jeecg.p3.weixin.entity.WeixinReceivetext;

/**
 * 描述：</b>消息存储<br>
 * @author：LeeShaoQing
 * @since：2018年07月25日 16时02分13秒 星期三 
 * @version:1.0
 */
@Repository("weixinReceivetextDao")
public class WeixinReceivetextDaoImpl extends GenericDaoDefault<WeixinReceivetext> implements WeixinReceivetextDao{

	@Override
	public Integer count(PageQuery<WeixinReceivetext> pageQuery) {
		return (Integer) super.queryOne("count",pageQuery);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<WeixinReceivetext> queryPageList(
			PageQuery<WeixinReceivetext> pageQuery,Integer itemCount) {
		PageQueryWrapper<WeixinReceivetext> wrapper = new PageQueryWrapper<WeixinReceivetext>(pageQuery.getPageNo(), pageQuery.getPageSize(),itemCount, pageQuery.getQuery());
		return (List<WeixinReceivetext>)super.query("queryPageList",wrapper);
	}


}

