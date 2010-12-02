/*
 * ViewItemDetails.scala
 *
 * Adapted from ViewItemDetails.java, created on August 3, 2006, 9:37 PM
 *
 */

package ebay.client

import ebay.apis._
import java.awt.{Color, Font, Graphics}
import java.awt.event.ComponentEvent
import java.awt.image.BufferedImage
import javax.swing.{GroupLayout, JFrame, JLabel, JPanel, JScrollPane,
                    JSeparator, JTextPane, WindowConstants}
import javax.swing.LayoutStyle.ComponentPlacement

/**
 * @author Elancheran
 * @author Stephane Micheloud
 */
class ViewItemDetails(itemId: String, item: ItemType) extends JFrame {

  private var image: BufferedImage = _
  private var itemDetailString: String = _
  private val details = new Array[String](5)
  private val values = new Array[String](5)
  private var desc: String = _

  var currency: String = _
       
  setTitle("Item Id: "+itemId)
        
  if (item.getTitle != null) {
    itemDetailString = item.getTitle
    //println("Found item: " + itemDetailString)
  }
  if (item.getPictureDetails() != null) {
    //println("Picture url is " + item.getPictureDetails.getGalleryURL)
    try {
      val url = new java.net.URL(item.getPictureDetails.getGalleryURL)
      image = javax.imageio.ImageIO.read(url)
    } catch {
      case ie: java.io.IOException =>
        //ie.printStackTrace()
        //println("Image not found!!!")
        image = null
    }
  }
  if (item.getCurrency != null) {
    //println("Currency: " + item.getCurrency().value())
    currency = item.getCurrency().value()
  }
  if (item.getDescription != null) {
    //println("Description: " + item.getDescription)
    desc = item.getDescription
  }
  var i = 0
  if (item.getBestOfferDetails != null) {
    //println("Current bid: " + item.getBestOfferDetails())
    details(i) = "Current Bid: "
    if (currency != null){
      values(i) = currency + " " +
        String.valueOf(item.getBestOfferDetails().getBestOffer().getValue());
    } else {
      values(i) = String.valueOf(item.getBestOfferDetails()
                   .getBestOffer().getValue())
    }
    i += 1
  }

  if (item.getBuyItNowPrice() != null) {
    details(i) = "Buy it Now: "
    //println("Buy it now: " + item.getBuyItNowPrice().getValue())
    values(i) = String.valueOf(item.getBuyItNowPrice().getValue())
    if (currency != null)
      values(i) = values(i) +  " " + currency
    i += 1
  }

  if (item.getTimeLeft != null) {
    //println("End Time: " + item.getTimeLeft.toString)
    details(i) ="End Time: "
           
    var timeLeft = ""
    if (item.getTimeLeft().getDays != 0)
      timeLeft = item.getTimeLeft().getDays + " Days "
    if (item.getTimeLeft().getHours != 0)
      timeLeft = timeLeft + item.getTimeLeft().getHours + " Hrs "
    if (item.getTimeLeft().getMinutes() != 0)
      timeLeft = timeLeft + item.getTimeLeft().getMinutes + " Mins "
    if (item.getTimeLeft().getSeconds() != 0)
      timeLeft = timeLeft + item.getTimeLeft().getSeconds + " Secs "
              
    values(i) = timeLeft
    i += 1
  }
  if (item.getQuantity != null) {
    //println("Quantity: " + item.getQuantity)
    details(i) = "Quantity: "
    values(i) = String.valueOf(item.getQuantity)
    i += 1
  }
       
  if (item.getLocation != null) {
    //println("Location: " + item.getLocation)
    details(i) = "Location: "
    values(i) = item.getLocation
  }

  initComponents()
  setVisible(true)

