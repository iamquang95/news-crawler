package edu.vnu.uet.smm.crawler;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;

import edu.vnu.uet.smm.common.base.SMMDocument;

public class XMLWriter {
	private static void appendToElement(Document doc, Element parentElement, String name, String text) {
		if (text == null) 
			text = " ";
		Element childElement = doc.createElement(name);
		childElement.appendChild(doc.createTextNode(text));
		parentElement.appendChild(childElement);
	}
	
	public static void writeToFile(SMMDocument smmdoc, String path){
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			
			// Root element <info>
			Document doc = docBuilder.newDocument();
			Element infoElement = doc.createElement("info");
			doc.appendChild(infoElement);
			
			// News element
			Element newsElement = doc.createElement("news");
			infoElement.appendChild(newsElement);
			
			// Link in news
			appendToElement(doc, newsElement, "link", smmdoc.getLink());
			// Category in news
			appendToElement(doc, newsElement, "category", smmdoc.getCategory());
			// Date in news
			appendToElement(doc, newsElement, "date", smmdoc.getDate());
			// Update in news
			appendToElement(doc, newsElement, "update", smmdoc.getLastUpdate()); // Cant invoke getLastUpdate() ????
			// Title in news
			appendToElement(doc, newsElement, "title", smmdoc.getTitle());
			// Likes in news
			appendToElement(doc, newsElement, "like", smmdoc.getLike());
			// Content in news
			appendToElement(doc, newsElement, "content", smmdoc.getContent());
			
			// Write to file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(path));
			transformer.transform(source, result);
			
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		  } catch (TransformerException tfe) {
			  tfe.printStackTrace();
		  } 
	}
}
