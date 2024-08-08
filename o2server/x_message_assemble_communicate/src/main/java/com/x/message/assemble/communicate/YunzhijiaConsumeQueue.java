package com.x.message.assemble.communicate;

import com.x.base.core.container.EntityManagerContainer;
import com.x.base.core.container.factory.EntityManagerContainerFactory;
import com.x.base.core.project.cache.CacheManager;
import com.x.base.core.project.config.Config;
import com.x.base.core.project.connection.HttpConnection;
import com.x.base.core.project.logger.Logger;
import com.x.base.core.project.logger.LoggerFactory;
import com.x.base.core.project.message.MessageConnector;

import com.x.base.core.project.queue.AbstractQueue;
import com.x.base.core.project.tools.DefaultCharset;
import com.x.message.assemble.communicate.message.*;
import com.x.message.core.entity.Message;
import org.apache.commons.lang3.StringUtils;

import java.net.URLEncoder;

public class YunzhijiaConsumeQueue extends AbstractQueue<Message> {

	private static final Logger LOGGER = LoggerFactory.getLogger(YunzhijiaConsumeQueue.class);

	protected void execute(Message message) throws Exception {
		if (Config.yunzhijia().getEnable() && Config.yunzhijia().getMessageEnable()) {
			try (EntityManagerContainer emc = EntityManagerContainerFactory.instance().create()) {
				Business business = new Business(emc);
				// 构建消息
				String m = generateMessage(message, business);
				String address = Config.yunzhijia().getApiAddress() + "/pubacc/pubsendV2";
				YunzhijiaMessageResp resp = HttpConnection.postAsObject(address, null, m,
						YunzhijiaMessageResp.class);
				if (resp.getCode() != 0) {
					ExceptionYunzhijiaMessage e = new ExceptionYunzhijiaMessage(resp.getCode(), resp.getMessage());
					LOGGER.error(e);
				} else {
					Message messageEntityObject = emc.find(message.getId(), Message.class);
					if (null != messageEntityObject) {
						emc.beginTransaction(Message.class);
						messageEntityObject.setConsumed(true);
						emc.commit();
					}
				}
			}
		}
	}

