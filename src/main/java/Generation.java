//package Success;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.io.File;
import java.io.IOException;

import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.namespace.NamespaceContext;
import javax.xml.XMLConstants;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;


public class Generation {
	
    private static final Logger logger =
            LoggerFactory.getLogger(Generation.class);
	
	public static void main(String[] args) {

//		String orderxml = generateOrder();
//		WriteStringToFile(orderxml);
		generateOrder();
	}
	
	public static Map<String, String> generateOrder() {
		DocumentBuilderFactory builder;
		Document document = null;
		
		long startTime = System.nanoTime();
		Map<String, String> generatedOrder = new HashMap<String, String>();
		
		try {
			builder = DocumentBuilderFactory.newInstance();
			builder.setNamespaceAware(true);
			document = builder.newDocumentBuilder().parse(new File("src/main/java/TT_order.xml"));
					
			XPath xpath = getRoot(document);
			if (xpath == null) {
				throw new Exception("Error parsing xml");
			}

			document = prepareDocument(document, xpath);
			
			DomContext ctx = buildContext(document);

			replaceNamedValues(ctx, "OrderID");
			replaceNamedValues(ctx, "SamKey");
			replaceNamedValues(ctx, "WorkOrderId");
			replaceNamedValues(ctx, "HHID");

			replaceCBP(ctx, document.getDocumentElement());
			replaceSerialNumbers(ctx);
			replaceMac(ctx);

			Map<String, String> repl = generateReplacements(ctx.valueIndex, true, false);
			applyReplacement(ctx.valueIndex, repl, "OrderItemReferenceNumber", ctx);

			replaceAffectedProducts(ctx);
			
			generatedOrder.put(ctx.referenceNumber, documentToString(document));
		}			
		
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {

		    long endTime = System.nanoTime();
		    long durationMillis = (endTime - startTime) / 1_000_000;

		    logger.info("Program completed in {} ms ({} seconds)", durationMillis, durationMillis / 1000.0);

		}
		return generatedOrder;
	}
	
