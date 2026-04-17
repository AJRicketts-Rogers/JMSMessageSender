<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="1.0">
<xsl:param name="osm_ws_api_username"/>
<xsl:param name="osm_ws_api_password"/>
<xsl:param name="CORR_ID"/>
<xsl:param name="MSG_ID"/>
<xsl:param name="EVENT_TIME"/>
<xsl:param name="ACTION"/>
<xsl:param name="MACADDR"/>
<xsl:param name="CAN"/>
<xsl:param name="PRIORITY"/>
<xsl:output method="xml" indent="yes"/>
<xsl:template match="/">
	<soapenv:Envelope xmlns:ord="http://xmlns.oracle.com/communications/ordermanagement" xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
		<soapenv:Header>
			<wsse:Security soapenv:mustUnderstand="1" xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd">
				<wsse:UsernameToken wsu:Id="UsernameToken-1" xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd">
					<wsse:Username>
						<xsl:value-of select="$osm_ws_api_username"/>
					</wsse:Username>
					<wsse:Password Type="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText">
						<xsl:value-of select="$osm_ws_api_password"/>
					</wsse:Password>
				</wsse:UsernameToken>
			</wsse:Security>
		</soapenv:Header>
		<soapenv:Body>
	      <ord:CreateOrder>
	         <eai:OSMServiceOrderRequest xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:eai="http://www.rogers.com/eai/oss/osm/services" xmlns:comtypes="http://www.rogers.com/eai/oss/osm/commontypes">
	            <eai:ServiceOrderHeader>
	               <eai:serviceHeader>
	                  <com:apiClientId xmlns:com="http://www.rogers.com/eai/oss/osm/commontypes">EAI</com:apiClientId>
	                  <com:origin xmlns:com="http://www.rogers.com/eai/oss/osm/commontypes">SS</com:origin>
	                  <com:correlationId xmlns:com="http://www.rogers.com/eai/oss/osm/commontypes">CORR<xsl:value-of select="$CORR_ID"/></com:correlationId>
	                  <com:messageId xmlns:com="http://www.rogers.com/eai/oss/osm/commontypes"><xsl:value-of select="$CORR_ID"/></com:messageId>
	                  <com:batchGroup xmlns:com="http://www.rogers.com/eai/oss/osm/commontypes">0</com:batchGroup>
	                  <com:sequenceId xmlns:com="http://www.rogers.com/eai/oss/osm/commontypes">000001</com:sequenceId>
	                  <com:priority xmlns:com="http://www.rogers.com/eai/oss/osm/commontypes"><xsl:value-of select="$PRIORITY"/></com:priority>
	                  <com:state xmlns:com="http://www.rogers.com/eai/oss/osm/commontypes">INITIAL</com:state>
	                  <com:eventDateTime xmlns:com="http://www.rogers.com/eai/oss/osm/commontypes"><xsl:value-of select="$EVENT_TIME"/></com:eventDateTime>
	                  <com:srqAction xmlns:com="http://www.rogers.com/eai/oss/osm/commontypes"><xsl:value-of select="$ACTION"/></com:srqAction>
	                  <com:serviceType xmlns:com="http://www.rogers.com/eai/oss/osm/commontypes">DTV</com:serviceType>
	                  <com:clientEventRxDateTime xmlns:com="http://www.rogers.com/eai/oss/osm/commontypes"><xsl:value-of select="$EVENT_TIME"/></com:clientEventRxDateTime>
	                  <com:orderKey xmlns:com="http://www.rogers.com/eai/oss/osm/commontypes">
	                     <com:type>MACADDRESS</com:type>
	                     <com:primaryKey>54:D4:6F:39:05:6C</com:primaryKey>
	                  </com:orderKey>
	               </eai:serviceHeader>
	            </eai:ServiceOrderHeader>
	            <eai:ServiceItems>
	               <eai:requestItem>
	                  <com:serviceValue xmlns:com="http://www.rogers.com/eai/oss/osm/commontypes">
	                     <com:name>CAN</com:name>
	                     <com:value><xsl:value-of select="$CAN"/></com:value>
	                  </com:serviceValue>
	                  <com:serviceValue xmlns:com="http://www.rogers.com/eai/oss/osm/commontypes">
	                     <com:name>EC_ID</com:name>
	                     <com:value>SA00</com:value>
	                  </com:serviceValue>
	                  <com:serviceValue xmlns:com="http://www.rogers.com/eai/oss/osm/commontypes">
	                     <com:name>DHCT_DMS</com:name>
	                     <com:value>Y</com:value>
	                  </com:serviceValue>
	                  <com:serviceValue xmlns:com="http://www.rogers.com/eai/oss/osm/commontypes">
	                     <com:name>DHCT_DIS</com:name>
	                     <com:value>Y</com:value>
	                  </com:serviceValue>
	                  <com:serviceValue xmlns:com="http://www.rogers.com/eai/oss/osm/commontypes">
	                     <com:name>DHCT_ANALOG</com:name>
	                     <com:value>N</com:value>
	                  </com:serviceValue>
	                  <com:serviceValue xmlns:com="http://www.rogers.com/eai/oss/osm/commontypes">
	                     <com:name>DHCT_IPPV</com:name>
	                     <com:value>Y</com:value>
	                  </com:serviceValue>
	                  <com:serviceValue xmlns:com="http://www.rogers.com/eai/oss/osm/commontypes">
	                     <com:name>DHCT_MAX_IPPV</com:name>
	                     <com:value>10</com:value>
	                  </com:serviceValue>
	                  <com:serviceValue xmlns:com="http://www.rogers.com/eai/oss/osm/commontypes">
	                     <com:name>DHCT_CREDIT</com:name>
	                     <com:value>100</com:value>
	                  </com:serviceValue>
	                  <com:serviceValue xmlns:com="http://www.rogers.com/eai/oss/osm/commontypes">
	                     <com:name>DHCT_PIN_ENABLE</com:name>
	                     <com:value>Y</com:value>
	                  </com:serviceValue>
	                  <com:serviceValue xmlns:com="http://www.rogers.com/eai/oss/osm/commontypes">
	                     <com:name>DHCT_PIN</com:name>
	                     <com:value>0000</com:value>
	                  </com:serviceValue>
	                  <com:serviceValue xmlns:com="http://www.rogers.com/eai/oss/osm/commontypes">
	                     <com:name>DHCT_REFRESH</com:name>
	                     <com:value>N</com:value>
	                  </com:serviceValue>
	                  <com:serviceValue xmlns:com="http://www.rogers.com/eai/oss/osm/commontypes">
	                     <com:name>DHCT_LOC_X</com:name>
	                     <com:value>230</com:value>
	                  </com:serviceValue>
	                  <com:serviceValue xmlns:com="http://www.rogers.com/eai/oss/osm/commontypes">
	                     <com:name>DHCT_LOC_Y</com:name>
	                     <com:value>230</com:value>
	                  </com:serviceValue>
	                  <com:serviceValue xmlns:com="http://www.rogers.com/eai/oss/osm/commontypes">
	                     <com:name>DHCT_ADMIN_STATE</com:name>
	                     <com:value>2</com:value>
	                  </com:serviceValue>
	                  <com:serviceValue xmlns:com="http://www.rogers.com/eai/oss/osm/commontypes">
	                     <com:name>DHCT_INIT_IPG</com:name>
	                     <com:value>N</com:value>
	                  </com:serviceValue>
	                  <com:serviceValue xmlns:com="http://www.rogers.com/eai/oss/osm/commontypes">
	                     <com:name>DTA_RESET_DEREG</com:name>
	                     <com:value/>
	                  </com:serviceValue>
	                  <com:serviceValue xmlns:com="http://www.rogers.com/eai/oss/osm/commontypes">
	                     <com:name>DHCT_INSTANT_HIT</com:name>
	                     <com:value>N</com:value>
	                  </com:serviceValue>
	                  <com:serviceValue xmlns:com="http://www.rogers.com/eai/oss/osm/commontypes">
	                     <com:name>PACKAGE_TYPE</com:name>
	                     <com:value>T</com:value>
	                  </com:serviceValue>
	                  <com:serviceValue xmlns:com="http://www.rogers.com/eai/oss/osm/commontypes">
	                     <com:name>PACKAGE_NAME</com:name>
	                     <com:value>00001</com:value>
	                     <com:value>00002</com:value>
	                     <com:value>00073</com:value>
	                     <com:value>00074</com:value>
	                     <com:value>00075</com:value>
	                     <com:value>50000</com:value>
	                  </com:serviceValue>
	               </eai:requestItem>
	            </eai:ServiceItems>
	         </eai:OSMServiceOrderRequest>
	      </ord:CreateOrder>
		</soapenv:Body>
	</soapenv:Envelope>
</xsl:template>
</xsl:stylesheet>