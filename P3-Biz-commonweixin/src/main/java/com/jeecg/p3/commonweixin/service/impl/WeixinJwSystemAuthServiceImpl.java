package com.jeecg.p3.commonweixin.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Resource;

import org.jeecgframework.p3.core.utils.common.PageList;
import org.jeecgframework.p3.core.utils.common.PageQuery;
import org.jeecgframework.p3.core.utils.common.Pagenation;
import org.jeecgframework.p3.core.utils.common.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeecg.p3.commonweixin.dao.WeixinJwSystemAuthDao;
import com.jeecg.p3.commonweixin.entity.WeixinAuth;
import com.jeecg.p3.commonweixin.entity.WeixinJwSystemAuth;
import com.jeecg.p3.commonweixin.entity.WeixinMenu;
import com.jeecg.p3.commonweixin.entity.WeixinMenuFunction;
import com.jeecg.p3.commonweixin.service.WeixinJwSystemAuthService;


@Service("weixinJwSystemAuthService")
public class WeixinJwSystemAuthServiceImpl implements WeixinJwSystemAuthService {
	@Resource
	private WeixinJwSystemAuthDao weixinJwSystemAuthDao;

	@Override
	public void doAdd(WeixinJwSystemAuth jwSystemAuth) {
		jwSystemAuth.setDelStat("0");
		if(StringUtils.isEmpty(jwSystemAuth.getParentAuthId())){
			jwSystemAuth.setBizLevel("1");
		}else{
			WeixinJwSystemAuth jwSystemAuth2 = weixinJwSystemAuthDao.queryOneByAuthId(jwSystemAuth.getParentAuthId());
			if(jwSystemAuth2!=null&&StringUtils.isNotEmpty(jwSystemAuth2.getBizLevel())){
				jwSystemAuth.setBizLevel(Integer.parseInt(jwSystemAuth2.getBizLevel())+1+"");
			}else{
				jwSystemAuth.setBizLevel("1");
			}
		}
		weixinJwSystemAuthDao.add(jwSystemAuth);
	}

	@Override
	public void doEdit(WeixinJwSystemAuth jwSystemAuth) {
		if(StringUtils.isEmpty(jwSystemAuth.getParentAuthId())){
			jwSystemAuth.setBizLevel("1");
		}else{
			WeixinJwSystemAuth jwSystemAuth2 = weixinJwSystemAuthDao.queryOneByAuthId(jwSystemAuth.getParentAuthId());
			if(jwSystemAuth2!=null&&StringUtils.isNotEmpty(jwSystemAuth2.getBizLevel())){
				jwSystemAuth.setBizLevel(Integer.parseInt(jwSystemAuth2.getBizLevel())+1+"");
			}else{
				jwSystemAuth.setBizLevel("1");
			}
		}
		weixinJwSystemAuthDao.update(jwSystemAuth);
	}

	@Override
	public void doDelete(Long id) {
		weixinJwSystemAuthDao.delete(id);
	}

	@Override
	public WeixinJwSystemAuth queryById(Long id) {
		WeixinJwSystemAuth jwSystemAuth  = weixinJwSystemAuthDao.get(id);
		return jwSystemAuth;
	}

	@Override
	public PageList<WeixinJwSystemAuth> queryPageList(
		PageQuery<WeixinJwSystemAuth> pageQuery) {
		PageList<WeixinJwSystemAuth> result = new PageList<WeixinJwSystemAuth>();
		Integer itemCount = weixinJwSystemAuthDao.count(pageQuery);
		List<WeixinJwSystemAuth> list = weixinJwSystemAuthDao.queryPageList(pageQuery,itemCount);
		Pagenation pagenation = new Pagenation(pageQuery.getPageNo(), itemCount, pageQuery.getPageSize());
		result.setPagenation(pagenation);
		LinkedList<WeixinJwSystemAuth> linklist = new LinkedList<WeixinJwSystemAuth>();
		
		for(WeixinJwSystemAuth auth :list){
			if(StringUtils.isEmpty(auth.getParentAuthId())){
				linklist.add(auth);
			}else{
				for(int i=0;i<linklist.size();i++){
					if(linklist.get(i).getAuthId().equals(auth.getParentAuthId())){
						int j=i;
						while(j+1<linklist.size()&&linklist.get(j+1).getParentAuthId().equals(auth.getParentAuthId())){
							j++;
						}
						linklist.add(j+1, auth);
						break;
					}
				}
			}
		}
		
		result.setValues(linklist);
		return result;
	}
	
	@Override
	public List<WeixinMenuFunction> queryMenuAndFuncAuth() {
		return weixinJwSystemAuthDao.queryMenuAndFuncAuth();
	}

	@Override
	public List<WeixinMenuFunction> queryMenuAndFuncAuthByRoleId(String roleId) {
		return weixinJwSystemAuthDao.queryMenuAndFuncAuthByRoleId(roleId);
	}

	@Override
	public WeixinMenu queryMenuByAuthId(String authId) {
		return weixinJwSystemAuthDao.queryMenuByAuthId(authId);
	}

	@Override
	@Transactional(rollbackFor = {Exception.class})
	public void modifyOperateRoleAuthRel(String roleId,List<String> authIds) {
		this.weixinJwSystemAuthDao.deleteRoleAuthRels(roleId);
		if(authIds!=null&&authIds.size()>0){
			for(String authId:authIds){
				this.weixinJwSystemAuthDao.insertRoleAuthRels(roleId, authId);
			}
		}
	}

