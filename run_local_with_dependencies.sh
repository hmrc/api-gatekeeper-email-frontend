#!/bin/bash

sm --start ASSETS_FRONTEND -r 3.12.0

sm --start DEVELOPER_EMAIL_RENDERER EMAIL GATEKEEPER_EMAIL
sm --start DATASTREAM AUTH AUTH_LOGIN_API AUTH_LOGIN_STUB TIME_BASED_ONE_TIME_PASSWORD STRIDE_AUTH_FRONTEND STRIDE_AUTH STRIDE_IDP_STUB  USER_DETAILS

#sm --start API_PUBLISHER 


./run_local.sh
