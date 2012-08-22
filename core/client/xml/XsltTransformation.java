package xml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.*;

public class XsltTransformation {
	public static void generate(String xslSource, Source src, java.io.File res) throws Exception {
		generate(xslSource, src, res, new ArrayList());
	}
	
	public static void generate(String xslSource, Source src, java.io.File res, List parameters) throws Exception {
		TransformerFactory factory = TransformerFactory.newInstance();
		Templates template = factory.newTemplates(new StreamSource(xslSource));
		Transformer transformer = template.newTransformer();
	
		Iterator it = parameters.iterator();
		while(it.hasNext())
			transformer.setParameter((String)it.next(), it.next());
		transformer.transform(src, new StreamResult(res));
	}
}