	private String generateMessage(Message message, Business business) throws Exception {
		String content = message.getTitle();
		String workUrl = getOpenUrl(message);
		String no = Config.yunzhijia().getEid();
		String pub = Config.yunzhijia().getPub();
		String pubSecret = Config.yunzhijia().getPubSecret();
		String appId = Config.yunzhijia().getAppId();
		String openId = business.organization().person().getObject(message.getPerson()).getYunzhijiaId();
		// 有超链接的发送卡片消息 目前支持 内容管理和流程
		if (needTransferLink(message.getType()) && StringUtils.isNotEmpty(workUrl)) {
			YunzhijiaTextCardMessage cardMessage = new YunzhijiaTextCardMessage(no, pub, pubSecret);
			cardMessage.addUser(no, openId);
			cardMessage.setAppId(appId);
			cardMessage.setTodo(0);
			// 内容管理
			if (MessageConnector.TYPE_CMS_PUBLISH.equals(message.getType())
					|| MessageConnector.TYPE_CMS_PUBLISH_TO_CREATOR.equals(message.getType())) {
				String categoryName = DingdingConsumeQueue.OuterMessageHelper.getPropertiesFromBody("categoryName", message.getBody());
				if (StringUtils.isEmpty(categoryName)) {
					categoryName = "信息通知";
				}
				content = "【"+categoryName+"】" + "\n" + content;
			} else {
				String processName = DingdingConsumeQueue.OuterMessageHelper.getPropertiesFromBody("processName", message.getBody());
				if (StringUtils.isEmpty(processName)) {
					processName = "工作通知";
				}
				content = "【"+processName+"】" + "\n" + content;
			}
			cardMessage.setText(content);
			cardMessage.setUrl(workUrl);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("云之家文本链接消息：{}", cardMessage::toString);
			}
			return cardMessage.toJson();

		} else { // 其他是普通文本消息
			YunzhijiaTextMessage textMessage = new YunzhijiaTextMessage(no, pub, pubSecret);
			textMessage.setText(content);
			textMessage.addUser(no, openId);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("云之家文本消息：{}", textMessage::toString);
			}
			return textMessage.toJson();
		}

	}

	/**
	 * 判断打开地址
	 *
	 * @param message
	 * @return
	 */
	private String getOpenUrl(Message message) {
		String openUrl = "";
		// cms 文档
		if (MessageConnector.TYPE_CMS_PUBLISH.equals(message.getType())
				|| MessageConnector.TYPE_CMS_PUBLISH_TO_CREATOR.equals(message.getType())) {
			openUrl = getYzjOpenCMSDocumentUrl(message.getBody());
		} else { // 流程工作相关的
			openUrl = getYzjOpenWorkUrl(message.getBody());
		}
		return openUrl;
	}

	/**
	 * 文档打开的url
	 *
	 * @param messageBody
	 * @return
	 */
	private String getYzjOpenCMSDocumentUrl(String messageBody) {
		String o2oaUrl = null;
		try {
			o2oaUrl = Config.yunzhijia().getWorkUrl() + "yunzhijiasso.html?redirect=";
			o2oaUrl = DingdingConsumeQueue.OuterMessageHelper.getOpenCMSDocumentUrl(o2oaUrl, messageBody);
			if (StringUtils.isEmpty(o2oaUrl)) {
				return null;
			}
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("o2oa 地址：{}" , o2oaUrl);
			}
			o2oaUrl = URLEncoder.encode(o2oaUrl, DefaultCharset.name);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("encode url : {}", o2oaUrl);
			}
			String url = "";
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("final url : {}" , url);
			}
			return o2oaUrl;
		} catch (Exception e) {
			LOGGER.error(e);
			return null;
		}
	}

	/**
	 * 生成单点登录和打开工作的地址
	 * 
	 * @param messageBody
	 * @return
	 */
	private String getYzjOpenWorkUrl(String messageBody) {
		try {
			String work = DingdingConsumeQueue.OuterMessageHelper.getWorkIdFromBody(messageBody);
			String o2oaUrl = Config.yunzhijia().getWorkUrl();
			if (StringUtils.isEmpty(work) || StringUtils.isEmpty(o2oaUrl)) {
				return null;
			}
			String openPage = DingdingConsumeQueue.OuterMessageHelper.getOpenPageUrl(messageBody);
			if (StringUtils.isNotEmpty(openPage)) {
				o2oaUrl = o2oaUrl + "yunzhijiasso.html?redirect=" + openPage;
			} else {
				String workUrl = "workmobilewithaction.html?workid=" + work;
				String messageRedirectPortal = Config.yunzhijia().getMessageRedirectPortal();
				if (messageRedirectPortal != null && !"".equals(messageRedirectPortal)) {
					String portal = "portalmobile.html?id=" + messageRedirectPortal;
					portal = URLEncoder.encode(portal, DefaultCharset.name);
					workUrl += "&redirectlink=" + portal;
				}
				workUrl = URLEncoder.encode(workUrl, DefaultCharset.name);
				o2oaUrl = o2oaUrl + "yunzhijiasso.html?redirect=" + workUrl;
			}
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("o2oa 地址：{}", o2oaUrl);
			}
			o2oaUrl = URLEncoder.encode(o2oaUrl, DefaultCharset.name);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("encode url : {}" , o2oaUrl);
			}
			String url = "";
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("final url : {}" , url);
			}
			return o2oaUrl;
		} catch (Exception e) {
			LOGGER.error(e);
		}

		return "";
	}


	/**
	 * 是否需要把企业微信消息转成超链接消息 根据是否配置了企业微信应用链接、是否是工作消息（目前只支持工作消息）
	 * 
	 * @param messageType 消息类型 判断是否是工作消息
	 * @return
	 */
	private boolean needTransferLink(String messageType) {
		try {
			String workUrl = Config.yunzhijia().getWorkUrl();
			if (StringUtils.isNotEmpty(workUrl)) {
				return true;
			}
		} catch (Exception e) {
			LOGGER.error(e);
		}
		return false;
	}



	public static class YunzhijiaMessageResp {

		/**
		 * <code>	 {
		 *     "code": 0,
		 *     "data": {
		 *         "pubId": "XT-58607e8b-5646-4a40-961f-a4ded0a789c1",
		 *         "sourceMsgId": "XT-66b08f86e4b0e9ac0dffdaef"
		 *     },
		 *     "message": "发送成功",
		 *     "success": true
		 * }
		 * </code>
		 */

		private Integer code;
		private YunzhijiaMessageData data;
		private String message;
		private String success;

		public Integer getCode() {
			return code;
		}

		public void setCode(Integer code) {
			this.code = code;
		}

		public YunzhijiaMessageData getData() {
			return data;
		}

		public void setData(YunzhijiaMessageData data) {
			this.data = data;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public String getSuccess() {
			return success;
		}

		public void setSuccess(String success) {
			this.success = success;
		}
	}

	public static class YunzhijiaMessageData {
		private String pubId;
		private String sourceMsgId;

		public String getPubId() {
			return pubId;
		}

		public void setPubId(String pubId) {
			this.pubId = pubId;
		}

		public String getSourceMsgId() {
			return sourceMsgId;
		}

		public void setSourceMsgId(String sourceMsgId) {
			this.sourceMsgId = sourceMsgId;
		}
	}
}
