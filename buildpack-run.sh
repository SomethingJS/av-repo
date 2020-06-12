#!/bin/bash
if [ -d ".git" ]; then # After compile remove .git (as it will be replaced) and delete everything else with exception after $. Get website except wrong Procfile for this build
  mv plugins/ ../plugins/ # Save the plugins folder, below methode doesn't cover folders.
  rm -r .git/ & rm -rf $(find * -name "*" ! -name "av.jar" ! -name "avbotapache2.conf" ! -name "Procfile" ! -name "update.sh")
  mv ../plugins/ . # Save the plugins folder placing it back
  git clone https://github.com/av/website && rm website/Procfile && mv website/{.[!.],}* . && rm -r website/ # Example: av/website
  java -jar av.jar --generate-json-file && mv commandMap.json storage/commandMap.json # Get needed configs and move command file to right directory
else # If av.jar isn't compiled
  git clone https://github.com/av/av && mv av/.git . # Example: av/av
fi # The third time the build-run.sh is ran, it won't be this script but the one over at the website repo!