	public static XPath getRoot(Document document) {
		try {
			//Get an XPath object and evaluate the expression
			XPath xpath = XPathFactory.newInstance().newXPath();
			xpath.setNamespaceContext(new NamespaceContext() {
				public String getNamespaceURI(String prefix) {
					
					switch (prefix) {
				      	case "x": 
				      		return "http://www.w3.org/2001/XMLSchema-instance";
				        case "ng": 
				        	return  "http://ngpp.fulfilment.services.rogers.com";
				        case "a": 
				        	return "http://fulfilment.services.cust.oms.amdocs.com";
				        default:
				        	return XMLConstants.NULL_NS_URI;
				       }
				}
				
	            public String getPrefix(String namespaceURI) {
	                // Dummy implementation if not needed for your use case
	                return null; 
	            }

	            public Iterator<String> getPrefixes(String namespaceURI) {
	                // Dummy implementation if not needed for your use case
	                return null;
	            }
			});
			
			return xpath;
		}
			
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	private static class ValueIndex {
	    // Every element whose textContent == key
	    Map<String, List<Element>> byValue = new HashMap<>();

	    void add(Element el) {
	        String val = el.getTextContent();
	        if (val == null || val.isBlank()) return;
	        byValue.computeIfAbsent(val, k -> new ArrayList<>()).add(el);
	    }
	}
	
	private static void walkDOM(Node node, Consumer<Element> handler) {
	    if (node.getNodeType() == Node.ELEMENT_NODE) {
	        handler.accept((Element) node);
	    }

	    NodeList children = node.getChildNodes();
	    for (int i = 0; i < children.getLength(); i++) {
	        walkDOM(children.item(i), handler);
	    }
	}
	
	private static class RbtPair {
	    final String name;
	    final Element value;

	    RbtPair(String name, Element value) {
	        this.name = name;
	        this.value = value;
	    }
	}

	private static class DomContext {
	    ValueIndex valueIndex = new ValueIndex();

	    List<Element> orderItems = new ArrayList<>();
	    List<Element> affectedProducts = new ArrayList<>();

	    List<RbtPair> rbtPairs = new ArrayList<>();
	    String referenceNumber = null;
	}
	
	private static DomContext buildContext(Document doc) {
		DomContext ctx = new DomContext();

	    walkDOM(doc.getDocumentElement(), el -> {
	        String local = el.getLocalName();

	        // orderItems
	        if ("orderItems".equals(local)) {
	            ctx.orderItems.add(el);
	        }

	        // affectedProduct
	        if ("affectedProduct".equals(local)) {
	            ctx.affectedProducts.add(el);
	        }

	        // orderItemReferenceNumber
	        if ("orderItemReferenceNumber".equals(local)) {
	            ctx.valueIndex.add(el);
	        }

	        // externalID/key
	        if ("key".equals(local) && el.getParentNode() != null &&
	            "externalID".equals(el.getParentNode().getLocalName())) {

	            ctx.valueIndex.add(el);
	        }

	        // RBT / characteristicValues
	        if ("RBTCharacteristics".equals(local) ||
	            "characteristicValues".equals(local)) {

	            Element nameElement = null, valueElement = null;
	            NodeList kids = el.getChildNodes();

	            for (int i = 0; i < kids.getLength(); i++) {
	                Node childNode = kids.item(i);
	                
	                if (childNode.getNodeType() != Node.ELEMENT_NODE) continue;

	                if ("name".equals(childNode.getLocalName())) {
	                	nameElement = (Element) childNode;
	                }
	                else if ("value".equals(childNode.getLocalName())) {
	                	valueElement = (Element) childNode;
	                }
	            }

	            if (nameElement != null && valueElement != null) {
	                String name = nameElement.getTextContent().trim();
	                String value = valueElement.getTextContent().trim();
	                if (!value.isEmpty()) {
	                    ctx.rbtPairs.add(new RbtPair(name, valueElement));
	                }
	            }
	        }
	    });

	    return ctx;
	}
	
	private static Document prepareDocument(Document document, XPath xpath) throws XPathExpressionException {

		Node fulfillmentNode = (Node) xpath.evaluate(
		    "/ng:OMSOrder/a:FulfillmentOrderSvcRequestFulfillmentOrderInputs", document, XPathConstants.NODE);

		Element oldRoot = document.getDocumentElement();
		document.removeChild(oldRoot);
		document.appendChild(fulfillmentNode);
		
		return document;
	}

	
	private static Map<String, String> generateReplacements(ValueIndex index, boolean needsA, boolean isHHID) {
	    Map<String, String> replacements = new HashMap<>();

	    for (String oldVal : index.byValue.keySet()) {
	        // Preserve format: digits + 'A'
	    	String newVal = "";
	    	if (needsA) {
	    		newVal = generateRandomLong(oldVal.length() - 1) + "A";	    		
	    	}
	    	else if (isHHID) {
	    		String firstLetter = oldVal.substring(0, 1);
	    		newVal = firstLetter + generateRandomLong(oldVal.length() - 1);
	    	}
	    	else {
	    		newVal = Long.toString(generateRandomLong(oldVal.length()));
	    	}
	        replacements.put(oldVal, newVal);
	    }

	    return replacements;
	}
	
	private static int applyReplacement(ValueIndex index, Map<String, String> replacements, String label, DomContext ctx) {
	    int totalReplaced = 0;

	    for (Map.Entry<String, List<Element>> entry : index.byValue.entrySet()) {
	        String oldVal = entry.getKey();
	        String newVal = replacements.get(oldVal);
	        int count = entry.getValue().size();

	        logger.debug("Old {} value: {}", label, oldVal);
	        logger.debug("New {} value: {}", label, newVal);
	        logger.debug("Number of values changed: {}\n", count);

	        for (Element el : entry.getValue()) {
	            el.setTextContent(newVal);
	            totalReplaced++;
	        }
	        
	        if (label.equals("OrderID")) {
	        	ctx.referenceNumber = newVal;
	        }
	    }

	    return totalReplaced;
	}
	
	private static void replaceNamedValues(DomContext ctx, String name) {
	    ValueIndex index = new ValueIndex();

	    for (RbtPair pair : ctx.rbtPairs) {
	        if (name.equals(pair.name)) {
	            index.add(pair.value);
	        }
	    }

	    Map<String, String> repl = null;
	    
	    if ("HHID".equals(name)) {
	    	repl = generateReplacements(index, false, true);
	    }
	    else {
	    	repl = generateReplacements(index, false, false);	    	
	    }
	    applyReplacement(index, repl, name, ctx);
	}
	
	private static void replaceCBP(DomContext ctx, Node root) {
	    ValueIndex index = new ValueIndex();

	    walkDOM(root, el -> {
	        if ("Id".equals(el.getLocalName()) &&
	            el.getParentNode() != null &&
	            "customer".equals(el.getParentNode().getLocalName())) {

	            index.add(el);
	        }
	    });

	    Map<String, String> repl = generateReplacements(index, false, false);
	    applyReplacement(index, repl, "CBP", ctx);
	}
	
	private static void replaceSerialNumbers(DomContext ctx) {
	    ValueIndex index = new ValueIndex();

	    for (RbtPair pair : ctx.rbtPairs) {
	        if ("Serial_Number".equals(pair.name)) {
	            index.add(pair.value);
	        }
	    }

	    logger.debug("\n========== Serial Number Replacement ==========\n");

	    int count = 0;
	    for (List<Element> nodes : index.byValue.values()) {
	        for (Element el : nodes) {
	        	String oldVal = el.getTextContent();
	        	logger.debug("Old Serial_Number: {}", oldVal);
	        	String newVal = null;
	        	if (oldVal.matches("\\d++")) {
	        		newVal = String.valueOf(generateRandomLong(oldVal.length()));
	        	}
	        	else {
	        		newVal = generateRandomString(oldVal.length());
	        	}
	            logger.debug("New Serial_Number: {}", newVal);
	            el.setTextContent(newVal);
	            count++;
	        }
	    }

	    logger.debug("Number of values changed: {}\n", count);
	}

	private static void replaceAffectedProducts(DomContext ctx) {
	    logger.debug("\n========== Affected Product ID Replacement ==========\n");

	    for (Element ap : ctx.affectedProducts) {
	        Element id = null, apid = null;

	        NodeList kids = ap.getChildNodes();
	        for (int i = 0; i < kids.getLength(); i++) {
	            Node childNode = kids.item(i);
	            if (childNode.getNodeType() != Node.ELEMENT_NODE) continue;

	            if ("ID".equals(childNode.getLocalName())) id = (Element) childNode;
	            
	            if ("characteristicValues".equals(childNode.getLocalName())) {
	                Element nameElement = null, valueElement = null;
	                NodeList nestedChildNodes = childNode.getChildNodes();
	                
	                for (int j = 0; j < nestedChildNodes.getLength(); j++) {
	                    
	                	Node nestedChildNode = nestedChildNodes.item(j);
	                    if (nestedChildNode.getNodeType() != Node.ELEMENT_NODE) continue;
	                    
	                    if ("name".equals(nestedChildNode.getLocalName())) {
	                    	nameElement = (Element) nestedChildNode;
	                    }
	                    if ("value".equals(nestedChildNode.getLocalName())) {
	                    	valueElement = (Element) nestedChildNode;
	                    }
	                }
	                
	                if (nameElement != null && "APID".equals(nameElement.getTextContent())) {
	                	apid = valueElement;
	                }
	            }
	        }

	        if (id != null && apid != null) {
	            String oldVal = id.getTextContent();
	            String newVal = String.valueOf(generateRandomLong(oldVal.length()));

	            logger.debug("Old Affected Product id: {}", oldVal);
	            logger.debug("New Affected Product id: {}", newVal);

	            id.setTextContent(newVal);
	            apid.setTextContent(newVal);
	        }
	    }
	}
	
	private static void replaceMac(DomContext ctx) {
	    logger.debug("\n========== MAC Address Replacement ==========\n");

	    int count = 0;
	    for (RbtPair pair : ctx.rbtPairs) {
	        String name = pair.name;
	        if (name.contains("MAC_Address") || name.contains("Mac_Address")) {
	            String newMac = generateRandomString(12);
	            logger.debug("New Mac Address: {}", newMac);
	            pair.value.setTextContent(newMac);
	            count++;
	        }
	    }

	    logger.debug("\tNumber of values changed: {}\n", count);
	}
	
    public static long generateRandomLong(int length) {
        if (length < 1 || length > 19) {
            throw new IllegalArgumentException("Length must be between 1 and 19 for long type.");
        }
        long min = (long) Math.pow(10, length - 1);
        long max = (long) Math.pow(10, length); // The bound is exclusive, so use 10^length for max

        // nextLong(origin, bound) returns a number between origin (inclusive) and bound (exclusive)
        return ThreadLocalRandom.current().nextLong(min, max);
    }
    
    public static String generateRandomString(int length) {
    	String CHARACTERS = "ABCDEF0123456789";
    	Random rnd = new Random();
    	StringBuilder sb = new StringBuilder(length);
    	
    	for (int i = 0; i < length; i++) {
    		sb.append(CHARACTERS.charAt(rnd.nextInt(CHARACTERS.length())));
    	}
    	return sb.toString();
    }
	
	private static List<Node> convertNodeListToList(NodeList nodeList) {
		// Convert NodeList to List<Node>
		List<Node> list = new ArrayList<>(nodeList.getLength());
		
		for (int i = 0; i < nodeList.getLength(); i++) {
			list.add(nodeList.item(i));
		}
		
		return list;
	}
	
	public static String documentToString(Document doc) {
	    try {
	        StringWriter sw = new StringWriter();
	        TransformerFactory tf = TransformerFactory.newInstance();
	        Transformer transformer = tf.newTransformer();
	        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

	        transformer.transform(new DOMSource(doc), new StreamResult(sw));
	        return sw.toString();
	    } catch (Exception ex) {
	        throw new RuntimeException("Error converting to String", ex);
	    }
	}
	
	
	public static void WriteStringToFile(String str) {
        Path file = Paths.get("output.txt");

        try {
            // This will create a new file or overwrite an existing file
            Files.writeString(file, str);
            logger.info("Successfully wrote the string to the file.");
        } catch (IOException e) {
            logger.error("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
	}

	
	public static void prettyPrintNode(NodeList nodeList, Node node) throws Exception {
	    TransformerFactory tf = TransformerFactory.newInstance();
	    Transformer transformer = tf.newTransformer();
	
	    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	
	    // This property is required by most Java implementations
	    transformer.setOutputProperty(
	        "{http://xml.apache.org/xslt}indent-amount", "4"
	    );
	
	    if (nodeList != null) {
	    	for (int i=0; i < nodeList.getLength(); i++) {
	    		Node nodeFromList = nodeList.item(i);
	    	    transformer.transform(
	    		        new DOMSource(nodeFromList),
	    		        new StreamResult(System.out)
	    		    );
	    	}
	    }
	    else if (node != null) {
		    transformer.transform(
			        new DOMSource(node),
			        new StreamResult(System.out)
			    );
	    }

	}
	
	public static String prettyFormat(String input, int indent) {
	    try {
	        Source xmlInput = new StreamSource(new StringReader(input));
	        StringWriter stringWriter = new StringWriter();
	        StreamResult xmlOutput = new StreamResult(stringWriter);
	        TransformerFactory transformerFactory = TransformerFactory.newInstance();
	        transformerFactory.setAttribute("indent-number", indent);
	        transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
	        transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
	        Transformer transformer = transformerFactory.newTransformer(); 
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	        transformer.transform(xmlInput, xmlOutput);
	        return xmlOutput.getWriter().toString();
	    } catch (Exception e) {
	        throw new RuntimeException(e); // simple exception handling, please review it
	    }
	}

	public static String prettyFormat(String input) {
	    return prettyFormat(input, 2);
	}	
}