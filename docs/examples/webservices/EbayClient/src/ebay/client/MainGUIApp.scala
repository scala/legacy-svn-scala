/*
 * MainGUIApp.scala
 *
 * Adapted from MainGUIApp.java, created on August 2, 2006, 8:02 PM
 *
 */

package ebay.client

import ebay.apis._
import ebay.client.handler.RequesterCredentials;

import java.awt.{BorderLayout, Color, Container, Cursor, Font, Insets}
import java.awt.image.BufferedImage
import java.awt.event.{ActionEvent, ActionListener}
import java.util.Properties

import javax.xml.ws._
import javax.swing.{BorderFactory, ButtonGroup, DefaultComboBoxModel, GroupLayout,
                    JButton, JComboBox, JDialog, JFrame, JLabel,
                    JMenu, JMenuBar, JMenuItem, JOptionPane,
                    JPanel, JRadioButton, JTabbedPane, JTextField, UIManager,
                    SwingUtilities, WindowConstants}
import javax.swing.LayoutStyle.ComponentPlacement
import javax.xml.ws.handler.Handler

/**
 * @author Elancheran
 * @author Stephane Micheloud
 */
class MainGUIApp extends JFrame("Web Services Ebay Client") {
  import MainGUIApp._  // companion object

  private var image: BufferedImage = _
  private var itemDetailString: String = _
  private var itemId: String = _
  private val details = new Array[String](5)
  private var values = new Array[String](5)
  private var desc: String = _

  private var multiTabView = false

  try {
    UIManager setLookAndFeel UIManager.getSystemLookAndFeelClassName
    if (System.getProperty("http.proxyHost") != null &&
        System.getProperty("http.proxyPort") != null) {
      setProxyHost(System.getProperty("http.proxyHost"))
      setProxyPort(System.getProperty("http.proxyPort"))
    }
  } catch {
    case e: Exception => // do nothing
  }

  initComponents()

