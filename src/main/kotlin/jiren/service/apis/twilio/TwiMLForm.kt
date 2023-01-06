package jiren.service.apis.twilio

import com.sun.istack.NotNull
import jiren.data.entity.twilio.Message
import org.json.JSONArray

class TwiMLForm {
    var messageSid: String? = null
    var accountSid: String? = null
    var messagingServiceSid: String? = null
    @NotNull
    var from: String? = null
    var to: String? = null
    var body: String? = null
    var numMedia: String? = null
    var referralNumMedia: String? = null
    var mediaContentType: List<String>? = null
    var mediaUrl: List<String>? = null
    var errorMessage: String? = null
    var price: String? = null
    var direction: String? = null
    var dateUpdated: String? = null
    var uri: String? = null
    var dateSent: String? = null
    var dateCreated: String? = null
    var errorCode: String? = null
    var priceUnit: String? = null
    var apiVersion: String? = null
    var statusCallback: String? = null
    var profileName: String? = null
    var forwarded: String? = null

    fun convert(): Message {
        val message = Message()
        message.messageSid = this.messageSid
        message.fromNumber = this.from
        message.toNumber = this.to
        message.body = this.body
        message.numMedia = this.numMedia?.toInt() ?: 0
        message.referralNumMedia = this.referralNumMedia?.toInt() ?: 0
        message.mediaContentType = JSONArray(this.mediaContentType).toString()
        message.mediaUrl = JSONArray(this.mediaUrl).toString()
        message.errorMessage = this.errorMessage
        message.price = this.price ?: ""
        message.direction = this.direction ?: ""
        message.dateUpdated = this.dateUpdated
        message.uri = this.uri
        message.dateSent = this.dateSent
        message.dateCreated = this.dateCreated
        message.errorCode = this.errorCode
        message.priceUnit = this.priceUnit
        message.apiVersion = this.apiVersion
        message.statusCallback = statusCallback
        message.profileName = this.profileName
        message.forwarded = this.forwarded
        return message
    }

}