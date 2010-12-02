/*
 * Ebay.scala
 *
 * Adapted from Ebay.java
 *
 */

package ebay.server

import ebay.apis._
import javax.jws.WebService

@WebService(serviceName = "eBayAPIInterfaceService", portName = "eBayAPI", endpointInterface = "ebay.apis.EBayAPIInterface", targetNamespace = "urn:ebay:apis:eBLBaseComponents", wsdlLocation = "META-INF/wsdl/eBaySvc.wsdl")
class Ebay extends EBayAPIInterface with Items {

  private val items = loadItems

  def addDispute(req: AddDisputeRequestType): AddDisputeResponseType = null

  def addDisputeResponse(req: AddDisputeResponseRequestType): AddDisputeResponseResponseType = null

  def addItem(req: AddItemRequestType): AddItemResponseType = null

  def addLiveAuctionItem(req: AddLiveAuctionItemRequestType): AddLiveAuctionItemResponseType = null

  def addMemberMessage(req: AddMemberMessageRequestType): AddMemberMessageResponseType = null

  def addMemberMessagesAAQToBidder(req: AddMemberMessagesAAQToBidderRequestType): AddMemberMessagesAAQToBidderResponseType = null

  def addOrder(req: AddOrderRequestType): AddOrderResponseType = null

  def addSecondChanceItem(req: AddSecondChanceItemRequestType): AddSecondChanceItemResponseType = null

  def addToItemDescription(req: AddToItemDescriptionRequestType): AddToItemDescriptionResponseType = null

  def addToWatchList(req: AddToWatchListRequestType): AddToWatchListResponseType = null

  def approveLiveAuctionBidders(req: ApproveLiveAuctionBiddersRequestType): ApproveLiveAuctionBiddersResponseType = null

  def completeSale(req: CompleteSaleRequestType): CompleteSaleResponseType = null

  def deleteMyMessages(req: DeleteMyMessagesRequestType): DeleteMyMessagesResponseType = null

  def endItem(req: EndItemRequestType): EndItemResponseType = null

  def fetchToken(req: FetchTokenRequestType): FetchTokenResponseType = null

  def getAccount(req: GetAccountRequestType): GetAccountResponseType = null

  def getAdFormatLeads(req: GetAdFormatLeadsRequestType): GetAdFormatLeadsResponseType = null

  def getAllBidders(req: GetAllBiddersRequestType): GetAllBiddersResponseType = null

  def getApiAccessRules(req: GetApiAccessRulesRequestType): GetApiAccessRulesResponseType = null

  def getAttributesCS(req: GetAttributesCSRequestType): GetAttributesCSResponseType = null

  def getAttributesXSL(req: GetAttributesXSLRequestType): GetAttributesXSLResponseType = null

  def getBestOffers(req: GetBestOffersRequestType): GetBestOffersResponseType = null

  def getBidderList(req: GetBidderListRequestType): GetBidderListResponseType = null

  def getCategories(req: GetCategoriesRequestType): GetCategoriesResponseType = null

  def getCategory2CS(req: GetCategory2CSRequestType): GetCategory2CSResponseType = null

  def getCategory2FinanceOffer(req: GetCategory2FinanceOfferRequestType): GetCategory2FinanceOfferResponseType = null

  def getCategoryFeatures(req: GetCategoryFeaturesRequestType): GetCategoryFeaturesResponseType = null

  def getCategoryListings(req: GetCategoryListingsRequestType): GetCategoryListingsResponseType = null

  def getCategoryMappings(req: GetCategoryMappingsRequestType): GetCategoryMappingsResponseType = null

  def getCharities(req: GetCharitiesRequestType): GetCharitiesResponseType = null

  def getCrossPromotions(req: GetCrossPromotionsRequestType): GetCrossPromotionsResponseType = null

  def getDescriptionTemplates(req: GetDescriptionTemplatesRequestType): GetDescriptionTemplatesResponseType = null

  def getDispute(req: GetDisputeRequestType): GetDisputeResponseType = null

  def getFeedback(req: GetFeedbackRequestType): GetFeedbackResponseType = null

  def getFinanceOffers(req: GetFinanceOffersRequestType): GetFinanceOffersResponseType = null

  def getHighBidders(req: GetHighBiddersRequestType): GetHighBiddersResponseType = null

