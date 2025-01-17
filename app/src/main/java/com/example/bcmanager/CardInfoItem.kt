package com.example.bcmanager

abstract class CardInfoItem {
    class cardInfo(val CARD_NUMBER: Int,
                   val CARD_IMAGE: String, val CARD_NAME: String, val CARD_POSITION: String,
                   val CARD_PHONE: String, val CARD_TEL: String, val CARD_FAX: String,
                   val CARD_ADDRESS: String, val CARD_COMPANY: String, val CARD_EMAIL: String, val CARD_MEMO: String?)

    class detectedCardInfo(val CARD_IMAGE: String, val CARD_NAME: String, val CARD_POSITION: String,
                   val CARD_PHONE: String, val CARD_TEL: String, val CARD_FAX: String,
                   val CARD_ADDRESS: String, val CARD_COMPANY: String, val CARD_EMAIL: String, val CARD_MEMO: String?)

    //    CardOCR.ph = ""
//    CardOCR.nm = ""
//    CardOCR.ad = ""
//    CardOCR.em = ""
//    CardOCR.nb = ""
//    CardOCR.fx = ""
//    CardOCR.po = ""
//    CardOCR.cp = ""
//    CardOCR.memo = null
//    class cardsInfo(val USER_NUMBER: Int, val USER_ID: String, val CARD_NUMBER: Int,
//                   val CARD_IMAGE: String, val CARD_NAME: String, val CARD_POSITION: String,
//                   val CARD_PHONE: String, val CARD_TEL: String, val CARD_FAX: String,
//                   val CARD_ADDRESS: String, val CARD_COMPANY: String, val CARD_EMAIL: String)
}