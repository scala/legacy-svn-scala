/*
 * RequesterCredentials.scala
 *
 * Adapted from RequesterCredentials.java, created on August 29, 2006, 11:02 PM
 *
 */

package ebay.client.handler

import ebay.apis._
import ebay.client._

import java.io.{FileNotFoundException, FileReader, IOException}
import java.util.Properties

import javax.xml.namespace.QName
import javax.xml.ws.handler.MessageContext
import javax.xml.ws.handler.soap.{SOAPHandler, SOAPMessageContext}

/**
 * @author Elancheran
 * @author Stephane Micheloud
 */
class RequesterCredentials extends SOAPHandler[SOAPMessageContext] {
  import RequesterCredentials._  // companion object

  def getHeaders: java.util.Set[QName] = null

  def handleMessage(smc: SOAPMessageContext): Boolean = {
    addRequesterCredentials(smc)
    true
  }

  def handleFault(smc: SOAPMessageContext): Boolean = true    

  def close(mc: MessageContext) {}

  private def addRequesterCredentials(smc: SOAPMessageContext) {
    val messageOutbound = smc.getMessageOutbound

    if (messageOutbound.booleanValue()) {
      val message = smc.getMessage
      try {
        var header = message.getSOAPHeader
        if (header == null) {
          message.getSOAPPart.getEnvelope.addHeader()
          header = message.getSOAPHeader
		}
        val heSecurity = header.addChildElement(
          "RequesterCredentials", "ebl", 
          "urn:ebay:apis:eBLBaseComponents")
        heSecurity
          .addChildElement("eBayAuthToken", "ebl", "urn:ebay:apis:eBLBaseComponents")
          .addTextNode(props.get("authToken").asInstanceOf[String])
        val userNameToken = heSecurity.addChildElement(
          "Credentials", "ebl",
          "urn:ebay:apis:eBLBaseComponents")
        userNameToken
          .addChildElement("AppId", "ebl", "urn:ebay:apis:eBLBaseComponents")
          .addTextNode(props.get("appID").asInstanceOf[String])
        userNameToken
          .addChildElement("DevId", "ebl", "urn:ebay:apis:eBLBaseComponents")
          .addTextNode(props.get("devID").asInstanceOf[String])
        userNameToken
          .addChildElement("AuthCert", "ebl", "urn:ebay:apis:eBLBaseComponents")
          .addTextNode(props.get("certID").asInstanceOf[String])
		//message writeTo Console.out
      } catch {
        case e: Exception => e.printStackTrace()
      }
    }
  }
}

object RequesterCredentials {

  private val props = new Properties()

  try {
    props load new FileReader("ebay.properties")
  } catch {
    case fne: FileNotFoundException =>
      println("Could not find ebay.properties")
      exit(1)
    case ioe: IOException =>
      println("Error reading ebay.properties " + ioe.getMessage)
      exit(1)
  }

}
