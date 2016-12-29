<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
 
  <xsl:output method="xml" indent="yes"/>
  <xsl:template match="/">
 
    <xsl:processing-instruction name="mso-application">
      <xsl:text>progid="Word.Document"</xsl:text>
    </xsl:processing-instruction>
    <w:wordDocument
      xmlns:w="http://schemas.microsoft.com/office/word/2003/wordml">
 
      <w:body>
        <xsl:for-each select="//server">
        <w:p>
          <w:r>
		      <w:rPr>
			  <w:sz w:val="36"/>
				<w:b/>
			</w:rPr>
            <w:t><xsl:value-of select="@name"/></w:t>

          </w:r>
		  </w:p>        
				<xsl:for-each select="//logBlock">
				<w:p>
				  <w:r>
				  <w:rPr><w:b/></w:rPr>

					<w:t><xsl:value-of select="date"/></w:t>

				    </w:r>
				  </w:p>  					
				<w:p>
				  <w:r>
					<w:t><xsl:value-of select="log"/></w:t>

	
				  </w:r>
				  </w:p>        
				</xsl:for-each>
				<xsl:text>&#xa;</xsl:text>
        </xsl:for-each>
		

		
      </w:body>
    </w:wordDocument>
 
  </xsl:template>
</xsl:stylesheet>