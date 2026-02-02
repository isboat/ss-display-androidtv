package com.onscreensync.tvapp.apirequests

data class CodeActivationRequestBody(
    var clientId: String,
    var grantType: String
)
