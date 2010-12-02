/*
 * Copyright (c) 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package ebay.server

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, InputStream,
                OutputStream, StringReader}
import javax.activation.{DataHandler, DataSource}
import javax.annotation.Resource
import javax.jws.HandlerChain
import javax.xml.transform.{OutputKeys, Source, Transformer, TransformerFactory}
import javax.xml.transform.stream.{StreamResult, StreamSource}
import javax.xml.ws.{BindingType, Provider, Service, ServiceMode,
                     WebServiceContext, WebServiceProvider, WebServiceException}
import javax.xml.ws.handler.MessageContext
import javax.xml.ws.http.{HTTPBinding, HTTPException}

@WebServiceProvider
@BindingType(HTTPBinding.HTTP_BINDING)
@ServiceMode(value=Service.Mode.MESSAGE)
class ImageProvider extends Provider[DataSource] {

  @Resource
  protected var wsContext: WebServiceContext = _

  def invoke(ds: DataSource): DataSource = {
    val mc = wsContext.getMessageContext
    val method = mc.getRequestMethod
    if (method equals "GET")
      get(ds, mc)
	else
      throw new HTTPException(404)
  }

  private def get(source: DataSource, mc: MessageContext): DataSource = {
    val path = mc.getPathInfo
    //println("PathInfo = "+path)
    val image = "images" + (path match {
      case "/sun_blade_1000_h400px.jpg" |
           "/Sun_Fire_E20K_Server.jpg" |
           "/8d_2.jpg" => path
      case _ => throw new HTTPException(404)
    })
    new DataSource() {
      def getInputStream: InputStream =
        this.getClass.getClassLoader getResourceAsStream image
      def getOutputStream: OutputStream =
        null
      def getContentType: String =
        "image/jpeg"
      def getName: String =
        ""
    }
  }
}
