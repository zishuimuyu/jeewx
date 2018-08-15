package com.jeecg.p3.commonweixin.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.jeecgframework.p3.core.utils.common.PageList;
import org.jeecgframework.p3.core.utils.common.PageQuery;

import com.jeecg.p3.commonweixin.entity.WeixinAuth;
import com.jeecg.p3.commonweixin.entity.WeixinJwSystemAuth;
import com.jeecg.p3.commonweixin.entity.WeixinMenu;
import com.jeecg.p3.commonweixin.entity.WeixinMenuFunction;


/**
 * 描述：</b>JwSystemAuthService<br>
 * @author：junfeng.zhou
 * @since：2015年12月21日 10时28分27秒 星期一 
 * @version:1.0
 */
public interface WeixinJwSystemAuthService {
	
	
	public void doAdd(WeixinJwSystemAuth jwSystemAuth);
	
	public void doEdit(WeixinJwSystemAuth jwSystemAuth);
	
	public void doDelete(Long id);
	
	public WeixinJwSystemAuth queryById(Long id);
	
	public PageList<WeixinJwSystemAuth> queryPageList(PageQuery<WeixinJwSystemAuth> pageQuery);
	
	/**
     * 查询所有的权限（菜单权限和按钮功能权限）
     * @return  <pre>List<MenuFunction>:所有的权限
     *             <li>authNam:       权限名称</li>
     *             <li>authDesc:      权限说明</li>
     *             <li>authContr:     权限控制</li>
     *             <li>authId:        权限编码</li>
     *             <li>authType:      菜单类型</li>
     *             <li>parentAuthId:  上一级菜单编码</li>
     */
	public List<WeixinMenuFunction> queryMenuAndFuncAuth();
	
	/**
     * 查询所有的权限（菜单权限和按钮功能权限）
     * @param <pre>
     *             <li>roleId : 角色编码</li>
     *        </pre>
     *             
     * @return  <pre>List<MenuFunction>:所有的权限
     *             <li>authNam:       权限名称</li>
     *             <li>authDesc:      权限说明</li>
     *             <li>authContr:     权限控制</li>
     *             <li>authId:        权限编码</li>
     *             <li>authType:      菜单类型</li>
     *             <li>parentAuthId:  上一级菜单编码</li>
     *          </pre>
     */
	public List<WeixinMenuFunction> queryMenuAndFuncAuthByRoleId(String roleId);
	
	
	/**根据权限编码查询权限菜单
     * @param 
     * @return
     * @author：junfeng.zhou
     */
    public WeixinMenu queryMenuByAuthId(String authId);
	
	/**
     * 更新角色权限
     * @param <pre>
     *             <li>roleId : 角色编码</li>
     *             
     *             <li>List<String> authIds
     *             	   authIds : 权限编码List</li>
     *        </pre>
     *             
     * @return  <pre>List<Auth>:所有的权限
     *             <li>userId:        用户编码</li>
     *             <li>authContr:     权限控制</li>
     *          </pre>
     */
	public void modifyOperateRoleAuthRel(String roleId,List<String> authIds);
	
	/**
     * 根据用户编码和父菜单编码获取当前父菜单下的所有子菜单树
     * @param 
     * @return
     */
    public LinkedHashMap<WeixinMenu,ArrayList<WeixinMenu>> getSubMenuTree(String userId, String parentAuthId);
    
    
    /**
     * 根据用户编码获取菜单树
     * @param 
     * @return
     */
    public List<WeixinMenu> getMenuTree(String userId);
    
    
    /**
     * 查询所有的权限（菜单权限和按钮功能权限）
     * @param <pre>
     *             <li>userId : 用户编码</li>
     *        </pre>
     *             
     * @return  <pre>List<Auth>:所有的权限
     *             <li>userId:        用户编码</li>
     *             <li>authContr:     权限控制</li>
     *          </pre>
     */
	public List<WeixinAuth> queryAuthByUserId(String userId);
	
	/**
	 * 根据authContr查询权限
	 * @param authContr
	 * @return
	 */
	public List<WeixinAuth> queryAuthByAuthContr(String authContr);
	
	/**
     * 查询所有的权限（菜单权限和按钮功能权限）
     * @param <pre>
     *             <li>userId : 用户编码</li>
     *        </pre>
     *             
     * @return  <pre>List<Auth>:所有的权限
     *             <li>userId:        用户编码</li>
     *             <li>authContr:     权限控制</li>
     *          </pre>
     */
	public List<WeixinAuth> queryAuthByUserIdAndAuthContr(String userId,String authContr);
}

