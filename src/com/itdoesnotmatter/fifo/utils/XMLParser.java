package com.itdoesnotmatter.fifo.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.itdoesnotmatter.fifo.metadata.Coordinate;

public class XMLParser {
	public static void getData(String stringToParse, DefaultHandler handler) throws ParserConfigurationException, SAXException, IOException {
		
		InputStream is = new ByteArrayInputStream(stringToParse.getBytes());

   	 	SAXParserFactory spf = SAXParserFactory.newInstance(); 			//Запуск фабрики
   	 	SAXParser sp = spf.newSAXParser();								//Получение экземпляра парсера
   	 	XMLReader xr = sp.getXMLReader(); 								//XML ридер
   	 	xr.setContentHandler(handler); 									//Установка обработчика, объявляемого в классе, который вызывает парсер
   	 	xr.parse(new InputSource (is)); 								//Запуск парсера
	}
	
	public static List<Coordinate> parseCoordinates(String stringToParse) {
		CoordinatesParserHandler handler = new CoordinatesParserHandler();
		try {
			XMLParser.getData(stringToParse, handler);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return handler.data;
	}

}
