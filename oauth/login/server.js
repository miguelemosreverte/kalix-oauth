const dotenv = require('dotenv');
const express = require('express');
const http = require('http');
const logger = require('morgan');
const path = require('path');
const router = require('./routes/index');
const { auth } = require('express-openid-connect');

dotenv.load();

const app = express();

app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'ejs');

app.use(logger('dev'));
app.use(express.static(path.join(__dirname, 'public')));
app.use(express.json());

const session = require('express-session');

app.use(session({
  secret: 'your_secret_key',  // You should use a long, random string here
  resave: false,
  saveUninitialized: false,
  cookie: { secure: 'auto', httpOnly: true } // Use 'secure: true' if you are on HTTPS
}));


const grpcClient = require('./grpcClient');

function createCustomer(userDetails) {
  const customerData = {
    customer_id: userDetails.sub,  // Unique identifier for the customer
    email: userDetails.email,
    name: userDetails.name,
    address: {
      street: "TODO userDetails.address.street",
      city: "TODO userDetails.address.city"
    }
  };

  grpcClient.createCustomer(customerData, (error, response) => {
    if (error) {
      console.error('Failed to create customer:', error.message);
      return;
    }
  });
}


const config = {
  authRequired: false,
  auth0Logout: true
};

const port = process.env.PORT || 3000;
if (!config.baseURL && !process.env.BASE_URL && process.env.PORT && process.env.NODE_ENV !== 'production') {
  config.baseURL = `http://localhost:${port}`;
}

app.use(auth(config));

app.use((req, res, next) => {
  // Check if the user is authenticated and if the customer creation flag is not set
  if (req.oidc.isAuthenticated() && !req.session.customerCreated) {

    console.log("Registering user")
    console.log(req.oidc.user)

    createCustomer(req.oidc.user);
    req.session.customerCreated = true;  // Set a flag in the session
    res.locals.user = req.oidc.user;
  }
  next();
});

app.use('/', router);

// Catch 404 and forward to error handler
app.use(function (req, res, next) {
  const err = new Error('Not Found');
  err.status = 404;
  next(err);
});

// Error handlers
app.use(function (err, req, res, next) {
  res.status(err.status || 500);
  res.render('error', {
    message: err.message,
    error: process.env.NODE_ENV !== 'production' ? err : {}
  });
});

http.createServer(app)
  .listen(port, () => {
    console.log(`Listening on ${config.baseURL}`);
  });
