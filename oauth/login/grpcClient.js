const { exec } = require("child_process");

/**
 * Creates a customer using grpcurl.
 * @param {Object} userDetails - User details object.
 * @param {function} callback - A callback function to handle the response.
 */
function createCustomer(userDetails, callback) {
  /**
   * Executes a grpcurl command and returns the result via a callback.
   * @param {string} data - The JSON string data to be sent as the gRPC request body.
   * @param {function} callback - A callback function that handles the command response.
   */
  function executeGrpcUrl(userDetails, callback) {
    const customer_id = encodeURIComponent(userDetails.customer_id);
    const command = `grpcurl -plaintext -d '${JSON.stringify({
      customer_id: customer_id,
      email: userDetails.email,
      name: userDetails.name,
      address: {
        street: "userDetails.address.street",
        city: "userDetails.address.city",
      },
    })}' localhost:9000 customer.api.CustomerService/Create`;

    console.log("Executing command:", command); // Log the command to verify it

    exec(command, (error, stdout, stderr) => {
      if (error) {
        return callback(error, null);
      }
      if (stderr) {
        console.error(`Execution Stderr: ${stderr}`);
        return callback(new Error(stderr), null);
      }
      console.log(`Execution Stdout: ${stdout}`);
      console.log(
        "You can check the user has been registered to Kalix using the following command:"
      );
      console.log(
        `
        grpcurl -d '{"customer_id": "${customer_id}"}' --plaintext localhost:9000 customer.api.CustomerService/GetCustomer
        `
      );
      callback(null, stdout);
    });
  }
  executeGrpcUrl(userDetails, callback);
}

module.exports = { createCustomer };
