package com.x.message.assemble.communicate;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.x.base.core.container.EntityManagerContainer;
import com.x.base.core.container.factory.EntityManagerContainerFactory;
import com.x.base.core.project.config.Config;
import com.x.base.core.project.config.MPweixin;
import com.x.base.core.project.config.MPweixinMessageTemp;
import com.x.base.core.project.connection.HttpConnection;
import com.x.base.core.project.gson.GsonPropertyObject;
import com.x.base.core.project.gson.XGsonBuilder;
import com.x.base.core.project.logger.Logger;
import com.x.base.core.project.logger.LoggerFactory;
import com.x.base.core.project.queue.AbstractQueue;
import com.x.base.core.project.tools.DefaultCharset;
import com.x.message.core.entity.Message;
import com.x.organization.core.entity.Person;

/**
 * 发送微信公众号模版消息 Created by fancyLou on 3/11/21. Copyright © 2021 O2. All rights
 * reserved.
 */
public class MPWeixinConsumeQueue extends AbstractQueue<Message> {

	private static final Logger LOGGER = LoggerFactory.getLogger(MPWeixinConsumeQueue.class);

	private static final Gson gson = XGsonBuilder.instance();

	// creatorPerson 创建人 activityName 当前节点 processName 流程名称 startTime 开始时间 title 标题

	@Override
	protected void execute(Message message) throws Exception {
		String tempId = Config.mPweixin().getTempMessageId();
		List<MPweixinMessageTemp> list = Config.mPweixin().getFieldList();
		if (Config.mPweixin().getEnable() && Config.mPweixin().getMessageEnable() && StringUtils.isNotEmpty(tempId)
				&& (list != null && !list.isEmpty())) {
			Person person = getPerson(message.getPerson());
			if (person != null) {
				String openId = person.getMpwxopenId();
				LOGGER.debug("openId:{}.", () -> openId);
				if (StringUtils.isNotEmpty(openId)) {
					JsonObject object = gson.fromJson(message.getBody(), JsonObject.class);
					Map<String, WeixinTempMessageFieldObj> data = concreteData(message, object, list);
					String workId = object.get("work").getAsString();
					String workUrl = getOpenUrl(workId);
					WeixinTempMessage wxMessage = new WeixinTempMessage();
					wxMessage.setTouser(openId);
					wxMessage.setUrl(workUrl);
					wxMessage.setTemplate_id(tempId);
					wxMessage.setTopcolor("#fb4747");
					wxMessage.setData(data);
					LOGGER.debug("发送的消息对象:{}.", wxMessage::toString);
					String url = MPweixin.default_apiAddress + "/cgi-bin/message/template/send?access_token="
							+ Config.mPweixin().accessToken();
					WeixinResponse response = HttpConnection.postAsObject(url, null, wxMessage.toString(),
							WeixinResponse.class);
					LOGGER.debug("返回:{}.", () -> response);
					if (response.getErrcode() != 0) {
						ExceptionQiyeweixinMessage e = new ExceptionQiyeweixinMessage(response.getErrcode(),
								response.getErrmsg());
						LOGGER.error(e);
					} else {
						success(message.getId());
					}
				} else {
					LOGGER.warn("没有绑定微信公众号:{}.", message::getPerson);
				}
			} else {
				LOGGER.warn("没有找到用户.");
			}

		} else {
			LOGGER.warn("配置文件配置条件不足.");
		}

	}

	private Map<String, WeixinTempMessageFieldObj> concreteData(Message message, JsonObject object,
			List<MPweixinMessageTemp> list) {
		Map<String, WeixinTempMessageFieldObj> data = new HashMap<>();
		WeixinTempMessageFieldObj wobj = new WeixinTempMessageFieldObj();
		wobj.setValue(message.getTitle());
		data.put("first", wobj);
		for (int i = 0; i < list.size(); i++) {
			MPweixinMessageTemp filed = list.get(i);
			String name = filed.getName();
			String tempName = filed.getTempName();
			String value = object.get(name).getAsString();
			LOGGER.debug("解析出的结果 name：{}, value:{}, tempName:{}.", name, value, tempName);
			if ("title".equalsIgnoreCase(name)) { // 工作标题为空就用消息的标题
				if (StringUtils.isEmpty(value)) {
					value = "无标题";
				}
			} else {
				if ("creatorPerson".equalsIgnoreCase(name) && StringUtils.isNotEmpty(value)) {
					value = value.split("@")[0]; // 截取姓名
				}
				if (StringUtils.isEmpty(value)) {
					value = "unknown";
				}
			}
			WeixinTempMessageFieldObj obj = new WeixinTempMessageFieldObj();
			obj.setValue(value);
			obj.setColor("#173177");
			data.put(tempName, obj);
		}
		WeixinTempMessageFieldObj robj = new WeixinTempMessageFieldObj();
		robj.setValue("请注意查收！");
		data.put("remark", robj);
		return data;
	}

