#!/bin/bash

# Variables
AWS_REGION="us-east-1"
AWS_ACCOUNT_ID="851725277517"
REPO_NAME="cc4p1"

# Authenticate Docker to ECR
aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com

# Build Docker images
docker build -t $REPO_NAME:python-worker -f Dockerfile.python-worker .
docker build -t $REPO_NAME:js-worker -f Dockerfile.js-worker .
docker build -t $REPO_NAME:java-worker -f Dockerfile.java-worker .

# Tag Docker images
docker tag $REPO_NAME:python-worker $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$REPO_NAME:python-worker
docker tag $REPO_NAME:js-worker $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$REPO_NAME:js-worker
docker tag $REPO_NAME:java-worker $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$REPO_NAME:java-worker

# Push Docker images to ECR
docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$REPO_NAME:python-worker
docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$REPO_NAME:js-worker
docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$REPO_NAME:java-worker
