package com.onscreensync.tvapp

object Constants {
    const val ENDPOINT_BASEURL = "http://mydisplay123point.runasp.net/api/"
    const val ONSCREENSYNC_ENDPOINT_BASEURL = "https://www.onscreensync.com/"

    const val DEVICE_NAME = "deviceName"
    const val CONNECTION_ID = "connectionId"
    const val DEVICE_ID = "deviceId"
    const val TENANT_ID = "tenantId"
    const val CHECKSUM_DATA_KEY = "contentChecksum"
    const val TOKEN_REFRESH_GRANT_TYPE = "refresh_token"
}

object DisplayApiConfigConstants {
    const val BASEURL = "displayApiBaseUrl"
    const val CONTENT_DATA_URL = "CONTENT_DATA_URL"
    const val DEVICE_CODE_URL = "DEVICE_CODE_URL"
    const val DEVICE_INFO_URL = "DEVICE_INFO_URL"
    const val DEVICE_TOKEN_REQUEST_URL = "DEVICE_REFRESH_TOKEN_REQUEST_URL"
    const val DEVICE_REFRESH_TOKEN_REQUEST_URL = "DEVICE_REFRESH_TOKEN_REQUEST_URL"
    const val SIGNALR_ADD_CONNECTION_URL = "SIGNALR_ADD_CONNECTION_URL"
    const val SIGNALR_NEGOTIATION_URL = "SIGNALR_NEGOTIATION_URL"
    const val SIGNALR_REMOVE_CONNECTION_URL = "SIGNALR_REMOVE_CONNECTION_URL"
}