	private Person getPerson(String credential) throws Exception {
		Person person;
		try (EntityManagerContainer emc = EntityManagerContainerFactory.instance().create()) {
			Business business = new Business(emc);
			// openid 查询用户
			person = business.message().getPersonWithCredential(credential);
		}
		return person;
	}

	/**
	 * 标志消息消费成功
	 * 
	 * @param id
	 * @throws Exception
	 */
	private void success(String id) throws Exception {
		try (EntityManagerContainer emc = EntityManagerContainerFactory.instance().create()) {
			Message message = emc.find(id, Message.class);
			if (null != message) {
				emc.beginTransaction(Message.class);
				message.setConsumed(true);
				emc.commit();
			}
		}
	}

	private String getOpenUrl(String workId) {
		try {
			String httpProtocol = Config.currentNode().getCenter().getHttpProtocol();
			if (StringUtils.isEmpty(httpProtocol)) {
				LOGGER.error(new Exception("没有获取到http访问协议"));
				return null;
			}
			String host = Config.currentNode().getWeb().getProxyHost();
			if (StringUtils.isEmpty(host)) {
				LOGGER.error(new Exception("没有获取到代理地址"));
				return null;
			}
			String workUrl = "workmobilewithaction.html?workid=" + workId;
			String portalId = Config.mPweixin().getPortalId();
			String portal = "portalmobile.html?id=" + portalId;
			portal = URLEncoder.encode(portal, DefaultCharset.name);
			workUrl += "&redirectlink=" + portal;
			workUrl = URLEncoder.encode(workUrl, DefaultCharset.name);
			String o2oaUrl = httpProtocol + "://" + host + "/x_desktop/";
			o2oaUrl = o2oaUrl + "mpweixinsso.html?redirect=" + workUrl + "&type=login";
			o2oaUrl = URLEncoder.encode(o2oaUrl, DefaultCharset.name);
			String appId = Config.mPweixin().getAppid();
			String url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + appId + "&redirect_uri="
					+ o2oaUrl + "&response_type=code&scope=snsapi_userinfo&state=STATE#wechat_redirect";
			LOGGER.debug("final url:{}.", () -> url);
			return url;
		} catch (Exception e) {
			LOGGER.error(e);
			return null;
		}

	}

	public static class WeixinResponse {
		private Integer errcode;
		private String errmsg;
		private Long msgid;

		public Integer getErrcode() {
			return errcode;
		}

		public void setErrcode(Integer errcode) {
			this.errcode = errcode;
		}

		public String getErrmsg() {
			return errmsg;
		}

		public void setErrmsg(String errmsg) {
			this.errmsg = errmsg;
		}

		public Long getMsgid() {
			return msgid;
		}

		public void setMsgid(Long msgid) {
			this.msgid = msgid;
		}
	}

	/**
	 * 微信发送模版消息的对象
	 */
	public static class WeixinTempMessage extends GsonPropertyObject {

		private static final long serialVersionUID = 1577792511569896768L;
		private String touser;
		private String template_id;
		private String url;
		private String topcolor;
		private Map<String, WeixinTempMessageFieldObj> data; // 模版字段数据

		public String getTouser() {
			return touser;
		}

		public void setTouser(String touser) {
			this.touser = touser;
		}

		public String getTemplate_id() {
			return template_id;
		}

		public void setTemplate_id(String template_id) {
			this.template_id = template_id;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getTopcolor() {
			return topcolor;
		}

		public void setTopcolor(String topcolor) {
			this.topcolor = topcolor;
		}

		public Map<String, WeixinTempMessageFieldObj> getData() {
			return data;
		}

		public void setData(Map<String, WeixinTempMessageFieldObj> data) {
			this.data = data;
		}
	}

	/**
	 * 模版字段对象
	 */
	public static class WeixinTempMessageFieldObj extends GsonPropertyObject {

		private static final long serialVersionUID = -4230870572917531355L;
		private String value;
		private String color;

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public String getColor() {
			return color;
		}

		public void setColor(String color) {
			this.color = color;
		}
	}
}
