<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="1.0">
<xsl:param name="osm_ws_api_username"/>
<xsl:param name="osm_ws_api_password"/>
<xsl:param name="JR_PRODUCT_ORDER_KEY"/>
<xsl:param name="PRIORITY"/>
<xsl:param name="JR_EQUIPMENT_KEY"/>
<xsl:param name="SERIAL_NUMBER"/>
<xsl:param name="MACADDR"/>
<xsl:output method="xml" indent="yes"/>
<xsl:template match="/">
<om-v1-0:createAndStartRequestByValueRequest xsi:schemaLocation="http://jacobsrimell.com/ossj/xml/OrderManagement/v2-0 http://jacobsrimell.com/ossj/xml/OrderManagement/v2-0/JR-OM-Schema.xsd http://jacobsrimell.com/ossj/xml/Inventory/v2-0 http://jacobsrimell.com/ossj/xml/Inventory/v2-0/JR-Inventory-Schema.xsd http://jacobsrimell.com/ossj/xml/Common/v2-0 http://jacobsrimell.com/ossj/xml/Common/v2-0/JR-Common-Schema.xsd http://jacobsrimell.com/ossj/xml/Common-CBEParty/v2-0 http://jacobsrimell.com/ossj/xml/Common-CBEParty/v2-0/JR-Party-Schema.xsd http://jacobsrimell.com/ossj/xml/Common-CBEResource/v2-0 http://jacobsrimell.com/ossj/xml/Common-CBEResource/v2-0/JR-Resource-Schema.xsd http://jacobsrimell.com/ossj/xml/Common-CBEProduct/v2-0 http://jacobsrimell.com/ossj/xml/Common-CBEProduct/v2-0/JR-Product-Schema.xsd http://jacobsrimell.com/ossj/xml/Common-CBECustomer/v2-0 http://jacobsrimell.com/ossj/xml/Common-CBECustomer/v2-0/JR-Customer-Schema.xsd" xmlns:om-v1-0="http://ossj.org/xml/OrderManagement/v1-0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://www.w3.org/2001/XMLSchema" xmlns:co-v1-5="http://ossj.org/xml/Common/v1-5" xmlns:cbebi-v1-5="http://ossj.org/xml/Common-CBEBi/v1-5" xmlns:jrbi-v2-0="http://jacobsrimell.com/ossj/xml/OrderManagement-BusinessInteraction/v2-0" xmlns:cbecore-v1-5="http://ossj.org/xml/Common-CBECore/v1-5" xmlns:cberesource-v1-5="http://ossj.org/xml/Common-CBEResource/v1-5" xmlns:cbedatatypes-v1-5="http://ossj.org/xml/Common-CBEDatatypes/v1-5" xmlns:cbeproduct-v1-5="http://ossj.org/xml/Common-CBEProduct/v1-5" xmlns:cbeproductoffering-v1-5="http://ossj.org/xml/Common-CBEProductOffering/v1-5" xmlns:cbeservice-v1-5="http://ossj.org/xml/Common-CBEService/v1-5" xmlns:cbeparty-v1-5="http://ossj.org/xml/Common-CBEParty/v1-5" xmlns:jrresource-v2-0="http://jacobsrimell.com/ossj/xml/Common-CBEResource/v2-0" xmlns:jrcustomer-v2-0="http://jacobsrimell.com/ossj/xml/Common-CBECustomer/v2-0" xmlns:jrparty-v2-0="http://jacobsrimell.com/ossj/xml/Common-CBEParty/v2-0" xmlns:jrom-v2-0="http://jacobsrimell.com/ossj/xml/OrderManagement/v2-0" xmlns:cbecustomer-v1-5="http://ossj.org/xml/Common-CBECustomer/v1-5" xmlns:cbelocation-v1-5="http://ossj.org/xml/Common-CBELocation/v1-5" xmlns:jrproduct-v2-0="http://jacobsrimell.com/ossj/xml/Common-CBEProduct/v2-0" xmlns:jrcommon-v2-0="http://jacobsrimell.com/ossj/xml/Common/v2-0">---
	<om-v1-0:requestValue xsi:type="jrom-v2-0:JrResourceOrderValue" xmlns:xs="http://www.w3.org/2001/XMLSchema">
		<co-v1-5:key xsi:type="jrom-v2-0:JrResourceOrderKey">
			<co-v1-5:type>JrResourceOrderKey</co-v1-5:type>
			<co-v1-5:primaryKey>GW-<xsl:value-of select="$JR_PRODUCT_ORDER_KEY"/></co-v1-5:primaryKey>
		</co-v1-5:key>
		<jrom-v2-0:priority_Request><xsl:value-of select="$PRIORITY"/></jrom-v2-0:priority_Request>
		<om-v1-0:resourceOrderItems>
			<om-v1-0:item xsi:type="jrom-v2-0:JrResourceOrderItemValue">
				<cbebi-v1-5:action>create</cbebi-v1-5:action>
				<om-v1-0:resource xsi:type="jrresource-v2-0:JrEquipmentValue">
					<co-v1-5:key xsi:type="jrresource-v2-0:JrEquipmentKey">
						<co-v1-5:type>JrEquipmentKey</co-v1-5:type>
						<co-v1-5:primaryKey><xsl:value-of select="$JR_EQUIPMENT_KEY"/></co-v1-5:primaryKey>
					</co-v1-5:key>
					<jrresource-v2-0:make>redback</jrresource-v2-0:make>
					<jrresource-v2-0:model>netop1000</jrresource-v2-0:model>
					<jrresource-v2-0:serialNumber><xsl:value-of select="$SERIAL_NUMBER"/></jrresource-v2-0:serialNumber>
					<jrresource-v2-0:JrNetworkElements>
						<jrresource-v2-0:item xsi:type="jrresource-v2-0:JrCableModemNetworkElementValue">
							<cbecore-v1-5:describingSpecificationKey xsi:type="jrresource-v2-0:JrResourceSpecificationKey">
								<co-v1-5:type>JrResourceSpecificationKey</co-v1-5:type>
								<co-v1-5:primaryKey>CM</co-v1-5:primaryKey>
							</cbecore-v1-5:describingSpecificationKey>
							<jrresource-v2-0:macAddress><xsl:value-of select="$MACADDR"/></jrresource-v2-0:macAddress>
						</jrresource-v2-0:item>
					</jrresource-v2-0:JrNetworkElements>
				</om-v1-0:resource>
			</om-v1-0:item>
		</om-v1-0:resourceOrderItems>
		<jrom-v2-0:operator>BT</jrom-v2-0:operator>
	</om-v1-0:requestValue>
</om-v1-0:createAndStartRequestByValueRequest>
</xsl:template>
</xsl:stylesheet>