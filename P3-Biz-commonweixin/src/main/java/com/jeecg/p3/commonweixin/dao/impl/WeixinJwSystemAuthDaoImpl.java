package com.jeecg.p3.commonweixin.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jeecgframework.p3.core.utils.common.PageQuery;
import org.jeecgframework.p3.core.utils.common.PageQueryWrapper;
import org.jeecgframework.p3.core.utils.persistence.mybatis.GenericDaoDefault;
import org.springframework.stereotype.Repository;

import com.jeecg.p3.commonweixin.dao.WeixinJwSystemAuthDao;
import com.jeecg.p3.commonweixin.entity.WeixinAuth;
import com.jeecg.p3.commonweixin.entity.WeixinJwSystemAuth;
import com.jeecg.p3.commonweixin.entity.WeixinMenu;
import com.jeecg.p3.commonweixin.entity.WeixinMenuFunction;


/**
 * 描述：</b>JwSystemAuthDaoImpl<br>
 * @author：junfeng.zhou
 * @since：2015年12月21日 10时28分27秒 星期一 
 * @version:1.0
 */
@Repository("weixinJwSystemAuthDao")
public class WeixinJwSystemAuthDaoImpl extends GenericDaoDefault<WeixinJwSystemAuth> implements WeixinJwSystemAuthDao{

	@Override
	public Integer count(PageQuery<WeixinJwSystemAuth> pageQuery) {
		return (Integer) super.queryOne("count",pageQuery);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<WeixinJwSystemAuth> queryPageList(PageQuery<WeixinJwSystemAuth> pageQuery,Integer itemCount) {
		PageQueryWrapper<WeixinJwSystemAuth> wrapper = new PageQueryWrapper<WeixinJwSystemAuth>(pageQuery.getPageNo(), pageQuery.getPageSize(),itemCount, pageQuery.getQuery());
		return (List<WeixinJwSystemAuth>)super.query("queryPageList",wrapper);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<WeixinMenuFunction> queryMenuAndFuncAuth() {
		return (List<WeixinMenuFunction>)super.query("queryMenuAndFuncAuth");
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<WeixinMenuFunction> queryMenuAndFuncAuthByRoleId(String roleId) {
		return (List<WeixinMenuFunction>)super.query("queryMenuAndFuncAuthByRoleId",roleId);
	}

	@Override
	public WeixinMenu queryMenuByAuthId(String authId) {
		return (WeixinMenu)super.queryOne("queryMenuByAuthId",authId);
	}

	@Override
	public void deleteRoleAuthRels(String roleId) {
		super.delete("deleteRoleAuthRels", roleId);
	}

	@Override
	public void insertRoleAuthRels(String roleId, String authId) {
		Map<String,String> map = new HashMap<String,String>();
		map.put("roleId", roleId);
		map.put("authId", authId);
		super.getSqlSession().insert(getStatementId("insertRoleAuthRels"), map);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<WeixinMenu> queryMenuByUserIdAndParentAuthId(String userId,
			String parentAuthId) {
		Map<String,String> map = new HashMap<String,String>();
		map.put("userId", userId);
		map.put("parentAuthId", parentAuthId);
		return (List<WeixinMenu>)super.query("queryMenuByUserIdAndParentAuthId",map);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<WeixinAuth> queryAuthByUserId(String userId) {
		return (List<WeixinAuth>)super.query("queryAuthByUserId",userId);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<WeixinAuth> queryAuthByAuthContr(String authContr) {
		return (List<WeixinAuth>)super.query("queryAuthByAuthContr",authContr);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<WeixinAuth> queryAuthByUserIdAndAuthContr(String userId,
			String authContr) {
		Map<String,String> map = new HashMap<String,String>();
		map.put("userId", userId);
		map.put("authContr", authContr);
		return (List<WeixinAuth>)super.query("queryAuthByUserIdAndAuthContr",map);
	}

	@Override
	public WeixinJwSystemAuth queryOneByAuthId(String authId) {
		Map<String,String> param = new HashMap<String,String>();
		param.put("authId",authId);
		return (WeixinJwSystemAuth)super.queryOne("queryOneByAuthId",param);
	}

}

