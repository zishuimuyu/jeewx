package com.jeecg.p3.weixin.vo.resp;

/**
 * 图片消息
 * @author Administrator
 *
 */
public class ImageMessageResp extends BaseMessageResp{
	// 图片
    private Image Image;

	public Image getImage() {
		return Image;
	}

	public void setImage(Image image) {
		Image = image;
	}
    
    
}