  def getItem(req: GetItemRequestType): GetItemResponseType = {
    val response = new GetItemResponseType()
//  val item = items("9720685987")
    val item = items(req.getItemID)
    response setItem item
    response
  }

  def getItemRecommendations(req: GetItemRecommendationsRequestType): GetItemRecommendationsResponseType = null

  def getItemShipping(req: GetItemShippingRequestType): GetItemShippingResponseType = null

  def getItemTransactions(req: GetItemTransactionsRequestType): GetItemTransactionsResponseType = null

  def getItemsAwaitingFeedback(req: GetItemsAwaitingFeedbackRequestType): GetItemsAwaitingFeedbackResponseType = null

  def getLiveAuctionBidders(req: GetLiveAuctionBiddersRequestType): GetLiveAuctionBiddersResponseType = null

  def getLiveAuctionCatalogDetails(req: GetLiveAuctionCatalogDetailsRequestType): GetLiveAuctionCatalogDetailsResponseType = null

  def getMemberMessages(req: GetMemberMessagesRequestType): GetMemberMessagesResponseType = null

  def getMyMessages(req: GetMyMessagesRequestType): GetMyMessagesResponseType = null

  def getMyeBay(req: GetMyeBayRequestType): GetMyeBayResponseType = null

  def getMyeBayBuying(req: GetMyeBayBuyingRequestType): GetMyeBayBuyingResponseType = null

  def getMyeBayReminders(req: GetMyeBayRemindersRequestType): GetMyeBayRemindersResponseType = null

  def getMyeBaySelling(req: GetMyeBaySellingRequestType): GetMyeBaySellingResponseType = null

  def getNotificationPreferences(req: GetNotificationPreferencesRequestType): GetNotificationPreferencesResponseType = null

  def getNotificationsUsage(req: GetNotificationsUsageRequestType): GetNotificationsUsageResponseType = null

  def getOrderTransactions(req: GetOrderTransactionsRequestType): GetOrderTransactionsResponseType = null

  def getOrders(req: GetOrdersRequestType): GetOrdersResponseType = null

  def getPictureManagerDetails(req: GetPictureManagerDetailsRequestType): GetPictureManagerDetailsResponseType = null

  def getPictureManagerOptions(req: GetPictureManagerOptionsRequestType): GetPictureManagerOptionsResponseType = null

  def getPopularKeywords(req: GetPopularKeywordsRequestType): GetPopularKeywordsResponseType = null

  def getProductFamilyMembers(req: GetProductFamilyMembersRequestType): GetProductFamilyMembersResponseType = null

  def getProductFinder(req: GetProductFinderRequestType): GetProductFinderResponseType = null

  def getProductFinderXSL(req: GetProductFinderXSLRequestType): GetProductFinderXSLResponseType = null

  def getProductSearchPage(req: GetProductSearchPageRequestType): GetProductSearchPageResponseType = null

  def getProductSearchResults(req: GetProductSearchResultsRequestType): GetProductSearchResultsResponseType = null

  def getProductSellingPages(req: GetProductSellingPagesRequestType): GetProductSellingPagesResponseType = null

  def getPromotionRules(req: GetPromotionRulesRequestType): GetPromotionRulesResponseType = null

  def getRecommendations(req: GetRecommendationsRequestType): GetRecommendationsResponseType = null

  def getReturnURL(req: GetReturnURLRequestType): GetReturnURLResponseType = null

  def getRuName(req: GetRuNameRequestType): GetRuNameResponseType = null

  def getSearchResults(req: GetSearchResultsRequestType): GetSearchResultsResponseType = null

  def getSellerEvents(req: GetSellerEventsRequestType): GetSellerEventsResponseType = null

  def getSellerList(req: GetSellerListRequestType): GetSellerListResponseType = null

  def getSellerPayments(req: GetSellerPaymentsRequestType): GetSellerPaymentsResponseType = null

  def getSellerTransactions(req: GetSellerTransactionsRequestType): GetSellerTransactionsResponseType = null

  def getStore(req: GetStoreRequestType): GetStoreResponseType = null

  def getStoreCategoryUpdateStatus(req: GetStoreCategoryUpdateStatusRequestType): GetStoreCategoryUpdateStatusResponseType = null

  def getStoreCustomPage(req: GetStoreCustomPageRequestType): GetStoreCustomPageResponseType = null

  def getStoreOptions(req: GetStoreOptionsRequestType): GetStoreOptionsResponseType = null

