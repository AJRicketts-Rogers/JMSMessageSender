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
				<ssm:order xmlns:ser="http://jms/engine/services"
				    xmlns:ssm="http://xmlns.rogers.com/ngpp/SSMessage">
				    <ssm:messageId><xsl:value-of select="$CORR_ID"/></ssm:messageId>
				    <ssm:blockingKey><xsl:value-of select="$CAN"/>HSIPLAN</ssm:blockingKey>
				    <ssm:orderActionCode><xsl:value-of select="$ACTION"/></ssm:orderActionCode>
				    <ssm:priority><xsl:value-of select="$PRIORITY"/></ssm:priority>
				    <ssm:lob>HSD</ssm:lob>
				    <ssm:operator>SS</ssm:operator>
				    <ssm:subscriberManagementSystem>4</ssm:subscriberManagementSystem>
				    <ssm:customerAccount>
				        <ssm:canId><xsl:value-of select="$CAN"/></ssm:canId>
				        <ssm:samKey>2<xsl:value-of select="$CAN"/></ssm:samKey>
				        <ssm:oldCanId/>
				        <ssm:oldSamKey/>
				        <ssm:firstName>DFSDS</ssm:firstName>
				        <ssm:lastName>SDFSDFSDF</ssm:lastName>
				        <ssm:initials/>
				        <ssm:homeTelephone>9055433543</ssm:homeTelephone>
				        <ssm:businessTelephone>000000000000000</ssm:businessTelephone>
				    </ssm:customerAccount>
				    <ssm:timestamp><xsl:value-of select="$EVENT_TIME"/></ssm:timestamp>
				    <ssm:eaiTimestamp><xsl:value-of select="$EVENT_TIME"/></ssm:eaiTimestamp>
				    <ssm:correlationId><xsl:value-of select="$CORR_ID"/></ssm:correlationId>
				    <ssm:services>
				        <ssm:service>
				            <ssm:osmServiceType>HSI</ssm:osmServiceType>
				            <ssm:upstreamBandwidth>5700000</ssm:upstreamBandwidth>
				            <ssm:downstreamBandwidth>34200000</ssm:downstreamBandwidth>
				            <ssm:erbaUpBandBurst>0</ssm:erbaUpBandBurst>
				            <ssm:erbaDownBandBurst>10485760</ssm:erbaDownBandBurst>
				            <ssm:maxErbaDownstream>104857600</ssm:maxErbaDownstream>
				            <ssm:deviceStatus>up</ssm:deviceStatus>
				            <ssm:maxCPE>2</ssm:maxCPE>
				            <ssm:productCode>WRJH</ssm:productCode>
				            <ssm:resetDevice>CM</ssm:resetDevice>
				            <ssm:serviceProvider>Rogers</ssm:serviceProvider>
				            <ssm:serviceType>HSD</ssm:serviceType>
				            <ssm:serviceLevel>SS_Express</ssm:serviceLevel>
				            <ssm:baccProfile>NA</ssm:baccProfile>
				            <ssm:customerType>Residential</ssm:customerType>
				            <ssm:describingSpecificationKey>Cable HSD</ssm:describingSpecificationKey>
				            <ssm:cleanUpUsers/>
				        </ssm:service>
				        <ssm:service>
				            <ssm:osmServiceType>Equipment</ssm:osmServiceType>
				            <ssm:resetDevice>CM</ssm:resetDevice>
				            <ssm:baccProfile>NA</ssm:baccProfile>
				            <ssm:describingSpecificationKey>CM</ssm:describingSpecificationKey>
				            <ssm:planDescribingSpecificationKey>Cable HSD</ssm:planDescribingSpecificationKey>
				            <ssm:serialNumber>09298745634</ssm:serialNumber>
				            <ssm:make>HITRON</ssm:make>
				            <ssm:model>CGN3-ROG</ssm:model>
				            <ssm:macAddress><xsl:value-of select="$MACADDR"/></ssm:macAddress>
				            <ssm:modemProductCode>WZMR</ssm:modemProductCode>
				        </ssm:service>
				    </ssm:services>
				</ssm:order>
			</ord:CreateOrder>
		</soapenv:Body>
	</soapenv:Envelope>
</xsl:template>
</xsl:stylesheet>