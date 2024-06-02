


# kalix+openid


![demostration](https://github.com/miguelemosreverte/kalix-oauth/assets/9152392/f85653ad-cd9b-426f-9e89-ea56dfd7e16a)

This project demonstrates integration between a Node.js application using Auth0 for authentication and a Kalix service managing customer records.

## Prerequisites

- Docker
- Node.js
- npm
- grpcurl (for testing and verification)

## Setup and Running the Project

### Kalix Customer Registry Service

1. **Set up the environment:**
   Navigate to the `kalix/customer-registry` directory:
   ```bash
   cd kalix/customer-registry
   ```

2. **Start the Docker containers:**
   ```bash
   docker-compose up
   ```

3. **Install dependencies:**
   ```bash
   npm install
   ```

4. **Build the service:**
   ```bash
   npm run build
   ```

5. **Start the service:**
   ```bash
   npm start
   ```

### auth0 Node.js Application

1. **Navigate to the auth0 application directory:**
   ```bash
   cd auth0/login
   ```

2. **Install dependencies:**
   ```bash
   npm install
   ```

3. **Start the application:**
   ```bash
   npm start
   ```

   Upon starting, you should see logs indicating that the server is listening on `http://localhost:3000`. Follow the URL to interact with the application.


### Application Logs Overview

When you start and interact with the Node.js auth0 application and the Kalix service, the logs will look like this:

    
    npm start

    > start
    > node server.js

    Using 'form_post' for response_mode may cause issues for you logging in over http, see https://github.com/auth0/express-openid-connect/blob/master/FAQ.md
    Listening on http://localhost:3000
    GET /login 302 95.226 ms - 754
    POST /callback 302 294.176 ms - 86
    Registering user
    {
      sid: 'mFzbI40gw4ME_jZbDhr7T0KU6xx4WueR',
      nickname: 'miguellemos95',
      name: 'miguellemos95@gmail.com',
      picture: 'https://s.gravatar.com/avatar/249f08c87747e539cbb4e4251d907ef2?s=480&r=pg&d=https%3A%2F%2Fcdn.auth0.com%2Favatars%2Fmi.png',
      updated_at: '2024-05-24T01:26:53.984Z',
      email: 'miguellemos95@gmail.com',
      email_verified: true,
      sub: 'auth0|664fcd425d4b3faeb54905c7'
    }
    Executing command: grpcurl -plaintext -d '{"customer_id":"auth0%7C664fcd425d4b3faeb54905c7","email":"miguellemos95@gmail.com","name":"miguellemos95@gmail.com","address":{"street":"userDetails.address.street","city":"userDetails.address.city"}}' localhost:9000 customer.api.CustomerService/Create
    GET / 200 12.691 ms - 2672
    Execution Stdout: {}
    

   You can check the user has been registered to Kalix using the following command:

    
    grpcurl -d '{"customer_id": "auth0%7C664fcd425d4b3faeb54905c7"}' --plaintext localhost:9000 customer.api.CustomerService/GetCustomer
    {
      "customerId": "auth0%7C664fcd425d4b3faeb54905c7",
      "email": "miguellemos95@gmail.com",
      "name": "miguellemos95@gmail.com",
      "address": {
        "street": "userDetails.address.street",
        "city": "userDetails.address.city"
      }
    }
    

## Important Notes

- **Data Security:** Ensure that `.env` files and sensitive configurations are not uploaded to version control.
- **Dependencies:** This project uses Docker to manage the Kalix service dependencies and ensure that the environment is consistent.

## Troubleshooting

- If you encounter issues with `grpcurl` commands, ensure that your command syntax is correct and that `grpcurl` is properly installed and configured on your system.
- Check Docker and Node.js logs for any error messages that may indicate what went wrong.

## Auth0 JWT | [documentation](https://www.kalix.io/developer/blog/integrating-kalix-and-auth0-using-jwts)

- fetched .pem file from https://dev-hrbmijutnc83oxoe.us.auth0.com/pem
 ![Screenshot 2024-05-24 at 04 21 43](https://github.com/miguelemosreverte/kalix-openid/assets/9152392/9601321f-26aa-4d4f-8265-1c2941551b9a)
