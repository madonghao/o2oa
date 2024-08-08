package com.x.message.assemble.communicate.message;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;
import com.x.base.core.project.gson.GsonPropertyObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

public class YunzhijiaBaseMessage extends GsonPropertyObject {

	private static final long serialVersionUID = 3912873590699285995L;

	protected String no;
	protected String pub;
	protected String pubtoken;
	protected String nonce;
	protected String time;
	protected JsonArray to = new JsonArray();
	protected int type;
	protected JsonObject msg = new JsonObject();

	public YunzhijiaBaseMessage(String no, String pub, String pubSecret) {
		this.no = no;
		this.pub = pub;
		this.nonce = UUID.randomUUID().toString();
		this.time = String.valueOf(Instant.now().getEpochSecond());
		this.pubtoken = generatePubtoken(no, pub, pubSecret, nonce, time);
	}

	private String generatePubtoken(String no, String pub, String pubSecret, String nonce, String time) {
		String[] data = {no, pub, pubSecret, nonce, time};
		Arrays.sort(data);
		String joinedData = StringUtils.join(data);
		return DigestUtils.sha1Hex(joinedData);
	}

	public void addUser(String no, String... users) {
		JsonObject userObj = new JsonObject();
		userObj.addProperty("no", no);
		JsonArray userArray = new JsonArray();
		for (String user : users) {
			userArray.add(user);
		}
		userObj.add("user", userArray);
		this.to.add(userObj);
	}

	public String toJson() {
		JsonObject jsonObject = new JsonObject();
		JsonObject from = new JsonObject();
		from.addProperty("no", no);
		from.addProperty("pub", pub);
		from.addProperty("pubtoken", pubtoken);
		from.addProperty("nonce", nonce);
		from.addProperty("time", time);

		jsonObject.add("from", from);
		jsonObject.add("to", to);
		jsonObject.addProperty("type", type);
		jsonObject.add("msg", msg);

		return new Gson().toJson(jsonObject);
	}
}
