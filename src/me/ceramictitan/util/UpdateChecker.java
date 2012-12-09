package me.ceramictitan.util;




import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilderFactory;

import me.ceramictitan.spy.spy;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class UpdateChecker {
	
	private spy plugin;
	private URL filesFeed;
	
	private String version;
	private String link;
	
	public UpdateChecker(spy plugin, String url){
		this.plugin = plugin;
		
		try {
			this.filesFeed = new URL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	public boolean updateNeeded(){
		try {
			InputStream input = this.filesFeed.openConnection().getInputStream();
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);
			
			Node lastestFile = document.getElementsByTagName("item").item(0);
			NodeList files = lastestFile.getChildNodes();
			
			this.version = files.item(1).getTextContent().replaceAll("[a-zA-Z ]", "");
			this.link = files.item(3).getTextContent();
			
			if(!plugin.getDescription().getVersion().equals(this.version)){
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	public String getVersion(){
		return this.version;
	}
	public String getLink(){
		return this.link;
	}

}
