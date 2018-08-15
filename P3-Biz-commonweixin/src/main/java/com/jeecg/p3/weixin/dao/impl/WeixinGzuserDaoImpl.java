package com.jeecg.p3.weixin.dao.impl;

import java.util.List;
import java.util.Map;

import org.jeecgframework.p3.core.utils.common.PageQuery;
import org.jeecgframework.p3.core.utils.common.PageQueryWrapper;
import org.jeecgframework.p3.core.utils.persistence.mybatis.GenericDaoDefault;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Maps;
import com.jeecg.p3.weixin.dao.WeixinGzuserDao;
import com.jeecg.p3.weixin.entity.WeixinGzuser;

/**
 * 描述：</b>粉丝表<br>
 * @author：weijian.zhang
 * @since：2018年07月26日 15时38分40秒 星期四 
 * @version:1.0
 */
@Repository("weixinGzuserDao")
public class WeixinGzuserDaoImpl extends GenericDaoDefault<WeixinGzuser> implements WeixinGzuserDao{

	@Override
	public Integer count(PageQuery<WeixinGzuser> pageQuery) {
		return (Integer) super.queryOne("count",pageQuery);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<WeixinGzuser> queryPageList(
			PageQuery<WeixinGzuser> pageQuery,Integer itemCount) {
		PageQueryWrapper<WeixinGzuser> wrapper = new PageQueryWrapper<WeixinGzuser>(pageQuery.getPageNo(), pageQuery.getPageSize(),itemCount, pageQuery.getQuery());
		return (List<WeixinGzuser>)super.query("queryPageList",wrapper);
	}

	@Override
	public List<WeixinGzuser> queryNumberByJwid(String jwid, int pageNo,
			int pageSize) {
		Map<String,Object> map = Maps.newConcurrentMap();
		map.put("jwid", jwid);
		map.put("pageNo", pageNo);
		map.put("pageSize", pageSize);
		return (List<WeixinGzuser>)super.query("queryNumberByJwid", map);
	}

	@Override
	public WeixinGzuser queryByOpenId(String openId) {
		Map<String,Object> map = Maps.newConcurrentMap();
		map.put("openId", openId);
		return (WeixinGzuser)super.queryOne("queryByOpenId", map);
	}


}

