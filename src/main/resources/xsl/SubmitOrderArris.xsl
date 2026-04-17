<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="1.0">
<xsl:param name="osm_ws_api_username"/>
<xsl:param name="osm_ws_api_password"/>
<xsl:param name="CORR_ID"/>
<xsl:param name="PRIORITY"/>
<xsl:param name="EVENT_TIME"/>
<xsl:param name="ACTION"/>
<xsl:param name="MACADDR"/>
<xsl:param name="CAN"/>
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
					<eai:ServiceOrderHeader>.
						<eai:serviceHeader>
						      <comtypes:apiClientId>EAI</comtypes:apiClientId>
						      <comtypes:origin>SS</comtypes:origin>
						      <comtypes:correlationId>CORR<xsl:value-of select="$CORR_ID"/></comtypes:correlationId>
						      <comtypes:messageId>MQ<xsl:value-of select="$CORR_ID"/></comtypes:messageId>
						      <comtypes:responseId>ARRIS<xsl:value-of select="$CORR_ID"/></comtypes:responseId>
						      <comtypes:batchGroup>0</comtypes:batchGroup>
						      <comtypes:sequenceId>0</comtypes:sequenceId>
						      <comtypes:priority>5</comtypes:priority>
						      <comtypes:state>INITIAL</comtypes:state>
						      <comtypes:eventDateTime><xsl:value-of select="$EVENT_TIME"/></comtypes:eventDateTime>
						      <comtypes:srqAction><xsl:value-of select="$ACTION"/></comtypes:srqAction>
						      <comtypes:serviceType>ARRIS</comtypes:serviceType>
						      <comtypes:orderKey>
						         <comtypes:type>CAN</comtypes:type>
						         <comtypes:primaryKey>Rogers_Brampton<xsl:value-of select="$CAN"/></comtypes:primaryKey>
						      </comtypes:orderKey>
						</eai:serviceHeader>
					</eai:ServiceOrderHeader>
		           <eai:ServiceItems>
		               <eai:requestItem>
		                  <comtypes:serviceValue>
		                     <comtypes:name>MARKET_ID</comtypes:name>
		                     <comtypes:value>Brampton</comtypes:value>
		                  </comtypes:serviceValue>
		                  <comtypes:serviceValue>
		                     <comtypes:name>HEADEND_ID</comtypes:name>
		                     <comtypes:value>PHE_ROGERS_BRAMPTON</comtypes:value>
		                  </comtypes:serviceValue>
		                  <comtypes:serviceValue>
		                     <comtypes:name>CHANNELMAP_ID</comtypes:name>
		                     <comtypes:value>CMAP_ROGERS_LAB</comtypes:value>
		                  </comtypes:serviceValue>
		                  <comtypes:serviceValue>
		                     <comtypes:name>SERVICE_CODES</comtypes:name>
		                     <comtypes:value>MOXI_BASIC</comtypes:value>
		                  </comtypes:serviceValue>
		                  <comtypes:serviceValue>
		                     <comtypes:name>GW_MACADDR</comtypes:name>
		                     <comtypes:value>001DD03C8B41</comtypes:value>
		                  </comtypes:serviceValue>
		                  <comtypes:serviceValue>
		                     <comtypes:name>MP_MACADDR</comtypes:name>
		                     <comtypes:value>001DD5E57565</comtypes:value>
		                  </comtypes:serviceValue>
		               </eai:requestItem>
		            </eai:ServiceItems>
 				</eai:OSMServiceOrderRequest>
			</ord:CreateOrder>
		</soapenv:Body>
	</soapenv:Envelope>
</xsl:template>
</xsl:stylesheet>