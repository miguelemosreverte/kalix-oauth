import { Kalix } from "@kalix-io/kalix-javascript-sdk";
import generatedComponents from "../lib/generated/index.js";
import express from 'express';
import http from 'http';
import logger from 'morgan';
import path from 'path';

const kalixServer = new Kalix();
const app = express();

// This generatedComponents array contains all generated Actions, Views or Entities,
// and is kept up-to-date with any changes in your protobuf definitions.
// If you prefer, you may remove this line and manually register these components.
generatedComponents.forEach((component) => {
  kalixServer.addComponent(component);
});

app.use(logger('dev'));
app.use(express.static(path.join(__dirname, 'public')));
app.use(express.json());

// Simple "Hello World" route
app.get('/', (req, res) => {
  res.send('<h1>Hello World</h1>');
});

const expressServer = http.createServer(app);
const port = process.env.PORT || 3000;

expressServer.listen(port, () => {
  console.log(`Express server listening on http://localhost:${port}`);
});

// Start Kalix server on a different port
kalixServer.start({
  httpPort: 9000
});
