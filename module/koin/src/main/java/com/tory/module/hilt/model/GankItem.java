package com.tory.module.hilt.model;

import com.google.gson.annotations.SerializedName;

public class GankItem {

	@SerializedName("createdAt")
	private String createdAt;

	@SerializedName("publishedAt")
	private String publishedAt;

	@SerializedName("_id")
	private String id;

	@SerializedName("source")
	private String source;

	@SerializedName("used")
	private boolean used;

	@SerializedName("type")
	private String type;

	@SerializedName("url")
	private String url;

	@SerializedName("desc")
	private String desc;

	@SerializedName("who")
	private String who;

	public void setCreatedAt(String createdAt){
		this.createdAt = createdAt;
	}

	public String getCreatedAt(){
		return createdAt;
	}

	public void setPublishedAt(String publishedAt){
		this.publishedAt = publishedAt;
	}

	public String getPublishedAt(){
		return publishedAt;
	}

	public void setId(String id){
		this.id = id;
	}

	public String getId(){
		return id;
	}

	public void setSource(String source){
		this.source = source;
	}

	public String getSource(){
		return source;
	}

	public void setUsed(boolean used){
		this.used = used;
	}

	public boolean isUsed(){
		return used;
	}

	public void setType(String type){
		this.type = type;
	}

	public String getType(){
		return type;
	}

	public void setUrl(String url){
		this.url = url;
	}

	public String getUrl(){
		return url;
	}

	public void setDesc(String desc){
		this.desc = desc;
	}

	public String getDesc(){
		return desc;
	}

	public void setWho(String who){
		this.who = who;
	}

	public String getWho(){
		return who;
	}

	@Override
 	public String toString(){
		return 
			"GankItem{" +
			"createdAt = '" + createdAt + '\'' + 
			",publishedAt = '" + publishedAt + '\'' + 
			",_id = '" + id + '\'' + 
			",source = '" + source + '\'' + 
			",used = '" + used + '\'' + 
			",type = '" + type + '\'' + 
			",url = '" + url + '\'' + 
			",desc = '" + desc + '\'' + 
			",who = '" + who + '\'' + 
			"}";
		}
}
