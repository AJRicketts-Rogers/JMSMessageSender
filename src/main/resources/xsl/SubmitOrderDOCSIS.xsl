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
				<ssm:order xmlns:ser="http://jms/engine/services" xmlns:ssm="http://xmlns.rogers.com/docsis/SSMessage">
					<ssm:messageId><xsl:value-of select="$CORR_ID"/></ssm:messageId>
					<ssm:correlationId>CORR<xsl:value-of select="$CORR_ID"/></ssm:correlationId>
					<ssm:orderActionCode><xsl:value-of select="$ACTION"/></ssm:orderActionCode>
					<ssm:priority><xsl:value-of select="$PRIORITY"/></ssm:priority>
					<ssm:lob>DTV</ssm:lob>
					<ssm:operator>SS</ssm:operator>
					<ssm:customerAccount>
						<ssm:canId><xsl:value-of select="$CAN"/></ssm:canId>
						<ssm:samKey>4<xsl:value-of select="$CAN"/></ssm:samKey>
					</ssm:customerAccount>
					<ssm:timestamp><xsl:value-of select="$EVENT_TIME"/></ssm:timestamp>
					<ssm:eaiTimestamp><xsl:value-of select="$EVENT_TIME"/></ssm:eaiTimestamp>
					<ssm:services>
						<ssm:service>
							<ssm:osmServiceType>DTV DOCSIS</ssm:osmServiceType>
							<ssm:upstreamBandwidth>410033</ssm:upstreamBandwidth>
							<ssm:downstreamBandwidth>555</ssm:downstreamBandwidth>
							<ssm:deviceStatus>up</ssm:deviceStatus>
							<ssm:maxCPE>2</ssm:maxCPE>
							<ssm:productCode>CISE</ssm:productCode>
							<ssm:serviceProvider>Rogers</ssm:serviceProvider>
							<ssm:serviceType>DTV DOCSIS</ssm:serviceType>
							<ssm:serviceLevel>stb cisco</ssm:serviceLevel>
							<ssm:customerType>Residential</ssm:customerType>
						</ssm:service>
						<ssm:service>
							<ssm:osmServiceType>ECM</ssm:osmServiceType>
							<ssm:describingSpecificationKey>ECM</ssm:describingSpecificationKey>
							<ssm:serialNumber>00100100</ssm:serialNumber>
							<ssm:make>SCIENTIFIC ATLANTA</ssm:make>
							<ssm:model>SA 8642HD3 PVR</ssm:model>
							<ssm:macAddress><xsl:value-of select="$MACADDR"/></ssm:macAddress>
						</ssm:service>
					</ssm:services>
				</ssm:order>
	      </ord:CreateOrder>
		</soapenv:Body>
	</soapenv:Envelope>
</xsl:template>
</xsl:stylesheet>