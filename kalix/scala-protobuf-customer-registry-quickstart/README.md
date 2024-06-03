# Customer Registry

## Designing

To understand the Kalix concepts that are the basis for this example, see [Designing services](https://docs.kalix.io/developing/development-process-proto.html) in the documentation.

## Developing

This project demonstrates the use of Value Entity and View components.
To understand more about these components, see [Developing services](https://docs.kalix.io/services/)
and in particular the [Scala section](https://docs.kalix.io/java/)

## Building and running unit tests

To compile and test the code from the command line, use

```shell
sbt test
```

## Running Locally

When running a Kalix service locally, we need to have its companion Kalix Runtime running alongside it.

To start your service locally, run:

```shell
sbt runAll
```

This command will start your Kalix service and a companion Kalix Runtime as configured in [docker-compose.yml](./docker-compose.yml) file.

For further details see [Running a service locally](https://docs.kalix.io/developing/running-service-locally.html) in the documentation.

## Exercise the service

With both the Kalix Runtime and your service running, any defined endpoints should be available at `http://localhost:9000`. In addition to the defined gRPC interface, each method has a corresponding HTTP endpoint. Unless configured otherwise (see [Transcoding HTTP](https://docs.kalix.io/java-protobuf/writing-grpc-descriptors-protobuf.html#_transcoding_http)), this endpoint accepts POST requests at the path `/[package].[entity name]/[method]`. For example, using `curl`.

* Create a customer with:

```shell
grpcurl --plaintext -d '{"customer_id": "wip", "email": "wip@example.com", "name": "Very Important", "address": {"street": "Road 1", "city": "The Capital"}}' purple-flower-1819.us-east1.kalix.app:9000 customer.api.CustomerService/Create
```
> {
"token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJjdXN0b21lcklkIjoid2lwIn0.dbncor4CviWPTA1119FDsk6PHLWX4dG_jsRRecPiOhM"
}

* Retrieve the customer using the token:

```shell
 grpcurl --plaintext -H "Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ3aXAifQ.uXWBhRu0WHspLvlpn75hniQzXnX-eQRBMmHh58Qr9FQ" -d '{"customer_id": "wip"}' localhost:9000 customer.api.CustomerService/GetCustomer
```

* Then create a second customer with:

```shell
grpcurl --plaintext -d '{"customer_id": "wip2", "email": "wip2@example.com", "name": "Very Important", "address": {"street": "Road 1", "city": "The Capital"}}' localhost:9000 customer.api.CustomerService/Create  
```
> {
"token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ3aXAyIn0.BYhG7mOLoT4h0kSL1TiBPR3-g05OadeocROLyxG9cgI"
}

* Retrieve the second customer using the same token from first customer:

```shell
 grpcurl --plaintext -H "Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ3aXAifQ.uXWBhRu0WHspLvlpn75hniQzXnX-eQRBMmHh58Qr9FQ" -d '{"customer_id": "wip2"}' localhost:9000 customer.api.CustomerService/GetCustomer
```
> ERROR:
    Code: Internal
    Message: Unauthorized

* Retrieve the second customer using the same token from second customer:
```shell
 grpcurl --plaintext -H "Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ3aXAyIn0.BYhG7mOLoT4h0kSL1TiBPR3-g05OadeocROLyxG9cgI" -d '{"customer_id": "wip2"}' localhost:9000 customer.api.CustomerService/GetCustomer
```
> {
"customerId": "wip2",
"email": "wip2@example.com",
"name": "Very Important",
"address": {
"street": "Road 1",
"city": "The Capital"
}
}


* Query by name:

```shell
grpcurl --plaintext -d '{"customer_name": "Very Important"}' localhost:9000 customer.view.CustomerByName/GetCustomers
```

* Change name:

```shell
grpcurl --plaintext -d '{"customer_id": "wip", "new_name": "Most Important"}' localhost:9000 customer.api.CustomerService/ChangeName
```

* Change address:

```shell
grpcurl --plaintext -d '{"customer_id": "wip", "new_address": {"street": "Street 1", "city": "The City"}}' localhost:9000 customer.api.CustomerService/ChangeAddress
```

## Deploying

To deploy your service, install the `kalix` CLI as documented in
[Install Kalix](https://docs.kalix.io/kalix/install-kalix.html)
and configure a Docker Registry to upload your docker image to.

You will need to set the `docker.username` system property when starting sbt to be able to publish the image, for example `sbt -Ddocker.username=myuser Docker/publish`.

If you are publishing to a different registry than docker hub, you will also need to specify what registry using the system property `docker.registry`.

Refer to
[Configuring registries](https://docs.kalix.io/projects/container-registries.html)
for more information on how to make your docker image available to Kalix.

Finally, you can use the [Kalix Console](https://console.kalix.io)
to create a Kalix project and then deploy your service into it
through the `kalix` CLI. 

## Auth0

Login to Kalix
```shell
kalix auth login
```
Create a Kalix project
```shell
kalix projects new developer-test-01 "DeveloperTest01" --region=gcp-us-east1 --organization=miguelemosreverte
```
>   NAME                DESCRIPTION       ID                                     OWNER               REGION            
    developer-test-01   DeveloperTest01   d7fb8a8f-ffdf-426d-8754-4a8a210781dc   miguelemosreverte   gcp-us-east1

    'developer-test-01' is now the currently active project.

Create an application in Auth0 so that you can retrieve the .pem like:

```shell
curl https://dev-hrbmijutnc83oxoe.us.auth0.com/pem > auth0.pem
```

Extract the public key:

```shell
openssl x509 -pubkey -noout -in auth0.pem > auth0.pubkey.pem
```

Now we can configure that as a secret in our Kalix app:

```shell
kalix secret create asymmetric auth0 --public-key auth0.pubkey.pem
```
> Secret 'auth0' created.

```shell
docker login
```

```shell
sbt -Ddocker.username=miguelemos Docker/publish
```

```shell
kalix service jwts add jwt-auth0 --key-id auth0 --algorithm RS256 --secret auth0 
```

kalix service jwts add jwt-auth0 --key-id auth0 --algorithm RS256 --secret auth0 --issuer https://dev-hrbmijutnc83oxoe.us.auth0.com/
