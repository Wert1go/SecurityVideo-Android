package com.itdoesnotmatter.fifo.utils;

import java.util.List;
import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.itdoesnotmatter.fifo.metadata.Coordinate;

public class CoordinatesParserHandler extends DefaultHandler {
	private static final String response = "rsp";
	private static final String cell = "cell";
	
	public List<Coordinate> data;
	
	String thisElement = null;
    Coordinate coordinate;

    boolean isResponse = false;
    boolean isCoordinate = false;
    
    StringBuilder string = new StringBuilder("");
    public CoordinatesParserHandler() {
    	data = new Vector<Coordinate>();
    }
    
	@Override
    public void startElement(String uri, String localName, String qName,
    Attributes attributes) throws SAXException {
    	thisElement = localName;
    	
    	if (thisElement.equals(response)) {
    		
    		
    			
    		
    		isResponse = true;
    		coordinate = new Coordinate();
    	}
    	
    	if (thisElement.equals(cell)) {
    		isCoordinate = true;
    		
    		for (int i = 0; i < attributes.getLength(); i++) {
    			if (attributes.getLocalName(i).equalsIgnoreCase("lat")) {
    				coordinate.lat = Double.parseDouble(attributes.getValue(i));
    			} else if (attributes.getLocalName(i).equalsIgnoreCase("lon")) {
    				coordinate.lon = Double.parseDouble(attributes.getValue(i));
    			}
    		}
    	}
    	
    	
    }

    @Override
    public void endElement(String uri, String localName, String qName)
    throws SAXException {
    	if (localName.equals(response)) {
    		data.add(coordinate);
    		isResponse = false;
    	}
    	
    	if (localName.equals(cell)) {
    		isCoordinate = false;
    	}
    	
    	thisElement = "";
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {

    }
}
