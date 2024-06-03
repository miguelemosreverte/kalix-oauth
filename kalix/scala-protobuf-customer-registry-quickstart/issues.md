
# Kalix Project Setup and Deployment Issues

## Overview

This document outlines the issues encountered during the setup and deployment of a Kalix project, along with the steps taken to resolve them.

## Issues Encountered

### 1. Timeouts and Resource Constraints

**Issue:**
- Initial setup attempts experienced timeouts and resource constraints.

**Solution:**
- Migrated the setup to a Google Cloud instance to ensure sufficient resources and network stability.

### 2. Kalix JWT Validation Configuration

**Issue:**
- Error encountered when trying to configure JWT validation for the Kalix service:
  ```
Error reported from Kalix system: KLX-00711 Method [customer.api.CustomerService.GetCustomer] configured to validate a JWT token, however this service has not been configured for JWT validation.

A Kalix Service must have one or more JWT validation secrets configured in order to validate JWT tokens.

See documentation: https://docs.kalix.io/java-protobuf/using-jwts.html
  ```

**Attempted Solution:**
- Tried to download and extract the Auth0 public key, create a JWT secret, and add it to the Kalix service:
  ```bash
  curl https://dev-hrbmijutnc83oxoe.us.auth0.com/pem > auth0.pem
  openssl x509 -pubkey -noout -in auth0.pem > auth0.pubkey.pem
  kalix secret create asymmetric auth0 --public-key auth0.pubkey.pem
  kalix service jwts add jwt-auth0 --key-id auth0 --algorithm RS256 --secret auth0
  ```

**Error Encountered:**
- The Kalix CLI encountered a runtime error when trying to add JWTs:
  ```
  panic: runtime error: invalid memory address or nil pointer dereference
  [signal SIGSEGV: segmentation violation code=0x1 addr=0x130 pc=0x183b1b1]

  goroutine 1 [running]:
  main.(*jwtOptions).addJwt(0xc000100af0, 0xc000827d88?, {0xc000779dc0, 0x1, 0x0?})
          /home/circleci/project/cli/cmd/kalix/jwt.go:183 +0x91
  github.com/spf13/cobra.(*Command).execute(0xc0007e1180, {0xc000779d50, 0x7, 0x7})
          /home/circleci/go/pkg/mod/github.com/spf13/cobra@v1.4.0/command.go:856 +0x67c
  github.com/spf13/cobra.(*Command).ExecuteC(0xc000858500)
          /home/circleci/go/pkg/mod/github.com/spf13/cobra@v1.4.0/command.go:974 +0x3b4
  main.main()
          /home/circleci/project/cli/cmd/kalix/main.go:51 +0x1e
  ```

**Status:**
- No fix available for the segmentation fault error in the Kalix CLI. Further assistance from Kalix support may be required.

## Conclusion

While the migration to a Google Cloud instance resolved the timeout and resource constraints, the Kalix CLI encountered a segmentation fault error when attempting to configure JWT validation. This issue remains unresolved and requires further investigation or support from Kalix.

For more details on JWT configuration, refer to the [Kalix documentation](https://docs.kalix.io/java-protobuf/using-jwts.html).
