#!/bin/sh
# Run this to deploy a new jar to the server and restart the service
# 1) /etc/init.d/canucksbot should be setup on server
# 2) canucksbot user should be created on server
# 3) ssh keys need to be created    
#    canucksbot user should have public ssh key in ~/.ssh/authorized_keys
#    canucksbot private key should be at ~/.ssh/canucksbot
#    sudo visudo and add 'canucksbot ALL=NOPASSWD: /usr/bin/service canucksbot *' to bottom of the list
# deploy.sh [versionNumber] [serverName]
scp -i ~/.ssh/canucksbot target/canucksbot-$1-jar-with-dependencies.jar canucksbot@$2:~/builds
COMMANDS="mv builds/canucksbot-$1-jar-with-dependencies.jar builds/canucksbot-$1.jar && sudo /usr/bin/service canucksbot stop && ln -sfn builds/canucksbot-$1.jar canucksbot && sudo /usr/bin/service canucksbot start"
echo $COMMANDS
ssh -i ~/.ssh/canucksbot canucksbot@$2 $COMMANDS
