package com.rogers.jms.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.rogers.jms.util.WebLogicJmsXmlSender.OrderTemplate;

import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;


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
    private static final DocumentBuilderFactory DBF = DocumentBuilderFactory.newInstance();
    private static final TransformerFactory TF = TransformerFactory.newInstance();
    private static final Random RAND = new Random();
    static { DBF.setNamespaceAware(true); }

	private static final List<String> SAM_KEYS;
	private static final List<String> CBP_VALUES;
	private static final Path COMBO_INDEX_FILE = Paths.get(props.getProperty("combo.index.file", "combo-index.txt"));
	
	private static final AtomicLong COMBO_INDEX = new AtomicLong(0);
	private static final AtomicInteger SAMKEY_INDEX = new AtomicInteger(0);
	private static final int INDEX_FLUSH_EVERY = 200;
	
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

	static {
	    String raw = props.getProperty("cbp.values", "").trim();
	    if (raw.isEmpty()) {
	        CBP_VALUES = List.of();
	    } else {
	        CBP_VALUES = Arrays.stream(raw.split(","))
	                .map(String::trim)
	                .filter(s -> !s.isEmpty())
	                .collect(Collectors.toList());
	    }
	}
	
	static {
        if (SAM_KEYS.isEmpty() || CBP_VALUES.isEmpty()) {
        	logger.warn("");
            logger.warn("SamKey or CBP list is EMPTY. CBP and/or Samkey falling back to RANDOM generation.");
            logger.warn("**Uniqueness across orders is NOT guaranteed.**");
        }
	}
	
	static {
	    try {
	        if (Files.exists(COMBO_INDEX_FILE)) {
	            long start = Long.parseLong(Files.readString(COMBO_INDEX_FILE).trim());
	            COMBO_INDEX.set(start);
	            logger.info("Loaded SamKey/CBP combo index starting at {}\n", start);
	        }
	    } catch (Exception e) {
	        throw new RuntimeException("Failed to load combo index", e);
	    }
	}

	
	public static void main(String[] args) {
//		try {
//        	WebLogicJmsXmlSender.OrderTemplate tpl = WebLogicJmsXmlSender.getTemplateForType("3p");
//        	Map<String, String> order = generateOrder(tpl.xml, tpl.is3p);
//        	String formattedXML = prettyFormat(order.values().toArray()[0].toString());
//			System.out.println(formattedXML);
//			System.out.println(tpl.is3p);
//			
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}

	public static Map<String, String> generateOrder(String xml, boolean is3pOrder) throws IOException {

	    Document document;

	    long startTime = System.nanoTime();
	    Map<String, String> generatedOrder = new HashMap<>();

	    try (InputStream xmlStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))) {

	        document = DBF.newDocumentBuilder().parse(xmlStream);

	        XPath xpath = getRoot(document);
	        if (xpath == null) {
	            throw new Exception("Error parsing xml");
	        }

	        document = prepareDocument(document, xpath);

	        DomContext ctx = buildContext(document);

	        replaceNamedValues(ctx, "OrderID");
	        replaceNamedValues(ctx, "WorkOrderId");
	        replaceNamedValues(ctx, "HHID");
	        replaceNamedValues(ctx, "Associated_OA_ID");
	        replaceNamedValues(ctx, "CaseId");
	        replaceNamedValues(ctx, "Registration_Key");
	        
	        replaceorderItemReferenceNumbers(ctx);
	        replacePackageItems(ctx);
	        replacePhoneNumbers(ctx);
	        replaceSamKeyAndCBP(ctx, document, is3pOrder);
	        replaceSerialNumbers(ctx);
	        replaceMac(ctx);
	        replaceAffectedProducts(ctx);

	        
	        generatedOrder.put(ctx.referenceNumber, documentToString(document));

	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        long endTime = System.nanoTime();
	        long durationMillis = (endTime - startTime) / 1_000_000;
	        logger.debug(
	            "Order Generated in {} ms ({} seconds)",
	            durationMillis,
	            durationMillis / 1000.0
	        );
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
	
	private static Document prepareDocument(Document document, XPath xpath) throws XPathExpressionException {

		NodeList fulfillmentNodeList = (NodeList) xpath.evaluate("//a:FulfillmentOrderSvcRequestFulfillmentOrderInputs", document, XPathConstants.NODESET);

	    if (fulfillmentNodeList.getLength() == 0) {
	        throw new IllegalStateException("Expected element 'FulfillmentOrderSvcRequestFulfillmentOrderInputs' was not found, check xml.");
	    }
	    else if (fulfillmentNodeList.getLength() > 1) {
	    	throw new IllegalStateException("Found multiple 'FulfillmentOrderSvcRequestFulfillmentOrderInputs' elements, check xml.");
	    }

	    Node fulfillmentNode = fulfillmentNodeList.item(0);
	    Element currentRoot = document.getDocumentElement();
	    
	    if (fulfillmentNode.isSameNode(currentRoot)) {
	    	return document;
	    }
	    
	    Node imported = document.importNode(fulfillmentNode, true);
		document.removeChild(currentRoot);
		document.appendChild(imported);
		
		return document;
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
		
		List<Element> affectedProducts = new ArrayList<>();
		List<Element> customerIds = new ArrayList<>();
		List<Element> packageItemIds = new ArrayList<>();
		List<Element> serviceID_TN = new ArrayList<>();		
		
		List<RbtPair> rbtPairs = new ArrayList<>();
		String referenceNumber = null;
	}
	
	private static class Combo {
	    final String samKey;
	    final String cbp;

	    Combo(String samKey, String cbp) {
	        this.samKey = samKey;
	        this.cbp = cbp;
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
	
	private static DomContext buildContext(Document doc) {
		DomContext ctx = new DomContext();

	    walkDOM(doc.getDocumentElement(), el -> {
	        String local = el.getLocalName();

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
	        
	        if ("Id".equals(local) && el.getParentNode() != null &&
	        	    "packageItems".equals(el.getParentNode().getLocalName())) {
	        	
	        	    ctx.packageItemIds.add(el);
        	}
	    
		    if ("Id".equals(local) && el.getParentNode() != null
		            && "customer".equals(el.getParentNode().getLocalName())) {
		        ctx.customerIds.add(el);
		    }
		    
		    if ("serviceID".equals(local)) {
		    	if (el.getTextContent().startsWith("289")) {
		    		ctx.serviceID_TN.add(el);		    		
		    	}
		    }

		    });

	    return ctx;
	}
	
	private static Map<String, String> generateReplacements(ValueIndex index, boolean needsA, boolean isHHID, boolean isPhoneNumber) {
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
	    	else if (isPhoneNumber) {
	    		String areaCode = "289";
	    		int firstDigit = 2 + new Random().nextInt(8);
	    		newVal = areaCode + firstDigit + generateRandomLong(oldVal.length() - 4);
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
	        	newVal = props.getProperty("testPrefix") + "-" + newVal;
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
	    	if (name.equals("HHID")) {
	    		if ("HHID".equals(pair.name) || "W3ID".equals(pair.name)) {
	    			index.add(pair.value);	    			
	    		}
	    	}
	    	else if (name.equals(pair.name)){
	            index.add(pair.value);
	    	}
	    }

	    Map<String, String> repl = null;
	    
	    if ("HHID".equals(name)) {
	    	repl = generateReplacements(index, false, true, false);
	    }
	    else if ("Associated_OA_ID".equals(name)) {
	    	repl = generateReplacements(index, true, false, false);
	    }
	    else {
	    	repl = generateReplacements(index, false, false, false);	    	
	    }
	    applyReplacement(index, repl, name, ctx);
	}
	
	private static void replacePhoneNumbers(DomContext ctx) {
	    ValueIndex phoneIndex = new ValueIndex();

	    for (RbtPair pair : ctx.rbtPairs) {
	        if ("Main_TN".equals(pair.name) || "Phone_Number".equals(pair.name)) {
	            phoneIndex.add(pair.value);
	        }
	    }
	    
	    if (!ctx.serviceID_TN.isEmpty()) {
	    	phoneIndex.add(ctx.serviceID_TN.getFirst());	    	
	    }

	    Map<String, String> repl = generateReplacements(phoneIndex, false, false, true);

	    applyReplacement(phoneIndex, repl, "Phone Number", ctx);
	}
	
	private static void replacePackageItems(DomContext ctx) {
		ValueIndex index = new ValueIndex();
		for (Element el : ctx.packageItemIds) {
			index.add(el);
		}
		
	    Map<String, String> replacements = generateReplacements(index, false, false, false);

	    applyReplacement(index, replacements, "PackageItem ID", ctx);
	}
	
	private static void replaceorderItemReferenceNumbers(DomContext ctx) {
        Map<String, String> repl = generateReplacements(ctx.valueIndex, true, false, false);
        applyReplacement(ctx.valueIndex, repl, "orderItemReferenceNumber", ctx);
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
	            }
	        }
	    }

	    Map<String, String> replacements = generateReplacements(apidIndex, false, false, false);

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
	        if (name.toLowerCase().contains("mac")) {
	            String newMac = generateRandomString(12);
	            logger.debug("New Mac Address: {}", newMac);
	            pair.value.setTextContent(newMac);
	            count++;
	        }
	    }

	    logger.debug("\tNumber of values changed: {}\n", count);
	}
	
	private static void replaceSamKeyAndCBP(DomContext ctx, Document doc, boolean is3pOrder) {
    	Combo combo = nextCombo(is3pOrder);

	    // ---- SamKey (RBT) ----
	    ValueIndex samIndex = new ValueIndex();
	    for (RbtPair pair : ctx.rbtPairs) {
	        if ("SamKey".equals(pair.name)) {
	            samIndex.add(pair.value);
	        }
	    }

	    if (!samIndex.byValue.isEmpty()) {
	        applyReplacement(samIndex, Map.of(samIndex.byValue.keySet().iterator().next(), combo.samKey), "SamKey", ctx);
	    }

	    // ---- CBP (customer/Id) ----
	    ValueIndex cbpIndex = new ValueIndex();
	    for (Element el : ctx.customerIds) {
	        cbpIndex.add(el);
	    }


	    if (!cbpIndex.byValue.isEmpty()) {
	    	applyReplacement(cbpIndex, Map.of(cbpIndex.byValue.keySet().iterator().next(), combo.cbp), "CBP", ctx);	    		
	    }
	}
	
    public static long generateRandomLong(int length) {
//        if (length < 1 || length > 19) {
//        	logger.error("Length of value was {}, changing to 12", length);
//        	length = 12;
//        }
        long min = (long) Math.pow(10, length - 1);
        long max = (long) Math.pow(10, length);

        // nextLong(origin, bound) returns a number between origin (inclusive) and bound (exclusive)
        return ThreadLocalRandom.current().nextLong(min, max);
    }
    
    public static String generateRandomString(int length) {
    	String CHARACTERS = "ABCDEF0123456789";
    	StringBuilder sb = new StringBuilder(length);
    	
    	for (int i = 0; i < length; i++) {
    		sb.append(CHARACTERS.charAt(RAND.nextInt(CHARACTERS.length())));
    	}
    	return sb.toString();
    }
    
    private static Combo nextCombo(boolean is3pOrder) {

        //NON-3P orders: do NOT consume combo index
        if (!is3pOrder) {

            String samKey;
            String cbp;

            // SamKey: list if available, else random
            if (!SAM_KEYS.isEmpty()) {
                samKey = SAM_KEYS.get(Math.abs(SAMKEY_INDEX.getAndIncrement() % SAM_KEYS.size()));
            } else {
                samKey = Long.toString(generateRandomLong(15));
            }

            // CBP: ALWAYS random for non-3P
            cbp = Long.toString(generateRandomLong(9));

            return new Combo(samKey, cbp);
        }

        // 3P orders: enforce SamKey + CBP pairing
        if (SAM_KEYS.isEmpty() || CBP_VALUES.isEmpty()) {
            return new Combo(Long.toString(generateRandomLong(12)), Long.toString(generateRandomLong(9)));
        }

        long index = COMBO_INDEX.getAndIncrement();
        long max = (long) SAM_KEYS.size() * CBP_VALUES.size();

        if (index >= max) {
            throw new IllegalStateException("SamKey/CBP combinations exhausted, all " + max + " combinations used.");
        }

        int cbpCount = CBP_VALUES.size();
        int samIdx = (int) (index / cbpCount);
        int cbpIdx = (int) (index % cbpCount);

        // Occasionally write index to file
        if (index % INDEX_FLUSH_EVERY == 0) {
            try {
                Files.writeString(COMBO_INDEX_FILE, Long.toString(index),StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException("Failed to persist combo index", e);
            }
        }

        return new Combo(SAM_KEYS.get(samIdx), CBP_VALUES.get(cbpIdx));
    }
    
    public static long remainingSamKeyCbpCapacity() {

        // If either list is empty, we are falling back to generating random values
        if (SAM_KEYS.isEmpty() || CBP_VALUES.isEmpty()) {
            return Long.MAX_VALUE; // effectively unlimited
        }

        long total = (long) SAM_KEYS.size() * CBP_VALUES.size();
        long used = COMBO_INDEX.get();

        long remaining = total - used;
        return Math.max(remaining, 0);
    }
    
	public static String documentToString(Document doc) {
	    try {
	        StringWriter sw = new StringWriter();
	        Transformer transformer = TF.newTransformer();
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
	    	String cleanInput = input.replaceAll(">\\s+<", "><").trim();
	        Source xmlInput = new StreamSource(new StringReader(cleanInput));
	        StringWriter stringWriter = new StringWriter();
	        StreamResult xmlOutput = new StreamResult(stringWriter);
	        TransformerFactory transformerFactory = TransformerFactory.newInstance();
	        transformerFactory.setAttribute("indent-number", indent);
	        transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
	        transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
	        Transformer transformer = transformerFactory.newTransformer(); 
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
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