package plugins.moduleGeneration;

import org.jdom.Element;

public class ElementCreator {
final static int STRING = 0, INT = 1, LONG = 2, BYTE = 3, FLOAT = 4,
	DOUBLE = 5, BOOLEAN = 6, ENUM = 7, CLASS = 8, DICTIONARY = 9, 
	ARRAY = 10, TABLE = 11, INSTANCE = 12;

final String [][] content = {
	{"name", "length", "minLength", "maxLength", "description", "default"},
	{"name", "minValue", "maxValue", "description", "default"},
	{"name", "minValue", "maxValue", "description", "default"},
	{"name", "minValue", "maxValue", "description", "default"},
	{"name", "minValue", "maxValue", "description", "default"},
	{"name", "minValue", "maxValue", "description", "default"},
	{"name", "description", "default"},
	{"name", "description", "default"},
	{"name", "description"},
	{"name", "elementType", "size", "minSize", "maxSize"},
	{"name", "elementType", "anzCols", "minCols", "maxCols", "anzRows", "minRows", "maxRows"},
	{"name", "valueType", "description"},	
	{"name"}
};
	
final String [] type2name = {"string", "int", "long", "byte", "float", "double",
		"boolean", "enum", "class", "dictionary", "array", "table", "class"};
	public Element getElement(int type) {
		Element e = new Element(type2name[type]);
		for(int i=0; i<content[type].length; i++)
			e.addContent(new Element(content[type][i]));
		
		switch(type) {
		case ENUM: break;
		case CLASS: break;
		case ARRAY: break;
		case TABLE: break;
		case INSTANCE: break;
		}
		
		return e;
	}
}
