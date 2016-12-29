<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0"
      xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
      xmlns:fo="http://www.w3.org/1999/XSL/Format">
  <xsl:output method="xml" indent="yes"/>
  <xsl:template match="/">
    <fo:root>
      <fo:layout-master-set>
        <fo:simple-page-master master-name="A4-portrait"
              page-height="29.7cm" page-width="21.0cm" margin="2cm" >
          <fo:region-body/>
        </fo:simple-page-master>
      </fo:layout-master-set>
      <fo:page-sequence master-reference="A4-portrait" >
        <fo:flow flow-name="xsl-region-body" font-family="sans-serif"  >
		<fo:block space-before="1em" font-size="1.2em" font-weight="bold" text-indent="0em" text-align="center">
          <xsl:text>Siblion</xsl:text>
		 </fo:block>
		 
           <xsl:for-each select="//server">
			   <fo:block space-before="1em" font-size="1.2em" font-weight="bold" text-indent="0em">
					<xsl:value-of select="@name" />
				</fo:block>
			
				<xsl:for-each select="logBlock">
				<fo:block space-before="0.6em" font-size="0.45em">
					<xsl:value-of select="date" />
					<fo:block  font-size="1.1em" text-align="justify">
						<xsl:value-of select="log" />
					</fo:block>
				</fo:block>
				</xsl:for-each>
			
			</xsl:for-each>
			
          
        </fo:flow>
      </fo:page-sequence>
    </fo:root>
  </xsl:template>
</xsl:stylesheet>