#!/bin/sh
# Run this to deploy a new jar to the server and restart the service
# 1) /etc/init.d/nhlbot should be setup on server
# 2) Your user needs sudo access, and run 'sudo service' with no password
#    sudo visudo and add 'user ALL=NOPASSWD: /usr/bin/service nhlbot *' to bottom of the list
#
# HOW TO RUN:
# deploy.sh [versionNumber] [serverName]
scp target/nhlbot-$1-jar-with-dependencies.jar $2:~
COMMANDS="cd /nhlbot && sudo /bin/mv ~/nhlbot-$1-jar-with-dependencies.jar /nhlbot/builds/nhlbot-$1.jar && sudo /bin/systemctl stop nhlbot && sudo ln -sfn nhlbot-$1.jar builds/nhlbot && sudo /bin/systemctl start nhlbot"
echo "Executing commands on server: $COMMANDS"
ssh $2 $COMMANDS
