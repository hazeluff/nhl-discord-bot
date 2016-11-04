#!/bin/sh
# Run this to deploy a new jar to the server and restart the service
# 1) /etc/init.d/canucksbot should be setup on server
# 2) Your user needs sudo access, and run 'sudo service' with no password
#    sudo visudo and add 'user ALL=NOPASSWD: /usr/bin/service canucksbot *' to bottom of the list
#
# HOW TO RUN:
# deploy.sh [versionNumber] [serverName]
scp target/canucksbot-$1-jar-with-dependencies.jar $2:~
COMMANDS="cd /opt/canucksbot && sudo /bin/mv ~/canucksbot-$1-jar-with-dependencies.jar /opt/canucksbot/builds/canucksbot-$1.jar && sudo /usr/bin/service canucksbot stop && ln -sfn builds/canucksbot-$1.jar canucksbot && sudo /usr/bin/service canucksbot start"
echo "Executing commands on server: $COMMANDS"
ssh $2 $COMMANDS
