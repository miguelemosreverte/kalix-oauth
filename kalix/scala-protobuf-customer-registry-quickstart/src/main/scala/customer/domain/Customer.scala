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
    if (!authenticated(currentState.customerId)) return effects.error("Unauthorized")
    effects.reply(convertToApi(currentState))
  }

  override def changeName(currentState: CustomerState, changeNameRequest: api.ChangeNameRequest): ValueEntity.Effect[Empty] = {
    if (!authenticated(currentState.customerId)) return effects.error("Unauthorized")
     effects.updateState(currentState.copy(name = changeNameRequest.newName))
     .thenReply(Empty.defaultInstance)
  }

  override def changeAddress(currentState: CustomerState, changeAddressRequest: api.ChangeAddressRequest): ValueEntity.Effect[Empty] = {
    if (!authenticated(currentState.customerId)) return effects.error("Unauthorized")
     effects.updateState(currentState.copy(address = changeAddressRequest.newAddress.map(convertToDomain)))
     .thenReply(Empty.defaultInstance)
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

  private def jwtCustomerId = commandContext().metadata.jwtClaims.subject.getOrElse("")
  private def authenticated(customerId: String): Boolean =
    jwtCustomerId == customerId
}
