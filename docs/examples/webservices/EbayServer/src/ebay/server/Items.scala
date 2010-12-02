/*
 * Items.scala
 *
 * Created on November 30, 2010, 22:37 PM
 *
 */

package ebay.server

import ebay.apis.{AmountType, CurrencyCodeType, ItemType, PictureDetailsType}
import scala.xml.{Node, XML}

/**
 * @author Stephane Micheloud
 */
trait Items {

  protected val imagesRoot = "http://localhost:7070/ImageProvider/"
  protected val xmlFile = "items.xml"

  protected def loadItems: Map[String, ItemType] = {
    def newItemType(x: Node): ItemType = {
      val item = new ItemType()
      item setItemID (x \ "@id").text
      item setDescription (x \ "description").text
      item setTitle (x \ "title").text
      val amount = new AmountType()
      amount setValue (x \ "value").text.toDouble
      amount setCurrencyID CurrencyCodeType.fromValue((x \ "currency").text)
      item setBuyItNowPrice amount
      val picDetails = new PictureDetailsType()
      picDetails setGalleryURL imagesRoot+(x \ "fname").text
      item setPictureDetails picDetails
      item
    }
    val in = this.getClass.getClassLoader getResourceAsStream xmlFile
    val items = (XML load in) \ "item"
    var idx = 0
    Map[String, ItemType]() ++ (items flatMap (node => {
      idx += 1
      val item = newItemType(node)
      List(item.getItemID -> item, idx.toString -> item)
    }))
  }

}
