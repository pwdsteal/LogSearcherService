<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:template match="/">

	<HTML>
	<HEAD>
	  <style type="text/css">
   H3 { 
    font-size: 100%; /* ?????? ?????? */
    color: #DDDDDD; /* ???? ?????? */
   }
   body {
   font-family: Verdana, Arial, sans-serif; /* ????????? ?????? */
   background-color: #111111;
   }
   p {
	font-size: 80%; /* ?????? ?????? */
   color: #DDDDDD;
   }
   h1 {

   color: #DDDDDD;
   }
  </style>
	<TITLE>LogsSearcher</TITLE>
	</HEAD>
	<BODY>
		<xsl:for-each select="//server">
			<font><h1><xsl:value-of select="@name" /></h1></font>
			<xsl:for-each select="//logBlock">
				<h3 ><xsl:value-of select="date" /></h3>
				<p><xsl:value-of select="log" /></p>
			</xsl:for-each>
		</xsl:for-each>
	
	
	</BODY>
	</HTML>




</xsl:template>

</xsl:stylesheet>