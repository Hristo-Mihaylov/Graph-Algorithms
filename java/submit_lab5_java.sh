#!/bin/bash

# This script will tag your last commit with '<branch-name>_solution' and push this. 
# If a previous tag of this name exists it will be deleted locally and remotely first.
#
# If submission is not working then you can try logging into Gitlab and clearing tags via the interface
# before rerunning this script.
#
# Note that the branch and tag names are important; do not change them.
#
# Author: Giles Reger
# Modified by: Louise Dennis

TAG="lab5_java_solution"

# remove tag if it already exists (locally and remotely)
git tag -d "${TAG}"
git push origin :refs/tags/"${TAG}"

# add tag and push
git tag "${TAG}"
git push
git push origin "${TAG}"

