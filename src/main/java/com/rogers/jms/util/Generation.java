package com.rogers.jms.util;

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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.namespace.NamespaceContext;
import javax.xml.XMLConstants;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;


public class Generation {
	
	private static Properties props = ConfigLoader.load();
    private static final Logger logger = LoggerFactory.getLogger(Generation.class);

	private static final List<String> SAM_KEYS;
	private static final AtomicInteger SAMKEY_INDEX = new AtomicInteger(0);

	
	static {
	    String rawValue = props.getProperty("samkey.values", "");
	    if (rawValue.isBlank()) {
	        SAM_KEYS = List.of();
	    } 
	    else {
	        SAM_KEYS = Arrays.stream(rawValue.split(","))
	                .map(String::trim)
	                .filter(s -> !s.isEmpty())
	                .collect(Collectors.toList());
	    }
	}

	
	public static void main(String[] args) {

//		String orderxml = generateOrder();
//		WriteStringToFile(orderxml);
		
		try {
			generateOrder("/xml/TT_order.xml");
//			System.out.println(generateOrder("/xml/TT_order.xml").values());
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Map<String, String> generateOrder(String filePath) throws IOException {
		DocumentBuilderFactory builder;
		Document document = null;
		
		long startTime = System.nanoTime();
		Map<String, String> generatedOrder = new HashMap<String, String>();
		
		InputStream xmlStream = WebLogicJmsXmlSender.class.getResourceAsStream(filePath);
		
		if (xmlStream == null) {
		    throw new IOException("TT_order.xml not found on classpath");
		}
		
		try {
			builder = DocumentBuilderFactory.newInstance();
			builder.setNamespaceAware(true);
			document = builder.newDocumentBuilder().parse(xmlStream);
					
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
			e.printStackTrace();
		} finally {

		    long endTime = System.nanoTime();
		    long durationMillis = (endTime - startTime) / 1_000_000;

		    logger.info("Order Generated in {} ms ({} seconds)", durationMillis, durationMillis / 1000.0);

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
	                // Dummy implementation
	                return null; 
	            }

	            public Iterator<String> getPrefixes(String namespaceURI) {
	                // Dummy implementation
	                return null;
	            }
			});
			
			return xpath;
		}
			
		catch (Exception e) {
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

	        	RbtPair rbtPair = extractNameValue(el);

	            if (rbtPair != null && rbtPair.name != null && rbtPair.value != null) {
	                String value = rbtPair.value.getTextContent().trim();
	                if (!value.isEmpty()) {
	                    ctx.rbtPairs.add(rbtPair);
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

	        if (label.equals("OrderID")) {
	        	newVal = newVal + "-" + props.getProperty("testPrefix");
	        	ctx.referenceNumber = newVal;
	        }

	        logger.debug("Old {} value: {}", label, oldVal);
	        logger.debug("New {} value: {}", label, newVal);
	        logger.debug("Number of values changed: {}\n", count);

	        for (Element el : entry.getValue()) {
	            el.setTextContent(newVal);
	            totalReplaced++;
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
	    

	    if ("SamKey".equals(name)) {
	        repl = generateSamKeyReplacement(index);
	    }
	    else if ("HHID".equals(name)) {
	    	repl = generateReplacements(index, false, true);
	    }
	    else {
	    	repl = generateReplacements(index, false, false);	    	
	    }
	    applyReplacement(index, repl, name, ctx);
	}
	
	private static Map<String, String> generateSamKeyReplacement(ValueIndex index) {

	    // Fallback to existing random logic if none configured
	    if (SAM_KEYS.isEmpty()) {
	    	logger.debug(System.lineSeparator());
	    	logger.debug("samkey.values is empty in loadtest.properties, generating a random samkey.");
	    	logger.debug("Add a comma seperated list of valid samkeys for the current test environment "
	    			+ "if needed, example: samkey.values = SAMKEY001,SAMKEY002,...\n");
	        return generateReplacements(index, false, false);
	    }

	    String originalSamkey = index.byValue.keySet().iterator().next();

	    // Round robin through configured values
	    String newSamKey = SAM_KEYS.get(Math.abs(SAMKEY_INDEX.getAndIncrement() % SAM_KEYS.size()));

	    Map<String, String> repl = new HashMap<>();
	    repl.put(originalSamkey, newSamKey);
	    return repl;
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

	    ValueIndex apidIndex = new ValueIndex();

	    // 1. Collect all APID-related elements
	    for (Element affectedProduct : ctx.affectedProducts) {

	        NodeList children = affectedProduct.getChildNodes();
	        for (int i = 0; i < children.getLength(); i++) {
	            Node childNode = children.item(i);
	            if (childNode.getNodeType() != Node.ELEMENT_NODE) continue;

	            Element childElement = (Element) childNode;
	            String localName = childElement.getLocalName();

	            // <affectedProduct><ID>
	            if ("ID".equals(localName)) {
	                apidIndex.add(childElement);
	            }

	            // <characteristicValues>
	            if ("characteristicValues".equals(localName)) {
	            	
	                // Using RbtPair here but childElement is actually a <characteristicValues> element
	                RbtPair rbtPair = extractNameValue(childElement);
	                
	                if (rbtPair != null && rbtPair.name != null && rbtPair.value != null) {
	                	String nameText = rbtPair.name;
	                	
	                    if ("APID".equals(nameText) || "REF_AP_ID".equals(nameText)) {
	                        apidIndex.add(rbtPair.value);
	                    }
	                }
	                else {
	                	logger.error("Could not extract name value pair from charactersiticValues element");
	                }
	            }
	        }
	    }

	    Map<String, String> replacements = generateReplacements(apidIndex, false, false);

	    applyReplacement(apidIndex, replacements, "APID", ctx);
	}
	
	private static RbtPair extractNameValue(Element characteristicValues) {
	    Element name = null, value = null;
	    NodeList kids = characteristicValues.getChildNodes();

	    for (int i = 0; i < kids.getLength(); i++) {
	        Node node = kids.item(i);
	        if (node.getNodeType() != Node.ELEMENT_NODE) continue;

	        if ("name".equals(node.getLocalName())) {
	        	name = (Element) node;
	        }
	        else if ("value".equals(node.getLocalName())) {
	        	value = (Element) node;
	        }
	    }

	    if (name != null && value != null) {
	        return new RbtPair(name.getTextContent().trim(), value);
	    }
	    return null;
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
        long max = (long) Math.pow(10, length);

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
	
	    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
	
	    if (nodeList != null) {
	    	for (int i=0; i < nodeList.getLength(); i++) {
	    		Node nodeFromList = nodeList.item(i);
	    	    transformer.transform(new DOMSource(nodeFromList), new StreamResult(System.out));
	    	}
	    }
	    else if (node != null) {
		    transformer.transform(new DOMSource(node), new StreamResult(System.out));
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