  def getStorePreferences(req: GetStorePreferencesRequestType): GetStorePreferencesResponseType = null

  def getSuggestedCategories(req: GetSuggestedCategoriesRequestType): GetSuggestedCategoriesResponseType = null

  def getTaxTable(req: GetTaxTableRequestType): GetTaxTableResponseType = null

  def getUser(req: GetUserRequestType): GetUserResponseType = null

  def getUserContactDetails(req: GetUserContactDetailsRequestType): GetUserContactDetailsResponseType = null

  def getUserDisputes(req: GetUserDisputesRequestType): GetUserDisputesResponseType = null

  def getUserPreferences(req: GetUserPreferencesRequestType): GetUserPreferencesResponseType = null

  def getWantItNowPost(req: GetWantItNowPostRequestType): GetWantItNowPostResponseType = null

  def getWantItNowSearchResults(req: GetWantItNowSearchResultsRequestType): GetWantItNowSearchResultsResponseType = null

  def geteBayDetails(req: GeteBayDetailsRequestType): GeteBayDetailsResponseType = null

  def geteBayOfficialTime(req: GeteBayOfficialTimeRequestType): GeteBayOfficialTimeResponseType = null

  def issueRefund(req: IssueRefundRequestType): IssueRefundResponseType = null

  def leaveFeedback(req: LeaveFeedbackRequestType): LeaveFeedbackResponseType = null

  def placeOffer(req: PlaceOfferRequestType): PlaceOfferResponseType = null

  def relistItem(req: RelistItemRequestType): RelistItemResponseType = null

  def removeFromWatchList(req: RemoveFromWatchListRequestType): RemoveFromWatchListResponseType = null

  def respondToBestOffer(req: RespondToBestOfferRequestType): RespondToBestOfferResponseType = null

  def respondToFeedback(req: RespondToFeedbackRequestType): RespondToFeedbackResponseType = null

  def respondToWantItNowPost(req: RespondToWantItNowPostRequestType): RespondToWantItNowPostResponseType = null

  def reviseCheckoutStatus(req: ReviseCheckoutStatusRequestType): ReviseCheckoutStatusResponseType = null

  def reviseItem(req: ReviseItemRequestType): ReviseItemResponseType = null

  def reviseLiveAuctionItem(req: ReviseLiveAuctionItemRequestType): ReviseLiveAuctionItemResponseType = null

  def reviseMyMessages(req: ReviseMyMessagesRequestType): ReviseMyMessagesResponseType = null

  def reviseMyMessagesFolders(req: ReviseMyMessagesFoldersRequestType): ReviseMyMessagesFoldersResponseType = null

  def sellerReverseDispute(req: SellerReverseDisputeRequestType): SellerReverseDisputeResponseType = null

  def sendInvoice(req: SendInvoiceRequestType): SendInvoiceResponseType = null

  def setNotificationPreferences(req: SetNotificationPreferencesRequestType): SetNotificationPreferencesResponseType = null

  def setPictureManagerDetails(req: SetPictureManagerDetailsRequestType): SetPictureManagerDetailsResponseType = null

  def setPromotionRules(req: SetPromotionRulesRequestType): SetPromotionRulesResponseType = null

  def setReturnURL(req: SetReturnURLRequestType): SetReturnURLResponseType = null

  def setStore(req: SetStoreRequestType): SetStoreResponseType = null

  def setStoreCategories(req: SetStoreCategoriesRequestType): SetStoreCategoriesResponseType = null

  def setStoreCustomPage(req: SetStoreCustomPageRequestType): SetStoreCustomPageResponseType = null

  def setStorePreferences(req: SetStorePreferencesRequestType): SetStorePreferencesResponseType = null

  def setTaxTable(req: SetTaxTableRequestType): SetTaxTableResponseType = null

  def setUserNotes(req: SetUserNotesRequestType): SetUserNotesResponseType = null

  def setUserPreferences(req: SetUserPreferencesRequestType): SetUserPreferencesResponseType = null

  def validateTestUserRegistration(req: ValidateTestUserRegistrationRequestType): ValidateTestUserRegistrationResponseType = null

  def verifyAddItem(req: VerifyAddItemRequestType): VerifyAddItemResponseType = null

  def verifyAddSecondChanceItem(req: VerifyAddSecondChanceItemRequestType): VerifyAddSecondChanceItemResponseType = null

}