	@Override
	public LinkedHashMap<WeixinMenu, ArrayList<WeixinMenu>> getSubMenuTree(String userId, String parentAuthId) {
		/*所有子菜单*/
    	List<WeixinMenu> allSubMenuList = getAllSubMenuList(userId, parentAuthId, new ArrayList<WeixinMenu>());

    	LinkedHashMap<WeixinMenu, ArrayList<WeixinMenu>> result = new LinkedHashMap<WeixinMenu, ArrayList<WeixinMenu>>();
    	
    	for(WeixinMenu menu: allSubMenuList){
    		if(isParentMenu(menu, allSubMenuList)){
    			ArrayList<WeixinMenu> subMenuList = getSubMenuList(allSubMenuList, menu.getAuthId());
    			result.put(menu, subMenuList);
    		}else if(!isSonMenu(menu, allSubMenuList)) {
    			result.put(menu, null);
    		}
    	}
    	
    	return result;
	}
	
	
	@Override
	public List<WeixinMenu> getMenuTree(String userId) {
		/*所有子菜单*/
    	List<WeixinMenu> allSubMenuList = getMenuList(userId, null);
    	return allSubMenuList;
	}
	
	
	/**
     * 根据用户编码和父菜单编码获取当前父菜单下的所有子菜单
     * @param userId
     * @param parentAuthId
     * @param allSubMenu
     * @return List<Menu>
     */
	private List<WeixinMenu> getMenuList(String userId, String parentAuthId) {
		List<WeixinMenu> curSubMenu = weixinJwSystemAuthDao.queryMenuByUserIdAndParentAuthId(userId, parentAuthId);
		/*叶子节点菜单*/
		for (WeixinMenu menu : curSubMenu) {
			List<WeixinMenu> allSubMenu = getSubMenuList(userId, menu.getAuthId());
			menu.setChildMenu(allSubMenu);
		}
		return curSubMenu;
	}
	
	/**
     * 根据用户编码和父菜单编码获取当前父菜单下的所有子菜单
     * @param userId
     * @param parentAuthId
     * @param allSubMenu
     * @return List<Menu>
     */
	private List<WeixinMenu> getSubMenuList(String userId, String parentAuthId) {
		List<WeixinMenu> curSubMenu = weixinJwSystemAuthDao.queryMenuByUserIdAndParentAuthId(userId, parentAuthId);
		for (WeixinMenu menu : curSubMenu) {
			List<WeixinMenu> subMenu = getSubMenuList(userId, menu.getAuthId());
			menu.setChildMenu(subMenu);
		}
		return curSubMenu;
	}
	
	/**
     * 根据用户编码和父菜单编码获取当前父菜单下的所有子菜单
     * @param userId
     * @param parentAuthId
     * @param allSubMenu
     * @return List<Menu>
     */
	private List<WeixinMenu> getAllSubMenuList(String userId, String parentAuthId, List<WeixinMenu> allSubMenu) {
		List<WeixinMenu> curSubMenu = weixinJwSystemAuthDao.queryMenuByUserIdAndParentAuthId(userId, parentAuthId);
		/*叶子节点菜单*/
		if(curSubMenu.size() == 0)
			return allSubMenu;
		for (WeixinMenu menu : curSubMenu) {
			allSubMenu.add(menu);
			int allNum = allSubMenu.size();
			allSubMenu = getAllSubMenuList(userId, menu.getAuthId(), allSubMenu);
			int tmpNum = allSubMenu.size();
			/*叶子节点*/
			if(allNum == tmpNum)
				continue;
		}
		return allSubMenu;
	}
	
	/**
     * 判断当前菜单是否属于父菜单方法
     * @param 
     * @return
     */
    private Boolean isParentMenu(WeixinMenu curMenu, List<WeixinMenu> subMenuList){
    	//菜单的父菜单id在list中存在，表示此菜单属于子菜单
    	for(WeixinMenu menu : subMenuList){
    		if(curMenu.getAuthId().equals(menu.getParentAuthId())){
    			return true;
    		}
    	}
    	return false;
    }
    
    
    /**
     * 判断当前菜单是否属于子菜单方法
     * @param curMenu
     * @param subMenuList
     * @return Boolean
     */
    private Boolean isSonMenu(WeixinMenu curMenu, List<WeixinMenu> subMenuList){
    	for(WeixinMenu menu : subMenuList){
    		if(menu.getAuthId().equals(curMenu.getParentAuthId())){
    			return true;
    		}
    	}
    	
    	return false;
    }
    
    /**
     * 根据父菜单id获取其子菜单列表方法
     * @param 
     * @return
     */
    private ArrayList<WeixinMenu> getSubMenuList(List<WeixinMenu> subMenuList, String parentId){
    	ArrayList<WeixinMenu> result = new ArrayList<WeixinMenu>();
    	for(WeixinMenu menu : subMenuList){
    		if(parentId.equals(menu.getParentAuthId())){
    			result.add(menu);
    		}
    	}
    	return result;
    }

	@Override
	public List<WeixinAuth> queryAuthByUserId(String userId) {
		return weixinJwSystemAuthDao.queryAuthByUserId(userId);
	}

	@Override
	public List<WeixinAuth> queryAuthByAuthContr(String authContr) {
		return weixinJwSystemAuthDao.queryAuthByAuthContr(authContr);
	}

	@Override
	public List<WeixinAuth> queryAuthByUserIdAndAuthContr(String userId,
			String authContr) {
		return weixinJwSystemAuthDao.queryAuthByUserIdAndAuthContr(userId, authContr);
	}
	
}
