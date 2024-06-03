#!/bin/bash

# Update and install dependencies
sudo apt-get update -y
sudo apt-get upgrade -y
sudo apt-get install -y \
    apt-transport-https \
    ca-certificates \
    curl \
    software-properties-common \
    gnupg2 \
    unzip \
    zip \
    wget

# Install Docker
curl -fsSL https://download.docker.com/linux/debian/gpg | sudo apt-key add -
sudo add-apt-repository \
   "deb [arch=amd64] https://download.docker.com/linux/debian \
   $(lsb_release -cs) \
   stable"

sudo apt-get update -y
sudo apt-get install -y docker-ce docker-ce-cli containerd.io

# Start and enable Docker service
sudo systemctl start docker
sudo systemctl enable docker

# Add the current user to the docker group
sudo usermod -aG docker $USER

# Apply the new group membership without logout
newgrp docker

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/download/1.29.2/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
sudo ln -s /usr/local/bin/docker-compose /usr/bin/docker-compose

# Install SDKMAN!
curl -s "https://get.sdkman.io" | bash

# Source SDKMAN!
source "$HOME/.sdkman/bin/sdkman-init.sh"

# Install OpenJDK (Java) 21.0.3 from Amazon Corretto using SDKMAN!
sdk install java 21.0.3-amzn

# Install SBT using SDKMAN!
sdk install sbt

# Install Git
sudo apt-get install -y git

# Install Kalix CLI to a directory under the user's home directory
curl -sL https://docs.kalix.io/install-cli.sh | bash -s -- --prefix $HOME/.local/bin

# Add the local bin to PATH if it's not already there
export PATH=$HOME/.local/bin:$PATH

# Log in to Kalix CLI
kalix auth login --no-launch-browser

# Configure Kalix container registry with --disable-prompt
kalix auth container-registry configure --disable-prompt

# Clone the repository
git clone https://github.com/miguelemosreverte/kalix-openid
cd kalix-openid/kalix/scala-protobuf-customer-registry-quickstart

# Set JAVA_HOME to use the installed Java version
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk use java 21.0.3-amzn

# Build and tag the Docker image
sbt docker:publishLocal

# Ensure Docker is logged in to the Kalix container registry
docker login kcr.us-east-1.kalix.io

# Tag the Docker image with the desired name
docker tag kcr.us-east-1.kalix.io/miguelemosreverte/developer-test-01:latest kcr.us-east-1.kalix.io/miguelemos/developer-test-01:latest

# Push the Docker image to the Kalix container registry
docker push kcr.us-east-1.kalix.io/miguelemosreverte/developer-test-01:latest

# Install grpcurl
wget https://github.com/fullstorydev/grpcurl/releases/download/v1.8.7/grpcurl_1.8.7_linux_x86_64.tar.gz
tar -xvf grpcurl_1.8.7_linux_x86_64.tar.gz
sudo mv grpcurl /usr/local/bin/

# Deploy the service to Kalix
kalix service deploy developer-test-01 kcr.us-east-1.kalix.io/miguelemosreverte/developer-test-01:latest

# Expose the service
kalix services expose developer-test-01

echo "Setup and Docker image publish process completed successfully."

# Test the service
grpcurl --plaintext -d '{"customer_id": "wip", "email": "wip@example.com", "name": "Very Important", "address": {"street": "Road 1", "city": "The Capital"}}' purple-flower-1819.us-east1.kalix.app:9000 customer.api.CustomerService/Create
