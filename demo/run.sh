#!/bin/bash

read -p "***** Remember to have Quarkus app up and running by using quarkus dev command (./mvnw quarkus:dev) *****"

printf "\n"

read -p "* Image before"

./imgcat ./kid.jpg

printf "\n"

read -p "* Image after"

curl -s -X POST localhost:8080/cover -H 'Content-Type: multipart/form-data' -F 'picture=@./kid.jpg' > tmp.png

./imgcat ./tmp.png

