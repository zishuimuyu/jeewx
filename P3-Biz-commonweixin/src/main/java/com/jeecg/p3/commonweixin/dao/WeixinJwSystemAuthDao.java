package com.jeecg.p3.commonweixin.dao;

import java.util.List;

import org.jeecgframework.p3.core.utils.common.PageQuery;
import org.jeecgframework.p3.core.utils.persistence.GenericDao;

import com.jeecg.p3.commonweixin.entity.WeixinAuth;
import com.jeecg.p3.commonweixin.entity.WeixinJwSystemAuth;
import com.jeecg.p3.commonweixin.entity.WeixinMenu;
import com.jeecg.p3.commonweixin.entity.WeixinMenuFunction;


/**
 * 描述：</b>WxJwSystemAuthDao<br>
 * @author：junfeng.zhou
 * @since：2015年12月21日 10时28分27秒 星期一 
 * @version:1.0
 */
public interface WeixinJwSystemAuthDao extends GenericDao<WeixinJwSystemAuth>{
	
	public Integer count(PageQuery<WeixinJwSystemAuth> pageQuery);
	
	public List<WeixinJwSystemAuth> queryPageList(PageQuery<WeixinJwSystemAuth> pageQuery,Integer itemCount);
	
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
     * 删除角色下所有关联的权限
     * @param <pre>
     *             <li>roleId : 角色编码</li>
     *        </pre>
     *             
     * @return  
     * @author：junfeng.zhou
     */
    public void deleteRoleAuthRels(String roleId);
    
    /**
     * 删除角色下所有关联的权限
     * @param <pre>
     *             <li>roleId : 角色编码</li>
     *             <li>authId : 权限编码</li>
     *        </pre>
     *             
     * @return  
     * @author：junfeng.zhou
     */
    public void insertRoleAuthRels(String roleId,String authId);
    
    public List<WeixinMenu> queryMenuByUserIdAndParentAuthId(String userId, String parentAuthId);
    
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
	/**
	 * 通过权限id查询层级
	 * @param AuthId 权限Id
	 * @return
	 */
	public WeixinJwSystemAuth queryOneByAuthId(String authId);
}

