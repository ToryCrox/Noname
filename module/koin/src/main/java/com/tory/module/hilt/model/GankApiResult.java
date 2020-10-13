package com.tory.demo.jetpack.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GankApiResult{

	@SerializedName("error")
	private boolean error;

	@SerializedName("results")
	private List<GankItem> results;

	public void setError(boolean error){
		this.error = error;
	}

	public boolean isError(){
		return error;
	}

	public void setResults(List<GankItem> results){
		this.results = results;
	}

	public List<GankItem> getResults(){
		return results;
	}

	@Override
 	public String toString(){
		return 
			"GankApiResult{" + 
			"error = '" + error + '\'' + 
			",results = '" + results + '\'' + 
			"}";
		}
}
