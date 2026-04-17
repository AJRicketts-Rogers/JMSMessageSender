<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="1.0">
<xsl:param name="osm_ws_api_username"/>
<xsl:param name="osm_ws_api_password"/>
<xsl:param name="JR_PRODUCT_ORDER_KEY"/>
<xsl:param name="CBP"/>
<xsl:param name="SAM"/>
<xsl:param name="SERVICE_KEY"/>
<xsl:param name="W3ID"/>
<xsl:param name="PRIORITY"/>
<xsl:output method="xml" indent="yes"/>
<xsl:template match="/">
<om-v1-0:createAndStartRequestByValueRequest xmlns:om-v1-0="http://ossj.org/xml/OrderManagement/v1-0" xmlns:jrcustomer-v2-0="http://jacobsrimell.com/ossj/xml/Common-CBECustomer/v2-0" xmlns:cbeparty-v1-5="http://ossj.org/xml/Common-CBEParty/v1-5" xmlns:inv-v1-2="http://ossj.org/xml/Inventory/v1-2" xmlns:jrproduct-v2-0="http://jacobsrimell.com/ossj/xml/Common-CBEProduct/v2-0" xmlns:jrom-v2-0="http://jacobsrimell.com/ossj/xml/OrderManagement/v2-0" xmlns:cbeservice-v1-5="http://ossj.org/xml/Common-CBEService/v1-5" xmlns:co-v1-5="http://ossj.org/xml/Common/v1-5" xmlns:jrcommon-v2-0="http://jacobsrimell.com/ossj/xml/Common/v2-0" xmlns:jrresource-v2-0="http://jacobsrimell.com/ossj/xml/Common-CBEResource/v2-0" xmlns:cbecustomer-v1-5="http://ossj.org/xml/Common-CBECustomer/v1-5" xmlns:cberesource-v1-5="http://ossj.org/xml/Common-CBEResource/v1-5" xmlns:cbeuser-v1-5="http://ossj.org/xml/Common-CBEUser/v1-5" xmlns:cbebi-v1-5="http://ossj.org/xml/Common-CBEBi/v1-5" xmlns:cbeproductoffering-v1-5="http://ossj.org/xml/Common-CBEProductOffering/v1-5" xmlns:cbelocation-v1-5="http://ossj.org/xml/Common-CBELocation/v1-5" xmlns:jrparty-v2-0="http://jacobsrimell.com/ossj/xml/Common-CBEParty/v2-0" xmlns:jrinventory-v2-0="http://jacobsrimell.com/ossj/xml/Inventory/v2-0" xmlns:cbecore-v1-5="http://ossj.org/xml/Common-CBECore/v1-5" xmlns:cbedatatypes-v1-5="http://ossj.org/xml/Common-CBEDatatypes/v1-5" xmlns:cbereport-v1-5="http://ossj.org/xml/Common-CBEReport/v1-5" xmlns:ns8="http://ossj.org/xml/Common-CBEProduct/v1-5" xmlns:jrservice-v2-0="http://jacobsrimell.com/ossj/xml/Common-CBEService/v2-0">
	<om-v1-0:requestValue xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="jrom-v2-0:JrProductOrderValue">
		<co-v1-5:key xsi:type="jrom-v2-0:JrProductOrderKey">
			<co-v1-5:type>JrProductOrderKey</co-v1-5:type>
			<co-v1-5:primaryKey xmlns:ns25="http://www.w3.org/2001/XMLSchema" xsi:type="ns25:string">EMAIL-<xsl:value-of select="$JR_PRODUCT_ORDER_KEY"/></co-v1-5:primaryKey>
		</co-v1-5:key>
		<cbecore-v1-5:subGraphId xsi:nil="true"></cbecore-v1-5:subGraphId>
		<cbecore-v1-5:characterizedBy xsi:nil="true"></cbecore-v1-5:characterizedBy>
		<cbecore-v1-5:describedBy xsi:nil="true"></cbecore-v1-5:describedBy>
		<cbecore-v1-5:describingSpecificationKey xsi:nil="true"></cbecore-v1-5:describingSpecificationKey>
		<cbebi-v1-5:interactionDate xsi:nil="true"></cbebi-v1-5:interactionDate>
		<cbebi-v1-5:interactionDateComplete xsi:nil="true"></cbebi-v1-5:interactionDateComplete>
		<cbebi-v1-5:businessInteractionItemKeys xsi:nil="true"></cbebi-v1-5:businessInteractionItemKeys>
		<cbebi-v1-5:description xsi:nil="true"></cbebi-v1-5:description>
		<cbebi-v1-5:references xsi:nil="true"></cbebi-v1-5:references>
		<cbebi-v1-5:involvedLocations xsi:nil="true"></cbebi-v1-5:involvedLocations>
		<cbebi-v1-5:involvedRoles>
			<cbebi-v1-5:item xsi:type="om-v1-0:PartyInteractionRoleValue">
				<cbecore-v1-5:subGraphId xsi:nil="true"></cbecore-v1-5:subGraphId>
				<cbecore-v1-5:characterizedBy xsi:nil="true"></cbecore-v1-5:characterizedBy>
				<cbecore-v1-5:describedBy xsi:nil="true"></cbecore-v1-5:describedBy>
				<cbecore-v1-5:describingSpecificationKey xsi:nil="true"></cbecore-v1-5:describingSpecificationKey>
				<cbebi-v1-5:interactionRole xsi:nil="true"></cbebi-v1-5:interactionRole>
				<om-v1-0:identifiedBy xsi:type="jrcustomer-v2-0:JrConsumerValue">
					<co-v1-5:key xsi:type="jrcustomer-v2-0:JrConsumerKey">
						<co-v1-5:type>JrConsumerKey</co-v1-5:type>
						<co-v1-5:primaryKey xmlns:ns25="http://www.w3.org/2001/XMLSchema" xsi:type="ns25:string"><xsl:value-of select="$JR_PRODUCT_ORDER_KEY"/>HSD</co-v1-5:primaryKey>
					</co-v1-5:key>
					<cbecore-v1-5:subGraphId xsi:nil="true"></cbecore-v1-5:subGraphId>
					<cbecore-v1-5:characterizedBy xsi:nil="true"></cbecore-v1-5:characterizedBy>
					<cbecore-v1-5:describedBy xsi:nil="true"></cbecore-v1-5:describedBy>
					<cbecore-v1-5:describingSpecificationKey xsi:nil="true"></cbecore-v1-5:describingSpecificationKey>
					<cbeparty-v1-5:partyKey xsi:nil="true"></cbeparty-v1-5:partyKey>
					<cbeparty-v1-5:validFor xsi:nil="true"></cbeparty-v1-5:validFor>
					<cbecustomer-v1-5:customerRank xsi:nil="true"></cbecustomer-v1-5:customerRank>
					<jrcustomer-v2-0:party>
						<cbecore-v1-5:subGraphId xsi:nil="true"></cbecore-v1-5:subGraphId>
						<cbecore-v1-5:characterizedBy xsi:nil="true"></cbecore-v1-5:characterizedBy>
						<cbecore-v1-5:describedBy xsi:nil="true"></cbecore-v1-5:describedBy>
						<cbecore-v1-5:describingSpecificationKey xsi:nil="true"></cbecore-v1-5:describingSpecificationKey>
						<cbeparty-v1-5:validFor xsi:nil="true"></cbeparty-v1-5:validFor>
						<cbeparty-v1-5:individualPartyNames>
							<cbeparty-v1-5:item>
								<cbeparty-v1-5:formOfAddress xsi:nil="true"></cbeparty-v1-5:formOfAddress>
								<cbeparty-v1-5:givenName><xsl:value-of select="$CBP"/></cbeparty-v1-5:givenName>
								<cbeparty-v1-5:preferredGivenName xsi:nil="true"></cbeparty-v1-5:preferredGivenName>
								<cbeparty-v1-5:middleName xsi:nil="true"></cbeparty-v1-5:middleName>
								<cbeparty-v1-5:familyNamePrefix xsi:nil="true"></cbeparty-v1-5:familyNamePrefix>
								<cbeparty-v1-5:familyName><xsl:value-of select="$SAM"/></cbeparty-v1-5:familyName>
								<cbeparty-v1-5:familyGeneration xsi:nil="true"></cbeparty-v1-5:familyGeneration>
							</cbeparty-v1-5:item>
						</cbeparty-v1-5:individualPartyNames>
						<cbeparty-v1-5:organizationPartyNames xsi:nil="true"></cbeparty-v1-5:organizationPartyNames>
					</jrcustomer-v2-0:party>
					<jrcustomer-v2-0:subscriberManagementSystem>1</jrcustomer-v2-0:subscriberManagementSystem>
					<jrcustomer-v2-0:smsConsumerIdentifier><xsl:value-of select="$JR_PRODUCT_ORDER_KEY"/>HSD</jrcustomer-v2-0:smsConsumerIdentifier>
					<jrcustomer-v2-0:associatedBillingAccount>
						<co-v1-5:type>JrBillingAccountKey</co-v1-5:type>
						<co-v1-5:primaryKey xmlns:ns25="http://www.w3.org/2001/XMLSchema" xsi:type="ns25:string"><xsl:value-of select="$CBP"/></co-v1-5:primaryKey>
					</jrcustomer-v2-0:associatedBillingAccount>
				</om-v1-0:identifiedBy>
			</cbebi-v1-5:item>
		</cbebi-v1-5:involvedRoles>
		<jrom-v2-0:priority_Request><xsl:value-of select="$PRIORITY"/></jrom-v2-0:priority_Request>
		<om-v1-0:expectedCompletionDate xsi:nil="true"></om-v1-0:expectedCompletionDate>
		<om-v1-0:validFor xsi:nil="true"></om-v1-0:validFor>
		<om-v1-0:requestedCompletionDate xsi:nil="true"></om-v1-0:requestedCompletionDate>
		<om-v1-0:clientId xsi:nil="true"></om-v1-0:clientId>
		<om-v1-0:bulk xsi:nil="true"></om-v1-0:bulk>
		<om-v1-0:purchaseOrder xsi:nil="true"></om-v1-0:purchaseOrder>
		<om-v1-0:productOrderItems>
			<om-v1-0:item xsi:type="jrom-v2-0:JrProductOrderItemValue">
				<cbecore-v1-5:subGraphId xsi:nil="true"></cbecore-v1-5:subGraphId>
				<cbecore-v1-5:characterizedBy xsi:nil="true"></cbecore-v1-5:characterizedBy>
				<cbecore-v1-5:describedBy xsi:nil="true"></cbecore-v1-5:describedBy>
				<cbecore-v1-5:describingSpecificationKey xsi:nil="true"></cbecore-v1-5:describingSpecificationKey>
				<cbebi-v1-5:action>create</cbebi-v1-5:action>
				<cbebi-v1-5:quantity xsi:nil="true"></cbebi-v1-5:quantity>
				<cbebi-v1-5:places xsi:nil="true"></cbebi-v1-5:places>
				<cbebi-v1-5:involvedRoles xsi:nil="true"></cbebi-v1-5:involvedRoles>
				<cbebi-v1-5:references xsi:nil="true"></cbebi-v1-5:references>
				<om-v1-0:product xsi:type="jrproduct-v2-0:JrProductValue">
					<co-v1-5:key xsi:type="jrproduct-v2-0:JrProductKey">
						<co-v1-5:type>JrProductKey</co-v1-5:type>
						<co-v1-5:primaryKey xmlns:ns25="http://www.w3.org/2001/XMLSchema" xsi:type="ns25:string">EMAIL-<xsl:value-of select="$SERVICE_KEY"/></co-v1-5:primaryKey>
					</co-v1-5:key>
					<cbecore-v1-5:subGraphId xsi:nil="true"></cbecore-v1-5:subGraphId>
					<cbecore-v1-5:characterizedBy xsi:nil="true"></cbecore-v1-5:characterizedBy>
					<cbecore-v1-5:describedBy>
						<cbecore-v1-5:item xsi:type="jrcommon-v2-0:JrCharacteristicValue">
							<cbecore-v1-5:value xsi:nil="true"></cbecore-v1-5:value>
							<cbecore-v1-5:characteristic>accessProduct</cbecore-v1-5:characteristic>
							<jrcommon-v2-0:values xmlns:ns25="http://www.w3.org/2001/XMLSchema" xsi:type="ns25:string">RESEXTREME</jrcommon-v2-0:values>
						</cbecore-v1-5:item>
						<cbecore-v1-5:item xsi:type="jrcommon-v2-0:JrCharacteristicValue">
							<cbecore-v1-5:value xsi:nil="true"></cbecore-v1-5:value>
							<cbecore-v1-5:characteristic>accountNumber</cbecore-v1-5:characteristic>
							<jrcommon-v2-0:values xmlns:ns25="http://www.w3.org/2001/XMLSchema" xsi:type="ns25:string"><xsl:value-of select="$SAM"/></jrcommon-v2-0:values>
						</cbecore-v1-5:item>
						<cbecore-v1-5:item xsi:type="jrcommon-v2-0:JrCharacteristicValue">
							<cbecore-v1-5:value xsi:nil="true"></cbecore-v1-5:value>
							<cbecore-v1-5:characteristic>emailPostalCode</cbecore-v1-5:characteristic>
							<jrcommon-v2-0:values xmlns:ns25="http://www.w3.org/2001/XMLSchema" xsi:type="ns25:string">L6R3E4</jrcommon-v2-0:values>
						</cbecore-v1-5:item>
						<cbecore-v1-5:item xsi:type="jrcommon-v2-0:JrCharacteristicValue">
							<cbecore-v1-5:value xsi:nil="true"></cbecore-v1-5:value>
							<cbecore-v1-5:characteristic>mailboxAddress</cbecore-v1-5:characteristic>
							<jrcommon-v2-0:values xmlns:ns25="http://www.w3.org/2001/XMLSchema" xsi:type="ns25:string">hsimaestro<xsl:value-of select="$SAM"/>@rogers.com</jrcommon-v2-0:values>
						</cbecore-v1-5:item>
						<cbecore-v1-5:item xsi:type="jrcommon-v2-0:JrCharacteristicValue">
							<cbecore-v1-5:value xsi:nil="true"></cbecore-v1-5:value>
							<cbecore-v1-5:characteristic>reclaimIndicator</cbecore-v1-5:characteristic>
							<jrcommon-v2-0:values xmlns:ns25="http://www.w3.org/2001/XMLSchema" xsi:type="ns25:string">N</jrcommon-v2-0:values>
						</cbecore-v1-5:item>
						<cbecore-v1-5:item xsi:type="jrcommon-v2-0:JrCharacteristicValue">
							<cbecore-v1-5:value xsi:nil="true"></cbecore-v1-5:value>
							<cbecore-v1-5:characteristic>confirmationPassword</cbecore-v1-5:characteristic>
							<jrcommon-v2-0:values xmlns:ns25="http://www.w3.org/2001/XMLSchema" xsi:type="ns25:string">49034903</jrcommon-v2-0:values>
						</cbecore-v1-5:item>
						<cbecore-v1-5:item xsi:type="jrcommon-v2-0:JrCharacteristicValue">
							<cbecore-v1-5:value xsi:nil="true"></cbecore-v1-5:value>
							<cbecore-v1-5:characteristic>emailFamilyName</cbecore-v1-5:characteristic>
							<jrcommon-v2-0:values xmlns:ns25="http://www.w3.org/2001/XMLSchema" xsi:type="ns25:string">first</jrcommon-v2-0:values>
						</cbecore-v1-5:item>
						<cbecore-v1-5:item xsi:type="jrcommon-v2-0:JrCharacteristicValue">
							<cbecore-v1-5:value xsi:nil="true"></cbecore-v1-5:value>
							<cbecore-v1-5:characteristic>emailGivenName</cbecore-v1-5:characteristic>
							<jrcommon-v2-0:values xmlns:ns25="http://www.w3.org/2001/XMLSchema" xsi:type="ns25:string">Sanity</jrcommon-v2-0:values>
						</cbecore-v1-5:item>
						<cbecore-v1-5:item xsi:type="jrcommon-v2-0:JrCharacteristicValue">
							<cbecore-v1-5:value xsi:nil="true"></cbecore-v1-5:value>
							<cbecore-v1-5:characteristic>environment</cbecore-v1-5:characteristic>
							<jrcommon-v2-0:values xmlns:ns25="http://www.w3.org/2001/XMLSchema" xsi:type="ns25:string">PROD</jrcommon-v2-0:values>
						</cbecore-v1-5:item>
						<cbecore-v1-5:item xsi:type="jrcommon-v2-0:JrCharacteristicValue">
							<cbecore-v1-5:value xsi:nil="true"></cbecore-v1-5:value>
							<cbecore-v1-5:characteristic>numberOfIpDevices</cbecore-v1-5:characteristic>
							<jrcommon-v2-0:values xmlns:ns25="http://www.w3.org/2001/XMLSchema" xsi:type="ns25:string">1</jrcommon-v2-0:values>
						</cbecore-v1-5:item>
						<cbecore-v1-5:item xsi:type="jrcommon-v2-0:JrCharacteristicValue">
							<cbecore-v1-5:value xsi:nil="true"></cbecore-v1-5:value>
							<cbecore-v1-5:characteristic>ConnectionProfileConfiguration</cbecore-v1-5:characteristic>
							<jrcommon-v2-0:values xsi:type="cbecore-v1-5:ArrayOfCharacteristicValue">
								<cbecore-v1-5:item xsi:type="jrcommon-v2-0:JrCharacteristicValue">
									<cbecore-v1-5:value xsi:nil="true"></cbecore-v1-5:value>
									<cbecore-v1-5:characteristic>MailProfile</cbecore-v1-5:characteristic>
									<jrcommon-v2-0:values xmlns:ns25="http://www.w3.org/2001/XMLSchema" xsi:type="ns25:string">Yahoo-1</jrcommon-v2-0:values>
								</cbecore-v1-5:item>
							</jrcommon-v2-0:values>
						</cbecore-v1-5:item>
					</cbecore-v1-5:describedBy>
					<cbecore-v1-5:describingSpecificationKey xsi:type="jrproduct-v2-0:JrProductSpecificationKey">
						<co-v1-5:type>JrProductSpecificationKey</co-v1-5:type>
						<co-v1-5:primaryKey xmlns:ns25="http://www.w3.org/2001/XMLSchema" xsi:type="ns25:string">Email</co-v1-5:primaryKey>
					</cbecore-v1-5:describingSpecificationKey>
					<ns8:name xsi:nil="true"></ns8:name>
					<ns8:description xsi:nil="true"></ns8:description>
					<ns8:productSerialNumber xsi:nil="true"></ns8:productSerialNumber>
					<ns8:validFor xsi:nil="true"></ns8:validFor>
					<ns8:refProductKey xsi:nil="true"></ns8:refProductKey>
					<ns8:refPlaceKeys xsi:nil="true"></ns8:refPlaceKeys>
				</om-v1-0:product>
				<om-v1-0:productOffering xsi:nil="true"></om-v1-0:productOffering>
				<om-v1-0:productSpecification xsi:nil="true"></om-v1-0:productSpecification>
			</om-v1-0:item>
		</om-v1-0:productOrderItems>
		<om-v1-0:failedProductOrderItems xsi:nil="true"></om-v1-0:failedProductOrderItems>
		<jrom-v2-0:operator>BT</jrom-v2-0:operator>
	</om-v1-0:requestValue>
</om-v1-0:createAndStartRequestByValueRequest>
</xsl:template>
</xsl:stylesheet>