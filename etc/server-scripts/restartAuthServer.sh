#!/bin/bash

/bin/sh /usr/share/timadorus-auth-server/server-scripts/stopAuthServer.sh
/bin/sleep 3
/bin/sh /usr/share/timadorus-auth-server/server-scripts/startAuthServer.sh

