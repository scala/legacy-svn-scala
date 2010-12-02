/*
 * package.scala
 *
 * Created on November 30, 2010, 23:14 PM
 *
 */

package ebay

import javax.xml.ws.handler.MessageContext

/**
 * @author Stephane Micheloud
 */
package object server {

  implicit def messageContextWrapper(mc: MessageContext) = new {
    def getRequestHeaders: String =
      (mc get MessageContext.HTTP_REQUEST_HEADERS).asInstanceOf[String]
    def getRequestMethod: String =
      (mc get MessageContext.HTTP_REQUEST_METHOD).asInstanceOf[String]
    def getResponseCode: String =
      (mc get MessageContext.HTTP_RESPONSE_CODE).asInstanceOf[String]
    def getResponseHeaders: String =
      (mc get MessageContext.HTTP_RESPONSE_HEADERS).asInstanceOf[String]
    def getMessageOutbound: Boolean =
      (mc get MessageContext.MESSAGE_OUTBOUND_PROPERTY).asInstanceOf[Boolean]
    def getPathInfo: String =
      (mc get MessageContext.PATH_INFO).asInstanceOf[String]
    def getWSDLPort: String =
      (mc get MessageContext.WSDL_PORT).asInstanceOf[String]
    def getWSDLService: String =
      (mc get MessageContext.WSDL_SERVICE).asInstanceOf[String]
  }

}
