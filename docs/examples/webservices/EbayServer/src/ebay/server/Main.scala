/*
 * Main.scala
 *
 * Adapted from Main.java
 *
 */

package ebay.server

import javax.xml.ws.Endpoint

object Main {
  /**
   * @param args the command line arguments
   */
  def main(args: Array[String]) {
    // TODO code application logic here
    println("Starting endpoint with address http://localhost:7070/Ebay")
    Endpoint.publish("http://localhost:7070/Ebay", new Ebay())
    Endpoint.publish("http://localhost:7070/ImageProvider", new ImageProvider())
  }
}
