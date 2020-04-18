#!/bin/bash
# Script Buid Image Appfluentd

COMMENT=$1

git add .

echo "add"

git commit -m $COMMENT

echo "commit"

git push  https://'lollome':'$penelope1976'@github.com/lollome/AppFluentd.git


echo "Push!!!"