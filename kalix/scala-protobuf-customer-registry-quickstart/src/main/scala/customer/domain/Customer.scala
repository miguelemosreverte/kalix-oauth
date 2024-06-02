package customer.domain

import kalix.scalasdk.valueentity.ValueEntity
import kalix.scalasdk.valueentity.ValueEntityContext
import com.google.protobuf.empty.Empty
import customer.api
import customer.util.JwtUtil

class Customer(context: ValueEntityContext) extends AbstractCustomer {

  override def emptyState: CustomerState = CustomerState()

  override def create(currentState: CustomerState, customer: api.Customer): ValueEntity.Effect[api.CreateCustomerResponse] = {
    val state = convertToDomain(customer)
    val token = JwtUtil.createToken(customer.customerId)
    println(s"Generated token for customer ${customer.customerId}: $token")
    effects.updateState(state).thenReply(api.CreateCustomerResponse(token))
  }


  override def getCustomer(currentState: CustomerState, getCustomerRequest: api.GetCustomerRequest): ValueEntity.Effect[api.Customer] = {

    val asMap = commandContext().metadata.jwtClaims.asMap
    println(s"metadata.jwtClaims asMap was ${asMap}")
    val authToken = commandContext().metadata.jwtClaims.subject
    println(s"subject was ${authToken} customerId was ${currentState.customerId} command.commandId was ${commandContext().commandId}")
    authToken match {
      case Some(customerId) if customerId == currentState.customerId =>
        if (currentState.customerId == "") {
          effects.error(s"Customer ${getCustomerRequest.customerId} has not been created.")
        } else {
          effects.reply(convertToApi(currentState))
        }
      case _ => effects.error("Unauthorized")
    }
  }

  def convertToDomain(customer: api.Customer): CustomerState =
    CustomerState(
      customerId = customer.customerId,
      email = customer.email,
      name = customer.name,
      address = customer.address.map(convertToDomain)
    )

  def convertToDomain(address: api.Address): Address =
    Address(
      street = address.street,
      city = address.city
    )

  def convertToApi(customer: CustomerState): api.Customer =
    api.Customer(
      customerId = customer.customerId,
      email = customer.email,
      name = customer.name,
      address = customer.address.map(address => api.Address(address.street, address.city))
    )

  override def changeName(currentState: CustomerState, changeNameRequest: api.ChangeNameRequest): ValueEntity.Effect[Empty] = {
    val authToken = commandContext().metadata.jwtClaims.subject
    authToken match {
      case Some(customerId) if customerId == currentState.customerId =>
        effects.updateState(currentState.copy(name = changeNameRequest.newName)).thenReply(Empty.defaultInstance)
      case _ => effects.error("Unauthorized")
    }
  }

  override def changeAddress(currentState: CustomerState, changeAddressRequest: api.ChangeAddressRequest): ValueEntity.Effect[Empty] = {
    val authToken = commandContext().metadata.jwtClaims.subject
    authToken match {
      case Some(customerId) if customerId == currentState.customerId =>
        effects.updateState(currentState.copy(address = changeAddressRequest.newAddress.map(convertToDomain)))
          .thenReply(Empty.defaultInstance)
      case _ => effects.error("Unauthorized")
    }
  }
}
