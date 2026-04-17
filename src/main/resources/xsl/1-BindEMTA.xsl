<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="1.0">
<xsl:param name="osm_ws_api_username"/>
<xsl:param name="osm_ws_api_password"/>
<xsl:param name="CORR_ID"/>
<xsl:param name="EVENT_TIME"/>
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
		         <ng:order xsi:schemaLocation="http://xmlns.rogers.com/ngpp/SSRHPCMessage SSRHPCNGPPXMLSchema.xsd" xmlns:ng="http://xmlns.rogers.com/ngpp/SSRHPCMessage" xmlns:xsi="http://www.w3.org/2008/XMLSchema-instance">
		            <ng:messageId><xsl:value-of select="$CORR_ID"/></ng:messageId>
		            <ng:blockingKey>RHPC_<xsl:value-of select="$CAN"/></ng:blockingKey>
		            <ng:orderActionCode>BindEMTA</ng:orderActionCode>
		            <ng:priority>5</ng:priority>
		            <ng:lob>RHPC</ng:lob>
		            <ng:operator>SS</ng:operator>
		            <ng:subscriberManagementSystem>4</ng:subscriberManagementSystem>
		            <ng:customerAccount>
		               <ng:canId><xsl:value-of select="$CAN"/></ng:canId>
		               <ng:samKey>2390001283766</ng:samKey>
		               <ng:firstName>John</ng:firstName>
		               <ng:lastName>Smith</ng:lastName>
		               <ng:initials>JS</ng:initials>
		               <ng:title>Mr</ng:title>
		               <ng:homeTelephone>1234567008</ng:homeTelephone>
		               <ng:businessTelephone>123456700800008</ng:businessTelephone>
		               <ng:city>CALEDON</ng:city>
		            </ng:customerAccount>
		            <ng:SSSenttimestamp><xsl:value-of select="$EVENT_TIME"/></ng:SSSenttimestamp>
		            <ng:EAIReceivedSSTimestamp><xsl:value-of select="$EVENT_TIME"/></ng:EAIReceivedSSTimestamp>
		            <ng:EAISentOSMTimestamp><xsl:value-of select="$EVENT_TIME"/></ng:EAISentOSMTimestamp>
		            <ng:correlationId>COR<xsl:value-of select="$CORR_ID"/></ng:correlationId>
		            <ng:services>
		               <ng:service>
		                  <ng:equipment>
		                     <ng:serialNumber>88DBME39R038496</ng:serialNumber>
		                     <ng:make>arris</ng:make>
		                     <ng:model>TM402g</ng:model>
		                     <ng:modelCode>42G</ng:modelCode>
		                     <ng:clliCode>NYRKONNVCA0</ng:clliCode>
		                     <ng:cmMacAddress>4013119A1AAA</ng:cmMacAddress>
		                     <ng:modemProductCode>WWMR</ng:modemProductCode>
		                     <ng:mtaMacAddress>4013119A1AAB</ng:mtaMacAddress>
		                  </ng:equipment>
		               </ng:service>
		            </ng:services>
		         </ng:order>
			</ord:CreateOrder>
		</soapenv:Body>
	</soapenv:Envelope>
</xsl:template>
</xsl:stylesheet>