  private def initComponents() {
    mainPanel = new JPanel()
    headerPanel = new JPanel()
    itemDetailsLabel = new JLabel()
    itemIdLabel = new JLabel()
    detailsPanel = new JPanel()
    priceLabel = new JLabel()
    amoutLabel = new JLabel()
    jSeparator1 = new JSeparator()
    detailLabel1 = new JLabel()
    valueLabel1 = new JLabel()
    detailLabel2 = new JLabel()
    valueLabel2 = new JLabel()
    detailLabel3 = new JLabel()
    valueLabel3 = new JLabel()
    detailLabel4 = new JLabel()
    valueLabel4 = new JLabel()
    jSeparator2 = new JSeparator()
    jScrollPane1 = new JScrollPane()
    descriptionPane = new JTextPane()

    descriptionPane setEditable false

    imagePanel = if (image != null) {
      new JPanel() {
        override def processComponentEvent(evt: ComponentEvent) {
          super.processComponentEvent(evt)
          if (evt.getID() == ComponentEvent.COMPONENT_RESIZED) {
            repaint()
          }
        }
        override def paintComponent(g: Graphics) {
          super.paintComponent(g)
          g.drawImage(image, 0, 0, this.getWidth, this.getHeight, this)
        }
      }
    } else {
      new JPanel()
    }

    initComponentsData()

    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
    setResizable(false)

    itemDetailsLabel setFont new Font("Verdana", 1, 12)
    itemIdLabel setFont new Font("Verdana", 0, 12)

    val headerPanelLayout = new GroupLayout(headerPanel)
    headerPanel setLayout headerPanelLayout
    headerPanelLayout.setHorizontalGroup(
      headerPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
        .addGroup(GroupLayout.Alignment.TRAILING, headerPanelLayout.createSequentialGroup()
          .addContainerGap()
          .addComponent(itemDetailsLabel, GroupLayout.DEFAULT_SIZE, 306, Short.MaxValue)
          .addPreferredGap(ComponentPlacement.RELATED)
          .addComponent(itemIdLabel, GroupLayout.PREFERRED_SIZE, 145, GroupLayout.PREFERRED_SIZE)
          .addContainerGap())
    )
    headerPanelLayout.setVerticalGroup(
      headerPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
        .addGroup(GroupLayout.Alignment.TRAILING, headerPanelLayout.createSequentialGroup()
          .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MaxValue)
          .addGroup(headerPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
            .addComponent(itemIdLabel)
            .addComponent(itemDetailsLabel))
          .addContainerGap())
    )

    imagePanel setBackground new Color(255, 255, 204)
    val imagePanelLayout = new GroupLayout(imagePanel)
    imagePanel setLayout imagePanelLayout
    imagePanelLayout setHorizontalGroup
      imagePanelLayout
        .createParallelGroup(GroupLayout.Alignment.LEADING)
        .addGap(0, 150, Short.MaxValue)
    imagePanelLayout setVerticalGroup
      imagePanelLayout
        .createParallelGroup(GroupLayout.Alignment.LEADING)
        .addGap(0, 166, Short.MaxValue)

    priceLabel setFont new Font("Verdana", 0, 11)
    amoutLabel setFont new Font("Verdana", 1, 11)
    jSeparator1 setForeground new Color(255, 255, 255)
    detailLabel1 setFont new Font("Verdana", 0, 11)
    valueLabel1 setFont new Font("Verdana", 0, 11)
    detailLabel2 setFont new Font("Verdana", 0, 11)
    valueLabel2 setFont new Font("Verdana", 0, 11)
    detailLabel3 setFont new Font("Verdana", 0, 11)
    valueLabel3 setFont new Font("Verdana", 0, 11)
    detailLabel4 setFont new Font("Verdana", 0, 11)
    valueLabel4 setFont new Font("Verdana", 0, 11)
    jSeparator2 setForeground new Color(255, 255, 255)
    descriptionPane setFont new Font("Verdana", 0, 11)
    descriptionPane setBackground new Color(236, 233, 216)
    jScrollPane1 setViewportView descriptionPane

