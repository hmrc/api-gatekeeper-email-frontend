#!/bin/bash

sbt "run -Drun.mode=Dev -Dhttp.port=9692  -Dapplication.router=testOnlyDoNotUseInAppConf.Routes $*"
