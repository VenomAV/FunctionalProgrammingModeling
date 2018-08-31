package Expenses.Model

import java.util.UUID

import Expenses.Utils.ErrorManagement
import Expenses.Utils.ErrorManagement.Validated
import cats.implicits._
import Employee.implicits._

sealed case class Employee private (id : EmployeeId, name: String, surname: String)

sealed case class EmployeeId(uuid: UUID)

object Employee {
  private val validateId = ErrorManagement.notNull[EmployeeId]("id is null")(_)
  private val validateName = ErrorManagement.notEmptyString("name is empty")(_)
  private val validateSurname = ErrorManagement.notEmptyString("surname is empty")(_)

  def create(id: EmployeeId, name: String, surname: String) : Validated[Employee] =
    (validateId(id), validateName(name), validateSurname(surname))
      .mapN(new Employee(_, _, _))

  def create(name: String, surname: String) : Validated[Employee] =
    create(UUID.randomUUID(), name, surname)

  object implicits {
    import scala.language.implicitConversions

    implicit def uuidToEmployeeId(uuid: UUID) : EmployeeId = EmployeeId(uuid)
  }
}