    val detailsPanelLayout = new GroupLayout(detailsPanel)
    detailsPanel setLayout detailsPanelLayout
    detailsPanelLayout.setHorizontalGroup(
      detailsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
        .addGroup(detailsPanelLayout.createSequentialGroup()
          .addComponent(imagePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
          .addPreferredGap(ComponentPlacement.RELATED)
          .addGroup(detailsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
          .addGroup(detailsPanelLayout.createSequentialGroup()
            .addComponent(priceLabel, GroupLayout.PREFERRED_SIZE, 91, GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(ComponentPlacement.RELATED, 62, Short.MaxValue)
            .addComponent(amoutLabel, GroupLayout.PREFERRED_SIZE, 158, GroupLayout.PREFERRED_SIZE)
            .addContainerGap())
          .addComponent(jSeparator1, GroupLayout.DEFAULT_SIZE, 321, Short.MaxValue)
          .addGroup(detailsPanelLayout.createSequentialGroup()
            .addGroup(detailsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
              .addComponent(detailLabel2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MaxValue)
              .addComponent(detailLabel1, GroupLayout.DEFAULT_SIZE, 81, Short.MaxValue))
            .addPreferredGap(ComponentPlacement.RELATED, 66, Short.MaxValue)
            .addGroup(detailsPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
              .addComponent(valueLabel2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MaxValue)
              .addComponent(valueLabel1, GroupLayout.DEFAULT_SIZE, 164, Short.MaxValue)
              .addComponent(valueLabel3, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 164, GroupLayout.PREFERRED_SIZE)
              .addComponent(valueLabel4, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 164, GroupLayout.PREFERRED_SIZE))
            .addContainerGap())
          .addGroup(detailsPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
            .addComponent(detailLabel4, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MaxValue)
            .addComponent(detailLabel3, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MaxValue))))
            .addComponent(jSeparator2, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 477, Short.MaxValue)
            .addGroup(detailsPanelLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 461, Short.MaxValue)
                .addContainerGap())
        )
        detailsPanelLayout.setVerticalGroup(
            detailsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(detailsPanelLayout.createSequentialGroup()
                .addGroup(detailsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(imagePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addGroup(detailsPanelLayout.createSequentialGroup()
                        .addGap(11, 11, 11)
                        .addGroup(detailsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(priceLabel)
                            .addComponent(amoutLabel))
                        .addGap(14, 14, 14)
                        .addComponent(jSeparator1, GroupLayout.PREFERRED_SIZE, 10, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(detailsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(detailLabel1)
                            .addComponent(valueLabel1))
                        .addGap(15, 15, 15)
                        .addGroup(detailsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(detailLabel2)
                            .addComponent(valueLabel2))
                        .addGap(19, 19, 19)
                        .addGroup(detailsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(detailLabel3)
                            .addComponent(valueLabel3))
                        .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MaxValue)
                        .addGroup(detailsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(valueLabel4)
                            .addComponent(detailLabel4))))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(jSeparator2, GroupLayout.PREFERRED_SIZE, 10, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 96, Short.MaxValue))
        );

    val mainPanelLayout = new GroupLayout(mainPanel)
    mainPanel setLayout mainPanelLayout
    mainPanelLayout.setHorizontalGroup(
      mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
        .addGroup(mainPanelLayout.createSequentialGroup()
          .addContainerGap()
          .addGroup(mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(detailsPanel, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MaxValue)
            .addComponent(headerPanel, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MaxValue))
          .addContainerGap())
    )
    mainPanelLayout.setVerticalGroup(
      mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
        .addGroup(mainPanelLayout.createSequentialGroup()
           .addContainerGap()
           .addComponent(headerPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
           .addPreferredGap(ComponentPlacement.RELATED)
           .addComponent(detailsPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MaxValue)
           .addContainerGap())
    )

    val layout = new GroupLayout(getContentPane)
    getContentPane setLayout layout
    layout.setHorizontalGroup(
      layout.createParallelGroup(GroupLayout.Alignment.LEADING)
        .addComponent(mainPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MaxValue)
    )
    layout.setVerticalGroup(
      layout.createParallelGroup(GroupLayout.Alignment.LEADING)
        .addComponent(mainPanel, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MaxValue)
    )
    pack() 
  }

  private def initComponentsData() {
    itemDetailsLabel.setText(this.itemDetailString)
    itemIdLabel.setText("Item Id: "+ this.itemId)
    var i = 0
    if (details(i) != null && values(i) != null) {
      priceLabel setText details(i)
      amoutLabel setText values(i)
      i += 1
    }

    if (i < details.length && details(i) != null && values(i) != null ) {
      detailLabel1 setText details(i)
      valueLabel1 setText values(i)
      i += 1
    }

    if (i < details.length && details(i) != null && values(i) != null ) {
      detailLabel2 setText details(i)
      valueLabel2 setText values(i)
      i += 1
    }

    if (i < details.length && details(i) != null && values(i) != null ) {
      detailLabel3 setText details(i)
      valueLabel3 setText values(i)
      i += 1
    }

    if (i < details.length && details(i) != null && values(i) != null ) {
      detailLabel4 setText details(i)
      valueLabel4 setText values(i)
      i += 1
    }

    if (this.desc != null)
      descriptionPane.setText("Description:" + this.desc)
     else
      descriptionPane.setText("Description: None")
  }

  // Variables declaration - do not modify                     
  private var amoutLabel: JLabel = _
  private var descriptionPane: JTextPane = _
  private var detailLabel1: JLabel = _
  private var detailLabel2: JLabel = _
  private var detailLabel3: JLabel = _
  private var detailLabel4: JLabel = _
  private var detailsPanel: JPanel = _
  private var headerPanel: JPanel = _
  private var imagePanel: JPanel = _
  private var itemDetailsLabel: JLabel = _
  private var itemIdLabel: JLabel = _
  private var jScrollPane1: javax.swing.JScrollPane = _
  private var jSeparator1: JSeparator = _
  private var jSeparator2: JSeparator = _
  private var mainPanel: JPanel = _
  private var priceLabel: JLabel = _
  private var valueLabel1: JLabel = _
  private var valueLabel2: JLabel = _
  private var valueLabel3: JLabel = _
  private var valueLabel4: JLabel = _
  // End of variables declaration                   
}