  private def initComponents() {
    jPanel2 = new JPanel()
    jPanel2 setBackground new Color(236, 233, 216)
    jTextField1 = new JTextField()
    jComboBox1 = new JComboBox()
    jLabel1 = new JLabel()
    jLabel2 = new JLabel()
    jPanel3 = new JPanel()
    jPanel3 setBackground new Color(236, 233, 216)
    jButton1 = new JButton()
    jMenuBar1 = new JMenuBar()
    jMenu1 = new JMenu()
    jMenu2 = new JMenu()
    jMenuItem1 = new JMenuItem()
    jMenu3 = new JMenu()
    jMenuItem2 = new JMenuItem()

    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
    setResizable(false)
    getContentPane setBackground new Color(236, 233, 216)

    jComboBox1 setFont verdana12
    jComboBox1 setModel new DefaultComboBoxModel(Array[Object]("Ebay Server", "Local Server"))
    jLabel1 setFont verdana12
    jLabel1 setText "Item Id"
    jLabel2 setFont verdana12
    jLabel2 setText "Server"

    val jPanel2Layout = new GroupLayout(jPanel2)
    jPanel2 setLayout jPanel2Layout
    jPanel2Layout.setHorizontalGroup(
      jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
        .addGroup(GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
          .addGap(50, 50, 50)
          .addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
            .addComponent(jLabel2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MaxValue)
            .addComponent(jLabel1, GroupLayout.DEFAULT_SIZE, 77, Short.MaxValue))
          .addPreferredGap(ComponentPlacement.RELATED)
          .addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(jComboBox1, 0, 113, Short.MaxValue)
            .addComponent(jTextField1, GroupLayout.DEFAULT_SIZE, 113, Short.MaxValue))
          .addContainerGap())
    )
    jPanel2Layout.setVerticalGroup(
      jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
        .addGroup(jPanel2Layout.createSequentialGroup()
          .addGap(23, 23, 23)
           .addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
             .addComponent(jLabel1)
             .addComponent(jTextField1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
             .addGap(15, 15, 15)
             .addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
               .addComponent(jLabel2)
               .addComponent(jComboBox1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
          .addContainerGap(22, Short.MaxValue))
    )

    jButton1 setFont verdana12
    jButton1 setText "Get Details"
    jButton1 setMnemonic 'G'
    jButton1 addActionListener new ActionListener() {
      def actionPerformed(evt: ActionEvent) {
        jButton1ActionPerformed(evt)
      }
    }

    val jPanel3Layout = new GroupLayout(jPanel3)
    jPanel3 setLayout jPanel3Layout
    jPanel3Layout.setHorizontalGroup(
      jPanel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
        .addGroup(jPanel3Layout.createSequentialGroup()
          .addGap(164, 164, 164)
          .addComponent(jButton1)
          .addContainerGap(164, Short.MaxValue))
    )
    jPanel3Layout.setVerticalGroup(
      jPanel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
        .addGroup(jPanel3Layout.createSequentialGroup()
          .addContainerGap()
          .addComponent(jButton1)
          .addContainerGap(86, Short.MaxValue))
    )

    jMenu1 setText "File"
    jMenu1 setMnemonic 'F'

    jMenuItem1 setText "Exit"
    jMenu1 add jMenuItem1
    jMenuItem1 setMnemonic 'x'

    jMenuItem1 addActionListener new ActionListener() {
      def actionPerformed(evt: ActionEvent) {
        exitApplication()
      }
    }

    jMenuBar1 add jMenu1

    jMenu3 setText "Edit"
    jMenu3 setMnemonic 'E'
    jMenuItem2 setText "Preferences"
    jMenuItem2 setMnemonic 'P'
    jMenuItem2 addActionListener new ActionListener() {
      def actionPerformed(evt: ActionEvent) {
        showPreferencesDialog()
      }
    }
    jMenu3 add jMenuItem2
    jMenuBar1 add jMenu3
    setJMenuBar(jMenuBar1)

    val layout = new GroupLayout(getContentPane)
    getContentPane setLayout layout
    layout.setHorizontalGroup(
      layout.createParallelGroup(GroupLayout.Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
          .addGap(76, 76, 76)
          .addComponent(jPanel2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
          .addContainerGap(93, Short.MaxValue))
        .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
          .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MaxValue)
          .addComponent(jPanel3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
    )
    layout.setVerticalGroup(
      layout.createParallelGroup(GroupLayout.Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
          .addGap(80, 80, 80)
          .addComponent(jPanel2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
          .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MaxValue)
          .addComponent(jPanel3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
    )
    pack()
  }

  private def jButton1ActionPerformed(evt: ActionEvent) {
    // TODO add your handling code here:
    var currency: String = null

    val source = evt.getSource
    if (source == jButton1) {
      val str = jComboBox1.getSelectedItem.asInstanceOf[String]
      if (str equals "Ebay Server") {
        if (getProxyHost != null && getProxyPort != null ){
          System.setProperty("http.proxyHost", getProxyHost)
          System.setProperty("http.proxyPort", getProxyPort)
          System.setProperty("https.proxyHost", getProxyHost)
          System.setProperty("https.proxyPort", getProxyPort)
        }
      }

      val itemId = jTextField1.getText
      if (itemId == null || itemId.equals("")) {
        showMessageDialog(jPanel2, "Please enter the ItemId...", "ItemId Required...")
      } else {
        this setCursor new Cursor(Cursor.WAIT_CURSOR)

        val item = getItem(if (str equals "Ebay Server") "ebay" else "local", itemId.trim())
        if (item == null) {
          showMessageDialog(jPanel2, "Sorry there is no item found...", "Item Not Found...")
        } else {
          new ViewItemDetails(itemId, item)
          jTextField1 setText ""
        }
        this setCursor new Cursor(Cursor.DEFAULT_CURSOR)
      }
    }
  }

  def exitApplication() {
    dispose()
    exit(0)
  }

  def showPreferencesDialog() {
    SwingUtilities invokeLater new Runnable() {
      def run(){
        new SettingsDialog() setVisible true
      }
    }
  }

  class SettingsDialog extends JDialog(MainGUIApp.this, "Preferences...", true)
                          with ActionListener {
    setLocationRelativeTo(MainGUIApp.this)
    initComponents()
    createGUI()

    private def initComponents() {
      jTabbedPane1 = new JTabbedPane()
      proxySettingsPanel = new JPanel()
      directRadioButton = new JRadioButton()
      proxyRadioButton = new JRadioButton()
      proxyHostLabel = new JLabel()
      proxyHostTxtField = new JTextField()
      proxyPortLabel = new JLabel()
      proxyPortTxtField = new JTextField()

      setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
      setResizable(false)

      proxyHostLabel setFont verdana12
      proxyPortLabel setFont verdana12
      directRadioButton setFont verdana12
      proxyRadioButton setFont verdana12

      if (MainGUIApp.getProxyHost != null && MainGUIApp.getProxyPort != null) {
        proxyRadioButton setSelected true
        proxyHostTxtField setText MainGUIApp.getProxyHost
        proxyPortTxtField setText MainGUIApp.getProxyPort
      } else {
        directRadioButton setSelected true
        proxyPortTxtField setEnabled false
        proxyHostTxtField setEnabled false
      }

      jTabbedPane1 setToolTipText "Proxy Settings..."
      jTabbedPane1 setName "Network"
      directRadioButton setText "Direct Internet Connection"
      //directRadioButton setSelected true
      directRadioButton setBorder BorderFactory.createEmptyBorder(0, 0, 0, 0)
      directRadioButton setMargin new Insets(0, 0, 0, 0)
      directRadioButton addActionListener this

      proxyRadioButton setText "User Network HTTP Proxy Settings"
      proxyRadioButton setToolTipText "Network Proxy Required..."
      proxyRadioButton setBorder BorderFactory.createEmptyBorder(0, 0, 0, 0)
      proxyRadioButton setMargin new Insets(0, 0, 0, 0)
      proxyRadioButton setName "proxyRadioButton"
      proxyRadioButton addActionListener this

      val networkButtonGroup = new ButtonGroup()
      networkButtonGroup add directRadioButton
      networkButtonGroup add proxyRadioButton

      proxyHostLabel setText "Proxy Host"

      proxyPortLabel setText "Port"

      bottomPanel = new JPanel()
      cancel = new JButton()
      ok = new JButton()
      cancel setText "Cancel"
      cancel setMnemonic 'C'
      ok setText "Ok"
      ok setMnemonic 'O'

      ok addActionListener this
      cancel addActionListener this
    }

    private def createGUI() {
      val proxySettingsPanelLayout = new GroupLayout(proxySettingsPanel)
      proxySettingsPanel setLayout proxySettingsPanelLayout
      proxySettingsPanelLayout.setHorizontalGroup(
        proxySettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
          .addGroup(proxySettingsPanelLayout.createSequentialGroup()
            .addGap(38, 38, 38)
            .addGroup(proxySettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(proxySettingsPanelLayout.createSequentialGroup()
            .addGroup(proxySettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
              .addComponent(proxyPortLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MaxValue)
              .addComponent(proxyHostLabel, GroupLayout.DEFAULT_SIZE, 71, Short.MaxValue))
              .addPreferredGap(ComponentPlacement.RELATED)
              .addGroup(proxySettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
              .addComponent(proxyPortTxtField, GroupLayout.PREFERRED_SIZE, 83, GroupLayout.PREFERRED_SIZE)
              .addComponent(proxyHostTxtField, GroupLayout.PREFERRED_SIZE, 147, GroupLayout.PREFERRED_SIZE)))
              .addComponent(proxyRadioButton)
              .addComponent(directRadioButton))
              .addContainerGap(54, Short.MaxValue))
      )
      proxySettingsPanelLayout.setVerticalGroup(
        proxySettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
          .addGroup(proxySettingsPanelLayout.createSequentialGroup()
            .addGap(19, 19, 19)
            .addComponent(directRadioButton)
            .addPreferredGap(ComponentPlacement.RELATED)
            .addComponent(proxyRadioButton)
            .addGap(18, 18, 18)
            .addGroup(proxySettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
            .addComponent(proxyHostTxtField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
              .addComponent(proxyHostLabel))
              .addGap(14, 14, 14)
              .addGroup(proxySettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
              .addComponent(proxyPortTxtField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
              .addComponent(proxyPortLabel))
              .addContainerGap(38, Short.MaxValue))
      )
      jTabbedPane1.addTab("Network", proxySettingsPanel)
    
      getContentPane().add(jTabbedPane1, BorderLayout.CENTER)
      jTabbedPane1.getAccessibleContext().setAccessibleName("Widnow Settings")

      val bottomPanelLayout = new GroupLayout(bottomPanel)
      bottomPanel setLayout bottomPanelLayout
      bottomPanelLayout.setHorizontalGroup(
        bottomPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
          .addGroup(GroupLayout.Alignment.TRAILING, bottomPanelLayout.createSequentialGroup()
          .addContainerGap(175, Short.MaxValue)
          .addComponent(ok)
          .addGap(14, 14, 14)
          .addComponent(cancel)
          .addContainerGap())
      )
      bottomPanelLayout.setVerticalGroup(
        bottomPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
          .addGroup(bottomPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
          .addComponent(ok, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
          .addComponent(cancel, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE))
      )
      getContentPane.add(bottomPanel, BorderLayout.SOUTH)
      pack()
    }

    def actionPerformed(evt: ActionEvent) {
      val source = evt.getSource
      if (ok == source && proxyRadioButton.isSelected) {
        val hostName = proxyHostTxtField.getText
        val port = proxyPortTxtField.getText
        if (hostName.equals("") || port.equals("")) {
          MainGUIApp.showMessageDialog(this, 
            "Enter the proxy host/port properly...", 
            "Proxy Details Required...")
        } else {
          MainGUIApp.setProxyHost(hostName.trim())
          MainGUIApp.setProxyPort(port.trim())
          System.setProperty("http.proxyHost", hostName.trim())
          System.setProperty("http.proxyPort", port.trim())
          System.setProperty("https.proxyHost", hostName.trim())
          System.setProperty("https.proxyPort", port.trim())
           dispose()
        }
        //dispose()
      } else if( source == ok) {
        dispose()
      }

      if (cancel == source) {
        dispose()
      }
      if (source == directRadioButton) {
        proxyPortTxtField setEnabled false
        proxyHostTxtField setEnabled false
        System.setProperty("http.proxyHost", "");
        System.setProperty("http.proxyPort", "");
      }
            
      if (source == proxyRadioButton){
        proxyPortTxtField.setEnabled(true);
        proxyHostTxtField.setEnabled(true);
      }      
    }

    //Settings Dialog Variables
    private var directRadioButton: JRadioButton = _
    private var proxySettingsPanel: JPanel = _
    private var jTabbedPane1: JTabbedPane = _
    private var proxyHostLabel: JLabel = _
    private var proxyHostTxtField: JTextField = _
    private var proxyPortLabel: JLabel = _
    private var proxyPortTxtField: JTextField = _
    private var proxyRadioButton: JRadioButton = _
    private var bottomPanel: JPanel = _
    private var cancel: JButton = _
    private var ok: JButton = _

  }

  // Variables declaration - do not modify
  private var jMenu1: JMenu = _
  private var jMenu2: JMenu = _
  private var jMenu3: JMenu = _
  private var jMenuBar1: JMenuBar = _
  private var jMenuItem1: JMenuItem = _
  private var jMenuItem2: JMenuItem = _
  private var jButton1: JButton = _
  private var jComboBox1: JComboBox = _
  private var jLabel1: JLabel = _
  private var jLabel2: JLabel = _
  private var jPanel2: JPanel = _
  private var jPanel3: JPanel = _
  private var jTextField1: JTextField = _
  // End of variables declaration
       
}

object MainGUIApp {
  private val baseURL = "https://api.ebay.com/wsapi?"
  private val localURL = "http://localhost:7070/Ebay"
  private val sandboxURL = "https://api.sandbox.ebay.com/wsapi?"

  private val verdana12 = new Font("Verdana", 0, 12)
  private val props = new Properties()
  private var header: CustomSecurityHeaderType = _

  def getItem(endpointToUse: String, itemId: String): ItemType = {
    var error = false
    var endpointURL = ""
    val svc = new EBayAPIInterfaceService()
    val port = svc.getEBayAPI

    val bp = port.asInstanceOf[BindingProvider]
    if (endpointToUse equalsIgnoreCase "ebay") {
      endpointURL = baseURL + "callname=GetItem&siteid=0&appid=" + 
        props.get("appID").asInstanceOf[String] + "&version=455&Routing=new"
      val handler = new RequesterCredentials()
      import scala.collection.JavaConversions._
      bp.getBinding setHandlerChain List(handler)
    }
    else if(endpointToUse equalsIgnoreCase "local") {
      endpointURL = localURL
    }
    else {
      exit(1)
    }
    bp.getRequestContext.put(
      BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointURL)
    val itemRequest = new GetItemRequestType()
    itemRequest setVersion "455"

    itemRequest setItemID itemId
    itemRequest setErrorLanguage "en_US"
    //var itemResponse: GetItemResponseType = null
    try {
      val itemResponse = port getItem itemRequest
      itemResponse.getItem
    } catch {
      case e: Exception => null
    }
  }

  private var proxyHostString: String = _
  private var proxyPortString: String = _

  def setProxyHost(proxyHost: String) { proxyHostString = proxyHost }

  def setProxyPort(proxyPort: String) { proxyPortString = proxyPort }

  def getProxyPort: String = proxyPortString

  def getProxyHost: String = proxyHostString

  def showMessageDialog(parent: Container, msg: String, title: String) {
    SwingUtilities invokeLater new Runnable() {
      def run() {
        JOptionPane.showMessageDialog(
          parent, msg, title, JOptionPane.WARNING_MESSAGE)
      }
    }
  }

  /**
   * @param args the command line arguments
   */
  def main(args: Array[String]) {
    java.awt.EventQueue invokeLater new Runnable() {
      def run() {
        new MainGUIApp() setVisible true
      }
    }
  }

}
