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
		         <ng:order xsi:schemaLocation="http://xmlns.rogers.com/ngpp/SSRHPCMessage SSRHPCNGPPXMLSchema.xsd" xmlns:ng="http://xmlns.rogers.com/ngpp/SSRHPCMessage" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
		            <ng:messageId>MSG123456789001</ng:messageId>
		            <ng:blockingKey>RHPC_2390001283767</ng:blockingKey>
		            <ng:orderActionCode>CeaseAll</ng:orderActionCode>
		            <ng:priority>5</ng:priority>
		            <ng:lob>RHPC</ng:lob>
		            <ng:operator>SS</ng:operator>
		            <ng:subscriberManagementSystem>4</ng:subscriberManagementSystem>
		            <ng:customerAccount>
		               <ng:canId>123456789008</ng:canId>
		               <ng:samKey>2390001283766</ng:samKey>
		               <ng:firstName>Jimmy</ng:firstName>
		               <ng:lastName>Johnny</ng:lastName>
		               <ng:initials>JJ</ng:initials>
		               <ng:title>Mr</ng:title>
		               <ng:homeTelephone>1234567008</ng:homeTelephone>
		               <ng:businessTelephone>123456700800008</ng:businessTelephone>
		               <ng:city>CALEDON</ng:city>
		            </ng:customerAccount>
		            <ng:SSSenttimestamp>20151212-17:12:02</ng:SSSenttimestamp>
		            <ng:EAIReceivedSSTimestamp>20151212-17:15:02</ng:EAIReceivedSSTimestamp>
		            <ng:EAISentOSMTimestamp>20151212-17:15:02</ng:EAISentOSMTimestamp>
		            <ng:correlationId>COR123456789009</ng:correlationId>
		            <ng:services>
		               <ng:service>
		                  <ng:equipment>
		                     <ng:serialNumber>88DBME39R038496</ng:serialNumber>
		                     <ng:make>arris</ng:make>
		                     <ng:model>TM402g</ng:model>
		                     <ng:modelCode>42G</ng:modelCode>
		                     <ng:clliCode>NYRKONNVCA0</ng:clliCode>
		                     <ng:cmMacAddress>4013119a1aaa</ng:cmMacAddress>
		                     <ng:modemProductCode>WWMR</ng:modemProductCode>
		                     <ng:mtaMacAddress>4013119a1bbb</ng:mtaMacAddress>
		                  </ng:equipment>
		               </ng:service>
		               <ng:service>
		                  <ng:tnInfo>
		                     <ng:vtn1/>
		                     <ng:oldVtn1/>
		                     <ng:vtn2/>
		                     <ng:oldVtn2/>
		                     <ng:callDisplay411>
		                        <ng:firstname>Jimmy</ng:firstname>
		                        <ng:lastname>Johnny</ng:lastname>
		                     </ng:callDisplay411>
		                     <ng:tnProductCode>TEL1</ng:tnProductCode>
		                     <ng:vtn1ProductCode>TV01</ng:vtn1ProductCode>
		                     <ng:vtn2ProductCode>TV01</ng:vtn2ProductCode>
		                     <ng:tn>9055839980</ng:tn>
		                     <ng:exchange>PALGRAVE</ng:exchange>
		                     <ng:oldTn/>
		                     <ng:customerType>Residential</ng:customerType>
		                     <ng:lnpIdentifier>N</ng:lnpIdentifier>
		                     <ng:localRoutingNumber>4165551000</ng:localRoutingNumber>
		                     <ng:currentLDProvider>5066</ng:currentLDProvider>
		                     <ng:treatmentCode>UL</ng:treatmentCode>
		                     <ng:futureInterCIC/>
		                     <ng:interceptTreatment>1</ng:interceptTreatment>
		                     <ng:interceptAnnouncementReferenceTN/>
		                     <ng:currentLEC/>
		                     <ng:mtaVoicePort>1</ng:mtaVoicePort>
		                     <ng:timeZone>-05:00</ng:timeZone>
		                     <ng:faxUsageIndicator/>
		                     <ng:requestedLD/>
		                     <ng:ldEffectiveDate/>
		                     <ng:suspensionFlags/>
		                     <ng:languageCode/>
		                     <ng:featureParameters>
		                        <callBlockingPINResetIndicator/>
		                        <callForwardingRemoteAccessPINResetIndicator/>
		                        <warmLineForwardingTN/>
		                        <automaticLineForwardingTN/>
		                     </ng:featureParameters>
		                     <ng:callFeatureProductCode>
		                        <code>TP01</code>
		                     </ng:callFeatureProductCode>
		                  </ng:tnInfo>
		               </ng:service>
		            </ng:services>
		         </ng:order>
			</ord:CreateOrder>
		</soapenv:Body>
	</soapenv:Envelope>
</xsl:template>
</xsl:stylesheet>